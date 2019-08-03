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
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.targets.TargetManager;
import org.webcurator.core.util.CookieUtils;
import org.webcurator.domain.Pagination;
import org.webcurator.domain.model.core.Target;
import org.webcurator.domain.model.dto.GroupMemberDTO;
import org.webcurator.domain.model.dto.GroupMemberDTO.SAVE_STATE;
import org.webcurator.common.Constants;
import org.webcurator.common.ui.target.TargetEditorContext;
import org.webcurator.ui.target.command.AddParentsCommand;
import org.webcurator.ui.target.validator.AddParentsValidator;
import org.webcurator.ui.util.Tab;
import org.webcurator.ui.util.TabbedController.TabbedModelAndView;

/**
 * This controller manages the process of adding members to a Target Group.
 * @author bbeaumont
 */
@Controller
public class AddParentsController {
	/** the manager for Target and Group data. */
	private TargetManager targetManager = null;
	/** the parent controller for this handler. */
	private TabbedTargetController targetController = null;
	/** the manager for checking privleges. */
	private AuthorityManager authorityManager = null;
	private String subGroupSeparator;

	@Autowired
	private AddParentsValidator validator;

	/** Default COnstructor. */
	public AddParentsController() {
	}

	/**
	 * Retrive the editor context for the groups controller.
	 * @param req The HttpServletRequest so the session can be retrieved.
	 * @return The editor context.
	 */
	public TargetEditorContext getEditorContext(HttpServletRequest req) {
		TargetEditorContext ctx = (TargetEditorContext) req.getSession().getAttribute(TabbedTargetController.EDITOR_CONTEXT);
		if( ctx == null) {
			throw new IllegalStateException("tabEditorContext not yet bound to the session");
		}

		return ctx;
	}

	private List<GroupMemberDTO> getParents(HttpServletRequest req) {
		return getEditorContext(req).getParents();
	}

	private void addSelection(HttpServletRequest request, GroupMemberDTO selection)
	{
		List<GroupMemberDTO> selections = (List<GroupMemberDTO>)request.getSession().getAttribute(AddParentsCommand.SESSION_SELECTIONS);
		if(selections == null)
		{
			selections = new ArrayList<GroupMemberDTO>();
			request.getSession().setAttribute(AddParentsCommand.SESSION_SELECTIONS, selections);
		}

		if(!selections.contains(selection))
		{
			selections.add(selection);
		}
	}

	private List<GroupMemberDTO> getSelections(HttpServletRequest request)
	{
		List<GroupMemberDTO> selections = (List<GroupMemberDTO>)request.getSession().getAttribute(AddParentsCommand.SESSION_SELECTIONS);
		if(selections == null)
		{
			selections = new ArrayList<GroupMemberDTO>();
			request.getSession().setAttribute(AddParentsCommand.SESSION_SELECTIONS, selections);
		}

		return selections;
	}

	private void clearSelections(HttpServletRequest request)
	{
		request.getSession().removeAttribute(AddParentsCommand.SESSION_SELECTIONS);
	}


	@PostMapping(value = { "/curator/targets/add-parents.html", "/curator/groups/add-parents.html" })
	protected ModelAndView handle(@Validated @ModelAttribute("addParentsCommand") AddParentsCommand command,
                                  BindingResult bindingResult,
								  HttpServletRequest request, HttpServletResponse response) throws Exception {
		Target target = getEditorContext(request).getTarget();

		if( AddParentsCommand.ACTION_ADD_PARENTS.equals(command.getActionCmd()))
		{
			List<GroupMemberDTO> parents = getParents(request);

			//First, add any new selections to the list
			long[] parentOids = command.getParentOids();
			for(int i=0;i<parentOids.length;i++) {
				long npo = parentOids[i];
				GroupMemberDTO newDTO = targetManager.createGroupMemberDTO(npo, target);
				newDTO.setSaveState(SAVE_STATE.NEW);
				addSelection(request, newDTO);
			}

			//Now lets look for duplicates
			Iterator<GroupMemberDTO> it = getSelections(request).iterator();
			while(it.hasNext()) {
				GroupMemberDTO selection = it.next();
				if(parents.contains(selection)){
						// Trying to add a duplicate.
						String name = selection.getParentName();
						bindingResult.reject("target.error.duplicate_parent", new Object[] { name }, "This target is already in this group");
				}
			}

			if(bindingResult.hasErrors()) {
				return doSearch(command, bindingResult, request, response);
			}
			else {
				parents.addAll(getSelections(request));
				clearSelections(request);
				Tab membersTab = targetController.getTabConfig().getTabByID("GROUPS");
				TabbedModelAndView tmav = membersTab.getTabHandler().preProcessNextTab(targetController, membersTab, request, response, command, bindingResult);
				tmav.getTabStatus().setCurrentTab(membersTab);
				return tmav;
			}
		}
		else if( AddParentsCommand.ACTION_CANCEL.equals(command.getActionCmd())) {
			clearSelections(request);
			// Go back to the Members tab on the groups controller.
			Tab membersTab = targetController.getTabConfig().getTabByID("GROUPS");
			TabbedModelAndView tmav = membersTab.getTabHandler().preProcessNextTab(targetController, membersTab, request, response, command, bindingResult);
			tmav.getTabStatus().setCurrentTab(membersTab);
			return tmav;
		}
		else if( AddParentsCommand.ACTION_REMOVE.equals(command.getActionCmd())) {
			getSelections(request).remove(command.getParentIndex());
			return doSearch(command, bindingResult, request, response);
		}
		else {
			long[] parentOids = command.getParentOids();
			for(int i=0;i<parentOids.length;i++) {
				long npo = parentOids[i];
				GroupMemberDTO newDTO = targetManager.createGroupMemberDTO(npo, target);
				newDTO.setSaveState(SAVE_STATE.NEW);
				addSelection(request, newDTO);
			}
			return doSearch(command, bindingResult, request, response);
		}
	}

	/**
	 * Perform the search for Group members.
	 */
	private ModelAndView doSearch(AddParentsCommand command, BindingResult bindingResult,
                                  HttpServletRequest request, HttpServletResponse response) {

		// get value of page size cookie
		String currentPageSize = CookieUtils.getPageSize(request);

		if(command.getSearch() == null) {
			command.setSearch("");
			command.setSelectedPageSize(currentPageSize);
		}

		Pagination results = null;
		if (command.getSelectedPageSize().equals(currentPageSize)) {
			// user has left the page size unchanged..
			results = targetManager.getGroupDTOs(command.getSearch() + "%", command.getPageNumber(), Integer.parseInt(command.getSelectedPageSize()));
		}
		else {
			// user has selected a new page size, so reset to first page..
			results = targetManager.getGroupDTOs(command.getSearch() + "%", 0, Integer.parseInt(command.getSelectedPageSize()));
			// ..then update the page size cookie
			CookieUtils.setPageSize(response, command.getSelectedPageSize());
		}

		ModelAndView mav = new ModelAndView("target-add-parents");
		mav.addObject("page", results);
		mav.addObject(Constants.GBL_CMD_DATA, command);
		mav.addObject(AddParentsCommand.PARAM_SELECTIONS, getSelections(request));
		mav.addObject("subGroupSeparator", subGroupSeparator);
		if(bindingResult.hasErrors()) { mav.addObject(Constants.GBL_ERRORS, bindingResult); }
		return mav;
	}


	/**
	 * @param targetController The targetController to set.
	 */
	public void setTargetController(TabbedTargetController targetController) {
		this.targetController = targetController;
	}


	/**
	 * @param targetManager The targetManager to set.
	 */
	public void setTargetManager(TargetManager targetManager) {
		this.targetManager = targetManager;
	}


	/**
	 * @param authorityManager The authorityManager to set.
	 */
	public void setAuthorityManager(AuthorityManager authorityManager) {
		this.authorityManager = authorityManager;
	}

	/**
	 * @param subGroupSeparator the subGroupSeparator
	 */
	public void setSubGroupSeparator(String subGroupSeparator) {
		this.subGroupSeparator = subGroupSeparator;
	}
}
