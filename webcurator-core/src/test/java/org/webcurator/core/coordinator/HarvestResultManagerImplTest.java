package org.webcurator.core.coordinator;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import static org.junit.Assert.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.webcurator.core.scheduler.MockTargetInstanceManager;
import org.webcurator.core.visualization.VisualizationProgressView;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;
import org.webcurator.domain.MockTargetInstanceDAO;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.HarvestResultDTO;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.test.BaseWCTTest;

@AutoConfigureMockMvc
public class HarvestResultManagerImplTest extends BaseWCTTest<HarvestResultManagerImpl> {
    private HarvestResultManagerImpl harvestResultManager = new HarvestResultManagerImpl();
    private MockTargetInstanceDAO mockTargetInstanceDAO;
    private MockTargetInstanceManager mockTargetInstanceManager = null;
    private NetworkMapClient networkMapClient = mock(NetworkMapClient.class);

    public HarvestResultManagerImplTest() {
        super(HarvestResultManagerImpl.class, "/org/webcurator/core/harvester/coordinator/HarvestCoordinatorImplTest.xml");
    }

    // Override BaseWCTTest setup method
    public void setUp() throws Exception {
        // call the overridden method as well
        super.setUp();

        mockTargetInstanceDAO = new MockTargetInstanceDAO(testFile);
        mockTargetInstanceManager = new MockTargetInstanceManager(testFile);
        mockTargetInstanceManager.setTargetInstanceDao(mockTargetInstanceDAO);

        harvestResultManager.setTargetInstanceManager(mockTargetInstanceManager);
        harvestResultManager.setNetworkMapClient(networkMapClient);
    }

    @Test
    public void testUpdateHarvestResultStatusOfModify() {
        long targetInstanceId = 5000L;
        int harvestNumber = 2;

        int[] states = {HarvestResult.STATE_MODIFYING, HarvestResult.STATE_INDEXING};
        int[] statuses = {HarvestResult.STATUS_FINISHED, HarvestResult.STATUS_PAUSED, HarvestResult.STATUS_RUNNING, HarvestResult.STATUS_SCHEDULED, HarvestResult.STATUS_TERMINATED, HarvestResult.STATUS_UNASSESSED};
        TargetInstance ti = mockTargetInstanceDAO.load(targetInstanceId);
        ti.setState(TargetInstance.STATE_PATCHING);

        for (int state : states) {
            for (int status : statuses) {
                NetworkMapResult networkMapResultHarvestResultDTO = genNetworkMapResultOfHarvestResultDTO(targetInstanceId, harvestNumber, state, status);
                NetworkMapResult networkMapResultProgress = genNetworkMapResultOfProgress(targetInstanceId, harvestNumber, state, status);

                when(networkMapClient.getProcessingHarvestResultDTO(anyLong(), anyInt())).thenReturn(networkMapResultHarvestResultDTO);
                when(networkMapClient.getProgress(anyLong(), anyInt())).thenReturn(networkMapResultProgress);

                harvestResultManager.updateHarvestResultStatus(targetInstanceId, harvestNumber, HarvestResult.STATE_MODIFYING, HarvestResult.STATUS_RUNNING);

                HarvestResultDTO hrDTO = harvestResultManager.getHarvestResultDTO(targetInstanceId, harvestNumber);

                assertEquals(state, hrDTO.getState());
                assertEquals(status, hrDTO.getStatus());
            }
        }
    }

    @Test
    public void testUpdateHarvestResultStatusOfNonePatch() {
        long targetInstanceId = 5000L;
        int harvestNumber = 1;
        int state = HarvestResult.STATE_CRAWLING;
        int status = HarvestResult.STATUS_RUNNING;

        TargetInstance ti = mockTargetInstanceDAO.load(targetInstanceId);
        ti.setState(TargetInstance.STATE_RUNNING);

        NetworkMapResult networkMapResultHarvestResultDTO = genNetworkMapResultOfHarvestResultDTO(targetInstanceId, harvestNumber, state, status);
        NetworkMapResult networkMapResultProgress = genNetworkMapResultOfProgress(targetInstanceId, harvestNumber, state, status);

        when(networkMapClient.getProcessingHarvestResultDTO(anyLong(), anyInt())).thenReturn(networkMapResultHarvestResultDTO);
        when(networkMapClient.getProgress(anyLong(), anyInt())).thenReturn(networkMapResultProgress);

        harvestResultManager.updateHarvestResultStatus(targetInstanceId, harvestNumber, HarvestResult.STATE_MODIFYING, HarvestResult.STATUS_RUNNING);

        HarvestResultDTO hrDTO = harvestResultManager.getHarvestResultDTO(targetInstanceId, harvestNumber);

        assertEquals(HarvestResult.STATE_UNASSESSED, hrDTO.getState());
        assertEquals(HarvestResult.STATUS_UNASSESSED, hrDTO.getStatus());
    }


    private NetworkMapResult genNetworkMapResultOfHarvestResultDTO(long targetInstanceId, int harvestNumber, int state, int status) {
        NetworkMapResult networkMapResult = new NetworkMapResult();
        networkMapResult.setRspCode(NetworkMapResult.RSP_CODE_SUCCESS);

        VisualizationProgressView progressView = new VisualizationProgressView();
        progressView.setStage(HarvestResult.PATCH_STAGE_TYPE_MODIFYING);
        progressView.setTargetInstanceId(targetInstanceId);
        progressView.setHarvestResultNumber(harvestNumber);
        progressView.setState(state);
        progressView.setStatus(status);

        HarvestResultDTO harvestResultDTO = new HarvestResultDTO();
        harvestResultDTO.setTargetInstanceOid(targetInstanceId);
        harvestResultDTO.setHarvestNumber(harvestNumber);
        harvestResultDTO.setProgressView(progressView);

        networkMapResult.setPayload(obj2Json(harvestResultDTO));

        return networkMapResult;
    }

    private NetworkMapResult genNetworkMapResultOfProgress(long targetInstanceId, int harvestNumber, int state, int status) {
        NetworkMapResult networkMapResult = new NetworkMapResult();
        networkMapResult.setRspCode(NetworkMapResult.RSP_CODE_SUCCESS);

        VisualizationProgressView progressView = new VisualizationProgressView();
        progressView.setStage(HarvestResult.PATCH_STAGE_TYPE_MODIFYING);
        progressView.setTargetInstanceId(targetInstanceId);
        progressView.setHarvestResultNumber(harvestNumber);
        progressView.setState(state);
        progressView.setStatus(status);

        networkMapResult.setPayload(obj2Json(progressView));

        return networkMapResult;
    }
}
