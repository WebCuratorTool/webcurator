package org.webcurator.core.harvester.coordinator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.webcurator.core.check.CheckNotifier;
import org.webcurator.core.common.Constants;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRow;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRowMetadata;
import org.webcurator.domain.model.core.*;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;
import org.webcurator.domain.model.dto.SeedHistoryDTO;

import java.util.Collection;
import java.util.HashSet;

/**
 * The Server side implmentation of the HarvestAgentListener. This Service is deployed on the core and is used by the agents to send
 * messages to the core.
 *
 * @author nwaight
 */
@RestController
public class HarvestAgentListenerService implements HarvestAgentListener, CheckNotifier, IndexerService, DasCallback {
    /**
     * the logger.
     */
    private static Logger log = LoggerFactory.getLogger(HarvestAgentListenerService.class);
    /**
     * the harvest coordinator to delegate to.
     */
    @Autowired
    @Qualifier(Constants.BEAN_HARVEST_COORDINATOR)
    HarvestCoordinator harvestCoordinator;

    @Autowired
    TargetInstanceManager targetInstanceManager;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.webcurator.core.harvester.coordinator.HarvestAgentListener#heartbeat(org.webcurator.core.harvester.agent.HarvestAgentStatus
     * )
     */
    @PostMapping(path = HarvestCoordinatorPaths.HEARTBEAT)
    public void heartbeat(@RequestBody HarvestAgentStatusDTO aStatus) {
        log.info("Received heartbeat from {}://{}:{}", aStatus.getScheme(), aStatus.getHost(), aStatus.getPort());
        harvestCoordinator.heartbeat(aStatus);
    }

    @RequestMapping(path = HarvestCoordinatorPaths.RECOVERY, method = {RequestMethod.POST, RequestMethod.GET})
    public void requestRecovery(@RequestBody HarvestAgentStatusDTO aStatus) {
        log.info("Received recovery request from {}://{}:{}", aStatus.getScheme(), aStatus.getHost(), aStatus.getPort());
        harvestCoordinator.recoverHarvests(aStatus.getScheme(), aStatus.getHost(), aStatus.getPort(), aStatus.getService());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.webcurator.core.harvester.coordinator.HarvestAgentListener#harvestComplete(org.webcurator.core.model.HarvestResult)
     */
    @PostMapping(path = HarvestCoordinatorPaths.HARVEST_COMPLETE)
    public void harvestComplete(@RequestBody HarvestResultDTO aResult) {
        log.info("Received harvest complete for {} {}", aResult.getTargetInstanceOid(), aResult.getHarvestNumber());
        harvestCoordinator.harvestComplete(aResult);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.webcurator.core.harvester.coordinator.HarvestAgentListener#notification(Long, String)
     */
    @PostMapping(path = HarvestCoordinatorPaths.NOTIFICATION_BY_OID)
    public void notification(@RequestParam(value = "target-instance-oid") Long aTargetInstanceOid,
                             @RequestParam(value = "notification-category") int notificationCategory,
                             @RequestParam(value = "message-type") String aMessageType) {
        log.info("Received Notification TargetInstanceOid {} with MessageType of {}", aTargetInstanceOid, aMessageType);
        harvestCoordinator.notification(aTargetInstanceOid, notificationCategory, aMessageType);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.webcurator.core.harvester.coordinator.HarvestAgentListener#notification(String, String)
     */
    @PostMapping(path = HarvestCoordinatorPaths.NOTIFICATION_BY_SUBJECT)
    public void notification(@RequestParam(value = "subject") String aSubject,
                             @RequestParam(value = "notification-category") int notificationCategory,
                             @RequestParam(value = "message") String aMessage) {
        log.info("Received Notification {} {}", aSubject, aMessage);
        harvestCoordinator.notification(aSubject, notificationCategory, aMessage);
    }

    @PostMapping(path = HarvestCoordinatorPaths.CREATE_HARVEST_RESULT)
    public Long createHarvestResult(@RequestBody HarvestResultDTO harvestResultDTO) {
        try {
            log.info("Received createHarvestResult for Target Instance {}", harvestResultDTO.getTargetInstanceOid());
            return harvestCoordinator.createHarvestResult(harvestResultDTO);
        } catch (RuntimeException | Error ex) {
            log.error("Exception in createHarvestResult", ex);
            throw ex;
        }
    }

    @PostMapping(path = HarvestCoordinatorPaths.FINALISE_INDEX)
    public void finaliseIndex(@PathVariable(value = "harvest-result-oid") Long harvestResultOid) {
        try {
            log.info("Received finaliseIndex for Harvest Result {}", harvestResultOid);
            harvestCoordinator.finaliseIndex(harvestResultOid);
        } catch (RuntimeException | Error ex) {
            log.error("Exception in finaliseIndex", ex);
            throw ex;
        }
    }

    @PostMapping(path = HarvestCoordinatorPaths.NOTIFY_AQA_COMPLETE)
    public void notifyAQAComplete(@PathVariable(value = "aqa-id") String aqaId) {
        try {
            log.info("Received notifyAQAComplete for AQA Job {}", aqaId);
            harvestCoordinator.notifyAQAComplete(aqaId);
        } catch (RuntimeException | Error ex) {
            log.error("Exception in notifyAQAComplete", ex);
            throw ex;
        }
    }

    @PostMapping(path = HarvestCoordinatorPaths.COMPLETE_ARCHIVING)
    public void completeArchiving(@PathVariable(value = "target-instance-oid") Long targetInstanceOid,
                                  @RequestParam(value = "archive-id") String archiveIID) {
        try {
            log.info("Received completeArchiving for Target Instance {}  with archive IID of {}", targetInstanceOid, archiveIID);
            harvestCoordinator.completeArchiving(targetInstanceOid, archiveIID);
        } catch (RuntimeException | Error ex) {
            log.error("Exception in completeArchiving", ex);
            throw ex;
        }

    }

    @PostMapping(path = HarvestCoordinatorPaths.FAILED_ARCHIVING)
    public void failedArchiving(@PathVariable(value = "target-instance-oid") Long targetInstanceOid,
                                @RequestParam(value = "message") String message) {
        try {
            log.info("Received failedArchiving for Target Instance " + targetInstanceOid + " with message " + message);
            harvestCoordinator.failedArchiving(targetInstanceOid, message);
        } catch (RuntimeException | Error ex) {
            log.error("Exception in failedArchiving", ex);
            throw ex;
        }
    }

    @RequestMapping(path = HarvestCoordinatorPaths.TARGET_INSTANCE_HISTORY_SEED, method = {RequestMethod.POST, RequestMethod.GET})
    public SeedHistoryDTO querySeedHistory(@RequestParam Long targetInstanceOid, @RequestParam Integer harvestNumber) {
        SeedHistoryDTO seedHistoryDTO = new SeedHistoryDTO();
        seedHistoryDTO.setTargetInstanceId(targetInstanceOid);
        TargetInstance ti = targetInstanceManager.getTargetInstance(targetInstanceOid);
        if (ti == null) {
            seedHistoryDTO.setSeeds(new HashSet<SeedHistory>());
        } else {
            seedHistoryDTO.setSeeds(ti.getSeedHistory());
        }
        return seedHistoryDTO;
    }

    @RequestMapping(path = HarvestCoordinatorPaths.MODIFICATION_COMPLETE_PRUNE_IMPORT, method = {RequestMethod.POST, RequestMethod.GET})
    public void modificationComplete(@RequestParam Long targetInstanceOid, @RequestParam Integer harvestNumber) {
        harvestCoordinator.modificationComplete(targetInstanceOid, harvestNumber);
    }

    @Override
    public PruneAndImportCommandRow modificationDownloadFile(Long targetInstanceOid, Integer harvestNumber, PruneAndImportCommandRowMetadata cmd) {
        return null;
    }
}
