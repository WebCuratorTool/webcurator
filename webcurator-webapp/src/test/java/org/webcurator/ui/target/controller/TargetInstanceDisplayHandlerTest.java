package org.webcurator.ui.target.controller;

import static org.junit.Assert.*;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MockMessageSource;
import org.junit.Test;
import org.springframework.mock.web.*;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.common.util.DateUtils;
import org.webcurator.core.coordinator.WctCoordinator;
import org.webcurator.test.BaseWCTTest;
import org.webcurator.ui.admin.command.CreateUserCommand;
import org.webcurator.ui.target.command.*;
import org.webcurator.core.agency.*;
import org.webcurator.ui.util.*;
import org.webcurator.core.scheduler.*;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.domain.model.core.*;
import org.webcurator.ui.target.validator.*;

public class TargetInstanceDisplayHandlerTest extends BaseWCTTest<TargetInstanceDisplayHandler> {

	public TargetInstanceDisplayHandlerTest()
	{
		super(TargetInstanceDisplayHandler.class,
				"/org/webcurator/ui/target/controller/TargetInstanceDisplayHandlerTest.xml");
	}

	public void setUp() throws Exception
	{
		super.setUp();
		DateUtils.get().setMessageSource(new MockMessageSource());
	}

	private List<Tab> getTabList(TargetInstanceManager targetInstanceManager)
	{
		List<Tab> tabs = new ArrayList<Tab>();

		Tab tabGeneral = new Tab();
		tabGeneral.setCommandClass(TargetInstanceCommand.class);
		tabGeneral.setJsp("../target-instance-general.jsp");
		tabGeneral.setPageId("GENERAL");
		tabGeneral.setTitle("general");
		tabGeneral.setValidator(new TargetInstanceValidator());

		TargetInstanceGeneralHandler genHandler = new TargetInstanceGeneralHandler();
		genHandler.setAgencyUserManager(new MockAgencyUserManager(testFile));
		genHandler.setAuthorityManager(new AuthorityManager());
		genHandler.setTargetInstanceManager(targetInstanceManager);
		genHandler.setWctCoordinator(new WctCoordinator());
		tabGeneral.setTabHandler(genHandler);

		tabs.add(tabGeneral);

		Tab tabDisplay = new Tab();
		tabDisplay.setCommandClass(TargetInstanceCommand.class);
		tabDisplay.setJsp("../target-instance-display.jsp");
		tabDisplay.setPageId("DISPLAY");

		TargetInstanceDisplayHandler displayHandler = new TargetInstanceDisplayHandler();
		displayHandler.setTargetInstanceManager(targetInstanceManager);
		tabDisplay.setTabHandler(displayHandler);

		tabs.add(tabDisplay);
		return tabs;
	}


	@Test
	public final void testProcessTab() {
		HttpServletRequest aReq = new MockHttpServletRequest();
		TargetInstanceManager targetInstanceManager = new MockTargetInstanceManager(testFile);
		testInstance.setTargetInstanceManager(targetInstanceManager);
		TargetInstance targetInstance = targetInstanceManager.getTargetInstance(5001L);

		aReq.getSession().setAttribute(TargetInstanceCommand.SESSION_TI, targetInstance);
		aReq.getSession().setAttribute(TargetInstanceCommand.SESSION_MODE, true);

		HttpServletResponse aResp = new MockHttpServletResponse();
		TargetInstanceCommand aCmd = new TargetInstanceCommand(targetInstance);
		TabbedController tc = new TabbedTargetInstanceController();

		TabConfig tabConfig = new TabConfig();
		tabConfig.setViewName("targetInstance");
		List<Tab> tabs = getTabList(targetInstanceManager);
		tabConfig.setTabs(tabs);

		tc.setTabConfig(tabConfig);
		tc.setDefaultCommandClass(org.webcurator.ui.target.command.TargetInstanceCommand.class);

		boolean newDisplay = false;
		String newNote = "This is a new test note";
		Tab currentTab = tabs.get(1);
		aCmd.setCmd(TargetInstanceCommand.ACTION_EDIT);
		aCmd.setDisplay(newDisplay);
		aCmd.setDisplayNote(newNote);
		BindingResult bindingResult = new BindException(aCmd, aCmd.getCmd());
		testInstance.processTab(tc, currentTab, aReq, aResp, aCmd, bindingResult);
		assertTrue(targetInstance.getDisplay() == newDisplay);
		assertTrue(targetInstance.getDisplayNote().equals(newNote));

		newDisplay = true;
		newNote = "This is another new test note";
		currentTab = tabs.get(1);
		aCmd.setCmd(TargetInstanceCommand.ACTION_EDIT);
		aCmd.setDisplay(newDisplay);
		aCmd.setDisplayNote(newNote);
		bindingResult = new BindException(aCmd, aCmd.getCmd());
		testInstance.processTab(tc, currentTab, aReq, aResp, aCmd, bindingResult);
		assertTrue(targetInstance.getDisplay() == newDisplay);
		assertTrue(targetInstance.getDisplayNote().equals(newNote));
	}

	@Test
	public final void testPreProcessNextTab() {
		HttpServletRequest aReq = new MockHttpServletRequest();
		TargetInstanceManager targetInstanceManager = new MockTargetInstanceManager(testFile);
		testInstance.setTargetInstanceManager(targetInstanceManager);
		TargetInstance targetInstance = targetInstanceManager.getTargetInstance(5001L);

		aReq.getSession().setAttribute(TargetInstanceCommand.SESSION_TI, targetInstance);
		aReq.getSession().setAttribute(TargetInstanceCommand.SESSION_MODE, true);

		HttpServletResponse aResp = new MockHttpServletResponse();
		TargetInstanceCommand aCmd = new TargetInstanceCommand();
		aCmd.setCmd(TargetInstanceCommand.ACTION_EDIT);
		TabbedController tc = new TabbedTargetInstanceController();

		TabConfig tabConfig = new TabConfig();
		tabConfig.setViewName("targetInstance");
		List<Tab> tabs = getTabList(targetInstanceManager);
		tabConfig.setTabs(tabs);

		tc.setTabConfig(tabConfig);
		tc.setDefaultCommandClass(org.webcurator.ui.target.command.TargetInstanceCommand.class);

		Tab currentTab = tabs.get(1);
		BindingResult bindingResult = new BindException(aCmd, aCmd.getCmd());
		ModelAndView mav = testInstance.preProcessNextTab(tc, currentTab, aReq, aResp, aCmd, bindingResult);
		assertTrue(((TargetInstanceCommand)mav.getModel().get("command")).getDisplay() == targetInstance.getDisplay());
		assertTrue(((TargetInstanceCommand)mav.getModel().get("command")).getDisplayNote().equals(targetInstance.getDisplayNote()));
	}

	@Test
	public final void testProcessOther() {
		HttpServletRequest aReq = new MockHttpServletRequest();
		TargetInstanceManager targetInstanceManager = new MockTargetInstanceManager(testFile);
		testInstance.setTargetInstanceManager(targetInstanceManager);
		TargetInstance targetInstance = targetInstanceManager.getTargetInstance(5001L);

		aReq.getSession().setAttribute(TargetInstanceCommand.SESSION_TI, targetInstance);
		aReq.getSession().setAttribute(TargetInstanceCommand.SESSION_MODE, true);

		HttpServletResponse aResp = new MockHttpServletResponse();
		TargetInstanceCommand aCmd = new TargetInstanceCommand(targetInstance);
		TabbedController tc = new TabbedTargetInstanceController();

		TabConfig tabConfig = new TabConfig();
		tabConfig.setViewName("targetInstance");
		List<Tab> tabs = getTabList(targetInstanceManager);
		tabConfig.setTabs(tabs);

		tc.setTabConfig(tabConfig);
		tc.setDefaultCommandClass(org.webcurator.ui.target.command.TargetInstanceCommand.class);

		Tab currentTab = tabs.get(1);
		aCmd.setCmd(TargetInstanceCommand.ACTION_EDIT);
		BindingResult bindingResult = new BindException(aCmd, aCmd.getCmd());
		ModelAndView mav = testInstance.processOther(tc, currentTab, aReq, aResp, aCmd, bindingResult);
		assertTrue(mav != null);
	}

	@Test
	public final void testInitBinder() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		ServletRequestDataBinder binder = new ServletRequestDataBinder(new CreateUserCommand(), "command");
		try
		{
			testInstance.initBinder(request, binder);
		}
		catch (Exception e)
		{
			String message = e.getClass().toString() + " - " + e.getMessage();
			log.debug(message);
			fail(message);
		}
	}

	@Test
	public final void testSetTargetInstanceManager() {
		TargetInstanceManager targetInstanceManager = new MockTargetInstanceManager(testFile);
		testInstance.setTargetInstanceManager(targetInstanceManager);
	}
}
