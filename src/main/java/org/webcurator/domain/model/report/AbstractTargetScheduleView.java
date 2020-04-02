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

import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import javax.persistence.*;
import java.util.Date;


/**
 * The AbstractTargetScheduleView class is used to provide
 * fast lookups for the TargetGroupSchedulesReport report class.
 * 
 * @author oakleigh_sk
*/
// lazy="true" readonly="true"
@Entity
@Table(name = "ABSTRACT_TARGET_SCHEDULE_VIEW")
@NamedQueries({
		@NamedQuery(name = "org.webcurator.domain.model.report.AbstractTargetScheduleView.getAllByUserByAgencyByType",
				query = "SELECT t FROM AbstractTargetScheduleView t WHERE (t.state = 5 OR t.state = 9) AND ( ?1='All users' OR ( ?2 !='All users' AND ?3 = t.ownerName ) ) AND ( ?4='All agencies' OR ( ?5 !='All agencies' AND ?6 = t.agencyName) ) AND ( ?7 ='All target types' OR ( ?8!='All target types' AND ?9 = t.objectTypeDesc) )"),
		@NamedQuery(name = "org.webcurator.domain.model.report.AbstractTargetScheduleView.getSummaryStatsByAgency",
				query = "SELECT t.agencyName, CASE t.scheduleType WHEN 1 THEN 'Mondays at 9:00pm' WHEN 0 THEN 'Custom' WHEN -1 THEN 'Daily' WHEN -2 THEN 'Weekly' WHEN -3 THEN 'Monthly' WHEN -4 THEN 'Bi-Monthly' WHEN -5 THEN 'Quarterly' WHEN -6 THEN 'Half-Yearly' WHEN -7 THEN 'Annually' END as scheduleDesc, COUNT(t.scheduleType) FROM AbstractTargetScheduleView t WHERE (t.state = 5 OR t.state = 9) AND ( ?1='All agencies' OR ( ?2!='All agencies' AND ?3 = t.agencyName) ) GROUP BY t.agencyName, t.scheduleType ORDER BY t.agencyName")
})
public class AbstractTargetScheduleView {
	
	/** Query identifier for listing all records */
	public static final String QRY_GET_ALL_BY_USER_BY_AGENCY_BY_TYPE = "org.webcurator.domain.model.report.AbstractTargetScheduleView.getAllByUserByAgencyByType";
	public static final String QRY_GET_SUMMARY_STATS_BY_AGENCY = "org.webcurator.domain.model.report.AbstractTargetScheduleView.getSummaryStatsByAgency";
	
	/** The composite primary key. The abstract_target oid and the schedule oid strung together with a comma */
	@Id
	@Column(name = "THEKEY")
	@GeneratedValue(strategy=GenerationType.AUTO)
	// Prior to version 5.0, using the strategy AUTO was the equivalent of using native in a mapping.
	// This used the LegacyFallbackInterpreter.
	// TODO Since Hibernate 5.0, the default interpreter is the FallbackInterpeter which will either use a SEQUENCE
	// generator or TABLE generator depending on the underlying database.
	private String theKey;

	///** The database OID of the record */
    //private Long oid;

    /** 
     * Identifies whether this is a Target or Group.
     */
    @Column(name = "AT_OBJECT_TYPE_DESC")
    private String objectTypeDesc;

    /** The target (or group) name. */
    @Size(max=255)
    @Column(name = "AT_NAME")
    private String name;

    /** The state of the target (or group) */
    @Column(name = "AT_STATE")
    private int state; 
    
    /** The target (or group) owner name. */
    @Size(max=80)
    @Column(name = "USR_USERNAME")
    private String ownerName;

    /** The target (or group) owning agency name. */
    @Size(max=80)
    @Column(name = "AGC_NAME")
    private String agencyName;

	/** The oid of the schedule record. */
	@Column(name = "S_OID")
	private Long scheduleOid;

	/** The schedule start date*/
	@NotNull
	@Column(name = "S_END", columnDefinition = "TIMESTAMP(9)")
	@Temporal(TemporalType.TIMESTAMP)
	private Date scheduleStartDate;

	/** The schedule end date*/
	@Column(name = "S_START", columnDefinition = "TIMESTAMP(9)")
	@Temporal(TemporalType.TIMESTAMP)
	private Date scheduleEndDate;

    /** Type Identifier for schedules. */
    @Column(name = "S_TYPE")
    private int scheduleType; 
	
	/** The schedule cron pattern*/
	@Size(max=255)
	@Column(name = "S_CRON")
	private String scheduleCronPattern;

	
	/**
	 * constructor for Hibernate.
	 */
	
	protected AbstractTargetScheduleView() { }

	/**
	 * Standard constructor for WCT usage.
	 * @param aUrlPattern The UrlPattern.
	 * @param aPermission The CutdownPermission.
	//public AbstractTargetScheduleView(UrlPattern aUrlPattern, CutdownPermission aPermission) {
	//	urlPattern = aUrlPattern;
	//	permission = aPermission;
	//	
	//	domain = HierarchicalPermissionMappingStrategy.calculateDomain(aUrlPattern.getPattern());
	}
	 */

    /**
     * Get the primary key of the AbstractTargetScheduleView record.
     * @return Returns the key.
     */
	public String getTheKey() {
        return theKey;
    }

    /**
     * Hibernate method to set the the key.
     * @param aKey The key to set.
     */
	public void setTheKey(String aKey) {
		theKey = aKey;
    }

	/**
	 * Returns the object type description ('Target' or 'Group').
	 * @return The object type description.
	 */
	public String getObjectTypeDesc() {
		return objectTypeDesc;
	}
	
	/**
	 * setObjectTypeDesc is required for Hibernate. It is not used
	 * elsewhere.
	 * @param type The object type.
	 */
	@SuppressWarnings("unused")
	private void setObjectTypeDesc(String aObjectTypeDesc) {
		objectTypeDesc = aObjectTypeDesc;
	}

    /**
     * Returns the name of the AbstractTarget.
     * @return the name of the AbstractTarget.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the abstract target.
     * @param aName The name to set.
     */
    public void setName(String aName) {
    	name = aName;
    }    

	/**
	 * Returns the state (STATE_APPROVED, STATE_REJECTED etc). This can be used
	 * instead of instanceof, which is useful if the object isn't fully 
	 * initialised by Hibernate.
	 * @return STATE_APPROVED, STATE_REJECTED etc
	 */
	public int getState() {
		return state;
	}
	
	/**
	 * setState is required for Hibernate. It is not used
	 * elsewhere.
	 * @param state The state.
	 */
	@SuppressWarnings("unused")
	private void setState(int aState) {
		state = aState;
	}
    
    /**
     * Returns the name of the owner of the AbstractTarget.
     * @return the name of the owner of the AbstractTarget.
     */
    public String getOwnerName() {
        return ownerName;
    }

    /**
     * Sets the name of the owner of the abstract target.
     * @param aOwnerName The owner name to set.
     */
    public void setOwnerName(String aOwnerName) {
    	ownerName = aOwnerName;
    }    

    /**
     * Returns the name of the agency of the owner of the AbstractTarget.
     * @return the name of the agency of the owner of the AbstractTarget.
     */
    public String getAgencyName() {
        return agencyName;
    }

    /**
     * Sets the name of the owner of the abstract target.
     * @param aName The name to set.
     */
    public void setAgencyName(String aName) {
    	agencyName = aName;
    }    

	/**
	 * Returns the schedule oid.
	 * @return the schedule oid.
	 */
	public Long getScheduleOid() {
		return scheduleOid;
	}
	
	/**
	 * setScheduleOid is required for Hibernate.
	 * It is not used elsewhere.
	 * @param aScheduleOid The schedule oid.
	 */
	@SuppressWarnings("unused")
	private void setScheduleOid(Long aScheduleOid) {
		scheduleOid = aScheduleOid;
	}

    
    /**
     * Returns the date at which scheduling will start.
     * @return Returns the start date.
     */
    public Date getScheduleStartDate() {
        return scheduleStartDate;
    }
    
    /**
     * Sets the date at which to start scheduling.
     * @param aStartDate The date to start scheduling.
     */
    public void setScheduleStartDate(Date aStartDate) {
        this.scheduleStartDate = aStartDate;
    }
    
    /**
     * Gets the date to end scheduling. 
	 * @return Returns the end date of the schedule.
	 */
	public Date getScheduleEndDate() {
		return scheduleEndDate;
	}
	
	/**
	 * Sets the date to end scheduling.
	 * @param endDate The end date to set.
	 */
	public void setScheduleEndDate(Date endDate) {
		this.scheduleEndDate = endDate;
	}

	/**
	 * Returns the schedule type (CUSTOM_SCHEDULE, TYPE_DAILY, etc).
	 * @return the schedule type (CUSTOM_SCHEDULE, TYPE_DAILY, etc) a positive or negative integer.
	 */
	public int getScheduleType() {
		return scheduleType;
	}
	
	/**
	 * setScheduleType is required for Hibernate.
	 * It is not used elsewhere.
	 * @param aScheduleType The schedule type.
	 */
	@SuppressWarnings("unused")
	private void setScheduleType(int aScheduleType) {
		scheduleType = aScheduleType;
	}

	/**
	 * Returns the schedules cron pattern
	 * @return Returns the schedules cron pattern.
	 */
	public String getScheduleCronPattern() {
		return scheduleCronPattern;
	}

	/**
	 * Sets the schedules cron pattern.
	 * Private as this should only be called from Hibernate.
	 * @param aScheduleCronPattern The scheduleCronPattern to set. 
	 */
	@SuppressWarnings("unused")
	private void setScheduleCronPattern(String aScheduleCronPattern) {
		scheduleCronPattern = aScheduleCronPattern;
	}
	
}
