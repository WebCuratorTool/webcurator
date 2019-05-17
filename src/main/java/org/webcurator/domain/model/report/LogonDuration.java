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
package org.webcurator.domain.model.report;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * Logon duration of Users
 * @author MDubos
 */
// lazy="false"
@Entity
@Table(name = "WCT_LOGON_DURATION")
@NamedQueries({
		@NamedQuery(name = "org.webcurator.domain.model.report.LogonDuration.getDurationBySession",
				query = "SELECT ld FROM LogonDuration ld WHERE ld.sessionId = ? "),
		@NamedQuery(name = "org.webcurator.domain.model.report.LogonDuration.getLoggedUsersByPeriodByAgency",
				query = "SELECT new org.webcurator.core.report.dto.LogonUserDTO(u.oid, u.username, u.firstname, u.lastname, u.active, u.deactivateDate, ld.logonTime, ld.duration) FROM org.webcurator.domain.model.auth.User u, org.webcurator.domain.model.report.LogonDuration ld, org.webcurator.domain.model.auth.Agency ag WHERE u.oid = ld.userOid AND u.agency.oid = ag.oid AND ld.logonTime >= ? AND ld.logonTime < ? AND ( (ld.logoutTime is null AND ? < ?) OR (ld.logoutTime is not null AND ld.logoutTime <= ?) ) AND ( ?='All agencies' OR ( ?!='All agencies' AND ? = u.agency.name) )"),
		@NamedQuery(name = "org.webcurator.domain.model.report.LogonDuration.UnproperLoggedoutSessionsForCurrentUser",
				query = "SELECT ld FROM LogonDuration ld, org.webcurator.domain.model.auth.User u WHERE u.oid = ld.userOid AND u.oid = ? AND ld.sessionId != ? AND ld.duration is null")
})
public class LogonDuration {
	
	public static final String QRY_LOGON_DURATION_BY_SESSION = "org.webcurator.domain.model.report.LogonDuration.getDurationBySession";
	public static final String QRY_LOGGED_USERS_BY_PERIOD_BY_AGENCY = "org.webcurator.domain.model.report.LogonDuration.getLoggedUsersByPeriodByAgency";
	public static final String QRY_UNPROPER_LOGGED_OUT_SESSIONS_FOR_CURRENT_USER = "org.webcurator.domain.model.report.LogonDuration.UnproperLoggedoutSessionsForCurrentUser";

	@Id
	@Column(name="LOGDUR_OID", nullable =  false)
	// Note: From the Hibernate 4.2 documentation:
	// The Hibernate team has always felt such a construct as fundamentally wrong.
	// Try hard to fix your data model before using this feature.
	@TableGenerator(name = "SharedTableIdGenerator",
			table = "ID_GENERATOR",
			pkColumnName = "IG_TYPE",
			valueColumnName = "IG_VALUE",
			pkColumnValue = "LogonDuration",
			allocationSize = 1) // 50 is the default
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "SharedTableIdGenerator")
	private Long oid;
	@Column(name = "LOGDUR_USER_OID", nullable = false)
    private Long userOid;
	@Column(name = "LOGDUR_USERNAME", length = 80, nullable = true)
	private String userName;
	@Column(name = "LOGDUR_USER_REALNAME", length = 100, nullable = true)
    private String userRealName;
	@Column(name = "LOGDUR_SESSION_ID", length = 32, nullable = false)
    private String sessionId;
	@Column(name = "LOGDUR_LOGON_TIME", columnDefinition = "TIMESTAMP(9)", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date logonTime;
	@Column(name = "LOGDUR_LOGOUT_TIME", columnDefinition = "TIMESTAMP(9)", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
    private Date logoutTime;
    private Long duration;
    
    
    /**
     * Get the logon duration of the user. Accuracy cannot be expected
     * if session is not closed manually by the user
     * @return Logon duration (in seconds)
     */
    @Column(name = "LOGDUR_DURATION", nullable = true)
	public Long getDuration() {
		return duration;
	}
	public void setDuration(Long duration) {
		this.duration = duration;
	}
	/**
	 * Compute and set the duration, based on <code>logout time</code><br>
	 * <br>
	 * The parameter <code>logoutTime</code> has to be set <b>first</b>. 
	 * Otherwise, if it is found <code>null</code> it will be set to the 
	 * current time in order to compute a duration.
	 * @param setLogoutToNowIfNull <code>true</code> to set <code>logoutTime</code>
	 * to current time if it is null
	 */
	public void computeAndSetDuration(boolean setLogoutToNowIfNull){
		// null logoutTime e.g tomcat has been shutdown by a bad guy
		if(setLogoutToNowIfNull && logoutTime == null){
			Date now = new Date();
			setLogoutTime(now);
		}
		setDuration(computeDuration(getLogonTime(), getLogoutTime()));
	}
	
	/**
	 * Compute a duration
	 * @param logonDate Start time
	 * @param logoutDate End time
	 * @return Duration in seconds
	 */
	public static long computeDuration(Date logonDate, Date logoutDate){
		return logoutDate.getTime() / 1000 - logonDate.getTime() / 1000;
	}
	
	
	/**
	 * Get time of logon 
	 * @return Date of logon
	 */
	public Date getLogonTime() {
		return logonTime;
	}
	public void setLogonTime(Date logonTime) {
		this.logonTime = logonTime;
	}
	
	
	/**
	 * Get time of logout (manual or timeout logout)
	 * @return Date of logout
	 */
	public Date getLogoutTime() {
			return logoutTime;
	}
	public void setLogoutTime(Date logoutTime) {
		this.logoutTime = logoutTime;
	}
	
	
    /**
     * Get the Username of the user who log on / log out
     * @return the Username
     */
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
    /**
     * Get the User oid of the user who issued this action
     * @return the User Oid
     */
	public Long getUserOid() {
		return userOid;
	}
	public void setUserOid(Long userOid) {
		this.userOid = userOid;
	}
	
	
    /**
     * Get the Users first name plus last name
     * @return Users first name plus last name
     */
	public String getUserRealName() {
		return userRealName;
	}
	public void setUserRealName(String userRealName) {
		this.userRealName = userRealName;
	}
	
	
    /**
     * Unique session identifier
     * @return ID of the session
     */
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	
    /**
     * Get the LogonDuration oid, this is its primary key
     * @return The LogonDuration OID
     */
	public Long getOid() {
		return oid;
	}
	public void setOid(Long oid) {
		this.oid = oid;
	}
	
	/**
	 * Comnvenient method for displaying all parameters
	 */
	public String toString(){
		return userOid.toString() + "|" +
			userName + "|" +
			userRealName + "|" +
			sessionId + "|" +
			logonTime.toString() + "|" +
			(logoutTime == null ? "null" : logoutTime.toString()) + "|" +
			(duration == null ? "null" : duration.toString());
	}
    
	/**
	 * Clone the object, oid included!!
	 */
	public LogonDuration clone(){
		LogonDuration ld = new LogonDuration();
		ld.setOid(			getOid());
		ld.setUserOid(		getUserOid());
		ld.setUserName( 	getUserName());
		ld.setUserRealName(	getUserRealName());
		ld.setSessionId(	getSessionId());
		ld.setLogonTime(	getLogonTime());
		ld.setLogoutTime(	getLogoutTime());
		ld.setDuration(		getDuration());
		return ld;
	}
	
}
