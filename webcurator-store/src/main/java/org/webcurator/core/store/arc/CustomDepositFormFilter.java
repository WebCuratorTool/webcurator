package org.webcurator.core.store.arc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomDepositFormFilter implements Filter {

    @Value("${webapp.host}")
    String core;

    @Value("${webapp.port}")
    String corePort;

    @Value("${webapp.scheme}")
    String coreScheme;

    public CustomDepositFormFilter(){
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        // Set CORS headers for all responses
        HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("Access-Control-Allow-Headers","Access-Control-Allow-Origin,Access-Control-Allow-Methods");
        response.setHeader("Access-Control-Allow-Origin", coreScheme + "://" + core + ":" + corePort);
        response.setHeader("Access-Control-Allow-Methods","GET,POST,HEAD,OPTIONS");

        // Set CORS headers for all requests
        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest((HttpServletRequest) req);
        mutableRequest.putHeader("Access-Control-Allow-Headers","Access-Control-Allow-Origin,Access-Control-Allow-Methods");
        mutableRequest.putHeader("Access-Control-Allow-Origin", coreScheme + "://" + core + ":" + corePort);
        mutableRequest.putHeader("Access-Control-Allow-Methods","GET,POST,HEAD,OPTIONS");

        chain.doFilter(mutableRequest, response);
    }

    @Override
    public void destroy(){
    }

    public void init(FilterConfig fConfig) throws ServletException {
    }

}
