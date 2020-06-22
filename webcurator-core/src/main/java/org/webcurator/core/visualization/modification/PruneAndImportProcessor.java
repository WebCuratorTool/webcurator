package org.webcurator.core.visualization.modification;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.harvester.coordinator.HarvestCoordinatorPaths;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.core.store.WCTIndexer;
import org.webcurator.core.util.ApplicationContextFactory;
import org.webcurator.core.visualization.VisualizationCoordinator;
import org.webcurator.core.visualization.VisualizationManager;
import org.webcurator.core.visualization.VisualizationProgressBar;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRowMetadata;
import org.webcurator.domain.model.core.HarvestResult;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

public class PruneAndImportProcessor extends Thread {
    protected static final Logger log = LoggerFactory.getLogger(PruneAndImportProcessor.class);

    private static final Map<String, PruneAndImportProcessor> RUNNING_PROCESSOR = new HashMap<>();
    private static Semaphore CONCURRENCY_COUNT = new Semaphore(3);
    private final String fileDir; //Upload files
    private final String baseDir; //Harvest WARC files dir
    private final String logsDir; //log dir
    private final String reportsDir; //report dir
    private final PruneAndImportCommandApply cmd;
    private PruneAndImportCoordinator coordinator = null;
    private boolean running = true;
    private final Semaphore stopped = new Semaphore(1);
    private VisualizationProgressBar progressBar = new VisualizationProgressBar("MODIFYING");

    public static void setMaxConcurrencyModThreads(int max) {
        CONCURRENCY_COUNT = new Semaphore(max);
    }

    public PruneAndImportProcessor(VisualizationManager visualizationManager, PruneAndImportCommandApply cmd) {
        this.fileDir = visualizationManager.getUploadDir();
        this.baseDir = visualizationManager.getBaseDir();
        this.cmd = cmd;
        this.logsDir = baseDir + File.separator + cmd.getTargetInstanceId() + File.separator + visualizationManager.getLogsDir() + File.separator + HarvestResult.DIR_LOGS_EXT + File.separator + HarvestResult.DIR_LOGS_MOD + File.separator + cmd.getNewHarvestResultNumber();
        this.reportsDir = baseDir + File.separator + cmd.getTargetInstanceId() + File.separator + visualizationManager.getReportsDir() + File.separator + HarvestResult.DIR_LOGS_EXT + File.separator + HarvestResult.DIR_LOGS_MOD + File.separator + cmd.getNewHarvestResultNumber();
    }

    @Override
    public void run() {
        String key = getKey(cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
        try {
            RUNNING_PROCESSOR.put(key, this);
            CONCURRENCY_COUNT.acquire();
            stopped.acquire();
            if (running) {
                this.pruneAndImport();
                log.info("Prune and import finished, {} {}", cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
            }

            if (running) {
                this.notifyModificationComplete(cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
                log.info("Notify Core that modification is finished");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stopped.release();
            CONCURRENCY_COUNT.release();
            RUNNING_PROCESSOR.remove(key);
        }
    }

    public void pruneAndImport() throws Exception {
        //Initial derived archive file list
        File derivedDir = new File(baseDir, cmd.getTargetInstanceId() + File.separator + cmd.getHarvestResultNumber());
        List<File> derivedArchiveFiles = VisualizationCoordinator.grepWarcFiles(derivedDir);

        //Initial patching archive file list
        File patchHarvestDir = new File(this.baseDir, String.format("mod_%d_%d%s1", cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber(), File.separator));
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
        List<File> dirs = new LinkedList<File>();
        dirs.add(destDir);

        // Initial processor.
        PruneAndImportCoordinatorHeritrixWarc heritrixWarcCoordinator = new PruneAndImportCoordinatorHeritrixWarc();
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
                item.setCurLength(f.length());
            }
        }

        //Process source URL import
        for (File f : patchArchiveFiles) {
            if (running) {
                coordinator.importFromRecorder(f, urisToDelete, cmd.getNewHarvestResultNumber());
                VisualizationProgressBar.ProgressItem item = progressBar.getProgressItem(f.getName());
                item.setCurLength(f.length());
            }
        }

        //Process file import
        coordinator.importFromFile(cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber(), cmd.getNewHarvestResultNumber(), hrsToImport);
        progressItemFileImported.setCurLength(progressItemFileImported.getMaxLength());

        coordinator.writeReport();
        coordinator.close();
    }

    public void notifyModificationComplete(long targetInstanceId, int harvestResultNumber) {
        AbstractRestClient client = ApplicationContextFactory.getApplicationContext().getBean(WCTIndexer.class);
        RestTemplateBuilder restTemplateBuilder = ApplicationContextFactory.getApplicationContext().getBean(RestTemplateBuilder.class);
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(client.getUrl(HarvestCoordinatorPaths.MODIFICATION_COMPLETE_PRUNE_IMPORT))
                .queryParam("targetInstanceOid", targetInstanceId)
                .queryParam("harvestNumber", harvestResultNumber);
        RestTemplate restTemplate = restTemplateBuilder.build();
        URI uri = uriComponentsBuilder.build().toUri();
        restTemplate.getForObject(uri, Void.class);
    }

    public static PruneAndImportProcessor getProcessor(long targetInstanceId, int harvestNumber) {
        String key = getKey(targetInstanceId, harvestNumber);
        return RUNNING_PROCESSOR.get(key);
    }

    public static VisualizationProgressBar getProgress(long targetInstanceId, int harvestNumber) {
        String key = getKey(targetInstanceId, harvestNumber);
        PruneAndImportProcessor processor = RUNNING_PROCESSOR.get(key);
        return processor == null ? null : processor.getProgress();
    }

    public VisualizationProgressBar getProgress() {
        return this.progressBar;
    }

    public void delete(long targetInstanceId, int harvestNumber) {
        this.stopModification();
        try {
            this.stopped.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("Acquire token failed when stop modification task, {}, {}", targetInstanceId, harvestNumber);
            return;
        }

        //delete harvest result
        this.delete(baseDir + File.separator + targetInstanceId, Integer.toString(harvestNumber));

        //delete patching harvest
        this.delete(baseDir, getKey(targetInstanceId, harvestNumber));
    }

    private void delete(String rootDir, String dir) {
        File toPurge = new File(rootDir, dir);
        if (log.isDebugEnabled()) {
            log.debug("About to purge dir " + toPurge.toString());
        }
        try {
            FileUtils.deleteDirectory(toPurge);
        } catch (IOException e) {
            if (log.isWarnEnabled()) {
                log.warn("Unable to purge target instance folder: " + toPurge.getAbsolutePath());
            }
        }
    }

    public static String getKey(long targetInstanceId, int harvestNumber) {
        return String.format("mod_%d_%d", targetInstanceId, harvestNumber);
    }

    public void pauseModification() {
        if (this.getState().equals(State.RUNNABLE)) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error("Failed to pause");
            }
        }
    }

    public void resumeModification() {
        if (this.getState().equals(State.WAITING)) {
            this.notify();
        }
    }

    public void stopModification() {
        this.running = false;
        if (this.coordinator != null) {
            this.coordinator.running = false;
        }
    }
}

