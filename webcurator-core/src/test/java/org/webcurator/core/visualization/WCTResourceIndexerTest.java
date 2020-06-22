package org.webcurator.core.visualization;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.visualization.networkmap.WCTResourceIndexer;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;

import java.io.File;
import java.io.IOException;

public class WCTResourceIndexerTest {
    private static final Logger log = LoggerFactory.getLogger(WCTResourceIndexerTest.class);

    private static final String baseDir = "/usr/local/wct/store";
    private static final BDBNetworkMapPool pool = new BDBNetworkMapPool(baseDir);
    private long targetInstanceId = 5010;
    private int harvestResultNumber = 1;

    private void initTest() {
        String dbPath = pool.getDbPath(targetInstanceId, harvestResultNumber);
        File f = new File(dbPath);
        f.deleteOnExit(); //Clear the existing db
    }

    @Test
    public void testWCTResourceIndexer() throws IOException {
        String directory = String.format("%s/%d/%d", baseDir, targetInstanceId, harvestResultNumber);
        BDBNetworkMap db = pool.createInstance(targetInstanceId, harvestResultNumber);
        WCTResourceIndexer indexer = new WCTResourceIndexer(new File(directory), db, targetInstanceId, harvestResultNumber);

        indexer.indexFiles();

        VisualizationProgressBar progressBar = WCTResourceIndexer.getProgress(targetInstanceId, harvestResultNumber);
        assert progressBar != null;
        log.debug(progressBar.toString());

        assert progressBar.getProgressPercentage() == 100;

        indexer.clear();
        progressBar = WCTResourceIndexer.getProgress(targetInstanceId, harvestResultNumber);
        assert progressBar == null;
    }

//    @Test
//    public void testIndexFile() throws IOException {
//        Map<String, NetworkMapNode> results = new HashMap<>();
//        Set<SeedHistory> seeds = new HashSet<>();
//
//        long targetInstanceId = 5010;
//        int harvestResultNumber = 1;
//
//        ResourceExtractor extractor = new ResourceExtractorWarc(results, seeds);
//
//
//        File directory = new File(baseDir, Long.toString(targetInstanceId));
////        WCTResourceIndexer indexer = new WCTResourceIndexer(directory, null, 5010, 2);
//        WCTResourceIndexer indexer = new WCTResourceIndexer();
//
//        File archiveFile = new File(directory, String.format("%d%s%s", 2, File.separator, "IAH-20200612141024691-00001-mod~import~file-2.warc"));
//        indexer.indexFile(archiveFile, extractor);
//    }
}
