package org.webcurator.core.visualization.modification.processor;

import org.apache.commons.io.FileUtils;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.util.PatchUtil;
import org.webcurator.core.visualization.VisualizationAbstractProcessor;
import org.webcurator.core.visualization.VisualizationProgressBar;
import org.webcurator.core.visualization.VisualizationStatisticItem;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.modification.metadata.ModifyRowMetadata;
import org.webcurator.domain.model.core.HarvestResult;

import java.io.*;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("all")
public abstract class ModifyProcessor extends VisualizationAbstractProcessor {
    protected static final int BYTE_BUFF_SIZE = 1024;

    protected List<File> dirs = new LinkedList<>();

    /**
     * Arc files meta data date format.
     */
    protected static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    protected static final SimpleDateFormat writerDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    protected static final DateTimeFormatter fTimestamp17 = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private ModifyApplyCommand cmd;

    private ModifyProcessor(long targetInstanceId, int harvestResultNumber) {
        super(targetInstanceId, harvestResultNumber);
        this.state = HarvestResult.STATE_MODIFYING;
        this.flag = "MOD";
        this.reportTitle = StatisticItem.getPrintTitle();
    }

    public ModifyProcessor(ModifyApplyCommand cmd) {
        this(cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
        this.cmd = cmd;
    }

    @Override
    protected void initInternal() throws IOException {
        PatchUtil.modifier.savePatchJob(baseDir, cmd);

        // Calculate the source and destination directories.
        File destDir = new File(baseDir, cmd.getTargetInstanceId() + File.separator + cmd.getNewHarvestResultNumber());
        // Ensure the destination directory exists.
        if (!destDir.exists() && !destDir.mkdirs()) {
            log.error("Make dir failed, path: {}", destDir.getAbsolutePath());
            return;
        } else if (destDir.exists()) {
            //Clean all existing files
            FileUtils.cleanDirectory(destDir);
        }
        dirs.add(destDir);
    }

    @Override
    protected String getProcessorStage() {
        return HarvestResult.PATCH_STAGE_TYPE_MODIFYING;
    }

    @Override
    public void processInternal() throws Exception {
        //Initial derived archive file list
        File derivedDir = new File(baseDir, cmd.getTargetInstanceId() + File.separator + cmd.getHarvestResultNumber());
        List<File> derivedArchiveFiles = PatchUtil.listWarcFiles(derivedDir);

        //Initial patching archive file list
        File patchHarvestDir = new File(this.baseDir, String.format("%s%s1", PatchUtil.getPatchJobName(cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber()), File.separator));
        List<File> patchArchiveFiles = PatchUtil.listWarcFiles(patchHarvestDir);

        //Initial to be pruned and to be imported list
        final List<String> urisToDelete = new LinkedList<>();
        final List<String> urisToImportByUrl = new LinkedList<>();
        final List<String> urisToImportByFile = new LinkedList<>();
        final Map<String, ModifyRowMetadata> hrsToImport = new HashMap<>();
        cmd.getDataset().forEach(e -> {
            if (e.getOption().equalsIgnoreCase("prune")) {
                urisToDelete.add(e.getUrl());
            } else if (e.getOption().equalsIgnoreCase("url")) {
                urisToImportByUrl.add(e.getUrl());
                hrsToImport.put(e.getUrl(), e);
            } else if (e.getOption().equalsIgnoreCase("file")) {
                urisToImportByFile.add(e.getUrl());
                hrsToImport.put(e.getUrl(), e);
            }
        });

        //Initial progress data: derived files
        derivedArchiveFiles.forEach(f -> {
            VisualizationProgressBar.ProgressItem item = progressBar.getProgressItem(f.getName());
            item.setMaxLength(f.length());
        });

        //Initial progress data: patch harvesting files
        patchArchiveFiles.forEach(f -> {
            VisualizationProgressBar.ProgressItem item = progressBar.getProgressItem(f.getName());
            item.setMaxLength(f.length());
        });

        //Initial progress data: to be imported files
        VisualizationProgressBar.ProgressItem progressItemFileImported = progressBar.getProgressItem("ImportedFiles");
        final AtomicLong totMaxLength = new AtomicLong(0);
        hrsToImport.values().forEach(toImportFile -> {
            if (toImportFile.getOption().equalsIgnoreCase("file")) {
                totMaxLength.addAndGet(toImportFile.getLength());
            }
        });
        progressItemFileImported.setMaxLength(totMaxLength.get());

        //Process copy and file import
        for (File f : derivedArchiveFiles) {
            this.tryBlock();
            copyArchiveRecords(f, urisToDelete, hrsToImport, cmd.getNewHarvestResultNumber());
            VisualizationProgressBar.ProgressItem item = progressBar.getProgressItem(f.getName());
            item.setCurLength(item.getMaxLength());
        }

        //Process source URL import
        for (File f : patchArchiveFiles) {
            this.tryBlock();
            importFromPatchHarvest(f, urisToDelete, urisToImportByUrl, cmd.getNewHarvestResultNumber());
            VisualizationProgressBar.ProgressItem item = progressBar.getProgressItem(f.getName());
            item.setCurLength(item.getMaxLength());
        }

        //Process file import
        if (progressItemFileImported.getMaxLength() > 0) {
            importFromFile(cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber(), cmd.getNewHarvestResultNumber(), hrsToImport);
            progressItemFileImported.setCurLength(progressItemFileImported.getMaxLength());
        }
        writeReport();
    }

    @Override
    protected void terminateInternal() {

    }

    @Override
    public void deleteInternal() {
        //delete modification result
        this.delete(baseDir + File.separator + targetInstanceId, Integer.toString(harvestResultNumber));

        //delete patching harvest result
        this.delete(baseDir, PatchUtil.getPatchJobName(targetInstanceId, harvestResultNumber));
    }

    public File downloadFile(long job, int harvestResultNumber, ModifyRowMetadata metadata) throws IOException, DigitalAssetStoreException {
        String tempFileName = UUID.randomUUID().toString();
        File dirFile = new File(fileDir);
        if (!dirFile.exists() && !dirFile.mkdir()) {
            String err = String.format("Make dir failed: %s", fileDir);
            log.error(err);
            throw new DigitalAssetStoreException(err);
        }
        File downloadedFile = new File(fileDir, tempFileName);
        return wctClient.getDownloadFileURL(job, harvestResultNumber, metadata.getName(), downloadedFile);
    }

    public abstract void copyArchiveRecords(File fileFrom, List<String> urisToDelete, Map<String, ModifyRowMetadata> hrsToImport, int newHarvestResultNumber) throws Exception;

    public abstract void importFromFile(long job, int harvestResultNumber, int newHarvestResultNumber, Map<String, ModifyRowMetadata> hrsToImport) throws Exception;

    public abstract void importFromPatchHarvest(File fileFrom, List<String> urisToDelete, List<String> urisToImportByUrl, int newHarvestResultNumber) throws IOException, URISyntaxException, InterruptedException;

    static class WarcFilenameTemplate {
        private String prefix;
        private String timestamp;
        private int serialNo;
        private String heritrixInfo;

        public WarcFilenameTemplate(String strippedImpArcFilename) throws Exception {
            if (strippedImpArcFilename == null || strippedImpArcFilename.indexOf('-') < 0) {
                throw new Exception("Unsupported file template: " + strippedImpArcFilename);
            }

            int idx = strippedImpArcFilename.indexOf('-');
            this.prefix = strippedImpArcFilename.substring(0, idx);
        }

        public String toString() {
            return String.format("%s-%s-%05d-%s", this.prefix, this.timestamp, this.serialNo, this.heritrixInfo);
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public int getSerialNo() {
            return serialNo;
        }

        public void setSerialNo(int serialNo) {
            this.serialNo = serialNo;
        }

        public String getHeritrixInfo() {
            return heritrixInfo;
        }

        public void setHeritrixInfo(String heritrixInfo) {
            this.heritrixInfo = heritrixInfo;
        }
    }

    static class StatisticItem implements VisualizationStatisticItem {
        private String fromFileName = null;
        private long fromFileLength = -1;
        private String toFileName = null;
        private long toFileLength = -1;
        private int totalRecords = 0;
        private int skippedRecords = 0;
        private int copiedRecords = 0;
        private int prunedRecords = 0;
        private int failedRecords = 0;

        public static String getPrintTitle() {
            return "FromFileName\tFromFileLength\tToFileName\tToFileLength\tTotalRecords\tSkippedRecords\tPrunedRecords\tCopiedRecords\tFailedRecords";
        }

        @Override
        public String getPrintContent() {
            String pFromFileName = this.fromFileName == null ? "--" : this.fromFileName;
            String pFromFileLength = this.fromFileLength < 0 ? "--" : Long.toString(this.fromFileLength);
            String pToFileName = this.toFileName == null ? "--" : this.toFileName;
            String pToFileLength = this.toFileLength < 0 ? "--" : Long.toString(this.toFileLength);

            return String.format("%s\t%s\t%s\t%s\t%d\t%d\t%d\t%d\t%d",
                    pFromFileName,
                    pFromFileLength,
                    pToFileName,
                    pToFileLength,
                    this.totalRecords,
                    this.skippedRecords,
                    this.prunedRecords,
                    this.copiedRecords,
                    this.failedRecords);
        }

        public void setFromFileName(String fromFileName) {
            this.fromFileName = fromFileName;
        }

        public void setFromFileLength(long fromFileLength) {
            this.fromFileLength = fromFileLength;
        }

        public void setToFileName(String toFileName) {
            this.toFileName = toFileName;
        }

        public void setToFileLength(long toFileLength) {
            this.toFileLength = toFileLength;
        }

        public void increaseSkippedRecords() {
            this.increaseSkippedRecords(1);
        }

        public void increaseSkippedRecords(int num) {
            this.skippedRecords += num;
            this.totalRecords += num;
        }

        public void increasePrunedRecords() {
            this.increasePrunedRecords(1);
        }

        public void increasePrunedRecords(int num) {
            this.prunedRecords += num;
            this.totalRecords += num;
        }

        public void increaseCopiedRecords() {
            this.increaseCopiedRecords(1);
        }

        public void increaseCopiedRecords(int num) {
            this.copiedRecords += num;
            this.totalRecords += num;
        }

        public void increaseFailedRecords() {
            this.increaseFailedRecords(1);
        }

        public void increaseFailedRecords(int num) {
            this.failedRecords += num;
            this.totalRecords += num;
        }
    }
}
