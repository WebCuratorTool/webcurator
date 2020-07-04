package org.webcurator.core.visualization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class VisualizationProgressView {
    private String stage;
    private long targetInstanceId;
    private int harvestResultNumber;
    private int progressPercentage;

    public VisualizationProgressView() {
    }

    @JsonIgnore
    public VisualizationProgressView(VisualizationProgressBar progressBar) {
        if (progressBar != null) {
            this.stage = progressBar.getStage();
            this.targetInstanceId = progressBar.getTargetInstanceId();
            this.harvestResultNumber = progressBar.getHarvestResultNumber();
            this.progressPercentage = progressBar.getProgressPercentage();
        }
    }

    @JsonIgnore
    public static VisualizationProgressView getInstance(String json) {
        if (json == null) {
            return new VisualizationProgressView();
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, VisualizationProgressView.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new VisualizationProgressView();
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public long getTargetInstanceId() {
        return targetInstanceId;
    }

    public void setTargetInstanceId(long targetInstanceId) {
        this.targetInstanceId = targetInstanceId;
    }

    public int getHarvestResultNumber() {
        return harvestResultNumber;
    }

    public void setHarvestResultNumber(int harvestResultNumber) {
        this.harvestResultNumber = harvestResultNumber;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
}
