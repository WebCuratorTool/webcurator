package org.webcurator.ui.tools.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.webcurator.core.harvester.coordinator.HarvestCoordinator;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.core.store.DigitalAssetStore;
import org.webcurator.core.visualization.VisualizationConstants;
import org.webcurator.core.visualization.VisualizationProgressBar;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandResult;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRow;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRowMetadata;
import org.webcurator.core.visualization.modification.service.PruneAndImportService;
import org.webcurator.domain.TargetInstanceDAO;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.TargetInstance;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
public class HarvestModificationController implements PruneAndImportService {

    //For store component, it's a localClient; For webapp component, it's a remote component
    @Autowired
    private HarvestCoordinator harvestCoordinator;

    @Autowired
    private DigitalAssetStore digitalAssetStore;

    @Autowired
    private TargetInstanceManager targetInstanceManager;

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_UPLOAD_FILE, method = RequestMethod.POST, produces = "application/json")
    public PruneAndImportCommandRowMetadata uploadFile(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestBody PruneAndImportCommandRow cmd) {
        return harvestCoordinator.uploadFile(job, harvestResultNumber, cmd);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_CHECK_FILES, method = RequestMethod.POST, produces = "application/json")
    public PruneAndImportCommandResult checkFiles(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestBody List<PruneAndImportCommandRowMetadata> items) {
        return harvestCoordinator.checkFiles(job, harvestResultNumber, items);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_APPLY_PRUNE_IMPORT, method = RequestMethod.POST, produces = "application/json")
    public PruneAndImportCommandResult pruneAndImport(@RequestBody PruneAndImportCommandApply cmd) {
        return harvestCoordinator.pruneAndImport(cmd);
    }

    @RequestMapping(path = VisualizationConstants.PATH_DOWNLOAD_FILE, method = {RequestMethod.POST, RequestMethod.GET})
    public void downloadFile(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("fileName") String fileName, HttpServletRequest req, HttpServletResponse rsp) {
        try {
            harvestCoordinator.downloadFile(job, harvestResultNumber, fileName, rsp.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public VisualizationProgressBar getProgress(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber) {
        TargetInstance ti = targetInstanceManager.getTargetInstance(job);
        if (ti == null) {
            return null;
        }
        HarvestResult hr = ti.getHarvestResult(harvestResultNumber);
        if (hr == null) {
            return null;
        }

        if (hr.getState() >= HarvestResult.STATE_PATCH_MOD_RUNNING && hr.getState() <= HarvestResult.STATE_PATCH_MOD_FINISHED) {
            return digitalAssetStore.getProgress(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, job, harvestResultNumber);
        } else if (hr.getState() >= HarvestResult.STATE_PATCH_INDEX_RUNNING && hr.getState() <= HarvestResult.STATE_PATCH_INDEX_FINISHED) {
            return digitalAssetStore.getProgress(HarvestResult.PATCH_STAGE_TYPE_INDEXING, job, harvestResultNumber);
        } else {
            return null;
        }
    }
}