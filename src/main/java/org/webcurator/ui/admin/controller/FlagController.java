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
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.agency.AgencyUserManager;
import org.webcurator.core.util.AuthUtil;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.domain.model.auth.Privilege;
import org.webcurator.domain.model.auth.User;
import org.webcurator.domain.model.core.Flag;
import org.webcurator.ui.admin.command.FlagCommand;
import org.webcurator.common.ui.Constants;
/**
 * Manages the Flags Administration view and the actions associated with a Flag group definition
 * @author twoods
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class FlagController {
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
    public FlagController() {
        log = LogFactory.getLog(FlagController.class);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/curator/admin/flags.html")
    protected ModelAndView showForm(HttpServletRequest aReq) throws Exception {
        ModelAndView mav = new ModelAndView();
        String agencyFilter = (String)aReq.getSession().getAttribute(FlagCommand.MDL_AGENCYFILTER);
        if(agencyFilter == null)
        {
        	agencyFilter = AuthUtil.getRemoteUserObject().getAgency().getName();
        }
        mav.addObject(FlagCommand.MDL_AGENCYFILTER, agencyFilter);
        populateFlagList(mav);
        return mav;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/curator/admin/flags.html")
    protected ModelAndView processFormSubmission(HttpServletRequest aReq, @ModelAttribute FlagCommand flagCommand, BindingResult bindingResult)
            throws Exception {

        ModelAndView mav = new ModelAndView();
        if (flagCommand != null) {

        	if (FlagCommand.ACTION_DELETE.equals(flagCommand.getCmd())) {
                // Attempt to delete a indicator criteria from the system
                Long oid = flagCommand.getOid();
                Flag flag = agencyUserManager.getFlagByOid(oid);
                String name = flag.getName();
                try {
                    agencyUserManager.deleteFlag(flag);
                } catch (DataAccessException e) {
                    String[] codes = {"flag.delete.fail"};
                    Object[] args = new Object[1];
                    args[0] = flag.getName();
                    if (bindingResult == null) {
                        bindingResult = new BindException(flagCommand, "command");
                    }
                    bindingResult.addError(new ObjectError("command",codes,args,"Flag owns objects in the system and can't be deleted."));
                    mav.addObject(Constants.GBL_ERRORS, bindingResult);
                    populateFlagList(mav);
                    return mav;
                }
                populateFlagList(mav);
                mav.addObject(Constants.GBL_MESSAGES, messageSource.getMessage("flag.deleted", new Object[] { name }, Locale.getDefault()));
            } else if (FlagCommand.ACTION_FILTER.equals(flagCommand.getCmd())) {
                //Just filtering reasons by agency - if we change the default, store it in the session
            	aReq.getSession().setAttribute(FlagCommand.MDL_AGENCYFILTER, flagCommand.getAgencyFilter());
            	populateFlagList(mav);
            }
        } else {
            log.warn("No Action provided for FlagController.");
            populateFlagList(mav);
        }

        String agencyFilter = (String)aReq.getSession().getAttribute(FlagCommand.MDL_AGENCYFILTER);
        if(agencyFilter == null)
        {
        	agencyFilter = AuthUtil.getRemoteUserObject().getAgency().getName();
        }
        mav.addObject(FlagCommand.MDL_AGENCYFILTER, agencyFilter);
        return mav;
    }

    /**
     * Populate the Indicator Criteria list model object in the model and view provided.
     * @param mav the model and view to add the user list to.
     */
    private void populateFlagList(ModelAndView mav) {
        List<Flag> flags = agencyUserManager.getFlagForLoggedInUser();
        List<Agency> agencies = null;
        if (authorityManager.hasPrivilege(Privilege.MANAGE_FLAGS, Privilege.SCOPE_ALL)) {
        	agencies = agencyUserManager.getAgencies();
        } else {
            User loggedInUser = AuthUtil.getRemoteUserObject();
            Agency usersAgency = loggedInUser.getAgency();
            agencies = new ArrayList<Agency>();
            agencies.add(usersAgency);
        }

        mav.addObject(FlagCommand.MDL_FLAGS, flags);
        mav.addObject(FlagCommand.MDL_LOGGED_IN_USER, AuthUtil.getRemoteUserObject());
        mav.addObject(FlagCommand.MDL_AGENCIES, agencies);
        mav.setViewName("viewFlags");
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
