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
package org.webcurator.ui.site.controller;

import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.util.AuthUtil;
import org.webcurator.domain.model.core.Annotation;
import org.webcurator.domain.model.core.AuthorisingAgent;
import org.webcurator.domain.model.core.BusinessObjectFactory;
import org.webcurator.domain.model.core.Permission;
import org.webcurator.domain.model.core.PermissionExclusion;
import org.webcurator.domain.model.core.Site;
import org.webcurator.common.ui.Constants;
import org.webcurator.ui.site.SiteEditorContext;
import org.webcurator.ui.site.command.SitePermissionCommand;
import org.webcurator.ui.site.editor.EditorContextObjectEditor;
import org.webcurator.ui.site.editor.UrlPatternCollectionEditor;
import org.webcurator.common.util.DateUtils;
import org.webcurator.ui.site.validator.SitePermissionValidator;
import org.webcurator.ui.util.Tab;
import org.webcurator.common.util.Utils;
import org.webcurator.ui.util.TabbedController.TabbedModelAndView;

/**
 * The Controller for managing the creation and editing of a
 * harvest authorisations permissions.
 *
 * @author nwaight
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class SitePermissionController {
    /**
     * The SiteController that this is part of
     */
    @Autowired
    private SiteController siteController;

    /**
     * BusinessObjectFactory
     */
    @Autowired
    private BusinessObjectFactory businessObjectFactory;

    /**
     * The list of access statuses
     */
    private List<String> accessStatusList;

    @Autowired
    private SitePermissionValidator sitePermissionValidator;

    /**
     * Construct a new Controller. Sets the command class.
     */
    public SitePermissionController() {
    }


    /**
     * Get the editor context for the Site.
     *
     * @param req The HttpServletRequest object.
     * @return The editor context.
     */
    public SiteEditorContext getEditorContext(HttpServletRequest req) {
        SiteEditorContext ctx = (SiteEditorContext) req.getSession().getAttribute("siteEditorContext");
        if (ctx == null) {
            throw new IllegalStateException("siteEditorContext not yet bound to the session");
        }

        return ctx;
    }


    /**
     * Initialise some special binders for this command. (Overrides Spring
     * method).
     *
     * @param request The HttpServletRequest.
     * @param binder  The binder.
     */
    @InitBinder
    public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        NumberFormat nf = NumberFormat.getInstance(request.getLocale());

        // Register the binders.
        binder.registerCustomEditor(Long.class, "selectedPermission", new CustomNumberEditor(Long.class, nf, true));
        binder.registerCustomEditor(java.util.Date.class, "startDate", DateUtils.get().getFullDateEditor(true));
        binder.registerCustomEditor(java.util.Date.class, "endDate", DateUtils.get().getFullDateEditor(true));
        binder.registerCustomEditor(java.util.Date.class, "openAccessDate", DateUtils.get().getFullDateEditor(true));

        // If the session model is available, we want to register the Permission's
        // authorising agency editor.
        if (getEditorContext(request) != null) {
            binder.registerCustomEditor(AuthorisingAgent.class, "authorisingAgent", new EditorContextObjectEditor(getEditorContext(request), AuthorisingAgent.class));
            binder.registerCustomEditor(Set.class, "urls", new UrlPatternCollectionEditor(Set.class, true, getEditorContext(request)));
            binder.registerCustomEditor(Integer.class, "deleteExclusionIndex", new CustomNumberEditor(Integer.class, true));
        }
    }


    /**
     * Handle the cancel logic.
     *
     * @param req           The HttpServletRequest.
     * @param resp          The HttpServletResponse.
     * @param command       The SitePermissionCommand object.
     * @param bindingResult The Spring bindingResult.
     * @return The next view to display.
     */
    @SuppressWarnings("unchecked")
    private ModelAndView handleCancel(HttpServletRequest req, HttpServletResponse resp, SitePermissionCommand command,
                                      BindingResult bindingResult) {
        // Reset the permissions.
        SiteEditorContext ctx = getEditorContext(req);
        Permission p = (Permission) ctx.getObject(Permission.class, command.getIdentity());
        List<Annotation> oldPermissionAnnotations = (List<Annotation>) req.getSession().getAttribute("oldPermissionAnnotations");
        p.setAnnotations(oldPermissionAnnotations);
        req.getSession().removeAttribute("oldPermissionAnnotations");

        // Go back to the permissions tab.
        Tab membersTab = siteController.getTabConfig().getTabByID("PERMISSIONS");
        TabbedModelAndView tmav = membersTab.getTabHandler().preProcessNextTab(siteController, membersTab, req, resp, command, bindingResult);
        tmav.getTabStatus().setCurrentTab(membersTab);
        return tmav;
    }


    /**
     * Handle the save logic.
     *
     * @param req           The HttpServletRequest.
     * @param resp          The HttpServletResponse.
     * @param command       The SitePermissionCommand object.
     * @param bindingResult The Spring bindingResult.
     * @return The next view to display.
     */
    private ModelAndView handleSave(HttpServletRequest req, HttpServletResponse resp, SitePermissionCommand command,
                                    BindingResult bindingResult) {
        SiteEditorContext ctx = getEditorContext(req);

        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView();
            mav.addObject(Constants.GBL_CMD_DATA, bindingResult.getTarget());
            mav.addObject(Constants.GBL_ERRORS, bindingResult);
            mav.addObject("urls", ctx.getSortedUrlPatterns());
            mav.addObject("agents", ctx.getSortedAuthAgents());
            mav.addObject("permissionEditMode", true);
            mav.addObject("accessStatusList", accessStatusList);
            mav.addObject("permission", ctx.getObject(Permission.class, command.getIdentity()));

            mav.setViewName(Constants.VIEW_SITE_PERMISSIONS);
            return mav;
        }

        Permission permission;

        if (Utils.isEmpty(command.getIdentity())) {
            permission = businessObjectFactory.newPermission(ctx.getSite());
            ctx.putObject(permission);
            updateBusinessModel(ctx.getSite(), command, permission);
            ctx.getSite().addPermission(permission);
            throw new WCTRuntimeException("Permission object doesn't have identity set");
        } else {
            permission = (Permission) ctx.getObject(Permission.class, command.getSelectedPermission());
            updateBusinessModel(ctx.getSite(), command, permission);
            permission.setDirty(true);
            ctx.getSite().addPermission(permission);
        }

        // Remove the List of old annotations.
        req.getSession().removeAttribute("oldPermissionAnnotations");

        // Go back to the list of permissions
        Tab membersTab = siteController.getTabConfig().getTabByID("PERMISSIONS");
        TabbedModelAndView tmav = membersTab.getTabHandler().preProcessNextTab(siteController, membersTab, req, resp, command, bindingResult);
        tmav.getTabStatus().setCurrentTab(membersTab);

        return tmav;
    }


    /**
     * Handle the add annotation logic.
     *
     * @param req           The HttpServletRequest.
     * @param command       The SitePermissionCommand object.
     * @param bindingResult The Spring bindingResult.
     * @return The next view to display.
     */
    private ModelAndView handleAnnotation(HttpServletRequest req, SitePermissionCommand command,
                                          BindingResult bindingResult) {
        SiteEditorContext ctx = getEditorContext(req);
        Permission permission = (Permission) ctx.getObject(Permission.class, command.getIdentity());

        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView();
            mav.addObject(Constants.GBL_CMD_DATA, bindingResult.getTarget());
            mav.addObject(Constants.GBL_ERRORS, bindingResult);
            mav.addObject("urls", ctx.getSortedUrlPatterns());
            mav.addObject("agents", ctx.getSortedAuthAgents());
            mav.addObject("permissionEditMode", true);
            mav.addObject("accessStatusList", accessStatusList);
            mav.addObject("permission", ctx.getObject(Permission.class, command.getIdentity()));

            mav.setViewName(Constants.VIEW_SITE_PERMISSIONS);
            return mav;
        } else {
            if (command.isAction(SitePermissionCommand.ACTION_ADD_NOTE)) {
                // Add the annotation
                permission.addAnnotation(new Annotation(new Date(), command.getNote(), AuthUtil.getRemoteUserObject(), null, null, false));
            } else if (command.isAction(SitePermissionCommand.ACTION_MODIFY_NOTE)) {
                // Modify the annotation
                Annotation annotation = permission.getAnnotation(command.getNoteIndex());
                if (annotation != null &&
                        annotation.getUser().equals(AuthUtil.getRemoteUserObject())) {
                    annotation.setDate(new Date());
                    annotation.setNote(command.getNote());
                }
            } else if (command.isAction(SitePermissionCommand.ACTION_DELETE_NOTE)) {
                // Delete the annotations
                Annotation annotation = permission.getAnnotation(command.getNoteIndex());
                if (annotation != null &&
                        annotation.getUser().equals(AuthUtil.getRemoteUserObject())) {
                    permission.deleteAnnotation(command.getNoteIndex());
                }
            }

            //make sure annotations are sorted
            permission.sortAnnotations();

            ModelAndView mav = new ModelAndView();
            mav.addObject(Constants.GBL_CMD_DATA, bindingResult.getTarget());
            if(bindingResult.hasErrors()){mav.addObject(Constants.GBL_ERRORS, bindingResult);}
            mav.addObject("urls", ctx.getSortedUrlPatterns());
            mav.addObject("agents", ctx.getSortedAuthAgents());
            mav.addObject("permissionEditMode", true);
            mav.addObject("accessStatusList", accessStatusList);
            mav.addObject("permission", permission);

            mav.setViewName(Constants.VIEW_SITE_PERMISSIONS);
            return mav;
        }
    }


    /**
     * Handle the add exlusion logic.
     *
     * @param req           The HttpServletRequest.
     * @param command       The SitePermissionCommand object.
     * @param bindingResult The Spring bindingResult.
     * @return The next view to display.
     */
    private ModelAndView handleAddExclusion(HttpServletRequest req, SitePermissionCommand command,
                                            BindingResult bindingResult) {
        SiteEditorContext ctx = getEditorContext(req);
        Permission permission = (Permission) ctx.getObject(Permission.class, command.getIdentity());

        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView();
            mav.addObject(Constants.GBL_CMD_DATA, bindingResult.getTarget());
            mav.addObject(Constants.GBL_ERRORS, bindingResult);
            mav.addObject("urls", ctx.getSortedUrlPatterns());
            mav.addObject("agents", ctx.getSortedAuthAgents());
            mav.addObject("permissionEditMode", true);
            mav.addObject("accessStatusList", accessStatusList);
            mav.addObject("permission", ctx.getObject(Permission.class, command.getIdentity()));

            mav.setViewName(Constants.VIEW_SITE_PERMISSIONS);
            return mav;
        } else {
            permission.getExclusions().add(new PermissionExclusion(command.getExclusionUrl(), command.getExclusionReason()));

            ModelAndView mav = new ModelAndView();
            mav.addObject(Constants.GBL_CMD_DATA, bindingResult.getTarget());
            if(bindingResult.hasErrors()){mav.addObject(Constants.GBL_ERRORS, bindingResult);}
            mav.addObject("urls", ctx.getSortedUrlPatterns());
            mav.addObject("agents", ctx.getSortedAuthAgents());
            mav.addObject("permissionEditMode", true);
            mav.addObject("accessStatusList", accessStatusList);
            mav.addObject("permission", permission);

            mav.setViewName(Constants.VIEW_SITE_PERMISSIONS);
            return mav;
        }
    }

    /**
     * Handle the delete exlusion logic.
     *
     * @param req           The HttpServletRequest.
     * @param command       The SitePermissionCommand object.
     * @param bindingResult The Spring bindingResult.
     * @return The next view to display.
     */
    private ModelAndView handleDeleteExclusion(HttpServletRequest req, SitePermissionCommand command,
                                               BindingResult bindingResult) {
        SiteEditorContext ctx = getEditorContext(req);
        Permission permission = (Permission) ctx.getObject(Permission.class, command.getIdentity());

        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView();
            mav.addObject(Constants.GBL_CMD_DATA, bindingResult.getTarget());
            mav.addObject(Constants.GBL_ERRORS, bindingResult);
            mav.addObject("urls", ctx.getSortedUrlPatterns());
            mav.addObject("agents", ctx.getSortedAuthAgents());
            mav.addObject("permissionEditMode", true);
            mav.addObject("accessStatusList", accessStatusList);
            mav.addObject("permission", ctx.getObject(Permission.class, command.getIdentity()));

            mav.setViewName(Constants.VIEW_SITE_PERMISSIONS);
            return mav;
        } else {
            permission.getExclusions().remove((int) command.getDeleteExclusionIndex());

            ModelAndView mav = new ModelAndView();
            mav.addObject(Constants.GBL_CMD_DATA, bindingResult.getTarget());
            if(bindingResult.hasErrors()){mav.addObject(Constants.GBL_ERRORS, bindingResult);}
            mav.addObject("urls", ctx.getSortedUrlPatterns());
            mav.addObject("agents", ctx.getSortedAuthAgents());
            mav.addObject("permissionEditMode", true);
            mav.addObject("accessStatusList", accessStatusList);
            mav.addObject("permission", permission);

            mav.setViewName(Constants.VIEW_SITE_PERMISSIONS);
            return mav;
        }
    }


    /**
     * Handle the request by sending it to the appropriate sub-handler.
     *
     * @param req           The HttpServletRequest object.
     * @param resp          The HttpServletResponse object.
     * @param command          The Spring command object.
     * @param bindingResult The Spring bindingResult object.
     */
    @RequestMapping(value = "/curator/site/permissions.html", method = {RequestMethod.GET, RequestMethod.POST})
    protected ModelAndView handle(HttpServletRequest req, HttpServletResponse resp, SitePermissionCommand command, BindingResult bindingResult) throws Exception {
        sitePermissionValidator.validate(command, bindingResult);
        if (command.isAction(SitePermissionCommand.ACTION_CANCEL)) {
            return handleCancel(req, resp, command, bindingResult);
        } else if (command.isAction(SitePermissionCommand.ACTION_SAVE)) {
            return handleSave(req, resp, command, bindingResult);
        } else if (command.isAction(SitePermissionCommand.ACTION_ADD_NOTE) ||
                command.isAction(SitePermissionCommand.ACTION_MODIFY_NOTE) ||
                command.isAction(SitePermissionCommand.ACTION_DELETE_NOTE)) {
            return handleAnnotation(req, command, bindingResult);
        } else if (command.isAction(SitePermissionCommand.ACTION_ADD_EXCLUSION)) {
            return handleAddExclusion(req, command, bindingResult);
        } else if (command.isAction(SitePermissionCommand.ACTION_DELETE_EXCLUSION)) {
            return handleDeleteExclusion(req, command, bindingResult);
        } else {
            throw new WCTRuntimeException("Unknown ActionCmd");
        }
    }


    /**
     * Update the business object.
     *
     * @param aSite       The Site the permission belongs to.
     * @param aCommand    The command object to update from.
     * @param aPermission The permission object to update.
     */
    private void updateBusinessModel(Site aSite, SitePermissionCommand aCommand, Permission aPermission) {
        aPermission.setSite(aSite);
        aPermission.setQuickPick(aCommand.isQuickPick());
        aPermission.setDisplayName(aCommand.getDisplayName());
        aPermission.setStartDate(aCommand.getStartDate());
        aPermission.setEndDate(aCommand.getEndDate());
        aPermission.setStatus(aCommand.getStatus());
        aPermission.setAuthorisingAgent(aCommand.getAuthorisingAgent());
        aPermission.setSpecialRequirements(aCommand.getSpecialRequirements());
        aPermission.adjustUrlPatternSet(aCommand.getUrls());
        aPermission.setCreateSeekPermissionTask(aCommand.isCreateSeekPermissionTask());
        aPermission.setCopyrightStatement(aCommand.getCopyrightStatement());
        aPermission.setCopyrightUrl(aCommand.getCopyrightUrl());
        aPermission.setOpenAccessDate(aCommand.getOpenAccessDate());
        aPermission.setAccessStatus(aCommand.getAccessStatus());
        aPermission.setFileReference(aCommand.getFileReference());
        aPermission.setAuthResponse(aCommand.getAuthResponse());
    }


    /**
     * @param businessObjectFactory the businessObjectFactory to set
     */
    public void setBusinessObjectFactory(BusinessObjectFactory businessObjectFactory) {
        this.businessObjectFactory = businessObjectFactory;
    }

    /**
     * @param siteController the siteController to set
     */
    public void setSiteController(SiteController siteController) {
        this.siteController = siteController;
    }

    /**
     * Spring setter method for the list of Access Status values.
     *
     * @param accessStatusList The accessStatusList to set.
     */
    public void setAccessStatusList(List<String> accessStatusList) {
        this.accessStatusList = accessStatusList;
    }

}
