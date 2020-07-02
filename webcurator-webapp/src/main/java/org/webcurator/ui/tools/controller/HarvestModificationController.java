package org.webcurator.ui.tools.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.webcurator.core.coordinator.WctCoordinator;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.core.store.DigitalAssetStore;
import org.webcurator.core.visualization.VisualizationConstants;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandResult;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRow;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRowMetadata;
import org.webcurator.core.visualization.modification.service.PruneAndImportService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class HarvestModificationController implements PruneAndImportService {
    //For store component, it's a localClient; For webapp component, it's a remote component
    @Autowired
    private WctCoordinator wctCoordinator;

    @Autowired
    private DigitalAssetStore digitalAssetStore;

    @Autowired
    private TargetInstanceManager targetInstanceManager;

    @Autowired
    private HarvestModificationHandler harvestModificationHandler;

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_UPLOAD_FILE, method = RequestMethod.POST, produces = "application/json")
    public PruneAndImportCommandRowMetadata uploadFile(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestBody PruneAndImportCommandRow cmd) {
        return wctCoordinator.uploadFile(job, harvestResultNumber, cmd);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_CHECK_FILES, method = RequestMethod.POST, produces = "application/json")
    public PruneAndImportCommandResult checkFiles(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestBody List<PruneAndImportCommandRowMetadata> items) {
        return wctCoordinator.checkFiles(job, harvestResultNumber, items);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_APPLY_PRUNE_IMPORT, method = RequestMethod.POST, produces = "application/json")
    public PruneAndImportCommandResult pruneAndImport(@RequestBody PruneAndImportCommandApply cmd) {
        return wctCoordinator.pruneAndImport(cmd);
    }

//    @RequestMapping(path = VisualizationConstants.PATH_DOWNLOAD_FILE, method = {RequestMethod.POST, RequestMethod.GET})
//    public void downloadFile(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("fileName") String fileName, HttpServletRequest req, HttpServletResponse rsp) {
//        wctCoordinator.dasDownloadFile(job, harvestResultNumber, fileName, req, rsp);
//    }

    @RequestMapping(path = "/curator/modification/operate", method = {RequestMethod.POST, RequestMethod.GET})
    public void operateHarvestResultModification(@RequestParam("stage") String stage,
                                                 @RequestParam("command") String command,
                                                 @RequestParam("targetInstanceId") long targetInstanceId,
                                                 @RequestParam("harvestNumber") int harvestNumber) throws DigitalAssetStoreException {
        if (command.equalsIgnoreCase("start")) {
            harvestModificationHandler.clickStart(targetInstanceId, harvestNumber);
        } else if (command.equalsIgnoreCase("pause")) {
            harvestModificationHandler.clickPause(targetInstanceId, harvestNumber);
        } else if (command.equalsIgnoreCase("resume")) {
            harvestModificationHandler.clickResume(targetInstanceId, harvestNumber);
        } else if (command.equalsIgnoreCase("terminate")) {
            harvestModificationHandler.clickStop(targetInstanceId, harvestNumber);
        } else if (command.equalsIgnoreCase("delete")) {
            harvestModificationHandler.clickDelete(targetInstanceId, harvestNumber);
        }

    }

    @RequestMapping(path = "/curator/target/patching-hr-view-data", method = {RequestMethod.POST, RequestMethod.GET})
    public Map<String, Object> getHarvestResultViewData(@RequestParam("targetInstanceOid") long targetInstanceId,
                                                        @RequestParam("harvestResultId") long harvestResultId,
                                                        @RequestParam("harvestNumber") int harvestResultNumber) throws IOException {
        return harvestModificationHandler.getHarvestResultViewData(targetInstanceId, harvestResultId, harvestResultNumber);
    }
}