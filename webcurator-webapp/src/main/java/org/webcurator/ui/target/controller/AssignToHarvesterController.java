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

import java.util.HashMap;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.coordinator.WctCoordinator;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;
import org.webcurator.common.ui.Constants;
import org.webcurator.ui.target.command.TargetInstanceCommand;

@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
@RequestMapping("/curator/target/ti-harvest-now.html")
public class AssignToHarvesterController {

	/** The target instance manager */
	@Autowired
	private TargetInstanceManager targetInstanceManager;
	/* The Harvest Coordinator */
	@Autowired
	private WctCoordinator wctCoordinator;

	/**
	 * Create the controller object and set the command class.
	 */
	public AssignToHarvesterController() {
	}

	@GetMapping
	protected ModelAndView handle(@RequestParam("targetInstanceId") Long targetInstanceId,@RequestParam("harvestResultId") Long harvestResultId) throws Exception {
		TargetInstanceCommand command = new TargetInstanceCommand();
		command.setTargetInstanceId(targetInstanceId);
		command.setHarvestResultId(harvestResultId);
		HashMap<String, HarvestAgentStatusDTO> agents = wctCoordinator.getHarvestAgents();
		TargetInstance ti = targetInstanceManager.getTargetInstance(command.getTargetInstanceId());
		String instanceAgency = ti.getOwner().getAgency().getName();

		String key = "";
		HarvestAgentStatusDTO agent = null;
		HashMap<String, HarvestAgentStatusDTO> allowedAgents = new HashMap<String, HarvestAgentStatusDTO>();
		Iterator<String> it = agents.keySet().iterator();
		while (it.hasNext()) {
			key = (String) it.next();
			agent = agents.get(key);
			if ((agent.getAllowedAgencies().contains(instanceAgency) || agent.getAllowedAgencies().isEmpty())
					&& agent.getHarvesterType().equals(ti.getProfile().getHarvesterType())) {
				allowedAgents.put(key, agent);
			}
		}

		ModelAndView mav = new ModelAndView();
		mav.addObject(TargetInstanceCommand.SESSION_TI, ti);
		mav.addObject(TargetInstanceCommand.MDL_INSTANCE, ti);
		mav.addObject(Constants.GBL_CMD_DATA, command);
		mav.addObject(TargetInstanceCommand.MDL_AGENTS, allowedAgents);
		mav.setViewName(Constants.VIEW_HARVEST_NOW);

		return mav;
	}

	public WctCoordinator getHarvestCoordinator() {
		return wctCoordinator;
	}


	public void setHarvestCoordinator(WctCoordinator wctCoordinator) {
		this.wctCoordinator = wctCoordinator;
	}


	public TargetInstanceManager getTargetInstanceManager() {
		return targetInstanceManager;
	}


	public void setTargetInstanceManager(TargetInstanceManager targetInstanceManager) {
		this.targetInstanceManager = targetInstanceManager;
	}
}
