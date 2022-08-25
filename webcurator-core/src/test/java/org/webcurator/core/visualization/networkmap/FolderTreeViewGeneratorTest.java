package org.webcurator.core.visualization.networkmap;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.bdb.BDBRepoHolder;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeFolderEntity;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeUrlEntity;
import org.webcurator.core.visualization.networkmap.processor.FolderTreeViewGenerator;

public class FolderTreeViewGeneratorTest {
    protected static final Logger log = LoggerFactory.getLogger(FolderTreeViewGeneratorTest.class);

    protected static String baseDir = "/usr/local/wct/store";
    protected static String baseLogDir = "logs";
    protected static String baseReportDir = "reports";
    protected static long targetInstanceId = 1;
    protected static int harvestResultNumber = 1;
    protected static BDBNetworkMapPool pool = null;

    @BeforeClass
    public static void initTest() throws Exception {
        pool = new BDBNetworkMapPool(baseDir, "dbVersion");
    }


    @Test
    public void testSplitUrlToFolderPaths() {
        String[] urls = {"https://www.muzic.net.nz/charts/c90878/official-new-zealand-top-40-albums-2098-21-august-2017"
                , "https://www.muzic.net.nz/charts/s66402/coldplay-paradise?",
                "https://www.muzic.net.nz/charts/s66402/coldplay-paradise?a=1&b=2&3=3"};

        for (String url : urls) {
            NetworkMapNodeFolderEntity folderNode = new NetworkMapNodeFolderEntity();
            int depth = FolderTreeViewGenerator.getUrlDepth(url);
            log.debug("Max depth={}", depth);
            for (int i = 0; i <= depth; i++) {
                String folderName = FolderTreeViewGenerator.getFolderNameFromUrlDepth(url, i);
                log.debug("Current depth={}, folderName={}, url={}", i, folderName, url);
            }
        }

        assert true;
    }

    @Ignore
    @Test
    public void testClassifyFolders() {
        BDBRepoHolder db = pool.getInstance(targetInstanceId, harvestResultNumber);
        FolderTreeViewGenerator cascade = new FolderTreeViewGenerator();
        long rootFolderId = cascade.classifyTreePaths(db);
        assert rootFolderId > 0;
    }

    @Ignore
    @Test
    public void testWalkThroughFolderTree() {
        BDBRepoHolder db = pool.getInstance(targetInstanceId, harvestResultNumber);
        long rootFolderId = 25009;
        walkThroughFolderTreeNodes(db, rootFolderId);
    }

    public void walkThroughFolderTreeNodes(BDBRepoHolder db, long currentId) {
        if (currentId < 0) {
            return;
        }

        NetworkMapNodeFolderEntity folderEntity = db.getFolderById(currentId);
        if (folderEntity == null) {
            log.error("The folder entity does not exist: {}", currentId);
            return;
        }

        for (long id : folderEntity.getSubFolderList()) {
            walkThroughFolderTreeNodes(db, id);
        }

        log.debug("Folder: id={}, {} {} {} {}, sub url: {}, title={}", currentId, folderEntity.getTotSize(), folderEntity.getTotUrls(), folderEntity.getTotFailed(), folderEntity.getTotSuccess(), folderEntity.getSubUrlList().size(), folderEntity.getTitle());
        for (long id : folderEntity.getSubUrlList()) {
            NetworkMapNodeUrlEntity urlEntity = db.getUrlById(id);
            log.debug("Url: id={}, url={}", urlEntity.getId(), urlEntity.getUrl());
        }
    }

    @Ignore
    @Test
    public void testCountOfUrls() {
        BDBRepoHolder db = pool.getInstance(targetInstanceId, harvestResultNumber);
        long rootFolderId = 25009;
        countUrlsOfFolderTreeNodes(db, rootFolderId);
    }

    public int countUrlsOfFolderTreeNodes(BDBRepoHolder db, long currentId) {
        if (currentId < 0) {
            return 0;
        }

        NetworkMapNodeFolderEntity folderEntity = db.getFolderById(currentId);
        if (folderEntity == null) {
            log.error("The folder entity does not exist: {}", currentId);
            return 0;
        }

        int countOfUrls = folderEntity.getSubUrlList().size();
        for (long id : folderEntity.getSubFolderList()) {
            countOfUrls += countUrlsOfFolderTreeNodes(db, id);
        }
        if (countOfUrls > 1000) {
            log.debug("Folder: id={}, title={}, count of urls={}", folderEntity.getId(), folderEntity.getTitle(), countOfUrls);
        }
        return countOfUrls;
    }
}
