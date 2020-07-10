package org.webcurator.ui.tools.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.webcurator.core.coordinator.WctCoordinator;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.visualization.VisualizationConstants;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.modification.metadata.ModifyResult;
import org.webcurator.core.visualization.modification.metadata.ModifyRow;
import org.webcurator.core.visualization.modification.metadata.ModifyRowMetadata;
import org.webcurator.core.visualization.modification.service.ModifyService;
import org.webcurator.domain.model.core.HarvestResultDTO;

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
    public ModifyRowMetadata uploadFile(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestBody ModifyRow cmd) {
        return wctCoordinator.uploadFile(job, harvestResultNumber, cmd);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_CHECK_FILES, method = RequestMethod.POST, produces = "application/json")
    public ModifyResult checkFiles(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestBody List<ModifyRowMetadata> items) {
        return wctCoordinator.checkFiles(job, harvestResultNumber, items);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_APPLY_PRUNE_IMPORT, method = RequestMethod.POST, produces = "application/json")
    public ModifyResult applyPruneAndImport(@RequestBody ModifyApplyCommand cmd) {
        return wctCoordinator.applyPruneAndImport(cmd);
    }

    @RequestMapping(path = "/curator/modification/operate", method = {RequestMethod.POST, RequestMethod.GET})
    public ModifyResult operateHarvestResultModification(@RequestParam("stage") String stage,
                                                         @RequestParam("command") String command,
                                                         @RequestParam("targetInstanceId") long targetInstanceId,
                                                         @RequestParam("harvestNumber") int harvestNumber) {
        ModifyResult result = new ModifyResult();
        try {
            if (command.equalsIgnoreCase("start")) {
                harvestModificationHandler.clickStart(targetInstanceId, harvestNumber);
            } else if (command.equalsIgnoreCase("pause")) {
                harvestModificationHandler.clickPause(targetInstanceId, harvestNumber);
            } else if (command.equalsIgnoreCase("resume")) {
                harvestModificationHandler.clickResume(targetInstanceId, harvestNumber);
            } else if (command.equalsIgnoreCase("terminate")) {
                harvestModificationHandler.clickTerminate(targetInstanceId, harvestNumber);
            } else if (command.equalsIgnoreCase("delete")) {
                harvestModificationHandler.clickDelete(targetInstanceId, harvestNumber);
            }
        } catch (DigitalAssetStoreException | WCTRuntimeException e) {
            result.setRespCode(VisualizationConstants.RESP_CODE_ERROR_SYSTEM_ERROR);
            result.setRespMsg(e.getMessage());
        }

        return result;
    }

    @RequestMapping(path = "/curator/target/patching-hr-view-data", method = {RequestMethod.POST, RequestMethod.GET})
    public Map<String, Object> getHarvestResultViewData(@RequestParam("targetInstanceOid") long targetInstanceId,
                                                        @RequestParam("harvestResultId") long harvestResultId,
                                                        @RequestParam("harvestNumber") int harvestResultNumber) throws IOException, NoSuchAlgorithmException {
        return harvestModificationHandler.getHarvestResultViewData(targetInstanceId, harvestResultId, harvestResultNumber);
    }

    @RequestMapping(path = "/curator/target/derived-harvest-results", method = {RequestMethod.POST, RequestMethod.GET})
    public List<HarvestResultDTO> getDerivedHarvestResults(@RequestParam("targetInstanceOid") long targetInstanceId,
                                                           @RequestParam("harvestResultId") long harvestResultId,
                                                           @RequestParam("harvestNumber") int harvestResultNumber) throws IOException {
        return harvestModificationHandler.getDerivedHarvestResults(targetInstanceId, harvestResultId, harvestResultNumber);
    }
}