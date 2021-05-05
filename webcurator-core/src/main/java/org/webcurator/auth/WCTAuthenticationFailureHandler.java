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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.ForwardAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.webcurator.core.util.Auditor;
import org.webcurator.domain.model.auth.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * The handler for failed authentication requests.
 */
@Component
public class WCTAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private static Log log = LogFactory.getLog(WCTAuthenticationFailureHandler.class);
    private Auditor auditor = null;

    public WCTAuthenticationFailureHandler(String defaultFailureUrl) {
        super(defaultFailureUrl);
    }


    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        log.debug("calling onAuthenticationFailure for WCT");

        String username = request.getParameter("username");

        //audit failed login event
        auditor.audit(User.class.getName(), Auditor.ACTION_LOGIN_FAILURE, "Failed Login for username: " + username);

        log.info("Login failure for " + username + ": " + exception.getMessage());
        // continue to redirect destination
        //TODO investigate why redirection to /logon.jsp?failed=true isn't completing

        String mode = request.getHeader("Request-Mode");
        if (mode != null && mode.equalsIgnoreCase("embed")) {
            response.addHeader("Auth-Result", "failed");
        }

        // continue to redirect destination
        super.onAuthenticationFailure(request, response, exception);
    }

    /**
     * Spring setter
     *
     * @param auditor set the auditor bean
     */
    public void setAuditor(Auditor auditor) {
        this.auditor = auditor;
    }

}
