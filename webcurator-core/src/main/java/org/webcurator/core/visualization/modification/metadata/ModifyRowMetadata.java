package org.webcurator.core.visualization.modification.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ModifyRowMetadata {
    protected static final SimpleDateFormat writerDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private String url;
    private String option;
    private String name;
    private long length;
    private String contentType;
    private String modifiedMode;
    private long lastModified;
    private String lastModifiedPresentationString = "";
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

    public String getLastModifiedPresentationString() {
        if (modifiedMode == null) {
            return null;
        }
        if (modifiedMode.equalsIgnoreCase("FILE") || modifiedMode.equalsIgnoreCase("CUSTOM")) {
            Date warcDate = new Date();
            warcDate.setTime(lastModified);
            lastModifiedPresentationString = writerDF.format(warcDate);
        } else {
            lastModifiedPresentationString = modifiedMode;
        }
        return lastModifiedPresentationString;
    }

    public void setLastModifiedPresentationString(String lastModifiedPresentationString) {
        this.lastModifiedPresentationString = lastModifiedPresentationString;
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

    @JsonIgnore
    public static ModifyRowMetadata getInstance(String json) {
        if (json == null) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, ModifyRowMetadata.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
