package org.webcurator.rest.auth;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Handles authentication and authorization for the ReST API, using JWTs
 */
@Component
public class SessionManager {

    @Autowired
    Sessions sessions;

    @Autowired
    AuthenticationManager authenticationManager;


    // Private key used to sign JWTs
    private String secret = RandomStringUtils.random(64, true, true);


    public String authenticate(String username, String password) throws AuthenticationException {

        // Check credentials
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(authRequest);

        // Get roles
        Collection roles = authentication.getAuthorities();

        // Create JWT
        // FIXME implement
        String jwt = "jwt";

        // Add the session
        sessions.addSession(jwt);

        return jwt;
    }

    public boolean isAuthorized(String authorizationHeader, String role) {
        // FIXME implement
        return false;
    }

    public void invalidate(String authorizationHeader) {
       // FIXME implement
    }

    private String extractJwt(String authorizationHeader) {
        // FIXME implement
        return null;
    }

    public class AuthenticationException extends Exception {}

}
