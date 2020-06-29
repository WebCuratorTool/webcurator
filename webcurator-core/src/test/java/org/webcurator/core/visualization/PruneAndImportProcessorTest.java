package org.webcurator.core.visualization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.webcurator.core.coordinator.WctCoordinatorClient;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.core.visualization.modification.PruneAndImportProcessor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

public class PruneAndImportProcessorTest {
    private static final Logger log = LoggerFactory.getLogger(PruneAndImportProcessorTest.class);
    private static final String fileDir = "/usr/local/wct/store/uploadedFiles";
    private static final String baseDir = "/usr/local/wct/store";
    private static final String coreCacheDir = "/usr/local/wct/webapp/uploadedFiles";
    private WctCoordinatorClient wctCoordinatorClient;
    private final VisualizationManager visualizationManager = new VisualizationManager();

    private VisualizationProcessorQueue visualizationProcessorQueue = new VisualizationProcessorQueue();

    @Before
    public void initTest() {
        wctCoordinatorClient = new WctCoordinatorClient("http", "localhost", 8080, new RestTemplateBuilder());
    }

    @Test
    public void testPruneAndImport() throws IOException, URISyntaxException, DigitalAssetStoreException, InterruptedException {
        PruneAndImportCommandApply cmd = getPruneAndImportCommandApply(5010, 2);
        cmd.setNewHarvestResultNumber(2);

        VisualizationManager visualizationManager = new VisualizationManager();
        visualizationManager.setBaseDir(baseDir);
        visualizationManager.setUploadDir(fileDir);
        visualizationManager.setLogsDir("logs");
        visualizationManager.setReportsDir("reports");
        PruneAndImportProcessor processor = new PruneAndImportProcessor(cmd);

        visualizationProcessorQueue.startTask(processor);

        Thread.sleep(2000);

        log.info(processor.getProgress().toString());
    }

    public PruneAndImportCommandApply getPruneAndImportCommandApply(long job, int harvestNumber) throws IOException {
        String cmdFilePath = String.format("%s%smod_%d_%d.json", coreCacheDir, File.separator, job, harvestNumber);
        File cmdFile = new File(cmdFilePath);
        byte[] cmdJsonContent = Files.readAllBytes(cmdFile.toPath());
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(cmdJsonContent, PruneAndImportCommandApply.class);
    }
}
