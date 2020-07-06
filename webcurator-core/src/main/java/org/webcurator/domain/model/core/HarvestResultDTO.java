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


import org.webcurator.core.util.PatchUtil;
import org.webcurator.core.visualization.VisualizationProgressView;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;

import java.util.Date;

/**
 * The Object for transfering Harvest Results between the web curator components.
 *
 * @author bbeaumont
 */
public class HarvestResultDTO {
    /**
     * The unique ID of the HarvestResult
     */
    protected Long oid;
    /**
     * the id of the target instance that this result belongs to.
     */
    protected Long targetInstanceOid;
    /**
     * the date the result was created.
     */
    protected Date creationDate;
    /**
     * the number of the harvest.
     */
    protected int harvestNumber = 1;
    /**
     * The harvests provenance note.
     */
    protected String provenanceNote;

    protected String createdByFullName;
    protected int derivedFrom;
    protected int state = 0;
    protected int status = 0;
    protected int currentProgressPercentage = 0;

    public HarvestResultDTO() {
    }

    /**
     * Create a HarvestResultDTO from the HarvestResult, excluding the resources.
     *
     * @param hrOid             The HarvestResult to base the DTO on.
     * @param targetInstanceOid targetInstanceOid
     * @param creationDate      creationDate
     * @param harvestNumber     harvestNumber
     * @param provenanceNote    provenanceNote
     */
    public HarvestResultDTO(Long hrOid, Long targetInstanceOid, Date creationDate, int harvestNumber, String provenanceNote) {
        this.oid = hrOid;
        this.targetInstanceOid = targetInstanceOid;
        this.creationDate = creationDate;
        this.harvestNumber = harvestNumber;
        this.provenanceNote = provenanceNote;
    }


    /**
     * @return Returns the targetInstanceOid.
     */
    public Long getTargetInstanceOid() {
        return targetInstanceOid;
    }

    /**
     * @param targetInstanceOid The targetInstanceOid to set.
     */
    public void setTargetInstanceOid(Long targetInstanceOid) {
        this.targetInstanceOid = targetInstanceOid;
    }

    /**
     * @return Returns the creationDate.
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate The creationDate to set.
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return Returns the harvestNumber.
     */
    public int getHarvestNumber() {
        return harvestNumber;
    }

    /**
     * @param harvestNumber The harvestNumber to set.
     */
    public void setHarvestNumber(int harvestNumber) {
        this.harvestNumber = harvestNumber;
    }

    /**
     * @return Returns the provenanceNote.
     */
    public String getProvenanceNote() {
        return provenanceNote;
    }

    /**
     * @param provenanceNote The provenanceNote to set.
     */
    public void setProvenanceNote(String provenanceNote) {
        this.provenanceNote = provenanceNote;
    }

    /**
     * @return the oid
     */
    public Long getOid() {
        return oid;
    }

    /**
     * @param oid the oid to set
     */
    public void setOid(Long oid) {
        this.oid = oid;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getKey() {
        return PatchUtil.getPatchJobName(this.targetInstanceOid, this.harvestNumber);
    }

    public int getCurrentProgressPercentage() {
        return currentProgressPercentage;
    }

    public void setCurrentProgressPercentage(int currentProgressPercentage) {
        this.currentProgressPercentage = currentProgressPercentage;
    }

    public String getCreatedByFullName() {
        return createdByFullName;
    }

    public void setCreatedByFullName(String createdByFullName) {
        this.createdByFullName = createdByFullName;
    }

    public int getDerivedFrom() {
        return derivedFrom;
    }

    public void setDerivedFrom(int derivedFrom) {
        this.derivedFrom = derivedFrom;
    }

    public int getCrawlingProgressPercentage(NetworkMapClient networkMapClient) {
        if (state == HarvestResult.STATE_CRAWLING) {
            return getProgressPercentage();
        }
        return 100;
    }

    public int getModifyingProgressPercentage(NetworkMapClient networkMapClient) {
        if (state == HarvestResult.STATE_CRAWLING) {
            return 0; //Not started
        } else if (state != HarvestResult.STATE_MODIFYING) {
            return 100; //Finished
        }

        NetworkMapResult progressBar = networkMapClient.getProgress(targetInstanceOid, harvestNumber);
        if (progressBar.getRspCode() == NetworkMapResult.RSP_ERROR_DATA_NOT_EXIST) {
            return getProgressPercentage();
        } else {
            return VisualizationProgressView.getInstance(progressBar.getPayload()).getProgressPercentage();
        }
    }

    public int getIndexingProgressPercentage(NetworkMapClient networkMapClient) {
        if (state == HarvestResult.STATE_CRAWLING || state == HarvestResult.STATE_MODIFYING) {
            return 0; //Not started
        } else if (state != HarvestResult.STATE_INDEXING) {
            return 100; //Finished
        }

        NetworkMapResult progressBar = networkMapClient.getProgress(targetInstanceOid, harvestNumber);
        if (progressBar.getRspCode() == NetworkMapResult.RSP_ERROR_DATA_NOT_EXIST) {
            return getProgressPercentage();
        } else {
            return VisualizationProgressView.getInstance(progressBar.getPayload()).getProgressPercentage();
        }
    }

    private int getProgressPercentage() {
        if (status == HarvestResult.STATUS_SCHEDULED || status == HarvestResult.STATUS_TERMINATED) {
            return 0;
        } else if (status == HarvestResult.STATUS_FINISHED) {
            return 100;
        } else {
            return 50;
        }
    }
}