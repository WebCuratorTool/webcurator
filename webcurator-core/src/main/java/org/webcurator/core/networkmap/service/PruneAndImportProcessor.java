package org.webcurator.core.networkmap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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
            pruneAndImport();
            log.info("Prune and import finished, {} {}", cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());


        } catch (IOException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            ConcurrencyCount.release();
        }
    }

    private void pruneAndImport() throws IOException, URISyntaxException {
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
        if (!destDir.mkdirs()) {
            log.error("Make dir failed, path: {}", destDir.getAbsolutePath());
            return;
        }
        List<File> dirs = new LinkedList<File>();
        dirs.add(destDir);

        // Get all the files from the source dir.
        File[] archiveFiles = sourceDir.listFiles();

        List<String> extNameList = Arrays.stream(Objects.requireNonNull(archiveFiles)).map(f -> {
            int idx = f.getName().lastIndexOf(".");
            if (idx <= 0) {
                return "UNKNOWN";
            } else {
                return f.getName().substring(idx);
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
        for (File patchHarvestFile : patchHarvestFiles) {
            if (!patchHarvestFile.getName().toUpperCase().endsWith(coordinator.getArchiveType())) {
                continue;
            }
            coordinator.importFromRecorder(patchHarvestFile, urisToDelete, cmd.getNewHarvestResultNumber());
        }
    }
}
