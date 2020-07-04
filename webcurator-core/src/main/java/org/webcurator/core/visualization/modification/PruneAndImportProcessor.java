package org.webcurator.core.visualization.modification;

import org.apache.commons.io.FileUtils;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.util.PatchUtil;
import org.webcurator.core.visualization.VisualizationAbstractProcessor;
import org.webcurator.core.visualization.VisualizationCoordinator;
import org.webcurator.core.visualization.VisualizationProgressBar;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRowMetadata;
import org.webcurator.domain.model.core.HarvestResult;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class PruneAndImportProcessor extends VisualizationAbstractProcessor {
    private final PruneAndImportCommandApply cmd;
    private PruneAndImportCoordinator coordinator = null;

    public PruneAndImportProcessor(PruneAndImportCommandApply cmd) throws DigitalAssetStoreException {
        super(cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
        this.cmd = cmd;
    }

    @Override
    protected void initInternal() {
        try {
            PatchUtil.modifier.savePatchJob(baseDir, cmd);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void pruneAndImport() throws Exception {
        //Initial derived archive file list
        File derivedDir = new File(baseDir, cmd.getTargetInstanceId() + File.separator + cmd.getHarvestResultNumber());
        List<File> derivedArchiveFiles = VisualizationCoordinator.grepWarcFiles(derivedDir);

        //Initial patching archive file list
        File patchHarvestDir = new File(this.baseDir, String.format("%s%s1", PatchUtil.getPatchJobName(cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber()), File.separator));
        List<File> patchArchiveFiles = VisualizationCoordinator.grepWarcFiles(patchHarvestDir);

        //Initial to be pruned and to be imported list
        final List<String> urisToDelete = new LinkedList<>();
        final Map<String, PruneAndImportCommandRowMetadata> hrsToImport = new HashMap<>();
        cmd.getDataset().forEach(e -> {
            if (e.getOption().equalsIgnoreCase("prune")) {
                urisToDelete.add(e.getUrl());
            } else {
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
        List<File> dirs = new LinkedList<>();
        dirs.add(destDir);

        // Initial extractor.
        PruneAndImportCoordinatorHeritrixWarc heritrixWarcCoordinator = new PruneAndImportCoordinatorHeritrixWarc();
        heritrixWarcCoordinator.setWctCoordinatorClient(this.wctCoordinatorClient);
        heritrixWarcCoordinator.setDirs(dirs);
        heritrixWarcCoordinator.setFileDir(this.fileDir);
        heritrixWarcCoordinator.setBaseDir(this.baseDir);
        heritrixWarcCoordinator.init(this.logsDir, this.reportsDir, this.progressBar);
        this.coordinator = heritrixWarcCoordinator;

        //Process copy and file import
        for (File f : derivedArchiveFiles) {
            if (running) {
                coordinator.copyArchiveRecords(f, urisToDelete, hrsToImport, cmd.getNewHarvestResultNumber());
                VisualizationProgressBar.ProgressItem item = progressBar.getProgressItem(f.getName());
                item.setCurLength(item.getMaxLength());
            }
        }

        //Process source URL import
        for (File f : patchArchiveFiles) {
            if (running) {
                coordinator.importFromRecorder(f, urisToDelete, cmd.getNewHarvestResultNumber());
                VisualizationProgressBar.ProgressItem item = progressBar.getProgressItem(f.getName());
                item.setCurLength(item.getMaxLength());
            }
        }

        //Process file import
        if (progressItemFileImported.getMaxLength() > 0) {
            coordinator.importFromFile(cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber(), cmd.getNewHarvestResultNumber(), hrsToImport);
            progressItemFileImported.setCurLength(progressItemFileImported.getMaxLength());
        }
        coordinator.writeReport();
        coordinator.close();
    }

    @Override
    protected String getProcessorStage() {
        return HarvestResult.PATCH_STAGE_TYPE_MODIFYING;
    }

    @Override
    public void processInternal() throws Exception {
        if (running) {
            this.pruneAndImport();
            log.info("Prune and import finished, {} {}", cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
        }

//        if (running) {
//            wctCoordinatorClient.notifyModificationComplete(cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
//            log.info("Notify Core that modification is finished");
//        }
//
//        //Move the current metadata to history fold to avoid duplicated execution
//        PatchUtil.modifier.moveJob2History(baseDir, targetInstanceId, harvestResultNumber);
    }

    @Override
    public void deleteInternal() {
        //delete modification result
        this.delete(baseDir + File.separator + targetInstanceId, Integer.toString(harvestResultNumber));

        //delete patching harvest result
        this.delete(baseDir, PatchUtil.getPatchJobName(targetInstanceId, harvestResultNumber));
    }

    @Override
    protected void terminateInternal() {
        if (this.coordinator != null) {
            this.coordinator.running = false;
        }
    }
}

