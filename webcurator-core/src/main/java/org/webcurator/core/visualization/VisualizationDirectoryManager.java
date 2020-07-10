package org.webcurator.core.visualization;

import java.io.File;

public class VisualizationDirectoryManager {
    private static final String TEMPLATE_UPLOAD = "%s" + File.separator + "uploadedFiles" + File.separator + "%d";
    private static final String TEMPLATE_BASE_LOG_REPORT = "%s" + File.separator + "%d" + File.separator + "%s";
    private static final String TEMPLATE_PATCH_LOG_REPORT = "%s" + File.separator + "attached" + File.separator + "%d" + File.separator + "%s";

    private String baseDir = null;
    private String baseLogDir = null;
    private String baseReportDir = null;


    public VisualizationDirectoryManager(String baseDir, String baseLogDir, String baseReportDir) {
        this.baseDir = baseDir;
        this.baseLogDir = baseLogDir;
        this.baseReportDir = baseReportDir;
    }

    public String getBaseDir() {
        return this.baseDir;
    }

    public String getUploadDir(long targetInstanceId) {
        return String.format(TEMPLATE_UPLOAD, baseDir, targetInstanceId);
    }

    public String getBaseLogDir(long targetInstanceId) {
        return String.format(TEMPLATE_BASE_LOG_REPORT, baseDir, targetInstanceId, baseLogDir);
    }

    public String getBaseReportDir(long targetInstanceId) {
        return String.format(TEMPLATE_BASE_LOG_REPORT, baseDir, targetInstanceId, baseReportDir);
    }

    public String getPatchLogDir(String prefix, long targetInstanceId, int harvestResultNumber) {
//        if (harvestResultNumber == 1) {
//            return getBaseLogDir(targetInstanceId);
//        }
        return String.format(TEMPLATE_PATCH_LOG_REPORT, getBaseLogDir(targetInstanceId), harvestResultNumber, prefix);
    }

    public String getPatchReportDir(String prefix, long targetInstanceId, int harvestResultNumber) {
//        if (harvestResultNumber == 1) {
//            return getBaseReportDir(targetInstanceId);
//        }
        return String.format(TEMPLATE_PATCH_LOG_REPORT, getBaseReportDir(targetInstanceId), harvestResultNumber, prefix);
    }
}
