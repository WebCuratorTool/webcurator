package org.webcurator.ui.tools.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.webcurator.core.coordinator.WctCoordinator;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.visualization.VisualizationConstants;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.modification.metadata.ModifyResult;
import org.webcurator.core.visualization.modification.metadata.ModifyRowFullData;
import org.webcurator.core.visualization.modification.service.ModifyService;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;
import org.webcurator.domain.model.core.HarvestResultDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@RestController
public class HarvestModificationController implements ModifyService {
    //For store component, it's a localClient; For webapp component, it's a remote component
    @Autowired
    private WctCoordinator wctCoordinator;

    @Autowired
    private HarvestModificationHandler harvestModificationHandler;

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_UPLOAD_FILE, method = RequestMethod.POST, produces = "application/json")
    public ModifyRowFullData uploadFile(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestBody ModifyRowFullData cmd) {
        return wctCoordinator.uploadFile(job, harvestResultNumber, cmd);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_CHECK_FILES, method = RequestMethod.POST, produces = "application/json")
    public ModifyResult checkFiles(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestBody List<ModifyRowFullData> items) {
        return wctCoordinator.checkFiles(job, harvestResultNumber, items);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_APPLY_PRUNE_IMPORT, method = RequestMethod.POST, produces = "application/json")
    public ModifyResult applyPruneAndImport(@RequestBody ModifyApplyCommand cmd) {
        return wctCoordinator.applyPruneAndImport(cmd);
    }

    @RequestMapping(path = "/curator/target/patching-hr-view-data", method = {RequestMethod.POST, RequestMethod.GET})
    public Map<String, Object> getHarvestResultViewData(@RequestParam("targetInstanceOid") long targetInstanceId, @RequestParam("harvestResultId") long harvestResultId, @RequestParam("harvestNumber") int harvestResultNumber) throws IOException, NoSuchAlgorithmException {
        return harvestModificationHandler.getHarvestResultViewData(targetInstanceId, harvestResultId, harvestResultNumber);
    }

    @RequestMapping(path = "/curator/target/derived-harvest-results", method = {RequestMethod.POST, RequestMethod.GET})
    public List<HarvestResultDTO> getDerivedHarvestResults(@RequestParam("targetInstanceOid") long targetInstanceId, @RequestParam("harvestResultId") long harvestResultId, @RequestParam("harvestNumber") int harvestResultNumber) throws IOException {
        return harvestModificationHandler.getDerivedHarvestResults(targetInstanceId, harvestResultId, harvestResultNumber);
    }

    @RequestMapping(path = "/curator/tools/download/{hrOid}/**", method = {RequestMethod.POST, RequestMethod.GET})
    protected void handleDownload(@PathVariable("hrOid") Long hrOid, @RequestParam("url") String url, HttpServletRequest req, HttpServletResponse rsp) throws Exception {
        harvestModificationHandler.handleDownload(hrOid, url, req, rsp);
    }

    @RequestMapping(path = "/curator/tools/browse/{hrOid}/**", method = {RequestMethod.POST, RequestMethod.GET})
    protected void handleBrowse(@PathVariable("hrOid") Long hrOid, @RequestParam("url") String url, HttpServletRequest req, HttpServletResponse rsp) throws Exception {
        harvestModificationHandler.handleBrowse(hrOid, url, req, rsp);
    }

    @RequestMapping(path = "/curator/get/global-settings", method = {RequestMethod.POST, RequestMethod.GET})
    protected Map<String, String> getGlobalSettings(@RequestParam("targetInstanceOid") long targetInstanceId, @RequestParam("harvestResultId") long harvestResultId, @RequestParam("harvestNumber") int harvestResultNumber) {
        return harvestModificationHandler.getGlobalSettings(targetInstanceId, harvestResultId, harvestResultNumber);
    }

    @RequestMapping(path = "/curator/bulk-import/parse", method = {RequestMethod.POST, RequestMethod.GET})
    protected NetworkMapResult bulkImportParse(@RequestParam("targetInstanceOid") long targetInstanceId, @RequestParam("harvestNumber") int harvestResultNumber, @RequestBody ModifyRowFullData cmd) throws IOException, DigitalAssetStoreException {
        return harvestModificationHandler.bulkImportParse(targetInstanceId, harvestResultNumber, cmd);
    }

    @RequestMapping(path = "/curator/export/data", method = {RequestMethod.POST, RequestMethod.GET})
    protected void exportData(@RequestParam("targetInstanceOid") long targetInstanceId, @RequestParam("harvestNumber") int harvestResultNumber, @RequestParam("viewType") String viewType, @RequestBody List<ModifyRowFullData> dataset, HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        harvestModificationHandler.exportData(targetInstanceId, harvestResultNumber, viewType, dataset, req, rsp);
    }

    @RequestMapping(path = "/curator/check-and-append", method = {RequestMethod.POST, RequestMethod.GET})
    public NetworkMapResult checkAndAppendModificationRows(@RequestParam("targetInstanceOid") long targetInstanceId, @RequestParam("harvestNumber") int harvestResultNumber, @RequestBody List<ModifyRowFullData> dataset) {
        return harvestModificationHandler.checkAndAppendModificationRows(targetInstanceId, harvestResultNumber, dataset);
    }
}