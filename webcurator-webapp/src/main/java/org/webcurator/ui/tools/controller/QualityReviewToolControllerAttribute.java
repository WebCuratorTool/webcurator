package org.webcurator.ui.tools.controller;

import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.core.targets.TargetManager;
import org.webcurator.domain.TargetInstanceDAO;

public class QualityReviewToolControllerAttribute {
    protected TargetInstanceManager targetInstanceManager;
    protected TargetInstanceDAO targetInstanceDao;
    protected TargetManager targetManager = null;
    protected String archive1Url = null;
    protected String archive1Name = null;
    protected String archive2Url = null;
    protected String archive2Name = null;
    protected String archive3Url = null;
    protected String archive3Name = null;
    protected String accessToolUrl;
    protected String accessToolName;
    protected boolean enableBrowseTool = true;
    protected boolean enableAccessTool = false;
    protected String thumbnailRenderer = null;
    protected String dasBaseUrl = null;

    public void setTargetInstanceManager(TargetInstanceManager targetInstanceManager) {
        this.targetInstanceManager = targetInstanceManager;
    }

    public void setTargetInstanceDao(TargetInstanceDAO targetInstanceDao) {
        this.targetInstanceDao = targetInstanceDao;
    }

    public void setTargetManager(TargetManager targetManager) {
        this.targetManager = targetManager;
    }

    public void setArchive1Url(String archive1Url) {
        this.archive1Url = archive1Url;
    }

    public void setArchive1Name(String archive1Name) {
        this.archive1Name = archive1Name;
    }

    public void setArchive2Url(String archive2Url) {
        this.archive2Url = archive2Url;
    }

    public void setArchive2Name(String archive2Name) {
        this.archive2Name = archive2Name;
    }

    public String getArchive3Url() {
        return archive3Url;
    }

    public void setArchive3Url(String archive3Url) {
        this.archive3Url = archive3Url;
    }

    public String getArchive3Name() {
        return archive3Name;
    }

    public void setArchive3Name(String archive3Name) {
        this.archive3Name = archive3Name;
    }

    public String getAccessToolUrl() {
        return accessToolUrl;
    }

    public void setAccessToolUrl(String accessToolUrl) {
        this.accessToolUrl = accessToolUrl;
    }

    public String getAccessToolName() {
        return accessToolName;
    }

    public void setAccessToolName(String accessToolName) {
        this.accessToolName = accessToolName;
    }

    public void setEnableBrowseTool(boolean enableBrowseTool) {
        this.enableBrowseTool = enableBrowseTool;
    }

    public void setEnableAccessTool(boolean enableAccessTool) {
        this.enableAccessTool = enableAccessTool;
    }

    public void setThumbnailRenderer(String thumbnailRenderer) {
		this.thumbnailRenderer = thumbnailRenderer;
	}

    public void setDasBaseUrl(String dasBaseUrl) {
        this.dasBaseUrl = dasBaseUrl;
    }

}
