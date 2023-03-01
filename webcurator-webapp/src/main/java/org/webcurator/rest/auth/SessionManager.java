package org.webcurator.rest.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles authentication and authorization for the ReST API, using token-based access
 */
@Component
public class SessionManager {

    @Value("${rest.maxIdleTime}")
    private int maxIdleTime;

    private static Log logger = LogFactory.getLog(SessionManager.class);

    @Autowired
    Sessions sessions;

    @Autowired
    AuthenticationManager authenticationManager;

    /**
     * Checks username/password and returns a token upon success
     */
    public String authenticate(String username, String password) throws AuthenticationException {

        // Check credentials
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(authRequest);

        // Get roles
        List<String> roles = new ArrayList<>();
        for(GrantedAuthority authority : authentication.getAuthorities()) {
            roles.add(authority.getAuthority());
        }

        // Create token
        String token = TokenUtils.createToken();

        // Add the session
        sessions.addSession(token, roles, maxIdleTime);

        return token;
    }

    /**
     * Checks whether the bearer of the token has the supplied role
     */
    public AuthorizationResult authorize(String authorizationHeader, String role) {
        if (authorizationHeader == null) {
            return new AuthorizationResult(true, 401, "Unauthorized, no token was supplied");
        }
        try {
            String token = extractToken(authorizationHeader);
            List roles = sessions.getRoles(token);
            if (roles.contains("ROLE_" + role)) {
                return new AuthorizationResult(false, 200, "");
            } else {
                return new AuthorizationResult(true, 403, "User does not have role " + role);
            }
        } catch (Sessions.InvalidSessionException e) {
            return new AuthorizationResult(true, 401, "Invalid or expired session");
        }
    }

    /**I
     * Ends the session
     */
    public void invalidate(String token) {
        sessions.removeSession(token);
    }

    private String extractToken(String authorizationHeader) {
        return authorizationHeader.replaceAll("^Bearer\\s+", "");
    }

    public class AuthorizationResult {
        public boolean failed;
        public int status;
        public String message;
        public AuthorizationResult(boolean failed, int status, String message) {
            this.failed = failed;
            this.status = status;
            this.message = message;
        }
    }

}
