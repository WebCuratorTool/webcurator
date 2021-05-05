package org.webcurator.core.visualization.modification.metadata;

import org.webcurator.core.visualization.VisualizationAbstractApplyCommand;

import java.util.ArrayList;
import java.util.List;

public class ModifyApplyCommand extends VisualizationAbstractApplyCommand {
    public static final String OPTION_PRUNE = "prune";
    public static final String OPTION_RECRAWL = "recrawl";
    public static final String OPTION_FILE = "file";
    public static final int REPLACE_OPTION_STATUS_ALL = 1;
    public static final int REPLACE_OPTION_STATUS_FAILED = 2;
    public static final int REPLACE_OPTION_STATUS_NONE = 3;
    public static final int REPLACE_OPTION_OUTLINK_ALL = 1;
    public static final int REPLACE_OPTION_OUTLINK_FAILED = 2;
    public static final int REPLACE_OPTION_OUTLINK_NONE = 3;

    private int replaceOptionStatus = REPLACE_OPTION_STATUS_ALL;
    private int replaceOptionOutlink = REPLACE_OPTION_OUTLINK_ALL;
    private String provenanceNote = null;
    private List<ModifyRowFullData> dataset = new ArrayList<>();

    public int getReplaceOptionStatus() {
        return replaceOptionStatus;
    }

    public void setReplaceOptionStatus(int replaceOptionStatus) {
        this.replaceOptionStatus = replaceOptionStatus;
    }

    public int getReplaceOptionOutlink() {
        return replaceOptionOutlink;
    }

    public void setReplaceOptionOutlink(int replaceOptionOutlink) {
        this.replaceOptionOutlink = replaceOptionOutlink;
    }

    public String getProvenanceNote() {
        return provenanceNote;
    }

    public void setProvenanceNote(String provenanceNote) {
        this.provenanceNote = provenanceNote;
    }

    public List<ModifyRowFullData> getDataset() {
        return dataset;
    }

    public void setDataset(List<ModifyRowFullData> dataset) {
        this.dataset = dataset;
    }
}
