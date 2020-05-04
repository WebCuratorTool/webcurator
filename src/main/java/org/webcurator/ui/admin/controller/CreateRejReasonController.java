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
import java.util.ArrayList;
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
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.agency.AgencyUserManager;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.util.AuthUtil;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.domain.model.auth.Privilege;
import org.webcurator.domain.model.auth.User;
import org.webcurator.domain.model.core.RejReason;
import org.webcurator.ui.admin.command.CreateRejReasonCommand;
import org.webcurator.ui.admin.command.RejReasonCommand;
import org.webcurator.common.ui.Constants;
import org.webcurator.ui.admin.validator.CreateRejReasonValidator;

/**
 * Manages the creation flow for a Rejection Reason within WCT
 * @author oakleigh_sk
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class CreateRejReasonController {
	/** the logger. */
    private Log log = null;
    /** the agency user manager. */
    @Autowired
    private AgencyUserManager agencyUserManager;
    /** the authority manager. */
    @Autowired
    private AuthorityManager authorityManager;
    /** the message source. */
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private CreateRejReasonValidator createRejReasonValidator;
    /** Default Constructor. */
    public CreateRejReasonController() {
        log = LogFactory.getLog(CreateRejReasonController.class);
    }

    @InitBinder
    public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        NumberFormat nf = NumberFormat.getInstance(request.getLocale());
        binder.registerCustomEditor(java.lang.Long.class, new CustomNumberEditor(java.lang.Long.class, nf, true));
    }

    protected ModelAndView showForm() throws Exception {

        return null;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/curator/admin/create-rejreason.html")
    protected ModelAndView processFormSubmission(HttpServletRequest aReq, @ModelAttribute CreateRejReasonCommand createRejReasonCommand, BindingResult bindingResult)
            throws Exception {
        createRejReasonValidator.validate(createRejReasonCommand, bindingResult);
        ModelAndView mav = null;

        if (createRejReasonCommand != null) {
            if (bindingResult.hasErrors()) {
                mav = new ModelAndView();
                List agencies = agencyUserManager.getAgenciesForLoggedInUser();
                mav.addObject(CreateRejReasonCommand.MDL_AGENCIES, agencies);
                String mode = createRejReasonCommand.getMode();
                if (CreateRejReasonCommand.ACTION_EDIT.equals(mode)) {
                    mav.addObject(CreateRejReasonCommand.ACTION_EDIT, mode);
                }
                mav.addObject(Constants.GBL_CMD_DATA, bindingResult.getTarget());
                mav.addObject(Constants.GBL_ERRORS, bindingResult);
                mav.setViewName("newReason");

            } else if (CreateRejReasonCommand.ACTION_NEW.equals(createRejReasonCommand.getAction())) {
                mav = new ModelAndView();
                List agencies = agencyUserManager.getAgenciesForLoggedInUser();
                mav.addObject(CreateRejReasonCommand.MDL_AGENCIES, agencies);
                mav.setViewName("newReason");

            } else if (CreateRejReasonCommand.ACTION_VIEW.equals(createRejReasonCommand.getAction()) ||
            		CreateRejReasonCommand.ACTION_EDIT.equals(createRejReasonCommand.getAction())) {
                //View/Edit an existing reason
                mav = new ModelAndView();
                Long reasonOid = createRejReasonCommand.getOid();
                RejReason reason = agencyUserManager.getRejReasonByOid(reasonOid);
                CreateRejReasonCommand editCmd = new CreateRejReasonCommand();
                editCmd.setOid(reasonOid);
                editCmd.setAgencyOid(reason.getAgency().getOid());
                editCmd.setName(reason.getName());
                editCmd.setAvailableForTargets(reason.isAvailableForTargets());
                editCmd.setAvailableForTIs(reason.isAvailableForTIs());
                editCmd.setMode(createRejReasonCommand.getAction());

                List agencies = agencyUserManager.getAgenciesForLoggedInUser();
                mav.addObject(CreateRejReasonCommand.MDL_AGENCIES, agencies);
                mav.addObject(Constants.GBL_CMD_DATA, editCmd);
                mav.setViewName("newReason");

            } else if (CreateRejReasonCommand.ACTION_SAVE.equals(createRejReasonCommand.getAction())) {


                    try {
                        RejReason reason = new RejReason();
                        boolean update = (createRejReasonCommand.getOid() != null);
                        if (update == true) {
                            // Update an existing reason object by loading it in first
                        	reason = agencyUserManager.getRejReasonByOid(createRejReasonCommand.getOid());
                        } else {
                        	// Save the newly created reason object

                            //load Agency
                            Long agencyOid = createRejReasonCommand.getAgencyOid();
                            Agency agency = agencyUserManager.getAgencyByOid(agencyOid);
                            reason.setAgency(agency);
                        }

                        reason.setAvailableForTargets(createRejReasonCommand.isAvailableForTargets());
                        reason.setAvailableForTIs(createRejReasonCommand.isAvailableForTIs());
                        reason.setName(createRejReasonCommand.getName());

                        agencyUserManager.updateRejReason(reason, update);

                        List reasons = agencyUserManager.getRejReasonsForLoggedInUser();
                        List agencies = null;
                        if (authorityManager.hasPrivilege(Privilege.MANAGE_REASONS, Privilege.SCOPE_ALL)) {
                        	agencies = agencyUserManager.getAgencies();
                        } else {
                            User loggedInUser = AuthUtil.getRemoteUserObject();
                            Agency usersAgency = loggedInUser.getAgency();
                            agencies = new ArrayList<Agency>();
                            agencies.add(usersAgency);
                        }

                        mav = new ModelAndView();
                        String message;
                        if (update == true) {
                            message = messageSource.getMessage("reason.updated", new Object[] { createRejReasonCommand.getName() }, Locale.getDefault());
                        } else {
                            message = messageSource.getMessage("reason.created", new Object[] { createRejReasonCommand.getName() }, Locale.getDefault());
                        }
                        String agencyFilter = (String)aReq.getSession().getAttribute(RejReasonCommand.MDL_AGENCYFILTER);
                        if(agencyFilter == null)
                        {
                        	agencyFilter = AuthUtil.getRemoteUserObject().getAgency().getName();
                        }
                        mav.addObject(RejReasonCommand.MDL_AGENCYFILTER, agencyFilter);
                        mav.addObject(RejReasonCommand.MDL_LOGGED_IN_USER, AuthUtil.getRemoteUserObject());
                        mav.addObject(RejReasonCommand.MDL_REASONS, reasons);
                        mav.addObject(RejReasonCommand.MDL_AGENCIES, agencies);
                        mav.addObject(Constants.GBL_MESSAGES, message );

                        mav.setViewName("viewReasons");
                    }
                    catch (DataAccessException e) {
                        mav = new ModelAndView();
                        List agencies = agencyUserManager.getAgenciesForLoggedInUser();
                        mav.addObject(CreateRejReasonCommand.MDL_AGENCIES, agencies);
                        String mode = createRejReasonCommand.getMode();
                        if (CreateRejReasonCommand.ACTION_EDIT.equals(mode)) {
                            mav.addObject(CreateRejReasonCommand.ACTION_EDIT, mode);
                        }
                        bindingResult.reject("user.duplicate");
                        mav.addObject(Constants.GBL_CMD_DATA, bindingResult.getTarget());
                        mav.addObject(Constants.GBL_ERRORS, bindingResult);
                        mav.setViewName("newReason");
                    }

            }
        } else {
            log.warn("No Action provided for CreateRejReasonController.");
        }

        return mav;
    }

    /**
     * @param agencyUserManager the agency user manager.
     */
    public void setAgencyUserManager(AgencyUserManager agencyUserManager) {
        this.agencyUserManager = agencyUserManager;
    }

    /**
     * @param messageSource the message source.
     */
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
	 * Spring setter method for the Authority Manager.
	 * @param authorityManager The authorityManager to set.
	 */
	public void setAuthorityManager(AuthorityManager authorityManager) {
		this.authorityManager = authorityManager;
	}
}
