package org.webcurator.domain.model.core;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.GenericGenerator;
import org.webcurator.domain.model.auth.User;

import javax.persistence.*;

/**
 * View of AbstractTarget returning the group type if a group.
 * 
 * @author bbeaumont
 */
// lazy="true"
@Entity
@Table(name = "ABSTRACT_TARGET_GROUPTYPE_VIEW")
@NamedQueries({
        @NamedQuery(name = "org.webcurator.domain.model.core.AbstractTargetGroupTypeView.getNonSubGroupDTOsByNameAndType",
            query = "SELECT new org.webcurator.domain.model.dto.AbstractTargetDTO(t.oid, t.name, t.owner.oid, t.owner.username, t.owner.agency.name, t.state, t.profile.oid, t.objectType, t.type) FROM AbstractTargetGroupTypeView t where lower(t.name) like lower(:name) and ( t.type is null or t.type != :subgrouptype ) ORDER BY UPPER(t.name), t.type"),
        @NamedQuery(name = "org.webcurator.domain.model.core.AbstractTargetGroupTypeView.cntNonSubGroupDTOsByNameAndType",
            query = "SELECT count(*) FROM AbstractTargetGroupTypeView t where lower(t.name) like lower(:name) and ( t.type is null or t.type != :subgrouptype )")
})
public class AbstractTargetGroupTypeView {
	
	public static final String QUERY_NON_SUBGROUP_DTOS_BY_NAME_AND_TYPE = "org.webcurator.domain.model.core.AbstractTargetGroupTypeView.getNonSubGroupDTOsByNameAndType";
	public static final String QUERY_CNT_NON_SUBGROUP_DTOS_BY_NAME_AND_TYPE = "org.webcurator.domain.model.core.AbstractTargetGroupTypeView.cntNonSubGroupDTOsByNameAndType";	
	
    /** the primary key of the Target. */
	@Id
	@Column(name="AT_OID", nullable =  false)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MultipleHiLoPerTableGenerator")
	@GenericGenerator(name = "MultipleHiLoPerTableGenerator",
			strategy = "org.hibernate.id.MultipleHiLoPerTableGenerator",
			parameters = {
					@Parameter(name = "table", value = "ID_GENERATOR"),
					@Parameter(name = "primary_key_column", value = "IG_TYPE"),
					@Parameter(name = "value_column", value = "IG_VALUE"),
					@Parameter(name = "primary_key_value", value = "General")
			})
    private Long oid;
    /** The targets name. */
    @Column(name = "AT_NAME", length = 255, unique = true)
    private String name;
    /** the targets description. */
    @Column(name = "AT_DESC", length = 4000)
    private String description;
    /** The schedules related to the target. */
    @OneToMany(orphanRemoval = true, cascade = {CascadeType.ALL}) // default fetch type is LAZY
    @JoinColumn(name = "S_ABSTRACT_TARGET_ID")
    private Set<Schedule> schedules = new HashSet<Schedule>();
    /** Owner of the target **/
    @ManyToOne
    @JoinColumn(name = "AT_OWNER_ID")
    private User owner;
    /** Profile Overrides */
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }) // WAS cascade="save-update")
    @JoinColumn(name = "AT_PROF_OVERRIDE_OID")
    private ProfileOverrides overrides = new ProfileOverrides();
    /** The loaded state of the target **/
    private int originalState = -1;    
    /** The state of the target **/
    @Column(name = "AT_STATE")
    private int state; 
    /** The target's base profile. */
	@ManyToOne
	@JoinColumn(name = "T_PROFILE_ID")
    private Profile profile;
    /** The date the Target was created */
	@Column(name = "AT_CREATION_DATE", columnDefinition = "TIMESTAMP(9)")
	@Temporal(TemporalType.TIMESTAMP)
	private Date creationDate;
    /** The parents of this group */
	@OneToMany // default fetch type is LAZY
	@JoinColumn(name = "GM_CHILD_ID")
    private Set<GroupMember> parents = new HashSet<GroupMember>();
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
    @JoinColumn(name = "AT_DUBLIN_CORE_OID")
    private DublinCore dublinCoreMetaData;
    /** The Profile Note */
    @Column(name = "AT_PROFILE_NOTE", length = 255)
    private String profileNote = null;

    @Column(name = "AT_ACCESS_ZONE")
    private int accessZone; 

    @Column(name = "AT_DISPLAY_TARGET")
    private boolean displayTarget = true;

    /** The Display Note */
    @Column(name = "AT_DISPLAY_NOTE", length = 4000)
    private String displayNote = null;
    
	/** The type of the group or null if a target */
	@Column(name = "TG_TYPE", length = 255)
	private String type;
    
    
    /**
     * Base constructor to prevent no-arg instantiation. 
     */
    protected AbstractTargetGroupTypeView() {
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
        this.name = aName;
        if (name != null) {
        	this.name = this.name.trim();
        }
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
     * Retrieves the profile overrides of the AbstractTarget.
	 * @return Returns the overrides.
	 */
	public ProfileOverrides getOverrides() {
		return overrides;
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
	 * Returns the state of the AbstractTarget.
	 * @return Returns the state.
	 */
	public int getState() {
		return state;
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
		this.profile = aProfile;
	}   	
	
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
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}
}
