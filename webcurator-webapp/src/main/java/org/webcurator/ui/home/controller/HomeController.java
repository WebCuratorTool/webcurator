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
package org.webcurator.ui.home.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.notification.InTrayManager;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.core.sites.SiteManager;
import org.webcurator.core.targets.TargetManager;
import org.webcurator.core.util.AuthUtil;
import org.webcurator.domain.model.auth.User;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.common.ui.Constants;

/**
 * The home controller is responsible for rendering the home page.
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class HomeController extends AbstractController {
	public static final String MDL_CNT_NOTIFICATION = "notificationsCount";
	public static final String MDL_CNT_TASK = "tasksCount";
	public static final String MDL_CNT_SITE = "sitesCount";
	public static final String MDL_CNT_TARGET = "targetsCount";
	public static final String MDL_CNT_GROUPS = "targetGroupsCount";
	public static final String MDL_CNT_SCHEDULED = "scheduledCount";
	public static final String MDL_CNT_QR = "qualityReviewCount";

	/** The manager for getting task and notification counts. */
	@Autowired
	InTrayManager inTrayManager;
	/** the manager for accessing privileges for a user. */
	@Autowired
	AuthorityManager authorityManager;
	/** The manager for getting the site count. */
	@Autowired
	SiteManager siteManager;
	/** The manager for getting a count of targets. */
	@Autowired
	TargetManager targetManager;
	/** the manager for getting a count of target instances. */
	@Autowired
	TargetInstanceManager targetInstanceManager;
	/** enables the new Qa Home page **/
    @Value("${queueController.enableQaModule}")
	private boolean enableQaModule;

    // Default request mapping for root application context
	@RequestMapping(method = RequestMethod.GET, path = "/")
	protected ModelAndView defaultPage(){
		return new ModelAndView("redirect:/curator/home.html");
	}

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        ModelAndView mav = new ModelAndView();

        User user = AuthUtil.getRemoteUserObject();

        long notificationCount = inTrayManager.countNotifications(user);
        long taskCount = inTrayManager.countTasks(user);
        long siteCount = siteManager.countSites();
        long targetCount = targetManager.countTargets(user);
        long groupCount = targetManager.countTargetGroups(user);

        ArrayList<String> states = new ArrayList<String>();
        states.add(TargetInstance.STATE_SCHEDULED);
        long schedCount = targetInstanceManager.countTargetInstances(user, states);

        states.clear();
        states.add(TargetInstance.STATE_HARVESTED);
        long qaCount = targetInstanceManager.countTargetInstances(user, states);

        mav.addObject(MDL_CNT_NOTIFICATION, notificationCount);
        mav.addObject(MDL_CNT_TASK, taskCount);
        mav.addObject(MDL_CNT_SITE, siteCount);
        mav.addObject(MDL_CNT_TARGET, targetCount);
        mav.addObject(MDL_CNT_GROUPS, groupCount);
        mav.addObject(MDL_CNT_SCHEDULED, schedCount);
        mav.addObject(MDL_CNT_QR, qaCount);
        if (!enableQaModule) {
        	mav.setViewName(Constants.VIEW_HOME);
        } else {
        	mav.setViewName(Constants.VIEW_QA_HOME);
        }

        return mav;
    }

	/**
	 * @param inTrayManager the inTrayManager to set
	 */
	public void setInTrayManager(InTrayManager inTrayManager) {
		this.inTrayManager = inTrayManager;
	}

	/**
	 * @param authorityManager the authorityManager to set
	 */
	public void setAuthorityManager(AuthorityManager authorityManager) {
		this.authorityManager = authorityManager;
	}

	/**
	 * @param siteManager the siteManager to set
	 */
	public void setSiteManager(SiteManager siteManager) {
		this.siteManager = siteManager;
	}

	/**
	 * @param targetManager the targetManager to set
	 */
	public void setTargetManager(TargetManager targetManager) {
		this.targetManager = targetManager;
	}

	/**
	 * @param targetInstanceManager the targetInstanceManager to set
	 */
	public void setTargetInstanceManager(TargetInstanceManager targetInstanceManager) {
		this.targetInstanceManager = targetInstanceManager;
	}

	/**
	 * Enable/disable the new QA Module (disabled by default)
	 * @param enableQaModule Enables the QA module.
	 */
	public void setEnableQaModule(Boolean enableQaModule) {
		this.enableQaModule = enableQaModule;
	}
}
