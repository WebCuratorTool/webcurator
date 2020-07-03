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

@SuppressWarnings("all")
public class PruneAndImportProcessorTest {
    private static final Logger log = LoggerFactory.getLogger(PruneAndImportProcessorTest.class);
    private static final String fileDir = "/usr/local/wct/store/uploadedFiles";
    private static final String baseDir = "/usr/local/wct/store";
    private static final String coreCacheDir = "/usr/local/wct/webapp/uploadedFiles";
    private WctCoordinatorClient wctCoordinatorClient;
    private final VisualizationDirectoryManager visualizationDirectoryManager = new VisualizationDirectoryManager();

    private VisualizationProcessorManager visualizationProcessorManager;

    @Before
    public void initTest() {
        wctCoordinatorClient = new WctCoordinatorClient("http", "localhost", 8080, new RestTemplateBuilder());
        visualizationProcessorManager = new VisualizationProcessorManager(visualizationDirectoryManager, wctCoordinatorClient, null, 3, 3000, 3000);
    }

    @Test
    public void testPruneAndImport() throws IOException, URISyntaxException, DigitalAssetStoreException, InterruptedException {
        PruneAndImportCommandApply cmd = getPruneAndImportCommandApply(5010, 2);
        cmd.setNewHarvestResultNumber(2);

        VisualizationDirectoryManager visualizationDirectoryManager = new VisualizationDirectoryManager();
        visualizationDirectoryManager.setBaseDir(baseDir);
        visualizationDirectoryManager.setUploadDir(fileDir);
        visualizationDirectoryManager.setLogsDir("logs");
        visualizationDirectoryManager.setReportsDir("reports");
        PruneAndImportProcessor processor = new PruneAndImportProcessor(cmd);

        visualizationProcessorManager.startTask(processor);

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
