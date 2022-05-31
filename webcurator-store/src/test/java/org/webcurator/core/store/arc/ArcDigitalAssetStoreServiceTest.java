package org.webcurator.core.store.arc;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static reactor.core.publisher.Mono.when;

import java.io.*;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.webcurator.core.coordinator.WctCoordinatorClient;
import org.webcurator.core.visualization.VisualizationDirectoryManager;
import org.webcurator.core.visualization.VisualizationProcessorManager;
import org.webcurator.core.visualization.networkmap.NetworkMapDomainSuffix;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNode;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessor;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessorWarc;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClientLocal;
import org.webcurator.domain.model.core.SeedHistoryDTO;
import org.webcurator.test.BaseWCTStoreTest;
import org.webcurator.core.store.MockIndexer;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.apache.commons.httpclient.Header;

public class ArcDigitalAssetStoreServiceTest extends BaseWCTStoreTest<ArcDigitalAssetStoreService> {

    private static String baseDir = "src/test/java/org/webcurator/core/store/warc/";
    //	private static String TestCARC = "IAH-20080610152724-00000-test.arc.gz";
//    private static String TestCWARC = "IAH-20080610152754-00000-test.warc.gz";

    private static final long targetInstanceOid = 14055;
    private static final int harvestResultNumber = 1;
    private static final String dbVersion = "4.0.0";

    private class ARCFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.toLowerCase().endsWith(".arc") || name.toLowerCase().endsWith(".arc.gz")
                    || name.toLowerCase().endsWith(".warc") || name.toLowerCase().endsWith(".warc.gz"));
        }
    }

    public ArcDigitalAssetStoreServiceTest() {
        super(ArcDigitalAssetStoreService.class);
    }

    @BeforeClass
    public static void initialise() throws Exception {
        BaseWCTStoreTest.initialise();

        // Create the archive folders
        File resultDir = new File(baseDir + "/" + targetInstanceOid + "/" + harvestResultNumber);
        if (!resultDir.exists()) {
            resultDir.mkdirs();

            // copy the test files into them
            // copy(baseDir + "/" + TestCARC, baseDir + "/" + targetInstanceOid + "/" + harvestResultNumber);
            // copy(baseDir + "/" + TestCWARC, baseDir + "/" + targetInstanceOid + "/" + harvestResultNumber);
        }

        VisualizationProcessorManager processorManager = mock(VisualizationProcessorManager.class);
        VisualizationDirectoryManager directoryManager = new VisualizationDirectoryManager(baseDir, "logs", "reports");
        WctCoordinatorClient wctClient = mock(WctCoordinatorClient.class);
        Set<SeedHistoryDTO> seedHistoryDTOS = new LinkedHashSet<>();
        SeedHistoryDTO seedHistoryDTO = new SeedHistoryDTO();
        seedHistoryDTO.setSeed("https://www.kiwisaver.govt.nz/");
        seedHistoryDTO.setPrimary(true);
        seedHistoryDTO.setTargetInstanceOid(targetInstanceOid);
        seedHistoryDTO.setOid(0);
        seedHistoryDTOS.add(seedHistoryDTO);
        when(wctClient.getSeedUrls(targetInstanceOid, harvestResultNumber)).thenReturn(seedHistoryDTOS);

        NetworkMapDomainSuffix aTopDomainParser = new NetworkMapDomainSuffix();
        NetworkMapNode.setTopDomainParse(aTopDomainParser);

        BDBNetworkMapPool dbPool = new BDBNetworkMapPool(baseDir, dbVersion);

        NetworkMapClient networkMapClient = new NetworkMapClientLocal(dbPool, null);

        IndexProcessor indexProcessor = new IndexProcessorWarc(dbPool, targetInstanceOid, harvestResultNumber);
        indexProcessor.init(processorManager, directoryManager, wctClient, networkMapClient);
        indexProcessor.processInternal();
    }

    public void setUp() throws Exception {
        super.setUp();
        BDBNetworkMapPool bdbNetworkMapPool = new BDBNetworkMapPool(baseDir, dbVersion);
        VisualizationDirectoryManager directoryManager = new VisualizationDirectoryManager(baseDir, "logs", "report");
        WctCoordinatorClient wctCoordinatorClient = new WctCoordinatorClient("http://localhost:8080/", new RestTemplateBuilder());
        NetworkMapClient networkMapClient = new NetworkMapClientLocal(bdbNetworkMapPool, null);
        VisualizationProcessorManager processorManager = new VisualizationProcessorManager(directoryManager, wctCoordinatorClient, 1);
        testInstance.setBaseDir(baseDir);
//		testInstance.setArchive(new MockArchive());
        testInstance.setDasFileMover(new MockDasFileMover());
        testInstance.setIndexer(new MockIndexer());
        testInstance.setNetworkMapClient(networkMapClient);
    }

    @Ignore
    @Test
    public final void testARCGetResource() throws Exception {
        long length = 7109;
        long offset = 6865980;
        String name = "https://www.kiwisaver.govt.nz/";

        Path res = testInstance.getResource(targetInstanceOid, harvestResultNumber, name);
        assertNotNull(res);
        assertEquals(res.toFile().length(), length);
    }

    @Test
    public final void testWARCGetResource() throws Exception {
        long resLength = 18295;
        String name = "https://www.kiwisaver.govt.nz/";

        Path res = testInstance.getResource(targetInstanceOid, harvestResultNumber, name);
        assertNotNull(res);
        assertEquals(res.toFile().length(), resLength);
    }

    @Ignore
    @Test
    public final void testARCGetSmallResource() throws Exception {
        long length = 7109;
        long offset = 6865980;
        String name = "https://www.kiwisaver.govt.nz/";

        byte[] res = testInstance.getSmallResource(targetInstanceOid, harvestResultNumber, name);
        assertNotNull(res);
        assertEquals(res.length, length);
    }

    @Test
    public final void testWARCGetSmallResource() throws Exception {
        long resLength = 18295;
        String name = "https://www.kiwisaver.govt.nz/";

        byte[] res = testInstance.getSmallResource(targetInstanceOid, harvestResultNumber, name);
        assertNotNull(res);
        assertEquals(res.length, resLength);
    }

    @Ignore
    @Test
    public final void testARCGetHeaders() throws Exception {
        long length = 7109;
        long offset = 6865980;
        String name = "https://www.kiwisaver.govt.nz/";

        List<Header> headers = testInstance.getHeaders(targetInstanceOid, harvestResultNumber, name);
        assertNotNull(headers);
        assertEquals(4, headers.size());
    }

    @Test
    public final void testWARCGetHeaders() throws Exception {
        String name = "https://www.kiwisaver.govt.nz/";

        List<Header> headers = testInstance.getHeaders(targetInstanceOid, harvestResultNumber, name);
        assertNotNull(headers);
        assertEquals(13, headers.size());
    }

    private static void copy(String fromFileName, String toFileName) throws IOException {
        File fromFile = new File(fromFileName);
        File toFile = new File(toFileName);

        if (!fromFile.exists())
            throw new IOException("FileCopy: " + "no such source file: " + fromFileName);
        if (!fromFile.isFile())
            throw new IOException("FileCopy: " + "can't copy directory: " + fromFileName);
        if (!fromFile.canRead())
            throw new IOException("FileCopy: " + "source file is unreadable: " + fromFileName);

        if (toFile.isDirectory()) {
            toFile = new File(toFile, fromFile.getName());
        }

        if (toFile.exists()) {
            if (!toFile.canWrite())
                throw new IOException("FileCopy: " + "destination file is unwriteable: " + toFileName);
        } else {
            String parent = toFile.getParent();
            if (parent == null)
                parent = System.getProperty("user.dir");
            File dir = new File(parent);
            if (!dir.exists())
                throw new IOException("FileCopy: " + "destination directory doesn't exist: " + parent);
            if (dir.isFile())
                throw new IOException("FileCopy: " + "destination is not a directory: " + parent);
            if (!dir.canWrite())
                throw new IOException("FileCopy: " + "destination directory is unwriteable: " + parent);
        }

        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1)
                to.write(buffer, 0, bytesRead); // write
        } finally {
            if (from != null) {
                try {
                    from.close();
                } catch (IOException e) {
                    log.debug("Failed to close 'from' stream: " + e.getMessage());
                }
            }
            if (to != null) {
                try {
                    to.close();
                } catch (IOException e) {
                    log.debug("Failed to close 'to' stream: " + e.getMessage());
                }
            }
        }
    }

    private static void delDir(File directory) {
        // Ensure the destination directory exists.
        if (directory.exists()) {
            // Get all the files from the directory.
            File[] files = directory.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    delDir(file);
                } else {
                    if (file.delete()) {
                        log.debug("Deleted File: " + file.getAbsolutePath());
                    } else {
                        log.debug("Failed to delete File: " + file.getAbsolutePath());
                    }
                }
            }

            if (directory.delete()) {
                log.debug("Deleted Directory: " + directory.getAbsolutePath());
            } else {
                log.debug("Failed to delete Directory: " + directory.getAbsolutePath());
            }
        }
    }

    @Ignore
    @Test
    public void testGetDownloadFileURL() {
        try {
            File f = File.createTempFile("download", ".temp");
            f = testInstance.getDownloadFileURL("team.png", f);
            assert f.exists();

            f.delete();
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }

    }

    @Test
    public void testScreenshotFail() {
        Boolean screenshotResult = null;
        testInstance.setEnableScreenshots(true);

        try {
            Map<String, String> identifiers = new HashMap<>();
            screenshotResult = testInstance.createScreenshots(identifiers);
        } catch (DigitalAssetStoreException e) {
            assert screenshotResult == false;
        }
        System.out.println(screenshotResult);
        assert screenshotResult == false;


    }
}
