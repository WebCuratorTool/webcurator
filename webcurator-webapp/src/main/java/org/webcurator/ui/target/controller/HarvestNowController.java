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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.coordinator.WctCoordinator;
import org.webcurator.domain.TargetInstanceDAO;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;
import org.webcurator.common.ui.Constants;
import org.webcurator.ui.target.command.TargetInstanceCommand;
import org.webcurator.ui.target.validator.HarvestNowValidator;

/**
 * This controller is responsible for allocating a TargetInstance to a
 * Harvest Agent immediatly.
 *
 * @author nwaight
 */
// TODO Is this mapping even used?
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
@RequestMapping("/curator/target/harvest-now.html")
public class HarvestNowController {
    /**
     * The manager to use to access the target instance.
     */
    @Autowired
    private TargetInstanceDAO targetInstanceDAO;
    /**
     * The harvest coordinator for looking at the harvesters.
     */
    @Autowired
    private WctCoordinator wctCoordinator;
    /**
     * the message source.
     */
    @Autowired
    private MessageSource messageSource;
    /**
     * the logger.
     */
    private Log log;

    @Autowired
    private HarvestNowValidator validator;

    /**
     * Constructor to set the command class for this controller.
     */
    public HarvestNowController() {
        super();
        log = LogFactory.getLog(HarvestNowController.class);
    }

    @GetMapping
    protected ModelAndView showForm() throws Exception {
        if (log.isWarnEnabled()) {
            log.warn("the showForm method is not supported in this class.");
        }

        return null;
    }

    @PostMapping
    protected ModelAndView processFormSubmission(@ModelAttribute("targetInstanceCommand") TargetInstanceCommand cmd,
                                                 BindingResult bindingResult, HttpServletRequest aReq)
            throws Exception {
        validator.validate(cmd, bindingResult);

        String targetUrl = "";
        if (cmd.getHarvestResultId() <= 0) {
            targetUrl = Constants.CNTRL_TI_QUEUE;
        } else {
            targetUrl = String.format("curator/target/target-instance.html?targetInstanceId=%d&cmd=edit&init_tab=RESULTS", cmd.getTargetInstanceId());
        }

        if (!cmd.getCmd().equals(TargetInstanceCommand.ACTION_HARVEST)) {
            if (cmd.getHarvestResultId() <= 0) {
                aReq.getSession().removeAttribute(TargetInstanceCommand.SESSION_TI);
            }
            return new ModelAndView("redirect:/" + targetUrl);
        }

        HashMap<String, HarvestAgentStatusDTO> agents = wctCoordinator.getHarvestAgents();
        TargetInstance ti = targetInstanceDAO.load(cmd.getTargetInstanceId());
        String instanceAgency = ti.getOwner().getAgency().getName();

        String key = "";
        HarvestAgentStatusDTO agent = null;
        HashMap<String, HarvestAgentStatusDTO> allowedAgents = new HashMap<String, HarvestAgentStatusDTO>();
        Iterator<String> it = agents.keySet().iterator();
        while (it.hasNext()) {
            key = (String) it.next();
            agent = agents.get(key);
            if (agent.getAllowedAgencies().contains(instanceAgency)
                    || agent.getAllowedAgencies().isEmpty()) {
                allowedAgents.put(key, agent);
            }
        }

        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView();
            mav.addObject(Constants.GBL_CMD_DATA, cmd);
            mav.addObject(TargetInstanceCommand.MDL_AGENTS, allowedAgents);
            mav.addObject(Constants.GBL_ERRORS, bindingResult);
            mav.addObject(TargetInstanceCommand.MDL_INSTANCE, ti);
            mav.setViewName(Constants.VIEW_HARVEST_NOW);

            return mav;
        }

        ModelAndView mav = new ModelAndView("redirect:/" + targetUrl);
        // Get the TargetInstance and the HarvestAgentStatusDTO
        HarvestAgentStatusDTO has = (HarvestAgentStatusDTO) wctCoordinator.getHarvestAgents().get(cmd.getAgent());

        //Is the queue paused?
        if (wctCoordinator.isQueuePaused()) {
            // Display a global message and return to queue
            mav.addObject(Constants.GBL_MESSAGES, messageSource.getMessage("target.instance.queue.paused", new Object[]{ti.getOid()}, Locale.getDefault()));
        } else if (!has.isAcceptTasks()) {
            // Display a global message and return to queue
            mav.addObject(Constants.GBL_MESSAGES, messageSource.getMessage("target.instance.agent.paused", new Object[]{ti.getOid(), has.getName()}, Locale.getDefault()));
            mav.addObject(Constants.GBL_CMD_DATA, cmd);
            mav.addObject(TargetInstanceCommand.MDL_AGENTS, allowedAgents);
            mav.addObject(Constants.GBL_ERRORS, bindingResult);
            mav.addObject(TargetInstanceCommand.MDL_INSTANCE, ti);
            mav.setViewName(Constants.VIEW_HARVEST_NOW);

        } else if (has.getMemoryWarning()) {
            // Display a global message and return to queue
            mav.addObject(Constants.GBL_MESSAGES, messageSource.getMessage("target.instance.agent.notaccept", new Object[]{ti.getOid(), has.getName()}, Locale.getDefault()));
        } else {
            try {
                if (cmd.getHarvestResultId() <= 0) {
                    wctCoordinator.harvest(ti, has);
                } else {
                    HarvestResult hr = targetInstanceDAO.getHarvestResult(cmd.getHarvestResultId());
                    wctCoordinator.patchHarvest(ti, hr, has);
                }
            } catch (HibernateOptimisticLockingFailureException e) {
                ti = targetInstanceDAO.load(ti.getOid());
                if (ti.getState().equals(TargetInstance.STATE_RUNNING)
                        || ti.getState().equals(TargetInstance.STATE_STOPPING)) {
                    // Display a global message and return to queue
                    mav.addObject(Constants.GBL_MESSAGES, messageSource.getMessage("target.instance.run.by.other", new Object[]{ti.getOid()}, Locale.getDefault()));
                }
            }
        }
        return mav;
    }

    public void setTargetInstanceDAO(TargetInstanceDAO targetInstanceDAO) {
        this.targetInstanceDAO = targetInstanceDAO;
    }

    public void setWctCoordinator(WctCoordinator wctCoordinator) {
        this.wctCoordinator = wctCoordinator;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setValidator(HarvestNowValidator validator) {
        this.validator = validator;
    }
}
