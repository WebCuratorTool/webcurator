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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.harvester.HarvesterType;
import org.webcurator.core.harvester.agent.HarvestAgent;
import org.webcurator.core.util.AuthUtil;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.domain.model.core.Profile;
import org.webcurator.common.ui.Constants;
import org.webcurator.ui.profiles.command.ProfileImportCommand;
import org.webcurator.ui.profiles.command.ProfileListCommand;
import org.webcurator.ui.profiles.forms.ProfileImportForm;
import org.webcurator.ui.util.HarvestAgentUtil;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Date;

/**
 * Controller to list all the profiles.
 */
@Controller
public class ProfileListController extends ProfileListViewController {
    private Log log = LogFactory.getLog(ProfileListController.class);

    public static final String SESSION_KEY_SHOW_INACTIVE = "profile-list-show-inactive";
    public static final String SESSION_AGENCY_FILTER = "agency-filter";
    public static final String SESSION_HARVESTER_TYPE_FILTER = "harvester-type-filter";

    @Autowired
    private ApplicationContext context;

    /**
     * Construct a new ProfileListController.
     */
    public ProfileListController() {
    }

    @RequestMapping(path = "/curator/profiles/list/import", method = RequestMethod.POST)
    protected ModelAndView importList(@Valid @ModelAttribute ProfileImportCommand profileImportCommand,
                                      // Note that the BindingResult must come right after the object it validates
                                      // in the parameter list.
                                      BindingResult bindingResult, HttpSession session) {
        session.setAttribute(ProfileListController.SESSION_KEY_SHOW_INACTIVE, profileImportCommand.isShowInactive());
        Profile profile = new Profile();
        profile.setProfile(new String(profileImportCommand.getUploadedFile().getBytes()));
        profile.setDescription("Imported profile");
        profile.setImported(true);
        if (StringUtils.isBlank(profileImportCommand.getImportName())) {
            Date now = new Date();
            profile.setName("Profile imported on " + now.toString());
        } else {
            profile.setName(profileImportCommand.getImportName());
        }
        if (StringUtils.isBlank(profileImportCommand.getImportAgency())) {
            profile.setOwningAgency(AuthUtil.getRemoteUserObject().getAgency());
        } else {
            long agencyOid = Long.parseLong(profileImportCommand.getImportAgency());
            Agency agency = agencyUserManager.getAgencyByOid(agencyOid);
            profile.setOwningAgency(agency);
        }
        if (StringUtils.isBlank(profileImportCommand.getHarvesterType())) {
            profile.setHarvesterType(HarvesterType.DEFAULT.name());
        } else {
            profile.setHarvesterType(profileImportCommand.getHarvesterType());
        }

        // For now we only validate H3 profiles
        if (HarvesterType.HERITRIX3.name().equals(profile.getHarvesterType())) {
            HarvestAgent agent = getHarvestAgent();
            if (!agent.isValidProfile(profile.getProfile())) {
                Object[] vals = new Object[]{profile.getProfile()};
                log.info("Validation failed for H3 profile");
                bindingResult.reject("profile.invalid", vals, "The submitted profile is invalid.");
                ModelAndView mav = getView(profileImportCommand);
                mav.addObject(Constants.GBL_ERRORS, bindingResult);
                return mav;
            }
        }
        // Save to the database
        try {
            profileManager.saveOrUpdate(profile);
        } catch (HibernateOptimisticLockingFailureException e) {
            Object[] vals = new Object[]{profile.getName(), profile.getOwningAgency().getName()};
            bindingResult.reject("profile.modified", vals, "profile has been modified by another user.");
            ModelAndView mav = getView(profileImportCommand);
            mav.addObject(Constants.GBL_ERRORS, bindingResult);
            return mav;
        }
        return new ModelAndView("redirect:/curator/profiles/list");
    }


    @RequestMapping(path = "/curator/profiles/list.html", method = RequestMethod.GET)
    protected ModelAndView defaultList(@Valid @ModelAttribute("command") ProfileListCommand command,
                                       // Note that the BindingResult must come right after the object it validates
                                       // in the parameter list.
                                       BindingResult bindingResult, HttpSession session) {
        session.setAttribute(ProfileListController.SESSION_KEY_SHOW_INACTIVE, command.isShowInactive());
        String defaultAgency = (String) session.getAttribute(ProfileListController.SESSION_AGENCY_FILTER);
        if (defaultAgency == null) {
            defaultAgency = AuthUtil.getRemoteUserObject().getAgency().getName();
        }
        command.setDefaultAgency(defaultAgency);
        String harvesterType = (String) session.getAttribute(ProfileListController.SESSION_HARVESTER_TYPE_FILTER);
        if (harvesterType != null) {
            command.setHarvesterType(harvesterType);
        }

        ModelAndView mav = getView(command);
        mav.addObject(Constants.GBL_CMD_DATA, command);
        return mav;
    }

    @RequestMapping(path = "/curator/profiles/list/filter", method = RequestMethod.POST)
    protected ModelAndView filter(@Valid @ModelAttribute("command") ProfileListCommand command,
                                  // Note that the BindingResult must come right after the object it validates
                                  // in the parameter list.
                                  BindingResult bindingResult, HttpSession session) throws Exception {
        session.setAttribute(ProfileListController.SESSION_KEY_SHOW_INACTIVE, command.isShowInactive());
        session.setAttribute(ProfileListController.SESSION_AGENCY_FILTER, command.getDefaultAgency());
        session.setAttribute(ProfileListController.SESSION_HARVESTER_TYPE_FILTER, command.getHarvesterType());

        ModelAndView mav = getView(command);
        mav.addObject(Constants.GBL_CMD_DATA, command);
        return mav;
    }


    /**
     *
     * @return The first available H3 HarvestAgent instance that we can find.
     */
    private HarvestAgent getHarvestAgent() {
        // TODO Replace by autowiring
        return HarvestAgentUtil.getHarvestAgent(this.context);
    }

}
