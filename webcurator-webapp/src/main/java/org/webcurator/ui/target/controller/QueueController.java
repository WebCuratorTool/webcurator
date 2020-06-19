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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.common.ui.CommandConstants;
import org.webcurator.common.ui.Constants;
import org.webcurator.common.util.DateUtils;
import org.webcurator.core.agency.AgencyUserManager;
import org.webcurator.core.common.Environment;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.harvester.coordinator.HarvestCoordinator;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.core.util.AuthUtil;
import org.webcurator.core.util.CookieUtils;
import org.webcurator.domain.Pagination;
import org.webcurator.domain.TargetInstanceCriteria;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.domain.model.auth.User;
import org.webcurator.domain.model.core.*;
import org.webcurator.domain.model.dto.QueuedTargetInstanceDTO;
import org.webcurator.ui.admin.command.FlagCommand;
import org.webcurator.ui.target.command.TargetInstanceCommand;
import org.webcurator.ui.tools.controller.HarvestResourceUrlMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.NumberFormat;
import java.util.*;

/**
 * The controller for displaying a list of target instances and processing
 * commands to filter the list and display the target instances.
 *
 * @author nwaight
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
@PropertySource(value = "classpath:wct-webapp.properties")
public class QueueController {
	private Logger log = LoggerFactory.getLogger(getClass());

	private final List<String> PAGINATION_ACTIONS = Arrays.asList(TargetInstanceCommand.ACTION_NEXT,
			TargetInstanceCommand.ACTION_PREV, TargetInstanceCommand.ACTION_SHOW_PAGE);

	/** The manager to use to access the target instance. */
	@Autowired
	private TargetInstanceManager targetInstanceManager;
	/** The harvest coordinator for looking at the harvesters. */
	@Autowired
	private HarvestCoordinator harvestCoordinator;
	/** the WCT global environment settings. */
	@Autowired
	@Qualifier("environmentWCT")
	private Environment environment;
	/** The manager to use for user, role and agency data. */
	@Autowired
	private AgencyUserManager agencyUserManager;
	@Value("${queueController.thumbnailRenderer}")
	private String thumbnailRenderer = "browseTool";
	@Autowired
	private HarvestResourceUrlMapper harvestResourceUrlMapper;
	@Autowired
	private MessageSource messageSource;

	/** enables the new Target Instance and Harvest Summary pages **/
	@Value("${queueController.enableQaModule}")
	private boolean enableQaModule = false;

	/** the configured width of the harvest preview thumb-nail **/
	@Value("${queueController.thumbnailWidth}")
	private String thumbnailWidth = "150px;";

	/** the configured height of the harvest preview thumb-nail **/
	@Value("${queueController.thumbnailHeight}")
	private String thumbnailHeight = "100px;";

	@InitBinder
	public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		binder.registerCustomEditor(java.util.Date.class, DateUtils.get().getFullDateTimeEditor(true));

		NumberFormat nf = NumberFormat.getInstance(request.getLocale());
		binder.registerCustomEditor(java.lang.Long.class, new CustomNumberEditor(java.lang.Long.class, nf, true));
	}

	public ModelAndView showForm(HttpServletRequest aReq, HttpServletResponse aResp, BindingResult bindingResult)
			throws Exception {
		cleanSession(aReq);
		return processFilter(aReq, aResp, null, bindingResult);
	}

	@RequestMapping(value = "/curator/target/queue.html", method = RequestMethod.GET)
	public ModelAndView showForm(HttpServletRequest aReq, HttpServletResponse aResp)
			throws Exception {
		return showForm(aReq, aResp, null);
	}

	/**
	 * Remove session attributes which ar left over from a previous request
	 *
	 * @param aReq
	 */
	private void cleanSession(HttpServletRequest aReq) {
		HttpSession session = aReq.getSession();
		if (aReq.getParameter(TargetInstanceCommand.REQ_TYPE) != null) {
			session.removeAttribute(TargetInstanceCommand.SESSION_TI_SEARCH_CRITERIA);
		}
		session.removeAttribute(TargetInstanceCommand.SESSION_TI);
		session.removeAttribute(TargetInstanceCommand.SESSION_MODE);
		session.removeAttribute(Constants.GBL_SESS_EDIT_MODE);
	}

	@RequestMapping(value = "/curator/target/queue.html", method = RequestMethod.POST)
	protected ModelAndView processFormSubmission(HttpServletRequest aReq, HttpServletResponse aResp, @ModelAttribute TargetInstanceCommand command,
												 BindingResult bindingResult) throws Exception {
		if (command == null || command.getCmd() == null) {
			throw new WCTRuntimeException("Unknown command recieved.");
		}

		String cmd = command.getCmd();
		log.debug("process command {}", cmd);
		if (cmd.equals(TargetInstanceCommand.ACTION_FILTER) || PAGINATION_ACTIONS.contains(cmd)) {
			return processFilter(aReq, aResp, command, bindingResult);
		} else if (cmd.equals(TargetInstanceCommand.ACTION_RESET)) {
			command.setAgency("");
			command.setOwner("");
			command.setFrom(null);
			command.setTo(null);
			command.setName("");
			command.setStates(new HashSet<String>());
			command.setSearchOid(null);
			command.setFlagged(false);
			command.setSortorder(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_DEFAULT);
			command.setFlagOid(null);
			command.setRecommendationFilter(null);
			return processFilter(aReq, aResp, command, bindingResult);
		} else if (cmd.equals(TargetInstanceCommand.ACTION_PAUSE)) {
			return processPause(aReq, aResp, command, bindingResult);
		} else if (cmd.equals(TargetInstanceCommand.ACTION_RESUME)) {
			return processResume(aReq, aResp, command, bindingResult);
		} else if (cmd.equals(TargetInstanceCommand.ACTION_ABORT)) {
			return processAbort(aReq, aResp, command, bindingResult);
		} else if (cmd.equals(TargetInstanceCommand.ACTION_STOP)) {
			return processStop(aReq, aResp, command, bindingResult);
		} else if (cmd.equals(TargetInstanceCommand.ACTION_DELETE)) {
			processDelete(command.getTargetInstanceId(), bindingResult);
			return processFilter(aReq, aResp, null, bindingResult);
		} else if (cmd.equals(TargetInstanceCommand.ACTION_DELIST)) {
			processDelist(command, bindingResult);
			return processFilter(aReq, aResp, null, bindingResult);
		} else if (cmd.equals(TargetInstanceCommand.ACTION_MULTI_DELETE)) {
			processMultiDelete(command, bindingResult);
			return processFilter(aReq, aResp, null, bindingResult);
		} else if (cmd.equals(TargetInstanceCommand.ACTION_MULTI_ENDORSE)) {
			processMultiEndorse(aReq, command);
			return processFilter(aReq, aResp, null, bindingResult);
		} else if (cmd.equals(TargetInstanceCommand.ACTION_MULTI_REJECT)) {
			processMultiReject(aReq, command, bindingResult);
			return processFilter(aReq, aResp, null, bindingResult);
		} else if (cmd.equals(TargetInstanceCommand.ACTION_MULTI_ARCHIVE)) {
			// build a list of target instance ids in a query string then
			// redirect to the
			// ArchiveController for processing
			StringBuilder queryString = new StringBuilder("redirect:/curator/archive/submit.html?");
			List<String> multiSelect = command.getMultiselect();
			for (int i = 0; i < multiSelect.size(); i++) {
				String tIOid = multiSelect.get(i);
				queryString.append("instanceID=");
				queryString.append(tIOid);
				if (i < multiSelect.size()) {
					queryString.append("&");
				}
			}
			return new ModelAndView(queryString.toString());
		} else if (cmd.equals(TargetInstanceCommand.ACTION_MULTI_DELIST)) {
			processMultiDelist(command, bindingResult);
			return processFilter(aReq, aResp, null, bindingResult);
		} else {
			throw new WCTRuntimeException("Unknown command " + cmd + " recieved.");
		}
	}

	/**
	 * @param aTargetInstanceManager
	 *            The targetInstanceManager to set.
	 */
	public void setTargetInstanceManager(TargetInstanceManager aTargetInstanceManager) {
		targetInstanceManager = aTargetInstanceManager;
	}

	/**
	 * process the filter target instances action.
	 */
	@SuppressWarnings("unchecked")
	ModelAndView processFilter(HttpServletRequest aReq, HttpServletResponse aResp, TargetInstanceCommand aCmd,
							   BindingResult bindingResult) {

		ModelAndView mav = new ModelAndView();
		if (bindingResult != null && bindingResult.hasErrors()) {
			mav.addObject(Constants.GBL_ERRORS, bindingResult);
		}

		TargetInstanceCommand searchCommand = (TargetInstanceCommand) aReq.getSession().getAttribute(
				TargetInstanceCommand.SESSION_TI_SEARCH_CRITERIA);

		TargetInstanceCriteria criteria = new TargetInstanceCriteria();

		// get value of page size cookie
		String currentPageSize = CookieUtils.getPageSize(aReq);

		if (aCmd != null && !PAGINATION_ACTIONS.contains(aCmd.getCmd())) {
			copyCommandToCriteria(aCmd, criteria);
			aCmd.setCmd(TargetInstanceCommand.ACTION_FILTER);
		} else if (searchCommand != null) {
			aCmd = createFilterCommandFromSearchCommand(aReq, aCmd, mav, searchCommand, currentPageSize);
			copyCommandToCriteria(aCmd, criteria);
		} else {
			aCmd = createNewFilterCommand(aReq, criteria, currentPageSize);
		}

		// apply the recommendation filter (archive, reject, investigate, delist
		// etc)
		Set<String> recommendationFilter = aCmd.getRecommendationFilter();
		criteria.setRecommendationFilter(recommendationFilter);

		if (enableQaModule && aCmd.getFlagOid() != null) {
			Flag flag = agencyUserManager.getFlagByOid(aCmd.getFlagOid());
			criteria.setFlag(flag);
		}

		// run the search ...
		Pagination instances = null;
		if (aCmd.getSelectedPageSize() == null) {
			aCmd.setSelectedPageSize(currentPageSize);
		}

		if (aCmd.getSelectedPageSize().equals(currentPageSize)) {
			// user has left the page size unchanged..
			instances = targetInstanceManager.search(criteria, aCmd.getPageNo(), Integer.parseInt(aCmd.getSelectedPageSize()));
		} else {
			// user has selected a new page size, so reset to first page..
			instances = targetInstanceManager.search(criteria, 0, Integer.parseInt(aCmd.getSelectedPageSize()));
			// ..then update the page size cookie
			CookieUtils.setPageSize(aResp, aCmd.getSelectedPageSize());
		}

		HashMap<Long, Set<Indicator>> indicators = new HashMap<>();
		List<Long> targetList = new ArrayList<Long>();
		HashMap<Long, String> browseUrls = new HashMap<>();
		// we need to populate annotations to determine if targets are
		// alertable.
		if (instances != null) {
			List<TargetInstance> targetInstances = instances.getList();
			for (TargetInstance ti : targetInstances) {
				ti.setAnnotations(targetInstanceManager.getAnnotations(ti));
				// we also fetch any QA Indicators if the QA module is enabled
				List<Indicator> tiIndicators = ti.getIndicators();
				if (enableQaModule) {
					indicators.put(ti.getOid(), new HashSet<Indicator>(tiIndicators));
					addQaInformationForTi(indicators, browseUrls, ti);
				}
				// keep a track of the target oids so that we can retrieve the
				// furture schdeule
				targetList.add(ti.getTarget().getOid());
			}
			mav.addObject("browseUrls", browseUrls);
			mav.addObject("thumbnailRenderer", thumbnailRenderer);
		}

		HashMap<Long, Long> futureScheduleCount = new HashMap<Long, Long>();
		for (Long targetOid : targetList) {
			// determine if there is a future schedule (to enable the delist
			// checkbox)
			futureScheduleCount.put(targetOid, targetInstanceManager.countQueueLengthForTarget(targetOid));
		}

		aCmd.setQueuePaused(harvestCoordinator.isQueuePaused());

		mav.addObject(TargetInstanceCommand.MDL_INDICATORS, indicators);
		mav.addObject(TargetInstanceCommand.MDL_INSTANCES, instances);
		mav.addObject(TargetInstanceCommand.MDL_FUTURE_SCHEDULE_COUNT, futureScheduleCount);

		// Put the instances into the "page" attribute as well for the standard
		// pagination bar.
		mav.addObject("page", instances);
		mav.addObject("action", Constants.CNTRL_TI_QUEUE);
		mav.addObject(Constants.GBL_CMD_DATA, aCmd);
		aReq.getSession().setAttribute(TargetInstanceCommand.SESSION_TI_SEARCH_CRITERIA, aCmd);

		List<Agency> agencies = agencyUserManager.getAgencies();
		mav.addObject(TargetInstanceCommand.MDL_AGENCIES, agencies);

		Agency selectedAgency = null;
		for (Agency a : agencies) {
			if (a.getName().equals(aCmd.getAgency())) {
				selectedAgency = a;
				break;
			}
		}
		List owners = null;
		if (selectedAgency != null) {
			owners = agencyUserManager.getUserDTOs(selectedAgency.getOid());
		} else {
			owners = agencyUserManager.getUserDTOs();
		}
		mav.addObject(TargetInstanceCommand.MDL_OWNERS, owners);

		if (enableQaModule) {
			addQaInformation(mav);
			mav.setViewName(Constants.VIEW_TARGET_INSTANCE_QA_QUEUE);
		} else {
			mav.setViewName(Constants.VIEW_TARGET_INSTANCE_QUEUE);
		}

		return mav;
	}

	private void addQaInformation(ModelAndView mav) {
		// fetch the valid rejection reasons for targets
		// (used to populate the rejection reason drop-down)
		User user = AuthUtil.getRemoteUserObject();
		List<RejReason> rejectionReasons = agencyUserManager.getValidRejReasonsForTIs(user.getAgency().getOid());
		mav.addObject(TargetInstanceCommand.MDL_REASONS, rejectionReasons);
		// add all the flags defined on the system for the flag filter
		List<Flag> flags = agencyUserManager.getFlagForLoggedInUser();
		mav.addObject(FlagCommand.MDL_FLAGS, flags);
		// add the configured thumb-nail size
		mav.addObject(Constants.THUMBNAIL_WIDTH, thumbnailWidth);
		mav.addObject(Constants.THUMBNAIL_HEIGHT, thumbnailHeight);
	}

	void addQaInformationForTi(HashMap<Long, Set<Indicator>> indicators, HashMap<Long, String> browseUrls, TargetInstance ti) {
		// fetch the harvest results and seeds so that we can form the url for
		// the browse tool preview
		Long tiOid = ti.getOid();
		// find the last result with a state of un-assessed, indexing or
		// endorsed
		Long lastDisplayableResultOid = null;
		HarvestResult lastDisplayableResult = null;

		List<HarvestResult> results = targetInstanceManager.getHarvestResults(tiOid);
		for (HarvestResult result : results) {
			if (result.getState() == HarvestResult.STATE_UNASSESSED || result.getState() == HarvestResult.STATE_INDEXING
					|| result.getState() == HarvestResult.STATE_ENDORSED) {
				lastDisplayableResultOid = result.getOid();
				lastDisplayableResult = result;
			}
		}

		String seed = getPrimarySeedName(ti);
		String thumbnailRendererName = thumbnailRenderer.toUpperCase();
		if (thumbnailRendererName.equals("BROWSETOOL")) {
			if (lastDisplayableResultOid != null) {
				browseUrls.put(tiOid, "curator/tools/browse/" + String.valueOf(lastDisplayableResultOid) + "/" + seed);
			} else {
				browseUrls.put(tiOid, null);
			}
		}

		//TODO
		if (thumbnailRendererName.equals("ACCESSTOOL") && lastDisplayableResult != null) {
//			HarvestResourceDTO hRsr = null;
//			try {
//				hRsr = targetInstanceManager.getHarvestResourceDTO(lastDisplayableResult.getOid(), seed);
//			} catch (Exception e) {
//				log.debug("Multiple resource instances found for seed {}, ti: {}.  Using first instance.", seed, tiOid);
//				Map<String, HarvestResource> resources = lastDisplayableResult.getResources();
//				hRsr = resources.get(seed).buildDTO();
//			}
//			if (hRsr != null) {
//				browseUrls.put(tiOid, harvestResourceUrlMapper.generateUrl(lastDisplayableResult, hRsr));
//			} else {
//				log.warn("Cannot find seed '{}' in harvest result ({}).", seed, lastDisplayableResult.getOid());
//			}
		}
	}

	private String getPrimarySeedName(TargetInstance ti) {
		Set<Seed> seeds = ti.getTarget().getSeeds();
		if(seeds==null) return null;
		String lastSeedName = null;
		// fetch the primary seed
		for (Seed currentSeed : seeds) {
			String seedName = currentSeed.getSeed();
			if (currentSeed.isPrimary()) {
				return seedName;
			}
			lastSeedName = seedName;
		}
		// if no seed is marked as primary then use the last one, or null
		return lastSeedName;
	}

	// Package private so that this method can be unit tested reasonably cleanly
	TargetInstanceCommand createFilterCommandFromSearchCommand(HttpServletRequest aReq, TargetInstanceCommand aCmd,
															   ModelAndView mav, TargetInstanceCommand searchCommand, String currentPageSize) {
		if (aCmd == null) {
			// we have come from another page so update the page size in case it
			// has been changed since the searchcommand was saved
			searchCommand.setSelectedPageSize(currentPageSize);
		} else if (PAGINATION_ACTIONS.contains(aCmd.getCmd())) {
			searchCommand.setCmd(aCmd.getCmd());
			searchCommand.setPageNo(aCmd.getPageNo());
			searchCommand.setSelectedPageSize(aCmd.getSelectedPageSize());
		}

		String showSubmittedMessageParameter = aReq.getParameter(TargetInstanceCommand.REQ_SHOW_SUBMITTED_MSG);
		if (showSubmittedMessageParameter != null) {
			if (showSubmittedMessageParameter.equals("y")) {
				addMessageToModel(mav, "ui.label.targetinstance.submitToArchiveStarted");
			} else {
				addMessageToModel(mav, "ui.label.targetinstance.submitToArchiveFailed");
			}
		}

		searchCommand.setCmd(TargetInstanceCommand.ACTION_FILTER);
		return searchCommand;
	}

	private void addMessageToModel(ModelAndView mav, String message) {
		mav.addObject(Constants.GBL_MESSAGES, messageSource.getMessage(message, new Object[] {}, Locale.getDefault()));
	}

	private TargetInstanceCommand createNewFilterCommand(HttpServletRequest aReq, TargetInstanceCriteria criteria,
														 String currentPageSize) {
		TargetInstanceCommand aCmd;
		// Command is null and there is no search command. Need to set up the
		// new
		// filter parameters
		aCmd = new TargetInstanceCommand();
		aCmd.setSelectedPageSize(currentPageSize);

		Set<String> states = new HashSet<String>();
		String reqType = aReq.getParameter(TargetInstanceCommand.REQ_TYPE);
		if (TargetInstanceCommand.TYPE_TARGET.equals(reqType)) {
			String name = aReq.getParameter(TargetInstanceCommand.PARAM_TARGET_NAME);
			criteria.setName(name);
			aCmd.setName(name);
			aCmd.setStates(states);
			aCmd.setSortorder(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_DEFAULT);
		} else if (TargetInstanceCommand.TYPE_HARVESTED.equals(reqType)) {
			User user = AuthUtil.getRemoteUserObject();
			criteria.setOwner(user.getUsername());
			aCmd.setOwner(user.getUsername());
			criteria.setAgency(user.getAgency().getName());
			aCmd.setAgency(user.getAgency().getName());

			states.add(TargetInstance.STATE_HARVESTED);
			aCmd.setStates(states);
			aCmd.setSortorder(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_DEFAULT);
		} else {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, environment.getDaysToSchedule() + 1);
			cal.set(Calendar.HOUR_OF_DAY, cal.getActualMaximum(Calendar.HOUR_OF_DAY));
			cal.set(Calendar.MINUTE, cal.getActualMaximum(Calendar.MINUTE));
			cal.set(Calendar.SECOND, cal.getActualMaximum(Calendar.SECOND));
			criteria.setTo(cal.getTime());
			aCmd.setTo(cal.getTime());
			aCmd.setSortorder(CommandConstants.TARGET_INSTANCE_COMMAND_SORT_DEFAULT);

			if (TargetInstanceCommand.TYPE_QUEUE.equals(reqType)) {
				states.add(TargetInstance.STATE_SCHEDULED);
				states.add(TargetInstance.STATE_QUEUED);
				states.add(TargetInstance.STATE_RUNNING);
				states.add(TargetInstance.STATE_STOPPING);
				states.add(TargetInstance.STATE_PAUSED);
				aCmd.setStates(states);
			} else {
				cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, cal.getActualMinimum(Calendar.HOUR_OF_DAY));
				cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE));
				cal.set(Calendar.SECOND, cal.getActualMinimum(Calendar.SECOND));
				cal.add(Calendar.DATE, -1);
				criteria.setFrom(cal.getTime());
				aCmd.setFrom(cal.getTime());

				states.addAll(TargetInstance.getOrderedStates().keySet());

				User user = AuthUtil.getRemoteUserObject();
				criteria.setOwner(user.getUsername());
				aCmd.setOwner(user.getUsername());
				criteria.setAgency(user.getAgency().getName());
				aCmd.setAgency(user.getAgency().getName());

				criteria.setSearchOid(aCmd.getSearchOid());
			}
		}

		criteria.setStates(states);
		criteria.setSortorder(aCmd.getSortorder());

		aCmd.setCmd(TargetInstanceCommand.ACTION_FILTER);
		return aCmd;
	}

	/**
	 * Copy the search filter data from the command to the criteria
	 *
	 * @param aCmd
	 *            the command to copy search criteria from
	 * @param aCriteria
	 *            the criteria object to copy data to
	 */
	private void copyCommandToCriteria(TargetInstanceCommand aCmd, TargetInstanceCriteria aCriteria) {
		aCriteria.setFrom(aCmd.getFrom());
		aCriteria.setTo(aCmd.getTo());

		String name = aCmd.getName();
		if (name != null) {
			aCriteria.setName(name);
		}
		Set<String> commandStates = aCmd.getStates();
		if (commandStates != null) {
			if (!commandStates.isEmpty()) {
				aCriteria.setStates(commandStates);
			}
			if (commandStates.contains(TargetInstance.STATE_RUNNING)) {
				aCriteria.getStates().add(TargetInstance.STATE_STOPPING);
			}
		}
		String owner = aCmd.getOwner();
		if (owner != null) {
			aCriteria.setOwner(owner);
		}
		String agency = aCmd.getAgency();
		if (agency != null) {
			aCriteria.setAgency(agency);
		}
		aCriteria.setSearchOid(aCmd.getSearchOid());
		aCriteria.setFlagged(aCmd.getFlagged());
		aCriteria.setNondisplayonly(aCmd.getNondisplayonly());
		aCriteria.setSortorder(aCmd.getSortorder());

		String action = aCmd.getCmd();
		if (action.equals(TargetInstanceCommand.ACTION_NEXT)) {
			aCmd.setPageNo(aCmd.getPageNo() + 1);
		}

		if (action.equals(TargetInstanceCommand.ACTION_PREV)) {
			aCmd.setPageNo(aCmd.getPageNo() - 1);
		}
	}

	/**
	 * process the delete target instance action.
	 */
	private void processDelete(Long targetInstanceId, BindingResult bindingResult) {
		TargetInstance ti = null;
		try {
			try {
				ti = targetInstanceManager.getTargetInstance(targetInstanceId);
			} catch (RuntimeException e) {
				// assume that the target instance has already been deleted.
				ti = null;
			}

			// You can only delete a target instance if it is scheduled or
			// queued
			// and if it doesn't have a HarvestStatus (i.e. has not yet begun
			// harvesting)
			if (ti != null) {
				if (ti.getState() != null && !ti.getState().equals(TargetInstance.STATE_SCHEDULED)
						&& !ti.getState().equals(TargetInstance.STATE_QUEUED)) {
					bindingResult.reject("target.instance.not.deleteable", new Object[] { ti.getJobName() },
							"The target instance may not be deleted.");
				} else {
					if (ti.getStatus() == null) {
						targetInstanceManager.delete(ti);
					} else {
						bindingResult.reject("target.instance.not.deleteable", new Object[] { ti.getJobName() },
								"The target instance may not be deleted.");
					}
				}
			}
		} catch (org.springframework.orm.hibernate5.HibernateObjectRetrievalFailureException e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * process the multi-delete target instance action.
	 */
	private void processMultiDelete(TargetInstanceCommand aCmd, BindingResult bindingResult) {
		// the id of each TI to remove is passed in the multiselect attribute
		// for a multi-delete operation
		for (String targetInstanceOid : aCmd.getMultiselect()) {
			// delete the ti
			processDelete(Long.parseLong(targetInstanceOid), bindingResult);
		}
	}

	/**
	 * process the multi-endorse target instance action.
	 */
	private void processMultiEndorse(HttpServletRequest aReq, TargetInstanceCommand aCmd) {
		// the id of each TI to endorse is passed in the multiselect attribute
		// for a multi-endorse operation
		for (String selectedOid : aCmd.getMultiselect()) {
			Long targetInstanceOid = Long.parseLong(selectedOid);
			TargetInstance ti = processEndorse(targetInstanceOid, this.getLastHarvestResultOid(targetInstanceOid));
			aReq.getSession().setAttribute(TargetInstanceCommand.SESSION_TI, ti);
		}
	}

	/**
	 * process the endorse target instance action.
	 */
	private TargetInstance processEndorse(Long targetInstanceOid, Long harvestResultId) {
		// set the ti state and the hr states
		TargetInstance ti = targetInstanceManager.getTargetInstance(targetInstanceOid);
		ti.setState(TargetInstance.STATE_ENDORSED);

		for (HarvestResult hr : ti.getHarvestResults()) {
			if (hr.getOid().equals(harvestResultId)) {
				hr.setState(HarvestResult.STATE_ENDORSED);
			} else if (hr.getState() != HarvestResult.STATE_REJECTED) {
				hr.setState(HarvestResult.STATE_REJECTED);
				harvestCoordinator.removeIndexes(hr);
			}
			targetInstanceManager.save((HarvestResult) hr);
		}

		targetInstanceManager.save(ti);

		return ti;
	}

	/**
	 * process the reject target instance action.
	 */
	private void processReject(HttpServletRequest aReq, TargetInstanceCommand aCmd, BindingResult bindingResult) {
		// set the ti state and the hr states
		TargetInstance ti = targetInstanceManager.getTargetInstance(aCmd.getTargetInstanceId());
		for (HarvestResult hr : ti.getHarvestResults()) {
			if (hr.getOid().equals(aCmd.getHarvestResultId())) {
				if (hr.getState() != HarvestResult.STATE_REJECTED) {
					hr.setState(HarvestResult.STATE_REJECTED);
					RejReason rejReason = agencyUserManager.getRejReasonByOid(aCmd.getRejReasonId());
					if (rejReason == null) {
						bindingResult.reject("rejection.reason.missing", new Object[] { ti.getJobName() },
								"No rejection reason specified, but this is a required field.  Please create one if no rejection reasons are configured.");
					} else {
						hr.setRejReason(rejReason);
						harvestCoordinator.removeIndexes(hr);
					}
				}

				targetInstanceManager.save((HarvestResult) hr);
			}
		}

		boolean allRejected = true;
		for (HarvestResult hr : ti.getHarvestResults()) {
			if ((HarvestResult.STATE_REJECTED != hr.getState()) && (HarvestResult.STATE_ABORTED != hr.getState())) {
				allRejected = false;
				break;
			}
		}

		if (allRejected) {
			ti.setState(TargetInstance.STATE_REJECTED);
			ti.setArchivedTime(new Date());
		}

		targetInstanceManager.save(ti);
		aReq.getSession().setAttribute(TargetInstanceCommand.SESSION_TI, ti);
	}

	/**
	 * process the multi-reject target instance action.
	 */
	private void processMultiReject(HttpServletRequest aReq, TargetInstanceCommand aCmd, BindingResult bindingResult) {
		// the id of each TI to reject is passed in the multiselect attribute
		// for a multi-reject operation
		// NOTE: the reject reason will already have been set on the command (by
		// Spring MVC)
		for (String tIOid : aCmd.getMultiselect()) {
			Long targetInstanceOid = new Long(tIOid);
			// set the ti oid on the command
			aCmd.setTargetInstanceId(targetInstanceOid);
			// iterate over the harvest results setting the oid on the command
			List<HarvestResult> harvestResults = targetInstanceManager.getHarvestResults(targetInstanceOid);
			for (HarvestResult harvestResult : harvestResults) {
				aCmd.setHarvestResultId(harvestResult.getOid());
				// set the harvest result to reject
				processReject(aReq, aCmd, bindingResult);
			}
		}
	}

	/**
	 * process the delist target instance action (ie: terminate schedule)
	 *
	 * @param command
	 *            the command carrying the target instance id of the target
	 *            instance whos target will have its schedule terminated
	 * @param bindingResult
	 *            any bindingResult encountered
	 * @throws Exception
	 *             any unexpected exception
	 */
	private void processDelist(TargetInstanceCommand command, BindingResult bindingResult) {

		TargetInstance ti = targetInstanceManager.getTargetInstance(command.getTargetInstanceId());

		// fetch the original schedules (array avoid concurrent modifications)
		// iterate over the schedules and terminating each one
		Set<Schedule> schedules = ti.getTarget().getSchedules();
		for (Schedule schedule : schedules) {
			schedule.setEndDate(new Date());
			targetInstanceManager.save(ti);

		}

		// delete all the tis for the same target that have a state of scheduled
		// (using multi-delete)
		List<String> tisToDelete = new ArrayList<String>();

		Long targetOid = ti.getTarget().getOid();
		List<QueuedTargetInstanceDTO> queue = targetInstanceManager.getQueueForTarget(targetOid);
		for (QueuedTargetInstanceDTO qti : queue) {
			TargetInstance child = targetInstanceManager.getTargetInstance(qti.getOid());
			// delete the ti if it has the same target
			if (child.getTarget().getOid().equals(targetOid)) {
				tisToDelete.add(child.getOid().toString());
			}
		}
		// use multi-delete functionality to delete the queued sublist
		command.setMultiselect(tisToDelete);
		command.setCmd(TargetInstanceCommand.ACTION_MULTI_DELETE);
		processMultiDelete(command, bindingResult);
	}

	/**
	 * process the multi-delist target instance action
	 *
	 * @param command
	 *            the command carrying the list of target instance oid's whose
	 *            target instance's target should have its schedule terminated
	 * @param bindingResult
	 *            any bindingResult encountered
	 * @throws Exception
	 *             any unexpected exception
	 */
	private void processMultiDelist(TargetInstanceCommand command, BindingResult bindingResult) {
		// the id of each TI to reject is passed in the multi-select attribute
		// for a multi-delist operation
		for (String tIOid : command.getMultiselect()) {
			Long targetInstanceOid = new Long(tIOid);
			command.setTargetInstanceId(targetInstanceOid);
			// terminate the schedule for the tis target
			processDelist(command, bindingResult);
		}
	}

	/**
	 * Helper method to return the last harvest result oid for a specified
	 * harvest instance
	 *
	 * @param targetInstanceOid
	 *            the harvest instance oid that contains the list of harvest
	 *            results
	 * @return the last harvest result oid
	 */
	private final Long getLastHarvestResultOid(Long targetInstanceOid) {
		List<HarvestResult> harvestResults = targetInstanceManager.getHarvestResults(targetInstanceOid);
		int numResults = harvestResults.size();
		// return the last harvest result
		return harvestResults.get(numResults - 1).getOid();
	}

	/**
	 * process the pause target instance action.
	 */
	private ModelAndView processPause(HttpServletRequest aReq, HttpServletResponse aResp, TargetInstanceCommand aCmd,
									  BindingResult bindingResult) {
		TargetInstance ti = targetInstanceManager.getTargetInstance(aCmd.getTargetInstanceId());
		harvestCoordinator.pause(ti);

		return processFilter(aReq, aResp, null, bindingResult);
	}

	/**
	 * process the resume target instance action.
	 */
	private ModelAndView processResume(HttpServletRequest aReq, HttpServletResponse aResp, TargetInstanceCommand aCmd,
									   BindingResult bindingResult) {
		TargetInstance ti = targetInstanceManager.getTargetInstance(aCmd.getTargetInstanceId());
		harvestCoordinator.resume(ti);

		return processFilter(aReq, aResp, null, bindingResult);
	}

	/**
	 * process the abort target instance action.
	 */
	private ModelAndView processAbort(HttpServletRequest aReq, HttpServletResponse aResp, TargetInstanceCommand aCmd,
									  BindingResult bindingResult) {
		TargetInstance ti = targetInstanceManager.getTargetInstance(aCmd.getTargetInstanceId());
		harvestCoordinator.abort(ti);

		return processFilter(aReq, aResp, null, bindingResult);
	}

	/**
	 * process the stop target instance action.
	 */
	private ModelAndView processStop(HttpServletRequest aReq, HttpServletResponse aResp, TargetInstanceCommand aCmd,
									 BindingResult bindingResult) {
		TargetInstance ti = targetInstanceManager.getTargetInstance(aCmd.getTargetInstanceId());
		harvestCoordinator.stop(ti);

		return processFilter(aReq, aResp, null, bindingResult);
	}

	public void setHarvestCoordinator(HarvestCoordinator harvestCoordinator) {
		this.harvestCoordinator = harvestCoordinator;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public void setAgencyUserManager(AgencyUserManager agencyUserManager) {
		this.agencyUserManager = agencyUserManager;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * Enable/disable the new QA Module (disabled by default)
	 */
	public void setEnableQaModule(Boolean enableQaModule) {
		this.enableQaModule = enableQaModule;
	}

	public void setThumbnailWidth(String thumbnailWidth) {
		this.thumbnailWidth = thumbnailWidth;
	}

	public void setThumbnailHeight(String thumbnailHeight) {
		this.thumbnailHeight = thumbnailHeight;
	}

	public void setThumbnailRenderer(String thumbnailRenderer) {
		this.thumbnailRenderer = thumbnailRenderer;
	}

	public void setHarvestResourceUrlMapper(HarvestResourceUrlMapper harvestResourceUrlMapper) {
		this.harvestResourceUrlMapper = harvestResourceUrlMapper;
	}

}