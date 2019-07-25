package org.webcurator.core.harvester.coordinator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.webcurator.core.check.CheckNotifier;
import org.webcurator.core.common.Constants;
import org.webcurator.domain.model.core.ArcHarvestFileDTO;
import org.webcurator.domain.model.core.HarvestResourceDTO;
import org.webcurator.domain.model.core.HarvestResultDTO;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;

import java.util.Collection;

/**
 * The Server side implmentation of the HarvestAgentListener. This Service is deployed on the core and is used by the agents to send
 * messages to the core.
 *
 * @author nwaight
 */
public class HarvestAgentListenerService implements HarvestAgentListener, CheckNotifier,
        IndexerService, DasCallback {
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

    /*
     * (non-Javadoc)
     *
     * @see
     * org.webcurator.core.harvester.coordinator.HarvestAgentListener#heartbeat(org.webcurator.core.harvester.agent.HarvestAgentStatus
     * )
     */
    public void heartbeat(HarvestAgentStatusDTO aStatus) {
        log.info("Received heartbeat from {}:{}", aStatus.getHost(), aStatus.getPort());
        harvestCoordinator.heartbeat(aStatus);
    }

    public void requestRecovery(String haHost, int haPort, String haService) {
        log.info("Received recovery request from {}:{}", haHost, haPort);
        harvestCoordinator.recoverHarvests(haHost, haPort, haService);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.webcurator.core.harvester.coordinator.HarvestAgentListener#harvestComplete(org.webcurator.core.model.HarvestResult)
     */
    public void harvestComplete(HarvestResultDTO aResult) {
        log.info("Received harvest complete for {} {}", aResult.getTargetInstanceOid(), aResult.getHarvestNumber());
        harvestCoordinator.harvestComplete(aResult);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.webcurator.core.harvester.coordinator.HarvestAgentListener#notification(Long, String)
     */
    public void notification(Long aTargetInstanceOid, int notificationCategory, String aMessageType) {
        log.info("Received Notification TargetInstanceOid {} with MessageType of {}", aTargetInstanceOid, aMessageType);
        harvestCoordinator.notification(aTargetInstanceOid, notificationCategory, aMessageType);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.webcurator.core.harvester.coordinator.HarvestAgentListener#notification(String, String)
     */
    public void notification(String aSubject, int notificationCategory, String aMessage) {
        log.info("Received Notification {} {}", aSubject, aMessage);
        harvestCoordinator.notification(aSubject, notificationCategory, aMessage);
    }

    public void addToHarvestResult(Long harvestResultOid, ArcHarvestFileDTO ahf) {
        try {
            log.info("Received addToHarvestResult({},{})", harvestResultOid, ahf.getName());
            harvestCoordinator.addToHarvestResult(harvestResultOid, ahf);
        } catch (RuntimeException | Error ex) {
            log.error("Exception in createHarvestResult", ex);
            throw ex;
        }

    }

    public Long createHarvestResult(HarvestResultDTO harvestResultDTO) {
        try {
            log.info("Received createHarvestResult for Target Instance {}", harvestResultDTO.getTargetInstanceOid());
            return harvestCoordinator.createHarvestResult(harvestResultDTO);
        } catch (RuntimeException | Error ex) {
            log.error("Exception in createHarvestResult", ex);
            throw ex;
        }
    }

    public void finaliseIndex(Long harvestResultOid) {
        try {
            log.info("Received finaliseIndex for Harvest Result {}", harvestResultOid);
            harvestCoordinator.finaliseIndex(harvestResultOid);
        } catch (RuntimeException | Error ex) {
            log.error("Exception in finaliseIndex", ex);
            throw ex;
        }
    }

    public void notifyAQAComplete(String aqaId) {
        try {
            log.info("Received notifyAQAComplete for AQA Job {}", aqaId);
            harvestCoordinator.notifyAQAComplete(aqaId);
        } catch (RuntimeException | Error ex) {
            log.error("Exception in notifyAQAComplete", ex);
            throw ex;
        }
    }

    public void addHarvestResources(Long harvestResultOid, Collection<HarvestResourceDTO> harvestResources) {
        try {
            log.info("Received addHarvestResources for Harvest Result {}", harvestResultOid);
            harvestCoordinator.addHarvestResources(harvestResultOid, harvestResources);
        } catch (RuntimeException | Error ex) {
            log.error("Exception in createHarvestResult", ex);
            throw ex;
        }

    }

    public void completeArchiving(Long targetInstanceOid, String archiveIID) {
        try {
            log.info("Received completeArchiving for Target Instance {}  with archive IID of {}", targetInstanceOid, archiveIID);
            harvestCoordinator.completeArchiving(targetInstanceOid, archiveIID);
        } catch (RuntimeException | Error ex) {
            log.error("Exception in completeArchiving", ex);
            throw ex;
        }

    }

    public void failedArchiving(Long targetInstanceOid, String message) {
        try {
            log.info("Received failedArchiving for Target Instance " + targetInstanceOid + " with message " + message);
            harvestCoordinator.failedArchiving(targetInstanceOid, message);
        } catch (RuntimeException | Error ex) {
            log.error("Exception in failedArchiving", ex);
            throw ex;
        }

    }
}
