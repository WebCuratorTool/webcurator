package org.webcurator.core.visualization.networkmap.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleepycat.je.Transaction;
import org.apache.commons.lang.StringUtils;
import org.archive.io.*;
import org.archive.io.warc.WARCConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.util.PatchUtil;
import org.webcurator.core.visualization.VisualizationAbstractProcessor;
import org.webcurator.core.visualization.VisualizationProgressBar;
import org.webcurator.core.visualization.VisualizationStatisticItem;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.bdb.BDBRepoHolder;
import org.webcurator.core.visualization.networkmap.metadata.*;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.SeedHistoryDTO;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("all")
public abstract class IndexProcessor extends VisualizationAbstractProcessor {
    protected static final Logger log = LoggerFactory.getLogger(IndexProcessor.class);
    protected static final int MAX_URL_LENGTH = 1020;

    protected Map<String, NetworkMapNodeUrlDTO> urls = new Hashtable<>();
    protected BDBRepoHolder db;
    protected AtomicLong atomicIdGeneratorUrl = new AtomicLong(0);
    protected AtomicLong atomicIdGeneratorFolder = new AtomicLong(0);
    protected Map<String, Boolean> seeds = new HashMap<>();
    protected BDBNetworkMapPool pool;
    protected List<String> fileWarcNames = new ArrayList<>();

    public IndexProcessor(BDBNetworkMapPool pool, long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException {
        super(targetInstanceId, harvestResultNumber);
        this.pool = pool;
        this.flag = "IDX";
        this.reportTitle = StatisticItem.getPrintTitle();
        this.state = HarvestResult.STATE_INDEXING;
        this.db = pool.createInstance(targetInstanceId, harvestResultNumber);
    }

    @Override
    protected void initInternal() throws IOException {
        Set<SeedHistoryDTO> seedsHistory = wctClient.getSeedUrls(targetInstanceId, harvestResultNumber);
        seedsHistory.forEach(seed -> {
            this.seeds.put(seed.getSeed(), seed.isPrimary());
        });

        NetworkMapApplyCommand cmd = new NetworkMapApplyCommand();
        cmd.setTargetInstanceId(this.targetInstanceId);
        cmd.setHarvestResultNumber(this.harvestResultNumber);
        cmd.setNewHarvestResultNumber(this.harvestResultNumber);
        PatchUtil.indexer.savePatchJob(this.baseDir, cmd);
    }

    public void indexFile(ArchiveReader reader, String fileName) throws IOException {
        log.info("Start to index file: {}", fileName);
        this.writeLog("Start indexing from: " + fileName);

        StatisticItem statisticItem = new StatisticItem();
        statisticItem.setFromFileName(fileName);
        statisticItems.add(statisticItem);

        VisualizationProgressBar.ProgressItem progressItem = this.progressBar.getProgressItem(fileName);

        preProcess();
        for (ArchiveRecord record : reader) {
            if (this.status == HarvestResult.STATUS_TERMINATED) {
                log.info("Terminated when indexing");
                break;
            }

            this.tryBlock();

            try {
                extractRecord(record, fileName);
            } catch (Exception e) {
                log.warn("Failed to extract record", e);
                break;
            } finally {
                record.close();
            }
            progressItem.setCurLength(record.getHeader().getOffset());
//            log.debug("Extracting, results.size:{}", urls.size());
//            log.debug(progressItem.toString());
            writeLog(progressItem.toString());

            statisticItem.increaseSucceedRecords();
        }
        postProcess();

        this.writeLog("End indexing from: " + fileName);
        log.info("End index file: {}", fileName);
    }

    abstract protected void preProcess();

    abstract protected void postProcess();

    abstract protected void extractRecord(ArchiveRecord rec, String fileName) throws IOException;

    public void statAndSave() {
        this.tryBlock();
        NetworkMapAccessPropertyEntity accProp = new NetworkMapAccessPropertyEntity();

        AtomicLong domainIdGenerator = new AtomicLong();
        NetworkMapDomainManager domainManager = new NetworkMapDomainManager();

        //Filter out empty url nodes
        List<String> emptyUrlKeys = new ArrayList<>();
        this.urls.forEach((k, v) -> {
            if (StringUtils.isEmpty(v.getTopDomain()) || StringUtils.isEmpty(v.getDomain()) || StringUtils.isEmpty(v.getUrl())) {
                emptyUrlKeys.add(k);
            }
        });
        emptyUrlKeys.forEach(k -> {
            this.urls.remove(k);
            log.warn("Ignored the unknown url: {}", k);
        });
        emptyUrlKeys.clear();

        //Statistic by domain
        NetworkMapDomain rootDomainNode = new NetworkMapDomain(NetworkMapDomain.DOMAIN_NAME_LEVEL_ROOT, 0);
        rootDomainNode.addChildren(this.urls.values(), domainIdGenerator, domainManager);
        this.writeLog("rootDomainNode.addChildren: group by domain");
        rootDomainNode.addStatData(this.urls.values());
        this.writeLog("rootDomainNode.addStatData: accumulate by content type and status code");

        //Process parent relationship, outlinks and domain's outlink
        this.urls.values().forEach(node -> {
            this.tryBlock();

            // if url u-->v then domain du->dv, DU->DV, du->DV, DU->dv
            NetworkMapDomain domainNodeHigh = domainManager.getHighDomain(node);
            NetworkMapDomain domainNodeLower = domainManager.getLowerDomain(node);
            node.setDomainId(domainNodeLower.getId());
            if (node.isSeed() && (node.getSeedType() == NetworkMapNodeUrlDTO.SEED_TYPE_PRIMARY || node.getSeedType() == NetworkMapNodeUrlDTO.SEED_TYPE_SECONDARY)) {
                domainNodeHigh.setSeed(true);
                domainNodeLower.setSeed(true);
            }

            String viaUrl = node.getViaUrl();
            if (viaUrl == null || !this.urls.containsKey(viaUrl)) {
                node.setParentId(-1);
            } else {
                NetworkMapNodeUrlDTO parentNode = this.urls.get(viaUrl);
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
        String strRootDomain = getJson(rootDomainNode);
        log.debug(strRootDomain);
        accProp.setRootDomain(strRootDomain);
        this.writeLog("Finished storing domain nodes");
        rootDomainNode.clear();


        //Process and save url
        List<Long> rootUrls = new ArrayList<>();
        List<Long> malformedUrls = new ArrayList<>();
        //Using the transaction to improve the performance of bulk insert
        Transaction txn = db.env.beginTransaction(null, null);
        AtomicInteger batch_num = new AtomicInteger(0);
        for (NetworkMapNodeUrlDTO e : this.urls.values()) {
            this.tryBlock();
            NetworkMapNodeUrlEntity urlEntity = new NetworkMapNodeUrlEntity();
            urlEntity.copy(e);
            db.tblUrl.primaryId.putNoReturn(txn, urlEntity);

            if (e.isSeed() || e.getParentId() <= 0) {
                rootUrls.add(e.getId());
            }
            if (!e.isFinished()) {
                malformedUrls.add(e.getId());
            }

            if (batch_num.incrementAndGet() % 1000 == 0) {
                txn.commit();
                txn = db.env.beginTransaction(null, null);
                log.debug("Saved: {} rootUrls={}, malformedUrls={}", batch_num.get(), rootUrls.size(), malformedUrls.size());
            }
            urlEntity.clear();
        }
        txn.commit();
        this.urls.values().forEach(NetworkMapNodeUrlDTO::clear);
        this.urls.clear();

        //Create the folder treeview, permenit the paths and set parentPathId for all networkmap nodes.
        long rootFolderNodeId = FolderTreeViewGenerator.classifyTreePaths(db);
        accProp.setRootFolderNode(rootFolderNodeId);
        log.debug("rootTreeNode: {}", accProp.getRootFolderNode());

        accProp.setSeedUrlIDs(rootUrls);
        accProp.setMalformedUrlIDs(malformedUrls);
        db.insertAccProp(accProp);
        this.writeLog("Finished storing url nodes");
        rootUrls.clear();
        malformedUrls.clear();

        pool.shutdownRepo(db);
    }

    private boolean isWarcFormat(String name) {
        return name.toLowerCase().endsWith(WARCConstants.DOT_WARC_FILE_EXTENSION) ||
                name.toLowerCase().endsWith(WARCConstants.DOT_COMPRESSED_WARC_FILE_EXTENSION);
    }

    public void clear() {
        this.urls.values().forEach(NetworkMapNodeUrlDTO::clear);
        this.urls.clear();
    }


    @Override
    protected String getProcessorStage() {
        return HarvestResult.PATCH_STAGE_TYPE_INDEXING;
    }

    @Override
    public void processInternal() throws Exception {
        try {
            File directory = new File(this.baseDir, targetInstanceId + File.separator + harvestResultNumber);

            List<File> fileList = PatchUtil.listWarcFiles(directory);
            if (fileList == null || fileList.size() == 0) {
                log.error("Could not find any archive files in directory: {}", directory.getAbsolutePath());
                return;
            }
            fileList.sort(new Comparator<File>() {
                @Override
                public int compare(File f0, File f1) {
                    return f0.getName().compareTo(f1.getName());
                }
            });

            VisualizationProgressBar.ProgressItem progressItemStat = progressBar.getProgressItem("STAT");
            for (File f : fileList) {
                if (this.status == HarvestResult.STATUS_TERMINATED) {
                    log.info("Terminated when indexing");
                    break;
                }

                if (!isWarcFormat(f.getName())) {
                    continue;
                }
                this.fileWarcNames.add(f.getName());

                VisualizationProgressBar.ProgressItem progressItem = progressBar.getProgressItem(f.getName());
                progressItem.setMaxLength(f.length());
                progressItemStat.setMaxLength(progressItemStat.getMaxLength() + f.length());
            }

            log.debug(progressBar.toString());
            for (File f : fileList) {
                if (this.status == HarvestResult.STATUS_TERMINATED) {
                    log.info("Terminated when indexing");
                    break;
                }

                if (!isWarcFormat(f.getName())) {
                    this.writeLog("Skipped unknown file: " + f.getName());
                    continue;
                }
                ArchiveReader reader = null;
                try {
                    reader = ArchiveReaderFactory.get(f);
                    indexFile(reader, f.getName());
                } catch (Exception e) {
                    String err = "Failed to extract archive file: " + f.getAbsolutePath() + " with exception: " + e.getMessage();
                    log.error(err, e);
                    this.writeLog(err);
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }

                VisualizationProgressBar.ProgressItem progressItem = progressBar.getProgressItem(f.getName());
                progressItem.setCurLength(progressItem.getMaxLength()); //Set all finished
            }

            log.info("Finished url extracting of all the harvest files: {} {}", this.targetInstanceId, this.harvestResultNumber);

            this.statAndSave();

            this.writeReport();
            progressItemStat.setCurLength(progressItemStat.getMaxLength());//Set all finished

            this.status = HarvestResult.STATUS_FINISHED;
        } finally {
            this.urls.values().forEach(NetworkMapNodeUrlDTO::clear);
            this.urls.clear();
            this.pool.shutdownRepo(db);
        }
    }

    @Override
    protected void terminateInternal() {
        this.clear();
    }

    @Override
    public void deleteInternal() {
        //delete indexing data
        File directory = new File(this.baseDir, targetInstanceId + File.separator + harvestResultNumber);
        this.delete(directory.getAbsolutePath(), "_resource");
    }

    /**
     * borrowed(copied) from org.archive.io.arc.ARCRecord...
     *
     * @param bytes Array of bytes to examine for an EOL.
     * @return Count of end-of-line characters or zero if none.
     */
    public int getEolCharsCount(byte[] bytes) {
        int count = 0;
        if (bytes != null && bytes.length >= 1 &&
                bytes[bytes.length - 1] == '\n') {
            count++;
            if (bytes.length >= 2 && bytes[bytes.length - 2] == '\r') {
                count++;
            }
        }
        return count;
    }

    public String getJson(Object obj) {
        String json = "{}";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            json = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

    public Map<String, NetworkMapNodeUrlDTO> getUrls() {
        return urls;
    }

    public void setUrls(Map<String, NetworkMapNodeUrlDTO> urls) {
        this.urls = urls;
    }

    static class StatisticItem implements VisualizationStatisticItem {
        private String fromFileName = null;
        private long fromFileLength = -1;
        private int totalRecords = 0;
        private int failedRecords = 0;
        private int succeedRecords = 0;

        public static String getPrintTitle() {
            return "FromFileName\tFromFileLength\tTotalRecords\tSucceedRecords\tFailedRecords";
        }

        @Override
        public String getPrintContent() {
            String pFromFileName = this.fromFileName == null ? "--" : this.fromFileName;
            String pFromFileLength = this.fromFileLength < 0 ? "--" : Long.toString(this.fromFileLength);

            return String.format("%s\t%s\t%d\t%d\t%d",
                    pFromFileName,
                    pFromFileLength,
                    this.totalRecords,
                    this.succeedRecords,
                    this.failedRecords);
        }

        public void setFromFileName(String fromFileName) {
            this.fromFileName = fromFileName;
        }

        public void setFromFileLength(long fromFileLength) {
            this.fromFileLength = fromFileLength;
        }

        public void increaseSucceedRecords() {
            this.increaseSucceedRecords(1);
        }

        public void increaseSucceedRecords(int num) {
            this.succeedRecords += num;
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