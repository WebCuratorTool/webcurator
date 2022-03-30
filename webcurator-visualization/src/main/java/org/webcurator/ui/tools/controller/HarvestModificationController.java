package org.webcurator.ui.tools.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HarvestModificationController implements ModifyService {
    @Autowired
    private HarvestModificationHandler harvestModificationHandler;

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_UPLOAD_FILE, method = RequestMethod.POST, produces = "application/json")
    public ModifyRowFullData uploadFile(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestBody ModifyRowFullData cmd) {
        return harvestModificationHandler.uploadFile(job, harvestResultNumber, cmd);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_CHECK_FILES, method = RequestMethod.POST, produces = "application/json")
    public ModifyResult checkFiles(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestBody List<ModifyRowFullData> items) {
        return harvestModificationHandler.checkFiles(job, harvestResultNumber, items);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_APPLY_PRUNE_IMPORT, method = RequestMethod.POST, produces = "application/json")
    public ModifyResult applyPruneAndImport(@RequestBody ModifyApplyCommand cmd) {
        return harvestModificationHandler.applyPruneAndImport(cmd);
    }

    @RequestMapping(path = "/curator/modification/operate", method = {RequestMethod.POST, RequestMethod.GET})
    public ModifyResult operateHarvestResultModification(@RequestParam("stage") String stage, @RequestParam("command") String command, @RequestParam("targetInstanceId") long targetInstanceId, @RequestParam("harvestNumber") int harvestNumber) {
        return new ModifyResult();
    }

    @RequestMapping(path = "/curator/target/patching-hr-view-data", method = {RequestMethod.POST, RequestMethod.GET})
    public Map<String, Object> getHarvestResultViewData(@RequestParam("targetInstanceOid") long targetInstanceId, @RequestParam("harvestResultId") long harvestResultId, @RequestParam("harvestNumber") int harvestResultNumber) throws IOException, NoSuchAlgorithmException {
        return new HashMap<String, Object>();
    }

    @RequestMapping(path = "/curator/target/derived-harvest-results", method = {RequestMethod.POST, RequestMethod.GET})
    public List<HarvestResultDTO> getDerivedHarvestResults(@RequestParam("targetInstanceOid") long targetInstanceId, @RequestParam("harvestResultId") long harvestResultId, @RequestParam("harvestNumber") int harvestResultNumber) throws IOException {
        return harvestModificationHandler.getDerivedHarvestResults(targetInstanceId, harvestResultId, harvestResultNumber);
    }

    @RequestMapping(path = "/curator/bulk-import/parse", method = {RequestMethod.POST, RequestMethod.GET})
    protected NetworkMapResult bulkImportParse(@RequestParam("targetInstanceOid") long targetInstanceId, @RequestParam("harvestNumber") int harvestResultNumber, @RequestBody ModifyRowFullData cmd) throws IOException, DigitalAssetStoreException {
        return harvestModificationHandler.bulkImportParse(targetInstanceId, harvestResultNumber, cmd);
    }

    @RequestMapping(path = "/curator/export/data", method = {RequestMethod.POST, RequestMethod.GET})
    protected void exportData(@RequestParam("targetInstanceOid") long targetInstanceId, @RequestParam("harvestNumber") int harvestResultNumber, @RequestBody List<ModifyRowFullData> dataset, HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        harvestModificationHandler.exportData(targetInstanceId, harvestResultNumber, dataset, req, rsp);
    }

    @RequestMapping(path = "/curator/check-and-append", method = {RequestMethod.POST, RequestMethod.GET})
    public NetworkMapResult checkAndAppendModificationRows(@RequestParam("targetInstanceOid") long targetInstanceId, @RequestParam("harvestNumber") int harvestResultNumber, @RequestBody List<ModifyRowFullData> dataset) {
        return harvestModificationHandler.checkAndAppendModificationRows(targetInstanceId, harvestResultNumber, dataset);
    }
}