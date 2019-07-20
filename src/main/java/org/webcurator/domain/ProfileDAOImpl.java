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

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.webcurator.core.exceptions.WCTInvalidStateRuntimeException;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.domain.model.core.AbstractTarget;
import org.webcurator.domain.model.core.Target;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.domain.model.core.Profile;
import org.webcurator.domain.model.dto.ProfileDTO;

/**
 * The implementation of the ProfileDAO interface.
 * @author bbeaumont
 */
public class ProfileDAOImpl extends BaseDAOImpl implements ProfileDAO {
	/** Logger for this class */
	private Log log = LogFactory.getLog(ProfileDAOImpl.class);
	
	
	public Profile load(Long oid) {
		return (Profile) getHibernateTemplate().load(Profile.class, oid);
	}

	public void saveOrUpdate(final Profile aProfile) {
		txTemplate.execute(
				new TransactionCallback() {
					public Object doInTransaction(TransactionStatus ts) {
						try { 
							log.debug("Before Saving of Profile: " + aProfile.getName());
							currentSession().saveOrUpdate(aProfile);
							log.debug("After Saving Profile: " + aProfile.getName());
						}
						catch(Exception ex) {
							log.debug("Setting Rollback Only");
							ts.setRollbackOnly();
						}
						return null;
					}
				}
		);
	}
	

	@SuppressWarnings("unchecked")
	public List<ProfileDTO> getAllDTOs() {
		return getHibernateTemplate().execute(session ->
				session.getNamedQuery(Profile.QRY_GET_ALL_DTOS)
					.list());
	}

	@SuppressWarnings("unchecked")
	public List<ProfileDTO> getDTOs(boolean showInactive, String type) {
		if(showInactive) {
		    if (StringUtils.isEmpty(type)) {
				return getAllDTOs();
			} else {
				return getHibernateTemplate().execute(session ->
						session.getNamedQuery(Profile.QRY_GET_DTOS_BY_TYPE)
							.setParameter("harvesterType", type)
							.list());
			}
		}
		else {
			if (StringUtils.isEmpty(type)) {
				return getHibernateTemplate().execute(session ->
						session.getNamedQuery(Profile.QRY_GET_ACTIVE_DTOS)
							.list());
			} else {
				return getHibernateTemplate().execute(session ->
						session.getNamedQuery(Profile.QRY_GET_ACTIVE_DTOS_BY_TYPE)
							.setParameter("harvesterType", type)
							.list());
			}
		}
	}


	@SuppressWarnings("unchecked")
	public List<ProfileDTO> getAgencyDTOs(Agency agency, boolean showInactive, String type) {
		if(showInactive) {
		    if (StringUtils.isEmpty(type)) {
				return getHibernateTemplate().execute(session ->
						session.getNamedQuery(Profile.QRY_GET_AGENCY_DTOS)
							.setParameter("agencyOid", agency.getOid())
							.list());
			} else {
				return getHibernateTemplate().execute(session ->
						session.getNamedQuery(Profile.QRY_GET_AGENCY_DTOS_BY_TYPE)
								.setParameter("agencyOid", agency.getOid())
								.setParameter("harvesterType", type)
								.list());
			}
		}
		else {
		    if (StringUtils.isEmpty(type)) {
				return getHibernateTemplate().execute(session ->
						session.getNamedQuery(Profile.QRY_GET_ACTIVE_AGENCY_DTOS)
								.setParameter("agencyOid", agency.getOid())
								.list());
			} else {
				return getHibernateTemplate().execute(session ->
						session.getNamedQuery(Profile.QRY_GET_ACTIVE_AGENCY_DTOS_BY_TYPE)
								.setParameter("agencyOid", agency.getOid())
								.setParameter("harvesterType", type)
								.list());
			}
		}
	}		
	
	@SuppressWarnings("unchecked")
	public ProfileDTO getDTO(final Long aOid) {
		List dtos = getHibernateTemplate().execute(session ->
				session.getNamedQuery(Profile.QRY_GET_DTO)
						.setParameter("oid", aOid)
						.list());
		return (ProfileDTO) dtos.iterator().next();
	}	
	
	@SuppressWarnings("unchecked")
	public ProfileDTO getLockedDTO(final Long aOrigOid, final Integer aVersion) {
		ProfileDTO theDTO = null;
		List dtos = getHibernateTemplate().execute(session ->
				session.getNamedQuery(Profile.QRY_GET_LOCKED_DTO)
						.setParameter("origOid", aOrigOid)
						.setParameter("version", aVersion)
						.list());
		if(dtos.iterator().hasNext())
		{
			theDTO = (ProfileDTO) dtos.iterator().next();
		}
		return theDTO;
	}	

	/* (non-Javadoc)
	 * @see org.webcurator.domain.ProfileDAO#getDefaultProfile(org.webcurator.domain.model.auth.Agency)
	 */
	public Profile getDefaultProfile(Agency anAgency) {
		Criteria query = currentSession().createCriteria(Profile.class);
		query.createCriteria("owningAgency").add(Restrictions.eq("oid", anAgency.getOid()));
		query.add(Restrictions.eq("defaultProfile", true));
		query.add(Restrictions.eq("status", Profile.STATUS_ACTIVE));
		
		return (Profile) query.uniqueResult();
	}

	/**
	 * Get available profiles.
	 */
	@SuppressWarnings("unchecked")
	public List<ProfileDTO> getAvailableProfiles(final Agency anAgency, final int level, final Long currentProfileOid) {
		return (List<ProfileDTO>) getHibernateTemplate().execute(new HibernateCallback() {

			public Object doInHibernate(Session aSession) throws HibernateException {
				Query query = aSession.getNamedQuery(Profile.QRY_GET_AVAIL_DTOS);
				query.setParameter("agencyOid", anAgency.getOid());
				query.setParameter("requiredLevel", level);
				query.setParameter("default", true);
				query.setParameter("currentProfileOid", currentProfileOid);
				return query.list();
			} 			
		});		
	}

	/**
	 * Counts the number of Targets, and Target Groups
	 * @param aProfile The profile to count.
	 * @return The number of targets or groups using that profile.
	 */
	public long countProfileUsage(final Profile aProfile) {
		return (Long) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session) {						
						long targetCount = (Long) session.createCriteria(AbstractTarget.class)
										.setProjection(Projections.rowCount())
										.createCriteria("profile")
										.add(Restrictions.eq("oid", aProfile.getOid()))
										.uniqueResult();
						
						targetCount += (Integer) session.createCriteria(TargetInstance.class)
						.setProjection(Projections.rowCount())
						.createCriteria("lockedProfile")
						.add(Restrictions.eq("origOid", aProfile.getOrigOid()))
						.add(Restrictions.eq("version", aProfile.getVersion()))
						.uniqueResult();
		
						return targetCount;
					}
				}
			);	
		
	}
	
	/**
	 * Counts the number of Active Targets 
	 * that are currently using this profile.
	 * @param aProfile The profile to count.
	 * @return The number of active targets using that profile.
	 */
	public long countProfileActiveTargets(final Profile aProfile) {
		return (Long) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session) {						
						long targetCount = (Long) session.createCriteria(AbstractTarget.class)
										.setProjection(Projections.rowCount())
										.add(Restrictions.eq("objectType", AbstractTarget.TYPE_TARGET))
										.add(Restrictions.eq("state", Target.STATE_APPROVED))
										.createCriteria("profile").add(Restrictions.eq("oid", aProfile.getOid()))
										.uniqueResult();
						
						return targetCount;
					}
				}
			);	
		
	}
	
	/**
	 * Set the profile as the default for this agency.
	 * @param aProfile The profile to set as default.
	 */
	public void setProfileAsDefault(final Profile aProfile) {
		txTemplate.execute(
				new TransactionCallback() {
					public Object doInTransaction(TransactionStatus ts) {
						Query q = currentSession().createQuery("select p.status from Profile p where p.oid = :oid");
						q.setParameter("oid", aProfile.getOid());
						
						Integer status = (Integer) q.uniqueResult();
						if (status.intValue() == Profile.STATUS_INACTIVE) {
							throw new WCTInvalidStateRuntimeException("Profile " + aProfile.getOid() + " is inactive and cannot be set to be the default profile.");
						}
						if (status.intValue() == Profile.STATUS_LOCKED) {
							throw new WCTInvalidStateRuntimeException("Profile " + aProfile.getOid() + " is locked and cannot be set to be the default profile.");
						}
						
						try {
							currentSession().createQuery("UPDATE Profile p SET p.defaultProfile = :def, p.version = p.version + 1 WHERE p.owningAgency.oid = :agencyOid AND p.oid <> :newDefault")
								.setParameter("def", false)
								.setParameter("agencyOid", aProfile.getOwningAgency().getOid())
								.setParameter("newDefault", aProfile.getOid())
								.executeUpdate();

							currentSession().createQuery("UPDATE Profile p SET p.defaultProfile = :def, p.version = p.version + 1 WHERE p.owningAgency.oid = :agencyOid AND p.oid = :newDefault")
								.setParameter("def", true)
								.setParameter("agencyOid", aProfile.getOwningAgency().getOid())
								.setParameter("newDefault", aProfile.getOid())
								.executeUpdate();
							
						}
						catch(Exception ex) {
							log.debug("Setting Rollback Only");
							ts.setRollbackOnly();
							throw new WCTRuntimeException("Failed to set default profile" ,ex);
						}
						return null;
					}
				}
		);		
	}	
	
	
}
