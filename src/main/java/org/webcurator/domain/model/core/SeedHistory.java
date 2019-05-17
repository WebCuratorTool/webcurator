package org.webcurator.domain.model.core;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Defines a seed that was historically used by the Target Instance. Similar
 * to the 'originalSeeds' collection but not constrained by implementation
 * to the use of a single string. This is currently 'write only' for use externally 
 * to the application
 * 
 * @author kurwin
 */
// lazy="false"
@Entity
@Table(name = "SEED_HISTORY")
public class SeedHistory extends AbstractIdentityObject {
	/** The unique ID of the seed **/
	@Id
	@Column(name="SH_OID", nullable =  false)
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
	/** The seed itself **/
	@Column(name = "SH_SEED", length = 1024)
	private String seed;
	/** The seed's target instance**/
	@Column(name = "SH_TI_OID")
	private Long targetInstanceOid;
	/** Sets if the seed is primary or secondary. */
	@Column(name = "SH_PRIMARY")
	private boolean primary; 
	
	/**
	 * Don't allow empty instantiation of this object as it would
	 * break DB NOT NULL constraints when saved
	 */
	private SeedHistory()
	{
	}
	
	/**
	 * Create the history from a real Seed (use BusinessObjectFactory)
	 */
	protected SeedHistory(TargetInstance aTargetInstance, Seed seed)
	{
		this.seed = seed.getSeed();
		this.primary = seed.isPrimary();
		this.targetInstanceOid = aTargetInstance.getOid();
	}
	
    /**
     * Returns the database OID of the seed.
     * @return Returns the oid.
     */
    public Long getOid() {
        return oid;
    }	
	
    /**
     * Set the OID of the seed.
     * @param anOid The OID.
     */
    public void setOid(Long anOid) {
    	this.oid = anOid;
    }
	
	/**
	 * Gets the seed URL.
	 * @return Returns the seed.
	 */
	public String getSeed() {
		return seed;
	}

	/**
	 * Sets the Seed URL.
	 * @param seed The seed to set.
	 */
	public void setSeed(String seed) {
		this.seed = seed;
	}
	
	/**
	 * Get the Target Instance to which this seed belongs.
	 * @return Returns the Target Instance Oid.
	 */
	public Long getTargetInstanceOid() {
		return targetInstanceOid;
	}
	
	/**
	 * Set the Target Instance to which this seed belongs.
	 * @param aTargetInstanceOid The target instance oid to set.
	 */
	public void setTargetInstanceOid(Long aTargetInstanceOid) {
		this.targetInstanceOid = aTargetInstanceOid;
	}

	/**
	 * Checks if the seed is defined as a primary seed.
	 * @return true if primary; otherwise false.
	 */
	public boolean isPrimary() {
		return primary;
	}


	/**
	 * Sets whether this seed should be primary.
	 * @param primary true to set as primary; otherwise false.
	 */
	public void setPrimary(boolean primary) {
		this.primary = primary;
	}
}
