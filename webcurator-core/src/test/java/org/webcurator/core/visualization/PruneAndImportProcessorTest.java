package org.webcurator.core.visualization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.core.visualization.modification.PruneAndImportProcessor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

public class PruneAndImportProcessorTest {
    private static final String fileDir = "/usr/local/wct/store/uploadedFiles";
    private static final String baseDir = "/usr/local/wct/store";
    private static final String coreCacheDir = "/usr/local/wct/webapp/uploadedFiles";
    private MockWebAppClient webAppClient;
    private final VisualizationManager visualizationManager = new VisualizationManager();

    @Before
    public void initTest() {
        webAppClient = new MockWebAppClient("http", "localhost", 8080, new RestTemplateBuilder());
    }

    @Test
    public void testPruneAndImport() throws IOException, URISyntaxException, DigitalAssetStoreException {
        PruneAndImportCommandApply cmd = getPruneAndImportCommandApply(5010, 2);
        cmd.setNewHarvestResultNumber(2);

        VisualizationManager visualizationManager = new VisualizationManager();
        visualizationManager.setBaseDir(baseDir);
        visualizationManager.setUploadDir(fileDir);
        visualizationManager.setLogsDir("logs");
        visualizationManager.setReportsDir("reports");
        PruneAndImportProcessor p = new PruneAndImportProcessor(visualizationManager, cmd, webAppClient);

        p.start();
        try {
            p.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public PruneAndImportCommandApply getPruneAndImportCommandApply(long job, int harvestNumber) throws IOException {
        String cmdFilePath = String.format("%s%smod_%d_%d.json", coreCacheDir, File.separator, job, harvestNumber);
        File cmdFile = new File(cmdFilePath);
        byte[] cmdJsonContent = Files.readAllBytes(cmdFile.toPath());
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(cmdJsonContent, PruneAndImportCommandApply.class);
    }
}
