package org.webcurator.rest.auth;

import org.webcurator.domain.UserRoleDAO;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.domain.model.auth.RolePrivilege;
import org.webcurator.domain.model.auth.User;

import java.util.List;

/**
 * Stub that replaces UserRoleDao in tests
 */
public class UserRoleDaoStub extends UserRoleDAO {

    User user;
    List<RolePrivilege> rolePrivileges;

    public UserRoleDaoStub(String userName, String agencyName, List<RolePrivilege> rolePrivileges) {
        user = new User();
        user.setUsername(userName);
        Agency agency = new Agency();
        agency.setName(agencyName);
        user.setAgency(agency);
        this.rolePrivileges = rolePrivileges;
    }

    @Override
    public List<RolePrivilege> getUserPrivileges(String userName) {
        return rolePrivileges;
    }

    @Override
    public User getUserByName(String userName) {
        return user;
    }

}
