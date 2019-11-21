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
package org.webcurator.ui.admin.controller;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.agency.AgencyUserManager;
import org.webcurator.core.common.WCTTreeSet;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.ui.admin.command.AgencyCommand;
import org.webcurator.common.ui.Constants;

/**
 * Manages the Agency Administration view and the actions associated with a Agency
 * @author bprice
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class AgencyController {
	/** the logger. */
    private Log log = null;
    /** the user manager. */
    @Autowired
    private AgencyUserManager agencyUserManager;
    /** the message source. */
    @Autowired
    private MessageSource messageSource;
    /** Default Constructor. */

    @Autowired
	private WCTTreeSet dublinCoreTypesList;

    public AgencyController() {
        log = LogFactory.getLog(AgencyController.class);
    }

    @InitBinder
    public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        NumberFormat nf = NumberFormat.getInstance(request.getLocale());
        binder.registerCustomEditor(java.lang.Long.class, new CustomNumberEditor(java.lang.Long.class, nf, true));
    }

    @RequestMapping(method = RequestMethod.GET, path = "/curator/admin/agency.html")
    protected ModelAndView showForm() throws Exception {

        return populateAgencyList();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/curator/admin/agency.html")
    protected ModelAndView processFormSubmission(@ModelAttribute AgencyCommand agencyCommand, BindingResult bindingResult) throws Exception {

        ModelAndView mav = null;
        if (agencyCommand != null) {
            if (bindingResult.hasErrors()) {
                mav = new ModelAndView();
                mav = populateAgencyList();
                mav.addObject(Constants.GBL_CMD_DATA, bindingResult.getTarget());
                mav.addObject(Constants.GBL_ERRORS, bindingResult);
                mav.setViewName("newAgency");

            } else if (AgencyCommand.ACTION_NEW.equals(agencyCommand.getActionCommand())) {
                //Display the Create Agency screen

                mav = populateAgencyList();
                mav.setViewName("newAgency");
            } else if (AgencyCommand.ACTION_SAVE.equals(agencyCommand.getActionCommand())) {
                //Save the new Agency details
                boolean update = false;
                Agency agency = new Agency();

                if (agencyCommand.getOid() != null) {
                    update = true;
                    agency.setOid(agencyCommand.getOid());
                }

                agency.setName(agencyCommand.getName());
                agency.setAddress(agencyCommand.getAddress());
                agency.setPhone(agencyCommand.getPhone());
                agency.setFax(agencyCommand.getFax());
                agency.setEmail(agencyCommand.getEmail());
                agency.setAgencyURL(agencyCommand.getAgencyURL());
                agency.setAgencyLogoURL(agencyCommand.getAgencyLogoURL());
                agency.setShowTasks(agencyCommand.getShowTasks());
                agency.setDefaultDescriptionType(agencyCommand.getDescriptionType());

                agencyUserManager.updateAgency(agency, update);
                mav = populateAgencyList();
                if (update == true) {
                    mav.addObject(Constants.GBL_MESSAGES, messageSource.getMessage("agency.updated", new Object[] { agencyCommand.getName() }, Locale.getDefault()));
                } else {
                    mav.addObject(Constants.GBL_MESSAGES, messageSource.getMessage("agency.created", new Object[] { agencyCommand.getName() }, Locale.getDefault()));
                }
            } else if (AgencyCommand.ACTION_VIEW.equals(agencyCommand.getActionCommand()) ||
            		AgencyCommand.ACTION_EDIT.equals(agencyCommand.getActionCommand())) {

                Agency agency = agencyUserManager.getAgencyByOid(agencyCommand.getOid());
                AgencyCommand populatedCmd = new AgencyCommand();
                populatedCmd.setOid(agency.getOid());
                populatedCmd.setName(agency.getName());
                populatedCmd.setAddress(agency.getAddress());
                populatedCmd.setPhone(agency.getPhone());
                populatedCmd.setFax(agency.getFax());
                populatedCmd.setEmail(agency.getEmail());
                populatedCmd.setAgencyURL(agency.getAgencyURL());
                populatedCmd.setAgencyLogoURL(agency.getAgencyLogoURL());
                populatedCmd.setShowTasks(agency.getShowTasks());
                populatedCmd.setViewOnlyMode(AgencyCommand.ACTION_VIEW.equals(agencyCommand.getActionCommand()));
                populatedCmd.setDescriptionType(agency.getDefaultDescriptionType());

                mav = new ModelAndView();
                mav = populateAgencyList();
                mav.addObject(Constants.GBL_CMD_DATA, populatedCmd);
                mav.setViewName("newAgency");
            }
        } else {
            log.warn("No Action provided for AgencyController.");
            mav = populateAgencyList();
        }

		mav.addObject("descriptionTypes", dublinCoreTypesList);
        return mav;
    }

    /**
     * @return a model and view populated with the list of agencies.
     */
    private ModelAndView populateAgencyList() {
        List agencyList = agencyUserManager.getAgenciesForLoggedInUser();

        ModelAndView mav = new ModelAndView();
        mav.addObject(AgencyCommand.MDL_AGENCIES, agencyList);
        mav.setViewName("viewAgencies");
        return mav;
    }

    /**
     * @param agencyUserManager the agency manager
     */
    public void setAgencyUserManager(AgencyUserManager agencyUserManager) {
        this.agencyUserManager = agencyUserManager;
    }

    /**
     * @param messageSource the message source to use
     */
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

	public void setDublinCoreTypesList(WCTTreeSet dublinCoreTypesList) {
		this.dublinCoreTypesList = dublinCoreTypesList;
	}


}

