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
package org.webcurator.ui.target.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.targets.TargetManager;
import org.webcurator.ui.target.command.PermissionPopupCommand;

/**
 * The controller for the permission popup view.
 * @author bbeaumont
 */
@Controller
@RequestMapping("/curator/target/permission-popup.html")
public class PermissionPopupController {
    @Autowired
	private TargetManager targetManager;

	@GetMapping
	protected ModelAndView handle(@RequestParam("permissionOid") Long permissionOid) throws Exception {
		PermissionPopupCommand command = new PermissionPopupCommand();
		command.setPermissionOid(permissionOid);
		ModelAndView mav = new ModelAndView("permission-popup");
		mav.addObject("permission", targetManager.loadPermission(command.getPermissionOid()));
		return mav;
	}

	/**
	 * @param targetManager The targetManager to set.
	 */
	public void setTargetManager(TargetManager targetManager) {
		this.targetManager = targetManager;
	}




}
