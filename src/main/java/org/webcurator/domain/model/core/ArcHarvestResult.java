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

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The <code>HarvestResult</code> class describes the result of a harvest. It
 * contains and manages the resources. 
 * 
 **/
@SuppressWarnings("all")
@Entity
@Table(name = "ARC_HARVEST_RESULT")
@PrimaryKeyJoinColumn(name = "AHRS_HARVEST_RESULT_OID")
public class ArcHarvestResult extends HarvestResult {
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE}) // default fetch type is LAZY
	@JoinColumn(name = "AHF_ARC_HARVEST_RESULT_ID")
	private Set<ArcHarvestFile> arcFiles = new HashSet<ArcHarvestFile>();

	/**
	 * Construct a new HarvestResult.
	 */
	public ArcHarvestResult() {
        super();
	}

	public ArcHarvestResult(TargetInstance aTargetInstance, int harvestNumber) {
		super(aTargetInstance, harvestNumber);
	}

	/**
	 * Create an HarvestResult from its DTO.
	 * @param aResultDTO The DTO.
	 * @param aTargetInstance The TargetInstance that this HarvestResult
	 * 						  belongs to.
	 */
	public ArcHarvestResult(HarvestResultDTO aResultDTO, TargetInstance aTargetInstance) {
		super(aResultDTO, aTargetInstance);

		aResultDTO.getResources().forEach((key,value)->{
			ArcHarvestResourceDTO harvestResourceDTO = (ArcHarvestResourceDTO)value;
			ArcHarvestResource harvestResource = new ArcHarvestResource(harvestResourceDTO, this);
			this.getResources().put(key, harvestResource);
		});

		if(aResultDTO.getArcFiles() != null) {
			aResultDTO.getArcFiles().forEach(value->{
				ArcHarvestFile file = new ArcHarvestFile(value, this);
				this.arcFiles.add(file);
			});
		}
	}

	/**
	 * Get the set of ARC files that make up this HarvestResult.
	 * @return the set of ARC files that make up this HarvestResult
	 */
	public Set<ArcHarvestFile> getArcFiles() {
		return arcFiles;
	}

	/**
	 * Set the set of ARC files that make up this HarvestResult.
	 * @param arcFiles The set of ARC Harvest Files.
	 */
	public void setArcFiles(Set<ArcHarvestFile> arcFiles) {
		this.arcFiles = arcFiles;
	}

	/**
	 * Create an index for this HarvestResult, assuming that all ARC Files
	 * are in the basedir provided.
	 * @param baseDir The base directory for the ARC files.
	 * @throws IOException if the indexing fails.
	 */
	public void index(File baseDir) throws IOException {
		for(ArcHarvestFile ahf: arcFiles) {
			this.getResources().putAll(ahf.index(baseDir));
		}
	}

	/**
	 * Create an index for this HarvestResult, assuming that each ARC File is
	 * in the base directory that it states in the ArcHarvestFile object.
	 * @throws IOException if the indexing fails.
	 */
	public void index() throws IOException {
		for(ArcHarvestFile ahf: arcFiles) {
			this.getResources().putAll(ahf.index());
		}
	}
}
