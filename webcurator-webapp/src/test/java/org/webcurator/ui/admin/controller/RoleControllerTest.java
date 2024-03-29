package org.webcurator.ui.admin.controller;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.mock.web.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.context.MockMessageSource;

import org.webcurator.test.BaseWCTTest;
import org.webcurator.ui.admin.command.*;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.agency.*;
import org.webcurator.domain.model.auth.Privilege;
import java.util.List;

import javax.management.relation.Role;

import org.webcurator.core.util.AuthUtil;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.ui.admin.validator.RoleValidator;


public class RoleControllerTest extends BaseWCTTest<RoleController> {

	public RoleControllerTest()
	{
		super(RoleController.class,
                "/org/webcurator/ui/admin/controller/RoleControllerTest.xml");
	}

	@Test
	public final void testRoleController() {
		assert(testInstance != null);
	}

	@Test
	public final void testInitBinder() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		ServletRequestDataBinder binder = new ServletRequestDataBinder(new RoleCommand(), "command");
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
	public final void testDefaultView() {
		this.testSetAgencyUserManager();
		this.testSetAuthorityManager();
		this.testSetMessageSource();

		this.removeAllCurrentUserPrivileges();
		String agencyFilter = "Dummy";
		ModelAndView mav = testInstance.defaultView(agencyFilter);
		assertTrue(mav != null);
		assertTrue(mav.getViewName().equals("Roles"));
		List roles = (List)mav.getModel().get(RoleCommand.MDL_ROLES);
		assertTrue(roles != null);
		assertFalse(roles.isEmpty());
		RoleCommand command = (RoleCommand)mav.getModel().get("command");
		assertTrue(command != null);
		assertTrue(agencyFilter.equals(command.getAgencyFilter()));
		List<Agency> agencies = (List<Agency>)mav.getModel().get(RoleCommand.MDL_AGENCIES);
		assertTrue(agencies != null);
		assertTrue(agencies.size() == 1);

		this.removeAllCurrentUserPrivileges();
		this.addCurrentUserPrivilege(Privilege.SCOPE_AGENCY, Privilege.MANAGE_ROLES);
		mav = testInstance.defaultView(agencyFilter);
		assertTrue(mav != null);
		assertTrue(mav.getViewName().equals("Roles"));
		roles = (List)mav.getModel().get(RoleCommand.MDL_ROLES);
		assertTrue(roles != null);
		assertFalse(roles.isEmpty());
		command = (RoleCommand)mav.getModel().get("command");
		assertTrue(command != null);
		assertTrue(agencyFilter.equals(command.getAgencyFilter()));
		agencies = (List<Agency>)mav.getModel().get(RoleCommand.MDL_AGENCIES);
		assertTrue(agencies != null);
		assertTrue(agencies.size() == 1);

		this.removeAllCurrentUserPrivileges();
		this.addCurrentUserPrivilege(Privilege.SCOPE_ALL, Privilege.MANAGE_ROLES);
		mav = testInstance.defaultView(agencyFilter);
		assertTrue(mav != null);
		assertTrue(mav.getViewName().equals("Roles"));
		roles = (List)mav.getModel().get(RoleCommand.MDL_ROLES);
		assertTrue(roles != null);
		assertFalse(roles.isEmpty());
		command = (RoleCommand)mav.getModel().get("command");
		assertTrue(command != null);
		assertTrue(agencyFilter.equals(command.getAgencyFilter()));
		agencies = (List<Agency>)mav.getModel().get(RoleCommand.MDL_AGENCIES);
		assertTrue(agencies != null);
		assertTrue(agencies.size() > 1);
	}

	@Test
	public final void testProcessFormSubmission() {
		try
		{
			MockHttpServletRequest request = new MockHttpServletRequest();
            BindingResult bindingResult;
			this.testSetAgencyUserManager();
			this.testSetAuthorityManager();
			this.testSetMessageSource();
			ReflectionTestUtils.setField(testInstance, "roleValidator", new RoleValidator());

			RoleCommand aCommand = new RoleCommand();
			aCommand.setAction(RoleCommand.ACTION_NEW);
			aCommand.setOid(new Long(3000));
			bindingResult = new BindException(aCommand, RoleCommand.ACTION_NEW);
			ModelAndView mav = testInstance.processFormSubmission(request, aCommand, bindingResult);
			assertTrue(mav != null);
			assertTrue(mav.getViewName().equals("AddRole"));
			List<Agency> agencies = (List<Agency>)mav.getModel().get("agencies");
			assertTrue(agencies != null);
			assertTrue(agencies.size() > 0);

			aCommand = new RoleCommand();
			aCommand.setAction(RoleCommand.ACTION_VIEW);
			aCommand.setOid(new Long(3000));
			bindingResult = new BindException(aCommand, RoleCommand.ACTION_VIEW);
			mav = testInstance.processFormSubmission(request, aCommand, bindingResult);
			assertTrue(mav != null);
			assertTrue(mav.getViewName().equals("AddRole"));
			agencies = (List<Agency>)mav.getModel().get("agencies");
			assertTrue(agencies != null);
			assertTrue(agencies.size() > 0);
			RoleCommand newCommand = (RoleCommand)mav.getModel().get("command");
			assertTrue(newCommand != null);
			assertTrue(newCommand.getViewOnlyMode());

			aCommand = new RoleCommand();
			aCommand.setAction(RoleCommand.ACTION_EDIT);
			aCommand.setOid(new Long(3000));
			bindingResult = new BindException(aCommand, RoleCommand.ACTION_EDIT);
			mav = testInstance.processFormSubmission(request, aCommand, bindingResult);
			assertTrue(mav != null);
			assertTrue(mav.getViewName().equals("AddRole"));
			agencies = (List<Agency>)mav.getModel().get("agencies");
			assertTrue(agencies != null);
			assertTrue(agencies.size() > 0);
			newCommand = (RoleCommand)mav.getModel().get("command");
			assertTrue(newCommand != null);
			assertFalse(newCommand.getViewOnlyMode());

			aCommand = new RoleCommand();
			aCommand.setAction(RoleCommand.ACTION_SAVE);
			aCommand.setRoleName("New Test Role");
			aCommand.setDescription("A new role for testing");
			aCommand.setAgency(AuthUtil.getRemoteUserObject().getAgency().getOid());
			String[] scopedPrivileges = {};
			aCommand.setScopedPrivileges(scopedPrivileges);
			bindingResult = new BindException(aCommand, RoleCommand.ACTION_SAVE);
			mav = testInstance.processFormSubmission(request, aCommand, bindingResult);
			assertTrue(mav != null);
			assertTrue(mav.getViewName().equals("Roles"));
			List<Role> roles = (List<Role>)mav.getModel().get("roles");
			assertTrue(roles != null);
			assertTrue(roles.size() > 0);

			aCommand = new RoleCommand();
			aCommand.setAction(RoleCommand.ACTION_FILTER);
			String agencyFilter = "Dummy";
			aCommand.setAgencyFilter(agencyFilter);
			bindingResult = new BindException(aCommand, RoleCommand.ACTION_FILTER);
			mav = testInstance.processFormSubmission(request, aCommand, bindingResult);
			assertTrue(mav != null);
			assertTrue(mav.getViewName().equals("Roles"));
			RoleCommand command = (RoleCommand)mav.getModel().get("command");
			assertTrue(command != null);
			assertTrue(agencyFilter.equals(command.getAgencyFilter()));
			assertTrue(agencyFilter.equals((String)request.getSession().getAttribute(RoleCommand.PARAM_AGENCY_FILTER)));
		}
		catch (Exception e)
		{
			String message = e.getClass().toString() + " - " + e.getMessage();
			log.debug(message);
			fail(message);
		}
	}

	@Test
	public final void testShowForm() {
		try
		{
			MockHttpServletRequest request = new MockHttpServletRequest();
			this.testSetAgencyUserManager();
			this.testSetAuthorityManager();
			this.testSetMessageSource();
			ModelAndView mav = testInstance.showForm(request);
			assertTrue(mav != null);
			assertTrue(mav.getViewName().equals("Roles"));
			List roles = (List)mav.getModel().get(RoleCommand.MDL_ROLES);
			assertTrue(roles != null);
			assertFalse(roles.isEmpty());
			RoleCommand command = (RoleCommand)mav.getModel().get("command");
			assertTrue(command != null);
			assertTrue(command.getAgencyFilter().equals(AuthUtil.getRemoteUserObject().getAgency().getName()));
		}
		catch (Exception e)
		{
			String message = e.getClass().toString() + " - " + e.getMessage();
			log.debug(message);
			fail(message);
		}
	}

	@Test
	public final void testSetAgencyUserManager() {
		try
		{
			AgencyUserManager manager = new MockAgencyUserManager(testFile);
			testInstance.setAgencyUserManager(manager);
		}
		catch(Exception e)
		{
			String message = e.getClass().toString() + " - " + e.getMessage();
			log.debug(message);
			fail(message);
		}
	}

	@Test
	public final void testSetMessageSource() {
		try
		{
			testInstance.setMessageSource(new MockMessageSource());
		}
		catch(Exception e)
		{
			String message = e.getClass().toString() + " - " + e.getMessage();
			log.debug(message);
			fail(message);
		}
	}

	@Test
	public final void testSetAuthorityManager() {
		try
		{
			AuthorityManager manager = new AuthorityManager();
			testInstance.setAuthorityManager(manager);
		}
		catch(Exception e)
		{
			String message = e.getClass().toString() + " - " + e.getMessage();
			log.debug(message);
			fail(message);
		}
	}

}
