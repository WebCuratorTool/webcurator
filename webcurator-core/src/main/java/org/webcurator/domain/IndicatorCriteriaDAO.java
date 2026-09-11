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
import org.webcurator.domain.model.core.IndicatorCriteria;

/**
 * The object for accessing <code>IndicatorCriteria</code>s from the persistent store.
 */
@Transactional
public class IndicatorCriteriaDAO {
    
    private Log log = LogFactory.getLog(IndicatorCriteriaDAO.class);
    
    private TransactionTemplate txTemplate = null;

    private SessionFactory sessionFactory;

    public void saveOrUpdate(final Object aObject) {
        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try { 
                            log.debug("Before Saving of Object");
                            currentSession().persist(aObject);
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

    public IndicatorCriteria getIndicatorCriteriaByOid(final Long indicatorCriteriaOid) {
        Query<IndicatorCriteria> query = currentSession().createNamedQuery(
                IndicatorCriteria.QRY_GET_INDICATOR_CRITERIA_BY_OID, IndicatorCriteria.class);
        query.setParameter(1, indicatorCriteriaOid, Long.class);
        return query.uniqueResult();

    }

    public List<IndicatorCriteria> getIndicatorCriterias() {
        return currentSession()
                .createNamedQuery(IndicatorCriteria.QRY_GET_INDICATOR_CRITERIAS, IndicatorCriteria.class)
                .list();
    }

    public List<IndicatorCriteria> getIndicatorCriteriasByAgencyOid(Long agencyOid) {
        return currentSession()
                .createNamedQuery(IndicatorCriteria.QRY_GET_INDICATOR_CRITERIAS_BY_AGENCY, IndicatorCriteria.class)
                .setParameter(1, agencyOid)
                .list();
    }

    public void setTxTemplate(TransactionTemplate txTemplate) {
        this.txTemplate = txTemplate;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

}
