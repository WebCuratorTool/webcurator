package org.webcurator.core.visualization.modification.metadata;

public class PruneAndImportCommandRow {
    private String content;
    private PruneAndImportCommandRowMetadata metadata;
    private boolean start;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public PruneAndImportCommandRowMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(PruneAndImportCommandRowMetadata metadata) {
        this.metadata = metadata;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }
}
