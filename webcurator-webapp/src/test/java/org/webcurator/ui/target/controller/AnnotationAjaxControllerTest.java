package org.webcurator.ui.target.controller;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.context.MockMessageSource;
import org.springframework.mock.web.*;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.test.BaseWCTTest;
import org.webcurator.common.ui.Constants;
import org.webcurator.ui.target.command.TargetInstanceCommand;
import org.webcurator.common.util.DateUtils;
import org.webcurator.core.scheduler.*;
import org.webcurator.core.targets.MockTargetManager;
import org.webcurator.domain.MockTargetInstanceDAO;

public class AnnotationAjaxControllerTest extends BaseWCTTest<AnnotationAjaxController> {

    public AnnotationAjaxControllerTest() {
        super(AnnotationAjaxController.class,
                "/org/webcurator/ui/target/controller/QueueControllerTest.xml");
    }


    //Override BaseWCTTest setup method
    public void setUp() throws Exception {

        super.setUp();
        //add the extra bits
        DateUtils.get().setMessageSource(new MockMessageSource());

        MockTargetInstanceManager tim = new MockTargetInstanceManager(testFile);
        MockTargetInstanceDAO tidao = new MockTargetInstanceDAO(testFile);

        tim.setTargetInstanceDao(tidao);

        testInstance.setTargetInstanceManager(new MockTargetInstanceManager(testFile));
        testInstance.setTargetManager(new MockTargetManager(testFile));

    }

    @Test
    public final void testProcessFormSubmissionTargetInstanceRequest() {

        try {
            MockHttpServletRequest aReq = new MockHttpServletRequest();
            TargetInstanceCommand aCmd = new TargetInstanceCommand();
            aCmd.setCmd(TargetInstanceCommand.SESSION_TI_SEARCH_CRITERIA);
            aReq.getSession().setAttribute(TargetInstanceCommand.SESSION_TI_SEARCH_CRITERIA, new TargetInstanceCommand());

            aReq.setParameter(Constants.AJAX_REQUEST_TYPE, Constants.AJAX_REQUEST_FOR_TI_ANNOTATIONS);
            ModelAndView mav = testInstance.processFormSubmission(0L, 1L, Constants.AJAX_REQUEST_FOR_TI_ANNOTATIONS,     aReq);
            assertTrue(mav != null);
            assertTrue(mav.getViewName().equals(Constants.VIEW_TI_ANNOTATION_HISTORY));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


}
