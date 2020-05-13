package org.webcurator.core.networkmap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.harvester.coordinator.HarvestAgentManager;
import org.webcurator.core.harvester.coordinator.HarvestCoordinator;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.core.store.DigitalAssetStore;
import org.webcurator.core.util.Auditor;
import org.webcurator.core.util.AuthUtil;
import org.webcurator.domain.TargetInstanceDAO;
import org.webcurator.domain.model.core.ArcHarvestResult;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalInt;

public class PruneAndImportRemoteClient extends AbstractRestClient implements PruneAndImportService {
    private static final Logger log = LoggerFactory.getLogger(PruneAndImportRemoteClient.class);

    /**
     * The Target Instance Dao
     */
    private TargetInstanceDAO targetInstanceDao = null;
    /**
     * the auditor.
     */
    private Auditor auditor = null;

    /**
     * The directory to cache request command for pruning and importing
     */
    private String coreCacheDir;

    /**
     * The manager to use to access the target instance.
     */
    private TargetInstanceManager targetInstanceManager;

    /**
     * The harvest coordinator for looking at the harvesters.
     */
    private HarvestCoordinator harvestCoordinator;

    private HarvestAgentManager harvestAgentManager;

    public PruneAndImportRemoteClient(String scheme, String host, int port, RestTemplateBuilder restTemplateBuilder) {
        super(scheme, host, port, restTemplateBuilder);
    }

    @Override
    public PruneAndImportCommandRowMetadata uploadFile(long job, int harvestResultNumber, String fileName, boolean replaceFlag, byte[] doc) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(PruneAndImportServicePath.PATH_UPLOAD_FILE))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("fileName", fileName)
                .queryParam("replaceFlag", replaceFlag);
        URI uri = uriComponentsBuilder.build().toUri();

        HttpEntity<Object> request = new HttpEntity<>(doc);
        RestTemplate restTemplate = restTemplateBuilder.build();

        PruneAndImportCommandRowMetadata result;
        result = restTemplate.postForObject(uri, request, PruneAndImportCommandRowMetadata.class);
        return result;
    }

    @Override
    public PruneAndImportCommandRow downloadFile(long job, int harvestResultNumber, String fileName) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(PruneAndImportServicePath.PATH_DOWNLOAD_FILE))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("fileName", fileName);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        PruneAndImportCommandRow result;
        result = restTemplate.getForObject(uri, PruneAndImportCommandRow.class);
        return result;
    }

    @Override
    public PruneAndImportCommandResult checkFiles(long job, int harvestResultNumber, List<PruneAndImportCommandRowMetadata> items) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(PruneAndImportServicePath.PATH_CHECK_FILES))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        HttpEntity<String> request = createHttpRequestEntity(items);
        RestTemplate restTemplate = restTemplateBuilder.build();

        PruneAndImportCommandResult result;
        result = restTemplate.postForObject(uri, request, PruneAndImportCommandResult.class);
        return result;
    }

    @Override
    public PruneAndImportCommandResult pruneAndImport(long job, long harvestResultId, int harvestResultNumber, int newHarvestResultNumber, PruneAndImportCommandApply cmd) {
        PruneAndImportCommandResult result = new PruneAndImportCommandResult();

        try {
            this.savePruneAndImportCommandApply(job, cmd);
        } catch (IOException e) {
            result.setRespCode(RESP_CODE_ERROR_SYSTEM_ERROR);
            result.setRespMsg(e.getMessage());
            return result;
        }

        boolean isNeedHarvest = false;
        for (PruneAndImportCommandRowMetadata row : cmd.getDataset()) {
            if (row.getOption().equalsIgnoreCase("url")) {
                isNeedHarvest = true;
                break;
            }
        }

        //Update the status of target instance
        TargetInstance ti = targetInstanceManager.getTargetInstance(job);
        synchronized (ti.getJobName()) {
            if (!ti.getState().equalsIgnoreCase(TargetInstance.STATE_HARVESTED)) {
                result.setRespCode(RESP_CODE_ERROR_SYSTEM_ERROR);
                result.setRespMsg("Invalid target instance state for modification: " + ti.getState());
                log.error(result.getRespMsg());
                return result;
            }
            ti.setState(TargetInstance.STATE_MOD_SCHEDULED);
            targetInstanceManager.save(ti);
        }

        if (!isNeedHarvest) {
            ti.setState(TargetInstance.STATE_MOD_RUNNING);
            targetInstanceManager.save(ti);
            result = this.pushPruneAndImport(job, harvestResultId, harvestResultNumber, newHarvestResultNumber, cmd);
        } else {
            String instanceAgencyName = ti.getOwner().getAgency().getName();

            //Consider the HarvesterType of default modification profile has the same type of target instance profile
            HarvestAgentStatusDTO allowedAgent = harvestAgentManager.getHarvester(instanceAgencyName, ti.getProfile().getHarvesterType());

            result.setRespCode(RESP_CODE_SUCCESS);
            if (allowedAgent != null) {
                harvestCoordinator.modifyHarvest(ti, allowedAgent);
                result.setRespMsg("Modification harvest job started");
            } else {
                ti.setScheduledTime(new Date());
                ti.setState(TargetInstance.STATE_MOD_SCHEDULED);
                targetInstanceManager.save(ti);
                result.setRespMsg("No idle agent, modification harvest job is scheduled");
            }
        }

        log.debug(result.getRespMsg());
        return result;
    }

    public boolean pushPruneAndImport(long job) {
        PruneAndImportCommandApply cmd = null;
        try {
            cmd = getPruneAndImportCommandApply(job);
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error("Could not load PruneAndImportCommandApply of target instance: {}", job);
            return false;
        }

        PruneAndImportCommandResult result = pushPruneAndImport(job, cmd.getHarvestResultId(), cmd.getHarvestResultNumber(), cmd.getNewHarvestResultNumber(), cmd);
        return result.getRespCode() == RESP_CODE_SUCCESS;
    }

    public PruneAndImportCommandResult pushPruneAndImport(long job, long harvestResultId, int harvestResultNumber, int newHarvestResultNumber, PruneAndImportCommandApply cmd) {
        PruneAndImportCommandResult result = new PruneAndImportCommandResult();

        HarvestResult res = targetInstanceDao.getHarvestResult(harvestResultId);
        if (res == null) {
            result.setRespCode(RESP_CODE_ERROR_SYSTEM_ERROR);
            result.setRespMsg(String.format("Harvest result (OID=%d) does not exist in DB", harvestResultId));
            return result;
        }

        TargetInstance ti = res.getTargetInstance();
        if (ti == null) {
            result.setRespCode(RESP_CODE_ERROR_SYSTEM_ERROR);
            result.setRespMsg(String.format("Target instance (OID=%d) does not exist in DB", job));
            return result;
        } else if (ti.getOid() != job) {
            result.setRespCode(RESP_CODE_ERROR_SYSTEM_ERROR);
            result.setRespMsg(String.format("Target instance ID in DB (OID=%d) does not match the Id (job=%d) from input parameter", ti.getOid(), job));
            return result;
        }

        OptionalInt maxHarvestResultNumber = ti.getHarvestResults().stream().mapToInt(HarvestResult::getHarvestNumber).max();
        if (maxHarvestResultNumber.isPresent()) {
            newHarvestResultNumber = maxHarvestResultNumber.getAsInt() + 1;
        }
        // newHarvestResultNumber = ti.getHarvestResults().size() + 1;

        // Create the base record.
        HarvestResult hr = new ArcHarvestResult(ti, newHarvestResultNumber);
        hr.setDerivedFrom(res.getHarvestNumber());
        hr.setProvenanceNote(cmd.getProvenanceNote());
        //        hr.addModificationNotes();
        hr.setTargetInstance(ti);
        hr.setState(ArcHarvestResult.STATE_MODIFYING);
        if (AuthUtil.getRemoteUserObject() != null) {
            hr.setCreatedBy(AuthUtil.getRemoteUserObject());
        } else {
            hr.setCreatedBy(res.getCreatedBy());
        }

        ti.getHarvestResults().add(hr);

        // Save to the database.
        targetInstanceDao.save(hr);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(PruneAndImportServicePath.PATH_APPLY_PRUNE_IMPORT))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("newHarvestResultNumber", newHarvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        HttpEntity<String> request = createHttpRequestEntity(cmd);
        RestTemplate restTemplate = restTemplateBuilder.build();


        result = restTemplate.postForObject(uri, request, PruneAndImportCommandResult.class);
        return result;
    }

    public void savePruneAndImportCommandApply(long job, PruneAndImportCommandApply cmd) throws IOException {
        String cmdFilePath = String.format("%s%smod_%d.json", coreCacheDir, File.separator, job);
        File cmdFile = new File(cmdFilePath);

        ObjectMapper objectMapper = new ObjectMapper();

        byte[] cmdJsonContent = objectMapper.writeValueAsBytes(cmd);
        Files.write(cmdFile.toPath(), cmdJsonContent);
    }

    public PruneAndImportCommandApply getPruneAndImportCommandApply(long job) throws IOException {
        String cmdFilePath = String.format("%s%smod_%d.json", coreCacheDir, File.separator, job);
        File cmdFile = new File(cmdFilePath);
        byte[] cmdJsonContent = Files.readAllBytes(cmdFile.toPath());
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(cmdJsonContent, PruneAndImportCommandApply.class);
    }

    public TargetInstanceDAO getTargetInstanceDao() {
        return targetInstanceDao;
    }

    public void setTargetInstanceDao(TargetInstanceDAO targetInstanceDao) {
        this.targetInstanceDao = targetInstanceDao;
    }

    public Auditor getAuditor() {
        return auditor;
    }

    public void setAuditor(Auditor auditor) {
        this.auditor = auditor;
    }
}
