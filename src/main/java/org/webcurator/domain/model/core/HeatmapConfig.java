/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.webcurator.domain.model.core;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * @author mclean
 */
// lazy="false"
@Entity
@Table(name = "HEATMAP_CONFIG")
@NamedQueries({
		@NamedQuery(name = "org.webcurator.domain.model.core.HeatmapConfig.all",
				query = "from org.webcurator.domain.model.core.HeatmapConfig hm order by hm.thresholdLowest"),
		@NamedQuery(name = "org.webcurator.domain.model.core.HeatmapConfig.getConfigByOid",
				query = "SELECT hm FROM HeatmapConfig hm WHERE hm_oid=?")
})
public class HeatmapConfig {
	public static final String QUERY_ALL = "org.webcurator.domain.model.core.HeatmapConfig.all";
	/** name if the Day and Time bandwidth restrictions query. */
	public static final String PARAM_START = "start";
	/**
	 * name if the Day and Time bandwidth restrictions query parameter end time.
	 */
	public static final String PARAM_END = "end";

    public static final String QRY_GET_CONFIG_BY_OID = "org.webcurator.domain.model.core.HeatmapConfig.getConfigByOid";

	/** The primary key. */
	@Id
	@Column(name="HM_OID", nullable =  false)
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
	@Column(name = "HM_NAME", nullable = false)
	private String name;
	@Column(name = "HM_DISPLAY_NAME", nullable = false)
	private String displayName;
	/** Hexadecimal string representing the RGB color, e.g. 8FBC8F **/
	@Column(name = "HM_COLOR", nullable = false)
	private String color;
	/**
	 * lowest value to display this color. The upper limit is set by other
	 * configurations, if any e.g. low=1, medium=7, high=12 - low=1 to 6,
	 * medium=7 to 11, high=above 12. 0 in this example will be uncolored
	 **/
	@Column(name = "HM_THRESHOLD_LOWEST", nullable = false)
	private int thresholdLowest;

	/**
	 * Get the OID of the object.
	 * 
	 * @return Returns the oid.
	 */
	public Long getOid() {
		return oid;
	}

	public void setOid(Long oid) {
		this.oid = oid;
	}

	/**
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 */
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	/**
	 */
	public int getThresholdLowest() {
		return thresholdLowest;
	}

	public void setThresholdLowest(int thresholdLowest) {
		this.thresholdLowest = thresholdLowest;
	}

	/**
	 */
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

}
