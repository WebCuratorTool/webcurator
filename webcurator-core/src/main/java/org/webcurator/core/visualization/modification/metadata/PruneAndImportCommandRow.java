package org.webcurator.core.visualization.modification.metadata;

public class PruneAndImportCommandRow {
    private byte[] content;
    private PruneAndImportCommandRowMetadata metadata;

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public PruneAndImportCommandRowMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(PruneAndImportCommandRowMetadata metadata) {
        this.metadata = metadata;
    }
}
