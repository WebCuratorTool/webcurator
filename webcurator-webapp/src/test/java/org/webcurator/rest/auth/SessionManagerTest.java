package org.webcurator.rest.auth;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webcurator.domain.UserRoleDAO;
import org.webcurator.domain.model.auth.Privilege;
import org.webcurator.domain.model.auth.RolePrivilege;

import java.util.ArrayList;
import java.util.List;

public class SessionManagerTest {

    private static final String TOKEN = "token";
    private static final String USER = "user";
    private static final String AGENCY = "agency";

    SessionManager sessionManager;


    @Before
    public void setUp() {
        List<RolePrivilege> rolePrivileges = new ArrayList<>();

        RolePrivilege rolePrivilege = new RolePrivilege();
        rolePrivilege.setPrivilege(Privilege.APPROVE_TARGET);
        rolePrivilege.setPrivilegeScope(Privilege.SCOPE_AGENCY);
        rolePrivileges.add(rolePrivilege);

        rolePrivilege = new RolePrivilege();
        rolePrivilege.setPrivilege(Privilege.DELETE_TARGET);
        rolePrivilege.setPrivilegeScope(Privilege.SCOPE_OWNER);
        rolePrivileges.add(rolePrivilege);

        rolePrivilege = new RolePrivilege();
        rolePrivilege.setPrivilege(Privilege.MODIFY_TARGET);
        rolePrivilege.setPrivilegeScope(Privilege.SCOPE_ALL);
        rolePrivileges.add(rolePrivilege);

        rolePrivilege = new RolePrivilege();
        rolePrivilege.setPrivilege(Privilege.LOGIN);
        rolePrivilege.setPrivilegeScope(Privilege.SCOPE_NONE);
        rolePrivileges.add(rolePrivilege);

        UserRoleDAO userRoleDAO = new UserRoleDaoStub(USER, AGENCY, rolePrivileges);
        Sessions sessions = new Sessions();
        sessions.addSession(TOKEN, USER, 600000);
        sessionManager = new SessionManager(sessions, userRoleDAO, null);
    }

    /**
     * Verify that a user who does not have a given privilege is denied
     */
    @Test(expected = AuthorizationException.class)
    public void testAuthorizeRoleMissing() throws AuthorizationException {
        sessionManager.authorize(TOKEN, USER, AGENCY, Privilege.CREATE_TARGET);
    }

    /**
     * Verify that a user who does have a given privilege, but does not belong to the
     * agency scope valid for that privilege, is denied
     */
    @Test(expected = AuthorizationException.class)
    public void testAuthorizeAgencyScopeMissing() throws AuthorizationException {
        sessionManager.authorize(TOKEN, USER, "otherAgency", Privilege.APPROVE_TARGET);
    }

    /**
     * Verify that a user who does have a given privilege, but does not belong to the owner scope
     * valid for that privilege, is denied
     */
    @Test(expected = AuthorizationException.class)
    public void testAuthorizeOwnerScopeMissing() throws AuthorizationException {
        sessionManager.authorize(TOKEN, "otherUser", AGENCY, Privilege.DELETE_TARGET);
    }

    /**
     * Verify that a user who has a privilege with SCOPE_ALL is allowed, and that the owner and agency
     * arguments are immaterial in this case
     */
    @Test
    public void testAuthorizeScopeAll() {
        try {
            sessionManager.authorize(TOKEN, null, null, Privilege.MODIFY_TARGET);
        } catch (AuthorizationException e) {
            Assert.fail("Expected the user to be allowed");
        }
    }

    /**
     * Verify that a user who has a privilege with SCOPE_NONE is allowed, and that the owner and agency
     * arguments are immaterial in this case
     */
    @Test
    public void testAuthorizeScopeNone() {
        try {
            sessionManager.authorize(TOKEN, null, null, Privilege.LOGIN);
        } catch (AuthorizationException e) {
            Assert.fail("Expected the user to be allowed");
        }
    }
}