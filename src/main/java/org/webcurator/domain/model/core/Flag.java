package org.webcurator.domain.model.core;

import org.hibernate.annotations.GenericGenerator;
import org.webcurator.domain.model.auth.Agency;

import javax.persistence.*;

/**
 * An <code>Flag</code> represents an arbitrary grouping of <code>TargetInstance</code>s
 * 
 * @author twoods
 *
*/
// lazy="true"
@Entity
@Table(name = "FLAG")
@NamedQueries({
        @NamedQuery(name = "org.webcurator.domain.model.core.Flag.getFlags",
                query = "SELECT f FROM Flag f ORDER BY f_agc_oid, f.name"),
        @NamedQuery(name = "org.webcurator.domain.model.core.Flag.getFlagsByAgency",
                query = "SELECT f FROM Flag f WHERE f.agency.oid=? ORDER BY f.name"),
        @NamedQuery(name = "org.webcurator.domain.model.core.Flag.getFlagByOid",
                query = "SELECT f FROM Flag f WHERE f_oid=?")
})
public class Flag {
		
	/** Query key for retrieving all flag objects */
    public static final String QRY_GET_FLAGS = "org.webcurator.domain.model.core.Flag.getFlags";
	/** Query key for retrieving a flag objects by oid*/
    public static final String QRY_GET_FLAG_BY_OID = "org.webcurator.domain.model.core.Flag.getFlagByOid";
	/** Query key for retrieving reason objects by agency OID */
    public static final String QRY_GET_FLAGS_BY_AGENCY = "org.webcurator.domain.model.core.Flag.getFlagsByAgency";

	/** unique identifier **/
    @Id
    @Column(name="F_OID", nullable =  false)
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
	
	/** The name of the <code>Flag</code> that will be displayed **/
	@Column(name = "F_NAME", nullable = false)
	private String name;
	
	/** The colour components for the flag **/
	@Column(name = "F_RGB", nullable = false)
	private String rgb;
	
	/** The complement colour components for the flag (used for a contrasting font colour) **/
	@Column(name = "F_COMPLEMENT_RGB", nullable = false)
	private String complementRgb;

    /** The agency the <code>Flag</code> belongs to */
    @ManyToOne
    @JoinColumn(name = "F_AGC_OID", foreignKey = @ForeignKey(name = "FK_F_AGENCY_OID"), nullable = false)
    private Agency agency;
	
	/**
	 * Get the database OID of the <code>Flag</code>.
	 * @return the primary key
	 */
	public Long getOid() {
		return oid;
	}
	
	/**
	 * Set the database oid of the <code>Flag</code>.
	 * @param oid The new database oid.
	 */
	public void setOid(Long oid) {
		this.oid = oid;
	}
	
    /**
     * Gets the name of the <code>Flag</code>.
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the <code>Flag</code>.
     * @param name The new name for the <code>Flag</code>.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the value of the colour components for the <code>Flag</code>.
     * @return Returns the floating point value.
     */
    public String getRgb() {
        return rgb;
    }

    /**
     * Sets the value of the colour components for the <code>Flag</code>.
     * @param name The new value for the <code>Flag</code>.
     */
    public void setRgb(String rgb) {
        this.rgb = rgb;
    }
    
    /**
     * Gets the value of the complement colour components for the <code>Flag</code>.
     * @return Returns the floating point value.
     */
    public String getComplementRgb() {
        return complementRgb;
    }

    /**
     * Sets the value of the complement colour components for the <code>Flag</code>.
     * @param name The new value for the <code>Flag</code>.
     */
    public void setComplementRgb(String rgb) {
        this.complementRgb = rgb;
    }
    
    /**
     * gets the Agency to which this <code>Flag</code> belongs. 
     * @return the Agency object
     */
    public Agency getAgency() {
        return agency;
    }

    /**
     * Set the agency which can use this <code>Flag</code>.
     * @param agency The agency that can use this <code>Flag</code>.
     */
    public void setAgency(Agency agency) {
        this.agency = agency;
    }
    
}