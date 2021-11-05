package org.webcurator.core.harvester.coordinator;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.core.store.DigitalAssetStoreClient;
import org.webcurator.domain.TargetInstanceDAO;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.Target;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.test.BaseWCTTest;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HarvestQaManagerImplTest extends BaseWCTTest<HarvestQaManagerImpl> {
    public HarvestQaManagerImplTest() {
        super(HarvestQaManagerImpl.class, "", false);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        TargetInstanceDAO targetInstanceDAO = mock(TargetInstanceDAO.class);
        TargetInstanceManager targetInstanceManager = mock(TargetInstanceManager.class);

        RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
        when(restTemplateBuilder.build()).thenReturn(mock(RestTemplate.class));
        DigitalAssetStoreClient dasClient = new DigitalAssetStoreClient("http://localhost:8082", restTemplateBuilder);
        dasClient.setFileUploadMode("stream");


        testInstance = new HarvestQaManagerImpl();
        testInstance.setTargetInstanceDao(targetInstanceDAO);
        testInstance.setTargetInstanceManager(targetInstanceManager);
    }

    @Test
    public void testAutoPrune() {
        HarvestResult hr = mock(HarvestResult.class);
        when(testInstance.getTargetInstanceDao().getHarvestResult(1L)).thenReturn(hr);

        TargetInstance ti = mock(TargetInstance.class);
        when(hr.getTargetInstance()).thenReturn(ti);
        when(hr.getHarvestNumber()).thenReturn(2);
        when(hr.getDerivedFrom()).thenReturn(1);

        List<HarvestResult> harvestResultList = new ArrayList<>();
        when(ti.getJobName()).thenReturn("5050");
        when(ti.getHarvestResults()).thenReturn(harvestResultList);

        Target target = mock(Target.class);
        when(ti.getTarget()).thenReturn(target);

        when(target.isAutoPrune()).thenReturn(true);

        try {
            testInstance.autoPrune(ti);
        } catch (DigitalAssetStoreException e) {
            e.printStackTrace();
            assert false;
        }
        assert true;
    }

    @Ignore
    @Test
    public void testRunQaRecommendationService() {
        HarvestResult hr = mock(HarvestResult.class);
        when(testInstance.getTargetInstanceDao().getHarvestResult(1L)).thenReturn(hr);

        TargetInstance ti = mock(TargetInstance.class);
        when(hr.getTargetInstance()).thenReturn(ti);
        when(hr.getHarvestNumber()).thenReturn(2);
        when(hr.getDerivedFrom()).thenReturn(1);

        testInstance.runQaRecommentationService(ti);
    }
}
