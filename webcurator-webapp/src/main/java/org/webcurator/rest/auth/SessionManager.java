package org.webcurator.rest.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.webcurator.domain.UserRoleDAO;
import org.webcurator.domain.model.auth.Privilege;
import org.webcurator.domain.model.auth.RolePrivilege;

import java.util.HashMap;
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
    UserRoleDAO userRoleDAO;

    @Autowired
    AuthenticationManager authenticationManager;

    /**
     * Checks username/password and returns a token upon success
     */
    public String authenticate(String username, String password) throws AuthenticationException {

        // Check credentials
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
        authenticationManager.authenticate(authRequest);

        // Get the privileges so we can store them in the newly created session
        List<RolePrivilege> rolePrivileges = userRoleDAO.getUserPrivileges(username);
        HashMap<String, Integer> privileges = new HashMap<>();
        for (RolePrivilege r : rolePrivileges) {
            privileges.put(r.getPrivilege(), r.getPrivilegeScope());
        }

        // We also need to store the agency name for authorization checks
        String agency = userRoleDAO.getUserByName(username).getAgency().getName();

        // Create token
        String token = TokenUtils.createToken();

        // Add the session
        sessions.addSession(token, privileges, username, agency, maxIdleTime);

        return token;
    }

    /**
     * Check whether the user in the current session has sufficient privilege scope to alter the data for the supplied
     * user and/or agency and role
     */
    public void checkScope(String authorizationHeader, String owner, String agency, String role)
                                                                                    throws AuthorizationException {

        if (authorizationHeader == null) {
            throw new AuthorizationException("Unauthorized, no token was supplied", 401);
        }
        try {
            String token = extractToken(authorizationHeader);
            HashMap<String, Integer> privileges = sessions.getPrivileges(token);
            if (!privileges.containsKey(role)) {
                throw new AuthorizationException("User does not have role " + role, 403);
            } else {
                int scope = privileges.get(role);
                switch (scope) {
                    case Privilege.SCOPE_NONE:
                        return;
                    case Privilege.SCOPE_ALL:
                        return;
                    case Privilege.SCOPE_AGENCY:
                        if (sessions.getAgency(token).equals(agency)) {
                            return;
                        } else {
                            throw new AuthorizationException(String.format(
                                    "User has role %s but it's limited to objects belonging to the user's agency %s (not %s)",
                                    role, sessions.getAgency(token), agency), 403);

                        }
                    case Privilege.SCOPE_OWNER:
                        if (sessions.getUser(token).equals(owner)) {
                            return;
                        } else {
                            throw new AuthorizationException(String.format(
                                                        "User has role %s but it's limited to objects the user owns", role), 403);
                        }
                    default:
                        throw new AuthorizationException("Unknown authorization error", 403);
                }
            }
        } catch (Sessions.InvalidSessionException e) {
            throw new AuthorizationException("Invalid or expired session", 401);
        }
    }

    /**
     * Checks whether the bearer of the token has the supplied role with the supplied scope
     */
    public AuthorizationResult authorize(String authorizationHeader, String role, int scope) {
        if (authorizationHeader == null) {
            return new AuthorizationResult(true, 401, "Unauthorized, no token was supplied");
        }
        try {
            String token = extractToken(authorizationHeader);
            HashMap<String, Integer> privileges = sessions.getPrivileges(token);
            if (!privileges.keySet().contains(role)) {
                return new AuthorizationResult(true, 403, "User does not have role " + role);
            } else {
                if (privileges.get(role) == scope) {
                    return new AuthorizationResult(false, 200, "");
                } else {
                    return new AuthorizationResult(true, 403, String.format("User has role %s but not the required scope %d",
                                                                                                            role, scope));
                }
            }
        } catch (Sessions.InvalidSessionException e) {
            return new AuthorizationResult(true, 401, "Invalid or expired session");
        }
    }

    /**
     * Checks whether the bearer of the token has the supplied role
     */
    public AuthorizationResult authorize(String authorizationHeader, String role) {
        return authorize(authorizationHeader, role, Privilege.SCOPE_NONE);
    }

    /**
     * Does this token point to a valid session?
     * // FIXME the GET /token/{token} call (that will be coming downstream from another branch) should use this method instead of using Sessions directly
     */
    public boolean isValid(String token) {
        return sessions.exists(token);
    }

    /**
     * Ends the session
     */
    public void invalidate(String token) {
        sessions.removeSession(token);
    }

    private String extractToken(String authorizationHeader) {
        return authorizationHeader.replaceAll("^Bearer\\s+", "");
    }

}
