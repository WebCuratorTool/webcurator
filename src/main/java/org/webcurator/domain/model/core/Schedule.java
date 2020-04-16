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
package org.webcurator.domain.model.core;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;

import org.quartz.CronExpression;
import org.webcurator.core.util.DateUtils;
import org.webcurator.domain.UserOwnable;
import org.webcurator.domain.model.auth.User;

import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import javax.persistence.*;

/**
 * A schedule determines how often a Target or TargetGroup will be harvested.
 * This Schedule object is dependent on the CronExpression class from Quartz
 *
 * Schedules may be custom schedules (dependent on a custom cron pattern) or 
 * may be picked from some predefined schedules. 
 * 
 * @see org.webcurator.domain.model.core.SchedulePattern
 * 
 * @author Brett Beaumont
 */
// lazy="false"
@Entity
@Table(name = "SCHEDULE")
public class Schedule extends AbstractIdentityObject implements UserOwnable {
	/** Constant for a custom schedule */
	public static final int CUSTOM_SCHEDULE = 0;
	
	public static final int TYPE_DAILY = -1;
	public static final int TYPE_WEEKLY = -2;
	public static final int TYPE_MONTHLY = -3;
	public static final int TYPE_BI_MONTHLY = -4;
	public static final int TYPE_QUARTERLY = -5;
	public static final int TYPE_HALF_YEARLY = -6;
	public static final int TYPE_ANNUALLY = -7;
	
    /** The primary key. */
	@Id
	@NotNull
	@Column(name="S_OID")
	// Note: From the Hibernate 4.2 documentation:
	// The Hibernate team has always felt such a construct as fundamentally wrong.
	// Try hard to fix your data model before using this feature.
	@TableGenerator(name = "SharedTableIdGenerator",
			table = "ID_GENERATOR",
			pkColumnName = "IG_TYPE",
			valueColumnName = "IG_VALUE",
			pkColumnValue = "General",
			allocationSize = 1) // 50 is the default
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "SharedTableIdGenerator")
	private Long oid;
    /** The start date and time of the schedule. */
    @NotNull
    @Column(name = "S_START")
    @Temporal(TemporalType.TIMESTAMP)
	private Date startDate;
    /** the end date of the schedule. */
    @Column(name = "S_END")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;
    /** The pattern for deciding how often to run the schedule. */
    @Size(max=255)
    @NotNull
    @Column(name = "S_CRON")
    private String cronPattern;
    /** the target the schedule is related to. */
    @ManyToOne(cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "S_ABSTRACT_TARGET_ID") //
    private AbstractTarget target;
    /** Set of related target instances */
    // cascade="save-update"
	@OneToMany(cascade ={CascadeType.REFRESH}) // default fetch type is LAZY,(cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinColumn(name = "TI_SCHEDULE_ID")
    private Set<TargetInstance> targetInstances;
    /** Type Identifier for quick schedules. */
    @NotNull
    @Column(name = "S_TYPE")
    private int scheduleType = CUSTOM_SCHEDULE; 
    /** The owner of the schedule */
	@ManyToOne
	@JoinColumn(name = "S_OWNER_OID")
    private User owner;
    /** The first date after the currently assigned period on which this schedule should run */
	@Column(name = "S_NEXT_SCHEDULE_TIME")
	@Temporal(TemporalType.TIMESTAMP)
    private Date nextScheduleAfterPeriod;
    /**  */
	@Column(name = "S_LAST_PROCESSED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
    private Date lastProcessedDate;
    /** The first date after the currently assigned period on which this schedule should run */
    @Transient
    private boolean savedInThisSession = false;
    
    /**
     * Protected constructor - all schedules should be constructed by 
     * the BusinessObjectFactory.
     */
    protected Schedule() {}
    
    /**
     * Gets the database OID of the schedule.
     * @return Returns the oid.
     */
    public Long getOid() {
        return oid;
    }
    
    /**
     * Sets the database oid of the schedule.
     * @param aOid The oid to set.
     */
    public void setOid(Long aOid) {
        this.oid = aOid;
    }
    
    /**
     * Gets the Cron Pattern string.
     * @return Returns the cronPattern.
     */
    public String getCronPattern() {
        return cronPattern;
    }
    
    /**
     * Gets the cron pattern string without the seconds component. This is 
     * useful for the user interface display.
     * @return The cron pattern string without the seconds component.
     */
    public String getCronPatternWithoutSeconds() {
    	int ix = cronPattern.indexOf(' ');
    	return cronPattern.substring(ix+1);
    }
    
    /**
     * Sets the cron pattern for this schedule.
     * @param aCronPattern The cronPattern to set.
     */
    public void setCronPattern(String aCronPattern) {
        this.cronPattern = aCronPattern;
    }
    
    /**
     * Returns the date at which scheduling will start.
     * @return Returns the start date.
     */
    public Date getStartDate() {
        return startDate;
    }
    
    /**
     * Sets the date at which to start scheduling.
     * @param aStartDate The date to start scheduling.
     */
    public void setStartDate(Date aStartDate) {
        this.startDate = aStartDate;
    }
    
    /**
     * Gets the date to end scheduling. 
	 * @return Returns the end date of the schedule.
	 */
	public Date getEndDate() {
		return endDate;
	}
	
	/**
	 * Sets the date to end scheduling.
	 * @param endDate The end date to set.
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	/**
	 * Get the target that this schedule belongs to.
     * @return Returns the target.
     */
    public AbstractTarget getTarget() {
        return target;
    }
    
    /**
     * Set the target that owns this schedule.
     * @param aTarget The target to set.
     */
    public void setTarget(AbstractTarget aTarget) {
        this.target = aTarget;
    }

    /**
     * Get the list of target instances associated with this schedule.
	 * @return Returns the targetInstances.
	 */
	public Set<TargetInstance> getTargetInstances() {
		return targetInstances;
	}
	
	/**
	 * Set the targetinstances associated with this schedule.
	 * @param targetInstances The targetInstances to set.
	 */
	public void setTargetInstances(Set<TargetInstance> targetInstances) {
		this.targetInstances = targetInstances;
	}
	
	/**
     * Retrieves the next execution time based on the schedule. This
     * method delegates to getNextExecutionDate(Date) assuming the 
     * current date.
     * @return The next execution time.
     */
    public Date getNextExecutionDate() {
    	return getNextExecutionDate(DateUtils.latestDate(new Date(), getStartDate()));
    }
    
    
    /**
     * Retrieves the next execution time based on the schedule and
     * the supplied date.
     * @param after The date to get the next invocation after.
     * @return The next execution time.
     */
    public Date getNextExecutionDate(Date after) {
    	try {
    		
	    	CronExpression expression = new CronExpression(this.getCronPattern());
	    	Date next = expression.getNextValidTimeAfter(DateUtils.latestDate(after, new Date()));
	    	if(next == null) { 
	    		return null; 
	    	}
	    	else if(endDate != null && next.after(endDate)) {
	    		return null;
	    	}
	    	else {
	    		return next;
	    	}
    	}
    	catch(ParseException ex) {
        	System.out.println(" Encountered ParseException for cron expression: " + this.getCronPattern() + " in schedule: " + this.getOid());
    		return null;
    	}
    }
    

    
    /**
     * Check for equality. Two objects are equal if, and only if, 
     * their cronPatterns are the same, or if both objects have a 
     * null cronPattern.
     * @param o The other object.
     */
    public boolean equals(Object o) { 
    	return o instanceof Schedule && 
    	       ( cronPattern == null && ((Schedule)o).cronPattern == null ||
    	         ((Schedule)o).cronPattern.equals(cronPattern)
    	       );
    }
    

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
    	return cronPattern == null ? 0 : cronPattern.hashCode();
    }
    
	/**
	 * Gets the type of the schedule (custom or predefined).
	 * @return Returns the scheduleType.
     * @see org.webcurator.domain.model.core.SchedulePattern
	 */
	public int getScheduleType() {
		return scheduleType;
	}
	
	/**
	 * Sets the type of the schedule (custom or predefined).
	 * @param scheduleType The scheduleType to set.
     * @see org.webcurator.domain.model.core.SchedulePattern
	 */
	public void setScheduleType(int scheduleType) {
		this.scheduleType = scheduleType;
	}
	
	/**
	 * Sets the owner of a schedule.
	 * @param anOwner The owner of the schedule.
	 */
	public void setOwningUser(User anOwner) {
		this.owner = anOwner;
	}
	
	/**
	 * Gets the user that owns the schedule.
	 * @return Returns the owner.
	 */
	public User getOwningUser() {
		return owner;
	}

	/**
	 * Gets the next scheduled time after the "number of days to schedule" 
	 * setting. This is a management piece of information used to identify 
	 * when the scheduling next needs to consider this schedule. It is not
	 * for general use. 
	 * 
	 * @return Returns the nextScheduleAfterPeriod.
	 */
	public Date getNextScheduleAfterPeriod() {
		return nextScheduleAfterPeriod;
	}

	/**
	 * Sets the next scheduled time after the "number of days to schedule" 
	 * setting. This is a management piece of information used to identify 
	 * when the scheduling next needs to consider this schedule. It is not
	 * for general use. 
	 * @param nextScheduleAfterPeriod The nextScheduleAfterPeriod to set.
	 */
	public void setNextScheduleAfterPeriod(Date nextScheduleAfterPeriod) {
		this.nextScheduleAfterPeriod = nextScheduleAfterPeriod;
	}

	/**
	 * @return Returns the lastProcessedDate.
	 */
	public Date getLastProcessedDate() {
		return lastProcessedDate;
	}

	public void setLastProcessedDate(Date lastProcessedDate) {
		this.lastProcessedDate = lastProcessedDate;
	}

	public boolean isSavedInThisSession() {
		return savedInThisSession;
	}

	/**
	 * Sets whether the schedule has been processed for generating
	 * Target Instances. Used when a Target Instance is saved via the 
	 * Annotations screen, to prevent a duplicate scheduling bug. 
	 */
	public void setSavedInThisSession(boolean savedInThisSession) {
		this.savedInThisSession = savedInThisSession;
	}
}
