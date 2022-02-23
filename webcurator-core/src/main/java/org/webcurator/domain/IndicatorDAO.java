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
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.webcurator.domain.model.core.Indicator;

/**
 * implements the IndicatorDAO Interface and provides the database calls for
 * querying any objects related to indicator criteria. 
 * @author oakleigh_sk
 */
public class IndicatorDAO extends HibernateDaoSupport {
    
    private Log log = LogFactory.getLog(IndicatorDAO.class);
    
    private TransactionTemplate txTemplate = null;

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
    }

    @Transactional
    public Indicator getIndicatorByOid(final Long indicatorOid) {
        return (Indicator)getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {
                        Query query = session.getNamedQuery(Indicator.QRY_GET_INDICATOR_BY_OID);
                        query.setParameter(1, indicatorOid);
                        return query.uniqueResult();
                    }
                }
            );
          
    }

    public List<Indicator> getIndicatorsByTargetInstanceOid(Long targetInstanceOid) {
        List<Indicator> results = getHibernateTemplate().execute(session ->
                session.getNamedQuery(Indicator.QRY_GET_INDICATORS_BY_TI_OID)
                        .setParameter(1, targetInstanceOid)
                        .list());

        return results;
    }

    public void setTxTemplate(TransactionTemplate txTemplate) {
        this.txTemplate = txTemplate;
    }

}
