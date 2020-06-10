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

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.harvester.coordinator.HarvestLogManager;
import org.webcurator.core.harvester.coordinator.PatchingHarvestLogManager;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.ui.target.command.LogReaderCommand;
import org.webcurator.ui.target.command.LogRetrieverCommand;

/**
 * Controller for retrieving a log file from the WCT core.
 *
 * @author beaumontb
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
@RequestMapping("/curator/target/log-retriever.html")
public class LogRetrieverController {
    @Autowired
    private HarvestLogManager harvestLogManager;
    @Autowired
    private TargetInstanceManager targetInstanceManager;

    @Autowired
    @Qualifier(HarvestResult.PATCH_STAGE_TYPE_NORMAL)
    private PatchingHarvestLogManager patchingHarvestLogManager;

    @Autowired
    @Qualifier(HarvestResult.PATCH_STAGE_TYPE_MODIFYING)
    private PatchingHarvestLogManager patchingHarvestLogManagerModification;

    @Autowired
    @Qualifier(HarvestResult.PATCH_STAGE_TYPE_INDEXING)
    private PatchingHarvestLogManager patchingHarvestLogManagerIndex;


    @GetMapping
    protected ModelAndView handle(@ModelAttribute("logRetrieverCommand") LogRetrieverCommand cmd,
                                  BindingResult bindingResult) throws Exception {
        //Go to patching log reading process
        if (cmd.getPrefix() != null && cmd.getPrefix().length() > 0) {
            return handlePatchingRetrieve(cmd, bindingResult);
        }

        TargetInstance ti = targetInstanceManager.getTargetInstance(cmd.getTargetInstanceOid());

        File f = null;

        try {
            f = harvestLogManager.getLogfile(ti, cmd.getLogFileName());
        } catch (WCTRuntimeException e) {

        }

        AttachmentView v = new AttachmentView(cmd.getLogFileName(), f, true);
        return new ModelAndView(v);
    }

    protected ModelAndView handlePatchingRetrieve(@ModelAttribute("logRetrieverCommand") LogRetrieverCommand cmd,
                                                  BindingResult bindingResult) throws Exception {
        TargetInstance ti = targetInstanceManager.getTargetInstance(cmd.getTargetInstanceOid());
        HarvestResult hr = ti.getHarvestResult(cmd.getHarvestResultNumber());

        PatchingHarvestLogManager logReader = null;
        if (cmd.getPrefix().equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_INDEXING)) {
            logReader = patchingHarvestLogManagerIndex;
        } else if (cmd.getPrefix().equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_MODIFYING)) {
            logReader = patchingHarvestLogManagerModification;
        } else {
            logReader = patchingHarvestLogManager;
        }

        File f = null;

        try {
            f = logReader.getLogfile(ti, hr, cmd.getLogFileName());
        } catch (WCTRuntimeException e) {

        }

        AttachmentView v = new AttachmentView(cmd.getLogFileName(), f, true);
        return new ModelAndView(v);
    }

    /**
     * @param harvestLogManager the harvestLogManager to set
     */
    public void setHarvestLogManager(HarvestLogManager harvestLogManager) {
        this.harvestLogManager = harvestLogManager;
    }

    /**
     * @param targetInstanceManager the targetInstanceManager to set
     */
    public void setTargetInstanceManager(TargetInstanceManager targetInstanceManager) {
        this.targetInstanceManager = targetInstanceManager;
    }


}
