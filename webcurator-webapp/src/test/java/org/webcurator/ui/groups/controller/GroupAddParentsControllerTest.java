package org.webcurator.ui.groups.controller;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.agency.MockAgencyUserManager;
import org.webcurator.core.common.WCTTreeSet;
import org.webcurator.core.targets.MockTargetManager;
import org.webcurator.core.targets.TargetManager;
import org.webcurator.test.BaseWCTTest;
import org.webcurator.ui.groups.GroupsEditorContext;
import org.webcurator.ui.groups.command.AddParentsCommand;
import org.webcurator.ui.groups.command.GeneralCommand;
import org.webcurator.ui.groups.command.MembersCommand;
import org.webcurator.ui.groups.validator.AddParentsValidator;
import org.webcurator.ui.groups.validator.GeneralValidator;
import org.webcurator.ui.util.Tab;
import org.webcurator.ui.util.TabConfig;

public class GroupAddParentsControllerTest extends BaseWCTTest<GroupAddParentsController> {

	private TargetManager tm = null;

	public GroupAddParentsControllerTest() {
		super(GroupAddParentsController.class, "/org/webcurator/ui/groups/controller/GroupAddParentsControllerTest.xml");
	}

	public void setUp() throws Exception
	{
		super.setUp();
		tm = new MockTargetManager(testFile);
	}


	private List<Tab> getTabList()
	{
		List<Tab> tabs = new ArrayList<Tab>();

		Tab tabGeneral = new Tab();
		tabGeneral.setCommandClass(GeneralCommand.class);
		tabGeneral.setJsp("../groups-general.jsp");
		tabGeneral.setPageId("GENERAL");
		tabGeneral.setTitle("general");
		tabGeneral.setValidator(new GeneralValidator());

		GeneralHandler genHandler = new GeneralHandler();
		genHandler.setAuthorityManager(new AuthorityManager());
		genHandler.setAgencyUserManager(new MockAgencyUserManager(testFile));
		genHandler.setTargetManager(tm);
		genHandler.setSubGroupSeparator(" > ");
		genHandler.setSubGroupTypeName("Sub-Group");
		List<String> subGroupTypes = new ArrayList<String>();
		subGroupTypes.add("");
		subGroupTypes.add("Collection");
		subGroupTypes.add("Subject");
		subGroupTypes.add("Thematic");
		subGroupTypes.add("Event");
		subGroupTypes.add("Functional");
		subGroupTypes.add("Sub-Group");

		WCTTreeSet groupTypesList = new WCTTreeSet(subGroupTypes, 50);
		genHandler.setGroupTypesList(groupTypesList);

		tabGeneral.setTabHandler(genHandler);

		tabs.add(tabGeneral);

		Tab tabMembers = new Tab();
		tabMembers.setCommandClass(MembersCommand.class);
		tabMembers.setJsp("../groups-members.jsp");
		tabMembers.setPageId("MEMBERS");

		MembersHandler membersHandler = new MembersHandler();
		membersHandler.setTargetManager(tm);
		tabMembers.setTabHandler(membersHandler);

		tabs.add(tabMembers);
		return tabs;
	}

	private GroupsEditorContext bindEditorContext(HttpServletRequest request, Long groupOid)
	{
		GroupsEditorContext groupsEditorContext = new GroupsEditorContext(tm.loadGroup(groupOid),true);
		request.getSession().setAttribute(TabbedGroupController.EDITOR_CONTEXT, groupsEditorContext);

		return groupsEditorContext;
	}

	@Test
	public final void testGetEditorContext() {
		try
		{
			HttpServletRequest aReq = new MockHttpServletRequest();
			GroupsEditorContext groupsEditorContext = bindEditorContext(aReq, 15000L);

			GroupsEditorContext gec = testInstance.getEditorContext(aReq);
			assertNotNull(gec);
			assertEquals(groupsEditorContext, gec);
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testHandleAddParents() {
		try
		{
			BindingResult bindingResult;
			testSetAuthorityManager();
			testSetTargetManager();
			testSetGroupsController();
			ReflectionTestUtils.setField(testInstance, "addParentsValidator", new AddParentsValidator());

			HttpServletRequest request = new MockHttpServletRequest();
			HttpServletResponse response = new MockHttpServletResponse();
			AddParentsCommand command = new AddParentsCommand();

			bindEditorContext(request, 15002L);
			testInstance.getEditorContext(request).getTargetGroup().setName("ParentGroup > ChildGroup");

			command.setActionCmd(AddParentsCommand.ACTION_ADD_PARENTS);
			long[]  oids = {15000L};
			command.setParentOids(oids);

			bindingResult = new BindException(command, "AddParentsCommand");

			ModelAndView mav = testInstance.handle(command, request, response,  bindingResult);
			assertNotNull(mav);
			assertEquals(mav.getViewName(), "groups");
			assertTrue(((GeneralCommand)mav.getModel().get("command")).getParentOid().equals("15000"));
			assertTrue(((GeneralCommand)mav.getModel().get("command")).getName().equals("ChildGroup"));
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testHandleCancel() {
		try
		{
			BindingResult bindingResult;
			testSetAuthorityManager();
			testSetTargetManager();
			testSetGroupsController();
			ReflectionTestUtils.setField(testInstance, "addParentsValidator", new AddParentsValidator());

			HttpServletRequest request = new MockHttpServletRequest();
			HttpServletResponse response = new MockHttpServletResponse();
			AddParentsCommand command = new AddParentsCommand();

			bindEditorContext(request, 15002L);
			testInstance.getEditorContext(request).getTargetGroup().setName("ParentGroup > ChildGroup");

			command.setActionCmd(AddParentsCommand.ACTION_CANCEL);
			long[]  oids = {15000L};
			command.setParentOids(oids);

			bindingResult = new BindException(command, "AddParentsCommand");

			ModelAndView mav = testInstance.handle(command, request, response, bindingResult);
			assertNotNull(mav);
			assertEquals(mav.getViewName(), "groups");
			assertTrue(((GeneralCommand)mav.getModel().get("command")).getParentOid().equals(""));
			assertTrue(((GeneralCommand)mav.getModel().get("command")).getName().equals("ChildGroup"));
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testHandleOther() {
		try
		{
			BindingResult bindingResult;
			testSetAuthorityManager();
			testSetTargetManager();
			testSetGroupsController();
			ReflectionTestUtils.setField(testInstance, "addParentsValidator", new AddParentsValidator());

			HttpServletRequest request = new MockHttpServletRequest();
			HttpServletResponse response = new MockHttpServletResponse();
			AddParentsCommand command = new AddParentsCommand();

			bindEditorContext(request, 15002L);

			command.setActionCmd(null);

			bindingResult = new BindException(command, "AddMembersCommand");

			ModelAndView mav = testInstance.handle(command, request, response, bindingResult);
			assertNotNull(mav);
			assertEquals(mav.getViewName(), "group-add-parents");
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testSetGroupsController() {
		try
		{
			TabbedGroupController tc = new TabbedGroupController();

			TabConfig tabConfig = new TabConfig();
			tabConfig.setViewName("groups");
			List<Tab> tabs = getTabList();
			tabConfig.setTabs(tabs);

			tc.setTabConfig(tabConfig);
			tc.setDefaultCommandClass(org.webcurator.ui.groups.command.DefaultCommand.class);
			testInstance.setGroupsController(tc);
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testSetTargetManager() {
		try
		{
			testInstance.setTargetManager(tm);
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testSetAuthorityManager() {
		try
		{
			testInstance.setAuthorityManager(new AuthorityManager());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

}
