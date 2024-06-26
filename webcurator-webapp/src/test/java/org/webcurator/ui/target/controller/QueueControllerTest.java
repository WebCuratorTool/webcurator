package org.webcurator.ui.target.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.context.MockMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.common.ui.CommandConstants;
import org.webcurator.core.agency.AgencyUserManager;
import org.webcurator.core.agency.MockAgencyUserManager;
import org.webcurator.core.archive.MockSipBuilder;
import org.webcurator.core.common.Environment;
import org.webcurator.core.coordinator.WctCoordinator;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.harvester.agent.MockHarvestAgentFactory;
import org.webcurator.core.harvester.coordinator.HarvestAgentManager;
import org.webcurator.core.notification.MockInTrayManager;
import org.webcurator.core.scheduler.MockTargetInstanceManager;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.core.targets.MockTargetManager;
import org.webcurator.core.util.AuthUtil;
import org.webcurator.domain.FlagDAO;
import org.webcurator.domain.MockTargetInstanceDAO;
import org.webcurator.domain.TargetInstanceCriteria;
import org.webcurator.domain.model.core.*;
import org.webcurator.test.BaseWCTTest;
import org.webcurator.ui.admin.command.FlagCommand;
import org.webcurator.common.ui.Constants;
import org.webcurator.ui.target.command.TargetInstanceCommand;
import org.webcurator.common.util.DateUtils;

import com.google.common.collect.Sets;

@SuppressWarnings("all")
public class QueueControllerTest extends BaseWCTTest<QueueController> {

    private MockHttpServletRequest mockRequest;
    private MockHttpServletResponse mockResponse;
    private MockTargetInstanceManager mockTargetInstanceManager;
    private TargetInstanceCommand command;
    private BindingResult errors;

    public QueueControllerTest() {
        super(QueueController.class, "/org/webcurator/ui/target/controller/QueueControllerTest.xml");

    }

    // Override BaseWCTTest setup method
    public void setUp() throws Exception {
        // call the overridden method as well
        super.setUp();

        // add the extra bits
        DateUtils.get().setMessageSource(new MockMessageSource());

        WctCoordinator hc = new WctCoordinator();
        mockTargetInstanceManager = new MockTargetInstanceManager(testFile);
        MockTargetInstanceDAO tidao = new MockTargetInstanceDAO(testFile);

        mockTargetInstanceManager.setTargetInstanceDao(tidao);

        hc.setTargetInstanceManager(mockTargetInstanceManager);
        hc.setTargetManager(new MockTargetManager(testFile));
        hc.setInTrayManager(new MockInTrayManager(testFile));
        hc.setSipBuilder(new MockSipBuilder(testFile));
        HarvestAgentManager harvestAgentManager = new HarvestAgentManager();
        harvestAgentManager.setHarvestAgentFactory(new MockHarvestAgentFactory());
        harvestAgentManager.setTargetInstanceManager(mockTargetInstanceManager);
        harvestAgentManager.setTargetInstanceDao(tidao);
        hc.setHarvestAgentManager(harvestAgentManager);
        hc.setTargetInstanceDao(tidao);

        testInstance.setWctCoordinator(hc);
        testInstance.setTargetInstanceManager(new MockTargetInstanceManager(testFile));
        MockAgencyUserManager mockUserAgencyManager = new MockAgencyUserManager(testFile);
        FlagDAO mockFlagDao = mock(FlagDAO.class);
        mockUserAgencyManager.setFlagDAO(mockFlagDao);
        testInstance.setAgencyUserManager(mockUserAgencyManager);
        testInstance.setEnvironment(new Environment());

        mockRequest = new MockHttpServletRequest();
        mockResponse = new MockHttpServletResponse();
        command = new TargetInstanceCommand();
        errors = new BindException(command, "DUMMY-COMMAND");

    }

    @Test
    public final void testInitBinder() throws Exception {
        ServletRequestDataBinder binder = new ServletRequestDataBinder(command, "command");
        testInstance.initBinder(mockRequest, binder);
    }

    @Test
    public final void testShowForm() throws Exception {

        HttpSession session = mockRequest.getSession();
        session.setAttribute(TargetInstanceCommand.SESSION_TI, "test2");
        session.setAttribute(TargetInstanceCommand.SESSION_MODE, "test3");
        session.setAttribute(Constants.GBL_SESS_EDIT_MODE, "test4");

        ModelAndView mav = testInstance.showForm(mockRequest, mockResponse, errors);
        assertNotNull(mav);
        TargetInstanceCommand command = (TargetInstanceCommand) mav.getModel().get("command");
        assertEquals(TargetInstanceCommand.ACTION_FILTER, command.getCmd());
        assertFalse(command.getFlagged());
        assertTrue(mav.getViewName().equals(Constants.VIEW_TARGET_INSTANCE_QUEUE));
        assertFalse(errors.hasErrors());

        // Ensure that session attributes are reset
        assertNull(mockRequest.getAttribute(TargetInstanceCommand.SESSION_TI_SEARCH_CRITERIA));
        assertNull(session.getAttribute(TargetInstanceCommand.SESSION_TI));
        assertNull(session.getAttribute(TargetInstanceCommand.SESSION_MODE));
        assertNull(session.getAttribute(Constants.GBL_SESS_EDIT_MODE));

    }

    @Test
    public final void testShowFormHarvested() throws Exception {

        mockRequest.addParameter(TargetInstanceCommand.REQ_TYPE, TargetInstanceCommand.TYPE_HARVESTED);

        ModelAndView mav = testInstance.showForm(mockRequest, mockResponse, errors);
        assertNotNull(mav);
        TargetInstanceCommand command = (TargetInstanceCommand) mav.getModel().get("command");
        assertEquals(TargetInstanceCommand.ACTION_FILTER, command.getCmd());
        assertFalse(command.getFlagged());
        assertTrue(mav.getViewName().equals(Constants.VIEW_TARGET_INSTANCE_QUEUE));
        assertFalse(errors.hasErrors());
        assertEquals(command.getOwner(), AuthUtil.getRemoteUser());
        assertEquals(command.getAgency(), AuthUtil.getRemoteUserObject().getAgency().getName());
        assertTrue(command.getStates().size() == 1);
        assertTrue(command.getStates().iterator().next() == TargetInstance.STATE_HARVESTED);
        assertNull(mockRequest.getAttribute(TargetInstanceCommand.SESSION_TI_SEARCH_CRITERIA));
    }

    @Test
    public final void testShowFormTarget() throws Exception {

        mockRequest.addParameter(TargetInstanceCommand.REQ_TYPE, TargetInstanceCommand.TYPE_TARGET);
        mockRequest.addParameter(TargetInstanceCommand.PARAM_TARGET_OID, "2112");

        ModelAndView mav = testInstance.showForm(mockRequest, mockResponse, errors);
        assertNotNull(mav);
        TargetInstanceCommand command = (TargetInstanceCommand) mav.getModel().get("command");
        assertEquals(TargetInstanceCommand.ACTION_FILTER, command.getCmd());
        assertFalse(command.getFlagged());
        assertTrue(mav.getViewName().equals(Constants.VIEW_TARGET_INSTANCE_QUEUE));
        assertFalse(errors.hasErrors());
        assertEquals(command.getTargetid().longValue(), 2112L);
        assertEquals(0, command.getStates().size());
    }

    @Test
    public final void testProcessFormSubmissionFlagged() throws Exception {
        command.setCmd(TargetInstanceCommand.ACTION_FILTER);
        command.setFlagged(true);
        BindingResult bindingResult = new BindException(command, TargetInstanceCommand.ACTION_FILTER);

        TargetInstanceCommand command = processSubmitForm(Constants.VIEW_TARGET_INSTANCE_QUEUE);

        assertEquals(TargetInstanceCommand.ACTION_FILTER, command.getCmd());
        assertTrue(command.getFlagged());
        assertFalse(bindingResult.hasErrors());
    }

    @Test
    public final void testProcessFormSubmissionScheduled() throws Exception {
        command.setCmd(TargetInstanceCommand.ACTION_DELETE);
        command.setTargetInstanceId(5000L);
        BindingResult bindingResult = new BindException(command, TargetInstanceCommand.ACTION_DELETE);

        TargetInstanceCommand command = processSubmitForm(Constants.VIEW_TARGET_INSTANCE_QUEUE);
        assertEquals(TargetInstanceCommand.ACTION_FILTER, command.getCmd());
        assertFalse(bindingResult.hasErrors());
    }

    @Test
    public final void testProcessFormSubmissionQueuedWithoutHarvesterStatus() throws Exception {
        command.setCmd(TargetInstanceCommand.ACTION_DELETE);
        command.setTargetInstanceId(5001L);
        BindingResult bindingResult = new BindException(command, TargetInstanceCommand.ACTION_DELETE);

        TargetInstanceCommand command = processSubmitForm(Constants.VIEW_TARGET_INSTANCE_QUEUE);
        assertEquals(TargetInstanceCommand.ACTION_FILTER, command.getCmd());
        assertFalse(bindingResult.hasErrors());
    }

    @Test
    public final void testProcessFormSubmissionQueuedWithHarvesterStatus() throws Exception {
        command.setCmd(TargetInstanceCommand.ACTION_DELETE);
        command.setTargetInstanceId(5002L);
        BindingResult bindingResult = new BindException(command, TargetInstanceCommand.ACTION_DELETE);

        TargetInstanceCommand command = processSubmitForm(Constants.VIEW_TARGET_INSTANCE_QUEUE, bindingResult);
        assertEquals(TargetInstanceCommand.ACTION_FILTER, command.getCmd());
        assertTrue(bindingResult.hasErrors());
        assertEquals(1, bindingResult.getErrorCount());
    }

    @Test
    public final void testProcessFormSubmissionReset() throws Exception {
        command.setCmd(TargetInstanceCommand.ACTION_RESET);
        command.setTargetInstanceId(5002L);

        // Set some values to clear
        command.setAgency("test");
        command.setOwner("");
        command.setFrom(new Date());
        command.setTo(new Date());
        command.setName("test");
        command.setSearchOid(123L);
        command.setFlagged(true);
        command.setSortorder(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_DATE_DESC);
        command.setFlagOid(124L);

        TargetInstanceCommand command = processSubmitForm(Constants.VIEW_TARGET_INSTANCE_QUEUE);
        assertEquals(TargetInstanceCommand.ACTION_FILTER, command.getCmd());
        assertFalse(errors.hasErrors());
        assertEquals("", command.getAgency());
        assertEquals("", command.getOwner());
        assertNull(command.getFrom());
        assertNull(command.getTo());
        assertEquals("", command.getName());
        assertNull("", command.getSearchOid());
        assertFalse(command.getFlagged());
        assertEquals(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_DEFAULT, command.getSortorder());
        assertNull("", command.getRecommendationFilter());
    }

    @Test
    public final void testProcessFormSubmissionPause() throws Exception {
        final long tiOid = 5002L;

        WctCoordinator mockHCI = mock(WctCoordinator.class);
        testInstance.setWctCoordinator(mockHCI);

        command.setCmd(TargetInstanceCommand.ACTION_PAUSE);
        command.setTargetInstanceId(tiOid);

        processSubmitForm(Constants.VIEW_TARGET_INSTANCE_QUEUE);
        verify(mockHCI).pause(mockTargetInstanceManager.getTargetInstance(tiOid));
    }

    @Test
    public final void testProcessFormSubmissionResume() throws Exception {
        final long tiOid = 5002L;

        WctCoordinator mockHCI = mock(WctCoordinator.class);
        testInstance.setWctCoordinator(mockHCI);

        command.setCmd(TargetInstanceCommand.ACTION_RESUME);
        command.setTargetInstanceId(tiOid);

        processSubmitForm(Constants.VIEW_TARGET_INSTANCE_QUEUE);
        verify(mockHCI).resume(mockTargetInstanceManager.getTargetInstance(tiOid));
    }

    @Test
    public final void testProcessFormSubmissionAbort() throws Exception {
        final long tiOid = 5002L;

        WctCoordinator mockHCI = mock(WctCoordinator.class);
        testInstance.setWctCoordinator(mockHCI);

        command.setCmd(TargetInstanceCommand.ACTION_ABORT);
        command.setTargetInstanceId(tiOid);

        processSubmitForm(Constants.VIEW_TARGET_INSTANCE_QUEUE);
        verify(mockHCI).abort(mockTargetInstanceManager.getTargetInstance(tiOid));
    }

    @Test
    public final void testProcessFormSubmissionStop() throws Exception {
        final long tiOid = 5002L;

        WctCoordinator mockHCI = mock(WctCoordinator.class);
        testInstance.setWctCoordinator(mockHCI);

        command.setCmd(TargetInstanceCommand.ACTION_STOP);
        command.setTargetInstanceId(tiOid);

        processSubmitForm(Constants.VIEW_TARGET_INSTANCE_QUEUE);
        verify(mockHCI).stop(mockTargetInstanceManager.getTargetInstance(tiOid));
    }

    @Test(expected = WCTRuntimeException.class)
    public final void testProcessFormSubmissionNullCommand() throws Exception {
        testInstance.processFormSubmission(mockRequest, mockResponse, null, errors);
    }

    @Test(expected = WCTRuntimeException.class)
    public final void testProcessFormSubmissionNullAction() throws Exception {
        command.setCmd(null);
        testInstance.processFormSubmission(mockRequest, mockResponse, command, errors);
    }

    @Test
    public final void testProcessFormSubmissionQaDisabled() throws Exception {
        command.setCmd(TargetInstanceCommand.ACTION_FILTER);
        ModelAndView mav = testInstance.processFormSubmission(mockRequest, mockResponse, command, errors);
        assertNotNull(mav);
        assertEquals(Constants.VIEW_TARGET_INSTANCE_QUEUE, mav.getViewName());

        assertNull(mav.getModel().get(TargetInstanceCommand.MDL_REASONS));
        assertNull(mav.getModel().get(FlagCommand.MDL_FLAGS));
        // add the configured thumb-nail size
        assertNull(mav.getModel().get(Constants.THUMBNAIL_WIDTH));
        assertNull(mav.getModel().get(Constants.THUMBNAIL_HEIGHT));
    }

    @Test
    public final void testProcessFormSubmissionQaEnabled() throws Exception {
        command.setCmd(TargetInstanceCommand.ACTION_FILTER);
        testInstance.setEnableQaModule(true);

        ModelAndView mav = testInstance.processFormSubmission(mockRequest, mockResponse, command, errors);
        assertNotNull(mav);
        assertEquals(Constants.VIEW_TARGET_INSTANCE_QA_QUEUE, mav.getViewName());

        assertNotNull(mav.getModel().get(TargetInstanceCommand.MDL_REASONS));
        assertNotNull(mav.getModel().get(FlagCommand.MDL_FLAGS));
        // add the configured thumb-nail size
        assertNotNull(mav.getModel().get(Constants.THUMBNAIL_WIDTH));
        assertNotNull(mav.getModel().get(Constants.THUMBNAIL_HEIGHT));
    }

    @Test
    public final void testProcessFormSubmissionQaEnabledCriteriaFlag() throws Exception {
        Long flagOid = 5L;
        command.setFlagOid(flagOid);
        command.setCmd(TargetInstanceCommand.ACTION_FILTER);
        testInstance.setEnableQaModule(true);

        MockTargetInstanceManager spy = spy(mockTargetInstanceManager);
        testInstance.setTargetInstanceManager(spy);

        AgencyUserManager mockAgencyUserManager = mock(AgencyUserManager.class);
        Flag flag = new Flag();
        flag.setName("testflag");
        flag.setOid(flagOid);
        when(mockAgencyUserManager.getFlagByOid(flagOid)).thenReturn(flag);
        testInstance.setAgencyUserManager(mockAgencyUserManager);

        ModelAndView mav = testInstance.processFormSubmission(mockRequest, mockResponse, command, errors);
        assertNotNull(mav);
        assertEquals(Constants.VIEW_TARGET_INSTANCE_QA_QUEUE, mav.getViewName());
        verify(mockAgencyUserManager).getFlagByOid(flagOid);

        ArgumentCaptor<TargetInstanceCriteria> captor = ArgumentCaptor.forClass(TargetInstanceCriteria.class);
        verify(spy).search(captor.capture(), anyInt(), anyInt());
        assertEquals(captor.getValue().getFlag(), flag);

    }

    @Test
    public void testCreateFilterCommandFromSearchCommand() {
        int pageNumber = 2;
        String pageSize = "10";

        TargetInstanceCommand searchCommand = new TargetInstanceCommand();
        ModelAndView mav = new ModelAndView();
        command.setCmd(TargetInstanceCommand.ACTION_SHOW_PAGE);
        command.setPageNo(pageNumber);
        command.setSelectedPageSize(pageSize);
        testInstance.createFilterCommandFromSearchCommand(mockRequest, command, mav, searchCommand, testFile);
        assertEquals(TargetInstanceCommand.ACTION_FILTER, searchCommand.getCmd());
        assertEquals(pageNumber, searchCommand.getPageNo());
        assertEquals(pageSize, searchCommand.getSelectedPageSize());
    }

    @Test
    public void testCreateFilterCommandFromSearchCommandNoPagination() {
        // The action is not a pagination so the value from the search command
        // should be ignored
        int pageNumber = 3;
        String pageSize = "100";

        TargetInstanceCommand searchCommand = new TargetInstanceCommand();
        searchCommand.setPageNo(pageNumber);
        searchCommand.setSelectedPageSize(pageSize);

        ModelAndView mav = new ModelAndView();
        command.setCmd(TargetInstanceCommand.ACTION_FILTER);
        command.setPageNo(2);
        command.setSelectedPageSize("50");

        testInstance.createFilterCommandFromSearchCommand(mockRequest, command, mav, searchCommand, testFile);

        assertEquals(TargetInstanceCommand.ACTION_FILTER, searchCommand.getCmd());
        assertEquals(pageNumber, searchCommand.getPageNo());
        assertEquals(pageSize, searchCommand.getSelectedPageSize());
    }

    @Test
    public void testCreateFilterCommandFromSearchCommandNullCommand() {
        String pageSize = "15";

        TargetInstanceCommand searchCommand = new TargetInstanceCommand();
        searchCommand.setSelectedPageSize("100");
        ModelAndView mav = new ModelAndView();
        testInstance.createFilterCommandFromSearchCommand(mockRequest, null, mav, searchCommand, pageSize);
        assertEquals(TargetInstanceCommand.ACTION_FILTER, searchCommand.getCmd());
        assertEquals(pageSize, searchCommand.getSelectedPageSize());
    }

    @Test
    public void testCreateFilterCommandFromSearchShowSubmittedYes() {
        mockRequest.setParameter(TargetInstanceCommand.REQ_SHOW_SUBMITTED_MSG, "y");
        TargetInstanceCommand searchCommand = new TargetInstanceCommand();
        ModelAndView mav = new ModelAndView();
        String startedMessage = "submitStarted";
        String failedMessage = "submitFailed";
        attachMessageSource(startedMessage, failedMessage);
        testInstance.createFilterCommandFromSearchCommand(mockRequest, command, mav, searchCommand, "10");
        String message = (String) mav.getModel().get(Constants.GBL_MESSAGES);
        assertEquals(message, startedMessage);
    }

    @Test
    public void testCreateFilterCommandFromSearchShowSubmittedNo() {
        mockRequest.setParameter(TargetInstanceCommand.REQ_SHOW_SUBMITTED_MSG, "n");
        TargetInstanceCommand searchCommand = new TargetInstanceCommand();
        ModelAndView mav = new ModelAndView();
        String startedMessage = "submitStarted";
        String failedMessage = "submitFailed";
        attachMessageSource(startedMessage, failedMessage);
        testInstance.createFilterCommandFromSearchCommand(mockRequest, command, mav, searchCommand, "10");
        String message = (String) mav.getModel().get(Constants.GBL_MESSAGES);
        assertEquals(message, failedMessage);
    }

    @Test
    public void testAddQaInformationForTiBrowseTool() {
        Long tOid = 123L;
        Long hrOid = 321L;
        HashMap<Long, Set<Indicator>> indicators = new HashMap<Long, Set<Indicator>>();
        HashMap<Long, String> browseUrls = new HashMap<Long, String>();

        Target target = mock(Target.class);
        Seed seed = createMockSeed("abcdef", true);
        when(target.getSeeds()).thenReturn(Sets.newHashSet(seed));
        TargetInstance targetInstance = createTargetInstance(tOid, target);

        BusinessObjectFactory businessObjectFactory = new BusinessObjectFactory();
        SeedHistory historySeed = businessObjectFactory.newSeedHistory(targetInstance, seed);
        targetInstance.getSeedHistory().add(historySeed);

        int state = HarvestResult.STATE_ENDORSED;
        TargetInstanceManager mockTiManager = setTargetInstanceManager(tOid, hrOid, state);

        HashMap<String, String> targetSeeds = new HashMap<>();
        HashMap<Long, String> reviewUrls = new HashMap<>();
        testInstance.addQaInformationForTi(indicators, browseUrls, targetSeeds, reviewUrls, targetInstance);

        verify(mockTiManager).getHarvestResults(tOid);
        assertTrue(browseUrls.containsKey(tOid));
        assertEquals("curator/tools/browse/321/abcdef", browseUrls.get(tOid));
    }

    @Test
    public void testAddQaInformationForTiBrowseToolNoPrimarySeed() {
        Long tOid = 5000L;
        Long hrOid = 321L;
        HashMap<Long, Set<Indicator>> indicators = new HashMap<Long, Set<Indicator>>();
        HashMap<Long, String> browseUrls = new HashMap<Long, String>();

        Target target = mock(Target.class);
        Seed seed1 = createMockSeed("abcdef", false);
        Seed seed2 = createMockSeed("test", false);
        Set<Seed> set = Sets.newLinkedHashSet();
        set.add(seed1);
        set.add(seed2);
        when(target.getSeeds()).thenReturn(set);

        TargetInstance targetInstance = createTargetInstance(tOid, target);

        BusinessObjectFactory businessObjectFactory = new BusinessObjectFactory();
        SeedHistory historySeed1 = businessObjectFactory.newSeedHistory(targetInstance, seed1);
        SeedHistory historySeed2 = businessObjectFactory.newSeedHistory(targetInstance, seed2);
        targetInstance.getSeedHistory().add(historySeed1);
        targetInstance.getSeedHistory().add(historySeed2);

        int state = HarvestResult.STATE_ENDORSED;
        TargetInstanceManager mockTiManager = setTargetInstanceManager(tOid, hrOid, state);

        HashMap<String, String> targetSeeds = new HashMap<>();
        HashMap<Long, String> reviewUrls = new HashMap<>();
        testInstance.addQaInformationForTi(indicators, browseUrls, targetSeeds, reviewUrls, targetInstance);

        verify(mockTiManager).getHarvestResults(tOid);
        assertTrue(browseUrls.containsKey(tOid));
        assertEquals("curator/tools/browse/321/test", browseUrls.get(tOid));
    }

    @Test
    public void testAddQaInformationForTiBrowseToolNoneDisplayable() {
        Long tOid = 123L;
        Long hrOid = 321L;
        HashMap<Long, Set<Indicator>> indicators = new HashMap<Long, Set<Indicator>>();
        HashMap<Long, String> browseUrls = new HashMap<Long, String>();

        Target target = mock(Target.class);
        when(target.getSeeds()).thenReturn(new HashSet<Seed>());
        TargetInstance targetInstance = createTargetInstance(tOid, target);

        TargetInstanceManager mockTiManager = setTargetInstanceManager(tOid, hrOid, HarvestResult.STATE_REJECTED);

        HashMap<String, String> targetSeeds = new HashMap<>();
        HashMap<Long, String> reviewUrls = new HashMap<>();
        testInstance.addQaInformationForTi(indicators, browseUrls, targetSeeds, reviewUrls, targetInstance);

        verify(mockTiManager).getHarvestResults(tOid);
        assertTrue(browseUrls.containsKey(tOid));
        assertNull(browseUrls.get(tOid));
    }

    @Test
    public void testAddQaInformationForTiAccessToolNoneDisplayable() {
        Long tOid = 123L;
        Long hrOid = 321L;
        HashMap<Long, Set<Indicator>> indicators = new HashMap<Long, Set<Indicator>>();
        HashMap<Long, String> browseUrls = new HashMap<Long, String>();

        Target target = mock(Target.class);
        when(target.getSeeds()).thenReturn(new HashSet<Seed>());
        TargetInstance targetInstance = createTargetInstance(tOid, target);

        TargetInstanceManager mockTiManager = setTargetInstanceManager(tOid, hrOid, HarvestResult.STATE_REJECTED);

        testInstance.setThumbnailRenderer("ACCESSTOOL");

        HashMap<String, String> targetSeeds = new HashMap<>();
        HashMap<Long, String> reviewUrls = new HashMap<>();
        testInstance.addQaInformationForTi(indicators, browseUrls, targetSeeds, reviewUrls, targetInstance);

        verify(mockTiManager).getHarvestResults(tOid);
        assertTrue(browseUrls.isEmpty());

    }

    @Test
    public void testMultiDelete() throws Exception {
        TargetInstance mockTI1 = mock(TargetInstance.class);
        TargetInstance mockTI2 = mock(TargetInstance.class);
        TargetInstanceManager mockTIManager = mock(TargetInstanceManager.class);
        when(mockTIManager.getTargetInstance(5000L)).thenReturn(mockTI1);
        when(mockTIManager.getTargetInstance(234L)).thenReturn(mockTI2);
        testInstance.setTargetInstanceManager(mockTIManager);
        command.setCmd(TargetInstanceCommand.ACTION_MULTI_DELETE);
        command.setMultiselect(Arrays.asList("5000", "234"));
        testInstance.processFormSubmission(mockRequest, mockResponse, command, errors);
        verify(mockTIManager).delete(mockTI1);
        verify(mockTIManager).delete(mockTI2);
        verify(mockTIManager, Mockito.times(2)).delete(any(TargetInstance.class));
    }

    @Test
    public void testMultiDeleteIgnoresMissing() throws Exception {
        TargetInstance mockTI1 = mock(TargetInstance.class);
        TargetInstanceManager mockTIManager = mock(TargetInstanceManager.class);
        when(mockTIManager.getTargetInstance(5000L)).thenReturn(mockTI1);
        testInstance.setTargetInstanceManager(mockTIManager);
        command.setCmd(TargetInstanceCommand.ACTION_MULTI_DELETE);
        command.setMultiselect(Arrays.asList("5000", "234"));
        testInstance.processFormSubmission(mockRequest, mockResponse, command, errors);
        verify(mockTIManager).delete(mockTI1);
        verify(mockTIManager, Mockito.times(1)).delete(any(TargetInstance.class));

    }

    @Test
    public void testMultiDeleteSkipsEndorsed() throws Exception {
        assertFalse(errors.hasErrors());
        TargetInstanceManager mockTIManager = mock(TargetInstanceManager.class);
        TargetInstance mockTI1 = mock(TargetInstance.class);
        when(mockTI1.getState()).thenReturn(TargetInstance.STATE_ENDORSED);
        TargetInstance mockTI2 = mock(TargetInstance.class);
        when(mockTIManager.getTargetInstance(5000L)).thenReturn(mockTI1);
        when(mockTIManager.getTargetInstance(234L)).thenReturn(mockTI2);
        testInstance.setTargetInstanceManager(mockTIManager);
        command.setCmd(TargetInstanceCommand.ACTION_MULTI_DELETE);
        command.setMultiselect(Arrays.asList("5000", "234"));
        testInstance.processFormSubmission(mockRequest, mockResponse, command, errors);
        verify(mockTIManager, times(0)).delete(mockTI1);
        verify(mockTIManager).delete(mockTI2);
        verify(mockTIManager, times(1)).delete(any(TargetInstance.class));
        assertTrue(errors.hasErrors());
    }

    @Test
    public void testMultiEndorse() throws Exception {
        TargetInstance mockTI1 = mock(TargetInstance.class);
        when(mockTI1.getState()).thenReturn(TargetInstance.STATE_HARVESTED);
        TargetInstance mockTI2 = mock(TargetInstance.class);
        when(mockTI2.getState()).thenReturn(TargetInstance.STATE_HARVESTED);
        TargetInstanceManager mockTiManager = mock(TargetInstanceManager.class);
        when(mockTiManager.getTargetInstance(5000L)).thenReturn(mockTI1);
        when(mockTiManager.getTargetInstance(234L)).thenReturn(mockTI2);

        HarvestResult result1 = new HarvestResult();
        result1.setState(HarvestResult.STATE_UNASSESSED);
        result1.setOid(1L);
        when(mockTiManager.getHarvestResults(5000L)).thenReturn(Arrays.asList(result1));
        HarvestResult result2 = new HarvestResult();
        result2.setState(HarvestResult.STATE_UNASSESSED);
        result2.setOid(2L);
        when(mockTiManager.getHarvestResults(234L)).thenReturn(Arrays.asList(result2));

        testInstance.setTargetInstanceManager(mockTiManager);
        command.setCmd(TargetInstanceCommand.ACTION_MULTI_ENDORSE);
        command.setMultiselect(Arrays.asList("5000", "234"));
        testInstance.processFormSubmission(mockRequest, mockResponse, command, errors);
        verify(mockTI1).setState(TargetInstance.STATE_ENDORSED);
        verify(mockTI2).setState(TargetInstance.STATE_ENDORSED);
        verify(mockTiManager).save(mockTI1);
        verify(mockTiManager).save(mockTI2);
    }

    @Test
    public void testMultiEndorseRejectOtherHarvestResults() throws Exception {
        TargetInstance mockTI1 = mock(TargetInstance.class);
        when(mockTI1.getState()).thenReturn(TargetInstance.STATE_HARVESTED);
        TargetInstanceManager mockTiManager = mock(TargetInstanceManager.class);
        when(mockTiManager.getTargetInstance(5000L)).thenReturn(mockTI1);

        HarvestResult result1 = mock(HarvestResult.class);
        when(result1.getState()).thenReturn(HarvestResult.STATE_ABORTED);
        when(result1.getOid()).thenReturn(1L);
        HarvestResult result2 = mock(HarvestResult.class);
        when(result2.getState()).thenReturn(HarvestResult.STATE_UNASSESSED);
        when(result2.getOid()).thenReturn(2L);
        when(mockTI1.getHarvestResults()).thenReturn(Arrays.asList(result1, result2));
        when(mockTiManager.getHarvestResults(5000L)).thenReturn(Arrays.asList(result1, result2));

        WctCoordinator mockWctCoordinator = mock(WctCoordinator.class);
        testInstance.setWctCoordinator(mockWctCoordinator);

        testInstance.setTargetInstanceManager(mockTiManager);
        command.setCmd(TargetInstanceCommand.ACTION_MULTI_ENDORSE);
        command.setMultiselect(Arrays.asList("5000"));
        testInstance.processFormSubmission(mockRequest, mockResponse, command, errors);
        verify(mockTI1).setState(TargetInstance.STATE_ENDORSED);
        // reject "old" harvest result
        verify(result1).setState(HarvestResult.STATE_REJECTED);
        verify(result2).setState(HarvestResult.STATE_ENDORSED);
        verify(mockTiManager).save(mockTI1);
        verify(mockWctCoordinator).removeIndexes(result1);
    }

    @Test
    public void testMultiReject() throws Exception {
        TargetInstance mockTI1 = mock(TargetInstance.class);
        when(mockTI1.getState()).thenReturn(TargetInstance.STATE_HARVESTED);
        TargetInstance mockTI2 = mock(TargetInstance.class);
        when(mockTI2.getState()).thenReturn(TargetInstance.STATE_HARVESTED);
        TargetInstanceManager mockTiManager = mock(TargetInstanceManager.class);
        when(mockTiManager.getTargetInstance(5000L)).thenReturn(mockTI1);
        when(mockTiManager.getTargetInstance(234L)).thenReturn(mockTI2);

        HarvestResult result1 = mock(HarvestResult.class);
        when(result1.getState()).thenReturn(HarvestResult.STATE_ABORTED);
        when(result1.getOid()).thenReturn(1L);
        when(mockTI1.getHarvestResults()).thenReturn(Arrays.asList(result1));
        when(mockTiManager.getHarvestResults(5000L)).thenReturn(Arrays.asList(result1));
        HarvestResult result2 = mock(HarvestResult.class);
        when(result2.getState()).thenReturn(HarvestResult.STATE_REJECTED);
        when(result2.getOid()).thenReturn(2L);
        when(mockTI2.getHarvestResults()).thenReturn(Arrays.asList(result2));
        when(mockTiManager.getHarvestResults(234L)).thenReturn(Arrays.asList(result2));

        WctCoordinator mockWctCoordinator = mock(WctCoordinator.class);
        testInstance.setWctCoordinator(mockWctCoordinator);
        AgencyUserManager mockAgencyUserManager = mock(AgencyUserManager.class);
        RejReason rejectionReason = new RejReason();
        when(mockAgencyUserManager.getRejReasonByOid(any(Long.class))).thenReturn(rejectionReason);
        testInstance.setAgencyUserManager(mockAgencyUserManager);

        testInstance.setTargetInstanceManager(mockTiManager);
        command.setCmd(TargetInstanceCommand.ACTION_MULTI_REJECT);
        command.setMultiselect(Arrays.asList("5000", "234"));
        command.setRejReasonId(1L);
        testInstance.processFormSubmission(mockRequest, mockResponse, command, errors);
        verify(mockTI1).setState(TargetInstance.STATE_REJECTED);
        verify(mockTI2).setState(TargetInstance.STATE_REJECTED);
        verify(mockTiManager).save(mockTI1);
        verify(mockWctCoordinator).removeIndexes(result1);
        // Already rejected
        verify(mockWctCoordinator, times(0)).removeIndexes(result2);
        verify(mockTiManager).save(mockTI2);
        verify(result1).setRejReason(rejectionReason);
        verify(result2, times(0)).setRejReason(rejectionReason);
    }

    @Test
    public void testMultiRejectNoRejectionReason() throws Exception {
        TargetInstance mockTI1 = mock(TargetInstance.class);
        when(mockTI1.getState()).thenReturn(TargetInstance.STATE_HARVESTED);
        TargetInstance mockTI2 = mock(TargetInstance.class);
        when(mockTI2.getState()).thenReturn(TargetInstance.STATE_HARVESTED);
        TargetInstanceManager mockTiManager = mock(TargetInstanceManager.class);
        when(mockTiManager.getTargetInstance(5000L)).thenReturn(mockTI1);
        when(mockTiManager.getTargetInstance(234L)).thenReturn(mockTI2);

        HarvestResult result1 = mock(HarvestResult.class);
        when(result1.getState()).thenReturn(HarvestResult.STATE_ABORTED);
        when(result1.getOid()).thenReturn(1L);
        when(mockTI1.getHarvestResults()).thenReturn(Arrays.asList(result1));
        when(mockTiManager.getHarvestResults(5000L)).thenReturn(Arrays.asList(result1));
        HarvestResult result2 = mock(HarvestResult.class);
        when(result2.getState()).thenReturn(HarvestResult.STATE_REJECTED);
        when(result2.getOid()).thenReturn(2L);
        when(mockTI2.getHarvestResults()).thenReturn(Arrays.asList(result2));
        when(mockTiManager.getHarvestResults(234L)).thenReturn(Arrays.asList(result2));

        WctCoordinator mockWctCoordinator = mock(WctCoordinator.class);
        testInstance.setWctCoordinator(mockWctCoordinator);

        testInstance.setTargetInstanceManager(mockTiManager);
        command.setCmd(TargetInstanceCommand.ACTION_MULTI_REJECT);
        command.setMultiselect(Arrays.asList("5000", "234"));
        testInstance.processFormSubmission(mockRequest, mockResponse, command, errors);
        verify(mockTI1).setState(TargetInstance.STATE_REJECTED);
        verify(mockTI2).setState(TargetInstance.STATE_REJECTED);
        verify(mockTiManager).save(mockTI1);
        verify(mockTiManager).save(mockTI2);
        verify(result1, times(0)).setRejReason(any(RejReason.class));
        verify(result2, times(0)).setRejReason(any(RejReason.class));
        assertTrue(errors.hasErrors());
    }

    private Seed createMockSeed(String seedName, boolean isPrimary) {
        Seed seed2 = mock(Seed.class);
        when(seed2.isPrimary()).thenReturn(isPrimary);
        when(seed2.getSeed()).thenReturn(seedName);
        return seed2;
    }

    private void attachMessageSource(String startedMessage, String failedMessage) {
        String submitStartedMessage = "ui.label.targetinstance.submitToArchiveStarted";
        String submitFailedMessage = "ui.label.targetinstance.submitToArchiveFailed";

        MessageSource mockMessageSource = mock(MessageSource.class);
        when(mockMessageSource.getMessage(eq(submitStartedMessage), any(Object[].class), any(Locale.class))).thenReturn(
                startedMessage);
        when(mockMessageSource.getMessage(eq(submitFailedMessage), any(Object[].class), any(Locale.class))).thenReturn(
                failedMessage);
        testInstance.setMessageSource(mockMessageSource);
    }

    private TargetInstanceCommand processSubmitForm(String expectedViewName) throws Exception {
        return processSubmitForm(expectedViewName, errors);
    }

    private TargetInstanceCommand processSubmitForm(String expectedViewName, BindingResult bindingResult) throws Exception {
        ModelAndView mav = testInstance.processFormSubmission(mockRequest, mockResponse, command, bindingResult);
        assertNotNull(mav);
        assertEquals(expectedViewName, mav.getViewName());
        TargetInstanceCommand command = (TargetInstanceCommand) mav.getModel().get("command");
        return command;
    }

    private TargetInstance createTargetInstance(Long tOid, Target target) {
        TargetInstance targetInstance = new TargetInstance();
        targetInstance.setOid(tOid);
        targetInstance.setTarget(target);
        return targetInstance;
    }

    private TargetInstanceManager setTargetInstanceManager(Long tOid, Long hrOid, int state) {
        HarvestResult result1 = new HarvestResult();
        result1.setState(state);
        result1.setOid(hrOid);
        TargetInstanceManager mockTiManager = mock(TargetInstanceManager.class);
        when(mockTiManager.getHarvestResults(tOid)).thenReturn(Arrays.asList(result1));
        testInstance.setTargetInstanceManager(mockTiManager);
        return mockTiManager;
    }
}
