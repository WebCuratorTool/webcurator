package org.webcurator.serv;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;

@RestController
public class StaticFileReaderController {
    private static final Logger log = LoggerFactory.getLogger(StaticFileReaderController.class);
    @Autowired
    private ServletContext servletContext;

    private static String getContentTypeFor(String path) {
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        path = path.toLowerCase();
        if (path.endsWith(".css")) {
            return "text/css";
        } else if (path.endsWith(".js")) {
            return "application/javascript";
        } else if (path.endsWith(".json")) {
            return "application/json";
        } else if (path.endsWith(".htm") || path.endsWith(".html")) {
            return "text/html;charset=UTF-8";
        } else if (path.endsWith(".png")) {
            return "image/png";
        } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (path.endsWith(".ico")) {
            return "image/vnd.microsoft.icon";
        }
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        return fileNameMap.getContentTypeFor(path);
    }

    /**
     * For the single page application, the request can be an internal route paths, there is no implicit files link to these kinds of paths. These kinks of links often point to the section of index.html (.js, .css included).
     *
     * @param request:  the http request
     * @param response: the http response
     * @throws IOException: error happens
     */
    @GetMapping("/**")
    public void outputFile(ServletRequest request, ServletResponse response) throws IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse rsp = ((HttpServletResponse) response);

        String contentUri = req.getContextPath();
        String url = req.getRequestURI().substring(contentUri.length());

        log.debug("Received request: {}", url);
        String contentType = getContentTypeFor(url);

        byte[] fileData = StreamUtils.copyToByteArray(servletContext.getResourceAsStream(url));
        if (fileData.length == 0) {
            fileData = StreamUtils.copyToByteArray(servletContext.getResourceAsStream("/index.html"));
            contentType = getContentTypeFor("/index.html");
        }

        if (!StringUtils.isEmpty(contentType)) {
            rsp.setContentType(contentType);
        } else {
            log.info("Can not get MIMEType for: {}", url);
        }

        IOUtils.write(fileData, rsp.getOutputStream());
    }
}