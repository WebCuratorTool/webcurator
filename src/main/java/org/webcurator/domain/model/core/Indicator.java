package org.webcurator.domain.model.core;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.annotations.GenericGenerator;
import org.webcurator.core.harvester.agent.HarvesterStatusUtil;
import org.webcurator.core.util.ConverterUtil;
import org.webcurator.domain.model.auth.Agency;

import javax.persistence.*;

/**
 * An <code>Indicator</code> represents a metric used to measure the
 * quality of a harvest and is associated with a specific <code>TargetInstance</code>
 * 
 * @author twoods
 *
*/
// lazy="true"
@Entity
@Table(name = "INDICATOR")
@NamedQueries({
		@NamedQuery(name = "org.webcurator.domain.model.core.Indicator.getIndicators",
				query = "SELECT i FROM Indicator i ORDER BY i_agc_oid, i.name"),
		@NamedQuery(name = "org.webcurator.domain.model.core.Indicator.getIndicatorsByTargetInstance",
				query = "SELECT i FROM Indicator i WHERE i_ti_oid=?1 ORDER BY i.name"),
		@NamedQuery(name = "org.webcurator.domain.model.core.Indicator.getIndicatorByOid",
				query = "SELECT i FROM Indicator i WHERE i_oid=?1")
})
public class Indicator {
		
	/** Query key for retrieving all reason objects */
    public static final String QRY_GET_INDICATORS = "org.webcurator.domain.model.core.Indicator.getIndicators";
	/** Query key for retrieving reason objects by agency OID */
    public static final String QRY_GET_INDICATORS_BY_TI_OID = "org.webcurator.domain.model.core.Indicator.getIndicatorsByTargetInstance";
	/** Query key for retrieving a reason objects by oid*/
    public static final String QRY_GET_INDICATOR_BY_OID = "org.webcurator.domain.model.core.Indicator.getIndicatorByOid";

	/** unique identifier **/
	@Id
	@Column(name="I_OID", nullable =  false)
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
	
	/** The <code>Indicator</code> on which this <code>Indicator</code> is based.**/
	@ManyToOne
	@JoinColumn(name = "I_IC_OID", foreignKey = @ForeignKey(name = "FK_I_IC_OID"), nullable = false)
	private IndicatorCriteria indicatorCriteria;
	
	/** The <code>TargetInstance</code> oid associated with this <code>Indicator</code> */
	@Column(name = "I_TI_OID")
	private Long targetInstanceOid;
	
	/** The name of the <code>Indicator</code> that will be displayed **/
	@Column(name = "I_NAME", nullable = false)
	private String name;
	
	/** The floating point value calculated for this <code>Indicator</code> by the rules engine **/
	@Column(name = "I_FLOAT_VALUE", nullable = false)
	private Float floatValue;
	
	/** The upper limit set for this <code>Indicator</code> as a percentage (eg: +10%) **/
	@Column(name = "I_UPPER_LIMIT_PERCENTAGE")
	private Float upperLimitPercentage;

	/** The lower limit set for this <code>Indicator</code> as a percentage (eg: -10%) **/
	@Column(name = "I_LOWER_LIMIT_PERCENTAGE")
	private Float lowerLimitPercentage;
	
	/** The upper limit set for this <code>Indicator</code> as a floating point number (some <code>Indicators</code> do not have associated percentage limits) **/
	@Column(name = "I_UPPER_LIMIT")
	private Float upperLimit;

	/** The lower limit set for this <code>Indicator</code> as a floating point number (some <code>Indicators</code> do not have associated percentage limits) **/
	@Column(name = "I_LOWER_LIMIT")
	private Float lowerLimit;
	
	/** The advice issued for this <code>Indicator</code>
	 * based on the supporting facts established by the rules engine.
	**/
	@Column(name = "I_ADVICE")
	private String advice = null;
	
	/**
	 * The unit of measurement used for the <code>Indicator</code>.
	 */
	@Column(name = "I_UNIT", nullable = false)
	private String unit = null;
	
	/**
	 * Display the delta between the reference crawl and the <code>TargetInstance</code> in the UI
	 */
	@Column(name = "I_SHOW_DELTA", nullable = false)
	private Boolean showDelta = false;
	
	/**
	 * The advice justification for this <code>Indicator</code>
	 * based on the supporting facts established by the rules engine.
	 */
	@Column(name = "I_JUSTIFICATION")
	private String justification = null;

    /** The agency the <code>Indicator</code> belongs to */
    @ManyToOne
    @JoinColumn(name = "I_AGC_OID", foreignKey = @ForeignKey(name = "FK_I_AGENCY_OID"), nullable = false)
    private Agency agency;
    
    /** The date and time on which this <code>Indicator</code> was created or updated **/
	@Column(name = "I_DATE", columnDefinition = "TIMESTAMP(9)", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
    private Date dateTime = null;
    
    /** The report lines that are generated for this <code>Indicator</code>s report **/
	@OneToMany(orphanRemoval = true, cascade = {CascadeType.ALL}) // default fetch type is LAZY
	@JoinColumn(name = "IRL_I_OID")
	// TODO @hibernate.collection-index column="IRL_INDEX"
	public List<IndicatorReportLine> indicatorReportLines = new LinkedList<IndicatorReportLine>();
    
	/**
	 * Get the database OID of the <code>Indicator</code>.
	 * @return the primary key
	 */
	public Long getOid() {
		return oid;
	}
	
	/**
	 * Set the database oid of the <code>Indicator</code>.
	 * @param oid The new database oid.
	 */
	public void setOid(Long oid) {
		this.oid = oid;
	}
	
    /**
     * Get the <code>IndicatorCriteria</code> that this <code>Indicator</code> is associated with.
     * @return Returns the <code>IndicatorCriteria</code>.
      */
    public IndicatorCriteria getIndicatorCriteria() {
        return indicatorCriteria;
    }
    
    /**
     * Set the <code>IndicatorCriteria</code> for the <code>Indicator</code>.
     * @param indicatorCriteria The <code>IndicatorCriteria</code> to set.
     */
    public void setIndicatorCriteria(IndicatorCriteria indicatorCriteria) {
        this.indicatorCriteria = indicatorCriteria;
    } 

	
    /**
     * Gets the name of the <code>Indicator</code>.
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the <code>TargetInstance</code> oid that this <code>Indicator</code> is associated with.
     * @return Returns the <code>TargetInstance</code>.
     */
    public Long getTargetInstanceOid() {
        return targetInstanceOid;
    }
    
    /**
     * Set the <code>TargetInstance</code> oid for the <code>Indicator</code>.
     * @param targetInstanceOid The <code>TargetInstance</code> oid to set.
     */
    public void setTargetInstanceOid(Long targetInstanceOid) {
        this.targetInstanceOid = targetInstanceOid;
    } 
    
    /**
     * Sets the name of the <code>Indicator</code>.
     * @param name The new name for the <code>Indicator</code>.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the value of the <code>Indicator</code>.
     * @return Returns the floating point value.
     */
    public Float getFloatValue() {
        return floatValue;
    }

    /**
     * Sets the value of the <code>Indicator</code>.
     * @param name The new value for the <code>Indicator</code>.
     */
    public void setFloatValue(Float floatValue) {
        this.floatValue = floatValue;
    }
    
    /**
     * Gets the upper limit percentage value of the <code>Indicator</code>.
     * @return Returns the upper limit percentage value.
     */
    public Float getUpperLimitPercentage() {
        return upperLimitPercentage;
    }

    /**
     * Sets the upper limit percentage value of the <code>Indicator</code>.
     * @param name The new upper limit percentage value for the <code>Indicator</code>.
     */
    public void setUpperLimitPercentage(Float floatValue) {
        this.upperLimitPercentage = floatValue;
    }
    
    /**
     * Gets the lower limit percentage value of the <code>Indicator</code>.
     * @return Returns the lower limit percentage value.
     */
    public Float getLowerLimitPercentage() {
        return lowerLimitPercentage;
    }

    /**
     * Sets the lower limit percentage value of the <code>Indicator</code>.
     * @param name The new lower limit percentage value for the <code>Indicator</code>.
     */
    public void setLowerLimitPercentage(Float floatValue) {
        this.lowerLimitPercentage = floatValue;
    }
    
    /**
     * Gets the upper limit value of the <code>Indicator</code>.
     * @return Returns the upper limit value.
     */
    public Float getUpperLimit() {
        return upperLimit;
    }
    
    /**
     * Sets the upper limit value of the <code>Indicator</code>.
     * @param name The new upper limit value for the <code>Indicator</code>.
     */
    public void setUpperLimit(Float floatValue) {
        this.upperLimit = floatValue;
    }

    /**
     * Gets the lower limit value of the <code>Indicator</code>.
     * @return Returns the lower limit value.
     */
    public Float getLowerLimit() {
        return lowerLimit;
    }

    /**
     * Sets the lower limit value of the <code>Indicator</code>.
     * @param name The new lower limit value for the <code>Indicator</code>.
     */
    public void setLowerLimit(Float floatValue) {
        this.lowerLimit = floatValue;
    }

	/**
	 * Fetches the advice set by the QA recommendation service
	 * @return the advised action
	 */
	public String getAdvice() {
		return advice;
	}
	
	/**
	 * Used by the QA Recommendation service to set the QA advice
	 * @param advice the advised action
	 */
	public void setAdvice(String advice) {
		this.advice = advice;
	}
	
	/**
	 * Fetches the justification set by the QA recommendation service
	 * @return the rationale for the advice
	 */
	public String getJustification() {
		return justification;
	}
	
	/**
	 * Used by the QA Recommendation service to set the QA justification
	 * @param the advice justification
	 */
	public void setJustification(String justification) {
		this.justification = justification;
	}
	
    /**
     * gets the Agency to which this <code>Indicator</code> belongs. 
     * @return the Agency object
     */
    public Agency getAgency() {
        return agency;
    }

    /**
     * Set the agency which can use this <code>Indicator</code>.
     * @param agency The agency that can use this <code>Indicator</code>.
     */
    public void setAgency(Agency agency) {
        this.agency = agency;
    }
    
    /**
     * fetches the <code>Indicator</code>s unit of measurement
     * @return the unit of measurement (eg: integer, millisecond, byte)
     */
    public String getUnit() {
    	return unit;
    }
    
    /**
     * sets the unit of measurement for this <code>Indicator</code> (eg: integer, millisecond, byte)
     */
    public void setUnit(String unit) {
    	this.unit = unit;
    }
    
    /**
     * fetches the <code>Indicator</code>s delta visibility
     * @return true if the delta for the <code>Indicator</code> should be displayed in the UI, false otherwise
     */
    public Boolean getShowDelta() {
    	return showDelta;
    }
    
    /**
     * sets the delta display visibility for this <code>Indicator</code> 
     */
    public void setShowDelta(Boolean showDelta) {
    	this.showDelta = showDelta;
    }
    
	/**
	 * Get time that the <code>Indicator<code> was generated 
	 * @return Date created
	 */
	public Date getDateTime() {
		return dateTime;
	}
	
	/**
	 * Set the time that the <code>Indicator<code> was generated
	 * @param dateTime
	 */
	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}
    
	/**
	 * Fetch the <code>IndicatorReportLine</code>s for this <code>Indicator</code> 
	 * @return A <code>List</code> of <code>IndicatorReportLine</code>s for the specified <code>Indicator</code>
	 */
	public List<IndicatorReportLine> getIndicatorReportLines() {
		return indicatorReportLines;
	}
	
	/**
	 * Sets the <code>IndicatorReportLine</code>s for the <code>Indicator<code>
	 * @param indicatorReportLines
	 */
	public void setIndicatorReportLines(List<IndicatorReportLine> indicatorReportLines) {		
		this.indicatorReportLines = indicatorReportLines;
	}
	
    /**
     * Fetch the value of the <code>Indicator</code> as a formatted string as dictated by the <code>Indicator</code>s unit value
     * @return the formatted string value of the <code>Indicator</code> (eg: 100GB) 
     */
    public final String getValue() {
    	return getValueOf(floatValue);
    }

    /**
     * Fetch the value of the supplied float value as a formatted string as dictated by the <code>Indicator</code>s unit value
     * @return the formatted string value of the supplied float value (eg: 100GB) 
     */
    public final String getValueOf(Float floatVal) {
    	String output = null;
    	
		Integer integerVal = floatVal.intValue();
		
		if (getUnit().equals("integer")) {
			output = integerVal.toString();
		} else if (unit.equals("millisecond")) {
			output = HarvesterStatusUtil.formatTime(floatVal.longValue());
		} else if (unit.equals("byte")) {
			Long bytes;
			// scale the number of bytes as appropriate
			String[] decimal = floatVal.toString().split("\\.");
			if (decimal.length > 1) {
				bytes = Long.parseLong(decimal[0]);
			} else {
				bytes = Long.parseLong(floatVal.toString());
			}
			output = ConverterUtil.formatBytes(bytes);
		} else {
			output = floatVal.toString();
		}
		return output;
    }
    
    public final String toString() {
    	return "oid=" + getOid()
    			+ ":name=" + getName()
    			+ ":floatValue==" + getFloatValue()
    			+ ":advice=" + getAdvice()
    			+ ":agency=" + getAgency()
    			+ ":lowerLimit=" + getLowerLimit()
    			+ ":upperLimit=" + getUpperLimit()
    			+ ":lowerLimitPercentage=" + getLowerLimitPercentage()
    			+ ":upperLimitPercentage=" + getUpperLimitPercentage()
    			+ ":justification=" + getJustification()
    			+ ":unit=" + getUnit()
    			+ ":targetInstanceOid=" + getTargetInstanceOid()
    			;
    }
    
}
