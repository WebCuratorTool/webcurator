/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.webcurator.ui.tools.controller;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.store.DigitalAssetStore;
import org.webcurator.domain.TargetInstanceDAO;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.ui.tools.command.BrowseCommand;

/**
 * The BrowseController is responsible for handling the Browse Quality Review
 * tool. This controller is responsible for reading the requested resource from
 * the DigitalAssetStore and performing any replacements that have been
 * registered with the BrowseHelper through the Spring configuration.
 *
 * @author bbeaumont
 */
@Controller
public class BrowseController {
    /**
     * Logger for the BrowseController.
     **/
    private static final Log log = LogFactory.getLog(BrowseController.class);

    /**
     * The BrowseHelper handles replacement of URLs in the resources.
     **/
    @Autowired
    private BrowseHelper browseHelper;

    /**
     * The QualityReviewFacade for this controller.
     **/
    @Autowired
    private DigitalAssetStore digitalAssetStore = null;

    @Autowired
    private TargetInstanceDAO targetInstanceDao;

    private final int MAX_MEMORY_SIZE = 1024 * 1024;
    // private final int MAX_MEMORY_SIZE = 0;

    /**
     * The buffer size for reading from the file.
     */
    private static final int BYTE_BUFFER_SIZE = 1024 * 8;

    private static final Pattern p = Pattern.compile("\\/(\\d+)\\/(.*)");

    private static final Pattern CHARSET_PATTERN = Pattern.compile(";\\s+charset=([A-Za-z0-9].[A-Za-z0-9_\\-\\.:]*)");

    private static final Charset CHARSET_LATIN_1 = StandardCharsets.UTF_8; // StandardCharsets.ISO_8859_1;

    /**
     * Map containing tokens (eg: browser redirect fragment) that will be
     * replaced during the browser helper fix operation
     */
    private static Map<String, String> fixTokens = null;

    @PostConstruct
    public void initialize() {
        if (fixTokens == null) {
            fixTokens = new HashMap<>();
            fixTokens.put("top.location", "//top.location");
            fixTokens.put("window.location", "//window.location");
            // Ensure that meta refresh redirect to the root path "/" is replaced by a relative path "./"
            fixTokens.put("http-equiv=&quot;refresh&quot; content=&quot;0; url=/", "http-equiv=&quot;refresh&quot; content=&quot;0; url=./");
        }
    }

    /**
     * Sets the BrowseHelper for the controller. This is primarily called from
     * the Spring configuration.
     *
     * @param browseHelper The browseHelper that the controller should use.
     */
    public void setBrowseHelper(BrowseHelper browseHelper) {
        this.browseHelper = browseHelper;
    }

    /**
     * Default constructor.
     */
    public BrowseController() {
    }

    private String getHeaderValue(List<Header> headers, String key) {
        if (headers != null) {
            for (Header h : headers) {
                if (key.equalsIgnoreCase(h.getName())) {
                    return h.getValue().trim();
                }
            }
        }
        return null;
    }


    /**
     * Get everything before the semi-colon.
     *
     * @param realContentType The full content type from the Heritrix ARC file.
     * @return The part of the content type before the semi-colon.
     */
    private String getSimpleContentType(String realContentType) {
        return (realContentType == null || realContentType.indexOf(';') < 0) ? realContentType
                : realContentType.substring(0, realContentType.indexOf(';'));

    }


    /**
     * The handle method is the entry method into the browse controller.
     */
//    @RequestMapping(path = "/curator/tools/browse/{hrOid}/**", method = {RequestMethod.POST, RequestMethod.GET})
    protected ModelAndView handle(@PathVariable("hrOid") Long hrOid, HttpServletRequest req, HttpServletResponse res) throws Exception {
        // Build a command with the items from the URL.
        BrowseCommand command = new BrowseCommand();
        command.setHrOid(hrOid);
        String prefix = req.getContextPath() + String.format("/curator/tools/browse/%d/", hrOid);

        String queryString = req.getQueryString();
        if (queryString != null && queryString.startsWith("url=")) {
            command.setResource(BrowseHelper.decodeUrl(queryString.substring("url=".length())));
        } else {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

        String base = req.getContextPath() + req.getServletPath();
        String line = req.getRequestURI().substring(base.length());

        Matcher matcher = p.matcher(line);
        if (matcher.matches()) {
            command.setHrOid(Long.parseLong(matcher.group(1)));
            command.setResource(matcher.group(2));
        }

        // Check if the command is prefixed with a forward slash.
        if (command.getResource().startsWith("/")) {
            command.setResource(command.getResource().substring(1));
        }

        // Now make sure that the domain name is in lowercase.
        Pattern urlBreakerPattern = Pattern.compile("(.*?)://(.*?)/(.*)");
        Matcher urlBreakerMatcher = urlBreakerPattern.matcher(command.getResource());
        if (urlBreakerMatcher.matches()) {
            command.setResource(urlBreakerMatcher.group(1) + "://"
                    + urlBreakerMatcher.group(2).toLowerCase() + "/"
                    + urlBreakerMatcher.group(3));
        }

        // Load the HarvestResourceDTO from the quality review facade.
        HarvestResult hr = targetInstanceDao.getHarvestResult(command.getHrOid());
        if (hr == null) {        // If the resource is not found, go to an error page.
            log.debug("Resource not found: " + command.getResource());
            return new ModelAndView("browse-tool-not-found", "resourceName", command.getResource());
        }

        TargetInstance ti = hr.getTargetInstance();
        if (ti == null) {        // If the resource is not found, go to an error page.
            log.debug("Resource not found: " + command.getResource());
            return new ModelAndView("browse-tool-not-found", "resourceName", command.getResource());
        }

        List<Header> headers = new ArrayList<>();
        try {        // catch any DigitalAssetStoreException and log assumptions
            headers = digitalAssetStore.getHeaders(ti.getOid(), hr.getHarvestNumber(), command.getResource());
        } catch (Exception e) {
            log.error("Unexpected exception encountered when retrieving WARC headers for ti " + ti.getOid());
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

        int statusCode = Integer.parseInt(getHeaderValue(headers, "HTTP-RESPONSE-STATUS-CODE"));

        // Send the headers for a redirect.
        if (statusCode == HttpServletResponse.SC_MOVED_TEMPORARILY || statusCode == HttpServletResponse.SC_MOVED_PERMANENTLY) {
            res.setStatus(statusCode);
            String location = getHeaderValue(headers, "Location");
            if (location != null) {
                String newUrl = browseHelper.convertUrl(command.getHrOid(), command.getResource(), location);
                res.setHeader("Location", newUrl);
            }
        }

        // Get the content type.
        String realContentType = getHeaderValue(headers, "Content-Type");
        String simpleContentType = this.getSimpleContentType(realContentType);

        String charset = null;
        if (realContentType != null) {
            Matcher charsetMatcher = CHARSET_PATTERN.matcher(realContentType);
            if (charsetMatcher.find()) {
                charset = charsetMatcher.group(1);
                log.debug("Desired charset: " + charset + " for " + command.getResource());
            } else {
                log.debug("No charset for: " + command.getResource());
                charset = CHARSET_LATIN_1.name();
                realContentType += ";charset=" + charset;
            }
        }


        // If the content has been registered with the browseHelper to require replacements, load the content and perform the necessary replacements.
        if (browseHelper.isReplaceable(simpleContentType)) {
            StringBuilder content = null;

            try {
                content = readFile(ti.getOid(), hr.getHarvestNumber(), command.getResource(), charset);
            } catch (DigitalAssetStoreException e) {
                log.warn(e.getMessage());
            }
            ModelAndView mav = new ModelAndView("browse-tool-html");
            if (content == null) {
                content = new StringBuilder();
            }

            // We might need to use a different base URL if a BASE HREF tag
            // is used. We use the TagMagix class to perform the search.
            // Note that TagMagix leaves leading/trailing slashes on the
            // URL, so we need to do that
            String baseUrl = command.getResource();
            Pattern baseUrlGetter = BrowseHelper.getTagMagixPattern("BASE", "HREF");
            Matcher m = baseUrlGetter.matcher(content);
            if (m.find()) {
                String u = m.group(1);
                if (u.startsWith("\"") && u.endsWith("\"") || u.startsWith("'") && u.endsWith("'")) {
                    // Ensure the detected Base HREF is not commented
                    // out (unusual case, but we have seen it).
                    int lastEndComment = content.lastIndexOf("-->", m.start());
                    int lastStartComment = content.lastIndexOf("<!--", m.start());
                    if (lastStartComment < 0 || lastEndComment > lastStartComment) {
                        baseUrl = u.substring(1, u.length() - 1);
                    }
                }
            }

            browseHelper.fix(content, simpleContentType, command.getHrOid(), baseUrl);
            mav.addObject("content", content.toString());
            mav.addObject("Content-Type", realContentType);
            return mav;
        } else { // If there are no replacements, send the content back directly.
            Date dt = new Date();
            Path path = digitalAssetStore.getResource(ti.getOid(), hr.getHarvestNumber(), command.getResource());
            ModelAndView mav = new ModelAndView("browse-tool-other");
            mav.addObject("file", path);
            mav.addObject("contentType", realContentType);

            log.info("TIME TO GET RESOURCE(old): " + (new Date().getTime() - dt.getTime()));
            return mav;
        }

    }

    private Charset loadCharset(String charset) {
        Charset cs = CHARSET_LATIN_1;
        if (charset != null) {
            try {
                cs = Charset.forName(charset);
            } catch (Exception ex) {
                log.warn("Could not load desired charset " + charset + "; using ISO-8859-1");
            }
        }

        return cs;
    }


    /**
     * Reads the contents of the HarvestResource described by the DTO.
     *
     * @param dto The HarvestResource to read the content of.
     * @return The content of the HarvestResource as a String.
     * @throws IOException
     */

    /**
     * Reads the contents of the HarvestResource described by the DTO.
     *
     * @param targetInstanceId    Target Instance Oid
     * @param harvestResultNumber Harvest Result Number
     * @param resourceUrl         Resource URL requested
     * @param charset             charset for response
     * @return content related to request URL
     * @throws DigitalAssetStoreException error happens
     */
    private StringBuilder readFile(long targetInstanceId, int harvestResultNumber, String resourceUrl, String charset)
            throws DigitalAssetStoreException {

        // this is always a temp file - need to delete it before exiting
        Path path = digitalAssetStore.getResource(targetInstanceId, harvestResultNumber, resourceUrl);

        StringBuilder content = new StringBuilder();
        BufferedInputStream is = null;

        try {
            // Try to get the appropriate character set.
            Charset cs = loadCharset(charset);

            is = new BufferedInputStream(new FileInputStream(path.toFile()));
            byte[] buff = new byte[BYTE_BUFFER_SIZE];
            int bytesRead = 0;

            bytesRead = is.read(buff);
            while (bytesRead > 0) {
                content.append(new String(buff, 0, bytesRead, cs.name()));
                bytesRead = is.read(buff);
            }

        } catch (Exception e) {
            throw new DigitalAssetStoreException("Failed to read file : " + path.toString() + " : " + e.getMessage(), e);
        } finally {
            try {
                is.close();
                // No point deleting it if it is already gone
                if (path.toFile().exists()) {
                    if (!path.toFile().delete()) {
                        log.error("Failed to delete temporary file: " + path.toString());
                    }
                }
            } catch (IOException e) {
                log.warn("Failed to close input stream " + e.getMessage(), e);
            }
        }

        return content;
    }

    /**
     * @param fixTokens the fixTokens to set
     */
    public void setFixTokens(Map<String, String> fixTokens) {
        this.fixTokens = fixTokens;
    }

    public static Map<String, String> getFixTokens() {
        return fixTokens;
    }

}
