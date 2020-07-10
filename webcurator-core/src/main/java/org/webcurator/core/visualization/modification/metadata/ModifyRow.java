package org.webcurator.core.visualization.modification.metadata;

public class ModifyRow {
    private String content;
    private ModifyRowMetadata metadata;
    private boolean start;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ModifyRowMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ModifyRowMetadata metadata) {
        this.metadata = metadata;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }
}
