package org.webcurator.core.visualization;

import org.junit.Test;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.visualization.networkmap.ResourceExtractorProcessor;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClientLocal;

import java.io.File;
import java.io.IOException;

public class BDBNetworkMapTest {
    private static final String DIR_ROOT = "/usr/local/wct/store/";

    @Test
    public void testBDB() {
        BDBNetworkMap db1 = new BDBNetworkMap();
        try {
            db1.initializeDB(DIR_ROOT + "_db_temp", "resource.db");
        } catch (IOException e) {
            e.printStackTrace();
        }

        int harvestResultNumber = 1;

        long job1 = 36;
        db1.delete(Long.toString(job1));
        testExtractor(db1, job1, harvestResultNumber);
        testReadData(db1, job1, harvestResultNumber);
        db1.shutdownDB();

        BDBNetworkMap db2 = new BDBNetworkMap();
        try {
            db1.initializeDB(DIR_ROOT + "_db_temp", "resource2.db");
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
        String directory = String.format("%s/%d/1", DIR_ROOT, job);
        try {
            ResourceExtractorProcessor indexer = new ResourceExtractorProcessor(new File(directory), db, job, harvestResultNumber, null, null);
            indexer.indexFiles();
        } catch (IOException | DigitalAssetStoreException e) {
            e.printStackTrace();
        }
    }

    void testReadData(BDBNetworkMap db, long job, int harvestResultNumber) {
        NetworkMapClientLocal client = new NetworkMapClientLocal(new BDBNetworkMapPool("/usr/local/store"));

        System.out.println(client.getAllDomains(job, harvestResultNumber));

    }
}
