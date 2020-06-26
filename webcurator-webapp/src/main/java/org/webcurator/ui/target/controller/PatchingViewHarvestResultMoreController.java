package org.webcurator.ui.target.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.harvester.coordinator.HarvestCoordinator;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRowMetadata;
import org.webcurator.domain.TargetInstanceDAO;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.TargetInstance;

import java.util.ArrayList;
import java.util.List;

@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@RequestMapping(path = "/curator/target/modification/view/more.html")
public class PatchingViewHarvestResultMoreController {
    @Autowired
    private HarvestCoordinator harvestCoordinator;

    @Autowired
    private TargetInstanceDAO targetInstanceDAO;

    @GetMapping
    public ModelAndView getHandle(@RequestParam("targetInstanceOid") long targetInstanceId, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("action") String action) throws Exception {
        ModelAndView mav = new ModelAndView("patching-view-hr-more");
        TargetInstance ti = targetInstanceDAO.load(targetInstanceId);
        if (ti == null) {
//            bindingResult.reject("Could not find Target Instance with ID: " + targetInstanceId);
//            mav.addObject(Constants.GBL_ERRORS, bindingResult);
            return mav;
        }

        HarvestResult hr = ti.getHarvestResult(harvestResultNumber);
        if (hr == null) {
//            bindingResult.reject("Could not find Harvest Number with Number: " + harvestResultNumber);
//            mav.addObject(Constants.GBL_ERRORS, bindingResult);
            return mav;
        }

        PruneAndImportCommandApply pruneAndImportCommandApply = harvestCoordinator.getPruneAndImportCommandApply(ti);
        List<PruneAndImportCommandRowMetadata> listToBePruned = new ArrayList<>();
        List<PruneAndImportCommandRowMetadata> listToBeImportedByFile = new ArrayList<>();
        List<PruneAndImportCommandRowMetadata> listToBeImportedByURL = new ArrayList<>();

        pruneAndImportCommandApply.getDataset().forEach(e -> {
            if (e.getOption().equalsIgnoreCase("prune")) {
                listToBePruned.add(e);
            } else if (e.getOption().equalsIgnoreCase("file")) {
                listToBeImportedByFile.add(e);
            } else if (e.getOption().equalsIgnoreCase("url")) {
                listToBeImportedByURL.add(e);
            }
        });

        mav.addObject("ti", ti);
        mav.addObject("hr", hr);
        mav.addObject("listToBePruned", listToBePruned);
        mav.addObject("listToBeImportedByFile", listToBeImportedByFile);
        mav.addObject("listToBeImportedByURL", listToBeImportedByURL);
        mav.addObject("action", action);
        return mav;
    }
}
