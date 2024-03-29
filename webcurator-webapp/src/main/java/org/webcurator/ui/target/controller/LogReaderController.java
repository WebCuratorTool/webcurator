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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.common.ui.Constants;
import org.webcurator.core.coordinator.WctCoordinator;
import org.webcurator.core.harvester.coordinator.HarvestLogManager;
import org.webcurator.core.harvester.coordinator.PatchingHarvestLogManager;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.ui.target.command.LogReaderCommand;
import org.webcurator.ui.target.validator.LogReaderValidator;

import java.util.*;

/**
 * The controller for handling the log viewer commands.
 *
 * @author nwaight
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
@SuppressWarnings("all")
public class LogReaderController {
	@Autowired
	WctCoordinator wctCoordinator;
	@Autowired
	TargetInstanceManager targetInstanceManager;

	private Map<String, String> filterTypes = null;
	private Map<String, String> filterNames = null;

	@Autowired
	@Qualifier("logReaderValidator")
	private LogReaderValidator logReaderValidator;

	@Autowired
	private HarvestLogManager harvestLogManager;

	@Autowired
	@Qualifier(HarvestResult.PATCH_STAGE_TYPE_CRAWLING)
	private PatchingHarvestLogManager patchingHarvestLogManager;

	@Autowired
	@Qualifier(HarvestResult.PATCH_STAGE_TYPE_MODIFYING)
	private PatchingHarvestLogManager patchingHarvestLogManagerModification;

	@Autowired
	@Qualifier(HarvestResult.PATCH_STAGE_TYPE_INDEXING)
	private PatchingHarvestLogManager patchingHarvestLogManagerIndex;

	public LogReaderController() {
		//Add values in reverse order of display
		filterNames = new HashMap<String, String>();
		filterNames.put(LogReaderCommand.VALUE_HEAD, "");
		filterNames.put(LogReaderCommand.VALUE_TAIL, "");
		filterNames.put(LogReaderCommand.VALUE_FROM_LINE, "Line Number");
		filterNames.put(LogReaderCommand.VALUE_TIMESTAMP, "Date/Time");
		filterNames.put(LogReaderCommand.VALUE_REGEX_INDENT, "Regular Expression");
		filterNames.put(LogReaderCommand.VALUE_REGEX_MATCH, "Regular Expression");
		filterNames.put(LogReaderCommand.VALUE_REGEX_CONTAIN, "Regular Expression");

		//Add values in reverse order of display
		filterTypes = new HashMap<String, String>();
		filterTypes.put(LogReaderCommand.VALUE_HEAD, "Return the specified number of lines from the HEAD of the file");
		filterTypes.put(LogReaderCommand.VALUE_TAIL, "Return the specified number of lines from the TAIL of the file");
		filterTypes.put(LogReaderCommand.VALUE_FROM_LINE, "Return the specified number of lines starting at the specified line number");
		filterTypes.put(LogReaderCommand.VALUE_TIMESTAMP, "Return the specified number of lines starting at the specified Date/Time");
		filterTypes.put(LogReaderCommand.VALUE_REGEX_INDENT, "Return all lines matching regex with following indented lines");
		filterTypes.put(LogReaderCommand.VALUE_REGEX_MATCH, "Return all lines matching regex");
		filterTypes.put(LogReaderCommand.VALUE_REGEX_CONTAIN, "Start from first line containing regex");
	}

	@RequestMapping(path = "/curator/target/log-viewer.html", method = {RequestMethod.POST, RequestMethod.GET})
	protected ModelAndView handle(@ModelAttribute("logReaderCommand") LogReaderCommand cmd,
								  BindingResult bindingResult) throws Exception {
		logReaderValidator.validate(cmd, bindingResult);

//        if (cmd.getPrefix() != null && cmd.getPrefix().length() > 0) {
//            return handlePatchingLogReader(cmd, bindingResult);
//        }

		TargetInstance ti = targetInstanceManager.getTargetInstance(cmd.getTargetInstanceOid());

		String messageText = "";
		int firstLine = 0;
		List<String> lines = Arrays.asList("", "");

		if (bindingResult.hasErrors()) {
			Iterator it = bindingResult.getAllErrors().iterator();
			while (it.hasNext()) {
				org.springframework.validation.ObjectError err = (org.springframework.validation.ObjectError) it.next();
				if (messageText.length() > 0) messageText += "; ";
				messageText += err.getDefaultMessage();
			}
		} else if (cmd.getTargetInstanceOid() != null) {
			cmd.setTargetName(ti.getTarget().getName());

			//Count the log lines first time in
			if ((cmd.getNumLines() == null || cmd.getNumLines().intValue() == -1)) {
				cmd.setNumLines(wctCoordinator.countLogLines(ti, cmd.getLogFileName()));
			}
			if (cmd.getNoOfLines() == null || cmd.getNoOfLinesInt() == 0) {
				cmd.setNoOfLines(700);
			}

			try {
				if (LogReaderCommand.VALUE_TIMESTAMP.equals(cmd.getFilterType())) {
					// do timestamp filtering
					if (cmd.getLongTimestamp() != -1) {
						firstLine = wctCoordinator.getFirstLogLineAfterTimeStamp(ti, cmd.getLogFileName(), cmd.getLongTimestamp());
						if (firstLine > -1) {
							lines = wctCoordinator.getLog(ti, cmd.getLogFileName(), firstLine, cmd.getNoOfLinesInt());
						} else {
							// do empty tail
							firstLine = -2;
							lines = wctCoordinator.tailLog(ti, cmd.getLogFileName(), 0);
						}
					} else {
						firstLine = -2;
						messageText = cmd.getFilter() + " is not a valid date/time format";
					}
				} else if (LogReaderCommand.VALUE_REGEX_CONTAIN.equals(cmd.getFilterType())) {
					firstLine = wctCoordinator.getFirstLogLineContaining(ti, cmd.getLogFileName(), cmd.getFilter());
					if (firstLine > -1) {
						lines = wctCoordinator.getLog(ti, cmd.getLogFileName(), firstLine, cmd.getNoOfLinesInt());
					} else {
						// do empty tail
						firstLine = -2;
						lines = wctCoordinator.tailLog(ti, cmd.getLogFileName(), 0);
					}
				} else if (LogReaderCommand.VALUE_REGEX_INDENT.equals(cmd.getFilterType())) {
					firstLine = -2;
					//Try to trap indent regex's
					if (cmd.getFilter().startsWith("^[ ") ||
							cmd.getFilter().startsWith("^[\\t")) {
						messageText = cmd.getFilter() + " will only return indented lines. Please choose a Regular Expression that can return non indented lines.";
					} else {
						//Fix up the regex
						String regex = cmd.getFilter();
						if (!cmd.getFilter().startsWith("^")) {
							regex = "^[^ \\t].*" + cmd.getFilter();
						}

						lines = wctCoordinator.getLogLinesByRegex(ti, cmd.getLogFileName(), cmd.getNoOfLinesInt(), regex, true);
						if (lines != null && lines.size() == 2) {
							StringBuilder sb = new StringBuilder();
							String[] subLines = lines.get(0).split("\n");
							for (int i = 0; i < subLines.length; i++) {
								sb.append(getFollowingIndentedLines(ti, cmd, subLines[i], cmd.getShowLineNumbers()));
							}

							lines.set(0, sb.toString());
						} else {
							// do empty tail
							firstLine = -2;
							lines = wctCoordinator.tailLog(ti, cmd.getLogFileName(), 0);
						}
					}
				} else if (LogReaderCommand.VALUE_REGEX_MATCH.equals(cmd.getFilterType())) {
					// do regex filtering
					firstLine = -2;
					lines = wctCoordinator.getLogLinesByRegex(ti, cmd.getLogFileName(), cmd.getNoOfLinesInt(), cmd.getFilter(), cmd.getShowLineNumbers());
				} else if (LogReaderCommand.VALUE_FROM_LINE.equals(cmd.getFilterType())) {
					//do get
					firstLine = cmd.getFilter() == null || cmd.getFilter().length() == 0 ? 0 : new Integer(cmd.getFilter()).intValue();
					lines = wctCoordinator.getLog(ti, cmd.getLogFileName(), firstLine, cmd.getNoOfLinesInt());
				} else if (LogReaderCommand.VALUE_HEAD.equals(cmd.getFilterType())) {
					//do head
					firstLine = 1;
					lines = wctCoordinator.headLog(ti, cmd.getLogFileName(), cmd.getNoOfLinesInt());
				} else {
					// do tail
					firstLine = -1;
					lines = wctCoordinator.tailLog(ti, cmd.getLogFileName(), cmd.getNoOfLinesInt());
				}
			} catch (org.webcurator.core.exceptions.WCTRuntimeException e) {
				if (e.getCause().getMessage().startsWith("java.util.regex.PatternSyntaxException")) {
					firstLine = -2;
					lines.set(0, e.getCause().getMessage().substring(e.getCause().getMessage().indexOf(":") + 2));
				} else {
					throw e;
				}
			}
		} else {
			messageText = "Context has been lost. Please close the Log Viewer and re-open.";
			cmd = new LogReaderCommand();
		}

		ModelAndView mav = new ModelAndView();
		mav.setViewName(Constants.VIEW_LOG_READER);
		mav.addObject(Constants.GBL_CMD_DATA, cmd);
		mav.addObject(LogReaderCommand.MDL_FILTER_NAMES, this.filterNames);
		mav.addObject(LogReaderCommand.MDL_FILTER_TYPES, this.filterTypes);
		mav.addObject(LogReaderCommand.MDL_LINES, parseLines(lines, cmd.getShowLineNumbers(), firstLine, cmd.getNumLines()));
		mav.addObject(Constants.MESSAGE_TEXT, messageText);

		return mav;
	}

	@RequestMapping(path = "/curator/target/patch-log-viewer.html", method = {RequestMethod.POST, RequestMethod.GET})
	protected ModelAndView handlePatchingLogReader(@ModelAttribute("logReaderCommand") LogReaderCommand cmd,
												   BindingResult bindingResult) throws Exception {
		logReaderValidator.validate(cmd, bindingResult);
		String messageText = "";
		int firstLine = 0;
		List<String> lines = Arrays.asList("", "");

		if (bindingResult.hasErrors()) {
			Iterator it = bindingResult.getAllErrors().iterator();
			while (it.hasNext()) {
				org.springframework.validation.ObjectError err = (org.springframework.validation.ObjectError) it.next();
				if (messageText.length() > 0) messageText += "; ";
				messageText += err.getDefaultMessage();
			}
		} else if (cmd.getTargetInstanceOid() != null) {
			int state = HarvestResult.STATE_UNASSESSED;
			if (cmd.getPrefix().equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_INDEXING)) {
				state = HarvestResult.STATE_INDEXING;
			} else if (cmd.getPrefix().equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_MODIFYING)) {
				state = HarvestResult.STATE_MODIFYING;
			} else if (cmd.getPrefix().equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_CRAWLING)) {
				state = HarvestResult.STATE_CRAWLING;
			}

			TargetInstance ti = targetInstanceManager.getTargetInstance(cmd.getTargetInstanceOid());
			cmd.setTargetName(ti.getTarget().getName());

			HarvestResult hr = ti.getHarvestResult(cmd.getHarvestResultNumber());

			PatchingHarvestLogManager logReader = null;
			if (cmd.getPrefix().equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_INDEXING)) {
				logReader = patchingHarvestLogManagerIndex;
			} else if (cmd.getPrefix().equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_MODIFYING)) {
				logReader = patchingHarvestLogManagerModification;
			} else {
				logReader = patchingHarvestLogManager;
			}

			//Count the log lines first time in
			if ((cmd.getNumLines() == null || cmd.getNumLines().intValue() == -1)) {
				cmd.setNumLines(logReader.countLogLines(ti.getOid(), hr.getHarvestNumber(), state, cmd.getLogFileName()));
			}
			if (cmd.getNoOfLines() == null || cmd.getNoOfLinesInt() == 0) {
				cmd.setNoOfLines(700);
			}

			try {
				if (LogReaderCommand.VALUE_TIMESTAMP.equals(cmd.getFilterType())) {
					// do timestamp filtering
					if (cmd.getLongTimestamp() != -1) {
						firstLine = logReader.getFirstLogLineAfterTimeStamp(ti.getOid(), hr.getHarvestNumber(), state, cmd.getLogFileName(), cmd.getLongTimestamp());
						if (firstLine > -1) {
							lines = logReader.getLog(ti.getOid(), hr.getHarvestNumber(), state, cmd.getLogFileName(), firstLine, cmd.getNoOfLinesInt());
						} else {
							// do empty tail
							firstLine = -2;
							lines = logReader.tailLog(ti.getOid(), hr.getHarvestNumber(), state, cmd.getLogFileName(), 0);
						}
					} else {
						firstLine = -2;
						messageText = cmd.getFilter() + " is not a valid date/time format";
					}
				} else if (LogReaderCommand.VALUE_REGEX_CONTAIN.equals(cmd.getFilterType())) {
					firstLine = logReader.getFirstLogLineContaining(ti.getOid(), hr.getHarvestNumber(), state, cmd.getLogFileName(), cmd.getFilter());
					if (firstLine > -1) {
						lines = logReader.getLog(ti.getOid(), hr.getHarvestNumber(), state, cmd.getLogFileName(), firstLine, cmd.getNoOfLinesInt());
					} else {
						// do empty tail
						firstLine = -2;
						lines = logReader.tailLog(ti.getOid(), hr.getHarvestNumber(), state, cmd.getLogFileName(), 0);
					}
				} else if (LogReaderCommand.VALUE_REGEX_INDENT.equals(cmd.getFilterType())) {
					firstLine = -2;
					//Try to trap indent regex's
					if (cmd.getFilter().startsWith("^[ ") ||
							cmd.getFilter().startsWith("^[\\t")) {
						messageText = cmd.getFilter() + " will only return indented lines. Please choose a Regular Expression that can return non indented lines.";
					} else {
						//Fix up the regex
						String regex = cmd.getFilter();
						if (!cmd.getFilter().startsWith("^")) {
							regex = "^[^ \\t].*" + cmd.getFilter();
						}

						lines = logReader.getLogLinesByRegex(ti.getOid(), hr.getHarvestNumber(), state, cmd.getLogFileName(), cmd.getNoOfLinesInt(), regex, true);
						if (lines != null && lines.size() == 2) {
							StringBuilder sb = new StringBuilder();
							String[] subLines = lines.get(0).split("\n");
							for (int i = 0; i < subLines.length; i++) {
								sb.append(getFollowingIndentedLines(ti, cmd, subLines[i], cmd.getShowLineNumbers()));
							}

							lines.set(0, sb.toString());
						} else {
							// do empty tail
							firstLine = -2;
							lines = logReader.tailLog(ti.getOid(), hr.getHarvestNumber(), state, cmd.getLogFileName(), 0);
						}
					}
				} else if (LogReaderCommand.VALUE_REGEX_MATCH.equals(cmd.getFilterType())) {
					// do regex filtering
					firstLine = -2;
					lines = logReader.getLogLinesByRegex(ti.getOid(), hr.getHarvestNumber(), state, cmd.getLogFileName(), cmd.getNoOfLinesInt(), cmd.getFilter(), cmd.getShowLineNumbers());
				} else if (LogReaderCommand.VALUE_FROM_LINE.equals(cmd.getFilterType())) {
					//do get
					firstLine = cmd.getFilter() == null || cmd.getFilter().length() == 0 ? 0 : new Integer(cmd.getFilter()).intValue();
					lines = logReader.getLog(ti.getOid(), hr.getHarvestNumber(), state, cmd.getLogFileName(), firstLine, cmd.getNoOfLinesInt());
				} else if (LogReaderCommand.VALUE_HEAD.equals(cmd.getFilterType())) {
					//do head
					firstLine = 1;
					lines = logReader.headLog(ti.getOid(), hr.getHarvestNumber(), state, cmd.getLogFileName(), cmd.getNoOfLinesInt());
				} else {
					// do tail
					firstLine = -1;
					lines = logReader.tailLog(ti.getOid(), hr.getHarvestNumber(), state, cmd.getLogFileName(), cmd.getNoOfLinesInt());
				}
			} catch (org.webcurator.core.exceptions.WCTRuntimeException e) {
				if (e.getCause().getMessage().startsWith("java.util.regex.PatternSyntaxException")) {
					firstLine = -2;
					lines.set(0, e.getCause().getMessage().substring(e.getCause().getMessage().indexOf(":") + 2));
				} else {
					throw e;
				}
			}
		} else {
			messageText = "Context has been lost. Please close the Log Viewer and re-open.";
			cmd = new LogReaderCommand();
		}

		ModelAndView mav = new ModelAndView();
		mav.setViewName(Constants.VIEW_LOG_READER);
		mav.addObject(Constants.GBL_CMD_DATA, cmd);
		mav.addObject(LogReaderCommand.MDL_FILTER_NAMES, this.filterNames);
		mav.addObject(LogReaderCommand.MDL_FILTER_TYPES, this.filterTypes);
		mav.addObject(LogReaderCommand.MDL_LINES, parseLines(lines, cmd.getShowLineNumbers(), firstLine, cmd.getNumLines()));
		mav.addObject(Constants.MESSAGE_TEXT, messageText);

		return mav;
	}

	/**
	 * @param wctCoordinator the wctCoordinator to set
	 */
	public void setWctCoordinator(WctCoordinator wctCoordinator) {
		this.wctCoordinator = wctCoordinator;
	}

	/**
	 * @param targetInstanceManager the targetInstanceManager to set
	 */
	public void setTargetInstanceManager(TargetInstanceManager targetInstanceManager) {
		this.targetInstanceManager = targetInstanceManager;
	}

	private List<String> parseLines(List<String> inLines, boolean showLineNumbers, int firstLine, int countLines) {
		List<String> outLines = new ArrayList<>(inLines.size());
		int lineNumber = firstLine;
		for (int i = 0; i < inLines.size(); i++) {
			if (i == 0 && showLineNumbers && lineNumber > -2) {
				String[] subLines = inLines.get(i).split("\n");
				if (lineNumber == -1) {
					//this is a tail
					lineNumber = 1 + (countLines - subLines.length);
				}

				outLines.add(addNumbers(inLines, lineNumber, showLineNumbers));
			} else {
				outLines.add(inLines.get(i));
			}
		}

		return outLines;
	}

	private String getFollowingIndentedLines(TargetInstance ti, LogReaderCommand cmd, String numberedLine, boolean showLineNumbers) {
		int index = numberedLine.indexOf(".");
		if (index == -1) {
			return "";
		}
		Integer firstLine = new Integer(numberedLine.substring(0, index));
		Integer lastLine = firstLine + 1;

		String[] nonIndents = fetchNonIndentedLines(ti, cmd);
		//find the first non indented line after firstLine
		for (int i = 0; i < nonIndents.length; i++) {
			try {
				String line = nonIndents[i];
				lastLine = new Integer(line.substring(0, line.indexOf(".")));
				if (lastLine > firstLine) {
					break;
				}
			} catch (Exception e) {
				lastLine = firstLine + 1;
				break;
			}
		}

		List<String> requiredLines = wctCoordinator.getLog(ti, cmd.getLogFileName(), firstLine, lastLine - firstLine);

		return addNumbers(requiredLines, firstLine, showLineNumbers);
	}

	private String addNumbers(List<String> result, Integer firstLine, boolean showLineNumbers) {
		StringBuilder sb = new StringBuilder();
		if (result != null && result.size() == 2) {
			String[] lineArray = result.get(0).split("\n");
			for (int i = 0; i < lineArray.length; i++) {
				if (i == lineArray.length - 1 && lineArray[i].length() == 0) {
					continue;
				}

				addLine(sb, firstLine++, lineArray[i], showLineNumbers);
			}
		}

		return sb.toString();
	}

	private void addLine(StringBuilder sb, Integer number, String body, boolean showLineNumbers) {
		if (showLineNumbers) {
			sb.append(number);
			sb.append(". ");
		}

		sb.append(body);
		sb.append("\n");
	}

	private String[] fetchNonIndentedLines(TargetInstance ti, LogReaderCommand cmd) {
		String[] nonIndentedLines = {""};
		//Get all non indented lines with line numbers
		List<String> nonIndents = wctCoordinator.getLogLinesByRegex(ti, cmd.getLogFileName(), cmd.getNumLines().intValue(), "^[^ \\t].*", true);
		if (nonIndents != null &&
				nonIndents.get(0).length() > 0) {
			nonIndentedLines = nonIndents.get(0).split("\n");
		}

		return nonIndentedLines;
	}
}
