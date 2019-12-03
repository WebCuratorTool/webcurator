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
package org.webcurator.domain;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.domain.model.auth.Role;
import org.webcurator.domain.model.auth.RolePrivilege;
import org.webcurator.domain.model.auth.User;
import org.webcurator.domain.model.dto.UserDTO;

import java.util.List;

/**
 * implements the UserRoleDAO Interface and provides the database calls for
 * querying any objects related to Authentication. These include User, Roles
 * and Privilges.
 * @author bprice
 */
@Repository
@Transactional
public class UserRoleDAOImpl implements UserRoleDAO {
    
    private Log log = LogFactory.getLog(UserRoleDAOImpl.class);


    SessionFactory sessionFactory;

    public List getUserDTOs(Long agencyOid) {
        Query q = sessionFactory.getCurrentSession().createNamedQuery(User.QRY_GET_ALL_USER_DTOS_BY_AGENCY);
        q.setParameter(1, agencyOid);
        return q.getResultList();
    }

    public List getUserDTOs() {
        Query q = sessionFactory.getCurrentSession().createNamedQuery(User.QRY_GET_ALL_USER_DTOS);
        return q.getResultList();
    }

    public UserDTO getUserDTOByOid(final Long userOid) {
        Query q = sessionFactory.getCurrentSession().createNamedQuery(User.QRY_GET_USER_DTO_BY_OID);
        q.setParameter(1, userOid);
        return (UserDTO)q.uniqueResult();
    }

    public List getRoles() {
        Query q = sessionFactory.getCurrentSession().createNamedQuery(Role.QRY_GET_ROLES);
        return q.getResultList();
    }

    public List getRoles(Long agencyOid) {
        Query q = sessionFactory.getCurrentSession().createNamedQuery(Role.QRY_GET_ROLES_BY_AGENCY);
        q.setParameter(1, agencyOid);
        return q.getResultList();
    }

    public User getUserByOid(Long oid) {
        return sessionFactory.getCurrentSession().get(User.class, oid);
    }

    public User getUserByName(String username) {
        Query q = sessionFactory.getCurrentSession().createNamedQuery(User.QRY_GET_USER_BY_NAME);
        q.setParameter(1, username);
        List results = q.getResultList();

        if(results.size() == 1) {
            return (User) results.get(0);
        }
        else {
            return null;
        }
    }

    public Agency getAgencyByOid(Long oid) {
        return (Agency)sessionFactory.getCurrentSession().get(Agency.class, oid);
    }
    
    public List getUserPrivileges(String username) {
        Query q = sessionFactory.getCurrentSession().createNamedQuery(RolePrivilege.QRY_GET_USER_PRIVILEGES);
        q.setParameter(1, username);
        return q.getResultList();
    }

    public void saveOrUpdate(final Object aObject) {
        sessionFactory.getCurrentSession().saveOrUpdate(aObject);
    }
    

    public void delete(final Object aObject) {
        sessionFactory.getCurrentSession().delete(aObject);
    }
    
    public List getUsers() {
        Query q = sessionFactory.getCurrentSession().createQuery("Select u from " + User.class.getCanonicalName()
                                                                    + " u order by u.userName");
        return q.getResultList();
    }
    
    public List getUsers(Long agencyOid) {
        Query q = sessionFactory.getCurrentSession().createNamedQuery(User.QRY_GET_USERS_BY_AGENCY);
        q.setParameter(1, agencyOid);
        return q.getResultList();
    }


    public List getAgencies() {
        Query q = sessionFactory.getCurrentSession().createNamedQuery(Agency.QRY_GET_ALL_AGENCIES);
        return q.getResultList();
    }

    public List getAssociatedRolesForUser(Long userOid) {
        Query q = sessionFactory.getCurrentSession().createNamedQuery(Role.QRY_GET_ASSOCIATED_ROLES_BY_USER);
        q.setParameter(1, userOid);
        return q.getResultList();
    }

    public Role getRoleByOid(Long oid) {
        return sessionFactory.getCurrentSession().get(Role.class, oid);
    }


    @SuppressWarnings("unchecked")
    public List<UserDTO> getUserDTOsByPrivilege(String privilege) {
        Query q = sessionFactory.getCurrentSession().createNamedQuery(User.QRY_GET_USER_DTOS_BY_PRIVILEGE);
        q.setParameter(1, privilege);
        return q.getResultList();
    }

    /**
     * @see org.webcurator.domain.UserRoleDAO#getUserDTOsByPrivilege(java.lang.String, java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    public List<UserDTO> getUserDTOsByPrivilege(String privilege, Long agencyOid) {
        Query q = sessionFactory.getCurrentSession().createNamedQuery(User.QRY_GET_USER_DTOS_BY_PRIVILEGE_FOR_AGENCY);
        q.setParameter(1, privilege);
        q.setParameter(2, agencyOid);
        return q.getResultList();
    }
    
    /**
     * @see org.webcurator.domain.UserRoleDAO#getUserDTOsByTargetPrivilege(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
	public List<UserDTO> getUserDTOsByTargetPrivilege(Long permissionOid) {
        Query q = sessionFactory.getCurrentSession().createNamedQuery(User.QRY_GET_USER_DTOS_BY_TARGET_PERMISSION);
        q.setParameter(1, permissionOid);
        return q.getResultList();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
