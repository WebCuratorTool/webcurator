package org.webcurator.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.webcurator.rest.auth.SessionManager;
import org.webcurator.rest.auth.Sessions;

import java.util.List;

/**
 * Handlers for the token endpoint
 */
@RestController
@RequestMapping(path = "/auth/{version}/token")
public class Token {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    Sessions sessions;

    @GetMapping(path = "/{token}")
    public ResponseEntity<?> get(@PathVariable String token) {
        try {
            List<String> roles = sessions.getRoles(token);
            if (!roles.isEmpty()) {
                return ResponseEntity.ok("valid");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
        } catch (Sessions.InvalidSessionException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }

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
     */
    @DeleteMapping(path = "/{token}")
    public ResponseEntity<?> delete(@PathVariable String token) {
        // We silently ignore invalid or nonexistent tokens
        sessionManager.invalidate(token);
        return ResponseEntity.ok("Token has been invalidated");
    }
}
