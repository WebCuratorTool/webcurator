package org.webcurator.core.visualization;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisualizationDirectoryManagerTest {
    private static final Logger log = LoggerFactory.getLogger(VisualizationDirectoryManagerTest.class);
    private static final VisualizationDirectoryManager directoryManager = MockVisualizationDirectoryManager.getDirectoryManagerInstance();
    private static final long targetInstanceId = 5010;
    private static final int harvestResultNumber = 1;

    @Test
    public void testAllDirectories() {
        log.debug(directoryManager.getUploadDir(targetInstanceId));
        log.debug(directoryManager.getBaseLogDir(targetInstanceId));
        log.debug(directoryManager.getBaseReportDir(targetInstanceId));
        log.debug(directoryManager.getPatchLogDir("default", targetInstanceId, harvestResultNumber));
        log.debug(directoryManager.getPatchReportDir("default", targetInstanceId, harvestResultNumber));
    }
}
