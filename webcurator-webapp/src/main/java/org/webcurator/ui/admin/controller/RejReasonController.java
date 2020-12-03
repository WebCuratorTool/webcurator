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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
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
import org.webcurator.ui.admin.command.RejReasonCommand;
import org.webcurator.common.ui.Constants;

/**
 * Manages the Rejection Reason Administration view and the actions associated with a Rejection Reason
 * @author oakleigh_sk
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class RejReasonController {
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
    /** Default Constructor. */
    public RejReasonController() {
        log = LogFactory.getLog(RejReasonController.class);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/curator/admin/rejreason.html")
    protected ModelAndView showForm(HttpServletRequest aReq) throws Exception {
        ModelAndView mav = new ModelAndView();
        String agencyFilter = (String)aReq.getSession().getAttribute(RejReasonCommand.MDL_AGENCYFILTER);
        if(agencyFilter == null)
        {
        	agencyFilter = AuthUtil.getRemoteUserObject().getAgency().getName();
        }
        mav.addObject(RejReasonCommand.MDL_AGENCYFILTER, agencyFilter);
        populateRejReasonList(mav);
        return mav;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/curator/admin/rejreason.html")
    protected ModelAndView processFormSubmission(HttpServletRequest aReq, @ModelAttribute RejReasonCommand rejReasonCommand, BindingResult bindingResult)
            throws Exception {
        ModelAndView mav = new ModelAndView();

        if (rejReasonCommand != null) {

        	if (RejReasonCommand.ACTION_DELETE.equals(rejReasonCommand.getCmd())) {
                // Attempt to delete a rejection reason from the system
                Long reasonOid = rejReasonCommand.getOid();
                RejReason reason = agencyUserManager.getRejReasonByOid(reasonOid);
                String name = reason.getName();
                try {
                    agencyUserManager.deleteRejReason(reason);
                } catch (DataAccessException e) {
                    String[] codes = {"rejreason.delete.fail"};
                    Object[] args = new Object[1];
                    args[0] = reason.getName();
                    if (bindingResult == null) {
                        bindingResult = new BindException(rejReasonCommand, "command");
                    }
                    bindingResult.addError(new ObjectError("command",codes,args,"Rejection Reason owns objects in the system and can't be deleted."));
                    mav.addObject(Constants.GBL_ERRORS, bindingResult);
                    populateRejReasonList(mav);
                    return mav;
                }
                populateRejReasonList(mav);
                mav.addObject(Constants.GBL_MESSAGES, messageSource.getMessage("rejreason.deleted", new Object[] { name }, Locale.getDefault()));
            } else if (RejReasonCommand.ACTION_FILTER.equals(rejReasonCommand.getCmd())) {
                //Just filtering reasons by agency - if we change the default, store it in the session
            	aReq.getSession().setAttribute(RejReasonCommand.MDL_AGENCYFILTER, rejReasonCommand.getAgencyFilter());
            	populateRejReasonList(mav);
            }
        } else {
            log.warn("No Action provided for RejReasonController.");
            populateRejReasonList(mav);
        }

        String agencyFilter = (String)aReq.getSession().getAttribute(RejReasonCommand.MDL_AGENCYFILTER);
        if(agencyFilter == null)
        {
        	agencyFilter = AuthUtil.getRemoteUserObject().getAgency().getName();
        }
        mav.addObject(RejReasonCommand.MDL_AGENCYFILTER, agencyFilter);
        return mav;
    }

    /**
     * Populate the rejection reason list model object in the model and view provided.
     * @param mav the model and view to add the user list to.
     */
    private void populateRejReasonList(ModelAndView mav) {
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

        mav.addObject(RejReasonCommand.MDL_REASONS, reasons);
        mav.addObject(RejReasonCommand.MDL_LOGGED_IN_USER, AuthUtil.getRemoteUserObject());
        mav.addObject(RejReasonCommand.MDL_AGENCIES, agencies);
        mav.setViewName("viewReasons");
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
