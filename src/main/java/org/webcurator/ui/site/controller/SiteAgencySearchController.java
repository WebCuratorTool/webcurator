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
package org.webcurator.ui.site.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.sites.SiteManager;
import org.webcurator.domain.Pagination;
import org.webcurator.domain.model.core.AuthorisingAgent;
import org.webcurator.common.ui.Constants;
import org.webcurator.ui.site.SiteEditorContext;
import org.webcurator.ui.site.command.AgencySearchCommand;
import org.webcurator.ui.util.Tab;
import org.webcurator.ui.util.TabbedController.TabbedModelAndView;

/**
 * The controller for managing searching for authorising agents.
 * @author bbeaumont
 */
@Controller
public class SiteAgencySearchController {
    @Autowired
	private SiteManager siteManager;
    @Autowired
	private SiteController siteController;

	public SiteAgencySearchController() {
	}

	/**
	 * Retrive the editor context for the groups controller.
	 * @param req The HttpServletRequest so the session can be retrieved.
	 * @return The editor context.
	 */
	public SiteEditorContext getEditorContext(HttpServletRequest req) {
		SiteEditorContext ctx = (SiteEditorContext) req.getSession().getAttribute(SiteController.EDITOR_CONTEXT);
		if( ctx == null) {
			throw new IllegalStateException("Editor Context not yet bound to the session");
		}

		return ctx;
	}


	protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object comm,
                                  BindingResult bindingResult) throws Exception {
		AgencySearchCommand command = (AgencySearchCommand) comm;

		if(AgencySearchCommand.ACTION_ADD.equals(command.getActionCmd())) {

			if(bindingResult.hasErrors()) {
				return getSearchView(comm, bindingResult);
			}
			else {
				long[] newAuthAgents = command.getSelectedOids();

				if(newAuthAgents != null && newAuthAgents.length > 0 ) {
					// Perform some validation before allowing the members to be
					// added.
					for(int i=0; i<newAuthAgents.length; i++) {
						AuthorisingAgent agent = siteManager.loadAuthorisingAgent(newAuthAgents[i]);
						SiteEditorContext ctx = getEditorContext(request);
						ctx.putObject(agent);
						ctx.getSite().getAuthorisingAgents().add(agent);
					}

					// Go back to the main view.
					return getAgencyTab(request, response, comm, bindingResult);
				}
				else {
					// Shouldn't ever reach this code, as newAuthAgents being
					// null or empty should cause a validation failure.
					return getSearchView(comm, bindingResult);
				}
			}
		}
		else if(AgencySearchCommand.ACTION_CANCEL.equals(command.getActionCmd())) {
			// Go back to the main view.
			return getAgencyTab(request, response, comm, bindingResult);
		}
		else {
			return getSearchView(comm, bindingResult);
		}
	}

	private ModelAndView getSearchView(Object comm, BindingResult bindingResult) {
		AgencySearchCommand command = (AgencySearchCommand) comm;
		Pagination results = siteManager.searchAuthAgents(command.getName(), command.getPageNumber());

		ModelAndView mav = new ModelAndView("site-auth-agency-search");
		mav.addObject("results", results);
		mav.addObject(Constants.GBL_CMD_DATA, command);

		if(bindingResult.hasErrors()) {
			mav.addObject(Constants.GBL_ERRORS, bindingResult);
		}

		return mav;
	}

	private ModelAndView getAgencyTab(HttpServletRequest request, HttpServletResponse response, Object command,
                                      BindingResult bindingResult) {
		// Go back to the Members tab on the groups controller.
		Tab authAgentTab = siteController.getTabConfig().getTabByID("AUTHORISING_AGENCIES");
		TabbedModelAndView tmav = authAgentTab.getTabHandler().preProcessNextTab(siteController, authAgentTab, request, response, command, bindingResult);
		tmav.getTabStatus().setCurrentTab(authAgentTab);
		return tmav;
	}

	/**
	 * @return Returns the siteController.
	 */
	public SiteController getSiteController() {
		return siteController;
	}

	/**
	 * @param siteController The siteController to set.
	 */
	public void setSiteController(SiteController siteController) {
		this.siteController = siteController;
	}

	/**
	 * @return Returns the siteManager.
	 */
	public SiteManager getSiteManager() {
		return siteManager;
	}

	/**
	 * @param siteManager The siteManager to set.
	 */
	public void setSiteManager(SiteManager siteManager) {
		this.siteManager = siteManager;
	}

}
