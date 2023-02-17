package org.webcurator.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.webcurator.rest.auth.SessionManager;

/**
 * Handlers for the token endpoint
 */
@RestController
@RequestMapping(path = "/api/{version}/token")
public class Token {

    @Autowired
    SessionManager sessionManager;

    /**
     * Login
     */
    @PostMapping(path = "")
    public ResponseEntity<?> post(@RequestParam String username, @RequestParam String password) {

        try {
            String jwt = sessionManager.authenticate(username, password);
            return ResponseEntity.ok(jwt);
        } catch (SessionManager.AuthenticationException e) {
            return ResponseEntity.status(403).body("Authentication failed");
        }
    }

    /**
     * Note: we don't add the token to the path, because the URL might become too large otherwise
     */
    @DeleteMapping(path = "")
    public ResponseEntity<?> delete(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {

        sessionManager.invalidate(authorizationHeader);

        // Note that we simply ignore tokens we don't know
        return ResponseEntity.ok("Token has been invalidated");
    }
}
