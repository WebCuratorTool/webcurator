package org.webcurator.core.visualization;

import org.junit.Before;
import org.junit.Test;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.visualization.networkmap.ResourceExtractorProcessor;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClientLocal;
import org.webcurator.domain.model.core.SeedHistoryDTO;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class BDBNetworkMapTest {
    private static final String baseDir = "/usr/local/wct/store/";
    private static final BDBNetworkMapPool pool = new BDBNetworkMapPool(baseDir);
    private final long targetInstanceId = 36;
    private final int harvestResultNumber = 1;
    private final Set<SeedHistoryDTO> seeds = new HashSet<>();
    private final VisualizationManager visualizationManager = new VisualizationManager();

    @Before
    public void initTest() throws IOException, DigitalAssetStoreException {
        String dbPath = pool.getDbPath(targetInstanceId, harvestResultNumber);
        File f = new File(dbPath);
        f.deleteOnExit(); //Clear the existing db

        SeedHistoryDTO seedHistoryPrimary = new SeedHistoryDTO(1, "http://www.google.com/", targetInstanceId, true);
        SeedHistoryDTO seedHistorySecondary = new SeedHistoryDTO(2, "http://www.baidu.com/", targetInstanceId, false);
        seeds.add(seedHistoryPrimary);
        seeds.add(seedHistorySecondary);

        visualizationManager.setBaseDir(baseDir);
        visualizationManager.setUploadDir(baseDir + File.separator + "uploadedFiles");
        visualizationManager.setReportsDir("reports");
        visualizationManager.setLogsDir("logs");
    }

    @Test
    public void testBDB() {
        long job1 = 36;
        int harvestResultNumber = 1;

        BDBNetworkMap db1 = pool.createInstance(job1, harvestResultNumber);

        db1.delete(Long.toString(job1));
        testExtractor(db1, job1, harvestResultNumber);
        testReadData(db1, job1, harvestResultNumber);
        db1.shutdownDB();

        BDBNetworkMap db2 = new BDBNetworkMap();
        try {
            db2.initializeDB(baseDir + "_db_temp", "resource2.db");
        } catch (IOException e) {
            e.printStackTrace();
        }

        long job2 = 53;
        db2.delete(Long.toString(job2));
        testExtractor(db2, job2, harvestResultNumber);
        testReadData(db2, job2, harvestResultNumber);

        db2.shutdownDB();
    }

    public void testExtractor(BDBNetworkMap db, long job, int harvestResultNumber) {
        String directory = String.format("%s/%d/1", baseDir, job);
        try {
            ResourceExtractorProcessor indexer = new ResourceExtractorProcessor(pool, job, harvestResultNumber, seeds, visualizationManager);
            indexer.indexFiles();
        } catch (IOException | DigitalAssetStoreException e) {
            e.printStackTrace();
        }
    }

    void testReadData(BDBNetworkMap db, long job, int harvestResultNumber) {
        NetworkMapClientLocal client = new NetworkMapClientLocal(new BDBNetworkMapPool("/usr/local/store"), new VisualizationProcessorQueue());

        System.out.println(client.getAllDomains(job, harvestResultNumber));

    }
}
