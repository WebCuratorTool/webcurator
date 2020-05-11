package org.webcurator.domain.model.core;

import org.webcurator.core.notification.UserInTrayResource;
import org.webcurator.domain.model.auth.User;

import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import javax.persistence.*;
import java.util.*;

@SuppressWarnings("all")
@Entity
@Table(name = "HARVEST_RESULT")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class HarvestResult implements UserInTrayResource {
    private static final int MAX_MOD_NOTE_LENGTH = 2000;

    /** The state for an unassessed HarvestResult - neither endorsed, rejected, indexed nor aborted */
    public static final int STATE_UNASSESSED = 0;
    /** The state for an Endorsed HarvestResult - ready for archiving */
    public static final int STATE_ENDORSED = 1;
    /** The state constant for a Rejected HarvestResult - one that should not be archived */
    public static final int STATE_REJECTED = 2;
    /** The state constant for a Harvest Result that is being indexed. */
    public static final int STATE_INDEXING = 3;
    /** The state constant for a Harvest Result that has been aborted in indexing. */
    public static final int STATE_ABORTED = 4;
    /** The state constant for a Harvest Result that is being modified. */
    public static final int STATE_MODIFYING = 5;

    /** The TargetInstance that this belongs to */
    @ManyToOne
    @JoinColumn(name = "HR_TARGET_INSTANCE_ID")
    private TargetInstance targetInstance;
    /** The Harvest number; the original harvest is always number 1, the prune tool can created additional harvest results */
    @Column(name = "HR_HARVEST_NO")
    private int harvestNumber = 1;
    /** The primary key of the harvest result */
    @Id
    @NotNull
    @Column(name="HR_OID")
    // Note: From the Hibernate 4.2 documentation:
    // The Hibernate team has always felt such a construct as fundamentally wrong.
    // Try hard to fix your data model before using this feature.
    @TableGenerator(name = "SharedTableIdGenerator",
            table = "ID_GENERATOR",
            pkColumnName = "IG_TYPE",
            valueColumnName = "IG_VALUE",
            pkColumnValue = "HarvestResult",
            allocationSize = 1) // 50 is the default
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SharedTableIdGenerator")
    private Long oid = null;
    /** An index of the resources within this harvest */
    // cascade="save-update"
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE}) // default fetch type is LAZY
    @JoinColumn(name = "HRC_HARVEST_RESULT_OID")
    @MapKeyColumn(name = "HRC_NAME")
    private Map<String,HarvestResource> resources = new HashMap<String,HarvestResource>();
    /** The provenance note (how this harvest result was created */
    @Size(max=1024)
    @NotNull
    @Column(name = "HR_PROVENANCE_NOTE")
    private String provenanceNote;
    /** The creation date of this harvest result */
    @Column(name = "HR_CREATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    /** Who created this harvest result */
    @ManyToOne
    @JoinColumn(name = "HR_CREATED_BY_ID")
    private User createdBy;
    /** The state of the HarvestResult - see the STATE_xxx constants */
    @Column(name = "HR_STATE")
    private int state = 0;
    /** A list of Harvest Modification Notes */
    // TODO @hibernate.list table="HR_MODIFICATION_NOTE" cascade="all-delete-orphan" --> not sure if can do cascade
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "HR_MODIFICATION_NOTE", joinColumns = @JoinColumn(name = "HMN_HR_OID"))
    @Size(max=2000)
    @Column(name = "HMN_NOTE")
    @OrderColumn(name = "HMN_INDEX")
    private List<String> modificationNotes = new LinkedList<String>();
    /** The Harvest ID that this harvest was derived from */
    @Column(name = "HR_DERIVED_FROM")
    private Integer derivedFrom;
    /** Why this harvest result was rejected */
    @ManyToOne
    @JoinColumn(name = "HR_RR_OID")
    private RejReason rejReason;

    /**
     * Construct a new HarvestResult.
     */
    public HarvestResult() {
        super();
        this.creationDate = new Date();
    }

    public HarvestResult(TargetInstance aTargetInstance, int harvestNumber) {
        super();
        this.targetInstance = aTargetInstance;
        this.creationDate = new Date();
        this.createdBy = aTargetInstance.getOwner();
        this.harvestNumber = harvestNumber;
    }

    /**
     * Create an HarvestResult from its DTO.
     * @param aResultDTO The DTO.
     * @param aTargetInstance The TargetInstance that this HarvestResult
     * 						  belongs to.
     */
    public HarvestResult(HarvestResultDTO aResultDTO, TargetInstance aTargetInstance) {
        super();
        targetInstance = aTargetInstance;
        harvestNumber = aResultDTO.getHarvestNumber();
        provenanceNote = aResultDTO.getProvenanceNote();
        creationDate = aResultDTO.getCreationDate();
        createdBy = aTargetInstance.getOwner();
    }

    /**
     * Get the number of the harvest result. This is 1 for the original harvest.
     * Additional harvests may be created by the quality review tools.
     * @return the number of the harvest  result.
     */
    public int getHarvestNumber() {
        return harvestNumber;
    }

    /**
     * Set the number of the harvest result.
     * @param harvestNumber The number of the harvest result.
     */
    public void setHarvestNumber(int harvestNumber) {
        this.harvestNumber = harvestNumber;
    }

    /**
     * Get the primary key of the HarvestResult object.
     * @return the primary key
     */
    @Override
    public Long getOid() {
        return oid;
    }

    /**
     * Set the oid of the HarvestResult object.
     * @param oid the primary key of the HarvestResult.
     */
    public void setOid(Long oid) {
        this.oid = oid;
    }

    /**
     * Get the target instance that this object belongs to.
     * @return The target instance that this object belongs to.
     */
    public TargetInstance getTargetInstance() {
        return targetInstance;
    }

    /**
     * Set the target instance that this belongs to.
     * @param targetInstance The target instance that this belongs to.
     */
    public void setTargetInstance(TargetInstance targetInstance) {
        this.targetInstance = targetInstance;
    }

    /**
     * Retrieve the map of resource names to HarvestResource objects. This can
     * be used for generating a tree of the resources or developing the
     * browse tool. This is essentially a name-based index of the harvest
     * result.
     *
     * @return the map of resource names to HarvestResource objects.
     */
    public Map<String, HarvestResource> getResources() {
        return resources;
    }

    /**
     * Set the Map of resource name to HarvestResource objects.
     * @param resources The Map of resource name to HarvestResource objects.
     */
    public void setResources(Map<String, HarvestResource> resources) {
        this.resources = resources;
    }

    /**
     * Get the provenance note that explains why this harvest was created.
     * @return the note that explains why this harvest was created.
     */
    public String getProvenanceNote() {
        return provenanceNote;
    }

    /**
     * Set the provenance note on this HarvestResult.
     * @param provenanceNote The note that explains why this result was created.
     */
    public void setProvenanceNote(String provenanceNote) {
        this.provenanceNote = provenanceNote;
    }

    /**
     * Get the date the result was created.
     * @return the date the record was created
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Set the creation date of the harvest result.
     * @param creationDate The creation date of the harvest result.
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Get the User that created the harvest result.
     * @return The User object for the user that created this object.
     */
    public User getCreatedBy() {
        return createdBy;
    }

    /**
     * Set the creator of this harvest result.
     * @param createdBy The User object for the user that created this object.
     */
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Get the Rejection Reason of this harvest result (if any).
     * @return The RejReason object corresponding to the reason specified when a
     * harvest is rejected.
     */
    public RejReason getRejReason() {
        return rejReason;
    }

    /**
     * Set the rejection reason for this harvest result.
     * @param rejReason The RejReason object.
     */
    public void setRejReason(RejReason rejReason) {
        this.rejReason = rejReason;
    }

    /**
     * Get the state of this Harvest Result.
     * @return the state
     */
    public int getState() {
        return state;
    }

    /**
     * Set the state of this harvest result.
     * @param state the state to set
     */
    public void setState(int state) {
        this.state = state;
    }


    /**
     * Safe way to add modification notes and ensure that they are truncated
     * to the appropriate length to fit in the database.
     * @param notes A List of notes to add.
     */
    public void addModificationNotes(List<String> notes) {
        for(String s: notes) {
            addModificationNote(s);
        }
    }

    /**
     * Safe way to add modification notes and ensure that they are truncated
     * to the appropriate length to fit in the database.
     * @param str The note to add.
     */
    public void addModificationNote(String str) {
        if(str.length() > HarvestResult.MAX_MOD_NOTE_LENGTH) {
            str = str.substring(0, HarvestResult.MAX_MOD_NOTE_LENGTH);
        }
        modificationNotes.add(str);
    }

    /**
     * Get the list of modification notes.
     * @return The list of modification notes.
     */
    public List<String> getModificationNotes() {
        return modificationNotes;
    }

    public void setModificationNotes(List<String> modificationNotes) {
        this.modificationNotes = modificationNotes;
    }

    /**
     * Get the Harvest Number that this harvest was derived from.
     * @return The harvest number that this harvest was derived from. If original, then
     *         this will be null.
     */
    public Integer getDerivedFrom() {
        return derivedFrom;
    }

    public void setDerivedFrom(Integer derivedFrom) {
        this.derivedFrom = derivedFrom;
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.notification.InTrayResource#getResourceName()
     */
    @Override
    public String getResourceName() {
        return this.getTargetInstance().getOid().toString()+"("+this.harvestNumber+")";
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.notification.InTrayResource#getResourceType()
     */
    @Override
    public String getResourceType() {
        return this.getClass().getName();
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.notification.InTrayResource#getOwningUser()
     */
    @Override
    public User getOwningUser() {
        return getCreatedBy();
    }
}
