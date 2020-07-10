package org.webcurator.core.visualization.modification.metadata;

import org.webcurator.core.visualization.VisualizationAbstractApplyCommand;

import java.util.ArrayList;
import java.util.List;

public class ModifyApplyCommand extends VisualizationAbstractApplyCommand {

    private String provenanceNote = null;
    private List<ModifyRowMetadata> dataset = new ArrayList<>();


    public String getProvenanceNote() {
        return provenanceNote;
    }

    public void setProvenanceNote(String provenanceNote) {
        this.provenanceNote = provenanceNote;
    }

    public List<ModifyRowMetadata> getDataset() {
        return dataset;
    }

    public void setDataset(List<ModifyRowMetadata> dataset) {
        this.dataset = dataset;
    }
}
