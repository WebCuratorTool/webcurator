package org.webcurator.core.store.coordinator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.webcurator.core.coordinator.WctCoordinator;
import org.webcurator.core.harvester.coordinator.DasCallback;
import org.webcurator.core.harvester.coordinator.IndexerService;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.domain.model.core.HarvestResultDTO;
import org.webcurator.domain.model.dto.SeedHistorySetDTO;
import org.webcurator.core.coordinator.WctCoordinatorPaths;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
public class DigitalAssetStoreListenerService implements DigitalAssetStoreListener, IndexerService, DasCallback {
    private static final Logger log = LoggerFactory.getLogger(DigitalAssetStoreListenerService.class);

    @Autowired
    private WctCoordinator wctCoordinator;
    @Autowired
    private TargetInstanceManager targetInstanceManager;

    @RequestMapping(path = WctCoordinatorPaths.MODIFICATION_DOWNLOAD_IMPORTED_FILE, method = {RequestMethod.POST, RequestMethod.GET})
    public void dasDownloadFile(@RequestParam("job") long targetInstanceOid, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("fileName") String fileName, HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        wctCoordinator.dasDownloadFile(targetInstanceOid, harvestResultNumber, fileName, req, rsp);
    }

    @Override
    @RequestMapping(path = WctCoordinatorPaths.DIGITAL_ASSET_STORE_HEARTBEAT, method = {RequestMethod.POST, RequestMethod.GET})
    public void dasHeartBeat(@RequestBody List<HarvestResultDTO> harvestResultDTOList) {
        wctCoordinator.dasHeartBeat(harvestResultDTOList);
    }

    @Override
    @RequestMapping(path = WctCoordinatorPaths.DIGITAL_ASSET_STORE_UPDATE_HR_STATUS, method = {RequestMethod.POST, RequestMethod.GET})
    public void dasUpdateHarvestResultStatus(@RequestBody HarvestResultDTO hrDTO) {
        wctCoordinator.dasUpdateHarvestResultStatus(hrDTO);
    }

    @RequestMapping(path = WctCoordinatorPaths.TARGET_INSTANCE_HISTORY_SEED, method = {RequestMethod.POST, RequestMethod.GET})
    public SeedHistorySetDTO dasQuerySeedHistory(@RequestParam long targetInstanceOid, @RequestParam int harvestNumber) {
        return wctCoordinator.dasQuerySeedHistory(targetInstanceOid, harvestNumber);
    }

    @RequestMapping(path = WctCoordinatorPaths.MODIFICATION_COMPLETE_PRUNE_IMPORT, method = {RequestMethod.POST, RequestMethod.GET})
    public void dasModificationComplete(@RequestParam long targetInstanceOid, @RequestParam int harvestNumber) {
        wctCoordinator.dasModificationComplete(targetInstanceOid, harvestNumber);
    }

    @PostMapping(path = WctCoordinatorPaths.COMPLETE_ARCHIVING)
    public void completeArchiving(@PathVariable(value = "target-instance-oid") Long targetInstanceOid,
                                  @RequestParam(value = "archive-id") String archiveIID) {
        try {
            log.info("Received completeArchiving for Target Instance {}  with archive IID of {}", targetInstanceOid, archiveIID);
            wctCoordinator.completeArchiving(targetInstanceOid, archiveIID);
        } catch (RuntimeException | Error ex) {
            log.error("Exception in completeArchiving", ex);
            throw ex;
        }

    }

    @PostMapping(path = WctCoordinatorPaths.FAILED_ARCHIVING)
    public void failedArchiving(@PathVariable(value = "target-instance-oid") Long targetInstanceOid,
                                @RequestParam(value = "message") String message) {
        try {
            log.info("Received failedArchiving for Target Instance " + targetInstanceOid + " with message " + message);
            wctCoordinator.failedArchiving(targetInstanceOid, message);
        } catch (RuntimeException | Error ex) {
            log.error("Exception in failedArchiving", ex);
            throw ex;
        }
    }


    @PostMapping(path = WctCoordinatorPaths.CREATE_HARVEST_RESULT)
    public Long createHarvestResult(@RequestBody HarvestResultDTO harvestResultDTO) {
        try {
            log.info("Received createHarvestResult for Target Instance {}", harvestResultDTO.getTargetInstanceOid());
            return wctCoordinator.createHarvestResult(harvestResultDTO);
        } catch (RuntimeException | Error ex) {
            log.error("Exception in createHarvestResult", ex);
            throw ex;
        }
    }

    @PostMapping(path = WctCoordinatorPaths.FINALISE_INDEX)
    public void finaliseIndex(@RequestParam(value = "targetInstanceId") long targetInstanceId, @RequestParam(value = "harvestNumber") int harvestNumber) {
        try {
            log.info("Received finaliseIndex for Harvest Result {} {}", targetInstanceId, harvestNumber);
            wctCoordinator.finaliseIndex(targetInstanceId, harvestNumber);
        } catch (RuntimeException | Error ex) {
            log.error("Exception in finaliseIndex", ex);
            throw ex;
        }
    }

    @PostMapping(path = WctCoordinatorPaths.NOTIFY_AQA_COMPLETE)
    public void notifyAQAComplete(@PathVariable(value = "aqa-id") String aqaId) {
        try {
            log.info("Received notifyAQAComplete for AQA Job {}", aqaId);
            wctCoordinator.notifyAQAComplete(aqaId);
        } catch (RuntimeException | Error ex) {
            log.error("Exception in notifyAQAComplete", ex);
            throw ex;
        }
    }
}
