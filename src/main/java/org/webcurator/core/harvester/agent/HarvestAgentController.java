package org.webcurator.core.harvester.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class HarvestAgentController implements HarvestAgent {
    Logger log= LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("harvestAgent")
    private HarvestAgent harvestAgent;

    @Override
    @GetMapping(path = HarvestAgentPaths.NAME)
    public String getName() {
        return harvestAgent.getName();
    }

    @Override
    @RequestMapping(path = HarvestAgentPaths.INITIATE_HARVEST, method = {RequestMethod.POST, RequestMethod.GET})
    public void initiateHarvest(@PathVariable(value = "job") String job,
                                @RequestBody Map<String, String> params) {
        log.debug("Initial harvest, job: {}", job);
        harvestAgent.initiateHarvest(job, params);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.RECOVER_HARVESTS)
    public void recoverHarvests(@RequestBody List<String> activeJobs) {
        log.debug("Recover harvest, jobs: {}", Arrays.toString(activeJobs.toArray()));
        harvestAgent.recoverHarvests(activeJobs);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.UPDATE_PROFILE_OVERRIDES)
    public void updateProfileOverrides(@PathVariable(value = "job") String job,
                                       @RequestParam(value = "profile") String profile) {
        log.debug("Update profile overrides, job: {}, profile: {}" ,job, profile);
        harvestAgent.updateProfileOverrides(job, profile);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.PURGE_ABORTED_TARGET_INSTANCES)
    public void purgeAbortedTargetInstances(@RequestBody List<String> targetInstanceNames) {
        log.debug("Purge aborted target instance, targetInstanceNames: {}", Arrays.toString(targetInstanceNames.toArray()));
        harvestAgent.purgeAbortedTargetInstances(targetInstanceNames);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.RESTRICT_BANDWIDTH)
    public void restrictBandwidth(@PathVariable(value = "job") String job,
                                  @RequestParam(value = "bandwidth-limit") int bandwidthLimit) {
        log.debug("Restrict bandwidth, job: {}, bandwidth-limit: {}", job, bandwidthLimit);
        harvestAgent.restrictBandwidth(job, bandwidthLimit);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.PAUSE)
    public void pause(@PathVariable(value = "job") String job) {
        log.debug("Pause job: {}", job);
        harvestAgent.pause(job);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.RESUME)
    public void resume(@PathVariable(value = "job") String job) {
        log.debug("Resume job: {}", job);
        harvestAgent.resume(job);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.ABORT)
    public void abort(@PathVariable(value = "job") String job) {
        log.debug("Abort job: {}", job);
        harvestAgent.abort(job);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.STOP)
    public void stop(@PathVariable(value = "job") String job) {
        log.debug("Stop job: {}", job);
        harvestAgent.stop(job);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.COMPLETE_HARVEST)
    public int completeHarvest(@PathVariable(value = "job") String job,
                               @RequestParam(value = "failure-step") int failureStep) {
        log.debug("Complete harvest, job: {}, failure-step: {}", job, failureStep);

        // TODO Note that the SOAP version did a harvestAgent.stop(job) and then returned HarvestAgent.NO_FAILURES
        return harvestAgent.completeHarvest(job, failureStep);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.LOAD_SETTINGS)
    public void loadSettings(@PathVariable(value = "job") String job) {
        log.debug("Load settings, job: {}", job);

        harvestAgent.loadSettings(job);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.PAUSE_ALL)
    public void pauseAll() {
        log.debug("Pause all jobs");
        harvestAgent.pauseAll();
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.RESUME_ALL)
    public void resumeAll() {
        log.debug("Resume all jobs");
        harvestAgent.resumeAll();
    }

    @Override
    @GetMapping(path = HarvestAgentPaths.MEMORY_WARNING)
    public boolean getMemoryWarning() {
        log.debug("Get memory warnings");
        return harvestAgent.getMemoryWarning();
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.MEMORY_WARNING)
    public void setMemoryWarning(@RequestParam(value = "memory-warning") boolean memoryWarning) {
        log.debug("Set memory warnings");
        harvestAgent.setMemoryWarning(memoryWarning);
    }

    @Override
    @GetMapping(path = HarvestAgentPaths.STATUS)
    public HarvestAgentStatusDTO getStatus() {
        log.debug("Get HarvestAgent status");
        return harvestAgent.getStatus();
    }

    @Override
    @GetMapping(path = HarvestAgentPaths.IS_VALID_PROFILE)
    public boolean isValidProfile(@RequestBody String profile) {
        log.debug("Is valid profile");
        return harvestAgent.isValidProfile(profile);
    }

    @Override
    @PostMapping(path = HarvestAgentPaths.EXECUTE_SHELL_SCRIPT)
    public HarvestAgentScriptResult executeShellScript(@PathVariable(value = "job-name") String jobName,
                                                       @RequestParam(value = "engine") String engine,
                                                       @RequestParam(value = "shell-script") String shellScript) {
        log.debug("HarvestAgent script result, job-name: {}, engine: {}, shell-script: {}", jobName, engine, shellScript);
        throw new RuntimeException("The execution of shell scripts is not supported by Heritrix 1");
    }
}
