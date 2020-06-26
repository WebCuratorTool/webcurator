package org.webcurator.ui.target.command;

import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRowMetadata;

import java.util.ArrayList;
import java.util.List;

public class ToBePrunedAndImportedMetadataCommand {
    private static final int MAX_ROWS = 10;
    private List<PruneAndImportCommandRowMetadata> rows = new ArrayList<>();
    private boolean more = false;
    private String action;

    public void addMetadata(PruneAndImportCommandRowMetadata metadata) {
        if (this.rows.size() < MAX_ROWS) {
            this.rows.add(metadata);
        } else {
            this.more = true;
        }
    }

    public List<PruneAndImportCommandRowMetadata> getRows() {
        return rows;
    }

    public void setRows(List<PruneAndImportCommandRowMetadata> rows) {
        this.rows = rows;
    }

    public boolean isMore() {
        return more;
    }

    public void setMore(boolean more) {
        this.more = more;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
