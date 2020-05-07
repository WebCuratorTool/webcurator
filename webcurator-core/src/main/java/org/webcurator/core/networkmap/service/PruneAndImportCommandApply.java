package org.webcurator.core.networkmap.service;

import java.util.List;

public class PruneAndImportCommandApply {
    private String provenanceNote;
    private List<PruneAndImportCommandTargetMetadata> dataset;

    public String getProvenanceNote() {
        return provenanceNote;
    }

    public void setProvenanceNote(String provenanceNote) {
        this.provenanceNote = provenanceNote;
    }

    public List<PruneAndImportCommandTargetMetadata> getDataset() {
        return dataset;
    }

    public void setDataset(List<PruneAndImportCommandTargetMetadata> dataset) {
        this.dataset = dataset;
    }
}
