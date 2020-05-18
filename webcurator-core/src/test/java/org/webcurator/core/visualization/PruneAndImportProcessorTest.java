package org.webcurator.core.visualization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.core.visualization.modification.PruneAndImportProcessor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

public class PruneAndImportProcessorTest {
    private static final String fileDir = "/usr/local/wct/store/uploadedFiles";
    private static final String baseDir = "/usr/local/wct/store";
    private static final String coreCacheDir = "/usr/local/wct/webapp/cache";

    @Test
    public void testPruneAndImport() throws IOException, URISyntaxException {
        PruneAndImportCommandApply cmd = getPruneAndImportCommandApply(5013);
        cmd.setNewHarvestResultNumber(22);

        PruneAndImportProcessor p = new PruneAndImportProcessor(fileDir, baseDir, cmd);

        p.pruneAndImport();
    }

    public PruneAndImportCommandApply getPruneAndImportCommandApply(long job) throws IOException {
        String cmdFilePath = String.format("%s%smod_%d.json", coreCacheDir, File.separator, job);
        File cmdFile = new File(cmdFilePath);
        byte[] cmdJsonContent = Files.readAllBytes(cmdFile.toPath());
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(cmdJsonContent, PruneAndImportCommandApply.class);
    }
}
