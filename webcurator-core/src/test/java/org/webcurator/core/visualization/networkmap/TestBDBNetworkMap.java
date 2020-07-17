package org.webcurator.core.visualization.networkmap;

import org.junit.Before;
import org.junit.Test;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.visualization.BaseVisualizationTest;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessor;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessorWarc;

import java.io.File;
import java.io.IOException;

public class TestBDBNetworkMap extends BaseVisualizationTest {
    @Before
    public void initTest() throws IOException, DigitalAssetStoreException {
        super.initTest();
        String dbPath = pool.getDbPath(targetInstanceId, harvestResultNumber);
        File f = new File(dbPath);
        f.deleteOnExit(); //Clear the existing db
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
            IndexProcessor indexer = new IndexProcessorWarc(pool, job, harvestResultNumber);
            indexer.processInternal();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void testReadData(BDBNetworkMap db, long job, int harvestResultNumber) {
//        NetworkMapClientLocal client = new NetworkMapClientLocal(new BDBNetworkMapPool("/usr/local/store"), new VisualizationProcessorManager());
//
//        System.out.println(client.getAllDomains(job, harvestResultNumber));

    }
}
