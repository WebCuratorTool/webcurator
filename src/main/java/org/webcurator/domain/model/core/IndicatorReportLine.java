package org.webcurator.domain.model.core;

import org.hibernate.annotations.GenericGenerator;

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
@Table(name = "INDICATOR_REPORT_LINE")
@NamedQueries({
		@NamedQuery(name = "org.webcurator.domain.model.core.IndicatorReportLine.getIndicatorReportLines",
				query = "SELECT irl FROM IndicatorReportLine irl ORDER BY irl.line"),
		@NamedQuery(name = "org.webcurator.domain.model.core.IndicatorReportLine.getIndicatorReportLinesByIndicator",
				query = "SELECT irl FROM IndicatorReportLine irl WHERE irl_i_oid=?1 ORDER BY irl.line"),
		@NamedQuery(name = "org.webcurator.domain.model.core.IndicatorReportLine.getIndicatorReportLineByOid",
				query = "SELECT irl FROM IndicatorReportLine irl WHERE irl_oid=?1")
})
public class IndicatorReportLine {
		
	/** Query key for retrieving all IndicatorReportLine objects */
    public static final String QRY_GET_INDICATOR_REPORT_LINES = "org.webcurator.domain.model.core.IndicatorReportLine.getIndicatorReportLines";
	/** Query key for retrieving IndicatorReportLine objects by agency OID */
    public static final String QRY_GET_INDICATOR_REPORT_LINES_BY_I_OID = "org.webcurator.domain.model.core.IndicatorReportLine.getIndicatorReportLinesByIndicator";
	/** Query key for retrieving a IndicatorReportLine objects by oid*/
    public static final String QRY_GET_INDICATOR_REPORT_LINE_BY_OID = "org.webcurator.domain.model.core.IndicatorReportLine.getIndicatorReportLineByOid";

	/** unique identifier **/
	@Id
	@Column(name="IRL_OID", nullable =  false)
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
	
	/** The <code>Indicator</code> on which this <code>IndicatorReportLine</code> is based.**/
	@ManyToOne
	@JoinColumn(name = "IRL_I_OID", foreignKey = @ForeignKey(name = "FK_IRL_I_OID"))
	private Indicator indicator = null;
	
	/** The report line (resource name etc) for this <code>IndicatorReportLine</code> **/
	@Column(name = "IRL_LINE")
	private String line = null;
	
	/**
	 * Get the database OID of the <code>IndicatorReportLine</code>.
	 * @return the primary key
	 */
	public Long getOid() {
		return oid;
	}
	
	/**
	 * Set the database oid of the <code>IndicatorReportLine</code>.
	 * @param oid The new database oid.
	 */
	public void setOid(Long oid) {
		this.oid = oid;
	}
	
    /**
     * Gets the <code>Indicator</code> for the associated <code>Indicator</code>.
     * @return Returns the value.
     */
    public Indicator getIndicator() {
        return indicator;
    }

    /**
     * Sets the <code>Indicator</code> oid for the associated <code>Indicator</code>.
     * @param indicatorOid The new <code>Indicator</code>.
     */
    public void setIndicator(Indicator indicator) {
        this.indicator = indicator;
    }
	
    /**
     * Gets the report line of the <code>IndicatorReportLine</code>.
     * @return Returns the line value.
     */
    public String getLine() {
        return line;
    }

    /**
     * Sets the report line of the <code>IndicatorReportLine</code>.  Removes all quote, double quote and comma characters.
     * @param name The new report line for the <code>IndicatorReportLine</code>.
     */
    public void setLine(String line) {
        this.line = line.replaceAll("'", "").replaceAll(",", "").replaceAll("\"", "");
    }

}
