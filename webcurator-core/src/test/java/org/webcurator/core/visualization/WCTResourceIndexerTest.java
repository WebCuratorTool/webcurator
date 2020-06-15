package org.webcurator.core.visualization;

import org.junit.Test;
import org.webcurator.core.visualization.networkmap.ResourceExtractor;
import org.webcurator.core.visualization.networkmap.ResourceExtractorWarc;
import org.webcurator.core.visualization.networkmap.WCTResourceIndexer;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNode;
import org.webcurator.domain.model.core.SeedHistory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WCTResourceIndexerTest {
    private static final String baseDir = "/usr/local/wct/store";

    @Test
    public void testIndexFile() throws IOException {
        Map<String, NetworkMapNode> results = new HashMap<>();
        Set<SeedHistory> seeds = new HashSet<>();

        ResourceExtractor extractor = new ResourceExtractorWarc(results, seeds);


        File directory = new File(baseDir, "5010");
//        WCTResourceIndexer indexer = new WCTResourceIndexer(directory, null, 5010, 2);
        WCTResourceIndexer indexer = new WCTResourceIndexer();

        File archiveFile = new File(directory, String.format("%d%s%s", 2, File.separator, "IAH-20200612141024691-00001-mod~import~file-2.warc"));
        indexer.indexFile(archiveFile, extractor);
    }
}
