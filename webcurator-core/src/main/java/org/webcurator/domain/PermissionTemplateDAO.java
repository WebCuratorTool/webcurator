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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.webcurator.domain.model.core.Permission;
import org.webcurator.domain.model.core.PermissionTemplate;

/**
 * Persistance Interface for the managing the Permission Template Request object
 * @author BPrice
 */
public class PermissionTemplateDAO {

    private Log log = LogFactory.getLog(PermissionTemplateDAO.class);
    
    private TransactionTemplate txTemplate;

    private SessionFactory sessionFactory;
    
    public PermissionTemplateDAO() {

    }

    public PermissionTemplate getTemplate(Long oid) {
        return (PermissionTemplate)currentSession().getReference(PermissionTemplate.class,oid);
    }

    public List<PermissionTemplate> getTemplates(Long agencyOid) {
                return currentSession().createNamedQuery(PermissionTemplate.QRY_GET_TEMPLATES_BY_AGENCY, PermissionTemplate.class)
                    .setParameter(1, agencyOid)
                    .list();
    }

    public List<PermissionTemplate> getAllTemplates() {
        Query<PermissionTemplate> q = currentSession().createQuery("from PermissionTemplate", PermissionTemplate.class);
        return q.getResultList();
    }

    public void setTxTemplate(TransactionTemplate txTemplate) {
        this.txTemplate = txTemplate;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void saveOrUpdate(final Object aObject) {
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
        
    }

    public Permission getPermission(Long oid) {
        return (Permission)currentSession().getReference(Permission.class,oid);
    }

    public void delete(final Object aObject) {
        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {
                            log.debug("Before Delete of Object");
                            currentSession().remove(aObject);
                            log.debug("After Delete Object");
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
        
    }

    private Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

}
