package org.webcurator.core.visualization.networkmap;

import org.junit.Before;
import org.junit.Test;
import org.webcurator.core.visualization.BaseVisualizationTest;
import org.webcurator.core.visualization.networkmap.bdb.BDBRepoHolder;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessor;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessorWarc;

import java.io.File;
import java.io.IOException;

public class BDBNetworkMapTest extends BaseVisualizationTest {
    @Before
    public void initTest() throws Exception {
        super.initTest();
        String dbPath = pool.getDbPath(targetInstanceId, harvestResultNumber);
        File f = new File(dbPath);
        f.deleteOnExit(); //Clear the existing db
    }

    @Test
    public void testBDB() {
        long job1 = 36;
        int harvestResultNumber = 1;

        BDBRepoHolder db1 = pool.createInstance(job1, harvestResultNumber);

        db1.deleteUrlById(job1);
        testExtractor(db1, job1, harvestResultNumber);
        testReadData(db1, job1, harvestResultNumber);
        db1.shutdownDB();

        BDBRepoHolder db2 = null;
        try {
            db2 = BDBRepoHolder.createInstance(baseDir + "_db_temp", "resource2.db");
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert db2 != null;

        long job2 = 53;
        db2.deleteUrlById(job2);
        testExtractor(db2, job2, harvestResultNumber);
        testReadData(db2, job2, harvestResultNumber);

        db2.shutdownDB();
    }

    public void testExtractor(BDBRepoHolder db, long job, int harvestResultNumber) {
        String directory = String.format("%s/%d/1", baseDir, job);
        try {
            IndexProcessor indexer = new IndexProcessorWarc(pool, job, harvestResultNumber);
            indexer.processInternal();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void testReadData(BDBRepoHolder db, long job, int harvestResultNumber) {
//        NetworkMapClientLocal client = new NetworkMapClientLocal(new BDBNetworkMapPool("/usr/local/store"), new VisualizationProcessorManager());
//
//        System.out.println(client.getAllDomains(job, harvestResultNumber));

    }
}
