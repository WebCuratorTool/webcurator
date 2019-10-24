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
package org.webcurator.core.harvester.agent.schedule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.netarchivesuite.heritrix3wrapper.Heritrix3Wrapper;
import org.quartz.*;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.web.client.HttpClientErrorException;
import org.webcurator.core.harvester.agent.HarvestAgent;
import org.webcurator.core.harvester.coordinator.HarvestCoordinatorNotifier;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;
import org.webcurator.domain.model.core.harvester.agent.HarvesterStatusDTO;
import java.util.Map;

/**
 * The HarvestAgentHeartBeatJob is a scheduled job that triggers the sending of 
 * the Harvest Agents status information to the Web Curator Tools Core. 
 * @author nwaight
 */
public class HarvestAgentHeartBeatJob extends QuartzJobBean {
    public static final String HEART_BEAT_TRIGGER_GROUP = "HeartBeatTriggerGroup";
    private static Log log = LogFactory.getLog(HarvestAgentHeartBeatJob.class);

    /** The harvest agent to use to get status information. */
    HarvestAgent harvestAgent;
    /** The notifier to use to send data to the WCT. */
    HarvestCoordinatorNotifier notifier;

    /** Default Constructor. */
    public HarvestAgentHeartBeatJob() {
        super();
    }

    @Override
    protected void executeInternal(JobExecutionContext aJobContext) throws JobExecutionException {
        Trigger.TriggerState triggerState = Trigger.TriggerState.NONE;
        TriggerKey triggerKey = TriggerKey.triggerKey("HeartBeatTrigger", HEART_BEAT_TRIGGER_GROUP);

        try {
            triggerState = aJobContext.getScheduler().getTriggerState(triggerKey);
            aJobContext.getScheduler().pauseTrigger(triggerKey);
            log.debug("Executing heartbeat - Trigger state: " + triggerState);

            HarvestAgentStatusDTO status = harvestAgent.getStatus();
            notifier.heartbeat(status);

            notifier.requestRecovery(status.getHost(), status.getPort(), status.getService());

            /* H3 polling begin*/

            Map<String, HarvesterStatusDTO> harvestStatus = status.getHarvesterStatus();

            for(Map.Entry<String, HarvesterStatusDTO> entry : harvestStatus.entrySet()) {
                HarvesterStatusDTO job = entry.getValue();
                String jobStatus = job.getStatus();

                if(jobStatus != null) {
                    // When the agent moves into the running state grab the settings we need at
                    // Job completion as some of these may no longer be available when the job is finished.
                    if (jobStatus.equals(Heritrix3Wrapper.CrawlControllerState.RUNNING.toString())) {
                        log.info("HeartBeatJob-H3Poll - job RUNNING: " + job.getJobName() + ". status: " + jobStatus);
                    }

                    // Schedule the job completion process to be run.
                    else if (jobStatus.equals("Finished") || jobStatus.equals("Could not launch job - Fatal InitializationException")) {
                        if(log.isDebugEnabled()){
                            log.debug("HeartBeatJob:H3Poll - job FINISHED: " + job.getJobName() + ". status: " + jobStatus);
                        }
                        SchedulerUtil.scheduleHarvestCompleteJob(job.getJobName());
                        log.info("HeartBeatJob-H3Poll - Scheduling Harvest Complete Job for: " + job.getJobName());
                    } else {
                        log.info("HeartBeatJob-H3Poll - job: " + job.getJobName() + ". status: " + jobStatus);
                    }
                }
            }

            /* H3 polling end*/

            aJobContext.getScheduler().resumeTrigger(triggerKey);
            log.debug("Executing heartbeat - Trigger state: " + triggerState);
        }
        catch (ObjectAlreadyExistsException ex){
            log.error("Failed to start harvest complete job: " + ex.getMessage());
            // Resume trigger group, other thread will suspend forever
            try {
                aJobContext.getScheduler().resumeTrigger(triggerKey);
            } catch (SchedulerException e) {
                e.printStackTrace();
                log.error("Failed to resume Trigger Group - HeartBeatTriggerGroup: " + e.getMessage());
                throw new JobExecutionException("Failed to resume Trigger Group - HeartBeatTriggerGroup: " + e.getMessage());
            }
        }
        catch (SchedulerException ex) {
            log.error(ex);
            throw new JobExecutionException("Heartbeat failed controlling the scheduler. (triggerState is: " + triggerState + ")");
        }
        catch (HttpClientErrorException ex){
            log.error("HarvestAgentHeartBeatJob Error: " + ex.getMessage(), ex);
            // Resume trigger group, otherwise thread will suspend forever
            try {
                aJobContext.getScheduler().resumeTrigger(triggerKey);
            } catch (SchedulerException e) {
                log.error("Failed to resume Trigger - HeartBeatTrigger: " + e.getMessage());
                throw new JobExecutionException("Failed to resume Trigger - HeartBeatTrigger: " + e.getMessage());
            }
        }
        catch (Exception ex){
            log.error("Heartbeat job failed", ex);
            throw new JobExecutionException(ex);
        }
    }

    /**
     * @param harvestAgent The harvestAgent to set.
     */
    public void setHarvestAgent(HarvestAgent harvestAgent) {
        this.harvestAgent = harvestAgent;
    }

    /**
     * @param notifier The notifier to set.
     */
    public void setNotifier(HarvestCoordinatorNotifier notifier) {
        this.notifier = notifier;
    }

}
