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

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.harvester.coordinator.HarvestLogManager;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.ui.target.command.LogReaderCommand;
import org.webcurator.ui.target.validator.LogReaderValidator;

/**
 * The controller for handling the log viewer commands.
 *
 * @author nwaight
 */
@Controller
@RequestMapping("/curator/target/content-viewer.html")
public class ContentReaderController {

	HarvestLogManager harvestLogManager;

	TargetInstanceManager targetInstanceManager;

	@Autowired
	private LogReaderValidator validator;

	public ContentReaderController() {
	}

	@GetMapping
	protected ModelAndView handle(@RequestParam("targetInstanceOid") Long targetInstanceOid,
								  @RequestParam("logFileName") String logFileName) throws Exception {
		LogReaderCommand cmd = new LogReaderCommand();
		cmd.setTargetInstanceOid(targetInstanceOid);
		cmd.setLogFileName(logFileName);

		TargetInstance ti = targetInstanceManager.getTargetInstance(cmd.getTargetInstanceOid());
		File f = null;

		try {
			f = harvestLogManager.getLogfile(ti, cmd.getLogFileName());
		} catch (WCTRuntimeException e) {

		}

		ContentView v = new ContentView(f, cmd.getLogFileName(), true);
		return new ModelAndView(v);
	}

	/**
	 * @param harvestLogManager
	 *            the harvestLogManager to set
	 */
	public void setHarvestLogManager(HarvestLogManager harvestLogManager) {
		this.harvestLogManager = harvestLogManager;
	}

	/**
	 * @param targetInstanceManager
	 *            the targetInstanceManager to set
	 */
	public void setTargetInstanceManager(TargetInstanceManager targetInstanceManager) {
		this.targetInstanceManager = targetInstanceManager;
	}
}
