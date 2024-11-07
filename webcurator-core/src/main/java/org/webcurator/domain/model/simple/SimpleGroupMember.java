package org.webcurator.domain.model.simple;

import javax.validation.constraints.NotNull;
import javax.persistence.*;


/**
 * Represents a child/parent relationship between an AbstractTarget and a
 * TargetGroup.
 *
 * @author bbeaumont
 */
@Entity
@Table(name = "GROUP_MEMBER")
public class SimpleGroupMember {
    /**
     * The database oid
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
    private Long oid = null;
    /**
     * The parent of the member
     */
    @ManyToOne
    @JoinColumn(name = "GM_PARENT_ID")
    private SimpleTargetGroup parent = null;

    /**
     * Get the OID of the GroupMember.
     *
     * @return Returns the oid.
     */
    public Long getOid() {
        return oid;
    }

    /**
     * Set the OID of the GroupMember.
     *
     * @param aOid The oid to set.
     */
    public void setOid(Long aOid) {
        this.oid = aOid;
    }

    /**
     * Get the parent of the relationship.
     *
     * @return Returns the parent.
     */
    public SimpleTargetGroup getParent() {
        return parent;
    }

    /**
     * Set the parent of the relationship.
     *
     * @param parent The parent to set.
     */
    public void setParent(SimpleTargetGroup parent) {
        this.parent = parent;
    }

}

