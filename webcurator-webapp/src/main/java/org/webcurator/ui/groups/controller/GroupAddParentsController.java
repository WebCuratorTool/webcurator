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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.targets.TargetManager;
import org.webcurator.core.util.CookieUtils;
import org.webcurator.domain.Pagination;
import org.webcurator.domain.model.core.TargetGroup;
import org.webcurator.domain.model.dto.GroupMemberDTO;
import org.webcurator.domain.model.dto.GroupMemberDTO.SAVE_STATE;
import org.webcurator.common.ui.Constants;
import org.webcurator.ui.groups.GroupsEditorContext;
import org.webcurator.ui.groups.command.AddParentsCommand;
import org.webcurator.ui.groups.validator.AddParentsValidator;
import org.webcurator.ui.util.Tab;
import org.webcurator.ui.util.TabbedController.TabbedModelAndView;

/**
 * This controller manages the process of adding members to a Target Group.
 *
 * @author bbeaumont
 */
@Controller
public class GroupAddParentsController {
    /**
     * the manager for Target and Group data.
     */
    @Autowired
    private TargetManager targetManager;
    /**
     * the parent controller for this handler.
     */
    @Autowired
    @Qualifier("groupsController")
    private TabbedGroupController groupsController;
    /**
     * the manager for checking privleges.
     */
    @Autowired
    private AuthorityManager authorityManager;
    @Autowired
    private AddParentsValidator addParentsValidator;

    /**
     * Retrive the editor context for the groups controller.
     *
     * @param req The HttpServletRequest so the session can be retrieved.
     * @return The editor context.
     */
    public GroupsEditorContext getEditorContext(HttpServletRequest req) {
        GroupsEditorContext ctx = (GroupsEditorContext) req.getSession().getAttribute(TabbedGroupController.EDITOR_CONTEXT);
        if (ctx == null) {
            throw new IllegalStateException("tabEditorContext not yet bound to the session");
        }

        return ctx;
    }

    private List<GroupMemberDTO> getParents(HttpServletRequest req) {
        return targetManager.getParents(getEditorContext(req).getTargetGroup());
    }

    @RequestMapping(value = {"/curator/groups/add-parents.html"}, method = {RequestMethod.GET, RequestMethod.POST})
    protected ModelAndView handle(@ModelAttribute("addParentsCommand") AddParentsCommand comm,
                                  HttpServletRequest request, HttpServletResponse response,
                                  BindingResult bindingResult) throws Exception {
        addParentsValidator.validate(comm, bindingResult);

        AddParentsCommand command = (AddParentsCommand) comm;
        TargetGroup target = getEditorContext(request).getTargetGroup();

        if (AddParentsCommand.ACTION_ADD_PARENTS.equals(command.getActionCmd())) {
            List<GroupMemberDTO> parents = getParents(request);
            GroupMemberDTO newDTO = null;
            if (command.getParentOids() != null && command.getParentOids().length == 1) {
                newDTO = targetManager.createGroupMemberDTO(target, command.getParentOids()[0]);
                newDTO.setSaveState(SAVE_STATE.NEW);

                if (parents.contains(newDTO)) {
                    // Trying to add a duplicate.
                    String name = newDTO.getParentName();
                    bindingResult.reject("target.error.duplicate_parent", new Object[]{name}, "This target is already in this group");
                }
            } else {
                bindingResult.reject("groups.bindingResult.addparents.must_select", null, "You must select a parent group");
            }

            if (bindingResult.hasErrors()) {
                return doSearch(request, response, comm, bindingResult);
            } else {
                Tab generalTab = groupsController.getTabConfig().getTabByID("GENERAL");
                TabbedModelAndView tmav = generalTab.getTabHandler().preProcessNextTab(groupsController, generalTab, request, response, command, bindingResult);
                tmav.getTabStatus().setCurrentTab(generalTab);
                return tmav;
            }
        } else if (AddParentsCommand.ACTION_CANCEL.equals(command.getActionCmd())) {
            // Go back to the Members tab on the groups controller.
            Tab generalTab = groupsController.getTabConfig().getTabByID("GENERAL");
            TabbedModelAndView tmav = generalTab.getTabHandler().preProcessNextTab(groupsController, generalTab, request, response, command, bindingResult);
            tmav.getTabStatus().setCurrentTab(generalTab);
            return tmav;
        } else {
            return doSearch(request, response, comm, bindingResult);
        }
    }

    /**
     * Perform the search for Group members.
     */
    private ModelAndView doSearch(HttpServletRequest request, HttpServletResponse response, Object comm,
                                  BindingResult bindingResult) {

        // get value of page size cookie
        String currentPageSize = CookieUtils.getPageSize(request);

        AddParentsCommand command = (AddParentsCommand) comm;

        if (command.getSearch() == null) {
            command.setSearch("");
            command.setSelectedPageSize(currentPageSize);
        }

        Pagination results = null;
        if (command.getSelectedPageSize().equals(currentPageSize)) {
            // user has left the page size unchanged..
            results = targetManager.getSubGroupParentDTOs(command.getSearch() + "%", command.getPageNumber(), Integer.parseInt(command.getSelectedPageSize()));
        } else {
            // user has selected a new page size, so reset to first page..
            results = targetManager.getSubGroupParentDTOs(command.getSearch() + "%", 0, Integer.parseInt(command.getSelectedPageSize()));
            // ..then update the page size cookie
            CookieUtils.setPageSize(response, command.getSelectedPageSize());
        }

        ModelAndView mav = new ModelAndView("group-add-parents");
        mav.addObject("page", results);
        mav.addObject(Constants.GBL_CMD_DATA, command);
        if (bindingResult.hasErrors()) {
            mav.addObject(Constants.GBL_ERRORS, bindingResult);
        }
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
