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
package org.webcurator.ui.profiles.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.profiles.ProfileManager;
import org.webcurator.core.util.AuthUtil;
import org.webcurator.domain.model.auth.Privilege;
import org.webcurator.domain.model.core.Profile;
import org.webcurator.ui.common.CommonViews;
import org.webcurator.common.ui.Constants;
import org.webcurator.ui.profiles.command.ProfileListCommand;
import org.webcurator.ui.profiles.command.ViewCommand;

/**
 * Delete a profile.
 * @author bbeaumont
 *
 */
@Controller
@RequestMapping("/curator/profiles/delete.html")
public class DeleteProfileController extends ProfileListController {

	/** The Message Source for retrieving messages */
	@Autowired
	private MessageSource messageSource;

	protected ModelAndView handle(HttpServletRequest req, Object comm, BindingResult bindingResult) throws Exception {
		ViewCommand command = (ViewCommand) comm;
		Profile profile = profileManager.load(command.getProfileOid());
		if(authorityManager.hasPrivilege(profile, Privilege.MANAGE_PROFILES)) {
			boolean showInactive = (Boolean) req.getSession().getAttribute(ProfileListController.SESSION_KEY_SHOW_INACTIVE);
	        String defaultAgency = (String)req.getSession().getAttribute(ProfileListController.SESSION_AGENCY_FILTER);
	        if(defaultAgency == null)
	        {
	        	defaultAgency = AuthUtil.getRemoteUserObject().getAgency().getName();
	        }
			ProfileListCommand pcomm = new ProfileListCommand();
			pcomm.setShowInactive(showInactive);
	        pcomm.setDefaultAgency(defaultAgency);

			if(profileManager.isProfileInUse(profile)) {
				bindingResult.reject("profile.bindingResult.delete.inuse", new Object[] { profile.getName() }, "Profile '" + profile.getName() + "' is in use");
				ModelAndView mav = getView(pcomm);
				mav.addObject(Constants.GBL_ERRORS, bindingResult);
				return mav;
			}
			else {
				profileManager.delete(profile);
				ModelAndView mav = getView(pcomm);
				String message = messageSource.getMessage("profile.message.delete.success", new Object[] { profile.getName() },  Locale.getDefault());
				mav.addObject(Constants.GBL_MESSAGES, message);
				return mav;
			}
		}
		else {
			return CommonViews.AUTHORISATION_FAILURE;
		}
	}

	/**
	 * @return Returns the profileManager.
	 */
	public ProfileManager getProfileManager() {
		return profileManager;
	}


	/**
	 * @param messageSource The messageSource to set.
	 */
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
