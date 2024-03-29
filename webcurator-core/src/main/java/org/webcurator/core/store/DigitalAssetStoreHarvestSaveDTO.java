package org.webcurator.core.store;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DigitalAssetStoreHarvestSaveDTO {
    private String fileUploadMode;
    private String targetInstanceName;
    private String directory;
    private String filePath;
    private String harvestBaseUrl;

    public String getFileUploadMode() {
        return fileUploadMode;
    }

    public void setFileUploadMode(String fileUploadMode) {
        this.fileUploadMode = fileUploadMode;
    }

    public String getTargetInstanceName() {
        return targetInstanceName;
    }

    public void setTargetInstanceName(String targetInstanceName) {
        this.targetInstanceName = targetInstanceName;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getHarvestBaseUrl() {
        return harvestBaseUrl;
    }

    public void setHarvestBaseUrl(String harvestBaseUrl) {
        this.harvestBaseUrl = harvestBaseUrl;
    }

    @Override
    @JsonIgnore
    public String toString() {
        return String.format("fileUploadMode=%s, targetInstanceName=%s, directory=%s, filePath=%s, harvestBaseUrl=%s", fileUploadMode, targetInstanceName, directory, filePath, harvestBaseUrl);
    }
}
