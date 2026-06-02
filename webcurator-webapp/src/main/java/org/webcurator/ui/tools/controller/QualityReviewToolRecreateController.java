package org.webcurator.ui.tools.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.webcurator.core.coordinator.WctCoordinator;

@RestController
public class QualityReviewToolRecreateController {
    @Autowired
    private WctCoordinator wctCoordinator;

    @GetMapping(path = "/curator/index/recreate")
    public RecreateResult recreateScreenshotAndIndex(@RequestParam("hrOid") Long hrOid, @RequestParam("recreateLive") Boolean recreateLive, @RequestParam("recreateIndex") Boolean recreateIndex, @RequestParam("recreateHarvested") Boolean recreateHarvested) {
        RecreateResult ret = new RecreateResult();
        try {
            ret.setMsg(wctCoordinator.recreateScreenshotAndIndex(hrOid, recreateLive, recreateIndex, recreateHarvested));
        } catch (Exception ex) {
            ret.setMsg(ex.getMessage());
        }
        return ret;
    }

    public static class RecreateResult {
        private String msg;

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
}


