package org.webcurator.ui.profiles.controller;

import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.springframework.mock.web.*;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.common.ui.Constants;
import org.webcurator.test.BaseWCTTest;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.agency.*;
import org.webcurator.core.profiles.*;
import org.webcurator.domain.model.auth.Privilege;
import org.webcurator.ui.profiles.command.ProfileListCommand;
import org.webcurator.ui.profiles.command.ViewCommand;
import org.springframework.context.MockMessageSource;

public class DeleteProfileControllerTest extends BaseWCTTest<DeleteProfileController> {

	public DeleteProfileControllerTest()
	{
		super(DeleteProfileController.class,
                "/org/webcurator/ui/profiles/controller/ProfileListControllerTest.xml");
	}


	private ModelAndView performTestHandle(String defaultAgency, Long profileOid)
	{
		ModelAndView mav = null;

		try
		{
			HttpServletRequest req = new MockHttpServletRequest();
			req.getSession().setAttribute(ProfileListController.SESSION_KEY_SHOW_INACTIVE, false);
			req.getSession().setAttribute(ProfileListController.SESSION_AGENCY_FILTER, defaultAgency);

			ViewCommand comm = new ViewCommand();
			comm.setProfileOid(profileOid);

            BindingResult bindingResult = new BindException(comm, "DUMMY-COMMAND");

			mav = testInstance.handle(req, comm, bindingResult);
			assertTrue(mav != null);
		}
		catch(Exception e)
		{
			fail(e.getClass().getName()+": "+e.getMessage());
		}

		return mav;
	}

	@Test
	public final void testHandle() {
		ModelAndView mav = null;
		ProfileListCommand command = null;

		this.testSetMessageSource();
		testInstance.setAuthorityManager(new AuthorityManager());
		testInstance.setProfileManager(new MockProfileManager(testFile));
		testInstance.setAgencyUserManager(new MockAgencyUserManager(testFile));

		try
		{
			this.removeAllCurrentUserPrivileges();
			mav = performTestHandle("", 13000L);
			assertTrue(mav.getViewName().equals("authorisation-failure"));

			this.addCurrentUserPrivilege(Privilege.SCOPE_ALL, Privilege.MANAGE_PROFILES);
			mav = performTestHandle("", 13000L);
			command = (ProfileListCommand)mav.getModel().get("command");
			assertTrue(command != null);
			assertTrue(command.getDefaultAgency().equals(""));
			assertTrue(mav.getViewName().equals("profile-list"));
			assertTrue(mav.getModel().get(Constants.GBL_ERRORS)!= null);

			mav = performTestHandle("", 13001L);
			command = (ProfileListCommand)mav.getModel().get("command");
			assertTrue(command != null);
			assertTrue(command.getDefaultAgency().equals(""));
			assertTrue(mav.getViewName().equals("profile-list"));
			assertTrue(mav.getModel().get(Constants.GBL_MESSAGES)!= null);
		}
		catch(Exception e)
		{
			fail(e.getClass().getName()+": "+e.getMessage());
		}
	}

	@Test
	public final void testDeleteProfileController() {
		assertTrue(testInstance != null);
	}

	@Test
	public final void testGetProfileManager() {
		testInstance.setProfileManager(new MockProfileManager(testFile));
		assertTrue(testInstance.getProfileManager() != null);
	}

	@Test
	public final void testSetMessageSource() {
		testInstance.setMessageSource(new MockMessageSource());
	}

}
