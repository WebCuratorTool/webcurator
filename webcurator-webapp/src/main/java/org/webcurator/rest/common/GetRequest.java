package org.webcurator.rest.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Used to change the method from POST to GET in a filter
 */
public class GetRequest extends HttpServletRequestWrapper {
    public GetRequest(HttpServletRequest request) {
        super(request);
    }
    public String getMethod() {
        return "GET";
    }
}
