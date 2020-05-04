package org.webcurator.ui.util;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.ui.util.TabbedController.TabbedModelAndView;

public class TestTabHandler extends TabHandler{

	List <String> methodsCalled;



	@Override
	public TabbedModelAndView preProcessNextTab(TabbedController tc,
			Tab nextTabID, HttpServletRequest req, HttpServletResponse res,
			Object comm, BindingResult bindingResult) {
		methodsCalled.add("TabHandler.preProcessNextTab");
		TabbedModelAndView tmav = tc.new TabbedModelAndView();
		return tmav;
	}

	@Override
	public ModelAndView processOther(TabbedController tc, Tab currentTab,
			HttpServletRequest req, HttpServletResponse res, Object comm,
                                     BindingResult bindingResult) {
		methodsCalled.add("TabHandler.processOther");
		return null;
	}

	@Override
	public void processTab(TabbedController tc, Tab currentTab,
			HttpServletRequest req, HttpServletResponse res, Object comm,
                           BindingResult bindingResult) {
		methodsCalled.add("TabHandler.processTab");

	}

	public List<String> getMethodsCalled() {
		return methodsCalled;
	}

	public void setMethodsCalled(List<String> methodsCalled) {
		this.methodsCalled = methodsCalled;
	}

}
