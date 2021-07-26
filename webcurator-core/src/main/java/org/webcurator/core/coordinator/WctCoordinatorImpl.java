/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.webcurator.core.coordinator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.webcurator.core.archive.SipBuilder;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.harvester.HarvesterType;
import org.webcurator.core.harvester.coordinator.*;
import org.webcurator.core.util.AuthUtil;
import org.webcurator.core.util.PatchUtil;
import org.webcurator.core.util.WctUtils;
import org.webcurator.core.visualization.VisualizationDirectoryManager;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.modification.metadata.ModifyResult;
import org.webcurator.core.visualization.modification.metadata.ModifyRowFullData;
import org.webcurator.core.notification.InTrayManager;
import org.webcurator.core.notification.MessageType;
import org.webcurator.core.profiles.Heritrix3Profile;
import org.webcurator.core.profiles.HeritrixProfile;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.core.store.DigitalAssetStore;
import org.webcurator.core.store.DigitalAssetStoreFactory;
import org.webcurator.core.targets.TargetManager;
import org.webcurator.core.visualization.VisualizationConstants;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;
import org.webcurator.domain.TargetInstanceCriteria;
import org.webcurator.domain.TargetInstanceDAO;
import org.webcurator.domain.model.auth.Privilege;
import org.webcurator.domain.model.core.*;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;
import org.webcurator.domain.model.dto.QueuedTargetInstanceDTO;
import org.webcurator.domain.model.dto.SeedHistorySetDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author nwaight
 */
@SuppressWarnings("all")
@Component("wctCoordinator")
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class WctCoordinatorImpl implements WctCoordinator {
    private static final long HOUR_MILLISECONDS = 60 * 60 * 1000;
    private static final Tika tika = new Tika();

    @Autowired
    private TargetInstanceManager targetInstanceManager;

    // Functionality segregated to reduce complexity and increase testability
    @Autowired
    private HarvestAgentManager harvestAgentManager;
    @Autowired
    private HarvestLogManager harvestLogManager;
    @Autowired
    private HarvestBandwidthManager harvestBandwidthManager;
    @Autowired
    private HarvestQaManager harvestQaManager;
    @Autowired
    private TargetInstanceDAO targetInstanceDao;
    @Autowired
    private DigitalAssetStoreFactory digitalAssetStoreFactory;
    @Autowired
    private VisualizationDirectoryManager visualizationDirectoryManager;

    /**
     * The Target Manager.
     */
    @Autowired
    private TargetManager targetManager;

    /**
     * The InTrayManager.
     */
    @Autowired
    private InTrayManager inTrayManager;

    /**
     * the number of days before a target instance's digital assets are purged
     * from the DAS.
     */
    @Value("${harvestCoordinator.daysBeforeDASPurge}")
    private int daysBeforeDASPurge = 14;

    /**
     * the number of days before an aborted target instance's remnant data are
     * purged from the system.
     */
    @Value("${harvestCoordinator.daysBeforeAbortedTargetInstancePurge}")
    private int daysBeforeAbortedTargetInstancePurge = 7;
    @Autowired
    private SipBuilder sipBuilder = null;

    private Logger log = LoggerFactory.getLogger(getClass());
    private boolean queuePaused = false;

    @Value("${harvestCoordinator.harvestOptimizationLookaheadHours}")
    private int harvestOptimizationLookAheadHours;
    @Value("${harvestCoordinator.numHarvestersExcludedFromOptimisation}")
    private int numHarvestersExcludedFromOptimisation;
    @Value("${harvestCoordinator.harvestOptimizationEnabled}")
    private boolean harvestOptimizationEnabled;

    @Autowired
    private HarvestResultManager harvestResultManager;

    @Autowired
    private NetworkMapClient networkMapClient;

    /**
     * Default Constructor.
     */
    public WctCoordinatorImpl() {
        super();
    }

    public void setHarvestAgentManager(HarvestAgentManager harvestAgentManager) {
        this.harvestAgentManager = harvestAgentManager;
    }

    public void setHarvestLogManager(HarvestLogManager harvestLogManager) {
        this.harvestLogManager = harvestLogManager;
    }

    public void setHarvestBandwidthManager(HarvestBandwidthManager harvestBandwidthManager) {
        this.harvestBandwidthManager = harvestBandwidthManager;
    }

    public void setHarvestQaManager(HarvestQaManager harvestQaManager) {
        this.harvestQaManager = harvestQaManager;
    }

    public void setVisualizationDirectoryManager(VisualizationDirectoryManager visualizationDirectoryManager) {
        this.visualizationDirectoryManager = visualizationDirectoryManager;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    /**
     * @see org.webcurator.core.harvester.coordinator.HarvestAgentListener#heartbeat(HarvestAgentStatusDTO)
     */
    public void heartbeat(HarvestAgentStatusDTO aStatus) {
        harvestAgentManager.heartbeat(aStatus);
    }

    @Override
    public void requestRecovery(HarvestAgentStatusDTO aStatus) {
        //Do nothing
    }

    /**
     * Execute search of Target Instances in 'Running' or 'Paused' states. Add any
     * active job names to List for Harvest Agent attempting recovery.
     *
     * @param aStatus harvest agent scheme/host/port/service requesting attempting recovery
     * @see org.webcurator.core.harvester.coordinator.HarvestCoordinator#recoverHarvests(String, int, String)
     */
    public void recoverHarvests(HarvestAgentStatusDTO aStatus) {
        TargetInstanceCriteria criteria = new TargetInstanceCriteria();
        Set<String> states = new HashSet<String>();
        states.add("Running");
        states.add("Paused");
        criteria.setStates(states);
        List<TargetInstance> results = targetInstanceDao.findTargetInstances(criteria);
        List<String> activeJobs = new ArrayList<String>();
        for (TargetInstance ti : results) {
            log.info("RecoverHarvests: sending data back for TI: {}", ti.getJobName());
            activeJobs.add(ti.getJobName());
        }
        harvestAgentManager.recoverHarvests(aStatus.getBaseUrl(), aStatus.getService(), activeJobs);
    }

    /**
     * @see org.webcurator.core.harvester.coordinator.HarvestAgentListener#harvestComplete(HarvestResultDTO)
     */
    public void harvestComplete(HarvestResultDTO aResult) {
        log.debug("Harvest Complete: ti: {}, havestResult: {}", aResult.getTargetInstanceOid(), aResult.getHarvestNumber());

        HarvestResult harvestResult = targetInstanceManager.getHarvestResult(aResult.getTargetInstanceOid(), aResult.getHarvestNumber());
        if (harvestResult != null) {
            if (harvestResult.getState() != HarvestResult.STATE_ABORTED) {
                this.pushPruneAndImport(aResult.getTargetInstanceOid(), aResult.getHarvestNumber());
            } else {
                log.error("Invalis status, tiOID:{}, hrNum:{}, tiState:{}, hrState:{}", aResult.getTargetInstanceOid(), harvestResult.getHarvestNumber(), harvestResult.getTargetInstance().getState(), harvestResult.getState());
            }
        } else {
            TargetInstance ti = targetInstanceDao.load(aResult.getTargetInstanceOid());
            if (ti == null) {
                throw new WCTRuntimeException("Unknown TargetInstance oid recieved " + aResult.getTargetInstanceOid()
                        + " failed to save HarvestResult.");
            }

            // The result is for the original harvest, but the TI already has one or more results
            if (aResult.getHarvestNumber() == 1 && !ti.getHarvestResults().isEmpty()) {
                // This is a repeat message probably due to a timeout. Leaving this to run
                // would generate a second 'Original Harvest' which will subsequently fail in indexing
                // due to a duplicate file name constraint in the arc_harvest_file table
                log.warn("Duplicate 'Harvest Complete' message received for job: " + ti.getOid() + ". Message ignored.");
                return;
            }

            log.info("'Harvest Complete' message received for job: " + ti.getOid() + ".");

            harvestResult = new HarvestResult(aResult, ti);
            harvestResult.setProvenanceNote("Original Harvest");
            harvestResult.setState(HarvestResult.STATE_INDEXING);
            harvestResult.setCreationDate(new Date());

            ti.getHarvestResults().add(harvestResult);
            ti.setState(TargetInstance.STATE_HARVESTED);
            targetInstanceDao.save(ti);

            harvestBandwidthManager.sendBandWidthRestrictions();

            // IF the associated target record for this TI has no active TIs remaining (scheduled, queued, running,
            // paused, stopping) AND the target's schedule is not active (i.e we're past
            // the schedule end date), THEN set the status of the associated target to 'complete'.
            boolean bActiveSchedules = false;
            AbstractTarget tiTarget = ti.getTarget();
            Date now = new Date();
            for (Schedule schedule : tiTarget.getSchedules()) {
                if (schedule.getEndDate() == null) {
                    bActiveSchedules = true;
                } else {
                    if (schedule.getEndDate().after(now)) {
                        bActiveSchedules = true;
                    }
                }
            }

            if (targetInstanceDao.countActiveTIsForTarget(tiTarget.getOid()) == 0L && !bActiveSchedules) {
                Target t = targetManager.load(tiTarget.getOid(), true);
                t.changeState(Target.STATE_COMPLETED);
                targetManager.save(t);
            }

            // Ask the DigitalAssetStore to index the ARC
            try {
                digitalAssetStoreFactory.getDAS().initiateIndexing(
                        new HarvestResultDTO(harvestResult.getOid(), harvestResult.getTargetInstance().getOid(), harvestResult
                                .getCreationDate(), harvestResult.getHarvestNumber(), harvestResult.getProvenanceNote()));
            } catch (DigitalAssetStoreException ex) {
                log.error("Could not send initiateIndexing message to the DAS", ex);
            }

            inTrayManager.generateNotification(ti.getOwner().getOid(), MessageType.CATEGORY_MISC, MessageType.TARGET_INSTANCE_COMPLETE,
                    ti);
            inTrayManager.generateTask(Privilege.ENDORSE_HARVEST, MessageType.TARGET_INSTANCE_ENDORSE, ti);

            log.info("'Harvest Complete' message processed for job: " + ti.getOid() + ".");

            //TODO WARNING - the auto prune process initiates it's own indexing, but it potentially does this
            //while the indexing initiated above is STILL RUNNING.  The fact that it works is likely attributable
            //to the fact that the second indexing is likely to finish after the first, but this may not always be
            //the case.
            runAutoPrune(ti);
        }
    }

    /**
     * @param ti
     */
    private void runAutoPrune(TargetInstance ti) {
        // auto-prune if this option is enabled on the target and the ti has
        // only one harvest result
        try {
            List<HarvestResult> harvestResults = targetInstanceManager.getHarvestResults(ti.getOid());
            if (harvestResults.size() == 1) {
                harvestQaManager.autoPrune(ti);
            }
        } catch (DigitalAssetStoreException ex) {
            log.error("Could not auto-prune the target instance with oid " + ti.getOid(), ex);
        }
    }

    /**
     * @see org.webcurator.core.harvester.coordinator.HarvestCoordinator#reIndexHarvestResult(HarvestResult)
     */
    public Boolean reIndexHarvestResult(HarvestResult origHarvestResult) {
        if (origHarvestResult != null) {
            NetworkMapResult rst = networkMapClient.initialIndex(origHarvestResult.getOid(), origHarvestResult.getHarvestNumber());
            return rst.getRspCode() == NetworkMapResult.RSP_CODE_SUCCESS;
        } else {
            log.error("Invalid input parameter, origHarvestResult is null.");
            return false;
        }
    }

    /**
     * @see org.webcurator.core.harvester.coordinator.HarvestAgentListener#notification(Long, int, String)
     * String, String)
     */
    public void notification(Long aTargetInstanceOid, int notificationCategory, String aMessageType) {
        TargetInstance ti = targetInstanceDao.load(aTargetInstanceOid);
        inTrayManager.generateNotification(ti.getOwner().getOid(), notificationCategory, aMessageType, ti);
    }

    /**
     *
     */
    public void notification(String aSubject, int notificationCategory, String aMessage) {
        List<String> privs = new ArrayList<String>();
        privs.add(Privilege.MANAGE_WEB_HARVESTER);
        inTrayManager.generateNotification(privs, notificationCategory, aSubject, aMessage);
    }

    /**
     * @see org.webcurator.core.harvester.coordinator.HarvestCoordinator#harvest(TargetInstance, HarvestAgentStatusDTO)
     */
    public void harvest(TargetInstance aTargetInstance, HarvestAgentStatusDTO aHarvestAgent) {
        if (aTargetInstance == null) {
            throw new WCTRuntimeException("A null target instance was provided to the harvest command.");
        }

        if (aHarvestAgent == null) {
            throw new WCTRuntimeException("A null harvest agent status was provided to the harvest command.");
        }

        if (!harvestAgentManager.lock(aTargetInstance.getOid())) {
            log.error("Target Instance is failed to get locked: {}", aTargetInstance.getOid());
            return;
        } else {
            log.debug("Target Instance is locked: {}", aTargetInstance.getOid());
        }

        try {
            // if the target is not approved to be harvested then do not harvest
            if (queuePaused || !isTargetApproved(aTargetInstance) || aHarvestAgent.getMemoryWarning()) {
                return;
            }

            // Prepare the instance for harvesting by storing its current
            // information.
            prepareHarvest(aTargetInstance);

            // Run the actual harvest.
            _harvest(aTargetInstance, aHarvestAgent);
        }finally {
            harvestAgentManager.unLock(aTargetInstance.getOid());
        }
    }

    private void prepareHarvest(TargetInstance aTargetInstance) {
        BusinessObjectFactory factory = new BusinessObjectFactory();
        Set<String> originalSeeds = new HashSet<String>();
        Set<SeedHistory> seedHistory = new HashSet<SeedHistory>();
        for (Seed seed : targetManager.getSeeds(aTargetInstance)) {
            originalSeeds.add(seed.getSeed());

            if (targetInstanceManager.getStoreSeedHistory()) {
                SeedHistory history = factory.newSeedHistory(aTargetInstance, seed);
                seedHistory.add(history);
            }
        }

        // Note that seed history should eventually supplant original seeds.
        // Original seeds
        // has been implemented as a Hibernate collection of String and thus the
        // target_instance_orig_seeds table has no id column, preventing further
        // expansion
        // of the collection. The seed history needs to include the primary
        // column. The
        // originalSeeds are used by the quality review (prune) tool to generate
        // the to level
        // tree view. The original seeds has been left in for this purpose to
        // support legacy
        // target instances created prior to this release. Future releases may
        // see the removal
        // of this functionality in favour of the SeedHistory; meanwhile this
        // can be turned off
        // via the targetInstanceManager bean in wct_core.xml
        aTargetInstance.setOriginalSeeds(originalSeeds);
        if (targetInstanceManager.getStoreSeedHistory()) {
            aTargetInstance.setSeedHistory(seedHistory);
        }
        // Generate some of the SIP.
        Map<String, String> sipParts = sipBuilder.buildSipSections(aTargetInstance);
        aTargetInstance.setSipParts(sipParts);

        // Save the sip parts and seeds to the database.
        targetInstanceDao.save(aTargetInstance);
    }

    /**
     * Internal harvest method.
     *
     * @param aTargetInstance The instance to harvest.
     * @param aHarvestAgent   The agent to harvest on.
     */
    private void _harvest(TargetInstance aTargetInstance, HarvestAgentStatusDTO aHarvestAgent) {
        if (aTargetInstance == null) {
            throw new WCTRuntimeException("A null target instance was provided to the harvest command.");
        }

        if (aHarvestAgent == null) {
            throw new WCTRuntimeException("A null harvest agent status was provided to the harvest command.");
        }

        // if the target is not approved to be harvested then do not harvest
        if (!isTargetApproved(aTargetInstance) || aHarvestAgent.getMemoryWarning()) {
            return;
        }

        // Create the seeds file contents.
        StringBuffer seeds = new StringBuffer();
        Set<String> originalSeeds = aTargetInstance.getOriginalSeeds();
        for (String seed : originalSeeds) {
            seeds.append(seed);
            seeds.append("\n");
        }

        // Get the profile.
        String profile = getHarvestProfileString(aTargetInstance);

        // Update the state of the allocated Target Instance
        long targetInstanceId = aTargetInstance.getOid();
        aTargetInstance = targetInstanceDao.load(targetInstanceId);
        aTargetInstance.setActualStartTime(new Date());
        aTargetInstance.setState(TargetInstance.STATE_RUNNING);
        aTargetInstance.setHarvestServer(aHarvestAgent.getName());

        // Save the updated information: to avoid asynchronous problem, update the satate before start harvesting
        targetInstanceManager.save(aTargetInstance);

        // Initiate harvest on the remote harvest agent
        harvestAgentManager.initiateHarvest(aHarvestAgent, aTargetInstance, profile, seeds.toString());

        log.info("HarvestCoordinator: Harvest initiated successfully for target instance " + aTargetInstance.getOid().toString());

        // Run the bandwidth calculations.
        harvestBandwidthManager.sendBandWidthRestrictions();
    }

    /**
     * Specify the seeds and profile, and allocate the target instance to an idle harvest agent.
     *
     * @param queuedTargetInstanceDTO: the target instance to modify
     */
    private void launchQueuedPatchHarvest(QueuedTargetInstanceDTO queuedTargetInstanceDTO) {
        TargetInstance ti = loadTargetInstance(queuedTargetInstanceDTO.getOid());
        List<HarvestResult> hrList = ti.getPatchingHarvestResults();
        for (HarvestResult hr : hrList) {
            HarvestResultDTO hrDTO = harvestResultManager.getHarvestResultDTO(ti.getOid(), hr.getHarvestNumber());
            if (hrDTO == null || hrDTO.getStatus() != HarvestResult.STATUS_SCHEDULED) {
                continue;
            }
            switch (hr.getState()) {
                case HarvestResult.STATE_CRAWLING:
                    patchHarvestCrawling(ti, hr, queuedTargetInstanceDTO);
                    break;
                case HarvestResult.STATE_MODIFYING:
                    pushPruneAndImport(ti, hr);
                    break;
                case HarvestResult.STATE_INDEXING:
                    initiateIndexing(ti, hr);
                    break;
                default:
                    log.error("Invalid Harvest Result. TargetInstanceId:{}, HarvestResultNumber: {}, HarvestResultState: {}", queuedTargetInstanceDTO.getOid(), hr.getHarvestNumber(), hr.getState());
            }
        }
    }

    private void patchHarvestCrawling(TargetInstance ti, HarvestResult hr, QueuedTargetInstanceDTO queuedTargetInstanceDTO) {
        boolean approved = true;

        // lock the ti
        Long tiOid = queuedTargetInstanceDTO.getOid();
        if (!harvestAgentManager.lock(tiOid)) {
            return;
        }

        log.info("Obtained lock for ti " + tiOid);

        boolean processed = false;
        while (!processed) {
            if (ti == null) {
                ti = loadTargetInstance(tiOid);
            }
            // Check to see what harvester resource is available
            HarvestAgentStatusDTO agent = harvestAgentManager.getHarvester(queuedTargetInstanceDTO.getAgencyName(), ti.getProfile().getHarvesterType());

            if (agent == null) {
                log.warn(String.format("No available harvest agent of type %s found for agency %s",
                        ti.getProfile().getHarvesterType(), queuedTargetInstanceDTO.getAgencyName()
                ));
            }

            if (harvestAgentCanHarvest(agent, queuedTargetInstanceDTO)) {
                synchronized (agent) {
                    // allocate the target instance to the agent
                    log.info("Allocating TI " + tiOid + " to agent " + agent.getName());
                    processed = patchHarvest(ti, hr, agent);
                }
            }
        }

        // release the lock
        harvestAgentManager.unLock(tiOid);
        log.info("Released lock for ti " + tiOid);
    }

    /**
     * Specify the seeds and profile, and allocate the target instance to an idle harvest agent.
     *
     * @param ti:                   the target instance to modify
     * @param hr:                   the harvest result to modify
     * @param harvestAgentStatusDTO the harvest agent
     */
    @Override
    public boolean patchHarvest(TargetInstance ti, HarvestResult hr, HarvestAgentStatusDTO harvestAgentStatusDTO) {
        boolean processed = false;
        if (ti == null || hr == null || harvestAgentStatusDTO == null) {
            log.error("Input parameter is null");
            return processed;
        }
        String state = ti.getState();
        if (!state.equalsIgnoreCase(TargetInstance.STATE_PATCHING)) {
            log.error("Could not start a un-modifying target instance: {}", ti.getState());
            return processed;
        }

        ModifyApplyCommand cmd = (ModifyApplyCommand) PatchUtil.modifier.readPatchJob(visualizationDirectoryManager.getBaseDir(), ti.getOid(), hr.getHarvestNumber());
        if (cmd == null) {
            log.error("Could not load ModifyApplyCommand of Harvest Result: {} {}", ti.getOid(), hr.getHarvestNumber());
            return processed;
        }

        // Create the seeds file contents.
        final StringBuilder seeds = new StringBuilder();
        cmd.getDataset().stream().filter(e -> {
            return e.getOption().equalsIgnoreCase(ModifyApplyCommand.OPTION_RECRAWL);
        }).forEach(e -> {
            seeds.append(e.getUrl());
            seeds.append("\n");
        });

        // Get the profile.
        Resource resource = new ClassPathResource("modificationH3Profile.xml");
        String profile = null;
        try {
            profile = IOUtils.toString(resource.getInputStream()); //new String(Files.readAllBytes(resource.getFile().toPath()));
        } catch (IOException e) {
            log.error("Failed to read modificationH3Profile.xml: ", e);
            return processed;
        }

        try {
            ti.setActualStartTime(new Date());
            ti.setHarvestServer(harvestAgentStatusDTO.getName());
            targetInstanceManager.save(ti);

            // Initiate harvest on the remote harvest agent
            harvestAgentManager.initiateHarvest(harvestAgentStatusDTO, PatchUtil.getPatchJobName(cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber()), profile, seeds.toString());
            harvestResultManager.updateHarvestResultStatus(cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber(), HarvestResult.STATE_CRAWLING, HarvestResult.STATUS_RUNNING);
            log.info("HarvestCoordinator: Harvest initiated successfully for Harvest Result {}", ti.getOid(), hr.getHarvestNumber());

            // Run the bandwidth calculations.
            harvestBandwidthManager.sendBandWidthRestrictions();

            harvestAgentStatusDTO.setInTransition(true);
            processed = true;
        } catch (Throwable e) {
            log.warn(MessageFormat.format("Failed to allocate harvest to agent {0}: {1}", harvestAgentStatusDTO.getName(), e.getMessage()), e);
            harvestAgentManager.markDead(harvestAgentStatusDTO);
        }
        return processed;
    }

    /**
     * @see HarvestCoordinator#checkForBandwidthTransition().
     */
    public void checkForBandwidthTransition() {
        harvestBandwidthManager.checkForBandwidthTransition();
    }

    /**
     * Get the profile string with the overrides applied.
     *
     * @return
     */
    private String getHarvestProfileString(TargetInstance aTargetInstance) {

        Profile profile = aTargetInstance.getTarget().getProfile();
        ProfileOverrides overrides = aTargetInstance.getProfileOverrides();

        if (profile.getHarvesterType().equals(HarvesterType.HERITRIX1.name())) {
            String profileString = profile.getProfile();

            // replace any ${TI_OID} tokens
            profileString = profileString.replace("${TI_OID}", aTargetInstance.getOid().toString());

            HeritrixProfile heritrixProfile = HeritrixProfile.fromString(profileString);

            if (overrides.hasOverrides()) {
                log.info("Applying Profile Overrides for " + aTargetInstance.getOid());
                overrides.apply(heritrixProfile);
            }

            heritrixProfile.setToeThreads(targetManager.getSeeds(aTargetInstance).size() * 2);
            return heritrixProfile.toString();
        }
        if (profile.getHarvesterType().equals(HarvesterType.HERITRIX3.name())) {
            String profileXml = profile.getProfile();
            if (overrides.hasH3Overrides()) {
                if (overrides.isOverrideH3RawProfile()) {
                    return overrides.getH3RawProfile();
                } else {
                    Heritrix3Profile h3Profile = new Heritrix3Profile(profileXml);
                    log.info("Applying H3 Profile Overrides for " + aTargetInstance.getOid());
                    overrides.apply(h3Profile);
                    return h3Profile.toProfileXml();
                }
            } else {
                return profileXml;
            }
        }
        return profile.getProfile();
    }

    public long getCurrentGlobalMaxBandwidth() {
        return harvestBandwidthManager.getCurrentGlobalMaxBandwidth();
    }

    /**
     * Checks if harvest optimization is permitted within the current bandwidth
     * restriction. Note that this is different to the
     * "isHarvestOptimizationEnabled" check.
     *
     * @return
     */
    public boolean isHarvestOptimizationAllowed() {
        return harvestBandwidthManager.isHarvestOptimizationAllowed();
    }

    /**
     * @see org.webcurator.core.harvester.coordinator.HarvestCoordinator#getBandwidthRestrictions()
     * .
     */
    public HashMap<String, List<BandwidthRestriction>> getBandwidthRestrictions() {
        return harvestBandwidthManager.getBandwidthRestrictions();
    }

    /**
     * @see org.webcurator.domain.HarvestCoordinatorDAO#getBandwidthRestriction(Long)
     * .
     */
    public BandwidthRestriction getBandwidthRestriction(Long aOid) {
        return harvestBandwidthManager.getBandwidthRestriction(aOid);
    }

    /**
     * @see org.webcurator.domain.HarvestCoordinatorDAO#getBandwidthRestriction(String,
     * Date).
     */
    public BandwidthRestriction getBandwidthRestriction(String aDay, Date aTime) {
        return harvestBandwidthManager.getBandwidthRestriction(aDay, aTime);
    }

    /**
     * @see org.webcurator.domain.HarvestCoordinatorDAO#saveOrUpdate(BandwidthRestriction)
     * .
     */
    public void saveOrUpdate(BandwidthRestriction bandwidthRestriction) {
        harvestBandwidthManager.saveOrUpdate(bandwidthRestriction);
    }

    /**
     * @see org.webcurator.domain.HarvestCoordinatorDAO#delete(BandwidthRestriction)
     */
    public void delete(BandwidthRestriction bandwidthRestriction) {
        harvestBandwidthManager.delete(bandwidthRestriction);
    }

    /**
     * @param aMinimumBandwidth The minimumBandwidth to set.
     */
    public void setMinimumBandwidth(int aMinimumBandwidth) {
        harvestBandwidthManager.setMinimumBandwidth(aMinimumBandwidth);
    }

    /**
     * @param targetInstanceDao The targetInstanceDao to set.
     */
    public void setTargetInstanceDao(TargetInstanceDAO targetInstanceDao) {
        this.targetInstanceDao = targetInstanceDao;
    }

    /**
     * @return Returns the maxBandwidthPercent.
     */
    public int getMaxBandwidthPercent() {
        return harvestBandwidthManager.getMaxBandwidthPercent();
    }

    /**
     * @param maxBandwidthPercent The maxBandwidthPercent to set.
     */
    public void setMaxBandwidthPercent(int maxBandwidthPercent) {
        harvestBandwidthManager.setMaxBandwidthPercent(maxBandwidthPercent);
    }

    /**
     * @see org.webcurator.core.harvester.coordinator.HarvestCoordinator#updateProfileOverrides(TargetInstance)
     */
    public void updateProfileOverrides(TargetInstance aTargetInstance) {
        if (aTargetInstance == null) {
            throw new WCTRuntimeException("A null target instance was provided.");
        }
        String profile = getHarvestProfileString(aTargetInstance);
        harvestAgentManager.updateProfileOverrides(aTargetInstance, profile);
    }

    /**
     * @see org.webcurator.core.harvester.coordinator.HarvestCoordinator#pause(TargetInstance)
     */
    public void pause(TargetInstance aTargetInstance) {
        if (aTargetInstance == null) {
            throw new WCTRuntimeException("A null target instance was provided to the harvest command.");
        }
        harvestAgentManager.pause(aTargetInstance);
    }

    /**
     * @see org.webcurator.core.harvester.coordinator.HarvestCoordinator#resume(TargetInstance)
     */
    public void resume(TargetInstance aTargetInstance) {
        harvestAgentManager.resume(aTargetInstance);
        // When profile overrides need updating we should also reset the
        // bandwidth restrictions
        harvestBandwidthManager.sendBandWidthRestrictions();
    }

    /**
     * @see org.webcurator.core.harvester.coordinator.HarvestCoordinator#abort(TargetInstance)
     */
    public void abort(TargetInstance aTargetInstance) {
        if (aTargetInstance == null) {
            throw new WCTRuntimeException("A null target instance was provided to the harvest command.");
        }
        harvestAgentManager.abort(aTargetInstance);
        harvestBandwidthManager.sendBandWidthRestrictions();
    }

    /**
     * @see org.webcurator.core.harvester.coordinator.HarvestCoordinator#stop(TargetInstance)
     */
    public void stop(TargetInstance aTargetInstance) {
        if (aTargetInstance == null) {
            throw new WCTRuntimeException("A null target instance was provided to the harvest command.");
        }
        harvestAgentManager.stop(aTargetInstance);
    }

    /**
     * @see org.webcurator.core.harvester.coordinator.HarvestCoordinator#pauseAll()
     */
    public void pauseAll() {
        harvestAgentManager.pauseAll();
    }

    /**
     * @see org.webcurator.core.harvester.coordinator.HarvestCoordinator#resumeAll()
     */
    public void resumeAll() {
        harvestAgentManager.resumeAll();
    }

    /**
     * @see org.webcurator.core.harvester.coordinator.HarvestCoordinator#pauseQueue()
     */
    public void pauseQueue() {
        queuePaused = true;
    }

    /**
     * @see org.webcurator.core.harvester.coordinator.HarvestCoordinator#resumeQueue()
     */
    public void resumeQueue() {
        queuePaused = false;
    }

    /**
     * @see org.webcurator.core.harvester.coordinator.HarvestCoordinator#isQueuePaused()
     */
    public boolean isQueuePaused() {
        return queuePaused;
    }

    /**
     * @see HarvestCoordinator#processSchedule().
     */
    public void processSchedule() {
        long now = System.currentTimeMillis();
        queueScheduledInstances();
        queueOptimisableInstances();

        log.debug("Processing schedules took {} ms", System.currentTimeMillis() - now);
    }

    private void queueScheduledInstances() {
        List<QueuedTargetInstanceDTO> theQueue = targetInstanceDao.getQueue();
        log.info("Start: Processing " + theQueue.size() + " entries from the queue.");

        QueuedTargetInstanceDTO ti = null;
        Iterator<QueuedTargetInstanceDTO> it = theQueue.iterator();
        while (it.hasNext()) {
            ti = it.next();
            log.info("Processing queue and modify entry: " + ti.toString());
            if (ti.getState().equalsIgnoreCase(TargetInstance.STATE_PATCHING)) {
                launchQueuedPatchHarvest(ti);
            } else {
                harvestOrQueue(ti);
            }
        }
        log.info("Finished: Processing {} entries from the queue.", theQueue.size());
    }

    public void queueOptimisableInstances() {
        if (harvestOptimizationEnabled && isHarvestOptimizationAllowed()) {
            int optimizedJobCount = 0;
            int optimizationUnavailableCount = 0;
            List<QueuedTargetInstanceDTO> upcomingJobs = targetInstanceDao.getUpcomingJobs(harvestOptimizationLookAheadHours
                    * HOUR_MILLISECONDS);
            int upcomingJobCount = upcomingJobs.size();
            log.info("Start: Attempting to optimize {} entries from the queue.", upcomingJobCount);
            for (QueuedTargetInstanceDTO qti : upcomingJobs) {
                if (loadAndStartOptimizable(qti)) {
                    optimizedJobCount++;
                } else {
                    optimizationUnavailableCount++;
                }
            }
            log.info(MessageFormat.format(
                    "Finished: Processed {0} of {1} upcoming jobs, of which {2} were not eligible/configured for optimization",
                    (optimizedJobCount + optimizationUnavailableCount), upcomingJobCount, optimizationUnavailableCount));
        }
    }

    private boolean loadAndStartOptimizable(QueuedTargetInstanceDTO qti) {
        log.info("Load and start optimizable: {}.", qti.getOid());

        if (!harvestAgentManager.lock(qti.getOid())) {
            log.info("Failed to get a lock: {}.", qti.getOid());
            return false;
        }
        try {
            log.info("Locked Target Instance: ", qti.getOid());
            TargetInstance targetInstance = loadTargetInstance(qti.getOid());
            AbstractTarget abstractTarget = targetInstance.getTarget();
            if (abstractTarget.getObjectType() == AbstractTarget.TYPE_TARGET) {
                Target target = targetManager.load(abstractTarget.getOid());
                if (target.isAllowOptimize()) {
                    boolean harvesterWasAvailableForOptimize = startOptimisableInstance(targetInstance, qti.getAgencyName());
                    return harvesterWasAvailableForOptimize;
                }
            }
        } finally {
            harvestAgentManager.unLock(qti.getOid());
            log.info("Unlocked Target Instance: .", qti.getOid());
        }

        return false;
    }

    private boolean startOptimisableInstance(TargetInstance targetInstance, String agencyName) {
        List<HarvestAgentStatusDTO> harvesters = harvestAgentManager.getAvailableHarvesters(agencyName);
        if (harvesters.size() > numHarvestersExcludedFromOptimisation) {
            return startOptimizedHarvest(targetInstance, harvesters);
        } else {
            log.trace("No available harvesters to optimize target instance id {}", targetInstance.getOid());
            return false;
        }
    }

    private boolean startOptimizedHarvest(TargetInstance targetInstance, List<HarvestAgentStatusDTO> harvesters) {
        for (HarvestAgentStatusDTO agent : harvesters) {
            log.trace("Harvester {} can optimize scheduled of target instance id {}", agent.getName(),
                    targetInstance.getOid());
            if (harvestTargetInstance(agent, targetInstance)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Run the checks to see if the target instance can be harvested or if it
     * must be queued. If harvest is possible and there is a harvester available
     * then allocate it.
     *
     * @param aTargetInstance the target instance to harvest
     */
    public void harvestOrQueue(QueuedTargetInstanceDTO aTargetInstance) {
        TargetInstance ti = null;
        boolean approved = true;

        // lock the ti
        Long tiOid = aTargetInstance.getOid();
        if (!harvestAgentManager.lock(tiOid)) {
            log.warn("The ti is locked: {}", tiOid);
            return;
        }

        try {
            log.info("Obtained lock for ti " + tiOid);

            if (TargetInstance.STATE_SCHEDULED.equals(aTargetInstance.getState())) {
                ti = loadTargetInstance(tiOid);
                approved = isTargetApproved(ti);
            }

            if (approved) {
                queueApprovedHarvest(aTargetInstance, ti, tiOid);
            }
        } finally {
            // release the lock
            harvestAgentManager.unLock(tiOid);
            log.info("Released lock for ti " + tiOid);
        }
    }

    private void queueApprovedHarvest(QueuedTargetInstanceDTO queuedTargetInstance, TargetInstance ti, Long tiOid) {
        boolean processed = false;
        while (!processed) {
            if (ti == null) {
                ti = loadTargetInstance(tiOid);
            }
            // Check to see what harvester resource is available
            HarvestAgentStatusDTO agent = harvestAgentManager.getHarvester(
                    queuedTargetInstance.getAgencyName(),
                    ti.getProfile().getHarvesterType());

            if (agent == null) {
                log.warn(String.format("No available harvest agent of type %s found for agency %s",
                        ti.getProfile().getHarvesterType(),
                        queuedTargetInstance.getAgencyName()
                ));
            }

            if (harvestAgentCanHarvest(agent, queuedTargetInstance)) {
                synchronized (agent) {
                    // allocate the target instance to the agent
                    log.info("Allocating TI " + tiOid + " to agent " + agent.getName());
                    processed = harvestTargetInstance(agent, ti);
                }
            } else {
                processed = true;
                log.info("Re-queueing TI " + tiOid);
                // if not already queued set the target instance to the
                // queued state.
                if (!queuedTargetInstance.getState().equals(TargetInstance.STATE_QUEUED)) {
                    if (ti == null) {
                        ti = loadTargetInstance(tiOid);
                    }

                    // Prepare the harvest for the queue.
                    prepareHarvest(ti);

                    ti.setState(TargetInstance.STATE_QUEUED);
                    targetInstanceDao.save(ti);
                    inTrayManager.generateNotification(ti.getOwner().getOid(), MessageType.CATEGORY_MISC,
                            MessageType.TARGET_INSTANCE_QUEUED, ti);
                }
            }
        }
    }

    private TargetInstance loadTargetInstance(Long tiOid) {
        TargetInstance ti = targetInstanceDao.load(tiOid);
        ti = targetInstanceDao.populate(ti);
        return ti;
    }

    private boolean harvestTargetInstance(HarvestAgentStatusDTO agent, TargetInstance ti) {
        boolean processed = false;
        try {
            if (!TargetInstance.STATE_QUEUED.equals(ti.getState())) {
                prepareHarvest(ti);
            }
            _harvest(ti, agent);
            agent.setInTransition(true);
            processed = true;
        } catch (Throwable e) {
            log.warn(MessageFormat.format("Failed to allocate harvest to agent {0}: {1}", agent.getName(), e.getMessage()), e);
            harvestAgentManager.markDead(agent);
        }
        return processed;
    }

    private boolean harvestAgentCanHarvest(HarvestAgentStatusDTO agent, QueuedTargetInstanceDTO aTargetInstance) {
        return !queuePaused && agent != null && agent.isAcceptTasks()
                && harvestBandwidthManager.isMiniumBandwidthAvailable(aTargetInstance);
    }

    /**
     * Check that the target that the instance belongs to is approved and if not
     * don't harvest.
     *
     * @param aTargetInstance the target instance whos target should be checked.
     * @return flag to indicat approval
     */
    private boolean isTargetApproved(TargetInstance aTargetInstance) {
        // Check permissions if none defer the target instance and send and
        // notification
        if (!targetManager.isTargetHarvestable(aTargetInstance)) {
            // Defer the schedule 24 hours and notifiy the owner.
            Calendar cal = Calendar.getInstance();
            cal.setTime(aTargetInstance.getScheduledTime());
            cal.add(Calendar.DATE, 1);

            aTargetInstance.setScheduledTime(cal.getTime());
            targetInstanceDao.save(aTargetInstance);

            log.info("The target {} is not apporoved for harvest and has been defered 24 hours.", aTargetInstance.getTarget()
                    .getName());
            inTrayManager.generateNotification(aTargetInstance.getOwner().getOid(), MessageType.CATEGORY_MISC,
                    MessageType.TARGET_INSTANCE_RESCHEDULED, aTargetInstance);

            return false;
        }

        return true;
    }

    /**
     * @see HarvestCoordinator#isMiniumBandwidthAvailable(TargetInstance) .
     */
    public boolean isMiniumBandwidthAvailable(TargetInstance aTargetInstance) {
        return harvestBandwidthManager.isMiniumBandwidthAvailable(aTargetInstance);
    }

    /**
     * @see HarvestCoordinator#listLogFiles(TargetInstance)
     */
    public List<String> listLogFiles(TargetInstance aTargetInstance) {
        if (aTargetInstance == null) {
            throw new WCTRuntimeException("A null target instance was provided to the listLogFiles command.");
        }
        return harvestLogManager.listLogFiles(aTargetInstance);
    }

    /**
     * @see HarvestCoordinator#listLogFileAttributes(TargetInstance)
     */
    public List<LogFilePropertiesDTO> listLogFileAttributes(TargetInstance aTargetInstance) {
        return harvestLogManager.listLogFileAttributes(aTargetInstance);
    }

    /**
     * @see HarvestCoordinator#tailLog(TargetInstance, String, int)
     */
    public List<String> tailLog(TargetInstance aTargetInstance, String aFileName, int aNoOfLines) {
        return harvestLogManager.tailLog(aTargetInstance, aFileName, aNoOfLines);
    }

    /**
     * @see HarvestCoordinator#countLogLines(TargetInstance, String)
     */
    public Integer countLogLines(TargetInstance aTargetInstance, String aFileName) {
        return harvestLogManager.countLogLines(aTargetInstance, aFileName);
    }

    /**
     * @see HarvestCoordinator#headLog(TargetInstance, String, int)
     */
    public List<String> headLog(TargetInstance aTargetInstance, String aFileName, int aNoOfLines) {
        return harvestLogManager.headLog(aTargetInstance, aFileName, aNoOfLines);
    }

    /**
     * @see HarvestCoordinator#getLog(TargetInstance, String, int, int)
     */
    public List<String> getLog(TargetInstance aTargetInstance, String aFileName, int aStartLine, int aNoOfLines) {
        return harvestLogManager.getLog(aTargetInstance, aFileName, aStartLine, aNoOfLines);
    }

    /**
     * @see HarvestCoordinator#getFirstLogLineBeginning(TargetInstance, String,
     * String)
     */
    public Integer getFirstLogLineBeginning(TargetInstance aTargetInstance, String aFileName, String match) {
        return harvestLogManager.getFirstLogLineBeginning(aTargetInstance, aFileName, match);
    }

    /**
     * @see HarvestCoordinator#getFirstLogLineContaining(TargetInstance, String,
     * String)
     */
    public Integer getFirstLogLineContaining(TargetInstance aTargetInstance, String aFileName, String match) {
        return harvestLogManager.getFirstLogLineContaining(aTargetInstance, aFileName, match);
    }

    /**
     * @see HarvestCoordinator#getFirstLogLineAfterTimeStamp(TargetInstance,
     * String, Long)
     */
    public Integer getFirstLogLineAfterTimeStamp(TargetInstance aTargetInstance, String aFileName, Long timestamp) {
        return harvestLogManager.getFirstLogLineAfterTimeStamp(aTargetInstance, aFileName, timestamp);
    }

    /**
     * @see HarvestCoordinator#getLogLinesByRegex(TargetInstance, String, int, String, boolean)
     */
    public List<String> getLogLinesByRegex(TargetInstance aTargetInstance, String aFileName, int aNoOfLines, String aRegex,
                                           boolean prependLineNumbers) {
        return harvestLogManager.getLogLinesByRegex(aTargetInstance, aFileName, aNoOfLines, aRegex, prependLineNumbers);
    }

    /**
     * @see HarvestCoordinator#getHopPath(TargetInstance, String, String)
     */
    public List<String> getHopPath(TargetInstance aTargetInstance, String aFileName, String aUrl) {
        return harvestLogManager.getHopPath(aTargetInstance, aFileName, aUrl);
    }

    public File getLogfile(TargetInstance aTargetInstance, String aFilename) {
        return harvestLogManager.getLogfile(aTargetInstance, aFilename);
    }

    /**
     * @see HarvestCoordinator#purgeDigitalAssets().
     */
    public void purgeDigitalAssets() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, daysBeforeDASPurge * -1);

        List<TargetInstance> tis = targetInstanceDao.findPurgeableTargetInstances(cal.getTime());
        log.debug("Attempting to purge {} harvests from the digital asset store.", tis.size());

        List<String> tiNames = tis.stream().map(TargetInstance::getJobName).collect(Collectors.toList());
        if (!tiNames.isEmpty()) {
            try {
                digitalAssetStoreFactory.getDAS().purge(tiNames);
                for (TargetInstance ti : tis) {
                    targetInstanceManager.purgeTargetInstance(ti);
                }
            } catch (DigitalAssetStoreException e) {
                log.error(MessageFormat.format("Failed to complete the purge {0}", e.getMessage()), e);
            }
        }
    }

    /**
     * @see HarvestCoordinator#purgeAbortedTargetInstances().
     */
    public void purgeAbortedTargetInstances() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, daysBeforeAbortedTargetInstancePurge * -1);

        List<TargetInstance> tis = targetInstanceDao.findPurgeableAbortedTargetInstances(cal.getTime());
        log.debug("Attempting to purge {} aborted harvests from the system.", tis.size());

        List<String> tiNames = tis.stream().map(TargetInstance::getJobName).collect(Collectors.toList());
        if (!tiNames.isEmpty()) {

            harvestAgentManager.purgeAbortedTargetInstances(tiNames);

            // call the same web-method on the DAS, to delete folders which
            // may have been created in error while a running harvest was in
            // transition from running to stopping to harvested.
            try {
                digitalAssetStoreFactory.getDAS().purgeAbortedTargetInstances(tiNames);
            } catch (DigitalAssetStoreException e) {
                log.error(MessageFormat.format("Failed to complete the purge of aborted ti data via DAS: {0}", e.getMessage()), e);
            }

            try {
                for (TargetInstance ti : tis) {
                    targetInstanceManager.purgeTargetInstance(ti);
                }
            } catch (Exception e) {
                log.error(
                        MessageFormat.format("Failed to set the purged flag on all of the eligible aborted TIs: {0}",
                                e.getMessage()), e);
            }

        }
    }

    /**
     * @param digitalAssetStoreFactory the digitalAssetStoreFactory to set
     */
    public void setDigitalAssetStoreFactory(DigitalAssetStoreFactory digitalAssetStoreFactory) {
        this.digitalAssetStoreFactory = digitalAssetStoreFactory;
    }

    /**
     * @param targetManager the targetManager to set
     */
    public void setTargetManager(TargetManager targetManager) {
        this.targetManager = targetManager;
    }

    public void setInTrayManager(InTrayManager inTrayManager) {
        this.inTrayManager = inTrayManager;
    }

    public void setHarvestResultManager(HarvestResultManager harvestResultManager) {
        this.harvestResultManager = harvestResultManager;
    }

    /**
     * @param daysBeforeDASPurge the daysBeforeDASPurge to set
     */
    public void setDaysBeforeDASPurge(int daysBeforeDASPurge) {
        this.daysBeforeDASPurge = daysBeforeDASPurge;
    }

    /**
     * @param daysBeforeAbortedTargetInstancePurge the daysBeforeAbortedTargetInstancePurge to set
     */
    public void setDaysBeforeAbortedTargetInstancePurge(int daysBeforeAbortedTargetInstancePurge) {
        this.daysBeforeAbortedTargetInstancePurge = daysBeforeAbortedTargetInstancePurge;
    }

    /**
     * @param sipBuilder the sipBuilder to set
     */
    public void setSipBuilder(SipBuilder sipBuilder) {
        this.sipBuilder = sipBuilder;
    }

    public Long createHarvestResult(HarvestResultDTO harvestResultDTO) {
        TargetInstance ti = targetInstanceDao.load(harvestResultDTO.getTargetInstanceOid());
        if (ti != null && ti.getState().equalsIgnoreCase(TargetInstance.STATE_PATCHING)) {
            HarvestResult hr = ti.getHarvestResult(harvestResultDTO.getHarvestNumber());
            if (hr == null) {
                log.error("Not able to find Harvest Result with harvestNumber {} of TI {}", harvestResultDTO.getHarvestNumber(), ti.getOid());
                return new Long(0);
            }

            hr.setState(HarvestResult.STATE_INDEXING);
            targetInstanceDao.save(hr);

            log.info("Update state for patching Harvest Result {} of TI {}", hr.getHarvestNumber(), ti.getOid());
            return hr.getOid();
        } else {
            HarvestResult result = new HarvestResult(harvestResultDTO, ti);
            ti.getHarvestResults().add(result);
            result.setState(HarvestResult.STATE_INDEXING);

            targetInstanceDao.save(result);
            return result.getOid();
        }
    }

    public void finaliseIndex(long targetInstanceId, int harvestNumber) {
        TargetInstance ti = targetInstanceDao.load(targetInstanceId);
        if (ti == null) {
            log.error("Not able to load TargetInstance: {}", targetInstanceId);
            return;
        }

        HarvestResult hr = ti.getHarvestResult(harvestNumber);
        if (hr == null) {
            log.error("Not able to load HarvestResult, job: {}, harvestNumber:{}", targetInstanceId, harvestNumber);
            return;
        }

        finaliseIndex(ti, hr);
    }

    private void finaliseIndex(TargetInstance ti, HarvestResult hr) {
        harvestResultManager.removeHarvestResult(ti.getOid(), hr.getHarvestNumber());

        hr.setState(HarvestResult.STATE_UNASSESSED);
        targetInstanceDao.save(hr);
        harvestQaManager.triggerAutoQA(hr);

        if (ti.getState().equalsIgnoreCase(TargetInstance.STATE_PATCHING)) {
            ti.setState(TargetInstance.STATE_HARVESTED);
            targetInstanceDao.save(ti);
            PatchUtil.modifier.moveJob2History(visualizationDirectoryManager.getBaseDir(), ti.getOid(), hr.getHarvestNumber());
        }

        // run the QA recommendation service to derive the Quality Indicators
        harvestQaManager.initialiseQaRecommentationService(hr.getOid());
    }

    public void runQaRecommentationService(TargetInstance ti) {
        harvestQaManager.runQaRecommentationService(ti);
    }

    public void notifyAQAComplete(String aqaId) {
        log.debug("Received notifyAQAComplete for job(" + aqaId + ").");

        try {
            String[] ids = aqaId.split("-");
            long tiOid = Long.parseLong(ids[0]);
            TargetInstance ti = targetInstanceDao.load(tiOid);
            int harvestNumber = Integer.parseInt(ids[1]);
            HarvestResult result = ti.getHarvestResult(harvestNumber);

            // Send a message.
            inTrayManager.generateNotification(ti.getOwner().getOid(), MessageType.CATEGORY_MISC,
                    MessageType.NOTIFICATION_AQA_COMPLETE, result);
        } catch (Exception e) {
            log.error("Unable to notify AQA Id: " + aqaId, e);
        }
    }

    public void removeIndexes(TargetInstance ti) {
        List<HarvestResult> results = ti.getHarvestResults();
        if (results != null) {
            Iterator<HarvestResult> it = results.iterator();
            while (it.hasNext()) {
                HarvestResult hr = it.next();
                if (hr.getState() != HarvestResult.STATE_REJECTED) {
                    // Rejected HRs have already had their indexes removed
                    // The endorsing process should mean there is only one none
                    // rejected HR
                    removeIndexes(hr);
                }
            }
        }
    }

    public void removeIndexes(HarvestResult hr) {
        DigitalAssetStore das = digitalAssetStoreFactory.getDAS();
        try {
            log.info("Attempting to remove indexes for TargetInstance " + hr.getTargetInstance().getOid() + " HarvestNumber "
                    + hr.getHarvestNumber());
            das.initiateRemoveIndexes(new HarvestResultDTO(hr.getOid(), hr.getTargetInstance().getOid(), hr.getCreationDate(),
                    hr.getHarvestNumber(), hr.getProvenanceNote()));
        } catch (DigitalAssetStoreException e) {
            log.error(MessageFormat.format(
                    "Could not send initiateRemoveIndexes message to the DAS for TargetInstance {0} HarvestNumber {1}: {2}", hr
                            .getTargetInstance().getOid(), hr.getHarvestNumber(), e.getMessage()), e);
        }
    }

    public void completeArchiving(Long targetInstanceOid, String archiveIID) {
        // Update the state.
        TargetInstance ti = targetInstanceDao.load(targetInstanceOid);

        // Sometimes the call (from store) to this method enters into a race condition with the state transition
        // to "Archiving" triggered by the UI. We pause for 300 ms here if it looks like that's about to happen
        if (!ti.getState().equals(TargetInstance.STATE_ARCHIVING)) {
            log.warn(String.format("Target instance %d was not yet in state %s, pausing before setting it to %s",
                    targetInstanceOid,
                    TargetInstance.STATE_ARCHIVING,
                    TargetInstance.STATE_ARCHIVED));
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }
            ti = targetInstanceDao.load(targetInstanceOid); // ti will most likely be stale, reload it
        }

        ti.setArchiveIdentifier(archiveIID);
        ti.setArchivedTime(new Date());
        ti.setState(TargetInstance.STATE_ARCHIVED);
        targetInstanceManager.save(ti);

        // Remove any indexes
        removeIndexes(ti);

        // Send a message.
        inTrayManager.generateNotification(ti.getOwner().getOid(), MessageType.CATEGORY_MISC,
                MessageType.NOTIFICATION_ARCHIVE_SUCCESS, ti);

    }

    public void failedArchiving(Long targetInstanceOid, String message) {
        // Update the state.
        TargetInstance ti = targetInstanceDao.load(targetInstanceOid);
        ti.setState(TargetInstance.STATE_ENDORSED);
        targetInstanceManager.save(ti);

        log.error("Failed to archive - trying to send message");

        // Send a message.
        inTrayManager.generateNotification(ti.getOwner().getOid(), MessageType.CATEGORY_MISC, "subject.archived.failed",
                new Object[]{ti.getTarget().getName(), ti.getResourceName()}, "message.archived.failed", new Object[]{
                        ti.getTarget().getName(), ti.getResourceName(), message}, ti, true);
    }

    /**
     * @param targetInstanceManager the targetInstanceManager to set
     */
    public void setTargetInstanceManager(TargetInstanceManager targetInstanceManager) {
        this.targetInstanceManager = targetInstanceManager;
    }

    @Override
    public void pauseAgent(String agentName) {
        harvestAgentManager.pauseAgent(agentName);
    }

    @Override
    public void resumeAgent(String agentName) {
        harvestAgentManager.resumeAgent(agentName);
    }

    @Override
    public int getHarvestOptimizationLookAheadHours() {
        return harvestOptimizationLookAheadHours;
    }

    public void setHarvestOptimizationLookAheadHours(int harvestOptimizeLookAheadHours) {
        this.harvestOptimizationLookAheadHours = harvestOptimizeLookAheadHours;
    }

    @Override
    public void setHarvestOptimizationEnabled(boolean state) {
        this.harvestOptimizationEnabled = state;
    }

    @Override
    public boolean isHarvestOptimizationEnabled() {
        return harvestOptimizationEnabled;
    }

    public int getNumHarvestersExcludedFromOptimisation() {
        return numHarvestersExcludedFromOptimisation;
    }

    public void setNumHarvestersExcludedFromOptimisation(int numHarvestersExcludedFromOptimisation) {
        this.numHarvestersExcludedFromOptimisation = numHarvestersExcludedFromOptimisation;
    }

    @Override
    public HashMap<String, HarvestAgentStatusDTO> getHarvestAgents() {
        return harvestAgentManager.getHarvestAgents();
    }

    @Override
    public void recoverHarvests(String baseUrl, String service) {
        HarvestAgentStatusDTO tempHarvestAgentStatusDTO = new HarvestAgentStatusDTO();
        tempHarvestAgentStatusDTO.setBaseUrl(baseUrl);
        tempHarvestAgentStatusDTO.setService(service);
        this.recoverHarvests(tempHarvestAgentStatusDTO);
    }

    @Override
    public ModifyRowFullData uploadFile(long job, int harvestResultNumber, ModifyRowFullData cmd) {
        ModifyRowFullData result = this.saveImportedFile(job, cmd);

        return result;
    }

    private ModifyRowFullData saveImportedFile(long job, ModifyRowFullData cmd) {
        File uploadedFilePath = new File(visualizationDirectoryManager.getUploadDir(job));
        if (!uploadedFilePath.exists()) {
            uploadedFilePath.mkdirs();
        }

        String cacheFileName = UUID.randomUUID().toString();

        uploadedFilePath = new File(uploadedFilePath, cacheFileName);
        if (uploadedFilePath.exists()) {
            uploadedFilePath.delete();
        }
        cmd.setCachedFileName(cacheFileName);

        try {
            int idx = cmd.getUploadFileContent().indexOf("base64");
            byte[] doc = Base64.getDecoder().decode(cmd.getUploadFileContent().substring(idx + 7));
            if (uploadedFilePath.exists()) {
                Files.write(uploadedFilePath.toPath(), doc, StandardOpenOption.APPEND);
            } else {
                Files.write(uploadedFilePath.toPath(), doc, StandardOpenOption.CREATE_NEW);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            cmd.setRespCode(VisualizationConstants.RESP_CODE_ERROR_FILE_IO);
            cmd.setRespMsg("Failed to write upload file to " + uploadedFilePath.getAbsolutePath());
            return cmd;
        }

        cmd.setRespCode(VisualizationConstants.RESP_CODE_SUCCESS);
        cmd.setRespMsg("OK");
        return cmd;
    }

    public ModifyResult modificationDownloadFile(long job, int harvestResultNumber, ModifyRowFullData metadata, HttpServletResponse resp) {
        ModifyResult result = new ModifyResult();

        //Ignore elements which are not 'file'
        if (!metadata.getOption().equalsIgnoreCase("FILE")) {
            result.setRespCode(VisualizationConstants.RESP_CODE_INVALID_REQUEST);
            result.setRespMsg("Invalid metadata type: " + metadata.getOption());
            return result;
        }

        //Check does file exist
        File uploadedFilePath = new File(visualizationDirectoryManager.getUploadDir(job), metadata.getCachedFileName());
        if (!uploadedFilePath.exists()) {
            result.setRespCode(VisualizationConstants.FILE_EXIST_NO); //Not exist
            result.setRespMsg("File or metadata is not uploaded");
            return result;
        }

        try {
            Files.copy(uploadedFilePath.toPath(), resp.getOutputStream());
        } catch (IOException e) {
            result.setRespCode(VisualizationConstants.RESP_CODE_ERROR_SYSTEM_ERROR);
            result.setRespMsg(e.getMessage());
            log.error(metadata.getRespMsg());
            return result;
        }

        result.setRespCode(VisualizationConstants.RESP_CODE_SUCCESS); //Exist
        result.setRespMsg("OK");
        return result;
    }

    public ModifyResult checkFiles(long job, int harvestResultNumber, List<ModifyRowFullData> items) {
        return this.checkFiles(job, items);
    }

    private ModifyResult checkFiles(long job, List<ModifyRowFullData> items) {
        ModifyResult result = new ModifyResult();

        /**
         * Checking do files exist and attaching properties for existing files
         */
        boolean isValid = true;
        for (ModifyRowFullData metadata : items) {
            //Ignore elements which are not 'file'
            if (!metadata.getOption().equalsIgnoreCase("FILE")) {
                continue;
            }

            //Check do files exist: walk through all elemenets
            File uploadedFilePath = new File(visualizationDirectoryManager.getUploadDir(job), metadata.getCachedFileName());
            if (!uploadedFilePath.exists()) {
                metadata.setRespCode(VisualizationConstants.FILE_EXIST_NO); //Not exist
                metadata.setRespMsg("File or metadata is not uploaded");
                isValid = false;
            }
        }

        if (!isValid) {
            result.setRespCode(VisualizationConstants.FILE_EXIST_NO);
            result.setRespMsg("Not all files are uploaded");
        } else {
            result.setRespCode(VisualizationConstants.RESP_CODE_FILE_EXIST);
            result.setRespMsg("OK");
        }
        result.setMetadataDataset(items);

        return result;
    }

    @Override
    public ModifyResult applyPruneAndImport(ModifyApplyCommand cmd) {
        /**
         * Checking do files exist and attaching properties for existing files
         */
        ModifyResult result = this.checkFiles(cmd.getTargetInstanceId(), cmd.getDataset());
        if (result.getRespCode() != VisualizationConstants.RESP_CODE_SUCCESS &&
                result.getRespCode() != VisualizationConstants.RESP_CODE_FILE_EXIST) {
            log.error(result.getRespMsg());
            return result;
        }

        //Checking is there any other patching job for this targetInstance
        TargetInstance ti = targetInstanceManager.getTargetInstance(cmd.getTargetInstanceId());
        if (ti == null) {
            result.setRespCode(VisualizationConstants.RESP_CODE_ERROR_SYSTEM_ERROR);
            result.setRespMsg("Could not find TargetInstance: " + cmd.getTargetInstanceId());
            log.error(result.getRespMsg());
            return result;
        }

        synchronized (ti.getJobName()) {
            if (!ti.getState().equalsIgnoreCase(TargetInstance.STATE_HARVESTED)) {
                result.setRespCode(VisualizationConstants.RESP_CODE_ERROR_INVALID_TI_STATE);
                result.setRespMsg("Invalid target instance state for modification: " + ti.getState());
                log.error(result.getRespMsg());
                return result;
            }
        }

        boolean isNeedHarvest = false;
        for (ModifyRowFullData row : cmd.getDataset()) {
            if (row.getOption().equalsIgnoreCase(ModifyApplyCommand.OPTION_RECRAWL)) {
                isNeedHarvest = true;
                break;
            }
        }

        HarvestResultDTO hrDTO = null;
        try {
            hrDTO = this.savePruneAndImportCommandApply(cmd, isNeedHarvest);
        } catch (WCTRuntimeException | IOException e) {
            result.setRespCode(VisualizationConstants.RESP_CODE_ERROR_SYSTEM_ERROR);
            result.setRespMsg(e.getMessage());
            return result;
        }

        if (!isNeedHarvest) {
            //If there is no source urls included, then skipping harvesting
            result = this.pushPruneAndImport(cmd);
        } else {
            result = patchHarvest(cmd);
        }

        if (result != null) {
            result.setDerivedHarvestResult(hrDTO);
            log.debug(result.getRespMsg());
        }

        return result;
    }

    @Override
    public ModifyResult patchHarvest(ModifyApplyCommand cmd) {
        ModifyResult result = new ModifyResult();

        TargetInstance ti = targetInstanceDao.load(cmd.getTargetInstanceId());
        HarvestResult hr = ti.getHarvestResult(cmd.getNewHarvestResultNumber());

        String instanceAgencyName = ti.getOwner().getAgency().getName();

        //Consider the HarvesterType of default modification profile has the same type of target instance profile
        HarvestAgentStatusDTO allowedAgent = harvestAgentManager.getHarvester(instanceAgencyName, ti.getProfile().getHarvesterType());

        result.setRespCode(VisualizationConstants.RESP_CODE_SUCCESS);
        if (allowedAgent != null) {
            boolean processed = this.patchHarvest(ti, hr, allowedAgent);
            if (processed) {
                result.setRespMsg("Patch crawling job started");
            } else {
                result.setRespMsg("Not able to launch patch crawling job now");
            }
        } else {
            ti.setScheduledTime(new Date());
            targetInstanceManager.save(ti);
            result.setRespMsg("No idle agent, modification harvest job is scheduled");
        }
        return result;
    }

    @Override
    public SeedHistorySetDTO dasQuerySeedHistory(long targetInstanceOid, int harvestNumber) {
        SeedHistorySetDTO seedHistorySetDTO = new SeedHistorySetDTO();
        seedHistorySetDTO.setTargetInstanceId(targetInstanceOid);
        TargetInstance ti = targetInstanceManager.getTargetInstance(targetInstanceOid);
        if (ti == null) {
            seedHistorySetDTO.setSeeds(new HashSet<>());
        } else {
            ti.getSeedHistory().forEach(seedHistory -> {
                seedHistorySetDTO.getSeeds().add(new SeedHistoryDTO(seedHistory));
            });
        }
        return seedHistorySetDTO;
    }

    @Override
    public void dasModificationComplete(long job, int harvestResultNumber) {
        TargetInstance ti = targetInstanceDao.load(job);
        if (ti == null) {
            log.error("Not able to load TargetInstance: {}", job);
            return;
        }

        HarvestResult harvestResult = ti.getHarvestResult(harvestResultNumber);
        if (harvestResult == null) {
            log.error("Not able to load HarvestResult, job: {}, harvestNumber:{}", job, harvestResultNumber);
            return;
        }

        dasModificationComplete(ti, harvestResult);
    }

    private void dasModificationComplete(TargetInstance ti, HarvestResult harvestResult) {
        //Update status to scheduled
        harvestBandwidthManager.sendBandWidthRestrictions();

        // Ask the DigitalAssetStore to index the ARC
        initiateIndexing(ti, harvestResult);

        log.info("'Modification Complete' message processed for job: " + ti.getOid() + ".");
    }

    private void initiateIndexing(long targetInstanceId, int harvestResultNumber) {
        TargetInstance ti = targetInstanceDao.load(targetInstanceId);
        if (ti == null) {
            throw new WCTRuntimeException("Unknown TargetInstance oid recieved " + targetInstanceId + " failed to load HarvestResult.");
        }

        HarvestResult hr = ti.getHarvestResult(harvestResultNumber);
        if (hr == null) {
            throw new WCTRuntimeException("Unknown HarvestResult number recieved " + harvestResultNumber + " failed to load HarvestResult.");
        }

        initiateIndexing(ti, hr);
    }

    private void initiateIndexing(TargetInstance ti, HarvestResult hr) {
        try {
            hr.setState(HarvestResult.STATE_INDEXING);
            targetInstanceDao.save(hr);

            harvestResultManager.updateHarvestResultStatus(ti.getOid(), hr.getHarvestNumber(), HarvestResult.STATE_INDEXING, HarvestResult.STATUS_SCHEDULED);

            digitalAssetStoreFactory.getDAS().initiateIndexing(
                    new HarvestResultDTO(hr.getOid(), hr.getTargetInstance().getOid(), hr
                            .getCreationDate(), hr.getHarvestNumber(), hr.getProvenanceNote()));
        } catch (DigitalAssetStoreException e) {
            log.error("Could not send initiateIndexing message to the DAS", e);
        }
    }

    @Override
    public boolean pushPruneAndImport(long targetInstanceId, int harvestResultNumber) {
        TargetInstance ti = targetInstanceDao.load(targetInstanceId);
        if (ti == null) {
            throw new WCTRuntimeException("Unknown TargetInstance oid recieved " + targetInstanceId + " failed to load HarvestResult.");
        }

        HarvestResult hr = ti.getHarvestResult(harvestResultNumber);
        if (hr == null) {
            throw new WCTRuntimeException("Unknown HarvestResult number recieved " + harvestResultNumber + " failed to load HarvestResult.");
        }

        return pushPruneAndImport(ti, hr);
    }

    private boolean pushPruneAndImport(TargetInstance ti, HarvestResult hr) {
        if (!ti.getState().equalsIgnoreCase(TargetInstance.STATE_PATCHING)) {
            log.error("Invalid TargetInstance state: {}", ti.getState());
            return false;
        }

        if (hr.getState() == HarvestResult.STATE_ABORTED) {
            log.error("Invalid HarvestResult state: {}", hr.getState());
            return false;
        }

        ModifyApplyCommand cmd = (ModifyApplyCommand) PatchUtil.modifier.readPatchJob(visualizationDirectoryManager.getBaseDir(), ti.getOid(), hr.getHarvestNumber());
        if (cmd == null) {
            log.error("Could not load ModifyApplyCommand of target instance: {}", ti.getOid(), hr.getHarvestNumber());
            return false;
        }

        //Next step is to excute pruning and imporing, so change the state and status to Modifying Scheduled
        ModifyResult result = pushPruneAndImport(cmd);
        return result.getRespCode() == VisualizationConstants.RESP_CODE_SUCCESS;
    }

    private ModifyResult pushPruneAndImport(ModifyApplyCommand cmd) {
        DigitalAssetStore digitalAssetStoreClient = digitalAssetStoreFactory.getDAS();
        ModifyResult result = digitalAssetStoreClient.initialPruneAndImport(cmd);
        if (result != null && result.getRespCode() != VisualizationConstants.RESP_CODE_SUCCESS) {
            log.error("Failed to request modification, {} {}", result.getRespCode(), result.getRespMsg());
        }
        return result;
    }

    private HarvestResultDTO savePruneAndImportCommandApply(ModifyApplyCommand cmd, boolean isNeedHarvest) throws IOException, WCTRuntimeException {
        TargetInstance ti = targetInstanceDao.load(cmd.getTargetInstanceId());
        HarvestResult res = targetInstanceDao.getHarvestResult(cmd.getHarvestResultId()); //Query the old Harvest Result
        if (ti == null || res == null) {
            throw new WCTRuntimeException(
                    String.format("Target instance (OID=%d) or HarvestResult (IOD=%d) does not exist in DB"
                            , cmd.getTargetInstanceId()
                            , cmd.getHarvestResultId()));
        }

        int newHarvestResultNumber = 0;
        List<HarvestResult> harvestResultList = ti.getHarvestResults();
        for (HarvestResult hr : harvestResultList) {
            if (hr != null && hr.getHarvestNumber() > newHarvestResultNumber) {
                newHarvestResultNumber = hr.getHarvestNumber();
            }
        }
        newHarvestResultNumber += 1;

        // Create a new harvest result.
        HarvestResult hr = new HarvestResult(ti, newHarvestResultNumber);
        hr.setDerivedFrom(cmd.getHarvestResultNumber());
        hr.setProvenanceNote(cmd.getProvenanceNote());
        //        hr.addModificationNotes();
        hr.setTargetInstance(ti);

        int state = isNeedHarvest ? HarvestResult.STATE_CRAWLING : HarvestResult.STATE_MODIFYING;
        hr.setState(state);

        if (AuthUtil.getRemoteUserObject() != null) {
            hr.setCreatedBy(AuthUtil.getRemoteUserObject());
        } else {
            hr.setCreatedBy(res.getCreatedBy());
        }

        ti.getHarvestResults().add(hr);
        ti.setState(TargetInstance.STATE_PATCHING);

        // Save to the database.
        targetInstanceDao.save(hr);
        targetInstanceDao.save(ti);

        cmd.setNewHarvestResultNumber(newHarvestResultNumber);

        PatchUtil.modifier.savePatchJob(visualizationDirectoryManager.getBaseDir(), cmd);

        HarvestResultDTO hrDTO = new HarvestResultDTO();
        hrDTO.setOid(hr.getOid());
        hrDTO.setTargetInstanceOid(ti.getOid());
        hrDTO.setHarvestNumber(newHarvestResultNumber);
        hrDTO.setDerivedFrom(res.getHarvestNumber());

        return hrDTO;
    }

    public String probeMimeType(File file) throws IOException {
//        Files.probeContentType(file.toPath());
        return tika.detect(file);
    }

    @Override
    public void dasDownloadFile(long targetInstanceId, int harvestResultNumber, String fileName, HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        File f = new File(visualizationDirectoryManager.getUploadDir(targetInstanceId), fileName);

        StringBuilder headers = new StringBuilder();
        headers.append("HTTP/1.1 200 OK\n");
        headers.append("Content-Type: ");
        headers.append(probeMimeType(f)).append("\n");
        headers.append("Content-Length: ");
        headers.append(f.length()).append("\n");
        headers.append("Connection: close\n");

        log.debug("Store <-- Webapp fileName: {}, headers: {}", fileName, headers.toString());

        OutputStream outputStream = rsp.getOutputStream();
        outputStream.write(headers.toString().getBytes());
        outputStream.write("\n".getBytes());

        InputStream inputStream = new BufferedInputStream(new FileInputStream(f));

        WctUtils.copy(inputStream, outputStream);

        log.debug("Finished file download: {}", fileName);
    }

    @Override
    public void dasHeartbeat(List<HarvestResultDTO> harvestResultDTOList) {
        for (HarvestResultDTO hrDTO : harvestResultDTOList) {
            if (hrDTO.getState() == HarvestResult.STATE_MODIFYING && hrDTO.getStatus() == HarvestResult.STATUS_FINISHED) {
                dasModificationComplete(hrDTO.getTargetInstanceOid(), hrDTO.getHarvestNumber());
            } else if (hrDTO.getState() == HarvestResult.STATE_INDEXING && hrDTO.getStatus() == HarvestResult.STATUS_FINISHED) {
                finaliseIndex(hrDTO.getTargetInstanceOid(), hrDTO.getHarvestNumber());
            }
        }

        harvestResultManager.updateHarvestResultsStatus(harvestResultDTOList);
    }

    @Override
    public void dasUpdateHarvestResultStatus(HarvestResultDTO hrDTO) {
//        harvestResultManager.addHarvestResult(hrDTO);
        harvestResultManager.updateHarvestResultStatus(hrDTO);
        if (hrDTO.getState() == HarvestResult.STATE_INDEXING && hrDTO.getStatus() == HarvestResult.STATUS_FINISHED) {
            finaliseIndex(hrDTO.getTargetInstanceOid(), hrDTO.getHarvestNumber());
        } else if (hrDTO.getState() == HarvestResult.STATE_MODIFYING && hrDTO.getStatus() == HarvestResult.STATUS_FINISHED) {
            initiateIndexing(hrDTO.getTargetInstanceOid(), hrDTO.getHarvestNumber());
        }
    }
}