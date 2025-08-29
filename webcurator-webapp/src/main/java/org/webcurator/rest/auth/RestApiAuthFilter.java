package org.webcurator.rest.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.webcurator.domain.model.auth.Privilege;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestApiAuthFilter implements Filter {
    @Autowired
    SessionManager sessionManager;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse rsp = ((HttpServletResponse) response);
        String contentUri = req.getContextPath();
        String url = req.getRequestURI().substring(contentUri.length());
        if (url.startsWith("/api")) {
            String authorizationHeader = req.getHeader(HttpHeaders.AUTHORIZATION);
            try {
                // More authorisation rules are checked at the endpoints
                sessionManager.authorize(authorizationHeader, null, null, Privilege.LOGIN);
            } catch (AuthorizationException e) {
                rsp.setStatus(e.getStatus());
                rsp.getOutputStream().print(e.getMessage());
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
