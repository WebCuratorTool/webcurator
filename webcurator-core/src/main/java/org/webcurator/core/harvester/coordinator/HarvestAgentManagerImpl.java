package org.webcurator.core.harvester.coordinator;

import java.text.MessageFormat;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.common.Environment;
import org.webcurator.core.common.EnvironmentFactory;
import org.webcurator.core.coordinator.HarvestResultManager;
import org.webcurator.core.coordinator.WctCoordinator;
import org.webcurator.core.harvester.agent.HarvestAgent;
import org.webcurator.core.harvester.agent.HarvestAgentFactory;
import org.webcurator.core.reader.LogReader;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.core.util.PatchUtil;
import org.webcurator.domain.TargetInstanceDAO;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.HarvesterStatus;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;
import org.webcurator.domain.model.core.harvester.agent.HarvesterStatusDTO;

@SuppressWarnings("all")
public class HarvestAgentManagerImpl implements HarvestAgentManager {
    static Set<Long> targetInstanceLocks = Collections.synchronizedSet(new HashSet<>());

    HashMap<String, HarvestAgentStatusDTO> harvestAgents = new HashMap<>();

    private final Logger log = LoggerFactory.getLogger(getClass());
    private TargetInstanceDAO targetInstanceDao;
    private TargetInstanceManager targetInstanceManager;
    private HarvestAgentFactory harvestAgentFactory;
    private WctCoordinator wctCoordinator;
    private HarvestResultManager harvestResultManager;

    @Override
    public void heartbeat(HarvestAgentStatusDTO aStatus) {
        if (harvestAgents.containsKey(aStatus.getName())) {
            log.debug("Updating status for {}", aStatus.getName());
        } else {
            log.info("Registering harvest agent " + aStatus.getName());
        }

        aStatus.setLastUpdated(new Date());
        HarvestAgentStatusDTO currentStatus = harvestAgents.get(aStatus.getName());
        if (currentStatus != null) {
            aStatus.setAcceptTasks(currentStatus.isAcceptTasks());
        }
        harvestAgents.put(aStatus.getName(), aStatus);

        HashMap<String, HarvesterStatusDTO> harvesterStatusMap = aStatus.getHarvesterStatus();
        for (String key : harvesterStatusMap.keySet()) {
            long tiOid = 0;
            int harvestResultNumber = 0;
            if (key.startsWith("mod")) {
                String[] items = key.split("_");
                tiOid = Long.parseLong(items[1]);
                harvestResultNumber = Integer.parseInt(items[2]);
            } else {
                tiOid = Long.parseLong(key.substring(key.lastIndexOf("-") + 1));
            }

            // lock the ti for update
            if (!lock(tiOid)) {
                log.debug("Skipping heartbeat, found locked target instance: " + tiOid);
                break;
            }
            try {
                log.debug("Obtained lock for ti {}", tiOid);
                TargetInstance ti = targetInstanceDao.load(tiOid);
                HarvesterStatusDTO harvesterStatusDto = (HarvesterStatusDTO) harvesterStatusMap.get(key);

                updateStatusWithEnvironment(harvesterStatusDto);
                HarvesterStatus harvesterStatus = createHarvesterStatus(ti, harvesterStatusDto);

                log.debug("Heartbeat for ti: {}, state: {}", tiOid, harvesterStatus.getStatus());

                String harvesterStatusValue = harvesterStatus.getStatus();
                if (StringUtils.isEmpty(harvesterStatusDto.getHarvesterState()) || StringUtils.isEmpty(harvesterStatusValue) || harvesterStatusDto.getHarvesterState().equals("H3 Job Not Found")) {
                    log.error("harvesterStatusValue is null, tiOid:{}", tiOid);
                    doHeartbeatLaunchFailed(ti, harvestResultNumber);
                    return;
                }

                if (harvesterStatusValue.startsWith("Paused")) {
                    doHeartbeatPaused(ti, harvestResultNumber);
                    return;
                }

                // We have seen cases where a running Harvest is showing as Queued
                // in the UI. Once in this state, the user has no control over the
                // harvest and cannot use it. This work around means that any
                // TIs in the wrong state will be corrected on the next heartbeat
                if (harvesterStatusValue.startsWith("Running")) {
                    doHeartbeatRunning(aStatus, ti, harvesterStatus, harvestResultNumber);
                    return;
                }

                if (harvesterStatusValue.startsWith("Finished")) {
                    doHeartbeatFinished(ti, harvestResultNumber);
                    return;
                }

                // This is a required because when a
                // "Could not launch job - Fatal InitializationException" job occurs
                // We do not get a notification that causes the job to stop nicely
                if (harvesterStatusValue.startsWith("Could not launch job - Fatal InitializationException")) {
                    doHeartbeatLaunchFailed(ti, harvestResultNumber);
                    return;
                }
            } catch (Exception e) {
                log.error("Failed to process: {}", tiOid, e);
            } finally {
                unLock(tiOid);
                log.debug("Released lock for ti " + tiOid);
            }
        }
    }

    private HarvesterStatus createHarvesterStatus(TargetInstance ti, HarvesterStatusDTO harvesterStatusDto) {
        HarvesterStatus harvesterStatus = null;
        if (ti.getStatus() == null) {
            harvesterStatus = new HarvesterStatus(harvesterStatusDto);
            ti.setStatus(harvesterStatus);
            harvesterStatus.setOid(ti.getOid());
        } else {
            harvesterStatus = ti.getStatus();
            harvesterStatus.update(harvesterStatusDto);
        }
        return harvesterStatus;
    }

    private void updateStatusWithEnvironment(HarvesterStatusDTO harvesterStatusDto) {
        // Update the harvesterStatus with current versions
        Environment env = EnvironmentFactory.getEnv();
        harvesterStatusDto.setApplicationVersion(env.getApplicationVersion());
        if (harvesterStatusDto.getHeritrixVersion() == null) {
            harvesterStatusDto.setHeritrixVersion(env.getHeritrixVersion());
        }
    }

    private void doHeartbeatLaunchFailed(TargetInstance ti, int harvestResultNumber) {
        String state = ti.getState();
        if (state.equals(TargetInstance.STATE_RUNNING)) {
            ti.setState(TargetInstance.STATE_ABORTED);
            targetInstanceManager.save(ti);
            HarvestAgentStatusDTO hs = getHarvestAgentStatusFor(ti.getJobName());
            if (hs == null) {
                log.warn("Forced Abort Failed. Failed to find the Harvest Agent for the Job {}.", ti.getJobName());
            } else {
                HarvestAgent agent = harvestAgentFactory.getHarvestAgent(hs);
                agent.abort(ti.getJobName());
            }
        } else if (state.equals(TargetInstance.STATE_PATCHING)) {
            String jobName = PatchUtil.getPatchJobName(ti.getOid(), harvestResultNumber);

            ti.setState(TargetInstance.STATE_HARVESTED);
            targetInstanceManager.save(ti);

            HarvestAgentStatusDTO hs = getHarvestAgentStatusFor(jobName);
            if (hs == null) {
                log.warn("Forced Abort Failed. Failed to find the Harvest Agent for the Job {}.", jobName);
            } else {
                log.debug("Forced Abort job: {}.", jobName);
                HarvestAgent agent = harvestAgentFactory.getHarvestAgent(hs);
                agent.abort(jobName);
            }

            harvestResultManager.updateHarvestResultStatus(ti.getOid(), harvestResultNumber, HarvestResult.STATE_ABORTED, HarvestResult.STATUS_TERMINATED);
        }
    }

    private void doHeartbeatFinished(TargetInstance ti, int harvestResultNumber) {
        String state = ti.getState();
        if (state.equals(TargetInstance.STATE_RUNNING)) {
            ti.setState(TargetInstance.STATE_STOPPING);
            targetInstanceManager.save(ti);
        } else if (state.equals(TargetInstance.STATE_PATCHING)) {
            log.info("Recrawle job is stopping, tiOID:{}, hrNum:{}", ti.getOid(), harvestResultNumber);
        }
    }

    private void doHeartbeatRunning(HarvestAgentStatusDTO aStatus, TargetInstance ti, HarvesterStatus harvesterStatus, int harvestResultNumber) {
        String state = ti.getState();
        if (state.equals(TargetInstance.STATE_PAUSED) || state.equals(TargetInstance.STATE_QUEUED)) {
            if (state.equals(TargetInstance.STATE_QUEUED)) {
                log.info("HarvestCoordinator: Target Instance state changed from Queued to Running for target instance {}", ti
                        .getOid().toString());
            }
            if (ti.getActualStartTime() == null) {
                // This was not set up correctly when harvest was initiated
                Date now = new Date();
                Date startTime = new Date(now.getTime() - harvesterStatus.getElapsedTime());
                ti.setActualStartTime(startTime);
                ti.setHarvestServer(aStatus.getName());

                log.info("HarvestCoordinator: Target Instance start time set for target instance " + ti.getOid().toString());
            }
            ti.setState(TargetInstance.STATE_RUNNING);
            targetInstanceManager.save(ti);
        } else if (state.equals(TargetInstance.STATE_PATCHING)) {
            harvestResultManager.updateHarvestResultStatus(ti.getOid(), harvestResultNumber, HarvestResult.STATE_CRAWLING, HarvestResult.STATUS_RUNNING);
        }
    }

    private void doHeartbeatPaused(TargetInstance ti, int harvestResultNumber) {
        String state = ti.getState();
        if (state.equals(TargetInstance.STATE_RUNNING)) {
            ti.setState(TargetInstance.STATE_PAUSED);
            targetInstanceManager.save(ti);
        } else if (state.equals(TargetInstance.STATE_PATCHING)) {
            harvestResultManager.updateHarvestResultStatus(ti.getOid(), harvestResultNumber, HarvestResult.STATE_CRAWLING, HarvestResult.STATUS_PAUSED);
        }
    }

    /**
     * @return a harvest agent status for the specified job name
     */
    HarvestAgentStatusDTO getHarvestAgentStatusFor(String aJobName) {
        for (HarvestAgentStatusDTO agentStatus : harvestAgents.values()) {
            if (agentStatus.getHarvesterStatus() != null) {
                if (agentHasJob(aJobName, agentStatus))
                    return agentStatus;
            }
        }
        return null;
    }

    boolean agentHasJob(String aJobName, HarvestAgentStatusDTO agentStatus) {
        for (Object hsObject : agentStatus.getHarvesterStatus().values()) {
            HarvesterStatusDTO harvesterStatus = (HarvesterStatusDTO) hsObject;
            if (harvesterStatus.getJobName().equals(aJobName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateProfileOverrides(TargetInstance aTargetInstance, String profileString) {
        HarvestAgentStatusDTO status = getHarvestAgentStatusFor(aTargetInstance.getJobName());
        if (status == null) {
            log.warn("Update Profile Overrides Failed. Failed to find the Harvest Agent for the Job {}.",
                    aTargetInstance.getJobName());
            return;
        }
        HarvestAgent agent = harvestAgentFactory.getHarvestAgent(status);
        agent.updateProfileOverrides(aTargetInstance.getJobName(), profileString);
    }

    @Override
    public void pause(TargetInstance aTargetInstance) {
        String jobName = aTargetInstance.getJobName();
        HarvestAgentStatusDTO status = getHarvestAgentStatusFor(jobName);
        if (status == null) {
            log.warn("PAUSE Failed. Failed to find the Harvest Agent for the Job {}.", jobName);
            return;
        }

        HarvestAgent agent = harvestAgentFactory.getHarvestAgent(status);

        // Update the state of the allocated Target Instance
        aTargetInstance.setState(TargetInstance.STATE_PAUSED);
        // Note that resume uses a different method to save the target instance!
        targetInstanceDao.save(aTargetInstance);

        agent.pause(jobName);
    }

    @Override
    public void resume(TargetInstance aTargetInstance) {
        HarvestAgentStatusDTO status = getHarvestAgentStatusFor(aTargetInstance.getJobName());
        if (status == null) {
            log.warn("RESUME Failed. Failed to find the Harvest Agent for the Job {}.", aTargetInstance.getJobName());
            return;
        }
        HarvestAgent agent = harvestAgentFactory.getHarvestAgent(status);

        // Update the state of the allocated Target Instance
        aTargetInstance.setState(TargetInstance.STATE_RUNNING);
        // Note that pause uses a different method to save the target instance!
        targetInstanceManager.save(aTargetInstance);

        agent.resume(aTargetInstance.getJobName());
    }

    @Override
    public void abort(TargetInstance aTargetInstance) {
        // Update the state of the allocated Target Instance
        aTargetInstance.setState(TargetInstance.STATE_ABORTED);
        targetInstanceDao.save(aTargetInstance);

        HarvestAgentStatusDTO status = getHarvestAgentStatusFor(aTargetInstance.getJobName());
        if (status == null) {
            log.warn("ABORT Failed. Failed to find the Harvest Agent for the Job {}.", aTargetInstance.getJobName());
        } else {
            HarvestAgent agent = harvestAgentFactory.getHarvestAgent(status);
            try {
                agent.abort(aTargetInstance.getJobName());
            } catch (RuntimeException e) {
                log.warn("ABORT Failed. Failed Abort the Job {} on the Harvest Agent {}.", aTargetInstance.getJobName(),
                        agent.getName());
            }
        }
    }

    @Override
    public void stop(TargetInstance aTargetInstance) {
        HarvestAgentStatusDTO status = getHarvestAgentStatusFor(aTargetInstance.getJobName());
        if (status == null) {
            log.warn("STOP Failed. Failed to find the Harvest Agent for the Job {}.", aTargetInstance.getJobName());
            return;
        }
        HarvestAgent agent = harvestAgentFactory.getHarvestAgent(status);

        agent.stop(aTargetInstance.getJobName());
    }

    @Override
    public void pauseAll() {
        for (HarvestAgentStatusDTO agentDTO : harvestAgents.values()) {
            if (agentDTO.getHarvesterStatus() != null && !agentDTO.getHarvesterStatus().isEmpty()) {
                HarvestAgent agent = harvestAgentFactory.getHarvestAgent(agentDTO);
                agent.pauseAll();
            }
        }
    }

    @Override
    public void resumeAll() {
        for (HarvestAgentStatusDTO agentDTO : harvestAgents.values()) {
            if (agentDTO.getHarvesterStatus() != null && !agentDTO.getHarvesterStatus().isEmpty()) {
                HarvestAgent agent = harvestAgentFactory.getHarvestAgent(agentDTO);
                agent.resumeAll();
            }
        }
    }

    @Override
    public LogReader getLogReader(TargetInstance aTargetInstance) {
        // If we are harvesting then get the log files from the harvester
        return getLogReader(aTargetInstance.getJobName());
    }

    @Override
    public LogReader getLogReader(String aJobName) {
        HarvestAgentStatusDTO status = getHarvestAgentStatusFor(aJobName);
        if (status == null) {
            log.warn("list Log Files Failed. Failed to find the Log Reader for the Job {}.", aJobName);
            return null;
        }

        return harvestAgentFactory.getLogReader(status);
    }

    @Override
    public void pauseAgent(String agentName) {
        HarvestAgentStatusDTO agent = harvestAgents.get(agentName);
        if (agent != null) {
            agent.setAcceptTasks(false);
        }
    }

    @Override
    public void resumeAgent(String agentName) {
        HarvestAgentStatusDTO agent = harvestAgents.get(agentName);
        if (agent != null) {
            agent.setAcceptTasks(true);
        }
    }

    @Override
    public boolean runningOrPaused(TargetInstance aTargetInstance) {
        String state = aTargetInstance.getState();
        if (state.equals(TargetInstance.STATE_RUNNING) || state.equals(TargetInstance.STATE_PAUSED)) {
            return true;
        }

        if (state.equals(TargetInstance.STATE_PATCHING)) {
            List<HarvestResult> hrList = aTargetInstance.getHarvestResults();
            for (HarvestResult hr : hrList) {
                if (hr.getState() == HarvestResult.STATE_CRAWLING) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void restrictBandwidthFor(TargetInstance targetInstance) {
        String jobName = targetInstance.getJobName();
        HarvestAgentStatusDTO ha = getHarvestAgentStatusFor(jobName);
        if (ha != null) {
            HarvestAgent agent = harvestAgentFactory.getHarvestAgent(ha);
            Long allocated = targetInstance.getAllocatedBandwidth();
            if (allocated == null || targetInstance.getAllocatedBandwidth().intValue() <= 0) {
                // zero signifies unlimited bandwidth, prevent this
                targetInstance.setAllocatedBandwidth(new Long(1));
            }
            agent.restrictBandwidth(jobName, targetInstance.getAllocatedBandwidth().intValue());
            targetInstanceDao.save(targetInstance);
        }
    }

    @Override
    public List<HarvestAgentStatusDTO> getHarvestersForAgency(String agencyName) {
        List<HarvestAgentStatusDTO> result = new ArrayList<HarvestAgentStatusDTO>();
        for (HarvestAgentStatusDTO agent : harvestAgents.values()) {
            ArrayList<String> allowedAgencies = agent.getAllowedAgencies();
            if (allowedAgencies == null || allowedAgencies.isEmpty() || allowedAgencies.contains(agencyName)) {
                result.add(agent);
            }
        }
        return result;
    }

    @Override
    public void markDead(HarvestAgentStatusDTO agent) {
        harvestAgents.remove(agent.getName());
    }

    @Override
    public void initiateHarvest(HarvestAgentStatusDTO aHarvestAgent, TargetInstance aTargetInstance, String profile,
                                String seedsString) {
        this.initiateHarvest(aHarvestAgent, aTargetInstance.getJobName(), profile, seedsString);
    }

    @Override
    public void initiateHarvest(HarvestAgentStatusDTO aHarvestAgent, String jobName, String profile,
                                String seedsString) {
        HarvestAgent agent = harvestAgentFactory.getHarvestAgent(aHarvestAgent);

        Map<String, String> params = new HashMap<String, String>();
        params.put("profile", profile);
        params.put("seeds", seedsString);
        agent.initiateHarvest(jobName, params);
    }

    @Override
    public void recoverHarvests(String baseUrl, String haService, List<String> activeJobs) {
        HarvestAgentStatusDTO tempHarvestAgentStatusDTO = new HarvestAgentStatusDTO();
        tempHarvestAgentStatusDTO.setBaseUrl(baseUrl);
        HarvestAgent agent = harvestAgentFactory.getHarvestAgent(tempHarvestAgentStatusDTO);
        agent.recoverHarvests(activeJobs);
    }

    @Override
    public boolean lock(Long tiOid) {
        return targetInstanceLocks.add(tiOid);
    }

    @Override
    public void unLock(Long tiOid) {
        targetInstanceLocks.remove(tiOid);
    }

    @Override
    public HashMap<String, HarvestAgentStatusDTO> getHarvestAgents() {
        return new HashMap<String, HarvestAgentStatusDTO>(harvestAgents);
    }

    public void setTargetInstanceManager(TargetInstanceManager targetInstanceManager) {
        this.targetInstanceManager = targetInstanceManager;
    }

    public void setHarvestAgentFactory(HarvestAgentFactory harvestAgentFactory) {
        this.harvestAgentFactory = harvestAgentFactory;
    }

    public void setTargetInstanceDao(TargetInstanceDAO targetInstanceDao) {
        this.targetInstanceDao = targetInstanceDao;
    }

//    public WctCoordinator getWctCoordinator() {
//        return wctCoordinator;
//    }
//
//    public void setWctCoordinator(WctCoordinator wctCoordinator) {
//        this.wctCoordinator = wctCoordinator;
//    }

    public HarvestResultManager getHarvestResultManager() {
        return harvestResultManager;
    }

    public void setHarvestResultManager(HarvestResultManager harvestResultManager) {
        this.harvestResultManager = harvestResultManager;
    }

    @Override
    public void purgeAbortedTargetInstances(List<String> tiNames) {
        for (HarvestAgentStatusDTO statusDto : harvestAgents.values()) {
            HarvestAgent ha = harvestAgentFactory.getHarvestAgent(statusDto);
            try {
                ha.purgeAbortedTargetInstances(tiNames);
            } catch (Exception e) {
                log.error(MessageFormat.format("Failed to complete the purge of aborted ti data via HA: {0}", e.getMessage()), e);
            }
        }
    }

    @Override
    public List<HarvestAgentStatusDTO> getAvailableHarvesters(String agencyName) {
        List<HarvestAgentStatusDTO> result = new ArrayList<HarvestAgentStatusDTO>();
        List<HarvestAgentStatusDTO> harvestersForAgency = getHarvestersForAgency(agencyName);
        for (HarvestAgentStatusDTO agent : harvestersForAgency) {
            if (harvesterCanHarvestNow(agent)) {
                result.add(agent);
            }
        }
        Collections.sort(result, new Comparator<HarvestAgentStatusDTO>() {
            @Override
            public int compare(HarvestAgentStatusDTO o1, HarvestAgentStatusDTO o2) {
                // Result is negated to get a descending sort order
                return -(o1.getHarvesterStatusCount() - o2.getHarvesterStatusCount());
            }
        });
        return result;
    }

    /**
     * Return the next harvest agent to allocate a target instance to.
     *
     * @param agencyName    the agency to get harvesters for
     * @param harvesterType the desired harvester type
     * @return the harvest agent
     */
    @Override
    public HarvestAgentStatusDTO getHarvester(String agencyName, String harvesterType) {
        HarvestAgentStatusDTO selectedAgent = null;

        List<HarvestAgentStatusDTO> harvestersForAgency = getHarvestersForAgency(agencyName);
        for (HarvestAgentStatusDTO agent : harvestersForAgency) {
            if (selectedAgent == null || agent.getHarvesterStatusCount() < selectedAgent.getHarvesterStatusCount()) {
                if (harvesterCanHarvestNow(agent) && agent.getHarvesterType().equals(harvesterType)) {
                    selectedAgent = agent;
                }
            }
        }
        return selectedAgent;
    }

    @Override
    public void pausePatching(String jobName) {
        HarvestAgentStatusDTO status = getHarvestAgentStatusFor(jobName);
        if (status == null) {
            log.warn("PAUSE Failed. Failed to find the Harvest Agent for the Job {}.", jobName);
            return;
        }

        HarvestAgent agent = harvestAgentFactory.getHarvestAgent(status);
        if (agent == null) {
            log.warn("PAUSE Failed. Failed to find the Harvest Agent for the Job {}.", jobName);
            return;
        }

        agent.pause(jobName);
    }

    @Override
    public void resumePatching(String jobName) {
        HarvestAgentStatusDTO status = getHarvestAgentStatusFor(jobName);
        if (status == null) {
            log.warn("RESUME Failed. Failed to find the Harvest Agent for the Job {}.", jobName);
            return;
        }

        HarvestAgent agent = harvestAgentFactory.getHarvestAgent(status);
        if (agent == null) {
            log.warn("RESUME Failed. Failed to find the Harvest Agent for the Job {}.", jobName);
            return;
        }

        agent.resume(jobName);
    }

    @Override
    public void abortPatching(String jobName) {
        HarvestAgentStatusDTO status = getHarvestAgentStatusFor(jobName);
        if (status == null) {
            log.warn("ABORT Failed. Failed to find the Harvest Agent for the Job {}.", jobName);
            return;
        }

        HarvestAgent agent = harvestAgentFactory.getHarvestAgent(status);
        if (agent == null) {
            log.warn("ABORT Failed. Failed to find the Harvest Agent for the Job {}.", jobName);
            return;
        }

        agent.abort(jobName);
    }

    @Override
    public void stopPatching(String jobName) {
        HarvestAgentStatusDTO status = getHarvestAgentStatusFor(jobName);
        if (status == null) {
            log.warn("STOP Failed. Failed to find the Harvest Agent for the Job {}.", jobName);
            return;
        }

        HarvestAgent agent = harvestAgentFactory.getHarvestAgent(status);
        if (agent == null) {
            log.warn("STOP Failed. Failed to find the Harvest Agent for the Job {}.", jobName);
            return;
        }

        agent.stop(jobName);
    }

    private boolean harvesterCanHarvestNow(HarvestAgentStatusDTO agent) {
        return !agent.getMemoryWarning() && !agent.isInTransition() && agent.getHarvesterStatusCount() < agent.getMaxHarvests();
    }
}
