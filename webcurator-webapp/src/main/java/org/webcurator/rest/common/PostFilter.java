package org.webcurator.rest.common;

import org.springframework.stereotype.Component;
import org.webcurator.rest.common.GetRequest;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Workaround to make sure POST requests with a method override header get routed to GET handlers.
 * This is necessary because JavaScript doesn't allow the sending of GET with a request body and
 * the QUERY method hasn't been implemented yet in Spring.
 */
@Component
public class PostFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;

        if (req.getMethod().equalsIgnoreCase("POST")) {
            String overrideHeader = req.getHeader("X-HTTP-Method-Override");
            if (overrideHeader != null && overrideHeader.equalsIgnoreCase("GET")) {
                req = new GetRequest(req);
            }
        }
        chain.doFilter(req, response);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
