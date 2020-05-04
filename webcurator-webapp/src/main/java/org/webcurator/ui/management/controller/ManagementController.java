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
package org.webcurator.ui.management.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.common.ui.Constants;

/**
 * Controller to render the management "menu" tab.
 * @author bprice
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
@PropertySource(value = "classpath:wct-webapp.properties")
public class ManagementController {

	/** enables the Management page (QA version) when true **/
    @Value("${queueController.enableQaModule}")
	private boolean enableQaModule;

    @RequestMapping(method = RequestMethod.GET, path = "/curator/admin/management.html")
    protected ModelAndView showForm() throws Exception {
        ModelAndView mav = new ModelAndView();

        if (!enableQaModule) {
        	mav.setViewName(Constants.VIEW_MANAGEMENT);
        } else {
        	mav.setViewName(Constants.VIEW_QA_MANAGEMENT);
        }
        return mav;
    }

    protected ModelAndView processFormSubmission() throws Exception {
        // TODO Implement this if we require a POST version of the management screen
        return null;
    }

	/**
	 * Enable/disable the new QA Module (disabled by default)
	 * @param enableQaModule Enables the QA module.
	 */
	public void setEnableQaModule(Boolean enableQaModule) {
		this.enableQaModule = enableQaModule;
	}

}
