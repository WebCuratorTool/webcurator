/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.webcurator.auth.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.webcurator.domain.UserRoleDAO;
import org.webcurator.domain.model.auth.RolePrivilege;

/**
 * If the user is authenticated by the LDAP repository then the WCTAuthoritiesPopulator
 * sets the granted authorities for the user from the WCT Database.
 * @author bprice
 */
public class WCTAuthoritiesPopulator implements LdapAuthoritiesPopulator {

    private UserRoleDAO auth;
    
    public WCTAuthoritiesPopulator() {
        super();
    }
    
    /**
     * Select the granted authorities for the sepcified user and return and 
     * array of the authorities found.
     * @param userData the context object which was returned by the LDAP authenticator.
     * @param username the user name to get the authorities for
     * @return the list of granted authorities
     */
    public Collection<GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) {

        List<GrantedAuthority> roles = new ArrayList<>();
        List privileges = auth.getUserPrivileges(username);
        if (privileges != null) {
            for (Object rolePrivilegeObject : privileges) {
                RolePrivilege rolePrivilege = (RolePrivilege) rolePrivilegeObject;
                GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_" + rolePrivilege.getPrivilege());
                roles.add(grantedAuthority);
            }
        }

        return roles;
    }
    
    /** 
     * Spring config setter.
     * @param auth the data access object for authorisation data.
     */
    public void setAuthDAO(UserRoleDAO auth) {
        this.auth = auth;
    }

    public Collection<GrantedAuthority> getGrantedAuthorities(LdapUserDetails userDetails) {
        return getGrantedAuthorities(null, userDetails.getUsername());
    }
}
