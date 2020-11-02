package org.webcurator.core.visualization.networkmap;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.webcurator.core.visualization.BaseVisualizationTest;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNode;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeDTO;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapTreeNodeDTO;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessor;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessorWarc;
import org.webcurator.core.visualization.networkmap.service.NetworkMapCascadePath;
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
        NetworkMapNode.setTopDomainParse(suffixParser);

        String dbPath = pool.getDbPath(targetInstanceId, harvestResultNumber);
        File f = new File(dbPath);
        f.deleteOnExit(); //Clear the existing db

        indexer = new IndexProcessorWarc(pool, targetInstanceId, harvestResultNumber);
        indexer.init(processorManager, directoryManager, wctClient, networkMapClient);

        indexer.processInternal();
    }

    @Test
    public void testClassifyTreeViewByPathName() {
        NetworkMapClientLocal localClient = new NetworkMapClientLocal(pool, processorManager);
        BDBNetworkMap db = pool.getInstance(targetInstanceId, harvestResultNumber);

        NetworkMapServiceSearchCommand searchCommand = new NetworkMapServiceSearchCommand();
        List<NetworkMapNodeDTO> networkMapNodeDTOList = localClient.searchUrlDTOs(db, targetInstanceId, harvestResultNumber, searchCommand);

        NetworkMapTreeNodeDTO rootTreeNodeDTO = new NetworkMapTreeNodeDTO();
        networkMapNodeDTOList.forEach(node -> {
            NetworkMapTreeNodeDTO treeNodeDTO = new NetworkMapTreeNodeDTO();
            treeNodeDTO.setUrl(node.getUrl());
            treeNodeDTO.setContentType(node.getContentType());
            treeNodeDTO.setStatusCode(node.getStatusCode());
            treeNodeDTO.setContentLength(node.getContentLength());

            rootTreeNodeDTO.getChildren().add(treeNodeDTO);
        });

        NetworkMapCascadePath cascadePath = new NetworkMapCascadePath();
        cascadePath.classifyTreePaths(rootTreeNodeDTO);

        log.debug("Size: {}", rootTreeNodeDTO.getChildren().size());
        assert true;
    }


    @Test
    public void testClassifyTreeViewByPathName2() {
        NetworkMapCascadePath cascadePath = new NetworkMapCascadePath();
        int first_level = 0;

        {
            NetworkMapTreeNodeDTO rootTreeNodeDTO = new NetworkMapTreeNodeDTO();
            insertNode(rootTreeNodeDTO, "http://a.b.c/d/e/f?x=1", 3);

            cascadePath.classifyTreePaths(rootTreeNodeDTO);
            assert rootTreeNodeDTO.getChildren().size() == 0;
        }

        {
            NetworkMapTreeNodeDTO rootTreeNodeDTO = new NetworkMapTreeNodeDTO();
            insertNode(rootTreeNodeDTO, "http://a.b.c/d/e/f?x=1", 3);
            insertNode(rootTreeNodeDTO, "http://a.b.c/d/u/v?x=1", 5);

            cascadePath.classifyTreePaths(rootTreeNodeDTO);
            assert rootTreeNodeDTO.getChildren().size() == 2;
        }

        {
            NetworkMapTreeNodeDTO rootTreeNodeDTO = new NetworkMapTreeNodeDTO();
            insertNode(rootTreeNodeDTO, "http://a.b.c/d/e/f?x=1", 3);
            insertNode(rootTreeNodeDTO, "http://a.b.c/d/u/v?x=1", 5);
            insertNode(rootTreeNodeDTO, "http://a.b.c/", 5);

            cascadePath.classifyTreePaths(rootTreeNodeDTO);
            assert rootTreeNodeDTO.getChildren().size() == 2;
        }

        {
            NetworkMapTreeNodeDTO rootTreeNodeDTO = new NetworkMapTreeNodeDTO();
            insertNode(rootTreeNodeDTO, "http://a.b.c/d/e/f?x=1", 3);
            insertNode(rootTreeNodeDTO, "http://a.b.c/d/u/v?x=1", 5);
            insertNode(rootTreeNodeDTO, "http://a.b.c/", 5);
            insertNode(rootTreeNodeDTO, "http://www.b.c/", 100);
            cascadePath.classifyTreePaths(rootTreeNodeDTO);
            assert rootTreeNodeDTO.getChildren().size() == 2;
        }

        log.debug("Finished");
    }

    private void insertNode(NetworkMapTreeNodeDTO rootTreeNodeDTO, String url, long contentLength) {
        NetworkMapTreeNodeDTO node = new NetworkMapTreeNodeDTO();
        node.setUrl(url);
        node.setContentType("N/A");
        node.setStatusCode(200);
        node.setContentLength(contentLength);
        node.accumulate(200, contentLength, "N/A");

        rootTreeNodeDTO.getChildren().add(node);
    }

    @Test
    public void testGetNextTitle() {
        NetworkMapCascadePath cascadePath = new NetworkMapCascadePath();
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
}
