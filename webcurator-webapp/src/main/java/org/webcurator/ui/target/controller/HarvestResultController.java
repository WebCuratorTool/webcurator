package org.webcurator.ui.target.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.domain.TargetInstanceDAO;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.TargetInstance;

@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class HarvestResultController {
    @Autowired
    private TargetInstanceDAO targetInstanceDAO;

    @RequestMapping(path = "/curator/target/harvest-result-summary.html")
    public ModelAndView showViewHarvestResultSummary(@RequestParam("targetInstanceOid") long targetInstanceId, @RequestParam("harvestResultId") long harvestResultId, @RequestParam("harvestNumber") int harvestResultNumber) throws Exception {
        ModelAndView mav = new ModelAndView("harvest-result-summary");
        TargetInstance ti = targetInstanceDAO.load(targetInstanceId);
        HarvestResult hr = ti.getHarvestResult(harvestResultNumber);
        mav.addObject("ti", ti);
        mav.addObject("hr", hr);
        return mav;

    }

    @RequestMapping(path = "/curator/target/harvest-result-networkmap.html")
    public ModelAndView showViewHarvestResultNetworkmap(@RequestParam("targetInstanceOid") long targetInstanceId, @RequestParam("harvestResultId") long harvestResultId, @RequestParam("harvestNumber") int harvestResultNumber) throws Exception {
        ModelAndView mav = new ModelAndView("harvest-result-networkmap");
        TargetInstance ti = targetInstanceDAO.load(targetInstanceId);
        HarvestResult hr = ti.getHarvestResult(harvestResultNumber);
        mav.addObject("ti", ti);
        mav.addObject("hr", hr);
        return mav;
    }
}
