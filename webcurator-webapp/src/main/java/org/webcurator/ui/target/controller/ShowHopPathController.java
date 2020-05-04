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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.harvester.coordinator.HarvestLogManager;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.common.ui.Constants;
import org.webcurator.ui.target.command.ShowHopPathCommand;
import org.webcurator.ui.target.validator.ShowHopPathValidator;

/**
 * The controller for handling the hop path viewer commands.
 * @author skillarney
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class ShowHopPathController {

    @Autowired
	HarvestLogManager harvestLogManager;

    @Autowired
	TargetInstanceManager targetInstanceManager;

	@Autowired
    ShowHopPathValidator validator;

	public ShowHopPathController() {
	}

	@RequestMapping(value = "/curator/target/show-hop-path.html", method = {RequestMethod.POST, RequestMethod.GET})
	protected ModelAndView handle(ShowHopPathCommand cmd , BindingResult bindingResult) throws Exception {
		validator.validate(cmd, bindingResult);

		String messageText = "";
		int firstLine = 0;
		List<String> lines = Arrays.asList("", "");

		if(bindingResult.hasErrors())
		{
			Iterator it = bindingResult.getAllErrors().iterator();
			while(it.hasNext())
			{
				org.springframework.validation.ObjectError err = (org.springframework.validation.ObjectError)it.next();
				if(messageText.length()>0) messageText += "; ";
				messageText += err.getDefaultMessage();
			}
		}
		else if(cmd.getTargetInstanceOid() != null)
		{
			TargetInstance ti = targetInstanceManager.getTargetInstance(cmd.getTargetInstanceOid());

			cmd.setTargetName(ti.getTarget().getName());

			lines = harvestLogManager.getHopPath(ti, cmd.getLogFileName(), cmd.getUrl());
		}
		else
		{
			messageText = "Context has been lost. Please close the Log Viewer and re-open.";
			cmd = new ShowHopPathCommand();
		}

		if (lines.size() == 0) {

		}
		ModelAndView mav = new ModelAndView();
		mav.setViewName(Constants.HOP_PATH__READER);
		mav.addObject(Constants.GBL_CMD_DATA, cmd);
		if (lines.size() == 0) {
			List<String> problem = Arrays.asList("Could not determine Hop Path for Url: " + cmd.getUrl());
			mav.addObject(ShowHopPathCommand.MDL_LINES, parseLines(problem, cmd.getShowLineNumbers(), firstLine, cmd.getNumLines()));
		} else {
			mav.addObject(ShowHopPathCommand.MDL_LINES, parseLines(lines, cmd.getShowLineNumbers(), firstLine, cmd.getNumLines()));
		}
		mav.addObject(Constants.MESSAGE_TEXT, messageText);

		return mav;
	}

	/**
	 * @param harvestLogManager the harvestLogManager to set
	 */
	public void setHarvestLogManager(HarvestLogManager harvestLogManager) {
		this.harvestLogManager = harvestLogManager;
	}

	/**
	 * @param targetInstanceManager the targetInstanceManager to set
	 */
	public void setTargetInstanceManager(TargetInstanceManager targetInstanceManager) {
		this.targetInstanceManager = targetInstanceManager;
	}

	private List<String> parseLines(List<String> inLines, boolean showLineNumbers, int firstLine, int countLines)
	{
		List<String> outLines = new ArrayList<>(inLines.size());
		int lineNumber = firstLine;
		for(int i = 0; i < inLines.size(); i++)
		{
			if(i == 0 && showLineNumbers && lineNumber > -2)
			{
				String[] subLines = inLines.get(i).split("\n");
				if(lineNumber == -1)
				{
					//this is a tail
					lineNumber = 1+(countLines-subLines.length);
				}

				outLines.add(addNumbers(inLines, lineNumber, showLineNumbers));
			}
			else
			{
				outLines.add(inLines.get(i));
			}
		}

		return outLines;
	}


	private String addNumbers(List<String> result, Integer firstLine, boolean showLineNumbers)
	{
		StringBuilder sb = new StringBuilder();
		if(result != null && result.size() == 2)
		{
			String[] lineArray = result.get(0).split("\n");
			for(int i = 0; i < lineArray.length; i++)
			{
				if(i == lineArray.length-1 && lineArray[i].length()==0)
				{
					continue;
				}

				addLine(sb, firstLine++, lineArray[i], showLineNumbers);
			}
		}

		return sb.toString();
	}

	private void addLine(StringBuilder sb, Integer number, String body, boolean showLineNumbers)
	{
		if(showLineNumbers)
		{
			sb.append(number);
			sb.append(". ");
		}

		sb.append(body);
		sb.append("\n");
	}

}
