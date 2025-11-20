package org.webcurator.rest.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.webcurator.domain.UserRoleDAO;
import org.webcurator.domain.model.auth.Privilege;
import org.webcurator.domain.model.auth.RolePrivilege;

import javax.servlet.http.HttpServletRequest;
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

        // Create token
        String token = TokenUtils.createToken();

        // Add the session
        sessions.addSession(token, username, maxIdleTime);

        return token;
    }


    /**
     * Check whether the request contains a valid token
     */
    public void checkToken(HttpServletRequest httpServletRequest) throws AuthorizationException {
        String token = extractToken(httpServletRequest);
        if (!sessions.exists(token)) {
            throw new AuthorizationException("Token is invalid", 403);
        }
    }

    /**
     * Check whether the user in the current session has sufficient privilege scope to alter the data for the supplied
     * user and/or agency and role
     */
    public void authorize(String token, String owner, String agency, String role)
            throws AuthorizationException {

        boolean hasRole = false;
        int scope = Privilege.SCOPE_NONE;
        List<RolePrivilege> rolePrivileges = userRoleDAO.getUserPrivileges(sessions.getUser(token));
        for (RolePrivilege rolePrivilege : rolePrivileges) {
            if (rolePrivilege.getPrivilege().equals(role)) {
                hasRole = true;
                scope = rolePrivilege.getPrivilegeScope();
                break;
            }
        }
        if (!hasRole) {
            throw new AuthorizationException("User does not have role " + role, 403);
        }
        switch (scope) {
            case Privilege.SCOPE_NONE:
            case Privilege.SCOPE_ALL:
                return;
            case Privilege.SCOPE_AGENCY:
                String userAgency = userRoleDAO.getUserByName(sessions.getUser(token)).getAgency().getName();
                if (userAgency.equals(agency)) {
                    return;
                } else {
                    throw new AuthorizationException(String.format(
                            "User has role %s but it's limited to objects belonging to the user's agency %s (not %s)",
                            role, userAgency, agency), 403);

                }
            case Privilege.SCOPE_OWNER:
                if (sessions.getUser(token).equals(owner)) {
                    return;
                } else {
                    throw new AuthorizationException(String.format(
                            "User has role %s but it's limited to objects the user owns", role), 403);
                }
            default:
                throw new AuthorizationException(String.format("Unknown scope %d for role %s", scope, role), 403);
        }

    }


    public void authorize(HttpServletRequest httpServletRequest, String owner, String agency, String role)
                                                                                    throws AuthorizationException {
        String token = extractToken(httpServletRequest);
        authorize(token, owner, agency, role);
    }

    /**
     * Does this token point to a valid session?
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

    private String extractToken(HttpServletRequest httpServletRequest) throws AuthorizationException {
        String authorizationHeader = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null) {
            throw new AuthorizationException("Unauthorized, no token was supplied", 401);
        }
        return authorizationHeader.replaceAll("^Bearer\\s+", "");
    }

}
