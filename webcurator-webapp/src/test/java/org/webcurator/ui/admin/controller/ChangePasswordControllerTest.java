package org.webcurator.ui.admin.controller;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.springframework.context.MockMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.agency.AgencyUserManager;
import org.webcurator.core.agency.MockAgencyUserManager;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.test.BaseWCTTest;
import org.webcurator.ui.admin.command.*;
import org.webcurator.ui.admin.validator.ChangePasswordValidator;

public class ChangePasswordControllerTest extends BaseWCTTest<ChangePasswordController>{

	public ChangePasswordControllerTest()
	{
		super(ChangePasswordController.class,
                "/org/webcurator/ui/admin/controller/CreateUserControllerTest.xml");
	}

	@Test
	public final void testChangePasswordController() {
		assertTrue(testInstance != null);
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
	public final void testShowForm() {
		try
		{
			AgencyUserManager manager = new MockAgencyUserManager(testFile);
			testInstance.setAgencyUserManager(manager);
			testInstance.showForm();
		}
		catch (Exception e)
		{
			String message = e.getClass().toString() + " - " + e.getMessage();
			log.debug(message);
			fail(message);
		}
	}

	@Test
	public final void testProcessFormSubmission() {
		MockHttpServletRequest request = new MockHttpServletRequest();
        BindingResult bindingResult;
		testSetAgencyUserManager();
		testSetAuthorityManager();
		testSetMessageSource();
		testSetEncoder();
		ReflectionTestUtils.setField(testInstance, "changePasswordValidator", new ChangePasswordValidator());

		try
		{
			ChangePasswordCommand aCommand = new ChangePasswordCommand();
			aCommand.setAction(ChangePasswordCommand.ACTION_SAVE);
			aCommand.setUserOid(1000L);
			aCommand.setNewPwd("Pa55word");
			aCommand.setConfirmPwd("Pa55word");
			bindingResult = new BindException(aCommand, CreateUserCommand.ACTION_SAVE);
			ModelAndView mav = testInstance.processFormSubmission(request, aCommand, bindingResult);
			assertTrue(mav != null);
			assertTrue(mav.getViewName().equals("viewUsers"));
			List<Agency> agencies = (List<Agency>)mav.getModel().get("agencies");
			assertTrue(agencies != null);
			assertTrue(agencies.size() > 0);
		}
		catch (Exception e)
		{
			String message = e.getClass().toString() + " - " + e.getMessage();
			log.debug(message);
			fail(message);
		}

	}

	@Test
	public final void testSetEncoder() {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		testInstance.setEncoder(passwordEncoder);
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
