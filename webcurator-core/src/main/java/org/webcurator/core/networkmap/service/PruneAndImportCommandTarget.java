package org.webcurator.core.networkmap.service;

public class PruneAndImportCommandTarget {
    private byte[] content;
    private PruneAndImportCommandTargetMetadata metadata;

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public PruneAndImportCommandTargetMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(PruneAndImportCommandTargetMetadata metadata) {
        this.metadata = metadata;
    }
}
