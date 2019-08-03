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
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.report.FileFactory;
import org.webcurator.core.report.OperationalReport;
import org.webcurator.ui.report.command.ReportPreviewCommand;

/**
 * Report Preview Controller
 * @author MDubos
 *
 */
public class ReportPreviewController {

	public static final String ACTION_PRINT = "Print";
	public static final String ACTION_SAVE  = "Save";
	public static final String ACTION_EMAIL = "E-mail";

	private Log log = LogFactory.getLog(ReportPreviewController.class);

	/**
	 * Default constructor
	 *
	 */
	public ReportPreviewController() {
	}


	protected ModelAndView showForm() throws Exception {

		return null;
	}

	protected ModelAndView processFormSubmission(HttpServletRequest req, Object comm) throws Exception {

		log.debug("process...");

		ReportPreviewCommand com = (ReportPreviewCommand)comm;
		ModelAndView mav = new ModelAndView();
		OperationalReport operationalReport =
			(OperationalReport) req.getSession().getAttribute("operationalReport");

		log.debug("action=" + com.getActionCmd());

		if(com.getActionCmd().equals(ACTION_PRINT)){
			mav.setViewName("reporting-preview");
		}

		else if(com.getActionCmd().equals(ACTION_SAVE)){
			mav.addObject("formats", FileFactory.getFormats());
			mav.addObject("operationalReport", operationalReport);
			mav.setViewName("reporting-save");
		}

		else if(com.getActionCmd().equals(ACTION_EMAIL)){
			mav.addObject("formats", FileFactory.getFormats());
			mav.addObject("subject", operationalReport.getName());
			mav.setViewName("reporting-email");
		}


		return mav;

	}



}
