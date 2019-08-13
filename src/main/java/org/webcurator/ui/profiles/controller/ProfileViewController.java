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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.profiles.ProfileManager;
import org.webcurator.domain.model.auth.Privilege;
import org.webcurator.domain.model.core.Profile;
import org.webcurator.ui.common.CommonViews;
import org.webcurator.ui.profiles.command.ViewCommand;


/**
 * Controller to handle users viewing profiles.
 * @author bbeaumont
 *
 */
@Controller
@RequestMapping(path = "/curator/profiles/view")
public class ProfileViewController {
	/** The profile manager to load the profile */
	@Autowired
	private ProfileManager profileManager;
	/** The authority manager for checking permissions */
	@Autowired
	private AuthorityManager authorityManager;

	/**
	 * Construct a new ProfileViewController.
	 */
	public ProfileViewController() {
	}

	@GetMapping
	protected ModelAndView getView(@ModelAttribute("command") ViewCommand command) {
		Profile profile = profileManager.load(command.getProfileOid());

		if(authorityManager.hasPrivilege(profile, Privilege.VIEW_PROFILES)) {
			ModelAndView mav = new ModelAndView("profile-view");
			mav.addObject("profile", profile);
			return mav;
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
	 * @param profileManager The profileManager to set.
	 */
	public void setProfileManager(ProfileManager profileManager) {
		this.profileManager = profileManager;
	}

	/**
	 * @param authorityManager The authorityManager to set.
	 */
	public void setAuthorityManager(AuthorityManager authorityManager) {
		this.authorityManager = authorityManager;
	}



}
