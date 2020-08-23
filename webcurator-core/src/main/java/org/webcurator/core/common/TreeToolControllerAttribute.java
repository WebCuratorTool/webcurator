package org.webcurator.core.common;

import java.io.File;

public class TreeToolControllerAttribute {
    public boolean enableAccessTool = false;
    public String uploadedFilesDir;
    public String autoQAUrl = "";

    public void setEnableAccessTool(boolean enableAccessTool) {
        this.enableAccessTool = enableAccessTool;
    }

    public void setUploadedFilesDir(String uploadedFilesDir) {
        File file = new File(uploadedFilesDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        this.uploadedFilesDir = uploadedFilesDir;
    }

    public void setAutoQAUrl(String autoQAUrl) {
        this.autoQAUrl = autoQAUrl;
    }

    public boolean isEnableAccessTool() {
        return enableAccessTool;
    }

    public String getUploadedFilesDir() {
        return uploadedFilesDir;
    }

    public String getAutoQAUrl() {
        return autoQAUrl;
    }
}
