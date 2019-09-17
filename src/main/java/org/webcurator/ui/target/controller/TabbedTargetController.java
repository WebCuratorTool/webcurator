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

import java.text.NumberFormat;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.targets.TargetManager;
import org.webcurator.domain.model.auth.Privilege;
import org.webcurator.domain.model.core.BusinessObjectFactory;
import org.webcurator.domain.model.core.Target;
import org.webcurator.common.ui.Constants;
import org.webcurator.common.ui.target.TargetEditorContext;
import org.webcurator.ui.target.command.TargetAnnotationCommand;
import org.webcurator.ui.target.command.TargetDefaultCommand;
import org.webcurator.ui.target.command.TargetSchedulesCommand;
import org.webcurator.ui.util.Tab;
import org.webcurator.ui.util.TabConfig;
import org.webcurator.ui.util.TabbedController;

/**
 * The controller for all Target tabs.
 * @author bbeaumont
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class TabbedTargetController extends TabbedController {
    @Autowired
	private TargetManager targetManager;

	public static final String EDITOR_CONTEXT = "targetEditorContext";
    @Autowired
	private BusinessObjectFactory businessObjectFactory;
    @Autowired
    @Qualifier("targetSearchController")
	private TargetSearchController searchController;
    @Autowired
	private MessageSource messageSource;
    @Autowired
	private AuthorityManager authorityManager;

    @Autowired
    private ApplicationContext context;

    @PostConstruct
    protected void init() {
        setDefaultCommandClass(TargetDefaultCommand.class);
        setTabConfig((TabConfig) context.getBean("targetTabConfig"));
    }

	@Override
	protected void switchToEditMode(HttpServletRequest req) {
		getEditorContext(req).setEditMode(true);
		bindEditorContext(req, getEditorContext(req));
	};

	/* (non-Javadoc)
	 * @see org.webcurator.ui.util.TabbedController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);

        NumberFormat nf = NumberFormat.getInstance(request.getLocale());
        binder.registerCustomEditor(Long.class, "targetOid", new CustomNumberEditor(Long.class, nf, true));
	}


	public void bindEditorContext(HttpServletRequest req, TargetEditorContext context) {
		req.getSession().setAttribute(TabbedTargetController.EDITOR_CONTEXT, context);
		req.getSession().setAttribute(Constants.GBL_SESS_EDIT_MODE, context.isEditMode());
		req.getSession().setAttribute(Constants.GBL_SESS_CAN_EDIT, context.isCanEdit());
	}

	public void unbindEditorContext(HttpServletRequest req) {
		req.getSession().removeAttribute(TabbedTargetController.EDITOR_CONTEXT);
		req.getSession().removeAttribute(Constants.GBL_SESS_EDIT_MODE);
		req.getSession().removeAttribute(Constants.GBL_SESS_CAN_EDIT);
	}

	public TargetEditorContext getEditorContext(HttpServletRequest req) {
		TargetEditorContext ctx = (TargetEditorContext) req.getSession().getAttribute(TabbedTargetController.EDITOR_CONTEXT);
		if( ctx == null) {
			throw new IllegalStateException("targetEditorContext not yet bound to the session");
		}

		return ctx;
	}

	@Override
	protected ModelAndView processSave(Tab currentTab, HttpServletRequest req, HttpServletResponse res, Object comm,
                                       BindingResult bindingResult) {

		// Load the session model.
		TargetEditorContext ctx = getEditorContext(req);
		Target target = ctx.getTarget();
		TargetSchedulesCommand cmd = null;

		if(comm instanceof TargetSchedulesCommand)
		{
			cmd = (TargetSchedulesCommand)comm;
		}

		if(!targetManager.isNameOk(target)) {
			bindingResult.reject("target.bindingResult.duplicatename");
		}

		if(target.getProfile()==null) {
			bindingResult.reject("target.bindingResult.profile.missing");
		}

		if( ( target.getState() == Target.STATE_APPROVED ||
			  target.getState() == Target.STATE_COMPLETED   ) && !target.isApprovable()) {
			bindingResult.reject("target.bindingResult.notapprovable");
		}

		if(cmd != null && cmd.isHarvestNowSet() && target.getState() != Target.STATE_APPROVED && target.getState() != Target.STATE_COMPLETED) {
			bindingResult.reject("target.error.notapproved");
		}

		//TODO if HarvestNow is set and it's completed, ensure the user has permission to do this
		if(cmd != null && cmd.isHarvestNowSet() && target.getState()==Target.STATE_COMPLETED) {
			if(!authorityManager.hasPrivilege(target, Privilege.APPROVE_TARGET) || !authorityManager.hasPrivilege(target, Privilege.REINSTATE_TARGET)) {
				bindingResult.reject("target.error.approve.denied");
			}
		}

		if(bindingResult.hasErrors()) {
			TabbedModelAndView tmav = currentTab.getTabHandler().preProcessNextTab(this, currentTab, req, res, comm ,bindingResult);
			tmav.getTabStatus().setCurrentTab(currentTab);
			tmav.addObject(Constants.GBL_CMD_DATA,  comm);
			tmav.addObject(Constants.GBL_ERRORS, bindingResult);
			return tmav;
		}
		else {
			if(cmd != null)
			{
				if (cmd.isHarvestNowSet()) {
					target.setHarvestNow(true);
					if (target.getState() == Target.STATE_COMPLETED) {
						target.changeState(Target.STATE_APPROVED);
					}
				}
				target.setAllowOptimize(cmd.isAllowOptimize());
			}

			int beforeSaveState = target.getState();
			targetManager.save(target, ctx.getParents());
			int afterSaveState = target.getState();

			if(comm instanceof TargetAnnotationCommand) {
				TargetAnnotationCommand tac = (TargetAnnotationCommand)comm;
				if(tac.isAction(TargetAnnotationCommand.ACTION_ADD_NOTE)) {
					TabbedModelAndView tmav = currentTab.getTabHandler().preProcessNextTab(this, currentTab, req, res, comm ,bindingResult);
					tmav.getTabStatus().setCurrentTab(currentTab);
					tmav.addObject(Constants.GBL_MESSAGES, messageSource.getMessage("target.saved", new Object[] { target.getName() }, Locale.getDefault()));
					tmav.addObject(Constants.GBL_CMD_DATA,  comm);

					// Refresh the context editor cache to use any new oids as the keys.
					ctx.refreshCachedSchedules();

					return tmav;
				}
			}

			ModelAndView mav = searchController.prepareSearchView(req, res);

			if( beforeSaveState == afterSaveState) {
				if (target.isHarvestNow() && afterSaveState == Target.STATE_APPROVED) {
					mav.addObject(Constants.GBL_MESSAGES, messageSource.getMessage("target.saved.schedulednow", new Object[] { target.getName() }, Locale.getDefault()));
				}
				else {
					mav.addObject(Constants.GBL_MESSAGES, messageSource.getMessage("target.saved", new Object[] { target.getName() }, Locale.getDefault()));
				}
			}
			else if(afterSaveState == Target.STATE_NOMINATED) {
				mav.addObject(Constants.GBL_MESSAGES, messageSource.getMessage("target.saved.nominated", new Object[] { target.getName() }, Locale.getDefault()));
			}
			else if(afterSaveState == Target.STATE_REINSTATED) {
				mav.addObject(Constants.GBL_MESSAGES, messageSource.getMessage("target.saved.reinstated", new Object[] { target.getName() }, Locale.getDefault()));
			}


			// Remove from the session and go back to search.
			unbindEditorContext(req);

			return mav;
		}
	}

	@Override
	protected ModelAndView processCancel(Tab currentTab, HttpServletRequest req, HttpServletResponse res, Object comm,
                                         BindingResult bindingResult) {

		// Remove from the session and go back to search.
		req.getSession().removeAttribute("targetSessionModel");
		return new ModelAndView("redirect:/curator/target/search.html");
	}

	@Override
	protected ModelAndView processInitial(HttpServletRequest req, HttpServletResponse res, Object comm,
                                          BindingResult bindingResult) {
		// TODO Auto-generated method stub
		return null;
	}

	private static Log log = LogFactory.getLog(TabbedTargetController.class);

	@Override
	protected ModelAndView showForm(HttpServletRequest req, HttpServletResponse res, Object comm,
                                    BindingResult bindingResult) throws Exception {
		TargetDefaultCommand command = (TargetDefaultCommand) comm;
		if( command != null && command.getTargetOid() != null) {
			Target aTarget = targetManager.load(command.getTargetOid(), true);

			if(command.isCopyMode()) {
				Target copy = targetManager.copy(aTarget);
				aTarget = copy;
			}

			TargetEditorContext ctx = new TargetEditorContext(targetManager, aTarget, command.isEditMode());


			//Code to display edit button (in layouts\tabbed-new.jsp) driven by session vars
			ctx.setCanEdit(false);
			if (!command.isEditMode())
			{
				if (authorityManager.hasPrivilege(aTarget,Privilege.MODIFY_TARGET))
				{
					ctx.setCanEdit(true);
				}
			}


			bindEditorContext(req, ctx);
		}
		else {
			Target aTarget = businessObjectFactory.newTarget();
			TargetEditorContext ctx = new TargetEditorContext(targetManager, aTarget, true);

			bindEditorContext(req, ctx);
		}

		Tab general = getTabConfig().getTabByID("GENERAL");
		TabbedModelAndView tmav = general.getTabHandler().preProcessNextTab(this, general, req, res, null, bindingResult);
		tmav.getTabStatus().setCurrentTab(general);

		return tmav;
	}


	/**
	 * @param targetManager The targetManager to set.
	 */
	public void setTargetManager(TargetManager targetManager) {
		this.targetManager = targetManager;
	}

	/**
	 * @param businessObjectFactory The businessObjectFactory to set.
	 */
	public void setBusinessObjectFactory(BusinessObjectFactory businessObjectFactory) {
		this.businessObjectFactory = businessObjectFactory;
	}

	/**
	 * @param searchController The searchController to set.
	 */
	public void setSearchController(TargetSearchController searchController) {
		this.searchController = searchController;
	}


	/**
	 * @param messageSource The messageSource to set.
	 */
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * @param authorityManager the authorityManager to set
	 */
	public void setAuthorityManager(AuthorityManager authorityManager) {
		this.authorityManager = authorityManager;
	}

}
