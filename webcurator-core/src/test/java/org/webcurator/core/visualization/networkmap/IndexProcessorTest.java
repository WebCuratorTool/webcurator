package org.webcurator.core.visualization.networkmap;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.webcurator.core.util.PatchUtil;
import org.webcurator.core.visualization.BaseVisualizationTest;
import org.webcurator.core.visualization.VisualizationConstants;
import org.webcurator.core.visualization.VisualizationProgressBar;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeUrlDTO;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeUrlEntity;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapUrlCommand;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessor;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessorWarc;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClientLocal;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.SeedHistoryDTO;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IndexProcessorTest extends BaseVisualizationTest {
    protected IndexProcessor indexer;

    @Before
    public void initTest() throws Exception {
        super.initTest();

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
        indexer.init(processorManager, directoryManager, wctClient, networkMapClient);
    }

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

    private File getOneWarcFile() {
        File directory = new File(directoryManager.getBaseDir(), String.format("%d%s%d", targetInstanceId, File.separator, harvestResultNumber));
        List<File> fileList = PatchUtil.listWarcFiles(directory);
        assert fileList.size() > 0;

        return fileList.get(0);
    }
}
