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
package org.webcurator.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.webcurator.common.ui.Constants;
import org.webcurator.core.report.LogonDurationDAO;
import org.webcurator.core.util.Auditor;
import org.webcurator.core.util.CookieUtils;
import org.webcurator.domain.UserRoleDAO;
import org.webcurator.domain.model.auth.User;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;


/**
 * The handler for successful authentication requests.
 *
 */
@Component
public class WCTAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private static Log log = LogFactory.getLog(WCTAuthenticationSuccessHandler.class);
    private Auditor auditor = null;
    private UserRoleDAO authDAO = null;
    private LogonDurationDAO logonDurationDAO = null;

    public WCTAuthenticationSuccessHandler(String defaultTargetUrl, Boolean alwaysUseDefaultTargetUrl) {
        super();
        setAlwaysUseDefaultTargetUrl(alwaysUseDefaultTargetUrl);
        setDefaultTargetUrl(defaultTargetUrl);
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth) throws IOException, ServletException {

        log.debug("calling onAuthenticationSuccess for WCT");
        String userName = auth.getName();

        User wctUser = authDAO.getUserByName(userName);

        if (wctUser != null) {
            log.debug("loaded WCT User object "+wctUser.getUsername()+" from database");
            ((UsernamePasswordAuthenticationToken)auth).setDetails(wctUser);
            log.debug("pushing back upat into SecurityContext with populated WCT User");

            //  audit successful login event
            auditor.audit(User.class.getName(), wctUser.getOid(), Auditor.ACTION_LOGIN_SUCCESS, "Successful Login for username: "+wctUser.getUsername());

            // set or re-set the page size cookie..
            // ..first get the value of the page size cookie
            String currentPageSize = CookieUtils.getPageSize(request);
            // ..then refresh the page size cookie, to expire in a year
            CookieUtils.setPageSize(response, currentPageSize);

            // set login for duration
            String sessionId = request.getSession().getId();
            logonDurationDAO.setLoggedIn(sessionId, new Date(), wctUser.getOid(), wctUser.getUsername(), wctUser.getNiceName());
            // Check previous records of duration
            logonDurationDAO.setProperLoggedoutForCurrentUser(wctUser.getOid(), sessionId);

            // check to see if the user should change their password
            if (wctUser.isForcePasswordChange() && !wctUser.isExternalAuth()) {
                response.sendRedirect("/" + Constants.CNTRL_RESET_PWD);
                auditor.audit(User.class.getName(),wctUser.getOid(),Auditor.ACTION_FORCE_PWD_CHANGE,"User has been forced to change password");
            } else {
                // continue to redirect destination
                super.onAuthenticationSuccess(request, response, auth);
            }

        }  else {

            //audit successful login but unsucessful load of WCT User event
            auditor.audit(User.class.getName(), Auditor.ACTION_LOGIN_FAILURE_NO_USER, "Un-successful login for username: "+userName+" as user doesn't exist in the WCT System.");

        }
    }
    
    /**
     * Spring setter.
     * @param authDAO set the authentication dao bean.
     */
    public void setAuthDAO(UserRoleDAO authDAO) {
        this.authDAO = authDAO;
    }
    
    /** 
     * Spring setter 
     * @param auditor set the auditor bean
     */
    public void setAuditor(Auditor auditor) {
        this.auditor = auditor;
    }

    /**
     * Spring setter.
     * @param logonDurationDAO set the logon duration dao bean.
     */
    public void setLogonDurationDAO(LogonDurationDAO logonDurationDAO) {
        this.logonDurationDAO = logonDurationDAO;
    }
}
