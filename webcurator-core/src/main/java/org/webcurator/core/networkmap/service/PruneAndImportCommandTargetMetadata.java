package org.webcurator.core.networkmap.service;

public class PruneAndImportCommandTargetMetadata {
    private String url;
    private String option;
    private String name;
    private long length;
    private String contentType;
    private long srcLastModified;
    private String content;
    private boolean replaceFlag;
    private int uploadedFlag;
    private String tempFileName;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSrcLastModified() {
        return srcLastModified;
    }

    public void setSrcLastModified(long srcLastModified) {
        this.srcLastModified = srcLastModified;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isReplaceFlag() {
        return replaceFlag;
    }

    public void setReplaceFlag(boolean replaceFlag) {
        this.replaceFlag = replaceFlag;
    }

    public int getUploadedFlag() {
        return uploadedFlag;
    }

    public void setUploadedFlag(int uploadedFlag) {
        this.uploadedFlag = uploadedFlag;
    }

    public String getTempFileName() {
        return tempFileName;
    }

    public void setTempFileName(String tempFileName) {
        this.tempFileName = tempFileName;
    }
}
