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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
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
import org.webcurator.ui.admin.command.CreateUserCommand;
import org.webcurator.ui.admin.command.UserCommand;
import org.webcurator.common.ui.Constants;

/**
 * Manages the creation flow for a User within WCT
 * @author bprice
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class CreateUserController {
	/** the logger. */
    private Log log = null;
    /** the password encoder. */
    @Autowired
    private PasswordEncoder passwordEncoder;
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
    public CreateUserController() {
        log = LogFactory.getLog(CreateUserController.class);
    }

    @InitBinder
    public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        NumberFormat nf = NumberFormat.getInstance(request.getLocale());
        binder.registerCustomEditor(java.lang.Long.class, new CustomNumberEditor(java.lang.Long.class, nf, true));
    }

    protected ModelAndView showForm() throws Exception {

        return null;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/curator/admin/create-user.html")
    protected ModelAndView processFormSubmission(HttpServletRequest aReq, @ModelAttribute CreateUserCommand createUserCommand, BindingResult bindingResult)
            throws Exception {

        ModelAndView mav = new ModelAndView();
        mav.addObject(UserCommand.MDL_LOGGED_IN_USER, AuthUtil.getRemoteUserObject());
        if (createUserCommand != null) {
            if (bindingResult.hasErrors()) {
                List agencies = agencyUserManager.getAgenciesForLoggedInUser();

                mav.addObject(CreateUserCommand.MDL_AGENCIES, agencies);
                String mode = createUserCommand.getMode();
                if (CreateUserCommand.ACTION_EDIT.equals(mode)) {
                    mav.addObject(CreateUserCommand.ACTION_EDIT, mode);
                }
                mav.addObject(Constants.GBL_CMD_DATA, bindingResult.getTarget());
                mav.addObject(Constants.GBL_ERRORS, bindingResult);
                mav.setViewName("newUser");

            } else if (CreateUserCommand.ACTION_NEW.equals(createUserCommand.getAction())) {
                List agencies = agencyUserManager.getAgenciesForLoggedInUser();
                mav.addObject(CreateUserCommand.MDL_AGENCIES, agencies);
                mav.setViewName("newUser");

            } else if (CreateUserCommand.ACTION_VIEW.equals(createUserCommand.getAction()) ||
            		CreateUserCommand.ACTION_EDIT.equals(createUserCommand.getAction())) {
                //View/Edit an existing user
                Long userOid = createUserCommand.getOid();
                User user = agencyUserManager.getUserByOid(userOid);
                CreateUserCommand editCmd = new CreateUserCommand();
                editCmd.setOid(userOid);
                editCmd.setActive(user.isActive());
                editCmd.setAddress(user.getAddress());
                editCmd.setAgencyOid(user.getAgency().getOid());
                editCmd.setUsername(user.getUsername());
                editCmd.setFirstname(user.getFirstname());
                editCmd.setLastname(user.getLastname());
                editCmd.setTitle(user.getTitle());
                editCmd.setPhone(user.getPhone());
                editCmd.setEmail(user.getEmail());
                editCmd.setNotificationsByEmail(user.isNotificationsByEmail());
                editCmd.setTasksByEmail(user.isTasksByEmail());
                editCmd.setExternalAuth(user.isExternalAuth());
                editCmd.setNotifyOnGeneral(user.isNotifyOnGeneral());
                editCmd.setNotifyOnHarvestWarnings(user.isNotifyOnHarvestWarnings());
                editCmd.setMode(createUserCommand.getAction());

                List agencies = agencyUserManager.getAgenciesForLoggedInUser();
                mav.addObject(CreateUserCommand.MDL_AGENCIES, agencies);
                List assignedRoles = agencyUserManager.getAssociatedRolesForUser(createUserCommand.getOid());
                mav.addObject(CreateUserCommand.MDL_ASSIGNED_ROLES, assignedRoles);
                mav.addObject(Constants.GBL_CMD_DATA, editCmd);
                mav.setViewName("newUser");

            } else if (CreateUserCommand.ACTION_SAVE.equals(createUserCommand.getAction())) {


                    try {
                        User user = new User();
                        boolean update = (createUserCommand.getOid() != null);
                        if (update == true) {
                            // Update an existing user object by loading it in first
                            user = agencyUserManager.getUserByOid(createUserCommand.getOid());
                        } else {
   //                  Save the newly created User object
                            user.setActive(true);

                            //load Agency
                            Long agencyOid = createUserCommand.getAgencyOid();
                            Agency agency = agencyUserManager.getAgencyByOid(agencyOid);
                            user.setAgency(agency);

                            user.setExternalAuth(createUserCommand.isExternalAuth());

   //                  Only set the password for WCT Authenticating users
                            if (createUserCommand.isExternalAuth() == false) {
                                String pwd = createUserCommand.getPassword();
                                String encodedPwd = passwordEncoder.encode(pwd);
                                user.setPassword(encodedPwd);
   //                      force a password change only for WCT users, not LDAP users
                                user.setForcePasswordChange(true);
                            }

                            user.setRoles(null);
                        }

                        user.setAddress(createUserCommand.getAddress());
                        user.setEmail(createUserCommand.getEmail());
                        user.setFirstname(createUserCommand.getFirstname());
                        user.setLastname(createUserCommand.getLastname());
                        user.setNotificationsByEmail(createUserCommand.isNotificationsByEmail());
                        user.setTasksByEmail(createUserCommand.isTasksByEmail());
                        user.setPhone(createUserCommand.getPhone());
                        user.setTitle(createUserCommand.getTitle());
                        user.setUsername(createUserCommand.getUsername());
                        user.setNotifyOnGeneral(createUserCommand.isNotifyOnGeneral());
                        user.setNotifyOnHarvestWarnings(createUserCommand.isNotifyOnHarvestWarnings());

                        agencyUserManager.updateUser(user, update);

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

                        String message;
                        if (update == true) {
                            message = messageSource.getMessage("user.updated", new Object[] { createUserCommand.getUsername() }, Locale.getDefault());
                        } else {
                            message = messageSource.getMessage("user.created", new Object[] { createUserCommand.getUsername() }, Locale.getDefault());
                        }
                        String agencyFilter = (String)aReq.getSession().getAttribute(UserCommand.MDL_AGENCYFILTER);
                        if(agencyFilter == null)
                        {
                        	agencyFilter = AuthUtil.getRemoteUserObject().getAgency().getName();
                        }
                        mav.addObject(UserCommand.MDL_AGENCYFILTER, agencyFilter);
                        mav.addObject(UserCommand.MDL_USERS, userDTOs);
                        mav.addObject(UserCommand.MDL_AGENCIES, agencies);
                        mav.addObject(Constants.GBL_MESSAGES, message );

                        mav.setViewName("viewUsers");
                    }
                    catch (DataAccessException e) {
                        List agencies = agencyUserManager.getAgenciesForLoggedInUser();
                        mav.addObject(CreateUserCommand.MDL_AGENCIES, agencies);
                        String mode = createUserCommand.getMode();
                        if (CreateUserCommand.ACTION_EDIT.equals(mode)) {
                            mav.addObject(CreateUserCommand.ACTION_EDIT, mode);
                        }
                        bindingResult.reject("user.duplicate");
                        mav.addObject(Constants.GBL_CMD_DATA, bindingResult.getTarget());
                        mav.addObject(Constants.GBL_ERRORS, bindingResult);
                        mav.setViewName("newUser");
                    }

            }
        } else {
            log.warn("No Action provided for CreateUserController.");
            return null;
        }

        return mav;
    }

    /**
     * @param passwordEncoder the password encoder.
     */
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
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
