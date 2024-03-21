package org.webcurator.core.screenshot;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.domain.model.core.SeedHistoryDTO;
import org.webcurator.test.BaseWCTTest;
import org.webcurator.test.WCTTestUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.attribute.UserDefinedFileAttributeView;

import static org.junit.Assert.assertTrue;

public class ScreenshotGeneratorTest extends BaseWCTTest<ScreenshotGenerator> {
    private final static String archivePath = "/org/webcurator/core/store/archiveFiles";
    private final static String windowSizeCommand = "native filepath=%image.png% url=%url% width=%width% height=%height%";
    private final static String screenSizeCommand = "native filepath=%image.png% url=%url% width=1400 height=800";
    private final static String fullpageSizeCommand = "native filepath=%image.png% url=%url%";
    private final static String baseDir = "/usr/local/wct/store";
//    private final String harvestWaybackViewerBaseUrl = "http://localhost:1080/my-web-archive/";

    private final long tiOid = 5000L;
//    private final int harvestNumber = 1;

    public ScreenshotGeneratorTest() {
        super(ScreenshotGenerator.class, "/org/webcurator/core/harvester/coordinator/HarvestCoordinatorImplTest.xml");
    }

    public void setUp() throws Exception {
        super.setUp();
        testInstance.setWindowSizeCommand(windowSizeCommand);
        testInstance.setScreenSizeCommand(screenSizeCommand);
        testInstance.setFullpageSizeCommand(fullpageSizeCommand);
        testInstance.setBaseDir(baseDir);
    }

    @Ignore
    @Test
    public void testLiveScreenshot() throws DigitalAssetStoreException {
        ScreenshotIdentifierCommand identifier = new ScreenshotIdentifierCommand();
        identifier.setTiOid(tiOid);
        identifier.setHarvestNumber(0);
        identifier.setScreenshotType(ScreenshotType.live);
        SeedHistoryDTO seedHistoryDTO = new SeedHistoryDTO();
        seedHistoryDTO.setSeed("https://www.rnz.co.nz/news");
        identifier.addSeed(seedHistoryDTO);

        boolean rstScreenshot = testInstance.createScreenshots(identifier);
        assertTrue(rstScreenshot);
    }

    @Ignore
    @Test
    public void testHarvestedScreenshot() throws DigitalAssetStoreException, IOException {
        callWayback(1, "owb", "2.4.0", "http://localhost:9090/wayback/");
        callWayback(2, "pywb", "2.7.3", "http://localhost:1080/my-web-archive/");
        callWayback(3, "pywb", "2.6.7", "http://localhost:2080/my-web-archive/");
        callWayback(4, "pywb", "2.3.0", "http://localhost:3080/my-web-archive/");
    }

    @Ignore
    @Test
    public void testAttributes() throws IOException {
        String toolUsed="native";
        File file=new File("/usr/local/wct/store/93/1/_snapshots/93_1_94_live_fullpage.png");
        UserDefinedFileAttributeView attributeView = Files.getFileAttributeView(file.toPath(), UserDefinedFileAttributeView.class);
        attributeView.write("screenshotTool-" + toolUsed, Charset.defaultCharset().encode(toolUsed));
        attributeView.list().forEach(System.out::println);
    }

    private void callWayback(int harvestNumber, String waybackName, String waybackVersion, String waybackViewUrl) throws DigitalAssetStoreException, IOException {
        String warcFile = "IAH-20230205231545437-00000-3211~I7~8443.warc";
        File inputDirectory = WCTTestUtils.getResourceAsFile(archivePath);
        File inputFile = new File(inputDirectory, warcFile);
        File outputDirectory = new File(baseDir + File.separator + tiOid + File.separator + harvestNumber);
        if (!outputDirectory.exists()) {
            boolean ret = outputDirectory.mkdir();
            assertTrue(ret);
        }
        File outputFile = new File(outputDirectory, warcFile);
        IOUtils.copy(Files.newInputStream(inputFile.toPath()), Files.newOutputStream(outputFile.toPath()));

        ScreenshotIdentifierCommand identifier = new ScreenshotIdentifierCommand();
        identifier.setTiOid(tiOid);
        identifier.setHarvestNumber(harvestNumber);
        identifier.setScreenshotType(ScreenshotType.harvested);
        SeedHistoryDTO seedHistoryDTO = new SeedHistoryDTO();
        seedHistoryDTO.setSeed("https://www.rnz.co.nz/news");
        identifier.addSeed(seedHistoryDTO);

        testInstance.setWaybackName(waybackName);
        testInstance.setWaybackVersion(waybackVersion);
        testInstance.setHarvestWaybackViewerBaseUrl(waybackViewUrl);

        boolean rstScreenshot = testInstance.createScreenshots(identifier);
        assertTrue(rstScreenshot);
    }
}
