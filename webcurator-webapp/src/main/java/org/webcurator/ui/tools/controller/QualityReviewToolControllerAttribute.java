package org.webcurator.ui.tools.controller;

import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.core.targets.TargetManager;
import org.webcurator.domain.TargetInstanceDAO;

public class QualityReviewToolControllerAttribute {
    protected TargetInstanceManager targetInstanceManager;
    protected TargetInstanceDAO targetInstanceDao;
    protected TargetManager targetManager = null;
    protected String archiveUrl = null;
    protected String archiveName = null;
    protected String archiveUrlAlternative = null;
    protected String archiveUrlAlternativeName = null;
    protected HarvestResourceUrlMapper harvestResourceUrlMapper;
    protected boolean enableBrowseTool = true;
    protected boolean enableAccessTool = false;
    protected String webArchiveTarget = null;
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

    public void setArchiveUrl(String archiveUrl) {
        this.archiveUrl = archiveUrl;
    }

    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }

    public void setArchiveUrlAlternative(String archiveUrlAlternative) {
        this.archiveUrlAlternative = archiveUrlAlternative;
    }

    public void setArchiveUrlAlternativeName(String archiveUrlAlternativeName) {
        this.archiveUrlAlternativeName = archiveUrlAlternativeName;
    }

    public void setHarvestResourceUrlMapper(HarvestResourceUrlMapper harvestResourceUrlMapper) {
        this.harvestResourceUrlMapper = harvestResourceUrlMapper;
    }

    public void setEnableBrowseTool(boolean enableBrowseTool) {
        this.enableBrowseTool = enableBrowseTool;
    }

    public void setEnableAccessTool(boolean enableAccessTool) {
        this.enableAccessTool = enableAccessTool;
    }

    public void setWebArchiveTarget(String webArchiveTarget) {
        this.webArchiveTarget = webArchiveTarget;
    }

    public void setThumbnailRenderer(String thumbnailRenderer) {
		this.thumbnailRenderer = thumbnailRenderer;
	}

    public void setDasBaseUrl(String dasBaseUrl) {
        this.dasBaseUrl = dasBaseUrl;
    }

}
