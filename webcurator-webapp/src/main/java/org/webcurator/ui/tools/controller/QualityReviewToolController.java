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

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.screenshot.*;
import org.webcurator.domain.model.core.*;
import org.webcurator.ui.tools.command.QualityReviewToolCommand;
import org.webcurator.ui.util.PrimarySeedFirstCompare;

/**
 * The QualityReviewToolController is responsible for displaying the "menu"
 * page where the user can access the other quality review tools.
 *
 * @author bbeaumont
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@RequestMapping(path = "/curator/target/quality-review-toc.html")
public class QualityReviewToolController {
    static private Log log = LogFactory.getLog(QualityReviewToolController.class);

    @Autowired
    private QualityReviewToolControllerAttribute attr;

    @Autowired
    private ScreenshotClient screenshotClient;

    @Value("${server.servlet.contextPath}")
    private String webappContextPath;

    @Value("${enableScreenshots}")
    private boolean enableScreenshots;

    private BusinessObjectFactory businessObjectFactory = null;

    public QualityReviewToolController() {
        businessObjectFactory = new BusinessObjectFactory();
    }

    @GetMapping
    public ModelAndView getHandle(@RequestParam("targetInstanceOid") String sTargetInstanceOid, @RequestParam("harvestResultId") String sHarvestResultId, @RequestParam("harvestNumber") String sHarvestNumber) throws Exception {
        long targetInstanceOid = -1;
        long harvestResultId = -1;
        int harvestNumber = 0;

        try {
            targetInstanceOid = Long.parseLong(sTargetInstanceOid);
            harvestResultId = Long.parseLong(sHarvestResultId);
            harvestNumber = Integer.parseInt(sHarvestNumber);
        } catch (NumberFormatException e) {
            log.error("Invalid parameter", e);
            throw e;
        }

        QualityReviewToolCommand cmd = new QualityReviewToolCommand();
        cmd.setTargetInstanceOid(targetInstanceOid);
        cmd.setHarvestResultId(harvestResultId);
        cmd.setHarvestNumber(harvestNumber);
        return handle(targetInstanceOid, harvestResultId, cmd);
    }

    @PostMapping
    protected ModelAndView postHandle(QualityReviewToolCommand cmd) throws Exception {
        long targetInstanceOid = cmd.getTargetInstanceOid();
        long harvestResultId = cmd.getHarvestResultId();
        return handle(targetInstanceOid, harvestResultId, cmd);
    }

    private ModelAndView handle(long targetInstanceOid, long harvestResultId, QualityReviewToolCommand cmd) throws Exception {
        TargetInstance ti = attr.targetInstanceManager.getTargetInstance(targetInstanceOid);

        //Do not load fully as this loads ALL resources, regardless of whether they're seeds. Causes OutOfMemory for large harvests.
        HarvestResult result = attr.targetInstanceDao.getHarvestResult(harvestResultId, false);

        ScreenshotIdentifierCommand identifiers = new ScreenshotIdentifierCommand();
        identifiers.setTiOid(targetInstanceOid);
        identifiers.setHarvestNumber(result.getHarvestNumber());
        identifiers.setScreenshotType(ScreenshotType.live);
        for (SeedHistory seedHistory : ti.getSeedHistory()) {
            SeedHistoryDTO seedHistoryDTO = new SeedHistoryDTO(seedHistory);
            identifiers.getSeeds().add(seedHistoryDTO);
        }
        ScreenshotState screenshotState = new ScreenshotState();
        try {
            screenshotState = screenshotClient.checkScreenshotState(identifiers);
        } catch (Exception e) {
            log.error("Failed to get screenshot state", e);
        }

        ModelAndView mav = new ModelAndView("quality-review-toc", "command", cmd);
        mav.addObject("targetInstanceOid", ti.getOid());
        mav.addObject("archiveUrl", attr.archiveUrl);
        mav.addObject("archiveName", attr.archiveName);
        mav.addObject("archiveAlternative", attr.archiveUrlAlternative);
        mav.addObject("archiveAlternativeName", attr.archiveUrlAlternativeName);
        mav.addObject("webArchiveTarget", attr.webArchiveTarget);
        mav.addObject("targetOid", ti.getTarget().getOid());
        mav.addObject("seedHistory", ti.getSeedHistory());
        mav.addObject("screenshotState", screenshotState);

        // Get seed ID of primary seed and populate array of all seeds
        List<Map<String, String>> sMap = Lists.newArrayList();

        List<SeedHistory> historySeeds = ti.getSeedHistory().stream().sorted(PrimarySeedFirstCompare.getComparator()).collect(Collectors.toList());
        for (SeedHistory s : historySeeds) {
            Map<String, String> m = new HashMap<>();
            m.put("id", String.valueOf(s.getOid()));
            m.put("seedUrl", s.getSeed());
            m.put("primary", String.valueOf(s.isPrimary()));

            if (attr.enableBrowseTool) {
                m.put("browseUrl", String.format("curator/tools/browse/%d/?url=%s", harvestResultId, Base64.getEncoder().encodeToString(s.getSeed().getBytes())));
            } else {
                m.put("browseUrl", "");
            }

            if (attr.enableAccessTool) {
                m.put("accessUrl", attr.harvestResourceUrlMapper.generateUrl(result) + s.getSeed());
            } else {
                m.put("accessUrl", "");
            }

            sMap.add(m);

            if (s.isPrimary()) {
                mav.addObject("primarySeedId", s.getOid());
                mav.addObject("primarySeedUrl", s.getSeed());
                //				break;
            }
        }
        mav.addObject(QualityReviewToolCommand.MDL_SEEDS, sMap);

        //		mav.addObject("screenshotUrl", attr.dasBaseUrl + "/store/" + targetOid + "/" + harvestNum + "/_resources/" + targetOid + "_" + harvestNum + "_seedId_live_screen-thumbnail.png");
        String img_model_name = ti.getOid() + "_" + result.getHarvestNumber() + "_seedId_live_screen-thumbnail.png";
        String browseUrl = webappContextPath + ScreenshotPaths.BROWSE_SCREENSHOT + "/" + ScreenshotPaths.getImagePath(ti.getOid(), result.getHarvestNumber()) + "/" + img_model_name;
        mav.addObject("screenshotUrl", browseUrl);

        mav.addObject("thumbnailRenderer", attr.thumbnailRenderer);
        mav.addObject("enableScreenshots", enableScreenshots);
        return mav;
    }

}