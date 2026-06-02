package org.webcurator.core.store;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.webcurator.domain.model.core.HarvestResultDTO;
import org.webcurator.test.BaseWCTTest;
import org.webcurator.test.WCTTestUtils;

import java.io.File;

public class PywbIndexerTest extends BaseWCTTest<PywbIndexer> {
    private final static String archivePath = "/org/webcurator/core/store/archiveFiles";
    private final static String pywbManagerColl = "my-web-archive";
    private final static File pywbManagerStoreDir = new File("/usr/local/wct/pywb");
    private final static long targetInstanceId = 19L;
    private final static int harvestResultNumber = 1;

    public PywbIndexerTest() {
        super(PywbIndexer.class, "");
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        testInstance.setPywbManagerColl(pywbManagerColl);
        testInstance.setPywbManagerStoreDir(pywbManagerStoreDir);
        testInstance.setEnabled(true);
    }

    //    @Ignore
    @Ignore
    @Test
    public void testIndexer() {
        HarvestResultDTO harvestResultDTO = new HarvestResultDTO();
        harvestResultDTO.setOid(targetInstanceId);
        harvestResultDTO.setHarvestNumber(harvestResultNumber);
        harvestResultDTO.setTargetInstanceOid(targetInstanceId);
        File directory = WCTTestUtils.getResourceAsFile(archivePath);
        testInstance.initialise(harvestResultDTO, directory);
        testInstance.indexFiles(targetInstanceId);
        assert true;
    }

    @Ignore
    @Test
    public void testIndexerIndividualCollection() {
        HarvestResultDTO harvestResultDTO = new HarvestResultDTO();
        harvestResultDTO.setOid(targetInstanceId);
        harvestResultDTO.setHarvestNumber(harvestResultNumber);
        harvestResultDTO.setTargetInstanceOid(targetInstanceId);
        File directory = WCTTestUtils.getResourceAsFile(archivePath);
        testInstance.setIndividualCollectionMode(true);
        testInstance.initialise(harvestResultDTO, directory);
        testInstance.indexFiles(targetInstanceId);
        assert true;
    }

    @Ignore
    @Test
    public void testRemoveIndexIndividualCollection() {
        HarvestResultDTO harvestResultDTO = new HarvestResultDTO();
        harvestResultDTO.setOid(targetInstanceId);
        harvestResultDTO.setHarvestNumber(harvestResultNumber);
        harvestResultDTO.setTargetInstanceOid(targetInstanceId);
        File directory = WCTTestUtils.getResourceAsFile(archivePath);
        testInstance.setIndividualCollectionMode(true);
        testInstance.initialise(harvestResultDTO, directory);
        testInstance.removeIndex(targetInstanceId);
        assert true;
    }
}
