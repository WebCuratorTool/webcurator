package org.webcurator.ui.target.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.webcurator.core.harvester.coordinator.HarvestCoordinator;
import org.webcurator.core.harvester.coordinator.PatchingHarvestLogManager;
import org.webcurator.core.visualization.VisualizationProgressBar;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandProgress;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRowMetadata;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;
import org.webcurator.domain.TargetInstanceDAO;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.LogFilePropertiesDTO;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.ui.target.command.PatchingProgressCommand;
import org.webcurator.ui.target.command.ToBePrunedAndImportedMetadataCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PatchingHarvestResultViewController {
    @Autowired
    private TargetInstanceDAO targetInstanceDAO;

    @Autowired
    private HarvestCoordinator harvestCoordinator;

    @Autowired
    @Qualifier(HarvestResult.PATCH_STAGE_TYPE_CRAWLING)
    private PatchingHarvestLogManager patchingHarvestLogManager;

    @Autowired
    @Qualifier(HarvestResult.PATCH_STAGE_TYPE_MODIFYING)
    private PatchingHarvestLogManager patchingHarvestLogManagerModification;

    @Autowired
    @Qualifier(HarvestResult.PATCH_STAGE_TYPE_INDEXING)
    private PatchingHarvestLogManager patchingHarvestLogManagerIndex;

    @Autowired
    NetworkMapClient networkMapClient;

    @RequestMapping(path = "/curator/target/patching-hr-view-data", method = {RequestMethod.POST, RequestMethod.GET})
    public Map<String, Object> getHarvestResultViewData(@RequestParam("targetInstanceOid") long targetInstanceId,
                                                        @RequestParam("harvestResultId") long harvestResultId,
                                                        @RequestParam("harvestNumber") int harvestResultNumber) throws IOException {
        Map<String, Object> result = new HashMap<>();
        TargetInstance ti = targetInstanceDAO.load(targetInstanceId);
        if (ti == null) {
            result.put("respCode", 1);
            result.put("respMsg", "Could not find target instance: " + targetInstanceId);
            return result;
        }

        HarvestResult hr = ti.getHarvestResult(harvestResultNumber);
        if (hr == null) {
            result.put("respCode", 1);
            result.put("respMsg", "Could not find harvest result: " + harvestResultNumber);
            return result;
        }

        PatchingProgressCommand progress = new PatchingProgressCommand();
        progress.setPercentageSchedule(100);
        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
            if (hr.getStatus() == HarvestResult.STATUS_SCHEDULED) {
                progress.setPercentageHarvest(0);
            } else if (hr.getStatus() == HarvestResult.STATUS_FINISHED) {
                progress.setPercentageHarvest(100);
            } else {
                progress.setPercentageHarvest(50);
            }
        } else if (hr.getState() == HarvestResult.STATE_MODIFYING) {
            progress.setPercentageHarvest(100);
            VisualizationProgressBar progressBarModify = networkMapClient.getProgress(ti.getOid(), hr.getHarvestNumber());
            if (progressBarModify == null) {
                progress.setPercentageModify(100);
            } else {
                progress.setPercentageModify(progressBarModify.getProgressPercentage());
            }
        }else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            progress.setPercentageHarvest(100);
            progress.setPercentageModify(100);
            VisualizationProgressBar progressBarIndex = networkMapClient.getProgress(ti.getOid(), hr.getHarvestNumber());
            if (progressBarIndex == null) {
                progress.setPercentageIndex(100);
            } else {
                progress.setPercentageIndex(progressBarIndex.getProgressPercentage());
            }
        }else{
            progress.setPercentageHarvest(100);
            progress.setPercentageModify(100);
            progress.setPercentageIndex(100);
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

        List<LogFilePropertiesDTO> logsCrawling = patchingHarvestLogManager.listLogFileAttributes(ti, hr);
        List<LogFilePropertiesDTO> logsModifying = patchingHarvestLogManagerModification.listLogFileAttributes(ti, hr);
        List<LogFilePropertiesDTO> logsIndexing = patchingHarvestLogManagerIndex.listLogFileAttributes(ti, hr);

        result.put("respCode", 0);
        result.put("respMsg", "Success");
//        result.put("ti", ti);
//        result.put("hr", hr);
        result.put("targetInstanceOid", ti.getOid());
        result.put("harvestResultNumber", hr.getHarvestNumber());
        result.put("derivedHarvestNumber", hr.getDerivedFrom());
        result.put("createdOwner", hr.getCreatedBy().getFullName());
        result.put("createdDate", hr.getCreationDate());
        result.put("hrState", hr.getState());

        result.put("progress", progress);
        result.put("listToBePruned", listToBePruned);
        result.put("listToBeImportedByFile", listToBeImportedByFile);
        result.put("listToBeImportedByURL", listToBeImportedByURL);
        result.put("logsCrawling", logsCrawling);
        result.put("logsModifying", logsModifying);
        result.put("logsIndexing", logsIndexing);
        return result;
    }
}
