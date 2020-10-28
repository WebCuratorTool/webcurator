package org.webcurator.core.store.tools;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.store.DigitalAssetStoreClient;
import org.webcurator.core.util.Auditor;
import org.webcurator.domain.TargetInstanceDAO;
import org.webcurator.domain.model.core.HarvestResourceDTO;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.test.BaseWCTTest;

import java.util.ArrayList;
import java.util.List;

public class QualityReviewFacadeTest extends BaseWCTTest<QualityReviewFacade> {
    private String archivePath = "/org/webcurator/domain/model/core/archiveFiles";

    public QualityReviewFacadeTest() {
        super(QualityReviewFacade.class, "", false);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        testInstance = new QualityReviewFacade();
        testInstance.setTargetInstanceDao(mock(TargetInstanceDAO.class));
        testInstance.setAuditor(mock(Auditor.class));

        RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
        when(restTemplateBuilder.build()).thenReturn(mock(RestTemplate.class));
        DigitalAssetStoreClient dasClient = new DigitalAssetStoreClient("http://localhost:8082", restTemplateBuilder);
        dasClient.setFileUploadMode("stream");

        testInstance.setDigialAssetStore(dasClient);
    }

    @Test
    public void testCopyAndPrune() {
        try {
            HarvestResult hr = mock(HarvestResult.class);
            when(testInstance.getTargetInstanceDao().getHarvestResult(1L)).thenReturn(hr);

            TargetInstance ti = mock(TargetInstance.class);
            when(hr.getTargetInstance()).thenReturn(ti);
            when(hr.getHarvestNumber()).thenReturn(2);
            when(hr.getDerivedFrom()).thenReturn(1);

            List<HarvestResult> harvestResultList = new ArrayList<>();
            when(ti.getJobName()).thenReturn("5050");
            when(ti.getHarvestResults()).thenReturn(harvestResultList);

            testInstance.copyAndPrune(1, new ArrayList<String>(), new ArrayList<HarvestResourceDTO>(), "", new ArrayList<String>());
        } catch (DigitalAssetStoreException e) {
            e.printStackTrace();
            fail();
        }
        assert true;
    }
}
