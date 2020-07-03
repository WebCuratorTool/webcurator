package org.webcurator.core.visualization.modification.metadata;

import org.webcurator.core.visualization.VisualizationAbstractCommandApply;

import java.util.ArrayList;
import java.util.List;

public class PruneAndImportCommandApply extends VisualizationAbstractCommandApply {

    private String provenanceNote = null;
    private List<PruneAndImportCommandRowMetadata> dataset = new ArrayList<>();


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
