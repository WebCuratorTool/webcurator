package org.webcurator.ui.target.controller;

import static org.junit.Assert.*;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.context.MockMessageSource;
import org.springframework.mock.web.*;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.test.BaseWCTTest;
import org.webcurator.common.Constants;
import org.webcurator.ui.target.command.TargetInstanceCommand;
import org.webcurator.common.util.DateUtils;
import org.webcurator.core.scheduler.*;
import org.webcurator.core.targets.MockTargetManager;
import org.webcurator.domain.MockTargetInstanceDAO;

public class AnnotationAjaxControllerTest extends BaseWCTTest<AnnotationAjaxController> {

	public AnnotationAjaxControllerTest()
	{
		super(AnnotationAjaxController.class,
				"src/test/java/org/webcurator/ui/target/controller/QueueControllerTest.xml");
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

		try
		{
			MockHttpServletRequest aReq = new MockHttpServletRequest();
			HttpServletResponse aResp = new MockHttpServletResponse();
			TargetInstanceCommand aCmd = new TargetInstanceCommand();
			aCmd.setCmd(TargetInstanceCommand.SESSION_TI_SEARCH_CRITERIA);
			aReq.getSession().setAttribute(TargetInstanceCommand.SESSION_TI_SEARCH_CRITERIA, new TargetInstanceCommand());
			aReq.setParameter("targetOid", "0");
			aReq.setParameter("targetInstanceOid", "1");
			BindException aErrors = new BindException(aCmd, TargetInstanceCommand.SESSION_TI_SEARCH_CRITERIA);

			aReq.setParameter(Constants.AJAX_REQUEST_TYPE, Constants.AJAX_REQUEST_FOR_TI_ANNOTATIONS);
			ModelAndView mav = testInstance.processFormSubmission(aReq, aResp, aCmd, aErrors);
			assertTrue(mav != null);
			assertTrue(mav.getViewName().equals(Constants.VIEW_TI_ANNOTATION_HISTORY));
			assertFalse(aErrors.hasErrors());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}


}
