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

    //private TransactionTemplate txTemplate = null;

    SessionFactory sessionFactory;

    public List getUserDTOs(Long agencyOid) {
        Query q = sessionFactory.getCurrentSession().createNamedQuery(User.QRY_GET_ALL_USER_DTOS_BY_AGENCY);
        q.setParameter(1, agencyOid);
        return q.getResultList();

        /*
        List results = sessionFactory.getCurrentSession().createNamedQuery()
        List results = getHibernateTemplate().execute(session ->
                session.getNamedQuery(User.QRY_GET_ALL_USER_DTOS_BY_AGENCY)
                    .setParameter(1, agencyOid)
                    .list());
        return results;
         */
    }

    public List getUserDTOs() {
        Query q = sessionFactory.getCurrentSession().createNamedQuery(User.QRY_GET_ALL_USER_DTOS);
        return q.getResultList();
        /*
        List results = getHibernateTemplate().execute(session ->
                session.getNamedQuery(User.QRY_GET_ALL_USER_DTOS)
                    .list());
        return results;
         */
    }

    public UserDTO getUserDTOByOid(final Long userOid) {
        Query q = sessionFactory.getCurrentSession().createNamedQuery(User.QRY_GET_USER_DTO_BY_OID);
        q.setParameter(1, userOid);
        return (UserDTO)q.uniqueResult();
        /*
        return (UserDTO)getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {
                        Query query = session.getNamedQuery(User.QRY_GET_USER_DTO_BY_OID);
                        query.setParameter(0,userOid);
                        return query.uniqueResult();
                    }
                }
            );
         */
          
    }

    public List getRoles() {
        Query q = sessionFactory.getCurrentSession().createNamedQuery(Role.QRY_GET_ROLES);
        return q.getResultList();
        /*
        return getHibernateTemplate().execute(session ->
                session.getNamedQuery(Role.QRY_GET_ROLES)
                        .list());
         */
    }

    public List getRoles(Long agencyOid) {
        Query q = sessionFactory.getCurrentSession().createNamedQuery(Role.QRY_GET_ROLES_BY_AGENCY);
        q.setParameter(1, agencyOid);
        return q.getResultList();
        /*
        return getHibernateTemplate().execute(session ->
                session.getNamedQuery(Role.QRY_GET_ROLES_BY_AGENCY)
                        .setParameter(1, agencyOid)
                        .list());
         */
    }

    public User getUserByOid(Long oid) {
        return sessionFactory.getCurrentSession().load(User.class, oid);
//        return (User)getHibernateTemplate().load(User.class,oid);
    }

    public User getUserByName(String username) {
        Query q = sessionFactory.getCurrentSession().createNamedQuery(User.QRY_GET_USER_BY_NAME);
        q.setParameter(1, username);
        List results = q.getResultList();
        /*
        List results = getHibernateTemplate().execute(session ->
                session.getNamedQuery(User.QRY_GET_USER_BY_NAME)
                        .setParameter(1, username)
                        .list());
         */

        if(results.size() == 1) {
            return (User) results.get(0);
        }
        else {
            return null;
        }
    }

    public Agency getAgencyByOid(Long oid) {
        return (Agency)sessionFactory.getCurrentSession().load(Agency.class, oid);
        //return (Agency)getHibernateTemplate().load(Agency.class,oid);
    }
    
    public List getUserPrivileges(String username) {
        Query q = sessionFactory.getCurrentSession().createNamedQuery(RolePrivilege.QRY_GET_USER_PRIVILEGES);
        q.setParameter(1, username);
        return q.getResultList();
        /*
        return getHibernateTemplate().execute(session ->
                session.getNamedQuery(RolePrivilege.QRY_GET_USER_PRIVILEGES)
                        .setParameter(1, username)
                        .list());
         */
    }

    public void saveOrUpdate(final Object aObject) {
        sessionFactory.getCurrentSession().saveOrUpdate(aObject);
        /*
        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try { 
                            log.debug("Before Saving of Object");
                            currentSession().saveOrUpdate(aObject);
                            log.debug("After Saving Object");
                        }
                        catch(Exception ex) {
                            log.warn("Setting Rollback Only",ex);
                            ts.setRollbackOnly();
                        }
                        return null;
                    }
                }
        );
         */
    }
    
//    public void saveOrUpdate(Object aObject) {
//        getHibernateTemplate().saveOrUpdate(aObject);
//    }
    
    public void delete(final Object aObject) {
        sessionFactory.getCurrentSession().delete(aObject);
        /*
        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {
                            log.debug("Before Delete of Object");
                            getHibernateTemplate().delete(aObject);
                            log.debug("After Deletes Object");
                        }
                        catch (DataAccessException e) {
                            log.warn("Setting Rollback Only",e);
                            ts.setRollbackOnly();
                            throw e;
                        }
                        return null;
                    }
                }
        );
         */
    }
    
    public List getUsers() {
        Query q = sessionFactory.getCurrentSession().createQuery("Select u from " + User.class.getCanonicalName()
                                                                    + " u order by u.userName");
        return q.getResultList();
//        return getHibernateTemplate().loadAll(User.class);
        /*
        return (List)getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {
                        Criteria query = session.createCriteria(User.class);
                        query.addOrder(Order.asc("username"));
                        return query.list();
                    }
                }
            );
         */

    }
    
    public List getUsers(Long agencyOid) {
        Query q = sessionFactory.getCurrentSession().createQuery(User.QRY_GET_USERS_BY_AGENCY);
        q.setParameter(1, agencyOid);
        return q.getResultList();
        /*
        return getHibernateTemplate().execute(session ->
                session.getNamedQuery(User.QRY_GET_USERS_BY_AGENCY)
                        .setParameter(1, agencyOid)
                        .list());
         */
    }

    /*
    public void setTxTemplate(TransactionTemplate txTemplate) {
        this.txTemplate = txTemplate;
    }
     */

    public List getAgencies() {
        Query q = sessionFactory.getCurrentSession().createQuery(Agency.QRY_GET_ALL_AGENCIES);
        return q.getResultList();
        /*
        return getHibernateTemplate().execute(session ->
                session.getNamedQuery(Agency.QRY_GET_ALL_AGENCIES)
                        .list());
         */
    }

    public List getAssociatedRolesForUser(Long userOid) {
        Query q = sessionFactory.getCurrentSession().createQuery(Role.QRY_GET_ASSOCIATED_ROLES_BY_USER);
        q.setParameter(1, userOid);
        return q.getResultList();
        /*
        return getHibernateTemplate().execute(session ->
                session.getNamedQuery(Role.QRY_GET_ASSOCIATED_ROLES_BY_USER)
                        .setParameter(1, userOid)
                        .list());
         */
    }

    public Role getRoleByOid(Long oid) {
        return sessionFactory.getCurrentSession().load(Role.class, oid);
        //return (Role)getHibernateTemplate().load(Role.class, oid);
    }

//    public void deleteUserRolesForUser(final User user) {
//        txTemplate.execute(
//                new TransactionCallback() {
//                    public Object doInTransaction(TransactionStatus ts) {
//                        try { 
//                                Set userRoles = user.getRoles();
//                                Iterator it = userRoles.iterator();
//                                while(it.hasNext()) {
//                                    UserRole userRole = (UserRole) it.next();
//                                    userRole.getRole().getUsers().remove(userRole);
//                                    userRole.getUser().getRoles().remove(userRole);
//                                    userRole.setUser(null);
//                                    userRole.setRole(null);
//                                }
//                                getHibernateTemplate().deleteAll(userRoles);        
//                        }
//                        catch(Exception ex) {
//                            log.warn("Setting Rollback Only",ex);
//                            ts.setRollbackOnly();
//                        }
//                        return null;
//                    }
//                }
//        );    
//    }

    @SuppressWarnings("unchecked")
    public List<UserDTO> getUserDTOsByPrivilege(String privilege) {
        Query q = sessionFactory.getCurrentSession().createQuery(User.QRY_GET_USER_DTOS_BY_PRIVILEGE);
        q.setParameter(1, privilege);
        return q.getResultList();
        /*
        return getHibernateTemplate().execute(session ->
                session.getNamedQuery(User.QRY_GET_USER_DTOS_BY_PRIVILEGE)
                        .setParameter(1, privilege)
                        .list());
         */
    }

    /**
     * @see org.webcurator.domain.UserRoleDAO#getUserDTOsByPrivilege(java.lang.String, java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    public List<UserDTO> getUserDTOsByPrivilege(String privilege, Long agencyOid) {
        Query q = sessionFactory.getCurrentSession().createQuery(User.QRY_GET_USER_DTOS_BY_PRIVILEGE_FOR_AGENCY);
        q.setParameter(1, privilege);
        q.setParameter(2, agencyOid);
        return q.getResultList();

        /*
        return getHibernateTemplate().execute(session ->
                session.getNamedQuery(User.QRY_GET_USER_DTOS_BY_PRIVILEGE_FOR_AGENCY)
                        .setParameter(1, privilege)
                        .setParameter(2, agencyOid)
                        .list());
         */
    }
    
    /**
     * @see org.webcurator.domain.UserRoleDAO#getUserDTOsByTargetPrivilege(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
	public List<UserDTO> getUserDTOsByTargetPrivilege(Long permissionOid) {
        Query q = sessionFactory.getCurrentSession().createQuery(User.QRY_GET_USER_DTOS_BY_TARGET_PERMISSION);
        q.setParameter(1, permissionOid);
        return q.getResultList();
        /*
        return getHibernateTemplate().execute(session ->
                session.getNamedQuery(User.QRY_GET_USER_DTOS_BY_TARGET_PERMISSION)
                        .setParameter(1, permissionOid)
                        .list());
         */
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
