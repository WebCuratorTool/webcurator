package org.webcurator.core.store.arc;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomDepositFormFilter implements Filter {

    public CustomDepositFormFilter(){
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        // Set CORS headers for all responses
        HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("Access-Control-Allow-Headers","Access-Control-Allow-Origin,Access-Control-Allow-Methods");
        response.setHeader("Access-Control-Allow-Origin","http://localhost:8080");
        response.setHeader("Access-Control-Allow-Methods","GET,POST,HEAD,OPTIONS");

        // Set CORS headers for all requests
        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest((HttpServletRequest) req);
        mutableRequest.putHeader("Access-Control-Allow-Headers","Access-Control-Allow-Origin,Access-Control-Allow-Methods");
        mutableRequest.putHeader("Access-Control-Allow-Origin","http://localhost:8080");
        mutableRequest.putHeader("Access-Control-Allow-Methods","GET,POST,HEAD,OPTIONS");

        chain.doFilter(mutableRequest, response);
    }

    @Override
    public void destroy(){
    }

    public void init(FilterConfig fConfig) throws ServletException {
    }

/*    @Bean
    public FilterRegistrationBean<CustomDepositFormFilter> filterRegistrationBean() {
        FilterRegistrationBean<CustomDepositFormFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CustomDepositFormFilter());
        registrationBean.addUrlPatterns("/customDepositForms/*");

        return registrationBean;
    }*/

}
