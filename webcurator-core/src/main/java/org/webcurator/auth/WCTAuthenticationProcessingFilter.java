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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.context.WebApplicationContext;
import org.webcurator.core.common.Constants;
import org.webcurator.core.report.LogonDurationDAO;
import org.webcurator.core.util.Auditor;
import org.webcurator.core.util.CookieUtils;
import org.webcurator.domain.UserRoleDAO;
import org.webcurator.domain.model.auth.User;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;


/**
 * The hook for allowing the result of authentication requests to be audited.
 * @author bprice
 */
public class WCTAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {
    public static final String LAST_USERNAME_KEY = "last_username";
    public static final String FORM_USERNAME_KEY = "j_username";
    public static final String FORM_PASSWORD_KEY = "j_password";
    private static Log log = LogFactory.getLog(WCTAuthenticationProcessingFilter.class);
    private Auditor auditor = null;
    private UserRoleDAO authDAO = null;

    @Autowired
    private WebApplicationContext webApplicationContext;

    /**
     * @param defaultFilterProcessesUrl the default value for <tt>filterProcessesUrl</tt>.
     */
    public WCTAuthenticationProcessingFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    /**
     * Creates a new instance
     *
     * @param requiresAuthenticationRequestMatcher the {@link RequestMatcher} used to
     * determine if authentication is required. Cannot be null.
     */
    public WCTAuthenticationProcessingFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
    }

    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        String username = request.getParameter(FORM_USERNAME_KEY);
        String password = request.getParameter(FORM_PASSWORD_KEY);

        if (username == null) {
            username = "";
        }

        if (password == null) {
            password = "";
        }

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

        // Place the last username attempted into HttpSession for views
        request.getSession().setAttribute(LAST_USERNAME_KEY, username); // TODO Where is this being used? Perhaps this is the wrong key?

        // Allow subclasses to set the "details" property
        //setDetails(request, authRequest);
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));

        return this.getAuthenticationManager().authenticate(authRequest);

    }

    @Override
    public void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                         Authentication authResult) throws IOException, ServletException {
        log.debug("calling onSuccessfulAuthentication for WCT");
        String userName = authResult.getName();
        
        User wctUser = authDAO.getUserByName(userName);

        // TODO This seems to work now, but it's a mess

        if (wctUser != null) {
	        log.debug("loaded WCT User object "+wctUser.getUsername()+" from database");
	        //UsernamePasswordAuthenticationToken auth =  (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
	        //auth.setDetails(wctUser);
            ((UsernamePasswordAuthenticationToken)authResult).setDetails(wctUser);
	        //log.debug("pushing back upat into SecurityContext with populated WCT User");
	        //SecurityContextHolder.getContext().setAuthentication(auth);
        
	        //audit successful login event
	        auditor.audit(User.class.getName(), wctUser.getOid(), Auditor.ACTION_LOGIN_SUCCESS, "Successful Login for username: "+wctUser.getUsername());
	
	        // Get the Spring Application Context.
			//WebApplicationContext ctx = ApplicationContextFactory.getWebApplicationContext();


			// set or re-set the page size cookie..
			// ..first get the value of the page size cookie
			String currentPageSize = CookieUtils.getPageSize(request);
			// ..then refresh the page size cookie, to expire in a year
			CookieUtils.setPageSize(response, currentPageSize);

	        // set login for duration
	        String sessionId = request.getSession().getId();
	        LogonDurationDAO logonDurationDAO = (LogonDurationDAO) webApplicationContext.getBean(Constants.BEAN_LOGON_DURATION_DAO);
	       	logonDurationDAO.setLoggedIn(sessionId, new Date(), wctUser.getOid(), wctUser.getUsername(), wctUser.getNiceName());
	       	
			// Check previous records of duration
	       	logonDurationDAO.setProperLoggedoutForCurrentUser(wctUser.getOid(), sessionId);
	       	
		}  else {
            
            //audit successful login but unsucessful load of WCT User event
            auditor.audit(User.class.getName(), Auditor.ACTION_LOGIN_FAILURE_NO_USER, "Un-successful login for username: "+userName+" as user doesn't exist in the WCT System.");

        }
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        super.unsuccessfulAuthentication(request, response, failed);
        
        String username = request.getParameter("j_username");
        
        //audit failed login event
        auditor.audit(User.class.getName(), Auditor.ACTION_LOGIN_FAILURE, "Failed Login for username: "+username);
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
}
