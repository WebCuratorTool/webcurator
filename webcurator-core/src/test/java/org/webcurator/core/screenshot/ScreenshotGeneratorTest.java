package org.webcurator.core.screenshot;

import org.junit.Ignore;
import org.junit.Test;
import org.webcurator.core.scheduler.MockTargetInstanceManager;
import org.webcurator.domain.TargetInstanceDAO;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.SeedHistory;
import org.webcurator.domain.model.core.SeedHistoryDTO;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.test.BaseWCTTest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class ScreenshotGeneratorTest extends BaseWCTTest<ScreenshotGenerator> {
    private final String windowSizeCommand = "native filepath=%image.png% url=%url% width=%width% height=%height%";
    private final String screenSizeCommand = "native filepath=%image.png% url=%url% width=1400 height=800";
    private final String fullpageSizeCommand = "native filepath=%image.png% url=%url%";
    private final String baseDir = "/usr/local/wct/store";
    private final String harvestWaybackViewerBaseUrl = "http://localhost:9090/my-web-archive/";

    private TargetInstanceDAO tiDao;

    private final long tiOid = 5000L;
    private final int harvestNumber = 1;

    public ScreenshotGeneratorTest() {
        super(ScreenshotGenerator.class, "/org/webcurator/core/harvester/coordinator/HarvestCoordinatorImplTest.xml");
    }

    public void setUp() throws Exception {
        super.setUp();
        testInstance.setWindowSizeCommand(this.windowSizeCommand);
        testInstance.setScreenSizeCommand(this.screenSizeCommand);
        testInstance.setFullpageSizeCommand(this.fullpageSizeCommand);
        testInstance.setBaseDir(this.baseDir);
        testInstance.setHarvestWaybackViewerBaseUrl(this.harvestWaybackViewerBaseUrl);

        MockTargetInstanceManager mockTargetInstanceManager = new MockTargetInstanceManager(testFile);
        tiDao = mockTargetInstanceManager.getTargetInstanceDAO();
    }

    @Ignore
    @Test
    public void testLiveScreenshot() {
        TargetInstance ti = tiDao.load(tiOid);
        HarvestResult hr = ti.getHarvestResult(harvestNumber);

        Set<SeedHistory> seedHistorySet = ti.getSeedHistory();
        for (SeedHistory seedHistory : seedHistorySet) {
            String timestamp = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
            SeedHistoryDTO seedHistoryDTO = new SeedHistoryDTO(seedHistory);
            seedHistoryDTO.setTimestamp(timestamp);
            boolean rstScreenshot = testInstance.createScreenshots(seedHistoryDTO, tiOid, ScreenshotType.live, harvestNumber);
            assertTrue(rstScreenshot);
        }
    }

    @Ignore
    @Test
    public void testHarvestedScreenshot() {
        TargetInstance ti = tiDao.load(tiOid);
        HarvestResult hr = ti.getHarvestResult(harvestNumber);

        Set<SeedHistory> seedHistorySet = ti.getSeedHistory();
        for (SeedHistory seedHistory : seedHistorySet) {
            String timestamp = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
            SeedHistoryDTO seedHistoryDTO = new SeedHistoryDTO(seedHistory);
            seedHistoryDTO.setTimestamp(timestamp);
            boolean rstScreenshot = testInstance.createScreenshots(seedHistoryDTO, tiOid, ScreenshotType.harvested, harvestNumber);
            assertTrue(rstScreenshot);
        }
    }
}