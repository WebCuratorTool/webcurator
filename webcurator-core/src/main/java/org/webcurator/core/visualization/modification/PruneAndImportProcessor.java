package org.webcurator.core.visualization.modification;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.thirdparty.guava.common.io.Files;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.harvester.coordinator.HarvestCoordinatorPaths;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.core.visualization.VisualizationAbstractProcessor;
import org.webcurator.core.visualization.VisualizationCoordinator;
import org.webcurator.core.visualization.VisualizationManager;
import org.webcurator.core.visualization.VisualizationProgressBar;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRowMetadata;
import org.webcurator.domain.model.core.HarvestResult;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class PruneAndImportProcessor extends VisualizationAbstractProcessor {
    private final PruneAndImportCommandApply cmd;
    private PruneAndImportCoordinator coordinator = null;
    private final AbstractRestClient client;


    public PruneAndImportProcessor(VisualizationManager visualizationManager, PruneAndImportCommandApply cmd, AbstractRestClient client) throws DigitalAssetStoreException {
        super(visualizationManager, cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
        this.cmd = cmd;
        this.client = client;
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
                if (urisToDelete.size() == 0) {
                    coordinator.copyArchiveRecords(f, urisToDelete, hrsToImport, cmd.getNewHarvestResultNumber());
                } else {
                    //Copy file directly
                    File to = new File(destDir, f.getName());
                    Files.copy(f, to);
                }
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
        coordinator.importFromFile(cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber(), cmd.getNewHarvestResultNumber(), hrsToImport);
        progressItemFileImported.setCurLength(progressItemFileImported.getMaxLength());

        coordinator.writeReport();
        coordinator.close();
    }

    private void notifyModificationComplete(long targetInstanceId, int harvestResultNumber) {
        RestTemplateBuilder restTemplateBuilder = client.getRestTemplateBuilder();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(client.getUrl(HarvestCoordinatorPaths.MODIFICATION_COMPLETE_PRUNE_IMPORT))
                .queryParam("targetInstanceOid", targetInstanceId)
                .queryParam("harvestNumber", harvestResultNumber);
        RestTemplate restTemplate = restTemplateBuilder.build();
        URI uri = uriComponentsBuilder.build().toUri();
        restTemplate.getForObject(uri, Void.class);
    }

    @Override
    protected String getProcessorStage() {
        return HarvestResult.PATCH_STAGE_TYPE_MODIFYING;
    }

    @Override
    public void processInternal() {
        try {
            this.stopped.acquire();
            if (running) {
                this.pruneAndImport();
                log.info("Prune and import finished, {} {}", cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
            }

            if (running) {
                this.notifyModificationComplete(cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
                log.info("Notify Core that modification is finished");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            this.stopped.release();
            VisualizationProgressBar.removeInstance(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
        }
    }

    @Override
    public void deleteInternal() {
        this.terminateTask();
        try {
            this.stopped.acquire(); //wait until process ended
        } catch (InterruptedException e) {
            log.error("Acquire token failed when stop modification task, {}, {}", targetInstanceId, harvestResultNumber);
            return;
        }

        //delete modification result
        this.delete(baseDir + File.separator + targetInstanceId, Integer.toString(harvestResultNumber));

        //delete patching harvest result
        this.delete(baseDir, String.format("mod_%d_%d", targetInstanceId, harvestResultNumber));
    }

    @Override
    protected void terminateInternal() {
        if (this.coordinator != null) {
            this.coordinator.running = false;
        }
    }
}

