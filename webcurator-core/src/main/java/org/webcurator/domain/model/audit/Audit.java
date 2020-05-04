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
package org.webcurator.domain.model.audit;

import org.hibernate.annotations.GenericGenerator;

import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import javax.persistence.*;
import java.util.Date;

/**
 * Audit object holds a single Audit action 
 * and can persist this to the database
 * @author bprice
 */
// lazy="false"
@Entity
@Table(name = "WCTAUDIT")
@NamedQuery(name = "org.webcurator.domain.model.audit.Audit.getAllByPeriodByAgencyByUser",
        query = "SELECT au FROM Audit au, org.webcurator.domain.model.auth.User u, org.webcurator.domain.model.auth.Agency ag WHERE au.userOid=u.oid AND u.agency.oid=ag.oid AND au.dateTime>=?1 AND au.dateTime<=?2 AND ( ?3='All agencies' OR ( ?4!='All agencies' AND ?5 = u.agency.name) ) AND ( ?6='All users' OR ( ?7 !='All users' AND au.userOid=u.oid AND ?8 = u.username) ) ORDER BY au.dateTime")
public class Audit {

	/** Query to retrieve Audit messages for a given user. */
	public static final String QRY_GET_ALL_BY_PERIOD_BY_AGENCY_BY_USER = "org.webcurator.domain.model.audit.Audit.getAllByPeriodByAgencyByUser";

	/** The database OID of the audit message */
    @Id
    @NotNull
    @Column(name="AUD_OID")
    // Note: From the Hibernate 4.2 documentation:
    // The Hibernate team has always felt such a construct as fundamentally wrong.
    // Try hard to fix your data model before using this feature.
    @TableGenerator(name = "SharedTableIdGenerator",
            table = "ID_GENERATOR",
            pkColumnName = "IG_TYPE",
            valueColumnName = "IG_VALUE",
            pkColumnValue = "Audit",
            allocationSize = 1) // 50 is the default
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SharedTableIdGenerator")
    private Long oid;
    /** The date/time at which the event took place */
    @NotNull
    @Column(name = "AUD_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTime;
    /** The OID of the user that performed this action */
    @Column(name = "AUD_USER_OID")
    private Long userOid;
    /** The OID of the agency that performed this action */
    @Column(name = "AUD_AGENCY_OID")
    private Long agencyOid;
    /** The username of the user that performed this action */
    @Size(max=80)
    @Column(name = "AUD_USERNAME")
    private String userName;
    /** The first name of the user that performed this action */
    @Size(max=50)
    @Column(name = "AUD_FIRSTNAME")
    private String firstname;
    /** The last name ofthe user that performed this action */
    @Size(max=50)
    @Column(name = "AUD_LASTNAME")
    private String lastname;
    /** The action that was performed */
    @Size(max=40)
    @NotNull
    @Column(name = "AUD_ACTION")
    private String action;
    /** The type of object this event acted on */
    @Size(max=255)
    @NotNull
    @Column(name = "AUD_SUBJECT_TYPE")
    private String subjectType;
    /** The OID of the object that was affected */
    @Column(name = "AUD_SUBJECT_OID")
    private Long subjectOid;
    /** The message string to go with the audit log */
    @Size(max=2000)
    @NotNull
    @Column(name = "AUD_MESSAGE")
    private String message;
    
    /**
     * gets the Audit Action 
     * @return the Audit Action
     */
    public String getAction() {
        return action;
    }
    
    /**
     * Sets the audit action.
     * @param action The audit action.
     */
    public void setAction(String action) {
        this.action = action;
    }
    
    /**
     * gets the Date and Time of the Audit entry
     * @return the DateTime
     */
    public Date getDateTime() {
        return dateTime;
    }
    
    /**
     * Sets the date/time of the audit event.
     * @param dateTime The date/time of the Audit event.
     */
    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }
    
    /**
     * gets the Users firstname
     * @return the users firstname
     */
    public String getFirstname() {
        return firstname;
    }
    
    /**
     * Sets the first name attribute on the audit message.
     * @param firstname The user's first name.
     */
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    
    /**
     * gets the Users lastname
     * @return the users lastname
     */
    public String getLastname() {
        return lastname;
    }
    
    /**
     * Sets the last name of the user on the audit message.
     * @param lastname The User's last name.
     */
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    
    /**
     * gets the Audit messgae
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Sets the audit message 
     * @param message The message to log.
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * gets the affected subjectType type, this is most likely a class
     * @return the subjectType type
     */
    public String getSubjectType() {
        return subjectType;
    }
    
    /**
     * Set the affected subject type. This is usually the name of the class
     * of the object that was acted upon.
     * @param subjectType The affected subject type.
     */
    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }
    
    /**
     * gets the Username of the user who issued the action
     * @return the Username
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * Set the username on the audit record.
     * @param userName The username to associate with the record.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    /**
     * gets the User oid of the user who issued this action
     * @return the User Oid
     */
    public Long getUserOid() {
        return userOid;
    }
    
    /**
     * Set the OID of the user that performed the action.
     * @param userOid The OID of the user that issued this action.
     */
    public void setUserOid(Long userOid) {
        this.userOid = userOid;
    }
    
    /** 
     * gets the affected Subject Oid, this can be used in conjunction with
     * the subject type to reconstruct the affected object at a later stage
     * @return the subject oid
     */
    public Long getSubjectOid() {
        return subjectOid;
    }
    
    /**
     * Get the OID of the object that was affected.
     * @param subjectOid The OID of the object affected.
     */
    public void setSubjectOid(Long subjectOid) {
        this.subjectOid = subjectOid;
    }
    
    /**
     * gets the Audit message oid, this is its primary key
     * @return the Audit oid
     */
    public Long getOid() {
        return oid;
    }
    
    /** 
     * Set the dataase OID of this audit message.
     * @param oid The database OID of this message.
     */
    public void setOid(Long oid) {
        this.oid = oid;
    }
    
    /**
     * gets the Agency oid of the user who issued this action
     * @return the Agency Oid
     */
    public Long getAgencyOid() {
        return agencyOid;
    }
    
    /**
     * Stores the agency OID of the user that issued this action.
     * @param agencyOid The OID of the user's agency.
     */
    public void setAgencyOid(Long agencyOid) {
        this.agencyOid = agencyOid;
    }
    
    
}
