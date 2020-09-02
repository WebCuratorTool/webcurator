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
package org.webcurator.ui.groups.controller;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.agency.AgencyUserManager;
import org.webcurator.core.common.WCTTreeSet;
import org.webcurator.core.targets.TargetManager;
import org.webcurator.core.util.AuthUtil;
import org.webcurator.core.util.CookieUtils;
import org.webcurator.domain.Pagination;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.domain.model.core.TargetGroup;
import org.webcurator.common.ui.Constants;
import org.webcurator.ui.groups.command.SearchCommand;

/**
 * The controller for processing target group searches.
 * @author bbeaumont
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class GroupSearchController {
	/** the manager for accessing target and group data. */
	@Autowired
	private TargetManager targetManager = null;
	/** the manager for accessing user and agency data. */
	@Autowired
	private AgencyUserManager agencyUserManager = null;
	/** The message source for localisation */
	@Autowired
	private MessageSource messageSource;
	/** Default Search on Agency only (not username) */
	@Value("${groupSearchController.defaultSearchOnAgencyOnly}")
	private boolean defaultSearchOnAgencyOnly = false;

	/** The list of available group types */
	@Autowired
	private WCTTreeSet groupTypesList = null;
	@Value("${groupTypes.subgroup}")
	private String subGroupType;
	@Value("${groupTypes.subgroupSeparator}")
	private String subGroupSeparator;


	/** Default Constructor. */
	public GroupSearchController() {
	}

	@InitBinder
	public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		NumberFormat nf = NumberFormat.getInstance(request.getLocale());
		binder.registerCustomEditor(java.lang.Long.class, new CustomNumberEditor(java.lang.Long.class, nf, true));
	}

	@RequestMapping(value = "/curator/groups/search.html", method = RequestMethod.GET)
	protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response) throws Exception {

		// fetch command object (if any) from session..
		SearchCommand command = (SearchCommand) request.getSession().getAttribute("groupsSearchCommand");
		if(command == null) {
			command = getDefaultCommand();
		}
		command.setSelectedPageSize(CookieUtils.getPageSize(request));

		return prepareSearchView(request, response, command);
	}

	/**
	 * @return The default populated search command.
	 */
	public SearchCommand getDefaultCommand() {
		SearchCommand command = new SearchCommand();
		Agency usersAgency = AuthUtil.getRemoteUserObject().getAgency();
		command.setPageNumber(0);
		command.setAgency(usersAgency.getName());
		if(!defaultSearchOnAgencyOnly)
		{
			command.setOwner(AuthUtil.getRemoteUserObject().getUsername());
		}
		command.setSearchOid(null);

		return command;
	}

	/**
	 * @return The search view.
	 */
	@SuppressWarnings("unchecked")
	public ModelAndView prepareSearchView(HttpServletRequest request, HttpServletResponse response) {

		// fetch command object (if any) from session..
		SearchCommand command = (SearchCommand) request.getSession().getAttribute("groupsSearchCommand");
		if(command == null) {
			command = getDefaultCommand();
		}
		command.setSelectedPageSize(CookieUtils.getPageSize(request));

		return prepareSearchView(request, response, command);
	}

	/**
	 * @return The search view.
	 */
	@SuppressWarnings("unchecked")
	public ModelAndView prepareSearchView(HttpServletRequest request, HttpServletResponse response,
										  SearchCommand command) {

		List<Agency> agencies = agencyUserManager.getAgencies();

		// We need to find the OID of the agency.
		Agency currentAgency = null;
		for(Agency a: agencies) {
			if(a.getName().equals(command.getAgency())) {
				currentAgency = a;
				break;
			}
		}

		List owners = null;
		if (currentAgency != null) {
			owners = agencyUserManager.getUserDTOs(currentAgency.getOid());
		}
		else {
			owners = agencyUserManager.getUserDTOs();
		}

		// get value of page size cookie
		String currentPageSize = CookieUtils.getPageSize(request);
		if(command.getSelectedPageSize() == null)
		{
			//belt and braces - should never get here if the jsp is correct
			command.setSelectedPageSize(currentPageSize);
		}

		Pagination results = null;
		if (command.getSelectedPageSize().equals(currentPageSize)) {
			// user has left the page size unchanged..
			results = targetManager.searchGroups(command.getPageNumber(), Integer.parseInt(command.getSelectedPageSize()), command.getSearchOid(), command.getName(), command.getOwner(), command.getAgency(), command.getMemberOf(), command.getGroupType(), command.getNondisplayonly());
		}
		else {
			// user has selected a new page size, so reset to first page..
			results = targetManager.searchGroups(0, Integer.parseInt(command.getSelectedPageSize()), command.getSearchOid(), command.getName(), command.getOwner(), command.getAgency(), command.getMemberOf(), command.getGroupType(), command.getNondisplayonly());
			// ..then update the page size cookie
			CookieUtils.setPageSize(response, command.getSelectedPageSize());
		}

		ModelAndView mav = new ModelAndView("groups-search");
		mav.addObject("agencies", agencies);
		mav.addObject("owners", owners);
		mav.addObject("groupTypesList", groupTypesList);
		mav.addObject(Constants.GBL_CMD_DATA, command);
		mav.addObject("page", results);
		mav.addObject("subGroupType", subGroupType);
		mav.addObject("subGroupSeparator", subGroupSeparator);

		return mav;
	}

	@RequestMapping(value = "/curator/groups/search.html", method = RequestMethod.POST)
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, @ModelAttribute SearchCommand command)
			throws Exception {

		if(command.isAction(SearchCommand.ACTION_DELETE)) {
			TargetGroup group = targetManager.loadGroup(command.getDeletedGroupOid());
			if(targetManager.deleteTargetGroup(group)) {
				ModelAndView mav = prepareSearchView(request, response, command);
				mav.addObject(Constants.GBL_MESSAGES, messageSource.getMessage("group.deleted.success", new Object[] { group.getName() }, Locale.getDefault()));
				return mav;
			}
			else {
				ModelAndView mav = prepareSearchView(request, response, command);
				mav.addObject(Constants.GBL_MESSAGES, messageSource.getMessage("group.deleted.failed.instances", new Object[] { group.getName() }, Locale.getDefault()));
				return mav;
			}
		}
		else if(command.isAction(SearchCommand.ACTION_RESET)) {
			command.setPageNumber(0);
			command.setAgency("");
			command.setName("");
			command.setOwner("");
			command.setMemberOf("");
			command.setGroupType("");
			command.setSearchOid(null);

			request.getSession().setAttribute("groupsSearchCommand", command);
			return prepareSearchView(request, response, command);
		}
		else {
			request.getSession().setAttribute("groupsSearchCommand", command);
			return prepareSearchView(request, response, command);
		}
	}




	/**
	 * @param targetManager The targetManager to set.
	 */
	public void setTargetManager(TargetManager targetManager) {
		this.targetManager = targetManager;
	}

	/**
	 * @param agencyUserManager The agencyUserManager to set.
	 */
	public void setAgencyUserManager(AgencyUserManager agencyUserManager) {
		this.agencyUserManager = agencyUserManager;
	}

	/**
	 * @param messageSource The messageSource to set.
	 */
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * @param defaultSearchOnAgencyOnly
	 */
	public void setDefaultSearchOnAgencyOnly(boolean defaultSearchOnAgencyOnly)
	{
		this.defaultSearchOnAgencyOnly = defaultSearchOnAgencyOnly;
	}

	/**
	 * @return defaultSearchOnAgencyOnly
	 */
	public boolean getDefaultSearchOnAgencyOnly()
	{
		return this.defaultSearchOnAgencyOnly;
	}

	/**
	 * @param groupTypesList The groupTypesList to set.
	 */
	public void setGroupTypesList(WCTTreeSet groupTypesList) {
		this.groupTypesList = groupTypesList;
	}


	/**
	 * @param subGroupType The subGroupType to set.
	 */
	public void setSubGroupType(String subGroupType) {
		this.subGroupType = subGroupType;
	}


	/**
	 * @param subGroupSeparator The subGroupSeparator to set.
	 */
	public void setSubGroupSeparator(String subGroupSeparator) {
		this.subGroupSeparator = subGroupSeparator;
	}
}
