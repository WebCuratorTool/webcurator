package org.webcurator.ui.tools.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.webcurator.core.visualization.VisualizationAbstractApplyCommand;
import org.webcurator.core.visualization.VisualizationConstants;
import org.webcurator.core.visualization.VisualizationDirectoryManager;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.modification.metadata.ModifyRowMetadata;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeDTO;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;
import org.webcurator.domain.TargetInstanceDAO;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.HarvestResultDTO;
import org.webcurator.domain.model.core.LogFilePropertiesDTO;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.ui.target.command.PatchingProgressCommand;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private VisualizationDirectoryManager directoryManager;

    @Value("${core.base.dir}")
    private String baseDir;

    public void clickStart(long targetInstanceId, int harvestResultNumber) throws WCTRuntimeException, DigitalAssetStoreException {
        HarvestResultDTO hr = harvestResultManager.getHarvestResultDTO(targetInstanceId, harvestResultNumber);
        if (hr.getStatus() != HarvestResult.STATUS_SCHEDULED) {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
            ModifyApplyCommand cmd = (ModifyApplyCommand) PatchUtil.modifier.readPatchJob(directoryManager.getBaseDir(), targetInstanceId, harvestResultNumber);
            wctCoordinator.patchHarvest(cmd);
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
        if (hr.getStatus() != HarvestResult.STATUS_RUNNING) {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
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
                hr.getStatus() != HarvestResult.STATUS_PAUSED &&
                hr.getStatus() != HarvestResult.STATUS_TERMINATED) {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
            String jobName = PatchUtil.getPatchJobName(targetInstanceId, harvestResultNumber);
            harvestAgentManager.stopPatching(jobName);
            harvestAgentManager.abortPatching(jobName);
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
            String jobName = PatchUtil.getPatchJobName(targetInstanceId, harvestResultNumber);
            harvestAgentManager.stopPatching(jobName);
            harvestAgentManager.abortPatching(jobName);
            List<String> jobList = new ArrayList<>();
            jobList.add(jobName);
            harvestAgentManager.purgeAbortedTargetInstances(jobList);
        } else if (hr.getState() == HarvestResult.STATE_MODIFYING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, "delete", targetInstanceId, harvestResultNumber);
        } else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_INDEXING, "delete", targetInstanceId, harvestResultNumber);
        } else {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        TargetInstance ti = targetInstanceDAO.load(targetInstanceId);

        //Delete the selected Harvest Result
        List<HarvestResult> hrList = ti.getHarvestResults();
        for (int i = 0; i < hrList.size(); i++) {
            if (hrList.get(i).getHarvestNumber() == hr.getHarvestNumber()) {
                hrList.remove(i);
                break;
            }
        }
        targetInstanceDAO.delete(hr);

        //Change the state of Target Instance to 'Harvested'
        if (ti.getPatchingHarvestResults().size() == 0) {
            ti.setState(TargetInstance.STATE_HARVESTED);
        }
        targetInstanceDAO.save(ti);
    }

    public Map<String, Object> getHarvestResultViewData(long targetInstanceId, long harvestResultId, int harvestResultNumber) throws IOException, NoSuchAlgorithmException {
        Map<String, Object> result = new HashMap<>();
        final HarvestResultDTO hrDTO;
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

        VisualizationAbstractApplyCommand cmd = PatchUtil.modifier.readPatchJob(baseDir, targetInstanceId, harvestResultNumber);
        if (cmd == null) {
            cmd = PatchUtil.modifier.readHistoryPatchJob(baseDir, targetInstanceId, harvestResultNumber);
        }

        ModifyApplyCommand pruneAndImportCommandApply = null;
        if (cmd != null) {
            pruneAndImportCommandApply = (ModifyApplyCommand) cmd;
        } else {
            pruneAndImportCommandApply = new ModifyApplyCommand();
        }

        Map<String,ModifyRowMetadata> mapToBePruned = new HashMap<>();
        Map<String, ModifyRowMetadata> mapToBeImportedByFile = new HashMap<>();
        Map<String, ModifyRowMetadata> mapToBeImportedByURL = new HashMap<>();
        pruneAndImportCommandApply.getDataset().forEach(e -> {
            if (e.getOption().equalsIgnoreCase("prune")) {
                mapToBePruned.put(e.getUrl(), e);
            } else if (e.getOption().equalsIgnoreCase("file")) {
                mapToBeImportedByFile.put(e.getUrl(), e);
            } else if (e.getOption().equalsIgnoreCase("url")) {
                mapToBeImportedByURL.put(e.getUrl(), e);
            }
        });

        /*Appended indexed results*/
        Map<String, Boolean> mapIndexedUrlNodes = getIndexedUrlNodes(targetInstanceId, harvestResultNumber, pruneAndImportCommandApply);
        appendIndexedResult(mapIndexedUrlNodes, mapToBeImportedByFile);
        appendIndexedResult(mapIndexedUrlNodes, mapToBeImportedByURL);

        mapToBePruned.forEach((k, v) -> {
            if (!mapIndexedUrlNodes.containsKey(k)) {
                v.setRespCode(VisualizationConstants.RESP_CODE_SUCCESS);
            } else if ((mapToBeImportedByFile.containsKey(k) && mapToBeImportedByFile.get(k).getRespCode() == VisualizationConstants.RESP_CODE_SUCCESS) ||
                    (mapToBeImportedByURL.containsKey(k) && mapToBeImportedByURL.get(k).getRespCode() == VisualizationConstants.RESP_CODE_SUCCESS)) {
                v.setRespCode(VisualizationConstants.RESP_CODE_SUCCESS);
            } else {
                v.setRespCode(VisualizationConstants.RESP_CODE_INDEX_NOT_EXIST);
            }
        });


        List<LogFilePropertiesDTO> logsCrawling = new ArrayList<>();
        List<LogFilePropertiesDTO> logsModifying = new ArrayList<>();
        List<LogFilePropertiesDTO> logsIndexing = new ArrayList<>();
        try {
            logsCrawling = patchingHarvestLogManager.listLogFileAttributes(hrDTO.getTargetInstanceOid(), hrDTO.getHarvestNumber(), HarvestResult.STATE_CRAWLING);
            logsModifying = patchingHarvestLogManagerModification.listLogFileAttributes(hrDTO.getTargetInstanceOid(), hrDTO.getHarvestNumber(), HarvestResult.STATE_MODIFYING);
            logsIndexing = patchingHarvestLogManagerIndex.listLogFileAttributes(hrDTO.getTargetInstanceOid(), hrDTO.getHarvestNumber(), HarvestResult.STATE_INDEXING);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

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

        putDataWithDigest("listToBePruned", mapToBePruned.values(), result);
        putDataWithDigest("listToBeImportedByFile", mapToBeImportedByFile.values(), result);
        putDataWithDigest("listToBeImportedByURL", mapToBeImportedByURL.values(), result);
        putDataWithDigest("logsCrawling", logsCrawling, result);
        putDataWithDigest("logsModifying", logsModifying, result);
        putDataWithDigest("logsIndexing", logsIndexing, result);
        return result;
    }

    public List<HarvestResultDTO> getDerivedHarvestResults(long targetInstanceId, long harvestResultId, int harvestResultNumber) {
        List<HarvestResultDTO> result = new ArrayList<>();
        TargetInstance ti = targetInstanceDAO.load(targetInstanceId);
        if (ti != null) {
            ti.getDerivedHarvestResults(harvestResultNumber).forEach(hr -> {
                HarvestResultDTO hrDTO = harvestResultManager.getHarvestResultDTO(targetInstanceId, hr.getHarvestNumber());
                result.add(hrDTO);
            });
        }

        return result;
    }

    private Map<String, Boolean> getIndexedUrlNodes(long targetInstanceId, int harvestResultNumber, ModifyApplyCommand cmd) throws JsonProcessingException {
        Map<String, Boolean> mapIndexedUrlNodes = new HashMap<>();
        HarvestResultDTO hrDTO = harvestResultManager.getHarvestResultDTO(targetInstanceId, harvestResultNumber);
        if (hrDTO.getState() == HarvestResult.STATE_CRAWLING ||
                hrDTO.getState() == HarvestResult.STATE_MODIFYING ||
                (hrDTO.getState() == HarvestResult.STATE_INDEXING && hrDTO.getStatus() != HarvestResult.STATUS_FINISHED)) {
            return mapIndexedUrlNodes;
        }

        List<String> listQueryUrlStatus = cmd.getDataset().stream().map(ModifyRowMetadata::getUrl).collect(Collectors.toList());
        NetworkMapResult urlsResult = networkMapClient.getUrlsByNames(targetInstanceId, harvestResultNumber, listQueryUrlStatus);
        if (urlsResult.getRspCode() != NetworkMapResult.RSP_CODE_SUCCESS) {
            return mapIndexedUrlNodes;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        List<NetworkMapNodeDTO> listUrlNodes = objectMapper.readValue(urlsResult.getPayload(), new TypeReference<List<NetworkMapNodeDTO>>() {
        });
        listUrlNodes.forEach(urlNode -> {
            mapIndexedUrlNodes.put(urlNode.getUrl(), true);
        });

        return mapIndexedUrlNodes;
    }

    private void appendIndexedResult(Map<String, Boolean> mapIndexedUrlNodes, Map<String, ModifyRowMetadata> mapTargetUrlNodes) {
        mapTargetUrlNodes.forEach((k, v) -> {
            if (mapIndexedUrlNodes.containsKey(k)) {
                v.setRespCode(VisualizationConstants.RESP_CODE_SUCCESS);
            } else {
                v.setRespCode(VisualizationConstants.RESP_CODE_INDEX_NOT_EXIST);
            }
        });
    }

    private String getDigest(Object obj) throws NoSuchAlgorithmException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(obj);

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(json.getBytes());
        byte[] digest = md.digest();

        return new String(Base64.getEncoder().encode(digest));
    }

    private void putDataWithDigest(String key, Object data, Map<String, Object> result) throws JsonProcessingException, NoSuchAlgorithmException {
        String digest = getDigest(data);
        Map<String, Object> pair = new HashMap<>();
        pair.put("digest", digest);
        pair.put("data", data);

        result.put(key, pair);
    }
}
