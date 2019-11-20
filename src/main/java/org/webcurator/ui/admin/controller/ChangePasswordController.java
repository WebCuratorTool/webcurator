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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.agency.AgencyUserManager;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.util.AuthUtil;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.domain.model.auth.Privilege;
import org.webcurator.domain.model.auth.User;
import org.webcurator.ui.admin.command.ChangePasswordCommand;
import org.webcurator.ui.admin.command.UserCommand;
import org.webcurator.common.ui.Constants;

/**
 * Manage the view for changing a users password.
 * @author bprice
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class ChangePasswordController {
	/** the agency user manager. */
	@Autowired
    private AgencyUserManager agencyUserManager;
    /** the authority manager */
    @Autowired
    private AuthorityManager authorityManager;
    /** the password encoder. */
    @Autowired
    @Qualifier("passwordEncoder")
    private PasswordEncoder encoder;
    /** the system wide encoding salt. */
    /** the message source. */
    @Autowired
    private MessageSource messageSource;

    /** Default Constructor. */
    public ChangePasswordController() {
    }

    public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        NumberFormat nf = NumberFormat.getInstance(request.getLocale());
        binder.registerCustomEditor(java.lang.Long.class, new CustomNumberEditor(java.lang.Long.class, nf, true));
    }

    protected ModelAndView showForm() throws Exception {

        return null;
    }

    protected ModelAndView processFormSubmission(HttpServletRequest aReq, Object aCmd, BindingResult bindingResult)
            throws Exception {
        ChangePasswordCommand pwdCmd = (ChangePasswordCommand) aCmd;
        if (ChangePasswordCommand.ACTION_SAVE.equals(pwdCmd.getAction())) {
            //Save the Change of password action
            ChangePasswordCommand aPwdCommand = (ChangePasswordCommand) aCmd;
            return processPasswordChange(aReq, aPwdCommand, bindingResult);
        } else {
            //Display the change password form
            return createDefaultModelAndView(pwdCmd);
        }
    }

    /**
     * Process the command tp change the users password.
     */
    private ModelAndView processPasswordChange(HttpServletRequest aReq, ChangePasswordCommand aCmd,
                                               BindingResult bindingResult) throws Exception {
        ModelAndView mav = new ModelAndView();
        if (bindingResult.hasErrors()) {
            mav.addObject(Constants.GBL_CMD_DATA, bindingResult.getTarget());
            mav.addObject(Constants.GBL_ERRORS, bindingResult);
            mav.setViewName("change-password");

            return mav;
        }

        try {

            User userAccount = agencyUserManager.getUserByOid(aCmd.getUserOid());

            String encodedPwd = encoder.encode(aCmd.getNewPwd());

            userAccount.setPassword(encodedPwd);

            agencyUserManager.updateUser(userAccount, true);

            mav.addObject(Constants.GBL_MESSAGES, messageSource.getMessage("user.password.change", new Object[] { userAccount.getUsername() }, Locale.getDefault()));
            List userDTOs = agencyUserManager.getUserDTOsForLoggedInUser();
            List agencies = null;
            if (authorityManager.hasPrivilege(Privilege.MANAGE_USERS, Privilege.SCOPE_ALL)) {
            	agencies = agencyUserManager.getAgencies();
            } else {
                User loggedInUser = AuthUtil.getRemoteUserObject();
                Agency usersAgency = loggedInUser.getAgency();
                agencies = new ArrayList<Agency>();
                agencies.add(usersAgency);
            }

            String agencyFilter = (String)aReq.getSession().getAttribute(UserCommand.MDL_AGENCYFILTER);
            if(agencyFilter == null)
            {
            	agencyFilter = AuthUtil.getRemoteUserObject().getAgency().getName();
            }
            mav.addObject(UserCommand.MDL_AGENCYFILTER, agencyFilter);
            mav.addObject(UserCommand.MDL_LOGGED_IN_USER, AuthUtil.getRemoteUserObject());
            mav.addObject(UserCommand.MDL_USERS, userDTOs);
            mav.addObject(UserCommand.MDL_AGENCIES, agencies);
            mav.setViewName("viewUsers");

            return mav;
        }
        catch (Exception e) {
            throw new Exception("Persistance Error occurred during password change", e);
        }
    }

    /**
     * Generate a default model and view.
     */
    private ModelAndView createDefaultModelAndView(ChangePasswordCommand pwdCmd) {
        ModelAndView mav = new ModelAndView();
        mav.addObject(Constants.GBL_CMD_DATA, pwdCmd);
        mav.setViewName("change-password");

        return mav;
    }

    /**
     * @param encoder set the password encoder.
     */
    public void setEncoder(PasswordEncoder encoder) {
        this.encoder = encoder;
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
