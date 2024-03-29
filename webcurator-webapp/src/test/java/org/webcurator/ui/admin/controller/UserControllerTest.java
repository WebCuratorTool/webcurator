package org.webcurator.ui.admin.controller;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.context.MockMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.agency.AgencyUserManager;
import org.webcurator.core.agency.MockAgencyUserManager;
import org.webcurator.test.BaseWCTTest;
import org.webcurator.ui.admin.command.UserCommand;
import org.webcurator.core.util.AuthUtil;


public class UserControllerTest extends BaseWCTTest<UserController>{

	public UserControllerTest()
	{
		super(UserController.class,
                "/org/webcurator/ui/admin/controller/CreateUserControllerTest.xml");
	}

	@Test
	public final void testUserController() {
		assertTrue(testInstance != null);
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
			String agencyFilter = (String)mav.getModel().get(UserCommand.MDL_AGENCYFILTER);
			assertTrue(agencyFilter != null);
			assertTrue(agencyFilter.equals(AuthUtil.getRemoteUserObject().getAgency().getName()));

			request = new MockHttpServletRequest();
			request.getSession().setAttribute(UserCommand.MDL_AGENCYFILTER, "");
			mav = testInstance.showForm(request);
			assertTrue(mav != null);
			agencyFilter = (String)mav.getModel().get(UserCommand.MDL_AGENCYFILTER);
			assertTrue(agencyFilter != null);
			assertTrue(agencyFilter.equals(""));
		}
		catch (Exception e)
		{
			String message = e.getClass().toString() + " - " + e.getMessage();
			log.debug(message);
			fail(message);
		}
	}

	@Test
	public final void testProcessFormSubmissionACTION_STATUS() {
		try
		{
			MockHttpServletRequest request = new MockHttpServletRequest();
			request.getSession().setAttribute(UserCommand.MDL_AGENCYFILTER, "Dummy");
			UserCommand command = new UserCommand();
			command.setOid(1001L);
			command.setCmd(UserCommand.ACTION_STATUS);
            BindingResult bindingResult = new BindException(new UserCommand(), UserCommand.ACTION_STATUS);
			AgencyUserManager manager = new MockAgencyUserManager(testFile);
			testInstance.setAgencyUserManager(manager);
			this.testSetAuthorityManager();
			this.testSetMessageSource();
			ModelAndView mav = testInstance.processFormSubmission(request, command, bindingResult);
			assertTrue(mav != null);
			String agencyFilter = (String)mav.getModel().get(UserCommand.MDL_AGENCYFILTER);
			assertTrue(agencyFilter != null);
			assertTrue(agencyFilter.equals("Dummy"));
			assertFalse(manager.getUserByOid(1001L).isActive());

			request = new MockHttpServletRequest();
			request.getSession().setAttribute(UserCommand.MDL_AGENCYFILTER, "Dummy");
			command = new UserCommand();
			command.setOid(1001L);
			command.setCmd(UserCommand.ACTION_STATUS);
			bindingResult = new BindException(new UserCommand(), UserCommand.ACTION_STATUS);
			mav = testInstance.processFormSubmission(request, command, bindingResult);
			assertTrue(mav != null);
			agencyFilter = (String)mav.getModel().get(UserCommand.MDL_AGENCYFILTER);
			assertTrue(agencyFilter != null);
			assertTrue(agencyFilter.equals("Dummy"));
			assertTrue(manager.getUserByOid(1001L).isActive());
		}
		catch (Exception e)
		{
			String message = e.getClass().toString() + " - " + e.getMessage();
			log.debug(message);
			fail(message);
		}
	}

	@Test
	public final void testProcessFormSubmissionACTION_DELETE() {
		try
		{
			MockHttpServletRequest request = new MockHttpServletRequest();
			request.getSession().setAttribute(UserCommand.MDL_AGENCYFILTER, "Dummy");
			UserCommand command = new UserCommand();
			command.setOid(1001L);
			command.setCmd(UserCommand.ACTION_DELETE);
            BindingResult bindingResult = new BindException(new UserCommand(), UserCommand.ACTION_STATUS);
			AgencyUserManager manager = new MockAgencyUserManager(testFile);
			testInstance.setAgencyUserManager(manager);
			this.testSetAuthorityManager();
			this.testSetMessageSource();
			ModelAndView mav = testInstance.processFormSubmission(request, command, bindingResult);
			assertTrue(mav != null);
			String agencyFilter = (String)mav.getModel().get(UserCommand.MDL_AGENCYFILTER);
			assertTrue(agencyFilter != null);
			assertTrue(agencyFilter.equals("Dummy"));
			assertTrue(manager.getUserByOid(1001L) == null);
		}
		catch (Exception e)
		{
			String message = e.getClass().toString() + " - " + e.getMessage();
			log.debug(message);
			fail(message);
		}
	}

	@Test
	public final void testProcessFormSubmissionACTION_FILTER() {
		try
		{
			MockHttpServletRequest request = new MockHttpServletRequest();
			UserCommand command = new UserCommand();
			command.setCmd(UserCommand.ACTION_FILTER);
			command.setAgencyFilter("Dummy");
            BindingResult bindingResult = new BindException(new UserCommand(), UserCommand.ACTION_STATUS);
			AgencyUserManager manager = new MockAgencyUserManager(testFile);
			testInstance.setAgencyUserManager(manager);
			this.testSetAuthorityManager();
			this.testSetMessageSource();
			ModelAndView mav = testInstance.processFormSubmission(request, command, bindingResult);
			assertTrue(mav != null);
			String agencyFilter = (String)mav.getModel().get(UserCommand.MDL_AGENCYFILTER);
			assertTrue(agencyFilter != null);
			assertTrue(agencyFilter.equals("Dummy"));
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
}
