/**
 *
 */
package org.webcurator.ui.admin.controller;

import static org.junit.Assert.*;

import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;
import org.webcurator.test.*;

import org.junit.Test;
import org.springframework.mock.web.*;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.context.MockMessageSource;

import org.webcurator.ui.admin.command.*;
import org.webcurator.core.agency.*;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.ui.admin.validator.AgencyValidator;


/**
 * @author kurwin
 *
 */
public class AgencyControllerTest extends BaseWCTTest<AgencyController> {

    public AgencyControllerTest() {
        super(AgencyController.class,
                "/org/webcurator/ui/admin/controller/AgencyControllerTest.xml");
    }

    /**
     * Test method for {@link org.webcurator.ui.admin.controller.AgencyController#AgencyController()}.
     */
    @Test
    public final void testAgencyController() {
        assertTrue(testInstance != null);
    }

    @Test
    public final void testInitBinder() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletRequestDataBinder binder = new ServletRequestDataBinder(new AgencyCommand(), "command");
        try {
            testInstance.initBinder(request, binder);
        } catch (Exception e) {
            String message = e.getClass().toString() + " - " + e.getMessage();
            log.debug(message);
            fail(message);
        }
    }

    @Test
    public final void testShowForm() {
        try {
            AgencyUserManager manager = new MockAgencyUserManager(testFile);
            testInstance.setAgencyUserManager(manager);
            testInstance.showForm();
        } catch (Exception e) {
            String message = e.getClass().toString() + " - " + e.getMessage();
            log.debug(message);
            fail(message);
        }
    }

    @Test
    public final void testProcessFormSubmission() {
        try {
            BindingResult bindingResult;
            testSetAgencyUserManager();
            testSetMessageSource();


            AgencyCommand aCommand = new AgencyCommand();
            aCommand.setActionCommand(AgencyCommand.ACTION_NEW);
            aCommand.setOid(new Long(2000));
            bindingResult = new BindException(aCommand, AgencyCommand.ACTION_NEW);
            ModelAndView mav = testInstance.processFormSubmission(aCommand, bindingResult);
            assertTrue(mav != null);
            assertTrue(mav.getViewName().equals("newAgency"));
            List<Agency> agencies = (List<Agency>) mav.getModel().get("agencies");
            assertTrue(agencies != null);
            assertTrue(agencies.size() > 0);

            aCommand = new AgencyCommand();
            aCommand.setActionCommand(AgencyCommand.ACTION_VIEW);
            aCommand.setOid(new Long(2000));
            aCommand.setViewOnlyMode(true);
            bindingResult = new BindException(aCommand, AgencyCommand.ACTION_VIEW);
            mav = testInstance.processFormSubmission(aCommand, bindingResult);
            assertTrue(mav != null);
            assertTrue(mav.getViewName().equals("newAgency"));
            agencies = (List<Agency>) mav.getModel().get("agencies");
            assertTrue(agencies != null);
            assertTrue(agencies.size() > 0);
            AgencyCommand newCommand = (AgencyCommand) mav.getModel().get("command");
            assertTrue(newCommand != null);
            assertTrue(newCommand.getViewOnlyMode());

            aCommand = new AgencyCommand();
            aCommand.setActionCommand(AgencyCommand.ACTION_EDIT);
            aCommand.setOid(new Long(2000));
            bindingResult = new BindException(aCommand, AgencyCommand.ACTION_EDIT);
            mav = testInstance.processFormSubmission(aCommand, bindingResult);
            assertTrue(mav != null);
            assertTrue(mav.getViewName().equals("newAgency"));
            agencies = (List<Agency>) mav.getModel().get("agencies");
            assertTrue(agencies != null);
            assertTrue(agencies.size() > 0);
            newCommand = (AgencyCommand) mav.getModel().get("command");
            assertTrue(newCommand != null);
            assertFalse(newCommand.getViewOnlyMode());

            aCommand = new AgencyCommand();
            aCommand.setActionCommand(AgencyCommand.ACTION_SAVE);
            aCommand.setName("New Test Agency");
            aCommand.setAddress("The Agency address");
            bindingResult = new BindException(aCommand, AgencyCommand.ACTION_SAVE);
            mav = testInstance.processFormSubmission(aCommand, bindingResult);
            assertTrue(mav != null);
            assertTrue(mav.getViewName().equals("viewAgencies"));
            agencies = (List<Agency>) mav.getModel().get("agencies");
            assertTrue(agencies != null);
            assertTrue(agencies.size() > 0);
        } catch (Exception e) {
            String message = e.getClass().toString() + " - " + e.getMessage();
            log.debug(message);
            fail(message);
        }
    }

    /**
     * Test method for {@link org.webcurator.ui.admin.controller.AgencyController#setAgencyUserManager(org.webcurator.core.agency.AgencyUserManager)}.
     */
    @Test
    public final void testSetAgencyUserManager() {
        try {
            AgencyUserManager manager = new MockAgencyUserManager(testFile);
            testInstance.setAgencyUserManager(manager);

            AgencyValidator agencyValidator = new AgencyValidator();
            ReflectionTestUtils.setField(agencyValidator, "agencyUserManager",manager);
            ReflectionTestUtils.setField(testInstance, "agencyValidator", agencyValidator);
        } catch (Exception e) {
            String message = e.getClass().toString() + " - " + e.getMessage();
            log.debug(message);
            fail(message);
        }
    }

    /**
     * Test method for {@link org.webcurator.ui.admin.controller.AgencyController#setMessageSource(org.springframework.context.MessageSource)}.
     */
    @Test
    public final void testSetMessageSource() {
        try {
            testInstance.setMessageSource(new MockMessageSource());
        } catch (Exception e) {
            String message = e.getClass().toString() + " - " + e.getMessage();
            log.debug(message);
            fail(message);
        }
    }
}
