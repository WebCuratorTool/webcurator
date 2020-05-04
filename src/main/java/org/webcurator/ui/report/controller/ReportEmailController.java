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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.notification.MailServer;
import org.webcurator.core.notification.Mailable;
import org.webcurator.core.report.FileFactory;
import org.webcurator.core.report.OperationalReport;
import org.webcurator.domain.model.auth.User;
import org.webcurator.ui.report.command.ReportEmailCommand;

/**
 * Report Email Controller
 *
 * @author MDubos
 *
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class ReportEmailController {

	public static final String ACTION_EMAIL = "Email";
	public static final String ACTION_CANCEL = "Cancel";

	private Log log = LogFactory.getLog(ReportEmailController.class);
    @Autowired
	private MailServer mailServer;

	@RequestMapping(method = RequestMethod.GET, path = "/curator/report/report-email.html")
	protected ModelAndView showForm() throws Exception {
		return null;
	}

	@RequestMapping(method = RequestMethod.POST, path = "/curator/report/report-email.html")
	protected ModelAndView processFormSubmission(HttpServletRequest req, @ModelAttribute ReportEmailCommand reportEmailCommand) throws Exception {

		ModelAndView mav = new ModelAndView();

		if(reportEmailCommand.getActionCmd().equals(ACTION_EMAIL)){

			OperationalReport operationalReport = (OperationalReport) req.getSession().getAttribute("operationalReport");

			// Get user's email address
			// ...user
	        String remoteUser = null;
	        Authentication auth = null;
	        SecurityContext springSecurityContext = (SecurityContext) req.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
	        if( springSecurityContext != null) {
	            auth = springSecurityContext.getAuthentication();
	            if (auth != null) {
	                remoteUser = auth.getName();
	            }
	        }
	        // ...email address
	        User user = (User) auth.getDetails();
	        String userEmailAddress = user.getEmail();

	        // Build attachment content
			String dataAttachment = operationalReport.getRendering(reportEmailCommand.getFormat());

			// E-mail
			Mailable email = new Mailable();
			email.setRecipients(reportEmailCommand.getRecipient());
			email.setSender(userEmailAddress);
			email.setSubject(reportEmailCommand.getSubject());
			email.setMessage(reportEmailCommand.getMessage());
			mailServer.send(email,
					"report" + FileFactory.getFileExtension(reportEmailCommand.getFormat()),
					FileFactory.getMIMEType(reportEmailCommand.getFormat()),
					dataAttachment );

			log.debug("email sent:");
			log.debug("  from:" + userEmailAddress);
			log.debug("  format=" + reportEmailCommand.getFormat());
			log.debug("  to=" + reportEmailCommand.getRecipient());
			log.debug("  subject=" + reportEmailCommand.getSubject());
			log.debug("  msg=" + reportEmailCommand.getMessage());

			mav.setViewName("reporting-preview");

		} else {
			log.error("Did not get send request: " + reportEmailCommand.getActionCmd());
			mav.setViewName("reporting-preview");
		}

		return mav;

	}

	/**
	 *
	 * @param mailServer Set the email server
	 */
	public void setMailServer(MailServer mailServer) {
		this.mailServer = mailServer;
	}


}
