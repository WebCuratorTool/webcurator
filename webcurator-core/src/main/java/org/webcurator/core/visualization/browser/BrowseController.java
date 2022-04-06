package org.webcurator.core.visualization.browser;

import bsh.StringUtil;
import org.apache.commons.httpclient.Header;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.webcurator.common.util.Utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class BrowseController {
    private static final Pattern p = Pattern.compile("\\/(\\d+)\\/(.*)");
    private static final Pattern CHARSET_PATTERN = Pattern.compile(";\\s+charset=([A-Za-z0-9].[A-Za-z0-9_\\-\\.:]*)");
    private static final Charset CHARSET_LATIN_1 = StandardCharsets.UTF_8; // StandardCharsets.ISO_8859_1;

    private static final Logger log = LoggerFactory.getLogger(BrowseController.class);

    @Autowired
    private VisWayBackClient visWayBackClient;

    @Autowired
    private BrowseHelper browseHelper;

    @RequestMapping(path = "/curator/tools/download/{jobId}/{harvestResultNumber}/**", method = {RequestMethod.POST, RequestMethod.GET})
    protected void handleDownload(@PathVariable("jobId") Long jobId, @PathVariable("harvestResultNumber") Integer harvestResultNumber, @RequestParam("url") String url, HttpServletRequest req, HttpServletResponse rsp) throws Exception {
        url = new String(Base64.getDecoder().decode(url));

        List<Header> headers = new ArrayList<>();
        try {        // catch any DigitalAssetStoreException and log assumptions
            headers = visWayBackClient.getHeaders(jobId, harvestResultNumber, url);
        } catch (Exception e) {
            log.error("Unexpected exception encountered when retrieving WARC headers for {} {}", jobId, harvestResultNumber);
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

        String strStatusCode = getHeaderValue(headers, "HTTP-RESPONSE-STATUS-CODE");
        if (headers.size() == 0 || Utils.isEmpty(strStatusCode)) {
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        int statusCode = Integer.parseInt(strStatusCode);

        // Send the headers for a redirect.
        if (statusCode == HttpServletResponse.SC_MOVED_TEMPORARILY || statusCode == HttpServletResponse.SC_MOVED_PERMANENTLY) {
            rsp.setStatus(statusCode);
            String location = getHeaderValue(headers, "Location");
            if (!Utils.isEmpty(location) && !location.startsWith("http")) {
                location = url + location;
            }
            String encodedLocation = Base64.getEncoder().encodeToString(location.getBytes());
            rsp.setHeader("Location", String.format("/curator/tools/browse/%d/%d/?url=%s", jobId, harvestResultNumber, encodedLocation));
        } else {
            // Get the content type.
            rsp.setHeader("Content-Type", getHeaderValue(headers, "Content-Type"));
            Path path = visWayBackClient.getResource(jobId, harvestResultNumber, url);
            IOUtils.copy(Files.newInputStream(path), rsp.getOutputStream());
        }
    }

    @RequestMapping(path = "/curator/tools/browse/{jobId}/{harvestResultNumber}/**", method = {RequestMethod.POST, RequestMethod.GET})
    protected void handleBrowse(@PathVariable("jobId") Long jobId, @PathVariable("harvestResultNumber") Integer harvestResultNumber, @RequestParam("url") String url, HttpServletRequest req, HttpServletResponse rsp) throws Exception {
        if (!Utils.isEmpty(url) && url.startsWith("/")) {
            url = url.substring(1);
        }
        String baseUrl = new String(Base64.getDecoder().decode(url));

        List<Header> headers = new ArrayList<>();
        try {        // catch any DigitalAssetStoreException and log assumptions
            headers = visWayBackClient.getHeaders(jobId, harvestResultNumber, baseUrl);
        } catch (Exception e) {
            log.error("Unexpected exception encountered when retrieving WARC headers for {} {} ", jobId, harvestResultNumber);
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

        // Get the content type.
        String realContentType = getHeaderValue(headers, "Content-Type");
        String simpleContentType = this.getSimpleContentType(realContentType);

        String charset = null;
        if (realContentType != null) {
            Matcher charsetMatcher = CHARSET_PATTERN.matcher(realContentType);
            if (charsetMatcher.find()) {
                charset = charsetMatcher.group(1);
                log.debug("Desired charset: " + charset + " for " + baseUrl);
            } else {
                log.debug("No charset for: " + baseUrl);
                charset = CHARSET_LATIN_1.name();
                realContentType += ";charset=" + charset;
            }
        }

        String strStatusCode = getHeaderValue(headers, "HTTP-RESPONSE-STATUS-CODE");
        if (headers.size() == 0 || Utils.isEmpty(strStatusCode)) {
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        int statusCode = Integer.parseInt(getHeaderValue(headers, "HTTP-RESPONSE-STATUS-CODE"));
        // Send the headers for a redirect.
        if (statusCode == HttpServletResponse.SC_MOVED_TEMPORARILY || statusCode == HttpServletResponse.SC_MOVED_PERMANENTLY) {
            rsp.setStatus(statusCode);
            String location = getHeaderValue(headers, "Location");
            if (!Utils.isEmpty(location) && !location.startsWith("http")) {
                location = baseUrl + location;
            }
            String encodedLocation = Base64.getEncoder().encodeToString(location.getBytes());
            rsp.setHeader("Location", browseHelper.getResourcePrefix(jobId, harvestResultNumber) + encodedLocation);
            return;
        }

        // Get the content type.
        rsp.setHeader("Content-Type", getHeaderValue(headers, "Content-Type"));

        Path path = visWayBackClient.getResource(jobId, harvestResultNumber, baseUrl);
        if (!browseHelper.isReplaceable(simpleContentType)) {
            IOUtils.copy(Files.newInputStream(path), rsp.getOutputStream());
            path.toFile().delete();
            return;
        }

        int fileLength = (int) path.toFile().length();
        byte[] buf = new byte[fileLength];
        IOUtils.read(Files.newInputStream(path), buf);
        path.toFile().delete();

        if (StringUtils.isEmpty(charset)) {
            charset = Charset.defaultCharset().name();
        }
        StringBuilder content = new StringBuilder(new String(buf, charset));

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
        browseHelper.fix(content, simpleContentType, jobId, harvestResultNumber, baseUrl);

        rsp.getOutputStream().write(content.toString().getBytes(charset));
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

}
