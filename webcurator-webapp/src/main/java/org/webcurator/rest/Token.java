package org.webcurator.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.webcurator.domain.model.auth.Privilege;
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
            String token = sessionManager.authenticate(username, password);
            return ResponseEntity.ok(token);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Authentication failed");
        }
    }

    /**
     * Logout
     *
     */
    @DeleteMapping(path = "/{token}")
    public ResponseEntity<?> delete(@PathVariable String token) {
        // We silently ignore invalid or nonexistent tokens
        sessionManager.invalidate(token);
        return ResponseEntity.ok("Token has been invalidated");
    }
}
