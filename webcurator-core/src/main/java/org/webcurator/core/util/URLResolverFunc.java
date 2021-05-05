package org.webcurator.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class URLResolverFunc {
    private static final Logger log = LoggerFactory.getLogger(URLResolverFunc.class);

    public static boolean isAbsolute(String url) {
        if (url == null) {
            return false;
        } else {
            return url.startsWith("http://")
                    || url.startsWith("https://")
                    || url.startsWith("ftp://")
                    || url.startsWith("feed://")
                    || url.startsWith("mailto:")
                    || url.startsWith("mail:")
                    || url.startsWith("javascript:")
                    || url.startsWith("rtsp://");
        }
    }

    public static String doResolve(String page, String base, String rel) {
        if (isAbsolute(rel)) {
            URL relUrl = null;
            try {
                relUrl = new URL(rel);
            } catch (MalformedURLException e) {
                return null;
            }
            return relUrl.toString();
        }

        URL absParent = null;
        if (isAbsolute(base)) {
            try {
                absParent = new URL(base);
            } catch (MalformedURLException e) {
                log.warn("Malformed base url: {}", base);
                return null;
            }
        } else if (isAbsolute(page)) {
            try {
                absParent = new URL(page);
            } catch (MalformedURLException e) {
                log.warn("Malformed page url: {}", page);
                return null;
            }
        }

        URL absURL = null;
        try {
            absURL = new URL(absParent, rel);
        } catch (MalformedURLException e) {
            log.warn("Malformed rel url: {}", rel);
            return null;
        }

        return absURL.toString();
    }

    public static String url2domain(String rel) {
        URL url = null;
        try {
            url = new URL(rel);
        } catch (MalformedURLException e) {
            log.warn("Malformed rel url: {}", rel);
            return null;
        }
        return url.getHost();
    }

    public static String trimContentType(String contentType) {
        if (contentType == null) {
            return "Unknown";
        }

        int idx = contentType.indexOf(';');
        if (idx < 1) {
            return contentType.trim();
        } else {
            return contentType.substring(0, idx);
        }
    }
}
