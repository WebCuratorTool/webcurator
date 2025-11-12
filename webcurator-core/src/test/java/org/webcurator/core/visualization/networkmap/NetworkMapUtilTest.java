package org.webcurator.core.visualization.networkmap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.webcurator.core.visualization.BaseVisualizationTest;
import org.webcurator.core.visualization.networkmap.bdb.BDBRepoHolder;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeUrlDTO;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeUrlEntity;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeFolderDTO;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessor;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessorWarc;
import org.webcurator.core.visualization.networkmap.processor.FolderTreeViewGenerator;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClientLocal;
import org.webcurator.core.visualization.networkmap.service.NetworkMapServiceSearchCommand;

import java.io.File;
import java.util.List;

public class NetworkMapUtilTest extends BaseVisualizationTest {
    private IndexProcessor indexer;

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
        indexer.init(directoryManager, wctClient, networkMapClient);

        indexer.processInternal();
    }

    @Test
    public void testClassifyTreeViewByPathName() {
        NetworkMapClientLocal localClient = new NetworkMapClientLocal(pool, processorManager);
        BDBRepoHolder db = pool.getInstance(targetInstanceId, harvestResultNumber);

        NetworkMapServiceSearchCommand searchCommand = new NetworkMapServiceSearchCommand();
        List<NetworkMapNodeUrlEntity> networkMapNodeUrlEntityList = localClient.searchUrlDTOs(db, searchCommand);

        NetworkMapNodeFolderDTO rootTreeNodeDTO = new NetworkMapNodeFolderDTO();
        networkMapNodeUrlEntityList.forEach(node -> {
            NetworkMapNodeFolderDTO treeNodeDTO = new NetworkMapNodeFolderDTO();
            treeNodeDTO.setUrl(node.getUrl());
            treeNodeDTO.setContentType(node.getContentType());
            treeNodeDTO.setStatusCode(node.getStatusCode());
            treeNodeDTO.setContentLength(node.getContentLength());

            rootTreeNodeDTO.getChildren().add(treeNodeDTO);
        });

        FolderTreeViewGenerator cascadePath = new FolderTreeViewGenerator();
        cascadePath.classifyTreePaths(rootTreeNodeDTO);

        log.debug("Size: {}", rootTreeNodeDTO.getChildren().size());
        assert true;
    }


    @Test
    public void testClassifyTreeViewByPathName2() {
        FolderTreeViewGenerator cascadePath = new FolderTreeViewGenerator();
        int first_level = 0;

        {
            NetworkMapNodeFolderDTO rootTreeNodeDTO = new NetworkMapNodeFolderDTO();
            insertNode(rootTreeNodeDTO, "http://a.b.c/d/e/f?x=1", 3);

            cascadePath.classifyTreePaths(rootTreeNodeDTO);
            assert rootTreeNodeDTO.getChildren().size() == 0;
        }

        {
            NetworkMapNodeFolderDTO rootTreeNodeDTO = new NetworkMapNodeFolderDTO();
            insertNode(rootTreeNodeDTO, "http://a.b.c/d/e/f?x=1", 3);
            insertNode(rootTreeNodeDTO, "http://a.b.c/d/u/v?x=1", 5);

            cascadePath.classifyTreePaths(rootTreeNodeDTO);
            assert rootTreeNodeDTO.getChildren().size() == 2;
        }

        {
            NetworkMapNodeFolderDTO rootTreeNodeDTO = new NetworkMapNodeFolderDTO();
            insertNode(rootTreeNodeDTO, "http://a.b.c/d/e/f?x=1", 3);
            insertNode(rootTreeNodeDTO, "http://a.b.c/d/u/v?x=1", 5);
            insertNode(rootTreeNodeDTO, "http://a.b.c/", 5);

            cascadePath.classifyTreePaths(rootTreeNodeDTO);
            assert rootTreeNodeDTO.getChildren().size() == 2;
        }

        {
            NetworkMapNodeFolderDTO rootTreeNodeDTO = new NetworkMapNodeFolderDTO();
            insertNode(rootTreeNodeDTO, "http://a.b.c/d/e/f?x=1", 3);
            insertNode(rootTreeNodeDTO, "http://a.b.c/d/u/v?x=1", 5);
            insertNode(rootTreeNodeDTO, "http://a.b.c/", 5);
            insertNode(rootTreeNodeDTO, "http://www.b.c/", 100);
            cascadePath.classifyTreePaths(rootTreeNodeDTO);
            assert rootTreeNodeDTO.getChildren().size() == 2;
        }

        log.debug("Finished");
    }

    private void insertNode(NetworkMapNodeFolderDTO rootTreeNodeDTO, String url, long contentLength) {
        NetworkMapNodeFolderDTO node = new NetworkMapNodeFolderDTO();
        node.setUrl(url);
        node.setContentType("N/A");
        node.setStatusCode(200);
        node.setContentLength(contentLength);
        node.accumulate(200, contentLength, "N/A");

        rootTreeNodeDTO.getChildren().add(node);
    }

    @Test
    public void testGetNextTitle() {
        FolderTreeViewGenerator cascadePath = new FolderTreeViewGenerator();
        {
            String url = "http://a.b.c/d/u/v?x=1";
            String parentTitle = null;
            String title = cascadePath.getNextTitle(parentTitle, url);

            assert title.equals("http://a.b.c/");
        }

        {
            String url = "http://a.b.c/d/u/v?x=1";
            String parentTitle = "http://a.b.c/";
            String title = cascadePath.getNextTitle(parentTitle, url);

            assert title.equals("http://a.b.c/d/");
        }

        {
            String url = "http://www.b.c/d/u/v?x=1";
            String parentTitle = "http://www.b.c/";
            String title = cascadePath.getNextTitle(parentTitle, url);

            assert title.equals("http://www.b.c/d/");
        }
    }

    @After
    public void close() {
        if (pool != null) {
            pool.close(targetInstanceId, harvestResultNumber);
            pool.close(targetInstanceId, newHarvestResultNumber);
        }
    }
}
