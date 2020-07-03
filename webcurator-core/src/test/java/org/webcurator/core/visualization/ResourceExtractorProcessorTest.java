package org.webcurator.core.visualization;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.visualization.networkmap.ResourceExtractorProcessor;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClientLocal;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.SeedHistoryDTO;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ResourceExtractorProcessorTest {
    private static final Logger log = LoggerFactory.getLogger(ResourceExtractorProcessorTest.class);

    private static final String baseDir = "/usr/local/wct/store";
    private static final BDBNetworkMapPool pool = new BDBNetworkMapPool(baseDir);
    private final long targetInstanceId = 5010;
    private final int harvestResultNumber = 1;
    private final Set<SeedHistoryDTO> seeds = new HashSet<>();
    private final VisualizationDirectoryManager visualizationDirectoryManager = new VisualizationDirectoryManager();

    private ResourceExtractorProcessor indexer;
    private VisualizationProcessorManager visualizationProcessorManager;

    @Before
    public void initTest() throws IOException, DigitalAssetStoreException {
        String dbPath = pool.getDbPath(targetInstanceId, harvestResultNumber);
        File f = new File(dbPath);
        f.deleteOnExit(); //Clear the existing db

        SeedHistoryDTO seedHistoryPrimary = new SeedHistoryDTO(1, "http://www.google.com/", targetInstanceId, true);
        SeedHistoryDTO seedHistorySecondary = new SeedHistoryDTO(2, "http://www.baidu.com/", targetInstanceId, false);
        seeds.add(seedHistoryPrimary);
        seeds.add(seedHistorySecondary);

        visualizationDirectoryManager.setBaseDir(baseDir);
        visualizationDirectoryManager.setUploadDir(baseDir + File.separator + "uploadedFiles");
        visualizationDirectoryManager.setReportsDir("reports");
        visualizationDirectoryManager.setLogsDir("logs");

        visualizationProcessorManager = new VisualizationProcessorManager(visualizationDirectoryManager, null, pool, 3, 3000, 3000);

        ResourceExtractorProcessor indexer = new ResourceExtractorProcessor(pool, targetInstanceId, harvestResultNumber);

        visualizationProcessorManager.startTask(indexer);


    }

    @Test
    public void testResourceExtractorProgress() throws DigitalAssetStoreException {
        VisualizationProgressBar progressBar = visualizationProcessorManager.getProgress(targetInstanceId, harvestResultNumber);
        assert progressBar != null;
        log.debug(progressBar.toString());

        assert progressBar.getStage().equals(HarvestResult.PATCH_STAGE_TYPE_INDEXING);
        assert progressBar.getTargetInstanceId() == targetInstanceId;
        assert progressBar.getHarvestResultNumber() == harvestResultNumber;
        assert progressBar.getProgressPercentage() == 100;

        indexer.clear();
        progressBar = visualizationProcessorManager.getProgress(targetInstanceId, harvestResultNumber);
        assert progressBar == null;
    }

    @Test
    public void testQueryDomain() {
        NetworkMapClient client = new NetworkMapClientLocal(pool, visualizationProcessorManager);
        NetworkMapResult result = client.getAllDomains(targetInstanceId, harvestResultNumber);

        assert result.getRspCode() == 0;
        assert result.getPayload() != null;

        log.debug(result.getPayload().toString());
    }
}
