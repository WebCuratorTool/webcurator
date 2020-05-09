package org.webcurator.core.networkmap.service;

import java.util.List;

public class PruneAndImportCommandApply {
    private String provenanceNote;
    private List<PruneAndImportCommandRowMetadata> dataset;

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
