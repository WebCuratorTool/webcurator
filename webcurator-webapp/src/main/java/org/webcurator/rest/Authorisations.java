package org.webcurator.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.webcurator.rest.auth.AuthorizationException;
import org.webcurator.rest.auth.SessionManager;
import org.webcurator.rest.common.FailureResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * Handlers for the authorisations endpoint
 */
@RestController
@RequestMapping(path = "/api/{version}/authorisations")
public class Authorisations {

    @Autowired
    SessionManager sessionManager;

    /**
     * Return the privileges for the user/session identified by the supplied access token
     */
    @GetMapping(path = "")
    public ResponseEntity<?> get(HttpServletRequest request) {
        try {
            return ResponseEntity.ok().body(sessionManager.getPrivileges(request));
        } catch (AuthorizationException e) {
            return FailureResponse.error(HttpStatus.valueOf(e.getStatus()), e.getMessage());
        }
    }
}
