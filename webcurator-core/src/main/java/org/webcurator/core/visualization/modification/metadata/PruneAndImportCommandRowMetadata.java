package org.webcurator.core.visualization.modification.metadata;

public class PruneAndImportCommandRowMetadata {
    private String url;
    private String option;
    private String name;
    private long length;
    private String contentType;
    private String modifiedMode;
    private long lastModified;
    private String content;
    private boolean replaceFlag;
    private String tempFileName;
    private int respCode;
    private String respMsg;

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

    public String getModifiedMode() {
        return modifiedMode;
    }

    public void setModifiedMode(String modifiedMode) {
        this.modifiedMode = modifiedMode;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
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

    public String getTempFileName() {
        return tempFileName;
    }

    public void setTempFileName(String tempFileName) {
        this.tempFileName = tempFileName;
    }

    public int getRespCode() {
        return respCode;
    }

    public void setRespCode(int respCode) {
        this.respCode = respCode;
    }

    public String getRespMsg() {
        return respMsg;
    }

    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg;
    }
}
