package org.webcurator.core.harvester.coordinator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.webcurator.core.check.CheckNotifier;
import org.webcurator.core.common.Constants;
import org.webcurator.core.coordinator.WctCoordinatorPaths;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.domain.model.core.*;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;

/**
 * The Server side implmentation of the HarvestAgentListener. This Service is deployed on the core and is used by the agents to send
 * messages to the core.
 *
 * @author nwaight
 */
@RestController
public class HarvestAgentListenerService implements HarvestAgentListener, CheckNotifier {
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
    @PostMapping(path = WctCoordinatorPaths.HEARTBEAT)
    public void heartbeat(@RequestBody HarvestAgentStatusDTO aStatus) {
        log.info("Received heartbeat from {}://{}:{}", aStatus.getScheme(), aStatus.getHost(), aStatus.getPort());
        harvestCoordinator.heartbeat(aStatus);
    }

    @RequestMapping(path = WctCoordinatorPaths.RECOVERY, method = {RequestMethod.POST, RequestMethod.GET})
    public void requestRecovery(@RequestBody HarvestAgentStatusDTO aStatus) {
        log.info("Received recovery request from {}://{}:{}", aStatus.getScheme(), aStatus.getHost(), aStatus.getPort());
        harvestCoordinator.recoverHarvests(aStatus.getScheme(), aStatus.getHost(), aStatus.getPort(), aStatus.getService());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.webcurator.core.harvester.coordinator.HarvestAgentListener#harvestComplete(org.webcurator.core.model.HarvestResult)
     */
    @PostMapping(path = WctCoordinatorPaths.HARVEST_COMPLETE)
    public void harvestComplete(@RequestBody HarvestResultDTO aResult) {
        log.info("Received harvest complete for {} {}", aResult.getTargetInstanceOid(), aResult.getHarvestNumber());
        harvestCoordinator.harvestComplete(aResult);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.webcurator.core.harvester.coordinator.HarvestAgentListener#notification(Long, String)
     */
    @PostMapping(path = WctCoordinatorPaths.NOTIFICATION_BY_OID)
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
    @PostMapping(path = WctCoordinatorPaths.NOTIFICATION_BY_SUBJECT)
    public void notification(@RequestParam(value = "subject") String aSubject,
                             @RequestParam(value = "notification-category") int notificationCategory,
                             @RequestParam(value = "message") String aMessage) {
        log.info("Received Notification {} {}", aSubject, aMessage);
        harvestCoordinator.notification(aSubject, notificationCategory, aMessage);
    }




}
