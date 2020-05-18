package org.webcurator.core.visualization;

import org.springframework.stereotype.Component;

@Component("visualizationManager")
public class VisualizationManager {
    private String uploadDir;
    private String baseDir;

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }
}
