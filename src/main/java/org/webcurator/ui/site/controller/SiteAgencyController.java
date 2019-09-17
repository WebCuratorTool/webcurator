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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;
import org.webcurator.core.sites.SiteManager;
import org.webcurator.domain.model.core.AuthorisingAgent;
import org.webcurator.domain.model.core.BusinessObjectFactory;
import org.webcurator.common.ui.Constants;
import org.webcurator.ui.site.SiteEditorContext;
import org.webcurator.ui.site.command.SiteAuthorisingAgencyCommand;
import org.webcurator.ui.util.Tab;
import org.webcurator.ui.util.TabbedController.TabbedModelAndView;

/**
 * The manager for Harvest Authorisation actions.
 * @author nwaight
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class SiteAgencyController {

	/** The site manager */
	@Autowired
	private SiteManager siteManager;
    @Autowired
	private SiteController siteController;
	/** BusinessObjectFactory */
	@Autowired
	private BusinessObjectFactory businessObjectFactory;

	public SiteAgencyController() {
	}

	public SiteEditorContext getEditorContext(HttpServletRequest req) {
		SiteEditorContext ctx = (SiteEditorContext) req.getSession().getAttribute("siteEditorContext");
		if( ctx == null) {
			throw new IllegalStateException("siteEditorContext not yet bound to the session");
		}

		return ctx;
	}

	protected ModelAndView handle(HttpServletRequest aReq, HttpServletResponse aResp, Object aCommand,
                                  BindingResult bindingResult) throws Exception {

		SiteAuthorisingAgencyCommand cmd = (SiteAuthorisingAgencyCommand) aCommand;
		SiteEditorContext ctx = getEditorContext(aReq);

		// Handle Cancel
		if(WebUtils.hasSubmitParameter(aReq, "_cancel_auth_agent")) {
			Tab membersTab = siteController.getTabConfig().getTabByID("AUTHORISING_AGENCIES");
			TabbedModelAndView tmav = membersTab.getTabHandler().preProcessNextTab(siteController, membersTab, aReq, aResp, cmd, bindingResult);
			tmav.getTabStatus().setCurrentTab(membersTab);
			return tmav;
		}

		// Handle Save
		if(WebUtils.hasSubmitParameter(aReq, "_save_auth_agent")) {

			if (bindingResult.hasErrors()) {
				ModelAndView mav = new ModelAndView();
				mav.addObject(Constants.GBL_CMD_DATA, bindingResult.getTarget());
				mav.addObject(Constants.GBL_ERRORS, bindingResult);
				mav.setViewName(Constants.VIEW_SITE_AGENCIES);
				mav.addObject("authAgencyEditMode", true);

				return mav;
			}

			// Are we creating a new item, or updating an existing
			// one?
			if(isEmpty(cmd.getIdentity())) {
				AuthorisingAgent agent = businessObjectFactory.newAuthorisingAgent();
				cmd.updateBusinessModel(agent);
				ctx.putObject(agent);
				ctx.getSite().getAuthorisingAgents().add(agent);
			}
			else {
				AuthorisingAgent agent = (AuthorisingAgent) ctx.getObject(AuthorisingAgent.class, cmd.getIdentity());
				cmd.updateBusinessModel(agent);
			}

			Tab membersTab = siteController.getTabConfig().getTabByID("AUTHORISING_AGENCIES");
			TabbedModelAndView tmav = membersTab.getTabHandler().preProcessNextTab(siteController, membersTab, aReq, aResp, cmd, bindingResult);
			tmav.getTabStatus().setCurrentTab(membersTab);
			return tmav;
		}

		ModelAndView mav = new ModelAndView();
		mav.addObject(Constants.GBL_CMD_DATA, cmd);
		mav.addObject(Constants.GBL_ERRORS, bindingResult);
		mav.setViewName(Constants.VIEW_SITE_AGENCIES);

		return mav;
	}

	private boolean isEmpty(String aString) {
		return aString == null || aString.trim().equals("");
	}

	/**
	 * @param siteController the siteController to set
	 */
	public void setSiteController(SiteController siteController) {
		this.siteController = siteController;
	}

	/**
	 * @param businessObjectFactory The busObjFactory to set.
	 */
	public void setBusinessObjectFactory(BusinessObjectFactory businessObjectFactory) {
		this.businessObjectFactory = businessObjectFactory;
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
