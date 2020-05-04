/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.webcurator.ui.report.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.report.FileFactory;
import org.webcurator.core.report.OperationalReport;
import org.webcurator.ui.report.command.ReportPreviewCommand;

/**
 * Report Preview Controller
 * @author MDubos
 *
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class ReportPreviewController {

	public static final String ACTION_PRINT = "Print";
	public static final String ACTION_SAVE  = "Save";
	public static final String ACTION_EMAIL = "E-mail";

	private Log log = LogFactory.getLog(ReportPreviewController.class);

	@RequestMapping(method = RequestMethod.GET, path = "/curator/report/report-preview.html")
	protected ModelAndView showForm() throws Exception {

		return null;
	}

	@RequestMapping(method = RequestMethod.POST, path = "/curator/report/report-preview.html")
	protected ModelAndView processFormSubmission(HttpServletRequest req, @ModelAttribute ReportPreviewCommand reportPreviewCommand) throws Exception {

		log.debug("process...");

		ModelAndView mav = new ModelAndView();
		OperationalReport operationalReport =
			(OperationalReport) req.getSession().getAttribute("operationalReport");

		log.debug("action=" + reportPreviewCommand.getActionCmd());

		if(reportPreviewCommand.getActionCmd().equals(ACTION_PRINT)){
			mav.setViewName("reporting-preview");
		}

		else if(reportPreviewCommand.getActionCmd().equals(ACTION_SAVE)){
			mav.addObject("formats", FileFactory.getFormats());
			mav.addObject("operationalReport", operationalReport);
			mav.setViewName("reporting-save");
		}

		else if(reportPreviewCommand.getActionCmd().equals(ACTION_EMAIL)){
			mav.addObject("formats", FileFactory.getFormats());
			mav.addObject("subject", operationalReport.getName());
			mav.setViewName("reporting-email");
		}


		return mav;

	}



}
