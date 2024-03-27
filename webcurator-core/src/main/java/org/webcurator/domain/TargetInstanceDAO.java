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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.*;
import org.hibernate.criterion.*;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.webcurator.common.ui.CommandConstants;
import org.webcurator.common.util.SafeSimpleDateFormat;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.util.Auditor;
import org.webcurator.domain.model.core.*;
import org.webcurator.domain.model.dto.HarvestHistoryDTO;
import org.webcurator.domain.model.dto.QueuedTargetInstanceDTO;
import org.webcurator.domain.model.dto.TargetInstanceDTO;

/**
 * The implementation of the TargetInstanceDAO interface.
 *
 * @author nwaight
 */
@SuppressWarnings("all")

/**
 * The object for accessing TargetInstances from the persistent store.
 * @author nwaight
 */
public class TargetInstanceDAO extends HibernateDaoSupport {

    private static SimpleDateFormat fullFormat = SafeSimpleDateFormat.getInstance("dd/MM/yyyy HH:mm:ss");

    private static Log log = LogFactory.getLog(TargetInstanceDAO.class);

    private TransactionTemplate txTemplate = null;

    private Auditor auditor;

    @Transactional
    public void save(final Object aObj) {

        TargetInstanceDTO originalTI = null;
        if (aObj instanceof TargetInstance) {
            TargetInstance targetInstance = (TargetInstance) aObj;
            if (targetInstance.getOid() != null) {
                originalTI = getTargetInstanceDTO(targetInstance.getOid());
            }

            if (log.isDebugEnabled()) {
                log.debug("About to save Target Instance: " + targetInstance.getOid());
            }
        }

        try {
            txTemplate.execute(
                    new TransactionCallback() {
                        public Object doInTransaction(TransactionStatus ts) {
                            try {
                                log.debug("Before Saving Object");
                                currentSession().saveOrUpdate(aObj);
                                log.debug("After Saving Object");
                            } catch (Exception ex) {
                                log.warn("Setting Rollback Only " + ex.getMessage(), ex);
                                ts.setRollbackOnly();
                            }
                            return null;
                        }
                    }
            );
        } catch (Exception e) {
            if (originalTI != null) {
                log.error("Failed to save Target Instance: " + originalTI.getOid(), e);
            } else {
                log.error(e);
            }
        }

        if (aObj instanceof TargetInstance) {
            TargetInstance targetInstance = (TargetInstance) aObj;
            if (originalTI == null) {
                auditor.audit(TargetInstance.class.getName(), targetInstance.getOid(), Auditor.ACTION_NEW_TARGET_INSTANCE, "The TargetInstance '" + targetInstance.getOid() + "' has been created");
            } else {
                auditor.audit(TargetInstance.class.getName(), targetInstance.getOid(), Auditor.ACTION_UPDATE_TARGET_INSTANCE, "The TargetInstance '" + targetInstance.getOid() + "' has been updated");
                if (!originalTI.getState().equals(targetInstance.getState())) {
                    auditor.audit(TargetInstance.class.getName(), targetInstance.getOid(), Auditor.ACTION_STATE_CHANGE_TARGET_INSTANCE, "The TargetInstance '" + targetInstance.getOid() + "' state has changed from " + originalTI.getState() + " to " + targetInstance.getState());
                }

                if (!originalTI.getOwnerOid().equals(targetInstance.getOwner().getOid())) {
                    auditor.audit(TargetInstance.class.getName(), targetInstance.getOid(), Auditor.ACTION_OWNER_CHANGE_TARGET_INSTANCE, "The TargetInstance '" + targetInstance.getOid() + "' owner has changed.");
                }
            }
        }
    }

    @Transactional
    public void delete(final Object aObject) {
        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {
                            log.debug("Before Delete Object");
                            currentSession().delete(aObject);
                            log.debug("After Delete Object");
                        } catch (Exception ex) {
                            log.warn("Setting Rollback Only");
                            ts.setRollbackOnly();
                        }
                        return null;
                    }
                }
        );
    }

    @Transactional
    public void deleteHarvestResources(final Long targetInstanceId) {
        getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session aSession) {

                List<HarvestResult> hrs = getHarvestResults(targetInstanceId);
                Iterator<HarvestResult> it = hrs.iterator();
                while (it.hasNext()) {
                    final HarvestResult hr = it.next();
                    getHibernateTemplate().initialize(hr);
                }
                return null;
            }
        });
    }

    @Transactional
    public void deleteHarvestResultResources(final Long harvestResultId) {
        log.info("Deleting harvest result resources for result: " + harvestResultId);
        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {
                            // Note: this DELETE cascades to ARC_HARVEST_RESOURCE as per the db schema
                            log.debug("Before deleting harvest result resources");
                            currentSession().createNativeQuery("DELETE FROM {h-schema}HARVEST_RESOURCE WHERE HRC_HARVEST_RESULT_OID=?1")
                                    .setParameter(1, harvestResultId).executeUpdate();
                            log.debug("After deleting harvest result resources");
                        } catch (Exception ex) {
                            log.warn("Problem occured deleting HarvestResource records", ex);
                            log.warn("Setting Rollback Only for delete of harvest result resources");
                            ts.setRollbackOnly();
                        }
                        return null;
                    }
                }
        );
    }

    @Transactional
    public void deleteHarvestResultFiles(final Long harvestResultId) {
        log.info("Deleting harvest result files for result: " + harvestResultId);
        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {
                            log.debug("Before deleting harvest result files");
                            currentSession().createQuery("DELETE ArcHarvestFile WHERE arcHarvestResult.oid=:hrOid").setLong("hrOid", harvestResultId)
                                    .executeUpdate();
                            log.debug("After deleting harvest result files");
                        } catch (Exception ex) {
                            log.warn("Problem occured deleting ArcHarvestFile records", ex);
                            log.warn("Setting Rollback Only for delete of harvest result files");
                            ts.setRollbackOnly();
                        }
                        return null;
                    }
                }
        );
    }

    @Transactional
    public void saveAll(final Collection coll) {
        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {
                            log.debug("Before Saving Object");
                            for (Object o : coll) {
                                currentSession().saveOrUpdate(o);
                            }
                            log.debug("After Saving Object");
                        } catch (Exception ex) {
                            log.warn("Setting Rollback Only");
                            ts.setRollbackOnly();
                        }
                        return null;
                    }
                }
        );
    }


    public TargetInstance load(final long targetInstanceOid) {
        try {
            TargetInstance t = (TargetInstance) getHibernateTemplate().load(TargetInstance.class, targetInstanceOid);
            t.getState(); // Workaround for bug in HibernateTemplate: its load method does not throw an exception, but returns a defunct entity
            return t;
        } catch (LazyInitializationException e) {
            return null;
        }
    }


    public HarvestResult getHarvestResult(final Long harvestResultOid) {
        return getHarvestResult(harvestResultOid, true);
    }

    public HarvestResult getHarvestResult(final Long harvestResultOid, final boolean loadFully) {
        HarvestResult hr = (HarvestResult) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session aSession) {
                HarvestResult hr = aSession.load(HarvestResult.class, harvestResultOid);

                // Force population of the resources and target instance
                if (loadFully) {
                    hr.getTargetInstance();
                }

                return hr;
            }
        });

        return hr;
    }

    @SuppressWarnings("unchecked")
    public List<HarvestResult> getHarvestResults(final long targetInstanceId) {
        return (List<HarvestResult>) getHibernateTemplate().find("select hr from HarvestResult hr where hr.targetInstance.oid=?0 order by hr.harvestNumber", targetInstanceId);
    }

    public Pagination search(final TargetInstanceCriteria aCriteria, final int aPage, final int aPageSize) {
        return (Pagination) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {
                        Criteria query = session.createCriteria(TargetInstance.class);
                        Criteria cntQuery = session.createCriteria(TargetInstance.class);

                        //To ignore duplicated data
                        query.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
                        cntQuery.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

                        Date from = aCriteria.getFrom();
                        if (null == from) {
                            try {
                                from = fullFormat.parse("01/01/1970 00:00:00");
                            } catch (ParseException e) {
                                if (log.isWarnEnabled()) {
                                    log.warn("Failed to parse default from date.");
                                }
                            }
                        }

                        Date to = aCriteria.getTo();
                        if (null == to) {
                            try {
                                to = fullFormat.parse("31/12/9999 23:59:59");
                            } catch (ParseException e) {
                                if (log.isWarnEnabled()) {
                                    log.warn("Failed to parse default from date.");
                                }
                            }
                        }

                        query.add(Expression.between("scheduledTime", from, to));
                        cntQuery.add(Expression.between("scheduledTime", from, to));

                        if (aCriteria.getStates() != null && !aCriteria.getStates().isEmpty()) {
                            Disjunction stateDisjunction = Restrictions.disjunction();
                            for (String s : aCriteria.getStates()) {
                                stateDisjunction.add(Restrictions.eq("state", s));
                            }
                            query.add(stateDisjunction);
                            cntQuery.add(stateDisjunction);
                        }

                        if (aCriteria.getRecommendationFilter() != null && !aCriteria.getRecommendationFilter().isEmpty()) {
                            Disjunction recommendationDisjunction = Restrictions.disjunction();
                            for (String s : aCriteria.getRecommendationFilter()) {
                                recommendationDisjunction.add(Restrictions.eq("recommendation", s));
                            }
                            query.add(recommendationDisjunction);
                            cntQuery.add(recommendationDisjunction);
                        }

                        Criteria owner = null;
                        Criteria cntOwner = null;
                        if (aCriteria.getOwner() != null && !aCriteria.getOwner().trim().equals("")) {
                            owner = query.createCriteria("owner").add(Restrictions.eq("username", aCriteria.getOwner()));
                            cntOwner = cntQuery.createCriteria("owner").add(Restrictions.eq("username", aCriteria.getOwner()));
                        }

                        if (aCriteria.getAgency() != null && !aCriteria.getAgency().trim().equals("")) {
                            if (null == owner) {
                                query.createCriteria("owner").createCriteria("agency").add(Restrictions.eq("name", aCriteria.getAgency()));
                                cntQuery.createCriteria("owner").createCriteria("agency").add(Restrictions.eq("name", aCriteria.getAgency()));
                            } else {
                                owner.createCriteria("agency").add(Restrictions.eq("name", aCriteria.getAgency()));
                                cntOwner.createCriteria("agency").add(Restrictions.eq("name", aCriteria.getAgency()));
                            }
                        }

                        if (aCriteria.getName() != null && !aCriteria.getName().trim().equals("")) {
                            query.createCriteria("target").add(Restrictions.ilike("name", aCriteria.getName(), MatchMode.START));
                            cntQuery.createCriteria("target").add(Restrictions.ilike("name", aCriteria.getName(), MatchMode.START));
                        }

                        if (aCriteria.getSearchOid() != null && aCriteria.getTargetSearchOid() == null) {
                            query.add(Restrictions.eq("oid", aCriteria.getSearchOid()));
                            cntQuery.add(Restrictions.eq("oid", aCriteria.getSearchOid()));
                        }

                        if (aCriteria.getTargetSearchOid() != null) {
                            query.createAlias("target", "t");
                            cntQuery.createAlias("target", "t");
                            query.add(Restrictions.eq("t.oid", aCriteria.getTargetSearchOid()));
                            cntQuery.add(Restrictions.eq("t.oid", aCriteria.getTargetSearchOid()));
                            // if the search oid is supplied, then we start the search at this oid
                            if (aCriteria.getSearchOid() != null) {
                                query.add(Restrictions.le("oid", aCriteria.getSearchOid()));
                                cntQuery.add(Restrictions.le("oid", aCriteria.getSearchOid()));
                            }
                        }

                        if (aCriteria.getFlagged()) {
                            query.add(Restrictions.eq("flagged", aCriteria.getFlagged()));
                            cntQuery.add(Restrictions.eq("flagged", aCriteria.getFlagged()));
                        }

                        if (aCriteria.getFlag() != null) {
                            query.add(Restrictions.eq("flag", aCriteria.getFlag()));
                            cntQuery.add(Restrictions.eq("flag", aCriteria.getFlag()));
                        }

                        if (aCriteria.getNondisplayonly()) {
                            query.add(Restrictions.eq("display", false));
                            cntQuery.add(Restrictions.eq("display", false));
                        }

                        if (aCriteria.getSortorder() == null ||
                                aCriteria.getSortorder().equals(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_DEFAULT)) {
                            // use defaults
                            query.addOrder(Order.asc("displayOrder"));
                            query.addOrder(Order.asc("sortOrderDate"));
                            query.addOrder(Order.asc("priority"));
                            query.addOrder(Order.asc("oid"));
                        } else if (aCriteria.getSortorder().equals(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_NAME_ASC)) {
                            query.createAlias("target", "t");
                            query.addOrder(Order.asc("t.name"));
                        } else if (aCriteria.getSortorder().equals(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_NAME_DESC)) {
                            query.createAlias("target", "t");
                            query.addOrder(Order.desc("t.name"));
                        } else if (aCriteria.getSortorder().equals(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_DATE_ASC)) {
                            query.addOrder(Order.asc("sortOrderDate"));
                        } else if (aCriteria.getSortorder().equals(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_DATE_DESC)) {
                            query.addOrder(Order.desc("sortOrderDate"));
                        } else if (aCriteria.getSortorder().equals(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_STATE_ASC)) {
                            query.addOrder(Order.asc("state"));
                        } else if (aCriteria.getSortorder().equals(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_STATE_DESC)) {
                            query.addOrder(Order.desc("state"));
                        } else if (aCriteria.getSortorder().equals(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_ELAPSEDTIME_ASC)) {
                            query.createAlias("status", "hs");
                            query.addOrder(Order.asc("hs.elapsedTime"));
                        } else if (aCriteria.getSortorder().equals(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_ELAPSEDTIME_DESC)) {
                            query.createAlias("status", "hs");
                            query.addOrder(Order.desc("hs.elapsedTime"));
                        } else if (aCriteria.getSortorder().equals(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_DATADOWNLOADED_ASC)) {
                            query.createAlias("status", "hs");
                            query.addOrder(Order.asc("hs.dataDownloaded"));
                        } else if (aCriteria.getSortorder().equals(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_DATADOWNLOADED_DESC)) {
                            query.createAlias("status", "hs");
                            query.addOrder(Order.desc("hs.dataDownloaded"));
                        } else if (aCriteria.getSortorder().equals(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_URLSSUCCEEDED_ASC)) {
                            query.createAlias("status", "hs");
                            query.addOrder(Order.asc("hs.urlsSucceeded"));
                        } else if (aCriteria.getSortorder().equals(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_URLSSUCCEEDED_DESC)) {
                            query.createAlias("status", "hs");
                            query.addOrder(Order.desc("hs.urlsSucceeded"));
                        } else if (aCriteria.getSortorder().equals(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_PERCENTAGEURLSFAILED_ASC)) {
                            query.createAlias("status", "hs");
                            query.addOrder(Order.asc("hs.percentageUrlsFailed"));
                        } else if (aCriteria.getSortorder().equals(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_PERCENTAGEURLSFAILED_DESC)) {
                            query.createAlias("status", "hs");
                            query.addOrder(Order.desc("hs.percentageUrlsFailed"));
                        } else if (aCriteria.getSortorder().equals(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_CRAWLS_ASC)) {
                            query.createAlias("target", "t");
                            query.addOrder(Order.asc("t.crawls"));
                        } else if (aCriteria.getSortorder().equals(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_CRAWLS_DESC)) {
                            query.createAlias("target", "t");
                            query.addOrder(Order.desc("t.crawls"));
                        } else if (aCriteria.getSortorder().equals(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_DATE_DESC_BY_TARGET_OID)) {
                            query.addOrder(Order.desc("sortOrderDate"));
                        }


                        cntQuery.setProjection(Projections.rowCount());

                        return new Pagination(cntQuery, query, aPage, aPageSize);
                    }
                }
        );
    }

    @SuppressWarnings("unchecked")
    public List findTargetInstances(final TargetInstanceCriteria aCriteria) {
        return (List) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {
                        Criteria query = session.createCriteria(TargetInstance.class);

                        Date from = aCriteria.getFrom();
                        if (null == from) {
                            try {
                                from = fullFormat.parse("01/01/1970 00:00:00");
                            } catch (ParseException e) {
                                if (log.isWarnEnabled()) {
                                    log.warn("Failed to parse default from date.");
                                }
                            }
                        }

                        Date to = aCriteria.getTo();
                        if (null == to) {
                            try {
                                to = fullFormat.parse("31/12/9999 23:59:59");
                            } catch (ParseException e) {
                                if (log.isWarnEnabled()) {
                                    log.warn("Failed to parse default from date.");
                                }
                            }
                        }

                        query.add(Expression.between("scheduledTime", from, to));

                        if (aCriteria.getStates() != null && !aCriteria.getStates().isEmpty()) {
                            Disjunction stateDisjunction = Restrictions.disjunction();
                            for (String s : aCriteria.getStates()) {
                                stateDisjunction.add(Restrictions.eq("state", s));
                            }
                            query.add(stateDisjunction);
                        }

                        Criteria owner = null;
                        if (aCriteria.getOwner() != null && !aCriteria.getOwner().trim().equals("")) {
                            owner = query.createCriteria("owner").add(Restrictions.eq("username", aCriteria.getOwner()));
                        }

                        if (aCriteria.getAgency() != null && !aCriteria.getAgency().trim().equals("")) {
                            if (null == owner) {
                                query.createCriteria("owner").createCriteria("agency").add(Restrictions.eq("name", aCriteria.getAgency()));
                            } else {
                                owner.createCriteria("agency").add(Restrictions.eq("name", aCriteria.getAgency()));
                            }
                        }

                        query.addOrder(Order.asc("displayOrder"));
                        query.addOrder(Order.asc("scheduledTime"));
                        query.addOrder(Order.asc("oid"));

                        return query.list();
                    }
                }
        );
    }

    @SuppressWarnings("unchecked")
    public List<TargetInstance> findPurgeableTargetInstances(final Date aPurgeDate) {
        return (List<TargetInstance>) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {
                        Query query = session.getNamedQuery(TargetInstance.QRY_GET_PURGEABLE_TIS);
                        query.setTimestamp(TargetInstance.QRY_PARAM_PURGE_TIME, aPurgeDate);
                        query.setString(TargetInstance.QRY_PARAM_ARCHIVED_STATE, TargetInstance.STATE_ARCHIVED);
                        query.setString(TargetInstance.QRY_PARAM_REJECTED_STATE, TargetInstance.STATE_REJECTED);

                        return query.list();
                    }
                }
        );
    }

    @SuppressWarnings("unchecked")
    public List<TargetInstance> findPurgeableAbortedTargetInstances(final Date aPurgeDate) {
        return (List<TargetInstance>) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {
                        Query query = session.getNamedQuery(TargetInstance.QRY_GET_PURGEABLE_ABORTED_TIS);
                        query.setTimestamp(TargetInstance.QRY_PARAM_PURGE_TIME, aPurgeDate);
                        query.setString(TargetInstance.QRY_PARAM_ABORTED_STATE, TargetInstance.STATE_ABORTED);

                        return query.list();
                    }
                }
        );
    }

    @SuppressWarnings("unchecked")
    public List<QueuedTargetInstanceDTO> getQueue() {
        final TargetInstanceCriteria criteria = new TargetInstanceCriteria();
        Set<String> states = new HashSet<String>();
        states.add(TargetInstance.STATE_SCHEDULED);
        states.add(TargetInstance.STATE_QUEUED);
        states.add(TargetInstance.STATE_PATCHING);
        criteria.setStates(states);
        criteria.setTo(new Date());

        return (List) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {

                        StringBuffer q = new StringBuffer();
                        q.append("select new org.webcurator.domain.model.dto.QueuedTargetInstanceDTO(ti.oid, ti.scheduledTime, ti.priority, ti.state, ti.bandwidthPercent, ti.owner.agency.name) ");
                        q.append("from TargetInstance ti where ti.scheduledTime <= :ed ");
                        q.append("and ti.state in ('Scheduled', 'Queued', 'ModScheduled', 'ModQueued') ");
                        q.append("order by ti.priority asc, ti.scheduledTime asc, ti.oid asc ");

                        Query query = session.createQuery(q.toString());

                        query.setTimestamp("ed", new Date());

                        return query.list();
                    }
                }
        );
    }

    @SuppressWarnings("unchecked")
    public List<QueuedTargetInstanceDTO> getUpcomingJobs(final long futureMs) {
        return (List) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {

                        StringBuffer q = new StringBuffer();
                        q.append("select new org.webcurator.domain.model.dto.QueuedTargetInstanceDTO(ti.oid, ti.scheduledTime, ti.priority, ti.state, ti.bandwidthPercent, ti.owner.agency.name) ");
                        q.append("from TargetInstance ti where ti.scheduledTime <= :ed ");
                        q.append("and ti.state in ('Scheduled', 'Queued') ");
                        q.append("order by ti.priority asc, ti.scheduledTime asc, ti.oid asc ");

                        Query query = session.createQuery(q.toString());

                        query.setTimestamp("ed", new Date(System.currentTimeMillis() + futureMs));

                        return query.list();
                    }
                }
        );
    }

    @SuppressWarnings("unchecked")
    public List<QueuedTargetInstanceDTO> getQueueForTarget(final Long targetOid) {
        final TargetInstanceCriteria criteria = new TargetInstanceCriteria();
        Set<String> states = new HashSet<String>();
        states.add(TargetInstance.STATE_SCHEDULED);
        states.add(TargetInstance.STATE_QUEUED);
        criteria.setStates(states);
        criteria.setTo(new Date());

        return (List) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {

                        StringBuffer q = new StringBuffer();
                        q.append("select new org.webcurator.domain.model.dto.QueuedTargetInstanceDTO(ti.oid, ti.scheduledTime, ti.priority, ti.state, ti.bandwidthPercent, ti.owner.agency.name) ");
                        q.append("from TargetInstance ti where ti.scheduledTime > :ed ");
                        q.append("and ti.state in ('Scheduled', 'Queued') and ti.target.oid = :toid ");
                        q.append("order by ti.priority asc, ti.scheduledTime asc, ti.oid asc ");

                        Query query = session.createQuery(q.toString());

                        query.setDate("ed", new Date());
                        query.setLong("toid", targetOid);

                        return query.list();
                    }
                }
        );
    }

    public Long countQueueLengthForTarget(final Long targetOid) {
        final TargetInstanceCriteria criteria = new TargetInstanceCriteria();
        Set<String> states = new HashSet<String>();
        states.add(TargetInstance.STATE_SCHEDULED);
        states.add(TargetInstance.STATE_QUEUED);
        criteria.setStates(states);
        criteria.setTo(new Date());

        return (Long) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {

                        StringBuffer q = new StringBuffer();
                        q.append("select count(*) ");
                        q.append("from TargetInstance ti where ti.scheduledTime > :ed ");
                        q.append("and ti.state in ('Scheduled', 'Queued') and ti.target.oid = :toid ");

                        Query query = session.createQuery(q.toString());

                        query.setDate("ed", new Date());
                        query.setLong("toid", targetOid);

                        return query.list().get(0);
                    }
                }
        );
    }


    public TargetInstance populate(final TargetInstance aTargetInstance) {
        TargetInstance ti = (TargetInstance) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session aSession) throws HibernateException {
                TargetInstance ati = (TargetInstance) aSession.load(TargetInstance.class, aTargetInstance.getOid());
                if (ati != null) {
                    Hibernate.initialize(ati.getOriginalSeeds());
                    Hibernate.initialize(ati.getSchedule());

                    if (ati.getTarget() != null) {
                        Hibernate.initialize(ati.getTarget().getSeeds());
                        if (ati.getTarget().getOwner() != null) {
                            Hibernate.initialize(ati.getTarget().getOwner().getAgency());
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("ati.getTarget().getOwner()==null for targetInstance " + ati.getOid());
                            }
                        }
                        Hibernate.initialize(ati.getTarget().getProfile());
                        Hibernate.initialize(ati.getTarget().getOverrides());
                        if (ati.getTarget().getOverrides() != null) {
                            Hibernate.initialize(ati.getTarget().getOverrides().getExcludedMimeTypes());
                            Hibernate.initialize(ati.getTarget().getOverrides().getExcludeUriFilters());
                            Hibernate.initialize(ati.getTarget().getOverrides().getIncludeUriFilters());
                            Hibernate.initialize(ati.getTarget().getOverrides().getCredentials());
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("ati.getTarget().getOverrides()==null for targetInstance " + ati.getOid());
                            }
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("ati.getTarget()==null for targetInstance " + ati.getOid());
                        }
                    }

                    Hibernate.initialize(ati.getHarvestResults());
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("ati==null");
                    }
                }

                return ati;
            }
        });

        return ti;
    }

    /**
     * @param txTemplate The txTemplate to set.
     */
    public void setTxTemplate(TransactionTemplate txTemplate) {
        this.txTemplate = txTemplate;
    }


    @Transactional
    public void deleteScheduledInstances(final Schedule aSchedule) {
        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {
                            log.debug("Before deleting scheduled instances");
                            currentSession().createQuery("DELETE TargetInstance WHERE state=:state AND schedule.oid=:scheduleOid")
                                    .setParameter("state", TargetInstance.STATE_SCHEDULED)
                                    .setParameter("scheduleOid", aSchedule.getOid())
                                    .executeUpdate();
                            log.debug("After deleting scheduled instances");
                        } catch (Exception ex) {
                            log.debug("Setting Rollback Only for delete of scheduled instances");
                            ts.setRollbackOnly();
                        }
                        return null;
                    }
                }
        );
    }

    @Transactional
    public void deleteScheduledInstances(final AbstractTarget anAbstractTarget) {
        // Remove all the target instances from one of the schedules.
        txTemplate.execute(new UnscheduleTargetTransaction(anAbstractTarget));


        // Reload the target from the database.
        getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session aSession) {
                        aSession.refresh(anAbstractTarget);
                        return null;
                    }
                });

    }

    /**
     * Detect and update TargetGroups that must be made inactive due to their
     * end date having been passed.
     */
    public void endDateGroups() {
        Date startTime = new Date();
        log.info("Starting Job to check end dates on groups");

        getHibernateTemplate().execute(new HibernateCallback() {
            @SuppressWarnings("unchecked")
            public Object doInHibernate(Session aSession) throws HibernateException {
                List<TargetGroup> groupsToEnd = aSession.createCriteria(TargetGroup.class)
                        .add(Restrictions.ne("state", TargetGroup.STATE_INACTIVE))
                        .add(Restrictions.lt("toDate", new Date()))
                        .list();

                for (TargetGroup group : groupsToEnd) {
                    deleteScheduledInstances(group);
                    group.changeState(TargetGroup.STATE_INACTIVE);
                }

                return null;
            }

        });

        log.info("Completed Job to check end dates on group: took " + (new Date().getTime() - startTime.getTime()) + "ms");
    }


    private class UnscheduleTargetTransaction implements TransactionCallback {
        private AbstractTarget target;

        private List<Schedule> schedules = new LinkedList<Schedule>();

        public UnscheduleTargetTransaction(AbstractTarget aTarget) {
            target = aTarget;
        }

        private void collectSchedules(AbstractTarget aTarget) {
            Map<Object, Object> duplicateValidator = new HashMap<>();
            collectSchedulesInternal(aTarget, duplicateValidator);
            duplicateValidator.clear();
        }

        private void collectSchedulesInternal(AbstractTarget aTarget, Map<Object, Object> duplicateValidator) {
            if (duplicateValidator.containsKey(aTarget)) {
                return;
            } else {
                duplicateValidator.put(aTarget, aTarget);
            }
            // Add all the schedules from this target.
            schedules.addAll(aTarget.getSchedules());

            // Get all the schedules from parents.
            for (GroupMember gm : aTarget.getParents()) {
                collectSchedulesInternal(gm.getParent(), duplicateValidator);
            }

        }


        private void removeSchedules(final AbstractTarget aTarget) {
            Map<Object, Object> duplicateValidator = new HashMap<>();
            removeSchedulesInternal(aTarget, duplicateValidator);
            duplicateValidator.clear();
        }

        private void removeSchedulesInternal(final AbstractTarget aTarget, final Map<Object, Object> duplicateValidator) {
            if (duplicateValidator.containsKey(aTarget)) {
                return;
            } else {
                duplicateValidator.put(aTarget, aTarget);
            }

            // Delete the scheduled instances from this target.
            getHibernateTemplate().execute(new HibernateCallback() {
                public Object doInHibernate(Session aSession) {
                    for (Schedule aSchedule : schedules) {
                        currentSession().createQuery("DELETE TargetInstance WHERE state=:state AND schedule.oid=:scheduleOid AND target.oid=:targetOid")
                                .setParameter("state", TargetInstance.STATE_SCHEDULED)
                                .setParameter("scheduleOid", aSchedule.getOid())
                                .setParameter("targetOid", aTarget.getOid())
                                .executeUpdate();
                    }

                    return null;
                }
            });

            // Delete all the scheduled instances from child targets.
            if (aTarget.getObjectType() == AbstractTarget.TYPE_GROUP) {
                AbstractTarget candidateGroup = aTarget;
                if (!(candidateGroup instanceof TargetGroup)) {
                    TargetDAO targetDAO = new TargetDAO();
                    targetDAO.setHibernateTemplate(getHibernateTemplate());
                    targetDAO.setTxTemplate(txTemplate);
                    targetDAO.setSessionFactory(getSessionFactory());
                    candidateGroup = targetDAO.loadGroup(candidateGroup.getOid());
                }

                for (GroupMember child : ((TargetGroup) candidateGroup).getChildren()) {
                    removeSchedulesInternal(child.getChild(), duplicateValidator);
                }
            }
        }

        public Object doInTransaction(TransactionStatus tx) {
            try {
                collectSchedules(target);
                removeSchedules(target);
                return null;
            } catch (Exception ex) {
                tx.setRollbackOnly();
                log.error("Failed to remove scheduled target instances", ex);
                throw new WCTRuntimeException("Failed to removed schedules target instances", ex);
            }
        }

    }


    /**
     * Delete TargetInstances for the specified Target and Schedule
     *
     * @param targetOid   The target OID.
     * @param scheduleOid The schedule OID.
     */
    @Transactional
    public void deleteScheduledInstances(final Long targetOid, final Long scheduleOid) {
        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {
                            log.debug("Before deleting scheduled instance for " + targetOid + " " + scheduleOid);
                            currentSession().createQuery("DELETE TargetInstance WHERE state=:state AND schedule.oid=:scheduleOid AND target.oid=:targetOid")
                                    .setParameter("state", TargetInstance.STATE_SCHEDULED)
                                    .setParameter("scheduleOid", scheduleOid)
                                    .setParameter("targetOid", targetOid)
                                    .executeUpdate();
                            log.debug("After deleting scheduled instance for " + targetOid + " " + scheduleOid);
                        } catch (Exception ex) {
                            log.debug("Setting Rollback Only");
                            ts.setRollbackOnly();
                        }
                        return null;
                    }
                }
        );
    }

    public long countTargetInstances(final String aUsername, final ArrayList<String> aStates) {
        return (Long) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {
                        Criteria query = session.createCriteria(TargetInstance.class);
                        query.setProjection(Projections.rowCount());

                        if (aStates != null && !aStates.isEmpty()) {
                            Disjunction stateDisjunction = Restrictions.disjunction();
                            for (String s : aStates) {
                                stateDisjunction.add(Restrictions.eq("state", s));
                            }
                            query.add(stateDisjunction);
                        }

                        query.createCriteria("owner").add(Restrictions.eq("username", aUsername));


                        Long count = (Long) query.uniqueResult();

                        return count;
                    }
                }
        );
    }

    public long countActiveTIsForTarget(final Long targetOid) {
        return (Long) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {
                        Criteria query = session.createCriteria(TargetInstance.class);
                        query.setProjection(Projections.rowCount());

                        Disjunction stateDisjunction = Restrictions.disjunction();

                        stateDisjunction.add(Restrictions.eq("state", TargetInstance.STATE_SCHEDULED));
                        stateDisjunction.add(Restrictions.eq("state", TargetInstance.STATE_QUEUED));
                        stateDisjunction.add(Restrictions.eq("state", TargetInstance.STATE_RUNNING));
                        stateDisjunction.add(Restrictions.eq("state", TargetInstance.STATE_PAUSED));
                        stateDisjunction.add(Restrictions.eq("state", TargetInstance.STATE_STOPPING));

                        query.add(stateDisjunction);

                        //query.createAlias("target", "t");
                        query.createCriteria("target").add(Restrictions.eq("oid", targetOid));

                        Long count = (Long) query.uniqueResult();

                        return count;
                    }
                }
        );
    }

    public long countTargetInstancesByTarget(final Long targetOid) {
        return (Long) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {
                        Criteria query = session.createCriteria(TargetInstance.class);
                        query.setProjection(Projections.rowCount());

                        query.createCriteria("target").add(Restrictions.eq("oid", targetOid));

                        Long count = (Long) query.uniqueResult();

                        return count;
                    }
                }
        );
    }

    /**
     * Return the DTO for the specified Target Instance.
     *
     * @param aOid the oid of the target instance DTO to return
     * @return the target instance DTO
     */
    public TargetInstanceDTO getTargetInstanceDTO(final Long aOid) {
        if (log.isDebugEnabled()) {
            log.debug("Get DTO for target instance: " + aOid);
        }
        return (TargetInstanceDTO) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {

                        StringBuffer q = new StringBuffer();
                        q.append("select new org.webcurator.domain.model.dto.TargetInstanceDTO(ti.oid, ti.scheduledTime, ti.priority, ti.state, ti.owner.oid) ");
                        q.append("from TargetInstance ti where ti.oid = :oid ");

                        Query query = session.createQuery(q.toString());

                        query.setParameter("oid", aOid);
                        query.setReadOnly(true);

                        return query.uniqueResult();
                    }
                }
        );
    }

    /**
     * @param auditor the auditor to set
     */
    public void setAuditor(Auditor auditor) {
        this.auditor = auditor;
    }

    public List<HarvestHistoryDTO> getHarvestHistory(final Long targetOid) {
        return (List<HarvestHistoryDTO>) getHibernateTemplate().execute(session ->
                session.getNamedQuery(TargetInstance.QRY_GET_HARVEST_HISTORY)
                        .setParameter(1, targetOid)
                        .list());
    }
}
