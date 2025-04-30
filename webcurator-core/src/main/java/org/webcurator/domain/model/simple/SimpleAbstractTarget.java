package org.webcurator.domain.model.simple;

import org.webcurator.domain.UserOwnable;
import org.webcurator.domain.model.auth.User;

import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
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
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class SimpleAbstractTarget implements UserOwnable {
    /**
     * the primary key of the Target.
     */
    @Id
    @NotNull
    @Column(name = "AT_OID")
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
    /**
     * The targets name.
     */
    @Size(max = 255)
    @Column(name = "AT_NAME")
    private String name;
    /**
     * the targets description.
     */
    @Size(max = 4000)
    @Column(name = "AT_DESC")
    private String description;

    /**
     * Owner of the target
     **/
    @ManyToOne
    @JoinColumn(name = "AT_OWNER_ID")
    private User owner;

    /**
     * The loaded state of the target
     **/
    @Transient
    private int originalState = -1;
    /**
     * The state of the target
     **/
    @Column(name = "AT_STATE")
    private int state;

    /**
     * The date the Target was created
     */
    @Column(name = "AT_CREATION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    /**
     * The parents of this group
     */
    @OneToMany(orphanRemoval = true) // default fetch type is LAZY
    @JoinColumn(name = "GM_CHILD_ID")
    private Set<SimpleGroupMember> parents = new HashSet<SimpleGroupMember>();

    /**
     * Identifies whether this is a target or group without needing to use
     * the instanceof operator, which can be important if the object is not
     * fully initialised by Hibernate.
     */
    @Column(name = "AT_OBJECT_TYPE")
    protected int objectType;


    @Column(name = "AT_DISPLAY_TARGET")
    private boolean displayTarget = true;


    /**
     * Base constructor to prevent no-arg instantiation.
     */
    protected SimpleAbstractTarget() {
    }


    /**
     * Constructor that sets the Object type.
     *
     * @param objectType Either TYPE_TARGET or TYPE_GROUP
     */
    protected SimpleAbstractTarget(int objectType) {
        this.objectType = objectType;
    }

    public @NotNull Long getOid() {
        return oid;
    }

    public void setOid(@NotNull Long oid) {
        this.oid = oid;
    }

    public @Size(max = 255) String getName() {
        return name;
    }

    public void setName(@Size(max = 255) String name) {
        this.name = name;
    }

    public @Size(max = 4000) String getDescription() {
        return description;
    }

    public void setDescription(@Size(max = 4000) String description) {
        this.description = description;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public int getOriginalState() {
        return originalState;
    }

    public void setOriginalState(int originalState) {
        this.originalState = originalState;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Set<SimpleGroupMember> getParents() {
        return parents;
    }

    public void setParents(Set<SimpleGroupMember> parents) {
        this.parents = parents;
    }

    public int getObjectType() {
        return objectType;
    }

    public void setObjectType(int objectType) {
        this.objectType = objectType;
    }

    public boolean isDisplayTarget() {
        return displayTarget;
    }

    public void setDisplayTarget(boolean displayTarget) {
        this.displayTarget = displayTarget;
    }

    /**
     * Gets the owner of the AbstractTarget
     *
     * @return The owner of the AbstractTarget.
     */
    public User getOwningUser() {
        return owner;
    }
}
