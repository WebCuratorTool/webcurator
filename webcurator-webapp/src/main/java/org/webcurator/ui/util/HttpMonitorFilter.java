package org.webcurator.ui.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.webcurator.webapp.beans.config.WctSecurityConfig;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpMonitorFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(HttpMonitorFilter.class);
    @Autowired
    private WctSecurityConfig wctSecurityConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse rsp = ((HttpServletResponse) response);
        String contentUri = req.getContextPath();
        String url = req.getRequestURI().substring(contentUri.length());

        boolean isResourceUrl = url.startsWith("/styles") ||
                url.startsWith("/images") ||
                url.startsWith("/scripts") ||
                url.startsWith("/harvest-coordinator") ||
                url.startsWith("/digital-asset-store");

        if (log.isDebugEnabled() && !isResourceUrl) {
            log.debug("Before doFilter {}", wctSecurityConfig.getCurrentSessionMessage(req, rsp));
        }

        String reqSessionMsg=wctSecurityConfig.getBasicRequestMessage(req);
        chain.doFilter(request, response);
        String rspSessionMsg= wctSecurityConfig.getBasicResponseMessage(rsp);

        if (log.isInfoEnabled() && !isResourceUrl) {
            log.info("{} {}" ,reqSessionMsg, rspSessionMsg);
        }

        if (log.isDebugEnabled() && !isResourceUrl) {
            log.debug("After doFilter {}", wctSecurityConfig.getCurrentSessionMessage(req, rsp));
        }

        log.debug("Request URL: {} {}", url, rsp.getStatus());
    }

    @Override
    public void destroy() {

    }

    private String getSessionKey(HttpSession curSession, Map<String, HttpSession> sessions) {
        if (sessions == null) {
            return null;
        }

//        sessions.forEach();

        return "";
    }
}
