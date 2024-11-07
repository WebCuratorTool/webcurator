package org.webcurator.domain.model.simple;


import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.webcurator.core.util.WctUtils;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * A TargetGroup contains a number of child targets or target groups.
 */
// TODO lazy="true"
@Entity
@Table(name = "TARGET_GROUP")
@PrimaryKeyJoinColumn(name = "TG_AT_OID", referencedColumnName = "AT_OID")
public class SimpleTargetGroup extends SimpleAbstractTarget {

    /** The maximum length of the type field. */
    public static final int MAX_TYPE_LENGTH = 255;

    /** The TargetGroup is Active - at least one child can be scheduled */
    public static final int STATE_ACTIVE = 9;
    /** The TargetGroup is inactive - the TargetGroup has reached its end date or all of its children have reached their end dates */
    public static final int STATE_INACTIVE = 10;
    /** The TargetGroup is Pending - none of its children are active */
    public static final int STATE_PENDING = 8;

    /** The type constant for a One SIP group - a group that gets harvested as a single target instance */
    public static final int ONE_SIP = 1;
    /** The type constant for a Many SIP group - a group that results in creating a target instance per child */
    public static final int MANY_SIP = 2;

    /** The type of the Group; one sip or many sip */
    @Column(name = "TG_SIP_TYPE")
    private int sipType = ONE_SIP;
    /** Date at which the Group's membership starts for meta-data purposes. */
    @Column(name = "TG_START_DATE")
    @Temporal(TemporalType.DATE)
    private Date fromDate = null;
    /** Date at which the Group's membership ends for meta-data purposes. */
    @Column(name = "TG_END_DATE")
    @Temporal(TemporalType.DATE)
    private Date toDate   = null;
    /** The ownership meta data. */
    @Size(max=255)
    @Column(name = "TG_OWNERSHIP_METADATA")
    private String ownershipMetaData = null;
    /** Children */
    @OneToMany // default fetch type is LAZY
    @JoinColumn(name = "GM_PARENT_ID")
    private Set<SimpleGroupMember> children = new HashSet<>();

    /** The type of the group */
    @Size(max=255)
    @Column(name = "TG_TYPE")
    private String type;

    /**
     * Protected constructor to ensure instantiation is through the 
     * BusinessObjectFactory.
     */
    protected SimpleTargetGroup() {
        super(SimpleAbstractTarget.TYPE_GROUP);
    }

    /* (non-Javadoc)
     * @see org.webcurator.domain.model.simple.AbstractTarget#getSeeds()
     */
    public Set<SimpleSeed> getSeeds() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Gets whether the TargetGroup is one SIP or many SIP.
     * @return Returns the type - either ONE_SIP or MANY_SIP.
     */
    public int getSipType() {
        return sipType;
    }

    /**
     * Sets whether the TargetGroup is ONE_SIP or MANY_SIP.
     * @param type The type to set.
     */
    public void setSipType(int type) {
        this.sipType = type;
    }

    /**
     * Get the date that the group becomes active.
     * @return Returns the fromDate.
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     * Set the date the group becomes active.
     * @param fromDate The fromDate to set.
     */
    public void setFromDate(Date fromDate) {
        if(fromDate == null) {
            this.fromDate = null;
        }
        else {
            this.fromDate = WctUtils.clearTime(fromDate);
        }
    }

    /**
     * Get the date the group becomes inactive.
     * @return Returns the toDate.
     */
    public Date getToDate() {
        return toDate;
    }

    /**
     * Set the date the group becomes inactive.
     * @param toDate The toDate to set.
     */
    public void setToDate(Date toDate) {
        if(toDate == null) {
            this.toDate = null;
        }
        else {
            this.toDate = WctUtils.endOfDay(toDate);
        }
    }

    /**
     * Get the ownership Meta Data for the TargetGroup. This allows additional
     * information about ownership to be added to the group, which is important
     * since a group can only have a single owner.
     *
     * @return Returns the ownerhsipMetaData.
     */
    public String getOwnershipMetaData() {
        return ownershipMetaData;
    }

    /**
     * Set the ownership meta data for the TargetGroup.
     * @see #getOwnershipMetaData()
     * @param ownerhsipMetaData The ownerhsipMetaData to set.
     */
    public void setOwnershipMetaData(String ownerhsipMetaData) {
        this.ownershipMetaData = ownerhsipMetaData;
    }

    /**
     * Gets a set of all the children.
     * @return Returns the children.
     */
    public Set<SimpleGroupMember> getChildren() {
        return children;
    }

    /**
     * Sets the set of children.
     * @param children The children to set.
     */
    public void setChildren(Set<SimpleGroupMember> children) {
        this.children = children;
    }

    /**
     * Is the target now schedulable?
     * @return True if the new state of the target is schedulable.
     */
    public boolean isSchedulable() {
        return getState() == SimpleTargetGroup.STATE_ACTIVE;
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
