package org.webcurator.core.store.arc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;

// This is a version of the MutableHttpServletRequest class detailed in http://wilddiary.com/adding-custom-headers-java-httpservletrequest/

@SuppressWarnings("unchecked")
final class MutableHttpServletRequest extends HttpServletRequestWrapper {

    private final Map<String, String> headers;

    public MutableHttpServletRequest(HttpServletRequest req) {
        super(req);
        this.headers = new HashMap<String,String>();
    }

    public String getHeader(String name) {
        if (this.headers.get(name) != null) return this.headers.get(name);
        // If null, return object from wrapper
        else return ((HttpServletRequest) getRequest()).getHeader(name);
    }

    public Enumeration<String> getHeaderNames() {
        Set<String> headerNames = new HashSet<String>(headers.keySet());
        // Add in headers from wrapped object
        Enumeration<String> wrappedHeaders = ((HttpServletRequest) getRequest()).getHeaderNames();
        while (wrappedHeaders.hasMoreElements()) {
            String header = wrappedHeaders.nextElement();
            headerNames.add(header);
        }
        return Collections.enumeration(headerNames);
    }

    public void putHeader(String name, String value) {
        this.headers.put(name, value);
    }

}
