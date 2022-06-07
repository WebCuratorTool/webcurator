package org.webcurator.ui.target.controller;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.springframework.context.MockMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.archive.MockSipBuilder;
import org.webcurator.core.harvester.agent.MockHarvestAgentFactory;
import org.webcurator.core.harvester.coordinator.HarvestAgentManagerImpl;
import org.webcurator.core.coordinator.WctCoordinatorImpl;
import org.webcurator.core.notification.MockInTrayManager;
import org.webcurator.core.scheduler.MockTargetInstanceManager;
import org.webcurator.core.screenshot.ScreenshotClient;
import org.webcurator.core.store.DigitalAssetStore;
import org.webcurator.core.store.DigitalAssetStoreFactory;
import org.webcurator.core.targets.MockTargetManager;
import org.webcurator.domain.MockTargetInstanceDAO;
import org.webcurator.domain.TargetInstanceDAO;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;
import org.webcurator.domain.model.core.harvester.agent.HarvesterStatusDTO;
import org.webcurator.test.BaseWCTTest;
import org.webcurator.ui.target.command.TargetInstanceCommand;
import org.webcurator.common.util.DateUtils;
import org.webcurator.ui.target.validator.MockHarvestNowValidator;

public class HarvestNowControllerTest extends BaseWCTTest<HarvestNowController> {

    private TargetInstanceDAO tidao;
    private WctCoordinatorImpl hc;

    public HarvestNowControllerTest() {
        super(HarvestNowController.class,
                "/org/webcurator/ui/target/controller/HarvestNowControllerTest.xml");

    }

    //Override BaseWCTTest setup method
    public void setUp() throws Exception {
        //call the overridden method as well
        super.setUp();

        //add the extra bits
        tidao = new MockTargetInstanceDAO(testFile);

        DateUtils.get().setMessageSource(new MockMessageSource());
        MockTargetInstanceManager tim = new MockTargetInstanceManager(testFile);

        tim.setTargetInstanceDao(tidao);

        hc = new WctCoordinatorImpl();

        hc.setTargetInstanceManager(tim);
        hc.setTargetManager(new MockTargetManager(testFile));
        hc.setInTrayManager(new MockInTrayManager(testFile));
        hc.setSipBuilder(new MockSipBuilder(testFile));
        HarvestAgentManagerImpl harvestAgentManager = new HarvestAgentManagerImpl();
        harvestAgentManager.setHarvestAgentFactory(new MockHarvestAgentFactory());
        harvestAgentManager.setTargetInstanceManager(tim);
        harvestAgentManager.setTargetInstanceDao(tidao);
        hc.setHarvestAgentManager(harvestAgentManager);
        hc.setTargetInstanceDao(tidao);

        ScreenshotClient screenshotClient=mock(ScreenshotClient.class);
        when(screenshotClient.createScreenshots(any())).thenReturn(true);
        hc.setScreenshotClient(screenshotClient);

        testInstance.setWctCoordinator(hc);
        testInstance.setTargetInstanceDAO(tidao);
        testInstance.setMessageSource(new MockMessageSource());
    }

    @Test
    public final void testShowForm() {
        try {
            assertNull(testInstance.showForm());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public final void testProcessFormSubmission() throws Exception {

        TargetInstance ti = tidao.load(5001L);
        ti.setState(TargetInstance.STATE_SCHEDULED);

        HashMap<String, HarvesterStatusDTO> aHarvesterStatus = new HashMap<String, HarvesterStatusDTO>();

        aHarvesterStatus.put("Target-5002", getStatusDTO("Running"));
        HarvestAgentStatusDTO aStatus = new HarvestAgentStatusDTO();
        aStatus.setName("Test Agent");
        aStatus.setHarvesterStatus(aHarvesterStatus);
        aStatus.setMaxHarvests(2);
        hc.heartbeat(aStatus);


        HttpServletRequest aReq = new MockHttpServletRequest();
        TargetInstanceCommand aCmd = new TargetInstanceCommand();
        aCmd.setCmd(TargetInstanceCommand.ACTION_HARVEST);
        aCmd.setAgent("Test Agent");
        aCmd.setTargetInstanceId(5001L);
        BindingResult bindingResult = new BindException(aCmd, TargetInstanceCommand.ACTION_HARVEST);
        ReflectionTestUtils.setField(testInstance, "harvestNowValidator", new MockHarvestNowValidator());

        DigitalAssetStoreFactory mockDasFactory = mock(DigitalAssetStoreFactory.class);
        hc.setDigitalAssetStoreFactory(mockDasFactory);
        DigitalAssetStore mockDas = mock(DigitalAssetStore.class);
        when(mockDasFactory.getDAS()).thenReturn(mockDas);
//        when(mockDasFactory.getDAS().createScreenshots(any(Map.class))).thenReturn(true);

        ModelAndView mav = testInstance.processFormSubmission(aCmd, bindingResult, aReq);
        assertNotNull(mav);
        assertEquals(TargetInstance.STATE_RUNNING, ti.getState());
    }

    @Test
    public final void testProcessFormSubmissionPaused() {

        TargetInstance ti = tidao.load(5001L);
        ti.setState(TargetInstance.STATE_SCHEDULED);

        hc.pauseQueue();

        HashMap<String, HarvesterStatusDTO> aHarvesterStatus = new HashMap<String, HarvesterStatusDTO>();

        aHarvesterStatus.put("Target-5002", getStatusDTO("Running"));
        HarvestAgentStatusDTO aStatus = new HarvestAgentStatusDTO();
        aStatus.setName("Test Agent");
        aStatus.setHarvesterStatus(aHarvesterStatus);
        aStatus.setMaxHarvests(2);
        hc.heartbeat(aStatus);


        HttpServletRequest aReq = new MockHttpServletRequest();
        TargetInstanceCommand aCmd = new TargetInstanceCommand();
        aCmd.setCmd(TargetInstanceCommand.ACTION_HARVEST);
        aCmd.setAgent("Test Agent");
        aCmd.setTargetInstanceId(5001L);
        BindingResult bindingResult = new BindException(aCmd, TargetInstanceCommand.ACTION_HARVEST);
        ReflectionTestUtils.setField(testInstance, "harvestNowValidator", new MockHarvestNowValidator());

        try {
            ModelAndView mav = testInstance.processFormSubmission(aCmd, bindingResult, aReq);
            assertTrue(mav != null);
            assertTrue(ti.getState().equals(TargetInstance.STATE_SCHEDULED));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public final void testProcessFormSubmissionAgentPaused() throws Exception {

        TargetInstance ti = tidao.load(5001L);
        ti.setState(TargetInstance.STATE_SCHEDULED);

        HashMap<String, HarvesterStatusDTO> aHarvesterStatus = new HashMap<String, HarvesterStatusDTO>();

        aHarvesterStatus.put("Target-5002", getStatusDTO("Running"));
        HarvestAgentStatusDTO aStatus = new HarvestAgentStatusDTO();
        aStatus.setName("Test Agent");
        aStatus.setHarvesterStatus(aHarvesterStatus);
        aStatus.setMaxHarvests(2);

        aStatus.setAcceptTasks(false);

        hc.heartbeat(aStatus);


        HttpServletRequest aReq = new MockHttpServletRequest();
        TargetInstanceCommand aCmd = new TargetInstanceCommand();
        aCmd.setCmd(TargetInstanceCommand.ACTION_HARVEST);
        aCmd.setAgent("Test Agent");
        aCmd.setTargetInstanceId(5001L);
        BindingResult bindingResult = new BindException(aCmd, TargetInstanceCommand.ACTION_HARVEST);
        ReflectionTestUtils.setField(testInstance, "harvestNowValidator", new MockHarvestNowValidator());

        ModelAndView mav = testInstance.processFormSubmission(aCmd, bindingResult, aReq);
        assertTrue(mav != null);
        assertTrue(ti.getState().equals(TargetInstance.STATE_SCHEDULED));
    }

    private HarvesterStatusDTO getStatusDTO(String aStatus) {
        HarvesterStatusDTO sdto = new HarvesterStatusDTO();
        sdto.setStatus(aStatus);
        return sdto;
    }
}
