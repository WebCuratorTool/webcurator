package org.webcurator.core.visualization;

import org.junit.Before;
import org.junit.Test;
import org.webcurator.core.exceptions.DigitalAssetStoreException;

import java.io.IOException;

public class TestVisualizationDirectoryManager extends BaseVisualizationTest {
    @Before
    public void initTest() throws IOException, DigitalAssetStoreException {
        super.initTest();
    }

    @Test
    public void testAllDirectories() {
        log.debug(directoryManager.getUploadDir(targetInstanceId));
        log.debug(directoryManager.getBaseLogDir(targetInstanceId));
        log.debug(directoryManager.getBaseReportDir(targetInstanceId));
        log.debug(directoryManager.getPatchLogDir("default", targetInstanceId, harvestResultNumber));
        log.debug(directoryManager.getPatchReportDir("default", targetInstanceId, harvestResultNumber));
    }
}
