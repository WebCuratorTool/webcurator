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
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.webcurator.domain.model.core.Flag;

/**
 * The object for accessing <code>Flag</code>s from the persistent store.
 */
@Transactional
public class FlagDAO {
    
    private Log log = LogFactory.getLog(FlagDAO.class);
    
    private TransactionTemplate txTemplate = null;

    private SessionFactory sessionFactory;

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
    
    public void delete(final Object aObject) {
        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {
                            log.debug("Before Delete of Object");
                            currentSession().remove(aObject);
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
    }

    public Flag getFlagByOid(final Long FlagOid) {
        Query<Flag> query = currentSession().createNamedQuery(Flag.QRY_GET_FLAG_BY_OID, Flag.class);
        query.setParameter(1, FlagOid);
        return query.uniqueResult();
    }
    
    public List<Flag> getFlags() {
        return currentSession().createNamedQuery(Flag.QRY_GET_FLAGS, Flag.class)
                    .list();
    }

    public List<Flag> getFlagsByAgencyOid(Long agencyOid) {
        return currentSession().createNamedQuery(Flag.QRY_GET_FLAGS_BY_AGENCY, Flag.class)
                    .setParameter(1, agencyOid)
                    .list();
    }

    private Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    public void setTxTemplate(TransactionTemplate txTemplate) {
        this.txTemplate = txTemplate;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

}
