package org.webcurator.core.networkmap.service;

import java.util.List;

public class PruneAndImportCommandApply {
    private long targetInstanceId;
    private long harvestResultId;
    private int harvestResultNumber;
    private int newHarvestResultNumber;
    private String provenanceNote;
    private List<PruneAndImportCommandRowMetadata> dataset;

    public long getTargetInstanceId() {
        return targetInstanceId;
    }

    public void setTargetInstanceId(long targetInstanceId) {
        this.targetInstanceId = targetInstanceId;
    }

    public long getHarvestResultId() {
        return harvestResultId;
    }

    public void setHarvestResultId(long harvestResultId) {
        this.harvestResultId = harvestResultId;
    }

    public int getHarvestResultNumber() {
        return harvestResultNumber;
    }

    public void setHarvestResultNumber(int harvestResultNumber) {
        this.harvestResultNumber = harvestResultNumber;
    }

    public int getNewHarvestResultNumber() {
        return newHarvestResultNumber;
    }

    public void setNewHarvestResultNumber(int newHarvestResultNumber) {
        this.newHarvestResultNumber = newHarvestResultNumber;
    }

    public String getProvenanceNote() {
        return provenanceNote;
    }

    public void setProvenanceNote(String provenanceNote) {
        this.provenanceNote = provenanceNote;
    }

    public List<PruneAndImportCommandRowMetadata> getDataset() {
        return dataset;
    }

    public void setDataset(List<PruneAndImportCommandRowMetadata> dataset) {
        this.dataset = dataset;
    }
}
