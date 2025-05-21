package org.webcurator.domain.model.simple;


import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import javax.persistence.*;

@Entity
@Table(name = "SEED")
public class SimpleSeed {
    /**
     * The unique ID of the seed
     **/
    @Id
    @NotNull
    @Column(name = "S_OID")
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
     * The seed itself
     **/
    @Size(max = 1024)
    @Column(name = "S_SEED")
    private String seed;

    /**
     * The seed's target
     **/
    @Column(name = "S_TARGET_ID")
    private long targetId;

    /**
     * Sets if the seed is primary or secondary.
     */
    @Column(name = "S_PRIMARY")
    private boolean primary;

    /**
     * Protected constructor to prevent instantiation by non-application
     * components. This class should be instantiated by the
     * BusinessObjectFactory.
     */
    protected SimpleSeed() {
    }


    /**
     * Returns the database OID of the seed.
     *
     * @return Returns the oid.
     */
    public Long getOid() {
        return oid;
    }

    /**
     * Set the OID of the seed.
     *
     * @param anOid The OID.
     */
    public void setOid(Long anOid) {
        this.oid = anOid;
    }

    /**
     * Gets the seed URL.
     *
     * @return Returns the seed.
     */
    public String getSeed() {
        return seed;
    }


    /**
     * Sets the Seed URL.
     *
     * @param seed The seed to set.
     */
    public void setSeed(String seed) {
        this.seed = seed;
    }

    public long getTargetId() {
        return targetId;
    }

    public void setTargetId(long targetId) {
        this.targetId = targetId;
    }


    /**
     * Checks if the seed is defined as a primary seed.
     *
     * @return true if primary; otherwise false.
     */
    public boolean isPrimary() {
        return primary;
    }


    /**
     * Sets whether this seed should be primary.
     *
     * @param primary true to set as primary; otherwise false.
     */
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}
