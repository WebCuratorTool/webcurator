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
package org.webcurator.core.report.impl;

import jakarta.persistence.criteria.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.webcurator.core.report.OperationalReport;
import org.webcurator.core.report.ReportGenerator;
import org.webcurator.core.report.ResultSet;
import org.webcurator.core.report.parameter.DateParameter;
import org.webcurator.core.report.parameter.Parameter;
import org.webcurator.core.report.parameter.StringParameter;
import org.webcurator.domain.model.auth.User;
import org.webcurator.domain.model.core.TargetInstance;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Report for Crawler Activity.
 * @author beaumontb
 *
 */
public class CrawlerActivityReport extends HibernateDaoSupport implements ReportGenerator {

	private Log log = LogFactory.getLog(CrawlerActivityReport.class);

	/**
	 * Generate report's data
	 * @param operationalReport An OperationReport
	 * @return Report's data as a <code>List</code> of <code>ResultSet</code>
	 */
	public List<ResultSet> generateData(OperationalReport operationalReport) {
		
		List<Parameter> parameters = operationalReport.getParameters();
		DateParameter startDate =	(DateParameter) parameters.get(0);
		DateParameter endDate   =	(DateParameter) parameters.get(1);
		StringParameter agency  = 	(StringParameter) parameters.get(2);
		StringParameter user    =	(StringParameter) parameters.get(3);
		
		SimpleDateFormat sdf = startDate.getDateFormat();
		log.debug(" startDate=" + sdf.format(startDate.getValue()));
		log.debug(" endDate=" + sdf.format(endDate.getValue()));
		log.debug(" agency=" + (agency == null ? "null" : agency.getValue()) );
		log.debug(" user=" + (user == null ? "null" : user.getValue()));
		
		return runReport(startDate.getValue(), endDate.getValue(), agency.getValue(), user.getValue());
	}
	
	/**
	 * Generate the Crawler Activity Report
	 * 
	 * @param startDate Start date<br>Is inclusive and value is mandatory
	 * @param endDate End date.<br> Is inclusive and value is mandatory
	 * @param agencyName Name of Agency<br><code>null</code> value is accepted
	 * @param userName Username<br><code>null</code> value is accepted
	 * @return A <code>List</code> of <code>ResultSet</code>
	 */
	@SuppressWarnings("unchecked")
	protected List<ResultSet> runReport(final Date startDate, final Date endDate, final String agencyName, final String userName){

		List results = (List) getHibernateTemplate().execute(new HibernateCallback() {

			@SuppressWarnings("unchecked")
			public Object doInHibernate(Session session) throws HibernateException {
                CriteriaBuilder cb = session.getCriteriaBuilder();
                CriteriaQuery<TargetInstance> query = cb.createQuery(TargetInstance.class);
                Root<TargetInstance> root = query.from(TargetInstance.class);
                query.select(root);

                Join<TargetInstance, User> userJoin = null;
                Predicate usernamePredicate = cb.and();
				if (userName != null && !userName.equals("All users")) {
                    userJoin = root.join("owner");
                    usernamePredicate = cb.equal(userJoin.get("username"), userName);
				}
                Predicate agencyNamePredicate = cb.and();
                if (agencyName != null && !agencyName.equals("All agencies")) {
                    if (userJoin == null) {
                        userJoin = root.join("owner");
                    }
                    agencyNamePredicate = cb.equal(userJoin.get("agency").get("name"), agencyName);
                }

                Predicate startDatePredicate = cb.greaterThanOrEqualTo(root.get("actualStartTime"), startDate);
                Predicate endDatePredicate = cb.lessThan(root.get("actualStartTime"), endDate);
                Predicate statePredicate = cb.not(root.get("state").in(TargetInstance.STATE_QUEUED, TargetInstance.STATE_SCHEDULED));

                Predicate whereClause = cb.and(usernamePredicate, agencyNamePredicate, startDatePredicate,
                        endDatePredicate, statePredicate);
                query.where(whereClause);
                query.orderBy(cb.asc(root.get("actualStartTime")));

				List<TargetInstance> results = session.createQuery(query).list();
				
				List realResults = new ArrayList<CrawlerActivityReportResultSet>(results.size());
				
				for(TargetInstance ti: results ) {
					Date endDate = null;
					
					// Target has finished, calculate the end date; otherwise leave it as null.
					if(!"Running".equals(ti.getState()) &&  !"Stopping".equals(ti.getState())
						&& ti.getActualStartTime() != null && ti.getStatus() != null) {
						endDate = new Date(ti.getActualStartTime().getTime() + ti.getStatus().getElapsedTime());
					}

					realResults.add(
							new CrawlerActivityReportResultSet(
									ti.getOid(), ti.getTarget().getName(), 
									ti.getState(), ti.getActualStartTime(), 
									endDate, ti.getStatus().getElapsedTime(), 
									ti.getStatus().getDataDownloaded(), 
									ti.getHarvestServer()));
				}
				
				return realResults;
			}
			
		});
		
    	return results;
    }	

}
