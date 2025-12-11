package org.webcurator.rest.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import java.util.*;

/**
 * Handles authentication and authorization for the ReST API, using token-based access
 */
@Component
public class SessionManager {

    @Value("${rest.maxIdleTime}")
    private int maxIdleTime;

    private static Log logger = LogFactory.getLog(SessionManager.class);

    Sessions sessions;

    UserRoleDAO userRoleDAO;

    AuthenticationManager authenticationManager;

    public SessionManager(Sessions sessions, UserRoleDAO userRoleDAO, AuthenticationManager authenticationManager) {
        this.sessions = sessions;
        this.userRoleDAO = userRoleDAO;
        this.authenticationManager = authenticationManager;
    }

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
     * user and/or agency and privilege
     */
    public void authorize(String token, String owner, String agency, String privilege)
            throws AuthorizationException {

        boolean hasPrivilege = false;
        int scope = Privilege.SCOPE_NONE;
        List<RolePrivilege> rolePrivileges = userRoleDAO.getUserPrivileges(sessions.getUser(token));
        for (RolePrivilege rolePrivilege : rolePrivileges) {
            if (rolePrivilege.getPrivilege().equals(privilege)) {
                hasPrivilege = true;
                // Make sure we get the widest scope for this privilege
                if (scope > rolePrivilege.getPrivilegeScope()) {
                    scope = rolePrivilege.getPrivilegeScope();
                }
            }
        }
        if (!hasPrivilege) {
            throw new AuthorizationException("User does not have privilege " + privilege, 403);
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
                            "User has privilege %s but it's limited to objects belonging to the user's agency %s (not %s)",
                            privilege, userAgency, agency), 403);

                }
            case Privilege.SCOPE_OWNER:
                if (sessions.getUser(token).equals(owner)) {
                    return;
                } else {
                    throw new AuthorizationException(String.format(
                            "User has privilege %s but it's limited to objects the user owns", privilege), 403);
                }
            default:
                throw new AuthorizationException(String.format("Unknown scope %d for privilege %s", scope, privilege), 403);
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

    /**
     * Get the privileges for the user in this session. Note that a privilege can be granted to a user
     * multiple times, because a user may have more than one role. This method returns the highest available scope
     * for every privilege.
     */
    public List<Map<String, Object>> getPrivileges(HttpServletRequest httpServletRequest) throws AuthorizationException {
        String token = extractToken(httpServletRequest);
        String userName = sessions.getUser(token);
        List<RolePrivilege> rolePrivileges = userRoleDAO.getUserPrivileges(userName);
        List<Map<String, Object>> privileges = new ArrayList<>();

        // First determine the privileges with the widest scope
        HashMap<String, Integer> rolePrivilegesMap = new HashMap<>();
        for (RolePrivilege rolePrivilege : rolePrivileges) {
            int scope = rolePrivilege.getPrivilegeScope();
            String privilege = rolePrivilege.getPrivilege();
            if (rolePrivilegesMap.containsKey(privilege) && rolePrivilegesMap.get(privilege) <= scope) {
                continue;
            }
            rolePrivilegesMap.put(privilege, scope);
        }

        // Now structure them as a list of maps
        for (Map.Entry<String, Integer> entry: rolePrivilegesMap.entrySet()) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("privilege", entry.getKey());
            map.put("scope", entry.getValue());
            privileges.add(map);
        }
        return privileges;
    }


    private String extractToken(HttpServletRequest httpServletRequest) throws AuthorizationException {
        String authorizationHeader = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null) {
            throw new AuthorizationException("Unauthorized, no token was supplied", 401);
        }
        return authorizationHeader.replaceAll("^Bearer\\s+", "");
    }


}
