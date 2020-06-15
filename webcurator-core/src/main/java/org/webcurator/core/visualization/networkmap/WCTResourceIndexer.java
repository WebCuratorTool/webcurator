package org.webcurator.core.visualization.networkmap;

import org.springframework.context.ApplicationContext;
import org.archive.io.*;
import org.archive.io.warc.WARCConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.harvester.coordinator.HarvestCoordinatorPaths;
import org.webcurator.core.visualization.VisualizationManager;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapDomain;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapDomainManager;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNode;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.core.store.WCTIndexer;
import org.webcurator.core.util.ApplicationContextFactory;
import org.webcurator.domain.model.core.ArcHarvestFileDTO;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.HarvestResultDTO;
import org.webcurator.domain.model.core.SeedHistory;
import org.webcurator.domain.model.dto.SeedHistoryDTO;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("all")
public class WCTResourceIndexer {
    private static final Logger log = LoggerFactory.getLogger(WCTResourceIndexer.class);
    private long targetInstanceId;
    private int harvestNumber;
    private File directory;
    private Map<String, NetworkMapNode> urls = new Hashtable<>();

    private BDBNetworkMap db;

    private Set<SeedHistory> seeds;

    private String logsDir; //log dir
    private String reportsDir; //report dir

    public WCTResourceIndexer() {

    }

    public WCTResourceIndexer(File directory, BDBNetworkMap db, long targetInstanceId, int harvestNumber) throws IOException {
        this.directory = directory;
        this.db = db;
        this.targetInstanceId = targetInstanceId;
        this.harvestNumber = harvestNumber;
        init(targetInstanceId, harvestNumber);
    }

    public void init(long targetInstanceId, int harvestNumber) {
        ApplicationContext context = ApplicationContextFactory.getApplicationContext();
        if (context != null) {
            AbstractRestClient client = ApplicationContextFactory.getApplicationContext().getBean(WCTIndexer.class);
            RestTemplateBuilder restTemplateBuilder = ApplicationContextFactory.getApplicationContext().getBean(RestTemplateBuilder.class);
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(client.getUrl(HarvestCoordinatorPaths.TARGET_INSTANCE_HISTORY_SEED))
                    .queryParam("targetInstanceOid", targetInstanceId)
                    .queryParam("harvestNumber", harvestNumber);
            RestTemplate restTemplate = restTemplateBuilder.build();
            URI uri = uriComponentsBuilder.build().toUri();
            ResponseEntity<SeedHistoryDTO> seedHistoryDTO = restTemplate.getForEntity(uri, SeedHistoryDTO.class);
            this.seeds = Objects.requireNonNull(seedHistoryDTO.getBody()).getSeeds();

            VisualizationManager visualizationManager = ApplicationContextFactory.getApplicationContext().getBean(VisualizationManager.class);
            String baseDir = visualizationManager.getBaseDir();
            this.logsDir = baseDir + File.separator + this.targetInstanceId + File.separator + visualizationManager.getLogsDir() + File.separator + HarvestResult.DIR_LOGS_EXT + File.separator + HarvestResult.DIR_LOGS_INDEX + File.separator + this.harvestNumber;
            this.reportsDir = baseDir + File.separator + this.targetInstanceId + File.separator + visualizationManager.getReportsDir() + File.separator + HarvestResult.DIR_LOGS_EXT + File.separator + HarvestResult.DIR_LOGS_INDEX + File.separator + this.harvestNumber;
        } else {
            this.seeds = new HashSet<>();
            this.logsDir = directory.getAbsolutePath() + File.separator + "temp_logs";
            this.reportsDir = directory.getAbsolutePath() + File.separator + "temp_reports";
            log.warn("Initial indexer for debug mode.");
        }
    }

    public List<ArcHarvestFileDTO> indexFiles() throws IOException {
        List<ArcHarvestFileDTO> arcHarvestFileDTOList = new ArrayList();

        File[] fileList = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.toLowerCase().endsWith(".arc") ||
                        name.toLowerCase().endsWith(".arc.gz") ||
                        name.toLowerCase().endsWith(".warc") ||
                        name.toLowerCase().endsWith(".warc.gz"));
            }
        });

        if (fileList == null) {
            log.error("Could not find any archive files in directory: {}", directory.getAbsolutePath());
            return arcHarvestFileDTOList;
        }

        ResourceExtractor extractor = new ResourceExtractorWarc(this.urls, this.seeds);
        extractor.init(this.logsDir, this.reportsDir);
        for (File f : fileList) {
            if (!isWarcFormat(f.getName())) {
                extractor.writeLog("Skipped unknown file: " + f.getName());
                continue;
            }
            ArcHarvestFileDTO dto = indexFile(f, extractor);
            if (dto != null) {
                HarvestResultDTO harvestResult = new HarvestResultDTO();
                harvestResult.setTargetInstanceOid(this.targetInstanceId);
                harvestResult.setHarvestNumber(this.harvestNumber);
                dto.setHarvestResult(harvestResult);
                arcHarvestFileDTOList.add(dto);
            }
        }

        this.statAndSave(extractor);
        this.clear();
        extractor.writeReport();
        extractor.close();
        return arcHarvestFileDTOList;
    }

    public ArcHarvestFileDTO indexFile(File archiveFile, ResourceExtractor extractor) {
        log.info("Indexing file: {}", archiveFile.getAbsolutePath());
        ArchiveReader reader = null;
        try {
            reader = ArchiveReaderFactory.get(archiveFile);
        } catch (IOException e) {
            String err = "Failed to open archive file: " + archiveFile.getName() + " with exception: " + e.getMessage();
            log.error(err);
            extractor.writeLog(err);
            return null;
        }

        extractor.writeLog("Start indexing from: " + archiveFile.getName());

        ArcHarvestFileDTO arcHarvestFileDTO = new ArcHarvestFileDTO();
        arcHarvestFileDTO.setBaseDir(archiveFile.getPath());
        arcHarvestFileDTO.setName(archiveFile.getName());
        arcHarvestFileDTO.setCompressed(reader.isCompressed());

        try {
            extractor.extract(reader);
        } catch (IOException e) {
            String err = "Failed to open index file: " + archiveFile.getName() + " with exception: " + e.getMessage();
            log.error(err);
            extractor.writeLog(err);
            return null;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        extractor.writeLog("End indexing from: " + archiveFile.getName());
        return arcHarvestFileDTO;
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
            db.put(e.getId(), e);

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
    }
}