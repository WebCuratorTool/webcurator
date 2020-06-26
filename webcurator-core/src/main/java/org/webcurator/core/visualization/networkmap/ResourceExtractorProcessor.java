package org.webcurator.core.visualization.networkmap;

import org.archive.io.*;
import org.archive.io.warc.WARCConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.visualization.VisualizationAbstractProcessor;
import org.webcurator.core.visualization.VisualizationCoordinator;
import org.webcurator.core.visualization.VisualizationManager;
import org.webcurator.core.visualization.VisualizationProgressBar;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.extractor.ResourceExtractor;
import org.webcurator.core.visualization.networkmap.extractor.ResourceExtractorWarc;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapDomain;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapDomainManager;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNode;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.SeedHistoryDTO;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("all")
public class ResourceExtractorProcessor extends VisualizationAbstractProcessor {
    private static final Logger log = LoggerFactory.getLogger(ResourceExtractorProcessor.class);
    private File directory;
    private Map<String, NetworkMapNode> urls = new Hashtable<>();
    private BDBNetworkMap db;
    private Set<SeedHistoryDTO> seeds;
    private ResourceExtractor extractor;

    public ResourceExtractorProcessor(BDBNetworkMapPool pool, long targetInstanceId, int harvestResultNumber, Set<SeedHistoryDTO> seeds, VisualizationManager visualizationManager) throws DigitalAssetStoreException {
        super(visualizationManager, targetInstanceId, harvestResultNumber);
        this.directory = new File(visualizationManager.getBaseDir(), targetInstanceId + File.separator + harvestResultNumber);
        this.db = pool.createInstance(targetInstanceId, harvestResultNumber);
        this.seeds = seeds;
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

        extractor = new ResourceExtractorWarc(this.urls, this.seeds);
        extractor.init(this.logsDir, this.reportsDir, this.progressBar);
        for (File f : fileList) {
            if (!isWarcFormat(f.getName())) {
                extractor.writeLog("Skipped unknown file: " + f.getName());
                continue;
            }
            indexFile(f);
            VisualizationProgressBar.ProgressItem progressItem = progressBar.getProgressItem(f.getName());
            progressItem.setCurLength(progressItem.getMaxLength()); //Set all finished
        }

        this.statAndSave(extractor);
        progressItemStat.setCurLength(progressItemStat.getMaxLength());//Set all finished
        extractor.writeReport();
        extractor.close();
    }

    public void indexFile(File archiveFile) {
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
        if (!running) {
            return;
        }

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
            if (this.running) {
                // if url u-->v then domain du->dv, DU->DV, du->DV, DU->dv
                NetworkMapDomain domainNodeHigh = domainManager.getHighDomain(node);
                NetworkMapDomain domainNodeLower = domainManager.getLowerDomain(node);
                if (node.isSeed() && (node.getSeedType() == NetworkMapNode.SEED_TYPE_PRIMARY || node.getSeedType() == NetworkMapNode.SEED_TYPE_SECONDARY)) {
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
            }
        });
        db.put(BDBNetworkMap.PATH_GROUP_BY_DOMAIN, rootDomainNode);
        extractor.writeLog("Finished storing domain nodes");

        //Process and save url
        List<Long> rootUrls = new ArrayList<>();
        List<Long> malformedUrls = new ArrayList<>();
        this.urls.values().forEach(e -> {
            if (this.running) {
                db.put(e.getId(), e);             //Indexed by ID, ID->NODE
                db.put(e.getUrl(), e.getId()); //Indexed by URL, URL->ID->NODE

                if (e.isSeed() || e.getParentId() <= 0) {
                    rootUrls.add(e.getId());
                }

                if (!e.isFinished()) {
                    malformedUrls.add(e.getId());
                }
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
    }

    @Override
    protected String getProcessorStage() {
        return HarvestResult.PATCH_STAGE_TYPE_INDEXING;
    }

    @Override
    public void processInternal() {
        try {
            indexFiles();
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            clear();
        }
    }

    @Override
    protected void terminateInternal() {
        if (extractor != null) {
            extractor.stop();
        }
    }

    @Override
    public void deleteInternal() {
        //delete indexing data
        this.delete(this.directory.getAbsolutePath(), "_resource");
    }
}