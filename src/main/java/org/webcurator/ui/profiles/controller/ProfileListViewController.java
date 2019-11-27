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
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.common.ui.Constants;
import org.webcurator.core.agency.AgencyUserManager;
import org.webcurator.core.harvester.HarvesterType;
import org.webcurator.core.profiles.ProfileManager;
import org.webcurator.core.util.AuthUtil;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.domain.model.auth.Privilege;
import org.webcurator.domain.model.auth.User;
import org.webcurator.domain.model.dto.ProfileDTO;
import org.webcurator.ui.profiles.command.ProfileListCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * Common superclass for several profile controllers,
 * used to generate the profile list view
 */
public class ProfileListViewController {

    /**
     * The profile Manager
     */
    @Autowired
    protected ProfileManager profileManager;
    @Autowired
    protected AgencyUserManager agencyUserManager;
    @Autowired
    protected AuthorityManager authorityManager;


    /**
     * Get the view of the list.
     *
     * @return The view.
     */
    protected ModelAndView getView(ProfileListCommand command) {
        ModelAndView mav = new ModelAndView("profile-list");

        List<Agency> agencies = new ArrayList<>();
        List<ProfileDTO> profiles = new ArrayList<>();
        if (authorityManager.hasAtLeastOnePrivilege(new String[]{Privilege.VIEW_PROFILES, Privilege.MANAGE_PROFILES})) {
            if (authorityManager.hasPrivilege(Privilege.VIEW_PROFILES, Privilege.SCOPE_ALL) ||
                    authorityManager.hasPrivilege(Privilege.MANAGE_PROFILES, Privilege.SCOPE_ALL)) {
                agencies = agencyUserManager.getAgencies();
                profiles = profileManager.getDTOs(command.isShowInactive(), command.getHarvesterType());
            } else {
                User loggedInUser = AuthUtil.getRemoteUserObject();
                Agency usersAgency = loggedInUser.getAgency();
                agencies = new ArrayList<>();
                agencies.add(usersAgency);
                profiles = profileManager.getAgencyDTOs(usersAgency, command.isShowInactive(), command.getHarvesterType());
            }

        }

        mav.addObject(Constants.GBL_CMD_DATA, command);
        mav.addObject("profiles", profiles);
        mav.addObject("agencies", agencies);
        mav.addObject("usersAgency", AuthUtil.getRemoteUserObject().getAgency());
        mav.addObject("types", HarvesterType.values());
        mav.addObject("defaultType", HarvesterType.DEFAULT);
        return mav;
    }

    /**
     * @param profileManager The profileManager to set.
     */
    public void setProfileManager(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    /**
     * @param agencyUserManager The agencyUserManager to set.
     */
    public void setAgencyUserManager(AgencyUserManager agencyUserManager) {
        this.agencyUserManager = agencyUserManager;
    }

    /**
     * @param authorityManager The authorityManager to set.
     */
    public void setAuthorityManager(AuthorityManager authorityManager) {
        this.authorityManager = authorityManager;
    }


}
