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
package org.webcurator.ui.tools.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.common.ui.Constants;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.ui.tools.command.PatchCommand;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.NumberFormat;

/**
 * The TreeToolController is responsible for rendering the
 * harvest web site as a tree structure.
 * @author bbeaumont
 */
@SuppressWarnings("all")
@Controller
public class PatchController {

    private static Log log = LogFactory.getLog(PatchController.class);

    @Autowired
    private TargetInstanceManager targetInstanceManager;


    @InitBinder
    public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        // Determine the necessary formats.
        NumberFormat nf = NumberFormat.getInstance(request.getLocale());

        // Register the binders.
        binder.registerCustomEditor(Long.class, new CustomNumberEditor(Long.class, nf, true));
        binder.registerCustomEditor(Boolean.class, "propagateDelete", new CustomBooleanEditor(true));

        // to actually be able to convert Multipart instance to byte[]
        // we have to register a custom editor (in this case the
        // ByteArrayMultipartEditor
        binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
        // now Spring knows how to handle multipart object and convert them
    }


    /**
     * Shows the Patch With Pywb page, the starting point of a pywb recording session
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(path = "/curator/tools/patch.html", method = RequestMethod.GET)
    protected ModelAndView handleGet(PatchCommand command, BindingResult bindingResult)	throws Exception {

        command.setRecordingUrl(getRecordingUrl(command.getTargetInstanceOid(), command.getHarvestNumber(), command.getSeedUrl()));

        ModelAndView mav = new ModelAndView("patch");
        mav.addObject("command", command);

        if (bindingResult.hasErrors()) {
            mav.addObject(Constants.GBL_ERRORS, bindingResult);
        }
        return mav;
    }


    /**
     * Handles all other actions on the page
     */
    @RequestMapping(path = "/curator/tools/patch.html", method = RequestMethod.POST)
    protected ModelAndView handlePost(HttpServletRequest req, HttpServletResponse resp, PatchCommand command, BindingResult bindingResult)	throws Exception {

        // Register and import the newly generated warc files
        if (command.isAction((PatchCommand.ACTION_SAVE))) {
            // This functionality will require modifications to a part of the code base
            // that is also touched by the visualisation branch
        }

        // TODO other cases?

        ModelAndView mav = new ModelAndView("patch");
        if(bindingResult.hasErrors()){mav.addObject( Constants.GBL_ERRORS, bindingResult);}
        return mav;

    }

    // FIXME maybe separate method not really needed
    private String getRecordingUrl(long tiOid, int harvestNumber, String seedUrl) {
        return "http://localhost:1991/" + tiOid + "-" + harvestNumber + "/record/" + seedUrl;
    }

}
