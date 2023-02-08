package org.webcurator.core.store;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.webcurator.core.coordinator.WctCoordinatorClient;
import org.webcurator.domain.model.core.HarvestResultDTO;
import org.webcurator.domain.model.core.SeedHistoryDTO;
import org.webcurator.test.BaseWCTTest;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PywbWarcDepositTest extends BaseWCTTest<PywbWarcDeposit> {
    private final String baseDir = "/usr/local/wct/store";
    private final long targetInstanceId = 25L;
    private final int harvestResultNumber = 1;
    private final String baseUrl = "http://localhost:8080/wct";
    private WctCoordinatorClient wctClient;
    private RestTemplateBuilder restTemplateBuilder;


    public PywbWarcDepositTest() {
        super(PywbWarcDeposit.class, "/org/webcurator/core/harvester/coordinator/HarvestCoordinatorImplTest.xml");
    }

    public void setUp() throws Exception {
        super.setUp();

//        restTemplateBuilder = mock(RestTemplateBuilder.class);
        restTemplateBuilder = new RestTemplateBuilder();
        testInstance.setBaseUrl(baseUrl);
        testInstance.setRestTemplateBuilder(restTemplateBuilder);
        testInstance.setPywbManagerColl("my-web-archive");
        testInstance.setPywbManagerStoreDir(new File("/usr/local/wct/pywb"));
        testInstance.setPywbCDXQueryUrl("http://localhost:9090/my-web-archive/cdx");
        testInstance.setMaxTrySeconds(60L);
        testInstance.setRootStorePath(baseDir);
        testInstance.setWctClient(wctClient);
        testInstance.setPywbEnabled(true);

        SeedHistoryDTO seedHistoryDTO = new SeedHistoryDTO();
        seedHistoryDTO.setOid(3555);
        seedHistoryDTO.setPrimary(true);
        seedHistoryDTO.setSeed("https://www.rnz.co.nz/news/");
        seedHistoryDTO.setTargetInstanceOid(targetInstanceId);
        Set<SeedHistoryDTO> seedHistoryDTOSet = new HashSet<>();
        seedHistoryDTOSet.add(seedHistoryDTO);
        this.wctClient = mock(WctCoordinatorClient.class);
        when(this.wctClient.getSeedUrls(targetInstanceId, harvestResultNumber)).thenReturn(seedHistoryDTOSet);
        testInstance.setWctClient(this.wctClient);
    }

    @Ignore
    @Test
    public void testDepositWarc() throws Exception {
        HarvestResultDTO harvestResultDTO = new HarvestResultDTO();
        harvestResultDTO.setOid(3555L);
        harvestResultDTO.setHarvestNumber(harvestResultNumber);
        harvestResultDTO.setTargetInstanceOid(targetInstanceId);
        testInstance.depositWarc(harvestResultDTO);
        assert true;
    }

    @Ignore
    @Test
    public void testGetTimestamp() throws UnsupportedEncodingException {
        String seed = "https://www.rnz.co.nz/news/";
        String from = "20220615000000", to = "20230215235959";
        Map<String, File> mappedWarcFiles = new HashMap<>();
        mappedWarcFiles.put("IAH-20230207222548929-00000-9907~I7~8443.warc", null);

        String timestamp = testInstance.getTimestampOfSeed(seed, from, to, mappedWarcFiles);
        assert timestamp != null;
    }

    @Ignore
    @Test
    public void testGetSeedWithTimestamp() throws Exception {
        HarvestResultDTO harvestResultDTO = new HarvestResultDTO();
        harvestResultDTO.setOid(3555L);
        harvestResultDTO.setHarvestNumber(harvestResultNumber);
        harvestResultDTO.setTargetInstanceOid(targetInstanceId);
        List<SeedHistoryDTO> identities = testInstance.getSeedWithTimestamps(harvestResultDTO);
        assert identities != null;
        assert identities.size() == 1;
    }
}
