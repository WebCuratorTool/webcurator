package org.webcurator.core.visualization.modification;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.harvester.coordinator.HarvestCoordinatorPaths;
import org.webcurator.core.reader.LogReader;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.core.store.WCTIndexer;
import org.webcurator.core.util.ApplicationContextFactory;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRowMetadata;
import org.webcurator.domain.model.core.HarvestResult;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PruneAndImportProcessor extends Thread {
    protected static final Logger log = LoggerFactory.getLogger(PruneAndImportProcessor.class);

    private static final Map<String, PruneAndImportProcessor> PROCESSOR_MAP = new HashMap<>();
    private static Semaphore CONCURRENCY_COUNT = new Semaphore(3);
    private final String fileDir; //Upload files
    private final String baseDir; //Harvest WARC files dir
    private final String logsDir; //log dir
    private final String reportsDir; //report dir
    private final PruneAndImportCommandApply cmd;
    private PruneAndImportCoordinator coordinator = null;
    private boolean running = true;
    private final Semaphore stopped = new Semaphore(1);

    public static void setMaxConcurrencyModThreads(int max) {
        CONCURRENCY_COUNT = new Semaphore(max);
    }

    public PruneAndImportProcessor(String fileDir, String baseDir, String logsDirName, String reportsDirName, PruneAndImportCommandApply cmd) {
        this.fileDir = fileDir;
        this.baseDir = baseDir;
        this.cmd = cmd;
        this.logsDir = baseDir + File.separator + cmd.getTargetInstanceId() + File.separator + logsDirName + File.separator + HarvestResult.DIR_LOGS_EXT + File.separator + HarvestResult.DIR_LOGS_MOD + File.separator + cmd.getNewHarvestResultNumber();
        this.reportsDir = baseDir + File.separator + cmd.getTargetInstanceId() + File.separator + reportsDirName + File.separator + HarvestResult.DIR_LOGS_EXT + File.separator + HarvestResult.DIR_LOGS_MOD + File.separator + cmd.getNewHarvestResultNumber();
    }

    @Override
    public void run() {
        String key = getKey(cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
        try {
            PROCESSOR_MAP.put(key, this);
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
        } catch (IOException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            stopped.release();
            CONCURRENCY_COUNT.release();
            PROCESSOR_MAP.remove(key);
        }
    }

    public void pruneAndImport() throws IOException, URISyntaxException {
        final List<String> urisToDelete = new LinkedList<>();
        final Map<String, PruneAndImportCommandRowMetadata> hrsToImport = new HashMap<>();
        cmd.getDataset().forEach(e -> {
            if (e.getOption().equalsIgnoreCase("prune")) {
                urisToDelete.add(e.getUrl());
            } else {
                hrsToImport.put(e.getUrl(), e);
            }
        });

        // Calculate the source and destination directories.
        File sourceDir = new File(baseDir, cmd.getTargetInstanceId() + File.separator + cmd.getHarvestResultNumber());
        File destDir = new File(baseDir, cmd.getTargetInstanceId() + File.separator + cmd.getNewHarvestResultNumber());

        // Ensure the destination directory exists.
        if (!destDir.exists() && !destDir.mkdirs()) {
            log.error("Make dir failed, path: {}", destDir.getAbsolutePath());
            return;
        }
        List<File> dirs = new LinkedList<File>();
        dirs.add(destDir);

        // Get all the files from the source dir.
        File[] archiveFiles = sourceDir.listFiles();

        List<String> extNameList = Arrays.stream(Objects.requireNonNull(archiveFiles)).filter(File::isFile).map(f -> {
            int idx = f.getName().lastIndexOf(".");
            if (idx <= 0) {
                return "UNKNOWN";
            } else {
                return f.getName().substring(idx + 1);
            }
        }).collect(Collectors.toList());

        Map<String, Long> extStatisticMap = extNameList.stream().filter(e -> {
            return !e.equalsIgnoreCase("CDX");
        }).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        if (extStatisticMap.size() != 1) {
            log.error("Multiple archive types included in dir: {}", sourceDir);
            return;
        }

        String archiveType = extStatisticMap.keySet().iterator().next();
        if (archiveType.equalsIgnoreCase(PruneAndImportCoordinatorHeritrixWarc.ARCHIVE_TYPE)) {
            PruneAndImportCoordinatorHeritrixWarc heritrixWarcCoordinator = new PruneAndImportCoordinatorHeritrixWarc();
            heritrixWarcCoordinator.setDirs(dirs);
            coordinator = heritrixWarcCoordinator;
        } else {
            log.error("Not supported archive file format");
            return;
        }

        coordinator.setFileDir(this.fileDir);
        coordinator.setBaseDir(this.baseDir);
        coordinator.init(this.logsDir,this.reportsDir);

        //Process copy and file import
        for (File archiveFile : archiveFiles) {
            if (!running) {
                continue;
            }
            try {
                coordinator.copyArchiveRecords(archiveFile, urisToDelete, hrsToImport, cmd.getNewHarvestResultNumber());
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        //Process file import
        coordinator.importFromFile(cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber(), cmd.getNewHarvestResultNumber(), hrsToImport);

        //Process source URL import
        File patchHarvestDir = new File(this.baseDir, String.format("mod_%d_%d%s1", cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber(), File.separator));
        File[] patchHarvestFiles = new File[0];
        if (patchHarvestDir.exists()) {
            patchHarvestFiles = patchHarvestDir.listFiles();
        }
        for (File patchHarvestFile : Objects.requireNonNull(patchHarvestFiles)) {
            if (!running) {
                continue;
            }

            if (!patchHarvestFile.isFile() || !patchHarvestFile.getName().toUpperCase().endsWith(coordinator.getArchiveType())) {
                continue;
            }
            coordinator.importFromRecorder(patchHarvestFile, urisToDelete, cmd.getNewHarvestResultNumber());
        }

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
        return PROCESSOR_MAP.get(key);
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

