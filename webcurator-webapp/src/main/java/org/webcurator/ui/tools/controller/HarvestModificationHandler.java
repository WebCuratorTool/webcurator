package org.webcurator.ui.tools.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.harvester.coordinator.HarvestAgentManager;
import org.webcurator.core.coordinator.WctCoordinatorImpl;
import org.webcurator.core.harvester.coordinator.PatchingHarvestLogManager;
import org.webcurator.core.store.DigitalAssetStore;
import org.webcurator.core.visualization.VisualizationProgressBar;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRowMetadata;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;
import org.webcurator.domain.TargetInstanceDAO;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.HarvestResultDTO;
import org.webcurator.domain.model.core.LogFilePropertiesDTO;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.ui.target.command.PatchingProgressCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("harvestModificationHandler")
public class HarvestModificationHandler {
    private static final Logger log = LoggerFactory.getLogger(HarvestModificationHandler.class);
    @Autowired
    private TargetInstanceDAO targetInstanceDAO;

    @Autowired
    private WctCoordinatorImpl wctCoordinator;

    @Autowired
    private HarvestAgentManager harvestAgentManager;

    @Autowired
    private DigitalAssetStore digitalAssetStore;

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

    public void clickPause(long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException {
        TargetInstance ti = targetInstanceDAO.load(targetInstanceId);
        HarvestResult hr = ti.getHarvestResult(harvestResultNumber);

        if (hr.getStatus() != HarvestResult.STATUS_RUNNING) {
            throw new DigitalAssetStoreException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
            harvestAgentManager.pausePatching(String.format("mod_%d_%d", ti.getOid(), hr.getHarvestNumber()));
        } else if (hr.getState() == HarvestResult.STATE_MODIFYING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, "pause", ti.getOid(), hr.getHarvestNumber());
        } else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_INDEXING, "pause", ti.getOid(), hr.getHarvestNumber());
        } else {
            throw new DigitalAssetStoreException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        //Change the status of Harvest Result
        hr.setStatus(HarvestResult.STATUS_PAUSED);
        targetInstanceDAO.save(hr);
    }

    public void clickResume(long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException {
        TargetInstance ti = targetInstanceDAO.load(targetInstanceId);
        HarvestResult hr = ti.getHarvestResult(harvestResultNumber);

        if (hr.getStatus() != HarvestResult.STATUS_PAUSED) {
            throw new DigitalAssetStoreException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
            harvestAgentManager.resumePatching(String.format("mod_%d_%d", ti.getOid(), hr.getHarvestNumber()));
        } else if (hr.getState() == HarvestResult.STATE_MODIFYING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, "resume", ti.getOid(), hr.getHarvestNumber());
        } else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_INDEXING, "resume", ti.getOid(), hr.getHarvestNumber());
        } else {
            throw new DigitalAssetStoreException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        //Change the status of Harvest Result
        hr.setStatus(HarvestResult.STATUS_PAUSED);
        targetInstanceDAO.save(hr);
    }

    public void clickStop(long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException {
        TargetInstance ti = targetInstanceDAO.load(targetInstanceId);
        HarvestResult hr = ti.getHarvestResult(harvestResultNumber);

        if (hr.getStatus() != HarvestResult.STATUS_RUNNING && hr.getStatus() != HarvestResult.STATUS_PAUSED) {
            throw new DigitalAssetStoreException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
            harvestAgentManager.stopPatching(String.format("mod_%d_%d", ti.getOid(), hr.getHarvestNumber()));
        } else if (hr.getState() == HarvestResult.STATE_MODIFYING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, "stop", ti.getOid(), hr.getHarvestNumber());
        } else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_INDEXING, "stop", ti.getOid(), hr.getHarvestNumber());
        } else {
            throw new DigitalAssetStoreException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        //Change the status of Harvest Result
        hr.setStatus(HarvestResult.STATUS_TERMINATED);
        targetInstanceDAO.save(hr);
    }

    public void clickDelete(long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException {
        TargetInstance ti = targetInstanceDAO.load(targetInstanceId);
        HarvestResult hr = ti.getHarvestResult(harvestResultNumber);

        if (hr.getStatus() != HarvestResult.STATUS_SCHEDULED && hr.getStatus() != HarvestResult.STATUS_TERMINATED) {
            throw new DigitalAssetStoreException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
            harvestAgentManager.abortPatching(String.format("mod_%d_%d", ti.getOid(), hr.getHarvestNumber()));
        } else if (hr.getState() == HarvestResult.STATE_MODIFYING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, "delete", ti.getOid(), hr.getHarvestNumber());
        } else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_INDEXING, "delete", ti.getOid(), hr.getHarvestNumber());
        } else {
            throw new DigitalAssetStoreException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        //Delete the selected Harvest Result
        targetInstanceDAO.delete(hr);

        //Change the state of Target Instance to 'Harvested'
        if (ti.getPatchingHarvestResult() == null) {
            ti.setState(TargetInstance.STATE_HARVESTED);
            targetInstanceDAO.save(ti);
        }
    }

    public void clickStart(long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException {
        TargetInstance ti = targetInstanceDAO.load(targetInstanceId);
        HarvestResult hr = ti.getHarvestResult(harvestResultNumber);

        if (hr.getState() == HarvestResult.STATE_CRAWLING && hr.getStatus() == HarvestResult.STATUS_FINISHED) {
        } else if ((hr.getState() == HarvestResult.STATE_MODIFYING && hr.getStatus() == HarvestResult.STATUS_SCHEDULED)
                || (hr.getState() == HarvestResult.STATE_CRAWLING && hr.getStatus() == HarvestResult.STATUS_FINISHED)) {
//            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, "start", ti.getOid(), hr.getHarvestNumber());
            wctCoordinator.pushPruneAndImport(ti);
            hr.setState(HarvestResult.STATE_MODIFYING);
        } else if ((hr.getState() == HarvestResult.STATE_INDEXING && hr.getStatus() == HarvestResult.STATUS_SCHEDULED)
                || (hr.getState() == HarvestResult.STATE_MODIFYING && hr.getStatus() == HarvestResult.STATUS_FINISHED)) {
//            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_INDEXING, "start", ti.getOid(), hr.getHarvestNumber());
            HarvestResultDTO harvestResultDTO = new HarvestResultDTO();
            harvestResultDTO.setTargetInstanceOid(targetInstanceId);
            harvestResultDTO.setHarvestNumber(harvestResultNumber);
            digitalAssetStore.initiateIndexing(harvestResultDTO);
            hr.setState(HarvestResult.STATE_INDEXING);
        } else {
            throw new DigitalAssetStoreException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        //Change the status of Harvest Result
        hr.setStatus(HarvestResult.STATUS_RUNNING);
        targetInstanceDAO.save(hr);
    }

    public Map<String, Object> getHarvestResultViewData(long targetInstanceId, long harvestResultId, int harvestResultNumber) throws IOException {
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
        } else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            progress.setPercentageHarvest(100);
            progress.setPercentageModify(100);
            VisualizationProgressBar progressBarIndex = networkMapClient.getProgress(ti.getOid(), hr.getHarvestNumber());
            if (progressBarIndex == null) {
                progress.setPercentageIndex(100);
            } else {
                progress.setPercentageIndex(progressBarIndex.getProgressPercentage());
            }
        } else {
            progress.setPercentageHarvest(100);
            progress.setPercentageModify(100);
            progress.setPercentageIndex(100);
        }

        PruneAndImportCommandApply pruneAndImportCommandApply = wctCoordinator.getPruneAndImportCommandApply(ti);
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
        result.put("targetInstanceOid", ti.getOid());
        result.put("harvestResultNumber", hr.getHarvestNumber());
        result.put("derivedHarvestNumber", hr.getDerivedFrom());
        result.put("createdOwner", hr.getCreatedBy().getFullName());
        result.put("createdDate", hr.getCreationDate());
        result.put("hrState", hr.getState());
        result.put("hrStatus", hr.getStatus());

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
