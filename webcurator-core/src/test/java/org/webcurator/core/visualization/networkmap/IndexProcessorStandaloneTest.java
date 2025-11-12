package org.webcurator.core.visualization.networkmap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleepycat.je.Transaction;
import org.apache.commons.io.FileUtils;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.warc.WARCConstants;
import org.archive.io.warc.WARCRecord;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.webcurator.core.coordinator.WctCoordinatorClient;
import org.webcurator.core.util.PatchUtil;
import org.webcurator.core.visualization.*;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.bdb.BDBRepoHolder;
import org.webcurator.core.visualization.networkmap.metadata.*;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessor;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessorWarc;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClientLocal;
import org.webcurator.core.visualization.networkmap.service.NetworkMapService;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.SeedHistoryDTO;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;

public class IndexProcessorStandaloneTest {
    protected static final Logger log = LoggerFactory.getLogger(IndexProcessorStandaloneTest.class);
    protected static String baseDir = "/usr/local/wct/store";
    protected static String baseLogDir = "logs";
    protected static String baseReportDir = "reports";
    protected static VisualizationDirectoryManager directoryManager = null;
    protected static BDBNetworkMapPool pool = null;
    protected static NetworkMapService networkMapClient = null;

    protected static long targetInstanceId = 1;
    protected static int harvestResultNumber = 1;
    protected static int newHarvestResultNumber = 2;
    protected static Set<SeedHistoryDTO> seeds = new HashSet<>();
    protected static VisualizationProcessorManager processorManager;
    protected static WctCoordinatorClient wctClient;
    private static IndexProcessor indexer;

    @BeforeClass
    public static void initTest() throws Exception {
        directoryManager = new VisualizationDirectoryManager(baseDir, baseLogDir, baseReportDir);
        pool = new BDBNetworkMapPool(baseDir, "dbVersion");

        SeedHistoryDTO seedHistoryPrimary = new SeedHistoryDTO(1, "http://www.google.com/", targetInstanceId, true);
        SeedHistoryDTO seedHistorySecondary = new SeedHistoryDTO(2, "http://www.baidu.com/", targetInstanceId, false);
        seeds.add(seedHistoryPrimary);
        seeds.add(seedHistorySecondary);
        //wctClient = new WctCoordinatorClient("http", "localhost", 8080, new RestTemplateBuilder());
        wctClient = mock(WctCoordinatorClient.class);

        processorManager = new VisualizationProcessorManager(directoryManager, wctClient, 3);
        networkMapClient = new NetworkMapClientLocal(pool, processorManager);

        NetworkMapDomainSuffix suffixParser = new NetworkMapDomainSuffix();
        Resource resource = new ClassPathResource("public_suffix_list.dat");

        try {
            suffixParser.init(resource.getFile());
        } catch (Exception e) {
            log.error("Load domain suffix file failed.", e);
        }
        NetworkMapNodeUrlDTO.setTopDomainParse(suffixParser);

        String dbPath = pool.getDbPath(targetInstanceId, harvestResultNumber);
        File f = new File(dbPath);
        f.deleteOnExit(); //Clear the existing db

        indexer = new IndexProcessorWarc(pool, targetInstanceId, harvestResultNumber);
        indexer.init(directoryManager, wctClient, networkMapClient);
    }

    @Ignore
    @Test
    public void testExtractMalformedWarcFile() throws IOException {
        File warcFolder = new File(baseDir, targetInstanceId + File.separator + harvestResultNumber);
        File f = new File(warcFolder, "NLNZ-20220725121845654-00568-22894~appserv17~8443.warc");
//        File f= new File(warcFolder,"NLNZ-20220725141841331-00569-22894~appserv17~8443.warc");
        ArchiveReader reader = null;
        try {
            reader = ArchiveReaderFactory.get(f);
            indexer.indexFile(reader, f.getName());
        } catch (Exception e) {
            String err = "Failed to extract archive file: " + f.getAbsolutePath() + " with exception: " + e.getMessage();
            log.error(err, e);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    @Ignore
    @Test
    public void testProcessInternal() throws Exception {
        indexer.processInternal();
    }

    @Ignore
    @Test
    public void testLoadAllUrlFromJson() {
        List<NetworkMapNodeUrlEntity> urlEntityList = loadAllUrlFromJson();
        log.debug("{} UrlEntities found", urlEntityList.size());
        urlEntityList.forEach(NetworkMapNodeUrlEntity::clear);
        urlEntityList.clear();
    }

    @Ignore
    @Test
    public void testSaveAllUrlToOneJsonFile() throws IOException {
        File directory = new File(baseDir, targetInstanceId + File.separator + harvestResultNumber);
        File jsonFile = new File(directory, "all_urls.json");
        List<NetworkMapNodeUrlEntity> urlEntityList = loadAllUrlFromJson();
        String urlJsonString = indexer.getJson(urlEntityList);
        FileUtils.write(jsonFile, urlJsonString);
    }

    @Ignore
    @Test
    public void testSaveAllUrlFromJsonToDB() {
        List<NetworkMapNodeUrlEntity> urlEntityList = loadAllUrlFromJson();

        BDBRepoHolder db = pool.createInstance(targetInstanceId, harvestResultNumber);
        long num = 1;
        AtomicReference<Transaction> txn = new AtomicReference<>(db.env.beginTransaction(null, null));
        for (NetworkMapNodeUrlEntity urlEntity : urlEntityList) {
            db.tblUrl.primaryId.putNoReturn(txn.get(), urlEntity);
            if (num % 10000 == 0) {
                log.debug("Saved: {} {}", num, urlEntity.getUrl());
                txn.get().commit();
                txn.set(db.env.beginTransaction(null, null));
            }
            num++;
        }
        txn.get().commit();
        pool.shutdownRepo(db);
        log.debug("{} UrlEntities saved", urlEntityList.size());
        urlEntityList.forEach(NetworkMapNodeUrlEntity::clear);
        urlEntityList.clear();
    }

    @Ignore
    @Test
    public void testStatAndSave() {
        List<NetworkMapNodeUrlEntity> urlEntityList = loadAllUrlFromJson();
        Map<String, NetworkMapNodeUrlDTO> urls = new HashMap<>();
        long num = 0;
        for (NetworkMapNodeUrlEntity urlEntity : urlEntityList) {
            NetworkMapNodeUrlDTO dto = new NetworkMapNodeUrlDTO();
            dto.copy(urlEntity);
            dto.setUrlAndDomain(urlEntity.getUrl());
            urls.put(dto.getUrl(), dto);
            num++;
            if (num % 10000 == 0) {
                log.debug("To DTO: {} {}", num, urlEntity.getUrl());
            }

        }
        indexer.setUrls(urls);
        indexer.statAndSave();
    }


    public List<NetworkMapNodeUrlEntity> loadAllUrlFromJson() {
        File directory = new File(baseDir, targetInstanceId + File.separator + harvestResultNumber);

        File[] fileAry = directory.listFiles();
        if (fileAry == null || fileAry.length == 0) {
            log.error("Could not find any archive files in directory: {}", directory.getAbsolutePath());
            return null;
        }
        List<File> fileList = Arrays.asList(fileAry).stream().filter(file -> {
            return file.getName().endsWith(".json");
        }).sorted(new Comparator<File>() {
            @Override
            public int compare(File f0, File f1) {
                return f0.getName().compareTo(f1.getName());
            }
        }).collect(Collectors.toList());

        final List<NetworkMapNodeUrlEntity> urlEntityList = new ArrayList<>();
        fileList.forEach(jsonFile -> {
            try {
                List<String> lines = FileUtils.readLines(jsonFile);
                for (String line : lines) {
                    NetworkMapNodeUrlEntity urlEntity = getNodeEntity(line);
                    urlEntityList.add(urlEntity);
                }
                lines.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.debug("Loaded, {}", jsonFile.getName());
        });
        return urlEntityList;
    }


    @Ignore
    @Test
    public void testInitialIndex() throws Exception {
        indexer.processInternal();

        //To test progress
        VisualizationProgressBar progressBar = indexer.getProgress();
        assert progressBar != null;
        log.debug(progressBar.toString());

        assert progressBar.getStage().equals(HarvestResult.PATCH_STAGE_TYPE_INDEXING);
        assert progressBar.getTargetInstanceId() == targetInstanceId;
        assert progressBar.getHarvestResultNumber() == harvestResultNumber;
        assert progressBar.getState() == HarvestResult.STATE_INDEXING;
        assert progressBar.getStatus() == HarvestResult.STATUS_FINISHED;
        assert progressBar.getProgressPercentage() == 100;

        NetworkMapClientLocal localClient = new NetworkMapClientLocal(pool, processorManager);
        //To test urls has been extracted
        File warcFileFrom = getOneWarcFile();
        assert warcFileFrom != null;
        List<String> listToBePrunedUrl = getRandomUrlsFromWarcFile(warcFileFrom);
        listToBePrunedUrl.forEach(actualUrl -> {
            NetworkMapUrlCommand url = new NetworkMapUrlCommand();
            url.setUrlName(actualUrl);
            NetworkMapResult result = localClient.getUrlByName(targetInstanceId, harvestResultNumber, url);
            assert result != null;
            assert result.getRspCode() == VisualizationConstants.RESP_CODE_SUCCESS;
            assert result.getPayload() != null;

            NetworkMapNodeUrlEntity urlNode = localClient.getNodeEntity(result.getPayload());

            assert urlNode != null;
            assert urlNode.getUrl().equals(actualUrl);

            assert urlNode.getFileName().equals(warcFileFrom.getName());
        });

        //To test domain
        NetworkMapResult domainResult = localClient.getAllDomains(targetInstanceId, harvestResultNumber);
        assert domainResult != null;
        assert domainResult.getRspCode() == VisualizationConstants.RESP_CODE_SUCCESS;
        assert domainResult.getPayload() != null;

        //To test seed urls
        NetworkMapResult seedsResult = localClient.getSeedUrls(targetInstanceId, harvestResultNumber);
        assert seedsResult != null;
        assert seedsResult.getRspCode() == VisualizationConstants.RESP_CODE_SUCCESS;
        assert seedsResult.getPayload() != null;

        List<NetworkMapNodeUrlEntity> indexedSeedUrls = localClient.getArrayListOfNetworkMapNode(seedsResult.getPayload());
        Map<String, NetworkMapNodeUrlEntity> indexedSeedUrlsMap = new HashMap<>();
        indexedSeedUrls.forEach(e -> {
            indexedSeedUrlsMap.put(e.getUrl(), e);
        });
        Set<SeedHistoryDTO> seedsHistory = wctClient.getSeedUrls(targetInstanceId, harvestResultNumber);
        assert indexedSeedUrls.size() >= seedsHistory.size();
        seedsHistory.forEach(seed -> {
            String seedUrl = seed.getSeed();
            int seedType = seed.isPrimary() ? NetworkMapNodeUrlEntity.SEED_TYPE_PRIMARY : NetworkMapNodeUrlEntity.SEED_TYPE_SECONDARY;
            NetworkMapNodeUrlEntity seedNode = indexedSeedUrlsMap.get(seedUrl);

            assert seedNode != null;
            assert seedNode.getParentId() <= 0;
            assert seedNode.isSeed();
            assert seedNode.getSeedType() == seedType;
            assert seedNode.getContentLength() > 0;
            assert seedNode.getContentType() != null;
            assert seedNode.getOutlinks().size() > 0;
            assert seedNode.getOffset() > 0;
        });

        //To test invalid urls
        NetworkMapResult invalidUrlResult = localClient.getMalformedUrls(targetInstanceId, harvestResultNumber);
        assert invalidUrlResult != null;
        assert invalidUrlResult.getRspCode() == VisualizationConstants.RESP_CODE_SUCCESS;
        assert invalidUrlResult.getPayload() != null;

        List<NetworkMapNodeUrlEntity> indexedInvalidUrls = localClient.getArrayListOfNetworkMapNode(invalidUrlResult.getPayload());
        assert indexedInvalidUrls.size() == 0;
    }

    private NetworkMapNodeUrlEntity getNodeEntity(String json) {
        if (json == null) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, NetworkMapNodeUrlEntity.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private File getOneWarcFile() {
        File directory = new File(directoryManager.getBaseDir(), String.format("%d%s%d", targetInstanceId, File.separator, harvestResultNumber));
        List<File> fileList = PatchUtil.listWarcFiles(directory);
        assert fileList.size() > 0;

        return fileList.get(0);
    }

    public boolean isUrlExistInWarcFile(File warcFile, List<String> urls) throws IOException {
        ArchiveReader reader = ArchiveReaderFactory.get(warcFile);
        for (ArchiveRecord rec : reader) {
            String mime = rec.getHeader().getMimetype();
            if (mime != null && mime.equals("text/dns")) {
                continue;
            }

            WARCRecord record = (WARCRecord) rec;
            ArchiveRecordHeader header = record.getHeader();
            if (header.getUrl() == null) {
                continue;
            }

            if (urls.contains(header.getUrl())) {
                return true;
            }
        }

        return false;
    }

    public List<String> getRandomUrlsFromWarcFile(File warcFile) throws IOException {
        int COUNT = 5, N = 0; //N: Current pointer
        String[] candidateUrls = new String[COUNT];
        ArchiveReader reader = ArchiveReaderFactory.get(warcFile);
        for (ArchiveRecord rec : reader) {
            String mime = rec.getHeader().getMimetype();
            if (mime.equals("text/dns")) {
                continue;
            }

            WARCRecord record = (WARCRecord) rec;
            ArchiveRecordHeader header = record.getHeader();
            if (header.getUrl() == null) {
                continue;
            }

            String type = rec.getHeader().getHeaderValue(WARCConstants.HEADER_KEY_TYPE).toString();
            if (!org.archive.format.warc.WARCConstants.WARCRecordType.request.toString().equals(type) &&
                    !org.archive.format.warc.WARCConstants.WARCRecordType.response.toString().equals(type) &&
                    !org.archive.format.warc.WARCConstants.WARCRecordType.metadata.toString().equals(type)) {
                continue;
            }

            String url = header.getUrl();
            for (int i = 0; i < COUNT; i++) {
                int randNum = this.random(N);
                if (randNum == 0) {
                    candidateUrls[i] = url;
                }
            }
            N++;
        }

        return Arrays.asList(candidateUrls);
    }

    public int random(int max) {
        Random random = new Random();
        return random.nextInt(max + 1);
    }

    public ModifyApplyCommand getApplyCommand() {
        ModifyApplyCommand cmd = new ModifyApplyCommand();
        cmd.setTargetInstanceId(targetInstanceId);
        cmd.setHarvestResultNumber(harvestResultNumber);
        cmd.setNewHarvestResultNumber(newHarvestResultNumber);
        return cmd;
    }

    public String getModifiedWarcFileName(File warcFileFrom) {
        String fromFileName = warcFileFrom.getName();
        int idx = fromFileName.indexOf('.');
        if (idx <= 0) {
            return null;
        }

        String part1 = fromFileName.substring(0, idx);
        String part2 = fromFileName.substring(idx + 1);

        return String.format("%s~%d.%s", part1, newHarvestResultNumber, part2);
    }
}
