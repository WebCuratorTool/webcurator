package org.webcurator.core.visualization;

import java.io.File;

public class VisualizationDirectoryManager {
    private static final String TEMPLATE_UPLOAD = "%s" + File.separator + "uploadedFiles" + File.separator + "%d";
    private static final String TEMPLATE_BASE_LOG_REPORT = "%s" + File.separator + "%d" + File.separator + "%s";
//    private static final String TEMPLATE_PATCH_LOG_REPORT = "%s" + File.separator + "attached" + File.separator + "%d" + File.separator + "%s";
//    private static final String TEMPLATE_BASE_PATCH_LOG_REPORT = "%s" + File.separator + "attached" + File.separator + "%d";

    private String baseDir = null;
    private String baseLogDir = null;
    private String baseReportDir = null;
    private String archiveRepository = null;
    private String archiveArcDirectory = null;
    private String openWayBack = null;

    public VisualizationDirectoryManager(String baseDir, String baseLogDir, String baseReportDir) {
        this.baseDir = baseDir;
        this.baseLogDir = baseLogDir;
        this.baseReportDir = baseReportDir;
    }

    public String getBaseDir() {
        return this.baseDir;
    }

    public String getSubHarvestResultFolder(long job, int harvestResultNumber) {
        return job + File.separator + harvestResultNumber;
    }

    public File getHarvestResultFolder(long job, int harvestResultNumber) {
        return new File(this.baseDir, getSubHarvestResultFolder(job, harvestResultNumber));
    }

    public String getDbName(long job, int harvestResultNumber) {
        return String.format("%d_%d", job, harvestResultNumber);
    }

    public String getDbPath(long job, int harvestResultNumber) {
        return String.format("%s%s%s%s_resource", this.baseDir, File.separator, getSubHarvestResultFolder(job, harvestResultNumber), File.separator);
    }


    public String getUploadDir(long targetInstanceId) {
        String s = String.format(TEMPLATE_UPLOAD, baseDir, targetInstanceId);
        File f = new File(s);
        if (!f.exists()) {
            f.mkdirs();
        }
        return s;
    }

    public String getBaseLogDir(long targetInstanceId) {
        String s = String.format(TEMPLATE_BASE_LOG_REPORT, baseDir, targetInstanceId, baseLogDir);
        File f = new File(s);
        if (!f.exists()) {
            f.mkdirs();
        }
        return s;
    }

    public String getBaseReportDir(long targetInstanceId) {
        String s = String.format(TEMPLATE_BASE_LOG_REPORT, baseDir, targetInstanceId, baseReportDir);
        File f = new File(s);
        if (!f.exists()) {
            f.mkdirs();
        }
        return s;
    }

    public String getPatchLogFileName(String prefix, int harvestResultNumber) {
        return String.format("%s-%d-running.log", prefix, harvestResultNumber);
    }

    public String getPatchReportFileName(String prefix, int harvestResultNumber) {
        return String.format("%s-%d-report.txt", prefix, harvestResultNumber);
    }

    public String getArchiveRepository() {
        return archiveRepository;
    }

    public void setArchiveRepository(String archiveRepository) {
        this.archiveRepository = archiveRepository;
    }

    public String getArchiveArcDirectory() {
        return archiveArcDirectory;
    }

    public void setArchiveArcDirectory(String archiveArcDirectory) {
        this.archiveArcDirectory = archiveArcDirectory;
    }

    public String getOpenWayBack() {
        return openWayBack;
    }

    public void setOpenWayBack(String openWayBack) {
        this.openWayBack = openWayBack;
    }
}
