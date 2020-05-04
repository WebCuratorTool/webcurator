package org.webcurator.ui.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

public class TestTabbedController extends TabbedController{

	List <String> methodsCalled;




	@Override
	protected ModelAndView processCancel(Tab currentTab,
			HttpServletRequest req, HttpServletResponse res, Object comm,
			BindingResult bindingResult) {
		methodsCalled.add("TabbedController.processCancel");
		return null;
	}

	@Override
	protected ModelAndView processInitial(HttpServletRequest req, HttpServletResponse res, Object comm,
                                          BindingResult bindingResult) {
		methodsCalled.add("TabbedController.processInitial");
		return null;
	}

	@Override
	protected ModelAndView processSave(Tab currentTab, HttpServletRequest req, HttpServletResponse res, Object comm,
                                       BindingResult bindingResult) {
		methodsCalled.add("TabbedController.processSave");
		return null;
	}

	@Override
	protected ModelAndView showForm(HttpServletRequest req, HttpServletResponse res, Object command,
                                    BindingResult bindingResult)
			throws Exception {
		methodsCalled.add("TabbedController.showForm");
		return null;
	}

	@Override
	protected void switchToEditMode(HttpServletRequest req) {
		methodsCalled.add("TabbedController.switchToEditMode");

	}

	public List <String> getMethodsCalled()
	{
		return methodsCalled;
	}

	public void setMethodsCalled(List<String> methodsCalled) {
		this.methodsCalled = methodsCalled;
	}



}
