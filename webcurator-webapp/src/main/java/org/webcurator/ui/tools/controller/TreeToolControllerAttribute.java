package org.webcurator.ui.tools.controller;

import org.webcurator.core.store.tools.QualityReviewFacade;

public class TreeToolControllerAttribute {
    protected boolean enableAccessTool = false;
    protected String uploadedFilesDir;
    protected String autoQAUrl = "";

    public void setEnableAccessTool(boolean enableAccessTool) {
        this.enableAccessTool = enableAccessTool;
    }

    public void setUploadedFilesDir(String uploadedFilesDir) {
        this.uploadedFilesDir = uploadedFilesDir;
    }

    public void setAutoQAUrl(String autoQAUrl) {
        this.autoQAUrl = autoQAUrl;
    }
}
