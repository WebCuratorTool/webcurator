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

import jakarta.persistence.criteria.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.webcurator.common.ui.CommandConstants;
import org.webcurator.common.ui.Constants;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.domain.model.core.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * The interface used for accessing persistent Harvest Authorisation data.
 * @author bbeaumont
 */
@Transactional
public class SiteDAO extends HibernateDaoSupport {
	private Log log = LogFactory.getLog(SiteDAO.class);
	
	private TransactionTemplate txTemplate = null;
	
	/**
	 * @param txTemplate The txTemplate to set.
	 */
	public void setTxTemplate(TransactionTemplate txTemplate) {
		this.txTemplate = txTemplate;
	}	
	
	public void saveOrUpdate(final Site aSite) {
		txTemplate.execute(
				new TransactionCallback() {
					public Object doInTransaction(TransactionStatus ts) {
						log.debug("Before Saving of Site");
							
						try {
							for(AuthorisingAgent agent: aSite.getAuthorisingAgents()) {
								currentSession().saveOrUpdate(agent);
							}
							currentSession().saveOrUpdate(aSite);
						}
						catch(Exception ex) {
							ts.setRollbackOnly();
							log.error(ex.getMessage(), ex);
							throw new WCTRuntimeException(ex.getMessage(), ex);
						}
						
						log.debug("After Saving Site");
						
						return null;
					}
				}
		);
		
		
	}
	
	public Site load(final long siteOid) {
		return load(siteOid, false);
	}
	
	public Site load(final long siteOid, boolean fullyInitialise) {
		if( !fullyInitialise) {
			return (Site) getHibernateTemplate().load(Site.class, siteOid);
		}
		else {
			Site site = (Site) getHibernateTemplate().load(Site.class, siteOid);
			
			// Initialise some more items that we'll need. This is used
			// to prevent lazy load exceptions, since we're doing things
			// across multiple sessions.
			for(Permission p : site.getPermissions()) {
				Hibernate.initialize(p.getUrls());
			}
			
			for(UrlPattern p : site.getUrlPatterns()) {
				Hibernate.initialize(p.getPermissions());
			}	
			
			return site;
		}	
	}
	
	/**
	 * Load an authorising agent from the database.
	 * @param authAgentOid The OID of the authorising agent to load.
	 * @return The authorising agent.
	 */
	public AuthorisingAgent loadAuthorisingAgent(final long authAgentOid) {
		return (AuthorisingAgent) getHibernateTemplate().load(AuthorisingAgent.class, authAgentOid);
	}	

	@SuppressWarnings("unchecked")
	@Transactional
	public List<Permission> getQuickPickPermissions(Agency anAgency) {
        CriteriaBuilder cb = currentSession().getCriteriaBuilder();
        CriteriaQuery<Permission> query = cb.createQuery(Permission.class);
        Root<Permission> root = query.from(Permission.class);
        query.select(root);

        Predicate datePredicate = cb.or(cb.isNull(root.get("endDate")),
                cb.greaterThanOrEqualTo(root.get("endDate"), new Date()));
        Predicate quickPickPredicate = cb.equal(root.get("quickPick"), true);
        Predicate owningAgencyPredicate = cb.equal(root.get("owningAgency").get("oid"), anAgency.getOid());

        Predicate whereClause = cb.and(datePredicate, quickPickPredicate, owningAgencyPredicate);
        query.where(whereClause);
        query.orderBy(cb.asc(root.get("displayName")));

		return currentSession().createQuery(query).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<Site> listSitesByTitle(final String aTitle) {		
		Object o = getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(final Session session) {
				Query query = session.createQuery("from Site s where lower(s.title) = :siteTitle");
				query.setParameter("siteTitle", aTitle, String.class);
				
				return query.list();
			}
		});
		
		return (List<Site>) o;
	}
	
	/**
	 * Find permissions by Site
	 * @param anAgencyOid The OID of the agency to restrict the search to.
	 * @param aSiteTitle The title of the site.
	 * @param aPageNumber The page number to return.
	 * @return A List of Permissions.
	 */
	public Pagination findPermissionsBySiteTitle(final Long anAgencyOid, final String aSiteTitle, final int aPageNumber) {
		return (Pagination) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session) {

                        CriteriaBuilder cb = session.getCriteriaBuilder();
                        CriteriaQuery<Permission> query = cb.createQuery(Permission.class);
                        CriteriaQuery<Long> cntQuery = cb.createQuery(Long.class);
                        Root<Permission> root = query.from(Permission.class);
                        Root<Permission> cntRoot = cntQuery.from(Permission.class);
                        query.select(root);
                        cntQuery.select(cb.count(cntRoot));

                        Predicate datePredicate = cb.or(cb.isNull(root.get("endDate")),
                                cb.greaterThanOrEqualTo(root.get("endDate"), new Date()));
                        Predicate cntDatePredicate = cb.or(cb.isNull(cntRoot.get("endDate")),
                                cb.greaterThanOrEqualTo(cntRoot.get("endDate"), new Date()));

                        Predicate owningAgencyPredicate = cb.equal(root.get("owningAgency").get("oid"), anAgencyOid);
                        Predicate cntOwningAgencyPredicate = cb.equal(cntRoot.get("owningAgency").get("oid"), anAgencyOid);

                        Join<Permission, Site> siteJoin = root.join("site");
                        Predicate sitePredicate = cb.and(cb.like(siteJoin.get("title"), aSiteTitle + "%"),
                                cb.equal(siteJoin.get("active"), true));
                        Join<Permission, Site> cntSiteJoin = root.join("site");
                        Predicate cntSitePredicate = cb.and(cb.like(cntSiteJoin.get("title"), aSiteTitle + "%"),
                                cb.equal(cntSiteJoin.get("active"), true));

                        Predicate whereClause = cb.and(datePredicate, owningAgencyPredicate, sitePredicate);
                        Predicate cntWhereClause = cb.and(cntDatePredicate, cntOwningAgencyPredicate, cntSitePredicate);

                        query.orderBy(cb.asc(siteJoin.get("title")));

						return new Pagination(session.createQuery(cntQuery), session.createQuery(query), aPageNumber, Constants.GBL_PAGE_SIZE);
					}
				}
			);			
	}

	
	public Pagination search(final SiteCriteria aCriteria, final int page, final int pageSize) {
		return (Pagination) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session) {

                        CriteriaBuilder cb = session.getCriteriaBuilder();
                        CriteriaQuery<Site> query = cb.createQuery(Site.class);
                        CriteriaQuery<Long> cntQuery = cb.createQuery(Long.class);
                        Root<Site> root = query.from(Site.class);
                        Root<Site> cntRoot = cntQuery.from(Site.class);
                        query.select(root);
                        cntQuery.select(cb.count(cntRoot));
                        query.distinct(true);
                        cntQuery.distinct(true);

					    Predicate siteTitle = cb.and();
                        Predicate cntSiteTitle = cb.and();
                        if(aCriteria != null && aCriteria.getTitle() != null && !"".equals(aCriteria.getTitle().trim())) {
                            siteTitle = cb.like(root.get("site").get("title"), aCriteria.getTitle().trim() + "%");
                            cntSiteTitle = cb.like(cntRoot.get("site").get("title"), aCriteria.getTitle().trim() + "%");
						}

                        Predicate orderNoPredicate = cb.and();
                        Predicate cntOrderNoPredicate = cb.and();
						if(aCriteria != null && aCriteria.getOrderNo() != null && !"".equals(aCriteria.getOrderNo().trim())) {
                            orderNoPredicate = cb.like(root.get("libraryOrderNo"), aCriteria.getOrderNo().trim() + "%");
                            cntOrderNoPredicate = cb.like(cntRoot.get("libraryOrderNo"), aCriteria.getOrderNo().trim() + "%");
						}

                        Predicate agentNamePredicate = cb.and();
                        Predicate cntAgentNamePredicate = cb.and();
						if(aCriteria != null && aCriteria.getAgentName() != null && !"".equals(aCriteria.getAgentName().trim())) {
                            Join<Site, AuthorisingAgent> authorisingAgentJoin = root.join("authorisingAgents");
                            agentNamePredicate = cb.like(authorisingAgentJoin.get("name"), aCriteria.getAgentName().trim() + "%");
                            Join<Site, AuthorisingAgent> cntAuthorisingAgentJoin = cntRoot.join("authorisingAgents");
                            cntAgentNamePredicate = cb.like(cntAuthorisingAgentJoin.get("name"), aCriteria.getAgentName().trim() + "%");
						}

                        Predicate activePredicate = cb.and();
                        Predicate cntActivePredicate = cb.and();
						if(aCriteria != null) {
							if (!aCriteria.isShowDisabled()) {
                                activePredicate = cb.equal(root.get("active"), true);
                                cntActivePredicate = cb.equal(cntRoot.get("active"), true);
							}
						}

						// Owning Agency criteria.
                        Predicate owningAgencyPredicate = cb.and();
                        Predicate cntOwningAgencyPredicate = cb.and();
						if(aCriteria != null && aCriteria.getAgency() != null && !"".equals(aCriteria.getAgency().trim())) {
                            owningAgencyPredicate = cb.like(root.get("owningAgency").get("name"), aCriteria.getAgency().trim() + "%");
                            cntOwningAgencyPredicate = cb.like(cntRoot.get("owningAgency").get("name"), aCriteria.getAgency().trim() + "%");
						}

                        Predicate oidPredicate = cb.and();
                        Predicate cntOidPredicate = cb.and();
						if(aCriteria != null && aCriteria.getSearchOid() != null) {
                            oidPredicate = cb.equal(root.get("oid"), aCriteria.getSearchOid());
                            cntOidPredicate = cb.equal(cntRoot.get("oid"), aCriteria.getSearchOid());
						}

						// URL Pattern's URL pattern criteria.
                        Predicate urlPatternPredicate = cb.and();
                        Predicate cntUrlPatternPredicate = cb.and();
						if(aCriteria != null && aCriteria.getUrlPattern() != null && !"".equals(aCriteria.getUrlPattern().trim())) {
                            urlPatternPredicate = cb.like(root.get("urlPatterns").get("pattern"), aCriteria.getUrlPattern().trim() + "%");
						}
						
						// Permission's File Reference criteria.
                        Predicate fileReferencePredicate = cb.and();
                        Predicate cntFileReferencePredicate = cb.and();
                        Join<Site, Permission> permissionJoin = null;
                        Join<Site, Permission> cntPermissionJoin = null;
						if(aCriteria != null && aCriteria.getPermsFileRef() != null && !"".equals(aCriteria.getPermsFileRef().trim())) {
                            permissionJoin = root.join("permissions");
                            cntPermissionJoin = cntRoot.join("permissions");
                            fileReferencePredicate = cb.like(permissionJoin.get("fileReference"), aCriteria.getPermsFileRef().trim() + "%");
                            cntFileReferencePredicate = cb.like(cntPermissionJoin.get("fileReference"), aCriteria.getPermsFileRef().trim() + "%");
						}

						// Permission's status flags criteria.
                        Predicate statesPredicate = cb.and();
                        Predicate cntStatesPredicate = cb.and();
						Set<Integer> states = null;
						if(aCriteria != null) { states = aCriteria.getStates(); }
						if(aCriteria != null && states != null && states.size() > 0) {
                            if (permissionJoin == null) {
                                permissionJoin = root.join("permissions");
                                cntPermissionJoin = cntRoot.join("permissions");
                            }

                            List<Predicate> disjunction = new ArrayList<>();
                            List<Predicate> cntDisjunction = new ArrayList<>();
							for(Integer i: states) {
                                disjunction.add(cb.equal(permissionJoin.get("status"), i));
                                cntDisjunction.add(cb.equal(cntPermissionJoin.get("status"), i));
							}
                            statesPredicate = cb.or(disjunction.toArray(new Predicate[disjunction.size()]));
                            cntStatesPredicate = cb.or(cntDisjunction.toArray(new Predicate[cntDisjunction.size()]));
						}

                        Predicate whereClause = cb.and(siteTitle, orderNoPredicate, agentNamePredicate, activePredicate,
                                owningAgencyPredicate, oidPredicate, urlPatternPredicate, fileReferencePredicate,
                                statesPredicate);
                        Predicate cntWhereClause = cb.and(cntSiteTitle, cntOrderNoPredicate, cntAgentNamePredicate, cntActivePredicate,
                                cntOwningAgencyPredicate, cntOidPredicate, cntUrlPatternPredicate, cntFileReferencePredicate,
                                cntStatesPredicate);
                        query.where(whereClause);
                        cntQuery.where(cntWhereClause);

						if( aCriteria.getSortorder() == null || 
							aCriteria.getSortorder().equals(CommandConstants.SITE_SEARCH_COMMAND_SORT_NAME_ASC)) {
                            query.orderBy(cb.asc(root.get("title")));
						} else if (aCriteria.getSortorder().equals(CommandConstants.SITE_SEARCH_COMMAND_SORT_NAME_DESC)) {
                            query.orderBy(cb.desc(root.get("title")));
						} else if (aCriteria.getSortorder().equals(CommandConstants.SITE_SEARCH_COMMAND_SORT_DATE_ASC)) {
                            query.orderBy(cb.asc(root.get("creationDate")));
						} else if (aCriteria.getSortorder().equals(CommandConstants.SITE_SEARCH_COMMAND_SORT_DATE_DESC)) {
                            query.orderBy(cb.desc(root.get("creationDate")));
						}
						
						return new Pagination(session.createQuery(cntQuery), session.createQuery(query), page, pageSize);
					}
				}
			);	
	}

	
	public Pagination searchAuthAgents(final String name, final int page) {
		return (Pagination) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session) {

                        CriteriaBuilder cb = session.getCriteriaBuilder();
                        CriteriaQuery<AuthorisingAgent> query = cb.createQuery(AuthorisingAgent.class);
                        CriteriaQuery<Long> cntQuery = cb.createQuery(Long.class);
                        Root<AuthorisingAgent> root = query.from(AuthorisingAgent.class);
                        Root<AuthorisingAgent> cntRoot = cntQuery.from(AuthorisingAgent.class);
                        query.select(root);
                        cntQuery.select(cb.count(cntRoot));

						if(name != null) {
                            query.where(cb.like(root.get("name"), name + "%"));
                            cntQuery.where(cb.like(cntRoot.get("name"), name + "%"));
						}

                        query.orderBy(cb.asc(root.get("name")));

						return new Pagination(session.createQuery(cntQuery), session.createQuery(query), page, Constants.GBL_PAGE_SIZE);
					}
				}
			);	
	}
	
	
	
	public long countSites() {
		return (Long) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session) {

                        CriteriaBuilder cb = session.getCriteriaBuilder();
                        CriteriaQuery<Long> query = cb.createQuery(Long.class);
                        Root<Site> root = query.from(Site.class);
                        query.select(cb.count(root));
                        query.where(cb.equal(root.get("active"), true));

                        Long count = session.createQuery(query).uniqueResult();

		                return count;
					}
				}
			);	
	}

	@Transactional
	public Permission loadPermission(long permOid) {
		Permission perm = (Permission) currentSession().load(Permission.class, permOid);
		Hibernate.initialize(perm.getUrls());
		return perm;
	}	

	/**
	 * Get a count of the number of seeds related to a given permission.
	 * @param aPermissionOid The permission oid
	 * @return The number of seeds linked to the permission
	 */
	public long countLinkedSeeds(final Long aPermissionOid) {
		return (Long) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session) {

                        CriteriaBuilder cb = session.getCriteriaBuilder();
                        CriteriaQuery<Long> query = cb.createQuery(Long.class);
                        Root<Seed> root = query.from(Seed.class);
                        query.select(cb.count(root));
                        query.where(cb.equal(root.get("permissions").get("oid"), aPermissionOid));
                        return session.createQuery(query).uniqueResult();
					}
				}
			);			
	}

	/**
	 * Check that the Authorising Agent name is unique.
	 * @param oid  The OID of the authorising agent, if available.
	 * @param name The name of the authorising agent.
	 * @return True if unique; otherwise false.
	 */
    public boolean isAuthAgencyNameUnique(final Long oid, final String name) {
		long count = (Long) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session) {
                        CriteriaBuilder cb = session.getCriteriaBuilder();
                        CriteriaQuery<Long> query = cb.createQuery(Long.class);
                        Root<AuthorisingAgent> root = query.from(AuthorisingAgent.class);
                        query.select(cb.count(root));

                        Predicate oidPredicate = cb.and();
						if(oid != null) {
                            oidPredicate = cb.notEqual(root.get("oid"), oid) ;
						}

                        Predicate namePredicate = cb.like(root.get("name"), name + "%");

                        Predicate whereClause = cb.and(oidPredicate, namePredicate);
                        query.where(whereClause);

                        return session.createQuery(query).uniqueResult();
					}
				}
			);
		
		return count == 0L;
    }	
	
	
}
