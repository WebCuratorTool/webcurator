package org.webcurator.core.visualization.networkmap;

import org.junit.Before;
import org.junit.Test;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.visualization.MockVisualizationCommonConfigureItems;
import org.webcurator.core.visualization.VisualizationProgressBar;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessor;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessorWarc;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClientLocal;
import org.webcurator.domain.model.core.HarvestResult;

import java.io.File;
import java.io.IOException;

public class TestIndexProcessor extends MockVisualizationCommonConfigureItems {
    private IndexProcessor indexer;

    @Before
    public void initTest() throws IOException, DigitalAssetStoreException {
        String dbPath = pool.getDbPath(targetInstanceId, harvestResultNumber);
        File f = new File(dbPath);
        f.deleteOnExit(); //Clear the existing db

        IndexProcessor indexer = new IndexProcessorWarc(pool, targetInstanceId, harvestResultNumber);
        processorManager.startTask(indexer);
    }

    @Test
    public void testResourceExtractorProgress() throws DigitalAssetStoreException {
        VisualizationProgressBar progressBar = processorManager.getProgress(targetInstanceId, harvestResultNumber);
        assert progressBar != null;
        log.debug(progressBar.toString());

        assert progressBar.getStage().equals(HarvestResult.PATCH_STAGE_TYPE_INDEXING);
        assert progressBar.getTargetInstanceId() == targetInstanceId;
        assert progressBar.getHarvestResultNumber() == harvestResultNumber;
        assert progressBar.getProgressPercentage() == 100;

        indexer.clear();
        progressBar = processorManager.getProgress(targetInstanceId, harvestResultNumber);
        assert progressBar == null;
    }

    @Test
    public void testQueryDomain() {
        NetworkMapClient client = new NetworkMapClientLocal(pool, processorManager);
        NetworkMapResult result = client.getAllDomains(targetInstanceId, harvestResultNumber);

        assert result.getRspCode() == 0;
        assert result.getPayload() != null;

        log.debug(result.getPayload().toString());
    }
}
