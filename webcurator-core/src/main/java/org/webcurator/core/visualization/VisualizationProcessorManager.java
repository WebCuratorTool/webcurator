package org.webcurator.core.visualization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.coordinator.WctCoordinatorClient;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.util.PatchUtil;
import org.webcurator.core.visualization.modification.PruneAndImportProcessor;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.core.visualization.networkmap.ResourceExtractorProcessor;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.HarvestResultDTO;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;

public class VisualizationProcessorManager {
    protected static final Logger log = LoggerFactory.getLogger(VisualizationProcessorManager.class);

    private final Timer timerHeartbeat = new Timer();
    private final Timer timerScanJob = new Timer();
    private final VisualizationManager visualizationManager;
    private final WctCoordinatorClient wctCoordinatorClient;
    private final BDBNetworkMapPool pool;
    private Semaphore max_processors_lock = new Semaphore(3);
    private final Map<String, VisualizationAbstractProcessor> queued_processors = new Hashtable<>();

    public VisualizationProcessorManager(VisualizationManager visualizationManager,
                                         WctCoordinatorClient wctCoordinatorClient,
                                         BDBNetworkMapPool pool,
                                         int maxConcurrencyModThreads,
                                         long heartbeatInterval,
                                         long jobScanInterval) {
        this.visualizationManager = visualizationManager;
        this.wctCoordinatorClient = wctCoordinatorClient;
        this.pool = pool;
        this.max_processors_lock = new Semaphore(maxConcurrencyModThreads);
        this.timerHeartbeat.scheduleAtFixedRate(new HeartBeat(), 0, heartbeatInterval);
        this.timerScanJob.scheduleAtFixedRate(new JobScan(), 0, heartbeatInterval);
    }

    synchronized public void startTask(VisualizationAbstractProcessor processor) {
        final String key = processor.getKey();
        if (queued_processors.containsKey(key)) {
            log.error("Processor is in the queue: {}", key);
            return;
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String key = processor.getKey();
                try {
                    // queued_keys.add(key);
                    queued_processors.put(key, processor);
                    max_processors_lock.acquire();
                    processor.init(visualizationManager, wctCoordinatorClient);
                    processor.process();
                } catch (Throwable e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                } finally {
                    max_processors_lock.release();
                    queued_processors.remove(key);
                }
            }
        });
        t.start();
    }


    public boolean pauseTask(String processorType, long targetInstanceId, int harvestResultNumber) {
        String key = getKey(processorType, targetInstanceId, harvestResultNumber);
        VisualizationAbstractProcessor processor = queued_processors.get(key);
        if (processor != null) {
            processor.pauseTask();
            return true;
        }
        return false;
    }

    public boolean resumeTask(String processorType, long targetInstanceId, int harvestResultNumber) {
        String key = getKey(processorType, targetInstanceId, harvestResultNumber);
        VisualizationAbstractProcessor processor = queued_processors.get(key);
        if (processor != null) {
            processor.resumeTask();
            return true;
        }
        return false;
    }

    public boolean terminateTask(String processorType, long targetInstanceId, int harvestResultNumber) {
        String key = getKey(processorType, targetInstanceId, harvestResultNumber);
        VisualizationAbstractProcessor processor = queued_processors.get(key);
        if (processor != null) {
            processor.terminateTask();
            return true;
        }
        return false;
    }

    public boolean deleteTask(String processorType, long targetInstanceId, int harvestResultNumber) {
        String key = getKey(processorType, targetInstanceId, harvestResultNumber);
        VisualizationAbstractProcessor processor = queued_processors.get(key);
        if (processor != null) {
            processor.deleteTask();
            return true;
        }
        return false;
    }

    public VisualizationProgressBar getProgress(long targetInstanceId, int harvestResultNumber) {
        String key = getKey("default", targetInstanceId, harvestResultNumber);
        VisualizationAbstractProcessor processor = queued_processors.get(key);
        if (processor != null) {
            return processor.getProgress();
        }
        return null;
    }

    /* There is no concurrency processors with the same targetInstanceId and harvestResultNumber
    , but with different processType*/
    public static String getKey(String processorType, long targetInstanceId, int harvestResultNumber) {
        return String.format("key_%d_%d", targetInstanceId, harvestResultNumber);
    }

    /*Heartbeat report task, to send current status of all processors to WCT*/
    class HeartBeat extends TimerTask {
        @Override
        public void run() {
            try {
                List<HarvestResultDTO> list = new ArrayList<>();
                queued_processors.forEach((k, v) -> {
                    int state = HarvestResult.STATE_UNASSESSED;
                    if (v.getProcessorStage().equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_MODIFYING)) {
                        state = HarvestResult.STATE_MODIFYING;
                    } else if (v.getProcessorStage().equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_INDEXING)) {
                        state = HarvestResult.STATE_INDEXING;
                    }

                    int status = v.getStatus();

                    HarvestResultDTO harvestResultDTO = new HarvestResultDTO();
                    harvestResultDTO.setState(state);
                    harvestResultDTO.setStatus(status);

                    list.add(harvestResultDTO);
                });

                wctCoordinatorClient.dasHeartBeat(list);
                list.clear();
            } catch (Throwable e) {
                log.error("Heartbeat failed: " + e.getMessage());
            }
        }
    }

    /*Scan unfinished jobs, and restart them if it's not running*/
    class JobScan extends TimerTask {
        @Override
        public void run() {
            try {
                List<VisualizationAbstractCommandApply> modifyingJobs = PatchUtil.modifier.listPatchJob(visualizationManager.getBaseDir());
                modifyingJobs.forEach(cmd -> {
                    String key = getKey(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
                    try {
                        VisualizationAbstractProcessor processor = new PruneAndImportProcessor((PruneAndImportCommandApply) cmd);
                        startTask(processor);
                    } catch (DigitalAssetStoreException e) {
                        log.error(e.getMessage());
                    }
                });

                List<VisualizationAbstractCommandApply> indexingJobs = PatchUtil.indexer.listPatchJob(visualizationManager.getBaseDir());
                indexingJobs.forEach(cmd -> {
                    String key = getKey(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
                    try {
                        VisualizationAbstractProcessor processor = new ResourceExtractorProcessor(pool, cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
                        startTask(processor);
                    } catch (DigitalAssetStoreException e) {
                        log.error(e.getMessage());
                    }
                });
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }
}
