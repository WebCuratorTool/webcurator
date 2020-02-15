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

import org.hibernate.annotations.Formula;
import org.webcurator.core.notification.UserInTrayResource;
import org.webcurator.core.util.Utils;
import org.webcurator.domain.UserOwnable;
import org.webcurator.domain.model.auth.User;

import javax.persistence.*;
import java.util.*;

/**
 * Base Target object to capture the common behaviour between groups and 
 * targets.
 * 
 * @author bbeaumont
 */
// lazy="true"
@SuppressWarnings("all")
@Entity
@Table(name = "ABSTRACT_TARGET")
@NamedQueries({
		@NamedQuery(name = "org.webcurator.domain.model.core.AbstractTarget.getAllDTOsByName",
				query = "SELECT new org.webcurator.domain.model.dto.AbstractTargetDTO(t.oid, t.name, t.owner.oid, t.owner.username, t.owner.agency.name, t.state, t.profile.oid, t.objectType) FROM AbstractTarget t where lower(t.name) like lower(?1) ORDER BY UPPER(t.name), t.objectType"),
		@NamedQuery(name = "org.webcurator.domain.model.core.AbstractTarget.cntAllDTOsByName",
				query = "SELECT count(*) FROM AbstractTarget t where lower(t.name) like lower(?1)"),
		@NamedQuery(name = "org.webcurator.domain.model.core.AbstractTarget.getGroupDTOsByName",
				query = "SELECT new org.webcurator.domain.model.dto.AbstractTargetDTO(t.oid, t.name, t.owner.oid, t.owner.username, t.owner.agency.name, t.state, t.profile.oid, t.objectType) FROM AbstractTarget t where t.objectType = 0 and lower(t.name) like lower(?1) ORDER BY UPPER(t.name), t.objectType"),
		@NamedQuery(name = "org.webcurator.domain.model.core.AbstractTarget.cntGroupDTOsByName",
				query = "SELECT count(*) FROM AbstractTarget t where t.objectType = 0 and lower(t.name) like lower(?1)"),
		@NamedQuery(name = "org.webcurator.domain.model.core.AbstractTarget.getDTOByOid",
				query = "SELECT new org.webcurator.domain.model.dto.AbstractTargetDTO(t.oid, t.name, t.owner.oid, t.owner.username, t.owner.agency.name, t.state, t.profile.oid, t.objectType) FROM AbstractTarget t where t.oid=:oid"),
		@NamedQuery(name = "org.webcurator.domain.model.core.AbstractTarget.getTargetDTOsByProfileOid",
				query = "SELECT new org.webcurator.domain.model.dto.AbstractTargetDTO(t.oid, t.name, t.owner.oid, t.owner.username, t.owner.agency.name, t.state, t.creationDate, t.profile.oid, t.objectType) FROM AbstractTarget t where t.objectType = 1 and t.profile.oid=:profileoid ORDER BY UPPER(t.name)"),
		@NamedQuery(name = "org.webcurator.domain.model.core.AbstractTarget.cntTargetDTOsByProfileOid",
				query = "SELECT count(*) FROM AbstractTarget t where t.objectType = 1 and t.profile.oid=:profileoid")
})
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractTarget extends AbstractIdentityObject implements UserOwnable, Annotatable, Overrideable, UserInTrayResource {
	/** Query identifier for retrieving AbstractTargetDTOs by name */
	public static final String QUERY_DTO_BY_NAME = "org.webcurator.domain.model.core.AbstractTarget.getAllDTOsByName";
	public static final String QUERY_CNT_DTO_BY_NAME = "org.webcurator.domain.model.core.AbstractTarget.cntAllDTOsByName";
	
	/** Query identifier for retrieving Group DTOs by name */
	public static final String QUERY_GROUP_DTOS_BY_NAME = "org.webcurator.domain.model.core.AbstractTarget.getGroupDTOsByName";
	public static final String QUERY_CNT_GROUP_DTOS_BY_NAME = "org.webcurator.domain.model.core.AbstractTarget.cntGroupDTOsByName";	
	
	/** Query identifier for retrieving an AbstractTargetDTO by OID. */
	public static final String QUERY_DTO_BY_OID = "org.webcurator.domain.model.core.AbstractTarget.getDTOByOid";
	
	/** Query identifier for retrieving Target DTOs by agency and profileOid */
	public static final String QUERY_TARGET_DTOS_BY_PROFILE = "org.webcurator.domain.model.core.AbstractTarget.getTargetDTOsByProfileOid";
	public static final String QUERY_CNT_TARGET_DTOS_BY_PROFILE = "org.webcurator.domain.model.core.AbstractTarget.cntTargetDTOsByProfileOid";

	/** The maximum length of the target name */
	public static final int CNST_MAX_NAME_LENGTH = 255;
	/** The maximum length of the reference number string */
	public static final int MAX_REFERENCE_LENGTH = 50;
	
	/** The maximum length of the profile note. */
	public static final int MAX_PROFILE_NOTE_LENGTH = 255;

	/** Maximum length for the Display Note */
	public static int MAX_DISPLAY_NOTE_LENGTH = 4000;
	
	/** Maximum length for the Display Change Reason */
	public static int MAX_DISPLAY_CHANGE_REASON_LENGTH = 1000;

	/** Target Group Type */
	public static final int TYPE_GROUP = 0;
	/** Target Type */
	public static final int TYPE_TARGET = 1;

    /** the primary key of the Target. */
	@Id
	@Column(name="AT_OID", nullable =  false)
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
    /** The targets name. */
    @Column(name = "AT_NAME", length = 255, unique = true)
    private String name;
    /** the targets description. */
    @Column(name = "AT_DESC", length = 4000)
    private String description;
    /** The schedules related to the target. */
	@OneToMany(cascade = CascadeType.ALL) // default fetch type is LAZY {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH}
	@JoinColumn(name = "S_ABSTRACT_TARGET_ID")
    private Set<Schedule> schedules = new HashSet<Schedule>();
    /** Owner of the target **/
    @ManyToOne
    @JoinColumn(name = "AT_OWNER_ID")
    private User owner;
    /** Profile Overrides */
    @ManyToOne(cascade = CascadeType.ALL) // WAS cascade="save-update"
    @JoinColumn(name = "AT_PROF_OVERRIDE_OID", foreignKey = @ForeignKey(name = "FK_T_PROF_OVERRIDE_OID"))
    private ProfileOverrides overrides = new ProfileOverrides();
    /** The loaded state of the target **/
    @Transient
    private int originalState = -1;    
    /** The state of the target **/
    @Column(name = "AT_STATE")
    private int state; 
    /** The list of annotations. */
    @Transient
    private List<Annotation> annotations = new LinkedList<Annotation>();
    /** The list of deleted annotations. */
	@Transient
	private List<Annotation> deletedAnnotations = new LinkedList<Annotation>();
    /** True if the annotations have been loaded */
	@Transient
	private boolean annotationsSet = false;
	/** Flag to state if the annotations have been sorted */
	@Transient
	private boolean annotationsSorted = false;
	/** Flag to state if the annotations contain any flagged as alertable, making the whole target/group alertable */
	@Transient
	private boolean alertable = false;
	/** Removed Schedules */
	@Transient
	private Set<Schedule> removedSchedules = new HashSet<Schedule>();
	/** The target's base profile. */
    @ManyToOne
    @JoinColumn(name = "T_PROFILE_ID")
    private Profile profile = new Profile();
    /** The date the Target was created */
    @Column(name = "AT_CREATION_DATE", columnDefinition = "TIMESTAMP(9)")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    /** The parents of this group */
    @OneToMany // default fetch type is LAZY
    @JoinColumn(name = "GM_CHILD_ID")
    private Set<GroupMember> parents = new HashSet<GroupMember>();
    /** Flag to state if the object is "dirty" */
	@Transient
	private boolean dirty = false;
	/**
     * Identifies whether this is a target or group without needing to use
     * the instanceof operator, which can be important if the object is not
     * fully initialised by Hibernate.
     */
    @Column(name = "AT_OBJECT_TYPE")
    protected int objectType;
    /** reference number to use when storing instances to the SIP.*/
    @Column(name = "AT_REFERENCE", length = 255)
    private String referenceNumber;
    /** A cross-domain information resource description of the target.*/
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "AT_DUBLIN_CORE_OID", foreignKey = @ForeignKey(name = "FK_AT_DUBLIN_CORE_OID"))
    private DublinCore dublinCoreMetaData;
    /** The Profile Note */
    @Column(name = "AT_PROFILE_NOTE", length = 255)
    private String profileNote = null;

	@Transient
	private List<GroupMember> newParents = new LinkedList<GroupMember>();
	@Transient
	private Set<Long> removedParents = new HashSet<Long>();

    @Column(name = "AT_DISPLAY_TARGET")
    private boolean displayTarget = true;

	/** Why this target was rejected */
    @ManyToOne
    @JoinColumn(name = "AT_RR_OID", foreignKey = @ForeignKey(name = "FK_AT_RR_OID"))
	protected RejReason rejReason;
	
    /** The total number of crawls (<code>TargetInstance</code>s) associated with the Target **/
    @Column(name = "AT_CRAWLS")
	@Formula("(SELECT COUNT(*) FROM DB_WCT.TARGET_INSTANCE TI WHERE TI.TI_TARGET_ID=AT_OID)")
    private int crawls = 0;
    
    /** The oid of the <code>TargetInstance</code> denoted as the <code>Target</code>s reference crawl **/
    @Column(name = "AT_REFERENCE_CRAWL_OID")
    private Long referenceCrawlOid = null;
    
    /** Determines if the any new target instances should be auto-pruned **/
    @Column(name = "AT_AUTO_PRUNE")
    private boolean autoPrune = false;
    
    /** Determines if a target instance should be denoted as a reference crawl when it is archived **/
    @Column(name = "AT_AUTO_DENOTE_REFERENCE_CRAWL")
    private boolean autoDenoteReferenceCrawl = false;
    
    /** Any information that should be given to the Archivists **/
    @Column(name = "AT_REQUEST_TO_ARCHIVISTS", length = 4000)
    private String requestToArchivists;
    
    /** The access zone of the target **/
    public static class AccessZone {
        public final static int PUBLIC=0, ONSITE=1, RESTRICTED=2;
    	private static String[] accessZoneText = {"Public","Onsite","Restricted"};
    	public static int getCount(){return accessZoneText.length;};
        public static String getText(int accessZone)
        {
        	if(accessZone < accessZoneText.length)
        	{
	         	return accessZoneText[accessZone];
        	}
        	
        	return "";
        }
    }

    @Column(name = "AT_ACCESS_ZONE")
    private int accessZone; 

    /** The Display Note */
    @Column(name = "AT_DISPLAY_NOTE", length = 4000)
    private String displayNote = null;
    
    /** The Display Change Reason */
    @Column(name = "AT_DISPLAY_CHG_REASON", length = 1000)
    private String displayChangeReason = null;

    /**
     * Base constructor to prevent no-arg instantiation. 
     */
    protected AbstractTarget() {
    }
    
    
    /**
     * Constructor that sets the Object type.
     * @param objectType Either TYPE_TARGET or TYPE_GROUP
     */
    protected AbstractTarget(int objectType) {
    	this.objectType = objectType;
    }
    
    /**
     * Returns the OID of the AbstractTarget.
     * @return Returns the oid.
     */
    public Long getOid() {
        return oid;
    }

    /**
     * Sets the OID of the AbstractTarget.
     * @param aOid The oid to set.
     */
    public void setOid(Long aOid) {
        this.oid = aOid;
    }
    
    /**
     * Returns the description of the AbstractTarget.
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the AbstractTarget.
     * @param aDescription The description to set.
     */
    public void setDescription(String aDescription) {
    	if(!Utils.hasChanged(description, aDescription)) { dirty = true; }
        this.description = aDescription;
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
    	if(!Utils.hasChanged(name, aName)) { dirty = true; }
        this.name = aName;
        if (name != null) {
        	this.name = this.name.trim();
        }
    }    

    
	/**
	 * Returns whether the object has been modified since being loaded from
	 * the database, or initialised from the BusinessObjectFactory.
	 * @return true if changed; otherwise false.
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Sets the object to be "dirty" (modified since initialisation).
	 * @param dirty true to indicate it has been changed; false to indicate it
	 * 				has not.
	 */
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	/* (Non-javadoc)
	 *  @see Annotatable#addAnnotation(Annotation). 
	 */
	public void addAnnotation(Annotation annotation) {
		annotations.add(annotation);
		annotationsSorted = false;
		alertable=false;
		for (Annotation ann: annotations) {
			if (ann.isAlertable()) {
				alertable=true;
				break;
			}
		}
	}
	
	/* (Non-javadoc)
	 *  @see Annotatable#getAnnotation(int). 
	 */
	public Annotation getAnnotation(int index) {
		return annotations.get(index);	
	}
		
	/* (Non-javadoc)
	 *  @see Annotatable#deleteAnnotation(int).
	 */
	public void deleteAnnotation(int index)
	{
		Annotation annotation = annotations.get(index);
		if(annotation != null)
		{
			deletedAnnotations.add(annotation);
			annotations.remove(index);
		}
		alertable=false;
		for (Annotation ann: annotations) {
			if (ann.isAlertable()) {
				alertable=true;
				break;
			}
		}

	}
	
	/* (non-Javadoc)
	 * @see org.webcurator.domain.model.core.Annotatable#getAnnotations()
	 */
	public List<Annotation> getAnnotations() {		
		return annotations;
	}
	
	/* (non-Javadoc)
	 * @see org.webcurator.domain.model.core.Annotatable#getDeletedAnnotations()
	 */
	public List<Annotation> getDeletedAnnotations() {		
		return deletedAnnotations;
	}
	
	/* (non-Javadoc)
	 * @see org.webcurator.domain.model.core.Annotatable#setAnnotations(java.util.List)
	 */
	public void setAnnotations(List<Annotation> aAnnotations) {		
		annotations = aAnnotations;
		deletedAnnotations.clear();
		annotationsSet = true;
		annotationsSorted = false;
		alertable=false;
		for (Annotation ann: annotations) {
			if (ann.isAlertable()) {
				alertable=true;
				break;
			}
		}
	}
	
	/**
	 * Returns true if the annotations have been initialised.
	 * @return true if the annotations have been initialised; otherwise false.
	 */
	public boolean isAnnotationsSet() {
		return annotationsSet;
	}	
    
	/*(non-Javadoc)
	 * @see org.webcurator.domain.model.core.Annotatable#getSortedAnnotations()
	 */
	public List<Annotation> getSortedAnnotations()
	{
		if(!annotationsSorted)
		{
			sortAnnotations();
		}
		return getAnnotations();
	}
		
	/*(non-Javadoc)
	 * @see org.webcurator.domain.model.core.Annotatable#sortAnnotations()
	 */
	public void sortAnnotations()
	{
		Collections.sort(annotations);
		annotationsSorted = true;
	}
		
	/**
	 * Returns the owner of the AbstractTarget.
	 * @return Returns the owner.
	 */
	public User getOwner() {
		return owner;
	}

	/**
	 * Sets the owner of the AbstractTarget.
	 * @param owner The owner to set.
	 */
	public void setOwner(User owner) {
		this.owner = owner;
	}

	/**
	 * Gets the owner of the AbstractTarget
	 * @return The owner of the AbstractTarget.
	 */
	public User getOwningUser() {
		return owner;
	}
	
    /**
     * Retrieves the profile overrides of the AbstractTarget.
	 * @return Returns the overrides.
	 */
	public ProfileOverrides getOverrides() {
		return overrides;
	}
	
    /**
     * Retrieves the profile overrides of the AbstractTarget.
	 * @return Returns the overrides.
	 */
	public ProfileOverrides getProfileOverrides() {
		return getOverrides();
	}	

	/**
	 * Sets the profile overrides of the AbstractTarget.
	 * @param overrides The overrides to set.
	 */
	public void setOverrides(ProfileOverrides overrides) {
		this.overrides = overrides;
	}	
	
	/**
	 * Gets the schedules of the AbstractTarget.
     * @return Returns the schedules.
     */
    public Set<Schedule> getSchedules() {
        return schedules;
    }

    /**
     * Sets the schedules on the AbstractTarget.
     * @param aSchedules The schedules to set.
     */
    public void setSchedules(Set<Schedule> aSchedules) {
        this.schedules = aSchedules;
    }
    
	/**
	 * Retrieves the original state, which is the state of initialisation or 
	 * the state that was loaded from the database. This is used to detect that
	 * the state has changed so that the TargetManager can perform the 
	 * necessary persistence logic.
	 * @return Returns the original state.
	 */
	public int getOriginalState() {
		if(this.originalState==-1){
			return this.state;
		}
		return this.originalState;
	}

	/**
	 * Returns the state of the AbstractTarget.
	 * @return Returns the state.
	 */
	public int getState() {
		return state;
	}
	
	
	/**
	 * Updates the state of the AbstractTarget. This should be used by non-
	 * internal classes instead of setState, allowing additional logic to be
	 * tied into this point without affecting Hibernate. 
	 * 
	 * @param newState The new state of the AbstractTarget.
	 */
	public void changeState(int newState) {
		setState(newState);
	}
	
	
	/**
	 * Private setter for Hibernate access.
	 * @param state The state of the object.
	 */
	protected void setState(int state) {
		this.state = state;
		if(originalState == -1) {
			originalState = state;
		}
	}
	
	/**
	 * Add a schedule to the AbstractTarget.
	 * @param aSchedule The schedule to add.
	 */
	public void addSchedule(Schedule aSchedule) {
		// Set the bi-directional navigation.
		aSchedule.setTarget(this);
		schedules.add(aSchedule);
	}
	
	/**
	 * Remove a schedule from the AbstractTarget.
	 * @param aSchedule The schedule to remove.
	 */
	public void removeSchedule(Schedule aSchedule) {
		// Remove the schedule
		this.schedules.remove(aSchedule);
		
		// If the schedule is an original, then we need to track its removal
		// to perform the business logic on save.
		if(!aSchedule.isNew()) {
			// Add record that fact that it has been removed.
			removedSchedules.add(aSchedule);
		}
	}	

	/**
	 * Gets the set of persisted schedules that have been removed from the 
	 * AbstractTarget and therefore need to be removed from the database. 
	 * @return A Set of persisted Schedule objects that have been removed from  
	 * 		   the AbstractTarget
	 */
	public Set<Schedule> getRemovedSchedules() {
		return removedSchedules;
	}
	
	
	/**
	 * Gets the associated harvest profile for this AbstractTarget.
	 * @return Returns the harvest profile associated with this AbstractTarget.
	 */
	public Profile getProfile() {
		return profile;
	}
	
	/**
	 * Returns the set of groups to which this AbstractTarget belongs.
	 * @return Returns a Set of GroupMember objects that identify child/parent
	 * 		   relationships.
     */
	public Set<GroupMember> getParents() {
		return parents;
	}

	/**
	 * Hibernate required method. Sets the parents of the object. 
	 * @param parents The parents to set.
	 */
	public void setParents(Set<GroupMember> parents) {
		this.parents = parents;
	}	
	
	
	/**
	 * Set the profile associated with this AbstractTarget.
	 * @param aProfile The profile to associate this target with.
	 */
	public void setProfile(Profile aProfile) {
		// don't setDirty here anymore..
		//if(!Utils.hasChanged(profile, aProfile)) { setDirty(true); }
		this.profile = aProfile;
	}   	
	
	
	/**
	 * Get the set of seeds that belong to this target.
	 * @return The set of seeds that belong to this target.
	 */
	public abstract Set<Seed> getSeeds();

	/**
	 * Is the target now schedulable?
	 * @return True if the new state of the target is schedulable.
	 */
	public abstract boolean isSchedulable();	
	
	/**
	 * Returns the object type (TYPE_TARGET or TYPE_GROUP). This can be used
	 * instead of instanceof, which is useful if the object isn't fully 
	 * initialised by Hibernate.
	 * @return Either TYPE_TARGET or TYPE_GROUP.
	 */
	public int getObjectType() {
		return objectType;
	}
	
	/**
	 * setObjectType is required for Hibernate. It is not used
	 * elsewhere.
	 * @param type The object type.
	 */
	@SuppressWarnings("unused")
	private void setObjectType(int type) {
		objectType = type;
	}
	
	/* (non-Javadoc)
	 * @see org.webcurator.core.notification.InTrayResource#getResourceName()
	 */
    public String getResourceName() {
    	return name;
    }
    
    /* (non-Javadoc)
     * @see org.webcurator.core.notification.InTrayResource#getResourceType()
     */
    public String getResourceType() {
    	if(this instanceof TargetGroup)
    	{
    		//special case for lazy loaded TargetGroup
    		return TargetGroup.class.getName();
    	}
    	else
    	{
    		return this.getClass().getName();
    	}
    }
    
	/**
	 * Get the date that the AbstractTarget was created.
	 * @return Returns the creation date.
	 */
	public Date getCreationDate() {
		return creationDate;
	}
	
	/**
	 * Set the date the object was created.
	 * @param creationDate The creationDate to set.
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the referenceNumber used for the instances stored in the archive.
	 */
	public String getReferenceNumber() {
		return referenceNumber;
	}

	/**
	 * @param referenceNumber the referenceNumber used for the instances stored in the archive to set.
	 */
	public void setReferenceNumber(String referenceNumber) {
		this.referenceNumber = referenceNumber;
	}  	


	/**
	 * @return Returns the profileNote.
	 */
	public String getProfileNote() {
		return profileNote;
	}


	/**
	 * @param profileNote The profileNote to set.
	 */
	public void setProfileNote(String profileNote) {
		this.profileNote = profileNote;
	}    
 
	
		
	/**
	 * @return the dublinCoreMetaData
	 */
	public DublinCore getDublinCoreMetaData() {
		return dublinCoreMetaData;
	}

	/**
	 * @param dublinCoreMetaData the dublinCoreMetaData to set
	 */
	public void setDublinCoreMetaData(DublinCore dublinCoreMetaData) {
		this.dublinCoreMetaData = dublinCoreMetaData;
	}

	/**
	 * Get the Rejection Reason of this target (if any).
	 * @return The RejReason object corresponding to the reason specified when a
	 * target is rejected.
	 */
	public RejReason getRejReason() {
		return rejReason;
	}

	/**
	 * Set the rejection reason for this target.
	 * @param rejReason The RejReason object.
	 */
	public void setRejReason(RejReason rejReason) {
		this.rejReason = rejReason;
	}

	/**
	 * @return Returns the displayTarget boolean.
	 */
	public boolean isDisplayTarget() {
		return displayTarget;
	}

	/**
	 * @param displayTarget The displayTarget to set.
	 */
	public void setDisplayTarget(boolean displayTarget) {
		this.displayTarget = displayTarget;
	}

	/**
	 * @return Returns the alertable boolean.
	 */
	public boolean getAlertable() {
		return alertable;
	}

	/**
	 * Returns the access zone of the AbstractTarget.
	 * @return Returns the access zone.
	 */
	public int getAccessZone() {
		return accessZone;
	}
	
	/**
	 * Updates the access zone of the AbstractTarget. 
	 * 
	 * @param accessZone The new access zone of the AbstractTarget.
	 */
	public void setAccessZone(int accessZone) {
		this.accessZone = accessZone;
	}

	/**
	 * Returns the text for the access zone of the AbstractTarget. 
	 * 
	 * @return accessZoneText The text for the current access zone of the AbstractTarget.
	 */
    public String getAccessZoneText()
    {
    	return AccessZone.getText(this.accessZone);
    }
	
	/**
	 * @return Returns the displayNote.
	 */
	public String getDisplayNote() {
		return displayNote;
	}


	/**
	 * @param displayNote The displayNote to set.
	 */
	public void setDisplayNote(String displayNote) {
		this.displayNote = displayNote;
	}    
	
	/**
	 * @return Returns the displayChangeReason.
	 */
	public String getDisplayChangeReason() {
		return displayChangeReason;
	}

	/**
	 * @param displayChangeReason The displayChangeReason to set.
	 */
	public void setDisplayChangeReason(String displayChangeReason) {
		this.displayChangeReason = displayChangeReason;
	}    
	
	public List<GroupMember> getNewParents() {
		return newParents;
	}


	public Set<Long> getRemovedParents() {
		return removedParents;
	}
	
	/**
	 * Get the number of previous crawls for the <code>Target</code>
	 * @return The total number of previous crawls
	 */
	public int getCrawls() {
		return this.crawls;
	}
	
	/**
	 * Set the number of previous crawls for the <code>Target</code>
	 * @param crawls the number of previous crawls
	 */
	public void setCrawls(int crawls) {
		this.crawls = crawls;
	}
	
	/**
	 * @return The oid of the <code>TargetInstance</code> that has been denoted as this <code>Target</code>s a reference crawl, null otherwise
	 */
	public Long getReferenceCrawlOid() {
		return this.referenceCrawlOid;
	}
	
	/**
	 * Denotes the specified <code>TargetInstance</code> as a reference crawl
	 * @param targetInstanceOid the oid of the <code>TargetInstance</code> to denote as the reference crawl
	 */
	public void setReferenceCrawlOid(Long targetInstanceOid) {
		this.referenceCrawlOid = targetInstanceOid;
	}
	
	/**
	 * @return true if new <code>TargetInstance</code>s should be autopruned, false otherwise
	 */
	public Boolean isAutoPrune() {
		return this.autoPrune;
	}
	
	/**
	 * Sets the auto prune state for this <code>Target</code>
	 * @param autoPrune the state to set
	 */
	public void setAutoPrune(Boolean autoPrune) {
		this.autoPrune = autoPrune;
	}
	
	/**
	 * @return true if new <code>TargetInstance</code>s should be automatically denoted as a reference crawl, false otherwise
	 */
	public Boolean isAutoDenoteReferenceCrawl() {
		return this.autoDenoteReferenceCrawl;
	}
	
	/**
	 * Sets the auto denote reference crawl state for this <code>Target</code>
	 * @param autoDenoteReferenceCrawl the state to set
	 */
	public void setAutoDenoteReferenceCrawl(Boolean autoDenoteReferenceCrawl) {
		this.autoDenoteReferenceCrawl = autoDenoteReferenceCrawl;
	}

    /**
     * Returns the descriptive request for the Archivists.
     * @return Returns the request.
     */
	public String getRequestToArchivists() {
		return requestToArchivists;
	}


	/**
	 * @param requestToArchivists the requestToArchivists to set
	 */
	public void setRequestToArchivists(String requestToArchivists) {
		this.requestToArchivists = requestToArchivists;
	}
}
