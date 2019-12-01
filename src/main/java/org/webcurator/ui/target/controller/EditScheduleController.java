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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.core.util.DateUtils;
import org.webcurator.domain.HeatmapDAO;
import org.webcurator.domain.Pagination;
import org.webcurator.domain.SchedulePatternFactory;
import org.webcurator.domain.TargetInstanceCriteria;
import org.webcurator.domain.model.core.BusinessObjectFactory;
import org.webcurator.domain.model.core.HeatmapConfig;
import org.webcurator.domain.model.core.Schedule;
import org.webcurator.domain.model.core.SchedulePattern;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.domain.model.dto.HeatmapConfigDTO;
import org.webcurator.ui.common.CommonViews;
import org.webcurator.common.ui.Constants;
import org.webcurator.common.ui.target.AbstractTargetEditorContext;
import org.webcurator.ui.target.command.TargetSchedulesCommand;
import org.webcurator.ui.target.command.Time;
import org.webcurator.ui.target.command.TimeEditor;
import org.webcurator.ui.target.validator.TargetSchedulesValidator;
import org.webcurator.ui.util.Tab;
import org.webcurator.ui.util.TabbedController;
import org.webcurator.ui.util.TabbedController.TabbedModelAndView;
import org.webcurator.common.util.Utils;

/**
 * The controller for editing a schedule.
 * @author bbeaumont
 */
public class EditScheduleController {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

	@Autowired
	private SchedulePatternFactory patternFactory;
	private TabbedController targetController;
	private String scheduleEditPrivilege = null;
	@Autowired
	private AuthorityManager authorityManager;
	private String contextSessionKey = null;
	@Autowired
	private BusinessObjectFactory businessObjectFactory;
	private String viewPrefix = null;
	@Autowired
	private HeatmapDAO heatmapConfigDao;
	@Autowired
	private TargetInstanceManager targetInstanceManager;

	@Autowired
    @Qualifier("targetSchedulesValidator")
	private TargetSchedulesValidator validator;

    public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		// Determine the necessary formats.
        NumberFormat nf = NumberFormat.getInstance(request.getLocale());

        // Register the binders.
        binder.registerCustomEditor(Long.class, new CustomNumberEditor(Long.class, nf, true));
        binder.registerCustomEditor(Integer.class, "scheduleType", new CustomNumberEditor(Integer.class, nf, true));
        binder.registerCustomEditor(java.util.Date.class, org.webcurator.common.util.DateUtils.get().getFullDateEditor(true));
        binder.registerCustomEditor(Time.class, new TimeEditor(false));
    }

	public AbstractTargetEditorContext getEditorContext(HttpServletRequest req) {
		return (AbstractTargetEditorContext) req.getSession().getAttribute(contextSessionKey);
	}

	protected ModelAndView handle(@Validated @ModelAttribute("targetSchedulesCommand") TargetSchedulesCommand command,
                                  HttpServletRequest request, HttpServletResponse response, BindingResult bindingResult)
            throws Exception {
		if(TargetSchedulesCommand.ACTION_EDIT.equals(command.getActionCmd())) {
			return handleEdit(request, command, bindingResult);
		}
		else if(TargetSchedulesCommand.ACTION_NEW.equals(command.getActionCmd())) {
			return handleNew(request, bindingResult);
		}
		else if(TargetSchedulesCommand.ACTION_TEST.equals(command.getActionCmd())) {
			return handleTest(command, bindingResult);
		}
		else if(TargetSchedulesCommand.ACTION_SAVE.equals(command.getActionCmd())) {
			return handleSave(request, response, command, bindingResult);
		}
		else if(TargetSchedulesCommand.ACTION_CANCEL.equals(command.getActionCmd())) {
			return handleCancel(request, response, command, bindingResult);
		}
		else if(TargetSchedulesCommand.ACTION_VIEW.equals(command.getActionCmd())) {
			return handleView(request, command, bindingResult);
		}
		else if(TargetSchedulesCommand.ACTION_REFRESH.equals(command.getActionCmd())) {
			return getEditView(command, bindingResult);
		}
		else {
			return handleCancel(request, response, command, bindingResult);
		}
	}

	protected ModelAndView handleNew(HttpServletRequest request, BindingResult bindingResult) {
		if( authorityManager.hasPrivilege( getEditorContext(request).getAbstractTarget(), scheduleEditPrivilege)) {
			TargetSchedulesCommand newCommand = new TargetSchedulesCommand();

			newCommand.setHeatMap(buildHeatMap());
			newCommand.setHeatMapThresholds(buildHeatMapThresholds());
			return getEditView(newCommand, bindingResult);
		}
		else {
			return CommonViews.AUTHORISATION_FAILURE;
		}
	}

	private Map<String, HeatmapConfigDTO> buildHeatMapThresholds() {
		Map<String, HeatmapConfigDTO> result = new LinkedHashMap<String, HeatmapConfigDTO>();
		Map<String, HeatmapConfig> heatmapConfigs = heatmapConfigDao.getHeatmapConfigurations();
		for(Entry<String, HeatmapConfig> configEntry:heatmapConfigs.entrySet()) {
			HeatmapConfig config = configEntry.getValue();
			result.put(config.getName(), new HeatmapConfigDTO(config.getOid(), config.getName(), config.getDisplayName(), config.getColor(), config.getThresholdLowest()));
		}
		return result;
	}

	private Map<String, Integer> buildHeatMap() {
		TargetInstanceCriteria aCriteria = new TargetInstanceCriteria();
		aCriteria.setFrom(new Date());
		Pagination queuePagination = targetInstanceManager.search(aCriteria , 0, 100);
		List queue = queuePagination.getList();
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		List<Schedule> processedSchedules = new ArrayList<Schedule>();
		for(Object queuedInstance:queue) {
			TargetInstance instance = (TargetInstance)queuedInstance;
			Schedule schedule = instance.getSchedule();
			boolean alreadyProcessed = false;
			if(schedule==null) {
				//Include ad-hoc TIs
				putHeatMapResult(result, instance.getScheduledTime());
			} else {
				//Do not process schedules already included (as there may be multiple TIs per schedule
				//Can't use contains() because it only comprase the cron expression.
				for(Schedule processed:processedSchedules) {
					if(processed.getOid().equals(schedule.getOid())) {
						alreadyProcessed = true;
						break;
					}
				}
				if(alreadyProcessed) {
					continue;
				}
				System.out.println(schedule.getOid());
				processedSchedules.add(schedule);
				Date nextExecutionDate = schedule.getNextExecutionDate();
				for(int i=0;i<100;i++) {
					if(nextExecutionDate==null) {
						break;
					}
					putHeatMapResult(result, nextExecutionDate);
					nextExecutionDate = schedule.getNextExecutionDate(nextExecutionDate);
				}
			}
		}
		return result;
	}


	private void putHeatMapResult(HashMap<String, Integer> result, Date date) {
		String dateKey = dateFormat.format(date);
		if(result.containsKey(dateKey)) {
			result.put(dateKey, result.get(dateKey)+1);
		} else {
			result.put(dateKey, 1);
		}
	}

	protected ModelAndView handleEdit(HttpServletRequest request, TargetSchedulesCommand command,
                                      BindingResult bindingResult) {
		if( authorityManager.hasPrivilege( getEditorContext(request).getAbstractTarget(), scheduleEditPrivilege)) {
			Schedule aSchedule = (Schedule) getEditorContext(request).getObject(Schedule.class, command.getSelectedItem());
			TargetSchedulesCommand newCommand = TargetSchedulesCommand.buildFromModel(aSchedule);
			newCommand.setHeatMap(buildHeatMap());
			newCommand.setHeatMapThresholds(buildHeatMapThresholds());
			return getEditView(newCommand, bindingResult);
		}
		else {
			return CommonViews.AUTHORISATION_FAILURE;
		}
	}

	protected ModelAndView handleView(HttpServletRequest request, Object comm, BindingResult bindingResult) {
		TargetSchedulesCommand command = (TargetSchedulesCommand) comm;
		Schedule aSchedule = (Schedule) getEditorContext(request).getObject(Schedule.class, command.getSelectedItem());

		TargetSchedulesCommand newCommand = TargetSchedulesCommand.buildFromModel(aSchedule);

		List<Date> testResults = new LinkedList<Date>();

		ModelAndView mav = getEditView(newCommand, bindingResult);
		mav.addObject("testResults", testResults);
		mav.addObject("viewMode", new Boolean(true));

		newCommand.setHeatMap(buildHeatMap());
		newCommand.setHeatMapThresholds(buildHeatMapThresholds());

		if(newCommand.getScheduleType() < 0) {
			mav.addObject("monthOptions", getMonthOptionsByType(newCommand.getScheduleType()));
		}

		try {
			CronExpression expr = new CronExpression(aSchedule.getCronPattern());
			Date d = DateUtils.latestDate(new Date(), newCommand.getStartDate());
			Date nextDate = null;
			for(int i = 0; i<10; i++) {
				nextDate = expr.getNextValidTimeAfter(d);
				if(nextDate == null || newCommand.getEndDate() != null && nextDate.after(newCommand.getEndDate())) {
					break;
				}
				testResults.add(nextDate);
				d = nextDate;
			}
		}
		catch(ParseException ex) {
			ex.printStackTrace();
		}

		return mav;
	}

	public static Map<String,String> getMonthOptionsByType(int scheduleType) {
		switch(scheduleType) {
		case Schedule.TYPE_ANNUALLY:
			return getMonthOptions(12);
		case Schedule.TYPE_HALF_YEARLY:
			return getMonthOptions(6);
		case Schedule.TYPE_QUARTERLY:
			return getMonthOptions(3);
		case Schedule.TYPE_BI_MONTHLY:
			return getMonthOptions(2);
		}

		return null;
	}

	public static Map<String,String> getMonthOptions(int monthsBetween) {
		Map<String,String> options = new LinkedHashMap<String,String>();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM");

		Calendar cal = Calendar.getInstance();
		cal.set(0, 0, 1);

		int occurrences = 12/monthsBetween;

		for(int i=0; i< monthsBetween; i++) {
			cal.set(0, i, 1);
			String key = null;

			if(occurrences > 1) {
				key = (i+1) + "/" + monthsBetween;
			}
			else {
				key = Integer.toString(i+1);
			}

			StringBuffer legend = new StringBuffer();
			for(int j=0;j<occurrences;j++,cal.add(Calendar.MONTH, monthsBetween)) {
				if(legend.length() !=0) {
					legend.append(", ");
				}
				legend.append(sdf.format(cal.getTime()));
			}

			options.put(key,legend.toString());
		}

		return options;
	}

	protected ModelAndView handleTest(TargetSchedulesCommand command, BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			return getEditView(command, bindingResult);
		}
		else {

			List<Date> testResults = new LinkedList<Date>();

			ModelAndView mav = getEditView(command, bindingResult);
			mav.addObject("testResults", testResults);

			try {
				CronExpression expr = new CronExpression(command.getCronExpression());
				Date d = DateUtils.latestDate(new Date(), command.getStartDate());
				Date nextDate = null;
				for(int i = 0; i<10; i++) {
					nextDate = expr.getNextValidTimeAfter(d);
					if(nextDate == null || command.getEndDate() != null && nextDate.after(command.getEndDate())) {
						break;
					}
					testResults.add(nextDate);
					d = nextDate;
				}
			}
			catch(ParseException ex) {
				ex.printStackTrace();
			}

			return mav;
		}
	}

	protected ModelAndView handleSave(HttpServletRequest request, HttpServletResponse response,
                                      TargetSchedulesCommand command, BindingResult bindingResult) {

		if( authorityManager.hasPrivilege( getEditorContext(request).getAbstractTarget(), scheduleEditPrivilege)) {
			if(bindingResult.hasErrors()) {
				return getEditView(command, bindingResult);
			}
			else {
				AbstractTargetEditorContext ctx = getEditorContext(request);

				if( !Utils.isEmpty(command.getSelectedItem())) {
					Schedule aSchedule = (Schedule) ctx.getObject(Schedule.class, command.getSelectedItem());
					ctx.getAbstractTarget().removeSchedule(aSchedule);
				}

				Schedule schedule = null;

				if(command.getScheduleType() <= Schedule.CUSTOM_SCHEDULE) {
			    	schedule = businessObjectFactory.newSchedule(ctx.getAbstractTarget());
			    	schedule.setStartDate(command.getStartDate());
			    	schedule.setEndDate(command.getEndDate());
			    	schedule.setScheduleType(command.getScheduleType());

			    	schedule.setCronPattern(command.getCronExpression());
				}
				else if(command.getScheduleType() > 0){
					SchedulePattern pattern = patternFactory.getPattern(command.getScheduleType());
					schedule = pattern.makeSchedule(businessObjectFactory, ctx.getAbstractTarget(), command.getStartDate(), command.getEndDate());
				}

				ctx.putObject(schedule);
				ctx.getAbstractTarget().addSchedule(schedule);

				return getSchedulesListView(request, response, command, bindingResult);
			}
		}
		else {
			return CommonViews.AUTHORISATION_FAILURE;
		}
	}

	protected ModelAndView handleCancel(HttpServletRequest request, HttpServletResponse response,
                                        TargetSchedulesCommand command, BindingResult bindingResult) {
		return getSchedulesListView(request, response, command, bindingResult);
	}

	private ModelAndView getEditView(TargetSchedulesCommand command, BindingResult bindingResult) {
		ModelAndView mav = new ModelAndView(viewPrefix + "-" + Constants.VIEW_EDIT_SCHEDULE);
		mav.addObject(Constants.GBL_CMD_DATA, command);
		if(bindingResult.hasErrors()){mav.addObject(Constants.GBL_ERRORS, bindingResult);}
		mav.addObject("viewPrefix", viewPrefix);
		mav.addObject("patterns", patternFactory.getPatterns());
		command.setHeatMap(buildHeatMap());
		command.setHeatMapThresholds(buildHeatMapThresholds());

		if(command.getScheduleType() < 0) {
			mav.addObject("monthOptions", getMonthOptionsByType(command.getScheduleType()));
		}

		return mav;
	}

	private ModelAndView getSchedulesListView(HttpServletRequest request, HttpServletResponse response,
                                              TargetSchedulesCommand command, BindingResult bindingResult) {
		Tab currentTab = targetController.getTabConfig().getTabByID("SCHEDULES");
		TargetSchedulesHandler handler = (TargetSchedulesHandler) currentTab.getTabHandler();
		TabbedModelAndView tmav = handler.preProcessNextTab(targetController, currentTab, request, response, command, bindingResult);
		tmav.getTabStatus().setCurrentTab(currentTab);

		return tmav;
	}

	/**
	 * @param patternFactory The patternFactory to set.
	 */
	public void setPatternFactory(SchedulePatternFactory patternFactory) {
		this.patternFactory = patternFactory;
	}

	/**
	 * @param scheduleEditPrivilege The scheduleEditPrivilege to set.
	 */
	public void setScheduleEditPrivilege(String scheduleEditPrivilege) {
		this.scheduleEditPrivilege = scheduleEditPrivilege;
	}

	/**
	 * @param targetController The targetController to set.
	 */
	public void setTargetController(TabbedController targetController) {
		this.targetController = targetController;
	}

	/**
	 * @param authorityManager The authorityManager to set.
	 */
	public void setAuthorityManager(AuthorityManager authorityManager) {
		this.authorityManager = authorityManager;
	}

	/**
	 * @param contextSessionKey The contextSessionKey to set.
	 */
	public void setContextSessionKey(String contextSessionKey) {
		this.contextSessionKey = contextSessionKey;
	}

	/**
	 * @param businessObjectFactory The businessObjectFactory to set.
	 */
	public void setBusinessObjectFactory(BusinessObjectFactory businessObjectFactory) {
		this.businessObjectFactory = businessObjectFactory;
	}

	/**
	 * @param viewPrefix The viewPrefix to set.
	 */
	public void setViewPrefix(String viewPrefix) {
		this.viewPrefix = viewPrefix;
	}


	public TargetInstanceManager getTargetInstanceManager() {
		return targetInstanceManager;
	}


	public void setTargetInstanceManager(TargetInstanceManager targetInstanceManager) {
		this.targetInstanceManager = targetInstanceManager;
	}


	public HeatmapDAO getHeatmapConfigDao() {
		return heatmapConfigDao;
	}


	public void setHeatmapConfigDao(HeatmapDAO heatmapConfigDao) {
		this.heatmapConfigDao = heatmapConfigDao;
	}


}
