package org.webcurator.core.visualization.modification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.harvester.coordinator.HarvestCoordinatorPaths;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.core.store.WCTIndexer;
import org.webcurator.core.util.ApplicationContextFactory;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRowMetadata;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PruneAndImportProcessor implements Runnable {
    protected static final Logger log = LoggerFactory.getLogger(PruneAndImportProcessor.class);

    private static final Semaphore ConcurrencyCount = new Semaphore(10);
    private final String fileDir; //Upload files
    private final String baseDir; //Harvest WARC files dir
    private final PruneAndImportCommandApply cmd;

    public PruneAndImportProcessor(String fileDir, String baseDir, PruneAndImportCommandApply cmd) {
        this.fileDir = fileDir;
        this.baseDir = baseDir;
        this.cmd = cmd;
    }

    @Override
    public void run() {
        try {
            ConcurrencyCount.acquire();
            this.pruneAndImport();
            log.info("Prune and import finished, {} {}", cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
            this.notifyModificationComplete(cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
            log.info("Notify Core that modification is finished");
        } catch (IOException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            ConcurrencyCount.release();
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

        PruneAndImportCoordinator coordinator = null;
        String archiveType = extStatisticMap.keySet().iterator().next();
        if (archiveType.equalsIgnoreCase(PruneAndImportCoordinatorHeritrixWarc.ARCHIVE_TYPE)) {
            PruneAndImportCoordinatorHeritrixWarc heritrixWarcCoordinator = new PruneAndImportCoordinatorHeritrixWarc();
            heritrixWarcCoordinator.setDirs(dirs);
            coordinator = heritrixWarcCoordinator;
        } else {
            log.error("Not supported archive file format");
            return;
        }

        //Process copy and file import
        for (File archiveFile : archiveFiles) {
            coordinator.copyArchiveRecords(archiveFile, urisToDelete, hrsToImport, cmd.getNewHarvestResultNumber());
            coordinator.importFromFile(hrsToImport);
        }

        //Process source URL import
        File patchHarvestDir = new File(this.fileDir, String.format("mod_%d_%d%s1", cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber(), File.separator));
        File[] patchHarvestFiles = new File[0];
        if (patchHarvestDir.exists()) {
            patchHarvestFiles = patchHarvestDir.listFiles();
        }
        for (File patchHarvestFile : Objects.requireNonNull(patchHarvestFiles)) {
            if (!patchHarvestFile.isFile() || !patchHarvestFile.getName().toUpperCase().endsWith(coordinator.getArchiveType())) {
                continue;
            }
            coordinator.importFromRecorder(patchHarvestFile, urisToDelete, cmd.getNewHarvestResultNumber());
        }
    }

    public void notifyModificationComplete(long targetInstanceId, int harvestResultNumber) {
        AbstractRestClient client = ApplicationContextFactory.getApplicationContext().getBean(WCTIndexer.class);
        RestTemplateBuilder restTemplateBuilder = ApplicationContextFactory.getApplicationContext().getBean(RestTemplateBuilder.class);
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(client.getUrl(HarvestCoordinatorPaths.COMPLETE_MODIFICATION))
                .queryParam("targetInstanceOid", targetInstanceId)
                .queryParam("harvestNumber", harvestResultNumber);
        RestTemplate restTemplate = restTemplateBuilder.build();
        URI uri = uriComponentsBuilder.build().toUri();
        restTemplate.getForObject(uri, Void.class);
    }
}
