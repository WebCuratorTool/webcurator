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

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.targets.TargetManager;
import org.webcurator.core.util.CookieUtils;
import org.webcurator.domain.Pagination;
import org.webcurator.domain.model.auth.Privilege;
import org.webcurator.domain.model.core.TargetGroup;
import org.webcurator.domain.model.dto.GroupMemberDTO;
import org.webcurator.common.ui.Constants;
import org.webcurator.ui.groups.GroupsEditorContext;
import org.webcurator.ui.groups.command.AddMembersCommand;
import org.webcurator.ui.util.Tab;
import org.webcurator.ui.util.TabbedController.TabbedModelAndView;

/**
 * This controller manages the process of adding members to a Target Group.
 * @author bbeaumont
 */
@Controller
public class AddMembersController {
	/** the manager for Target and Group data. */
	@Autowired
	private TargetManager targetManager;
	/** the parent controller for this handler. */
	@Autowired
    @Qualifier("groupsController")
	private TabbedGroupController groupsController;
	/** the manager for checking privleges. */
	@Autowired
	private AuthorityManager authorityManager;

	public class MemberSelection
	{
		private Long oid;
		private String name;

		public MemberSelection(Long oid)
		{
			this.oid = oid;
			this.name = targetManager.loadAbstractTarget(oid).getName();
		}

		public Long getOid()
		{
			return oid;
		}

		public String getName()
		{
			return name;
		}

		public boolean equals(Object selection)
		{
			if(selection instanceof MemberSelection)
			{
				return this.oid.equals(((MemberSelection)selection).oid);
			}
			else
			{
				return false;
			}
		}
	}

	/** Default COnstructor. */
	public AddMembersController() {
	}

	/**
	 * Retrive the editor context for the groups controller.
	 * @param req The HttpServletRequest so the session can be retrieved.
	 * @return The editor context.
	 */
	public GroupsEditorContext getEditorContext(HttpServletRequest req) {
		GroupsEditorContext ctx = (GroupsEditorContext) req.getSession().getAttribute(TabbedGroupController.EDITOR_CONTEXT);
		if( ctx == null) {
			throw new IllegalStateException("tabEditorContext not yet bound to the session");
		}

		return ctx;
	}

	private void addSelection(HttpServletRequest request, Long selection)
	{
		List<MemberSelection> selections = (List<MemberSelection>)request.getSession().getAttribute(AddMembersCommand.SESSION_SELECTIONS);
		if(selections == null)
		{
			selections = new ArrayList<MemberSelection>();
			request.getSession().setAttribute(AddMembersCommand.SESSION_SELECTIONS, selections);
		}

		MemberSelection newSelection = new MemberSelection(selection);
		if(!selections.contains(newSelection))
		{
			selections.add(newSelection);
		}
	}

	private List<MemberSelection> getSelections(HttpServletRequest request)
	{
		List<MemberSelection> selections = (List<MemberSelection>)request.getSession().getAttribute(AddMembersCommand.SESSION_SELECTIONS);
		if(selections == null)
		{
			selections = new ArrayList<MemberSelection>();
			request.getSession().setAttribute(AddMembersCommand.SESSION_SELECTIONS, selections);
		}

		return selections;
	}

	private void clearSelections(HttpServletRequest request)
	{
		request.getSession().removeAttribute(AddMembersCommand.SESSION_SELECTIONS);
	}

	@RequestMapping(path = "/curator/groups/add-members.html", method = {RequestMethod.POST, RequestMethod.GET})
	protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, AddMembersCommand command,
                                  BindingResult bindingResult) throws Exception {
		if( AddMembersCommand.ACTION_ADD_MEMBERS.equals(command.getActionCmd())) {
			TargetGroup group = getEditorContext(request).getTargetGroup();

			//First, add any new selections to the list
			long[] memberOids = command.getMemberOids();
			for(int i=0;i<memberOids.length;i++) {
				long nmo = memberOids[i];
				addSelection(request, nmo);
			}

			Set<Long> ancestorOids = targetManager.getAncestorOids(group);
			List<MemberSelection> newMembers = getSelections(request);

			if(newMembers != null && newMembers.size() > 0 ) {
				// Perform some validation before allowing the members to be
				// added.
				Iterator<MemberSelection> it = newMembers.iterator();
				while(it.hasNext()) {
					MemberSelection newMember = it.next();
					if(ancestorOids.contains(newMember)) {
						// Raise error to prevent creation of loops.
						String name = newMember.getName();
						bindingResult.reject("groups.error.loops", new Object[] { name }, "Adding one of these members would cause an inclusion loop");
					}
					else if(group.getOid() != null && newMember.equals(group.getOid())) {
						// Raise error as cannot create self references.
						String name = newMember.getName();
						bindingResult.reject("groups.error.selfref", new Object[] { name }, "Cannot create self referencing groups");
					}
					else if( targetManager.isDuplicateMember(group, newMember.getOid())) {
						// Prevent the addition of duplicate members.
						String name = newMember.getName();
						bindingResult.reject("groups.bindingResult.duplicate", new Object[] { name }, "Already a member of this group");
					}
				}
			}

			if(bindingResult.hasErrors()) {
				return doSearch(request, response, command, bindingResult);
			}
			else {
				// Add the members only if the user has the appropriate privilege.
				if(newMembers != null &&
						newMembers.size() > 0 &&
						authorityManager.hasPrivilege(group, Privilege.ADD_TARGET_TO_GROUP))
				{
					Iterator<MemberSelection> it = newMembers.iterator();
					while(it.hasNext()) {
						MemberSelection newMember = it.next();
						GroupMemberDTO dto = targetManager.createGroupMemberDTO(group, newMember.getOid());
						group.getNewChildren().add(dto);
					}
				}

				clearSelections(request);

				// Go back to the Members tab on the groups controller.
				Tab membersTab = groupsController.getTabConfig().getTabByID("MEMBERS");
				TabbedModelAndView tmav = membersTab.getTabHandler().preProcessNextTab(groupsController, membersTab, request, response, command, bindingResult);
				tmav.getTabStatus().setCurrentTab(membersTab);
				return tmav;
			}
		}
		else if( AddMembersCommand.ACTION_CANCEL.equals(command.getActionCmd())) {

			clearSelections(request);

			// Go back to the Members tab on the groups controller.
			Tab membersTab = groupsController.getTabConfig().getTabByID("MEMBERS");
			TabbedModelAndView tmav = membersTab.getTabHandler().preProcessNextTab(groupsController, membersTab, request, response, command, bindingResult);
			tmav.getTabStatus().setCurrentTab(membersTab);
			return tmav;
		}
		else if( AddMembersCommand.ACTION_REMOVE.equals(command.getActionCmd())) {
			getSelections(request).remove(command.getMemberIndex());
			return doSearch(request, response, command, bindingResult);
		}
		else {
			long[] memberOids = command.getMemberOids();
			for(int i=0;i<memberOids.length;i++) {
				long nmo = memberOids[i];
				addSelection(request, nmo);
			}

			return doSearch(request, response, command, bindingResult);
		}
	}

	/**
	 * Perform the search for Group members.
	 */
	private ModelAndView doSearch(HttpServletRequest request, HttpServletResponse response, AddMembersCommand command ,
                                  BindingResult bindingResult) {
		// get value of page size cookie
		String currentPageSize = CookieUtils.getPageSize(request);

		if(command.getSearch() == null) {
			command.setSearch("");
			command.setSelectedPageSize(currentPageSize);
		}

		Pagination results = null;
		if (command.getSelectedPageSize().equals(currentPageSize)) {
			// user has left the page size unchanged..
			results = targetManager.getNonSubGroupDTOs(command.getSearch() + "%", command.getPageNumber(), Integer.parseInt(command.getSelectedPageSize()));
		}
		else {
			// user has selected a new page size, so reset to first page..
			results = targetManager.getNonSubGroupDTOs(command.getSearch() + "%", 0, Integer.parseInt(command.getSelectedPageSize()));
			// ..then update the page size cookie
			CookieUtils.setPageSize(response, command.getSelectedPageSize());
		}

		ModelAndView mav = new ModelAndView("group-add-members");
		mav.addObject("page", results);
		mav.addObject(Constants.GBL_CMD_DATA, command);
		mav.addObject(AddMembersCommand.PARAM_SELECTIONS, getSelections(request));
		if(bindingResult.hasErrors()) { mav.addObject(Constants.GBL_ERRORS, bindingResult); }
		return mav;
	}


	/**
	 * @param groupsController The groupsController to set.
	 */
	public void setGroupsController(TabbedGroupController groupsController) {
		this.groupsController = groupsController;
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
}
