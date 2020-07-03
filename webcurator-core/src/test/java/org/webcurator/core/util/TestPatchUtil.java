package org.webcurator.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.webcurator.core.visualization.VisualizationAbstractCommandApply;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.domain.model.core.HarvestResult;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TestPatchUtil {
    private static final String baseDir = "/usr/local/wct/store";
    private final long targetInstanceId = 5010;
    private final int harvestResultNumber = 2;
    private final PruneAndImportCommandApply cmd = new PruneAndImportCommandApply();
    private File jobFile;
    private File historyFile;

    @Before
    public void initTest() {
        cmd.setTargetInstanceId(targetInstanceId);
        cmd.setHarvestResultNumber(1);
        cmd.setNewHarvestResultNumber(harvestResultNumber);

        jobFile = new File(baseDir, "jobs" + File.separator + getJobFileName(HarvestResult.PATCH_STAGE_TYPE_MODIFYING));
        historyFile = new File(baseDir, "history" + File.separator + getJobFileName(HarvestResult.PATCH_STAGE_TYPE_MODIFYING));
    }

    @Test
    public void testSave() throws IOException {
        PatchUtil.modifier.savePatchJob(baseDir, cmd);


        assertTrue(jobFile.exists());

        boolean deleteResult = jobFile.delete();
        assertTrue(deleteResult);

        assertFalse(jobFile.exists());
    }

    @Test
    public void testRead() throws IOException {
        PatchUtil.modifier.savePatchJob(baseDir, cmd);
        assertTrue(jobFile.exists());

        PruneAndImportCommandApply modifyingCommand = (PruneAndImportCommandApply) PatchUtil.modifier.readPatchJob(baseDir, targetInstanceId, harvestResultNumber);
        assertNotNull(modifyingCommand);
        assertEquals(modifyingCommand.getTargetInstanceId(), targetInstanceId);
        assertEquals(modifyingCommand.getNewHarvestResultNumber(), harvestResultNumber);

        boolean deleteResult = jobFile.delete();
        assertTrue(deleteResult);

        assertFalse(jobFile.exists());
    }

    @Test
    public void testHistoryRead() throws IOException {
        PatchUtil.modifier.savePatchJob(baseDir, cmd);
        assertTrue(jobFile.exists());

        PatchUtil.modifier.moveJob2History(baseDir, targetInstanceId, harvestResultNumber);
        assertTrue(historyFile.exists());
        assertFalse(jobFile.exists());

        PruneAndImportCommandApply modifyingCommand = (PruneAndImportCommandApply) PatchUtil.modifier.readHistoryPatchJob(baseDir, targetInstanceId, harvestResultNumber);
        assertNotNull(modifyingCommand);
        assertEquals(modifyingCommand.getTargetInstanceId(), targetInstanceId);
        assertEquals(modifyingCommand.getNewHarvestResultNumber(), harvestResultNumber);

        boolean deleteResult = historyFile.delete();
        assertTrue(deleteResult);
    }

    @Test
    public void testList() throws IOException {
        PatchUtil.modifier.savePatchJob(baseDir, cmd);
        assertTrue(jobFile.exists());

        List<VisualizationAbstractCommandApply> list = PatchUtil.modifier.listPatchJob(baseDir);
        assertNotNull(list);
        assertTrue(list.size() > 0);

        boolean deleteResult = jobFile.delete();
        assertTrue(deleteResult);

        assertFalse(jobFile.exists());
    }

    @Test
    public void testMoveToHistory() throws IOException {
        PatchUtil.modifier.savePatchJob(baseDir, cmd);
        assertTrue(jobFile.exists());

        PatchUtil.modifier.moveJob2History(baseDir, targetInstanceId, harvestResultNumber);
        assertFalse(jobFile.exists());
        assertTrue(historyFile.exists());
    }

    private String getJobFileName(String prefix) {
        return String.format("%s_%d_%d.json", prefix, targetInstanceId, harvestResultNumber);
    }
}
