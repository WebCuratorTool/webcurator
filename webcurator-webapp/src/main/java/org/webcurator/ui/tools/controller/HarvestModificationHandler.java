package org.webcurator.ui.tools.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.webcurator.core.coordinator.HarvestResultManager;
import org.webcurator.core.coordinator.WctCoordinator;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.harvester.coordinator.HarvestAgentManager;
import org.webcurator.core.harvester.coordinator.PatchingHarvestLogManager;
import org.webcurator.core.store.DigitalAssetStore;
import org.webcurator.core.util.PatchUtil;
import org.webcurator.core.visualization.VisualizationAbstractCommandApply;
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
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class HarvestModificationHandler {
    private static final Logger log = LoggerFactory.getLogger(HarvestModificationHandler.class);
    @Autowired
    private TargetInstanceDAO targetInstanceDAO;

    @Autowired
    private WctCoordinator wctCoordinator;

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
    private NetworkMapClient networkMapClient;

    @Autowired
    private HarvestResultManager harvestResultManager;

    @Value("${core.base.dir}")
    private String baseDir;

    public void clickStart(long targetInstanceId, int harvestResultNumber) throws WCTRuntimeException, DigitalAssetStoreException {
        HarvestResultDTO hr = harvestResultManager.getHarvestResultDTO(targetInstanceId, harvestResultNumber);
        if (hr.getStatus() != HarvestResult.STATUS_SCHEDULED) {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
            //TODO: POPUP HarvestAgent Select Window
            //Change the status of Harvest Result
            hr.setStatus(HarvestResult.STATUS_RUNNING);
        } else if (hr.getState() == HarvestResult.STATE_MODIFYING) {
            wctCoordinator.pushPruneAndImport(targetInstanceId, harvestResultNumber);
        } else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            digitalAssetStore.initiateIndexing(hr);
        } else {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }
    }

    public void clickPause(long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException, WCTRuntimeException {
        HarvestResultDTO hr = harvestResultManager.getHarvestResultDTO(targetInstanceId, harvestResultNumber);
        if (hr.getStatus() != HarvestResult.STATUS_SCHEDULED) {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATUS_RUNNING) {
            harvestAgentManager.pausePatching(PatchUtil.getPatchJobName(targetInstanceId, harvestResultNumber));

            //Change the status of Harvest Result
            hr.setStatus(HarvestResult.STATUS_PAUSED);
        } else if (hr.getState() == HarvestResult.STATE_MODIFYING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, "pause", targetInstanceId, harvestResultNumber);
        } else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_INDEXING, "pause", targetInstanceId, harvestResultNumber);
        } else {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }
    }

    public void clickResume(long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException, WCTRuntimeException {
        HarvestResultDTO hr = harvestResultManager.getHarvestResultDTO(targetInstanceId, harvestResultNumber);
        if (hr.getStatus() != HarvestResult.STATUS_PAUSED) {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
            harvestAgentManager.resumePatching(PatchUtil.getPatchJobName(targetInstanceId, harvestResultNumber));
            //Change the status of Harvest Result
            hr.setStatus(HarvestResult.STATUS_PAUSED);
        } else if (hr.getState() == HarvestResult.STATE_MODIFYING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, "resume", targetInstanceId, harvestResultNumber);
        } else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_INDEXING, "resume", targetInstanceId, harvestResultNumber);
        } else {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }
    }

    public void clickTerminate(long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException, WCTRuntimeException {
        HarvestResultDTO hr = harvestResultManager.getHarvestResultDTO(targetInstanceId, harvestResultNumber);
        if (hr.getStatus() != HarvestResult.STATUS_RUNNING &&
                hr.getStatus() != HarvestResult.STATUS_PAUSED) {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
            harvestAgentManager.stopPatching(PatchUtil.getPatchJobName(targetInstanceId, harvestResultNumber));
        } else if (hr.getState() == HarvestResult.STATE_MODIFYING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, "terminate", targetInstanceId, harvestResultNumber);
        } else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_INDEXING, "terminate", targetInstanceId, harvestResultNumber);
        } else {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        //Change the status of Harvest Result
        hr.setStatus(HarvestResult.STATUS_TERMINATED);
        targetInstanceDAO.save(hr);
    }

    public void clickDelete(long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException, WCTRuntimeException {
        HarvestResultDTO hr = harvestResultManager.getHarvestResultDTO(targetInstanceId, harvestResultNumber);
        if (hr.getStatus() != HarvestResult.STATUS_SCHEDULED && hr.getStatus() != HarvestResult.STATUS_TERMINATED) {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
            harvestAgentManager.abortPatching(PatchUtil.getPatchJobName(targetInstanceId, harvestResultNumber));
        } else if (hr.getState() == HarvestResult.STATE_MODIFYING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, "delete", targetInstanceId, harvestResultNumber);
        } else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_INDEXING, "delete", targetInstanceId, harvestResultNumber);
        } else {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        TargetInstance ti = targetInstanceDAO.load(targetInstanceId);
        //Delete the selected Harvest Result
        ti.getHarvestResults().remove(harvestResultNumber);

        //Change the state of Target Instance to 'Harvested'
        if (ti.getPatchingHarvestResult() == null) {
            ti.setState(TargetInstance.STATE_HARVESTED);
        }
        targetInstanceDAO.save(ti);
    }

    public Map<String, Object> getHarvestResultViewData(long targetInstanceId, long harvestResultId, int harvestResultNumber) throws IOException {
        Map<String, Object> result = new HashMap<>();
        HarvestResultDTO hrDTO = null;
        try {
            hrDTO = harvestResultManager.getHarvestResultDTO(targetInstanceId, harvestResultNumber);
        } catch (WCTRuntimeException e) {
            log.error(e.getMessage());
            result.put("respCode", 1);
            result.put("respMsg", e.getMessage());
            return result;
        }

        PatchingProgressCommand progress = new PatchingProgressCommand();
        progress.setPercentageSchedule(100);
        progress.setPercentageHarvest(hrDTO.getCrawlingProgressPercentage(networkMapClient));
        progress.setPercentageModify(hrDTO.getModifyingProgressPercentage(networkMapClient));
        progress.setPercentageIndex(hrDTO.getIndexingProgressPercentage(networkMapClient));

        VisualizationAbstractCommandApply cmd = PatchUtil.modifier.readPatchJob(baseDir, targetInstanceId, harvestResultNumber);
        if (cmd == null) {
            cmd = PatchUtil.modifier.readHistoryPatchJob(baseDir, targetInstanceId, harvestResultNumber);
        }

        PruneAndImportCommandApply pruneAndImportCommandApply = null;
        if (cmd != null) {
            pruneAndImportCommandApply = (PruneAndImportCommandApply) cmd;
        } else {
            pruneAndImportCommandApply = new PruneAndImportCommandApply();
        }

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

        List<LogFilePropertiesDTO> logsCrawling = patchingHarvestLogManager.listLogFileAttributes(hrDTO.getTargetInstanceOid(), hrDTO.getHarvestNumber(), HarvestResult.STATE_CRAWLING);
        List<LogFilePropertiesDTO> logsModifying = patchingHarvestLogManagerModification.listLogFileAttributes(hrDTO.getTargetInstanceOid(), hrDTO.getHarvestNumber(), HarvestResult.STATE_MODIFYING);
        List<LogFilePropertiesDTO> logsIndexing = patchingHarvestLogManagerIndex.listLogFileAttributes(hrDTO.getTargetInstanceOid(), hrDTO.getHarvestNumber(), HarvestResult.STATE_INDEXING);

        result.put("respCode", 0);
        result.put("respMsg", "Success");
        result.put("targetInstanceOid", targetInstanceId);
        result.put("harvestResultNumber", hrDTO.getHarvestNumber());
        result.put("derivedHarvestNumber", hrDTO.getDerivedFrom());
        result.put("createdOwner", hrDTO.getCreatedByFullName());
        result.put("createdDate", hrDTO.getCreationDate());
        result.put("hrState", hrDTO.getState());
        result.put("hrStatus", hrDTO.getStatus());

        result.put("progress", progress);
        result.put("listToBePruned", listToBePruned);
        result.put("listToBeImportedByFile", listToBeImportedByFile);
        result.put("listToBeImportedByURL", listToBeImportedByURL);
        result.put("logsCrawling", logsCrawling);
        result.put("logsModifying", logsModifying);
        result.put("logsIndexing", logsIndexing);
        return result;
    }

    public List<HarvestResultDTO> getDerivedHarvestResults(long targetInstanceId, long harvestResultId, int harvestResultNumber) {
        List<HarvestResultDTO> result = new ArrayList<>();
        TargetInstance ti = targetInstanceDAO.load(targetInstanceId);
        if (ti != null) {
            ti.getDerivedHarvestResults(harvestResultNumber).forEach(hr -> {
                HarvestResultDTO hrDTO = new HarvestResultDTO();
                hrDTO.setTargetInstanceOid(targetInstanceId);
                hrDTO.setHarvestNumber(hr.getHarvestNumber());
                hrDTO.setDerivedFrom(hr.getDerivedFrom());
                hrDTO.setOid(hr.getOid());
                result.add(hrDTO);
            });
        }

        return result;
    }
}
