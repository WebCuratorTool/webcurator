package org.webcurator.core.visualization.browser;

import org.apache.commons.httpclient.Header;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.webcurator.core.visualization.VisualizationDirectoryManager;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClientLocal;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VisWayBackClientLocalTest {
    private static final String baseDir = (new File(System.getProperty("user.dir"), "src/test/java/org/webcurator/core/store/warc/")).getAbsolutePath();

    private static final VisWayBackClientLocal testInstance = new VisWayBackClientLocal();
    //	private static String TestCARC = "IAH-20080610152724-00000-test.arc.gz";
//    private static String TestCWARC = "IAH-20080610152754-00000-test.warc.gz";

    private static final long targetInstanceOid = 14055;
    private static final int harvestResultNumber = 1;
    private static final String dbVersion = "4.0.0";

    @BeforeClass
    public static void initialise() throws Exception {
        // Create the archive folders
        File resultDir = new File(baseDir + "/" + targetInstanceOid + "/" + harvestResultNumber);
        if (!resultDir.exists()) {
            resultDir.mkdirs();

            // copy the test files into them
            // copy(baseDir + "/" + TestCARC, baseDir + "/" + targetInstanceOid + "/" + harvestResultNumber);
            // copy(baseDir + "/" + TestCWARC, baseDir + "/" + targetInstanceOid + "/" + harvestResultNumber);
        }
        VisualizationDirectoryManager directoryManager = new VisualizationDirectoryManager(baseDir, "logs", "reports");

        BDBNetworkMapPool dbPool = new BDBNetworkMapPool(baseDir, dbVersion);
        NetworkMapClient networkMapClient = new NetworkMapClientLocal(dbPool, null);
        testInstance.setBaseDir(baseDir);
        testInstance.setNetworkMapClient(networkMapClient);
        testInstance.setDirectoryManager(directoryManager);
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
}
