package org.webcurator.ui.profiles.controller;

import static org.junit.Assert.*;

import java.util.*;
import org.junit.Test;
import org.springframework.mock.web.*;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.test.BaseWCTTest;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.agency.*;
import org.webcurator.core.profiles.*;
import org.webcurator.core.util.AuthUtil;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.domain.model.auth.Privilege;
import org.webcurator.domain.model.dto.*;
import org.webcurator.ui.profiles.command.ProfileListCommand;

public class ProfileListControllerTest extends BaseWCTTest<ProfileListController> {

	public ProfileListControllerTest()
	{
		super(ProfileListController.class,
                "/org/webcurator/ui/profiles/controller/ProfileListControllerTest.xml");
	}

	private void performTestGetView(int scope, String privilege, boolean showInactive)
	{
		ModelAndView mav = null;

		this.removeAllCurrentUserPrivileges();

		if(scope >= 0)
		{
			this.addCurrentUserPrivilege(scope, privilege);
		}
		ProfileListCommand comm = new ProfileListCommand();
		comm.setShowInactive(showInactive);
		mav = testInstance.getView(comm);
		assertTrue(mav != null);
		assertTrue(mav.getViewName().equals("profile-list"));
		ProfileListCommand command = (ProfileListCommand)mav.getModel().get("command");
		assertTrue(command != null);
		assertEquals(command.isShowInactive(), showInactive);
		List<Agency> agencies = (List<Agency>)mav.getModel().get("agencies");
		assertTrue(agencies != null);
		List<ProfileDTO> profiles = (List<ProfileDTO>)mav.getModel().get("profiles");
		assertTrue(profiles != null);
		switch(scope)
		{
		case -1:
			assertTrue(agencies.size() == 0);
			assertTrue(profiles.size() == 0);
			break;
		case Privilege.SCOPE_AGENCY:
			assertTrue(agencies.size() == 1);
			assertTrue(showInactive?profiles.size() == 3: profiles.size() == 2);
			break;
		case Privilege.SCOPE_ALL:
			assertTrue(agencies.size() == 2);
			assertTrue(showInactive?profiles.size() == 5: profiles.size() == 3);
			break;
		}
	}

    private void performTestFilter(int scope, String privilege, boolean showInactive)
    {
        ModelAndView mav = null;

        this.removeAllCurrentUserPrivileges();

        if(scope >= 0)
        {
            this.addCurrentUserPrivilege(scope, privilege);
        }

        try
        {
            ProfileListCommand comm = new ProfileListCommand();
            comm.setShowInactive(showInactive);

            MockHttpSession session = new MockHttpSession();
            BindingResult bindingResult = new BindException(comm, null);

            mav = testInstance.filter(comm, bindingResult, session);
            assertTrue(mav != null);
            assertTrue(mav.getViewName().equals("profile-list"));
            ProfileListCommand command = (ProfileListCommand)mav.getModel().get("command");
            assertTrue(command != null);
            assertEquals(command.isShowInactive(), showInactive);

            assertTrue(command.getDefaultAgency().equals(""));

            List<Agency> agencies = (List<Agency>)mav.getModel().get("agencies");
            assertTrue(agencies != null);
            List<ProfileDTO> profiles = (List<ProfileDTO>)mav.getModel().get("profiles");
            assertTrue(profiles != null);
            switch(scope)
            {
                case -1:
                    assertTrue(agencies.size() == 0);
                    assertTrue(profiles.size() == 0);
                    break;
                case Privilege.SCOPE_AGENCY:
                    assertTrue(agencies.size() == 1);
                    assertTrue(showInactive?profiles.size() == 3: profiles.size() == 2);
                    break;
                case Privilege.SCOPE_ALL:
                    assertTrue(agencies.size() == 2);
                    assertTrue(showInactive?profiles.size() == 5: profiles.size() == 3);
                    break;
            }
            assertTrue((session.getAttribute(ProfileListController.SESSION_KEY_SHOW_INACTIVE)).equals(showInactive));
        }
        catch(Exception e)
        {
            fail(e.getClass().getName()+": "+e.getMessage());
        }
    }

    private void performTestList(int scope, String privilege, boolean showInactive)
    {
        ModelAndView mav = null;

        this.removeAllCurrentUserPrivileges();

        if(scope >= 0)
        {
            this.addCurrentUserPrivilege(scope, privilege);
        }

        try
        {
            ProfileListCommand comm = new ProfileListCommand();
            comm.setShowInactive(showInactive);

            MockHttpSession session = new MockHttpSession();
            BindingResult bindingResult = new BindException(comm, null);

            mav = testInstance.defaultList(comm, bindingResult, session);
            assertTrue(mav != null);
            assertTrue(mav.getViewName().equals("profile-list"));
            ProfileListCommand command = (ProfileListCommand)mav.getModel().get("command");
            assertTrue(command != null);
            assertEquals(command.isShowInactive(), showInactive);

            assertTrue(command.getDefaultAgency().equals(AuthUtil.getRemoteUserObject().getAgency().getName()));

            List<Agency> agencies = (List<Agency>)mav.getModel().get("agencies");
            assertTrue(agencies != null);
            List<ProfileDTO> profiles = (List<ProfileDTO>)mav.getModel().get("profiles");
            assertTrue(profiles != null);
            switch(scope)
            {
                case -1:
                    assertTrue(agencies.size() == 0);
                    assertTrue(profiles.size() == 0);
                    break;
                case Privilege.SCOPE_AGENCY:
                    assertTrue(agencies.size() == 1);
                    assertTrue(showInactive?profiles.size() == 3: profiles.size() == 2);
                    break;
                case Privilege.SCOPE_ALL:
                    assertTrue(agencies.size() == 2);
                    assertTrue(showInactive?profiles.size() == 5: profiles.size() == 3);
                    break;
            }
            assertTrue((session.getAttribute(ProfileListController.SESSION_KEY_SHOW_INACTIVE)).equals(showInactive));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            fail(e.getClass().getName()+": "+e.getMessage());
        }
    }

    @Test
	public final void testProfileListController() {
		assertTrue(testInstance != null);
	}

	@Test
	public final void testHandle() {

		this.testSetAgencyUserManager();
		this.testSetAuthorityManager();
		this.testSetProfileManager();

		performTestList(-1, "", false);
        performTestList(-1, "", true);
        performTestList(Privilege.SCOPE_AGENCY, Privilege.VIEW_PROFILES, false);
        performTestList(Privilege.SCOPE_AGENCY, Privilege.VIEW_PROFILES, true);
        performTestList(Privilege.SCOPE_AGENCY, Privilege.MANAGE_PROFILES, false);
        performTestList(Privilege.SCOPE_AGENCY, Privilege.MANAGE_PROFILES, true);
        performTestList(Privilege.SCOPE_ALL, Privilege.VIEW_PROFILES, false);
        performTestList(Privilege.SCOPE_ALL, Privilege.VIEW_PROFILES, true);
        performTestList(Privilege.SCOPE_ALL, Privilege.MANAGE_PROFILES, false);
        performTestList(Privilege.SCOPE_ALL, Privilege.MANAGE_PROFILES, true);
		performTestFilter(-1, "", false);
		performTestFilter(-1, "", true);
		performTestFilter(Privilege.SCOPE_AGENCY, Privilege.VIEW_PROFILES, false);
		performTestFilter(Privilege.SCOPE_AGENCY, Privilege.VIEW_PROFILES, true);
		performTestFilter(Privilege.SCOPE_AGENCY, Privilege.MANAGE_PROFILES, false);
		performTestFilter(Privilege.SCOPE_AGENCY, Privilege.MANAGE_PROFILES, true);
		performTestFilter(Privilege.SCOPE_ALL, Privilege.VIEW_PROFILES, false);
		performTestFilter(Privilege.SCOPE_ALL, Privilege.VIEW_PROFILES, true);
		performTestFilter(Privilege.SCOPE_ALL, Privilege.MANAGE_PROFILES, false);
		performTestFilter(Privilege.SCOPE_ALL, Privilege.MANAGE_PROFILES, true);
	}

	@Test
	public final void testGetView() {

		this.testSetAgencyUserManager();
		this.testSetAuthorityManager();
		this.testSetProfileManager();

		performTestGetView(-1, "", false);
		performTestGetView(-1, "", true);
		performTestGetView(Privilege.SCOPE_AGENCY, Privilege.VIEW_PROFILES, false);
		performTestGetView(Privilege.SCOPE_AGENCY, Privilege.VIEW_PROFILES, true);
		performTestGetView(Privilege.SCOPE_AGENCY, Privilege.MANAGE_PROFILES, false);
		performTestGetView(Privilege.SCOPE_AGENCY, Privilege.MANAGE_PROFILES, true);
		performTestGetView(Privilege.SCOPE_ALL, Privilege.VIEW_PROFILES, false);
		performTestGetView(Privilege.SCOPE_ALL, Privilege.VIEW_PROFILES, true);
		performTestGetView(Privilege.SCOPE_ALL, Privilege.MANAGE_PROFILES, false);
		performTestGetView(Privilege.SCOPE_ALL, Privilege.MANAGE_PROFILES, true);
	}

	@Test
	public final void testSetProfileManager() {
		testInstance.setProfileManager(new MockProfileManager(testFile));
	}

	@Test
	public final void testSetAgencyUserManager() {
		testInstance.setAgencyUserManager(new MockAgencyUserManager(testFile));
	}

	@Test
	public final void testSetAuthorityManager() {
		testInstance.setAuthorityManager(new AuthorityManager());
	}

}
