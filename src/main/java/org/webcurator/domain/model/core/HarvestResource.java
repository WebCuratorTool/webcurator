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
 * A HarvestResource is a resource that has been harvested. It may be 
 * subclassed to provide additional functionality such as that required to
 * support resources within ARC files. 
 * 
 * All Quality Review tools should ideally work with HarvestResource objects 
 * rather than their subclasses, though this may not always be possible.
 * 
 **/
// lazy="false"
@Inheritance(strategy = InheritanceType.JOINED)
@Entity
@Table(name = "HARVEST_RESOURCE")
public class HarvestResource {
	/** The name of the resource */
	@Column(name = "HRC_NAME", length = 1020, nullable = false)
	protected String name;
	/** The length of the resource */
	@Column(name = "HRC_LENGTH")
	protected long length;
	/** The status code of the resource */
	@Column(name = "HRC_STATUS_CODE", nullable = false)
	protected int statusCode;
	/** The database OID of the object */
	@Id
	@Column(name="HRC_OID", nullable =  false)
	// Note: From the Hibernate 4.2 documentation:
	// The Hibernate team has always felt such a construct as fundamentally wrong.
	// Try hard to fix your data model before using this feature.
	@TableGenerator(name = "SharedTableIdGenerator",
			table = "ID_GENERATOR",
			pkColumnName = "IG_TYPE",
			valueColumnName = "IG_VALUE",
			pkColumnValue = "HarvestResource",
			allocationSize = 1) // 50 is the default
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "SharedTableIdGenerator")
	protected Long oid;
	/** The HarvestResult that this resource belongs to */
	@ManyToOne
	@JoinColumn(name = "HRC_HARVEST_RESULT_OID")
	protected HarvestResult result;

	
	/**
	 * No-arg constructor.
	 */
    public HarvestResource() {
        super();
    }
    
    /**
     * Construct a HarvestResource from its DTO.
     * @param aResource The DTO to create the HarvestResource object from.
     * @param aResult   The HarvestResult that this resource belongs to.
     */
    public HarvestResource(HarvestResourceDTO aResource, HarvestResult aResult) {
        super();
        name = aResource.getName();
        length = aResource.getLength();
        statusCode = aResource.getStatusCode();
        result = aResult;
    }
    
	/**
	 * Get the primary key of the HarvestResource.
	 * @return the primary key
	 */
	public Long getOid() {
		return oid;
	}
	
	/**
	 * Set the primary key of the harvest resource.
	 * @param oid The OID of the resource.
	 */
	public void setOid(Long oid) {
		this.oid = oid;
	}
	
	/**
	 * Get the length of the resource in bytes.
	 * @return The length of the resource in bytes.
	 */
	public long getLength() {
		return length;
	}
	
	/**
	 * Set the length of the resource in bytes.
	 * @param length the length of the resource in bytes.
	 */
	public void setLength(long length) {
		this.length = length;
	}
	
	/**
	 * Get the name of the resource.
	 * 
	 * NB: The length of this property should be maintained both in the length
	 * Hibernate attribute, but also in ArcHarvestFileDTO.MAX_URL_LENGTH.
	 * 
	 * @return The name of the resource.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the name of the resource.
	 * @param name The name of the resource.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the harvest result that this resource belongs to.
	 * @return The HarvestResult that this resource belongs to.
	 */
	public HarvestResult getResult() {
		return result;
	}
	
	/**
	 * Set the harvest result that this resource belongs to.
	 * @param result The HarvesetResult that this resource belongs to.
	 */
	public void setResult(HarvestResult result) {
		this.result = result;
	}
	
	/**
	 * @return the statusCode
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * @param statusCode the statusCode to set
	 */
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}	
	
	/**
	 * Build a DTO from this object.
	 * @return A new DTO created from the data in this object.
	 */
	public HarvestResourceDTO buildDTO() {
		HarvestResourceDTO dto = new HarvestResourceDTO();
		dto.setLength(this.getLength());
		dto.setName(this.getName());
		dto.setOid(this.getOid());
		dto.setStatusCode(this.getStatusCode());
		dto.setTargetInstanceOid(this.result.targetInstance.getOid());
		return dto;
	}


	
}
