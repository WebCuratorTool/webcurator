package org.webcurator.core.visualization.networkmap;

import org.archive.io.*;
import org.archive.io.warc.WARCConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.visualization.VisualizationCoordinator;
import org.webcurator.core.visualization.VisualizationManager;
import org.webcurator.core.visualization.VisualizationProgressBar;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.visualization.networkmap.extractor.ResourceExtractor;
import org.webcurator.core.visualization.networkmap.extractor.ResourceExtractorWarc;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapDomain;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapDomainManager;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNode;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.SeedHistory;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("all")
public class ResourceExtractorProcessor {
    private static final Logger log = LoggerFactory.getLogger(ResourceExtractorProcessor.class);
    private static final Map<String, ResourceExtractorProcessor> RUNNING_INDEXER = new HashMap<>();
    private long targetInstanceId;
    private int harvestNumber;
    private File directory;
    private Map<String, NetworkMapNode> urls = new Hashtable<>();

    private BDBNetworkMap db;

    private Set<SeedHistory> seeds;

    private String logsDir; //log dir
    private String reportsDir; //report dir

    private VisualizationProgressBar progressBar = new VisualizationProgressBar("INDEXING");

    public ResourceExtractorProcessor(File directory, BDBNetworkMap db, long targetInstanceId, int harvestNumber, Set<SeedHistory> seeds, VisualizationManager visualizationManager) throws IOException {
        this.directory = directory;
        this.db = db;
        this.targetInstanceId = targetInstanceId;
        this.harvestNumber = harvestNumber;
        this.seeds = seeds;

        String baseDir = visualizationManager.getBaseDir();
        this.logsDir = baseDir + File.separator + this.targetInstanceId + File.separator + visualizationManager.getLogsDir() + File.separator + HarvestResult.DIR_LOGS_EXT + File.separator + HarvestResult.DIR_LOGS_INDEX + File.separator + this.harvestNumber;
        this.reportsDir = baseDir + File.separator + this.targetInstanceId + File.separator + visualizationManager.getReportsDir() + File.separator + HarvestResult.DIR_LOGS_EXT + File.separator + HarvestResult.DIR_LOGS_INDEX + File.separator + this.harvestNumber;

        String key = getKey(this.targetInstanceId, this.harvestNumber);
        RUNNING_INDEXER.put(key, this);
    }

    private static String getKey(long targetInstanceId, int harvestNumber) {
        return String.format("KEY_%d_%d", targetInstanceId, harvestNumber);
    }

    public static VisualizationProgressBar getProgress(long targetInstanceId, int harvestNumber) {
        String key = getKey(targetInstanceId, harvestNumber);
        ResourceExtractorProcessor indexer = RUNNING_INDEXER.get(key);
        if (indexer != null) {
            return indexer.progressBar;
        }
        return null;
    }

    public void indexFiles() throws IOException {
        List<File> fileList = VisualizationCoordinator.grepWarcFiles(directory);
        if (fileList == null || fileList.size() == 0) {
            log.error("Could not find any archive files in directory: {}", directory.getAbsolutePath());
            return;
        }

        VisualizationProgressBar.ProgressItem progressItemStat = progressBar.getProgressItem("STAT");
        for (File f : fileList) {
            if (!isWarcFormat(f.getName())) {
                continue;
            }
            VisualizationProgressBar.ProgressItem progressItem = progressBar.getProgressItem(f.getName());
            progressItem.setMaxLength(f.length());
            progressItemStat.setMaxLength(progressItemStat.getMaxLength() + f.length());
        }

        log.debug(progressBar.toString());

        ResourceExtractor extractor = new ResourceExtractorWarc(this.urls, this.seeds);
        extractor.init(this.logsDir, this.reportsDir, this.progressBar);
        for (File f : fileList) {
            if (!isWarcFormat(f.getName())) {
                extractor.writeLog("Skipped unknown file: " + f.getName());
                continue;
            }
            indexFile(f, extractor);
            VisualizationProgressBar.ProgressItem progressItem = progressBar.getProgressItem(f.getName());
            progressItem.setCurLength(progressItem.getMaxLength()); //Set all finished
        }

        this.statAndSave(extractor);
        progressItemStat.setCurLength(progressItemStat.getMaxLength());//Set all finished
        extractor.writeReport();
        extractor.close();
    }

    public void indexFile(File archiveFile, ResourceExtractor extractor) {
        log.info("Indexing file: {}", archiveFile.getAbsolutePath());
        ArchiveReader reader = null;
        try {
            reader = ArchiveReaderFactory.get(archiveFile);
        } catch (IOException e) {
            String err = "Failed to open archive file: " + archiveFile.getName() + " with exception: " + e.getMessage();
            log.error(err);
            extractor.writeLog(err);
            return;
        }

        extractor.writeLog("Start indexing from: " + archiveFile.getName());

        try {
            extractor.extract(reader, archiveFile.getName());
        } catch (IOException e) {
            String err = "Failed to open index file: " + archiveFile.getName() + " with exception: " + e.getMessage();
            log.error(err);
            extractor.writeLog(err);
            return;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        extractor.writeLog("End indexing from: " + archiveFile.getName());
    }

    private void statAndSave(ResourceExtractor extractor) {
        AtomicLong domainIdGenerator = new AtomicLong();
        NetworkMapDomainManager domainManager = new NetworkMapDomainManager();

        //Statistic by domain
        NetworkMapDomain rootDomainNode = new NetworkMapDomain(NetworkMapDomain.DOMAIN_NAME_LEVEL_ROOT, 0);
        rootDomainNode.addChildren(this.urls.values(), domainIdGenerator, domainManager);
        extractor.writeLog("rootDomainNode.addChildren: group by domain");
        rootDomainNode.addStatData(this.urls.values());
        extractor.writeLog("rootDomainNode.addStatData: accumulate by content type and status code");

        //Process parent relationship, outlinks and domain's outlink
        this.urls.values().forEach(node -> {
            /**
             * if url u-->v then domain du->dv, DU->DV, du->DV, DU->dv
             */

            NetworkMapDomain domainNodeHigh = domainManager.getHighDomain(node);
            NetworkMapDomain domainNodeLower = domainManager.getLowerDomain(node);
            if (node.isSeed()) {
                domainNodeHigh.setSeed(true);
                domainNodeLower.setSeed(true);
            }

            String viaUrl = node.getViaUrl();
            if (viaUrl == null || !this.urls.containsKey(viaUrl)) {
                node.setParentId(-1);
            } else {
                NetworkMapNode parentNode = this.urls.get(viaUrl);
                parentNode.addOutlink(node);

                NetworkMapDomain parentDomainNodeHigh = domainManager.getHighDomain(parentNode);
                NetworkMapDomain parentDomainNodeLower = domainManager.getLowerDomain(parentNode);

                node.setParentId(parentNode.getId());

                parentDomainNodeHigh.addOutlink(domainNodeHigh.getId());
                parentDomainNodeHigh.addOutlink(domainNodeLower.getId());
                parentDomainNodeLower.addOutlink(domainNodeHigh.getId());
                parentDomainNodeLower.addOutlink(domainNodeLower.getId());
            }
        });
//        db.put(BDBNetworkMap.PATH_METADATA_DOMAIN_NAME, statDomainMap.keySet());
        db.put(BDBNetworkMap.PATH_GROUP_BY_DOMAIN, rootDomainNode);
        extractor.writeLog("Finished storing domain nodes");

        //Process and save url
        List<Long> rootUrls = new ArrayList<>();
        List<Long> malformedUrls = new ArrayList<>();
        this.urls.values().forEach(e -> {
            db.put(e.getId(), e);             //Indexed by ID, ID->NODE
            db.put(e.getUrl(), e.getId()); //Indexed by URL, URL->ID->NODE

            if (e.isSeed() || e.getParentId() <= 0) {
                rootUrls.add(e.getId());
            }

            if (!e.isFinished()) {
                malformedUrls.add(e.getId());
            }
        });
        db.put(BDBNetworkMap.PATH_ROOT_URLS, rootUrls);
        rootUrls.clear();
        db.put(BDBNetworkMap.PATH_MALFORMED_URLS, malformedUrls);
        malformedUrls.clear();

        extractor.writeLog("Finished storing url nodes");
    }

    private boolean isWarcFormat(String name) {
        return name.toLowerCase().endsWith(WARCConstants.DOT_WARC_FILE_EXTENSION) ||
                name.toLowerCase().endsWith(WARCConstants.DOT_COMPRESSED_WARC_FILE_EXTENSION);
    }

    public void clear() {
        this.urls.values().forEach(NetworkMapNode::clear);
        this.urls.clear();
        this.progressBar.clear();

        String key = getKey(this.targetInstanceId, this.harvestNumber);
        RUNNING_INDEXER.remove(key);
    }
}