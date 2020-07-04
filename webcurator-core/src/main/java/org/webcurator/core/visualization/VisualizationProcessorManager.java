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
import java.util.concurrent.*;

public class VisualizationProcessorManager {
    protected static final Logger log = LoggerFactory.getLogger(VisualizationProcessorManager.class);
    private final Map<String, ProcessorHandler> queued_processors = new Hashtable<>();
    private final ExecutorService thread_pool;
    private final Timer timerHeartbeat = new Timer();
    private final Timer timerScanJob = new Timer();
    private final VisualizationDirectoryManager visualizationDirectoryManager;
    private final WctCoordinatorClient wctCoordinatorClient;
    private final BDBNetworkMapPool db_pool;
    private final long max_running_duration = 24 * 3600 * 1000; //default: 24Hours
//    private Semaphore max_processors_lock = new Semaphore(3);


    public VisualizationProcessorManager(VisualizationDirectoryManager visualizationDirectoryManager,
                                         WctCoordinatorClient wctCoordinatorClient,
                                         BDBNetworkMapPool db_pool,
                                         int maxConcurrencyModThreads,
                                         long heartbeatInterval,
                                         long jobScanInterval) {
        this.visualizationDirectoryManager = visualizationDirectoryManager;
        this.wctCoordinatorClient = wctCoordinatorClient;
        this.db_pool = db_pool;
        this.thread_pool = Executors.newFixedThreadPool(maxConcurrencyModThreads);
//        this.max_processors_lock = new Semaphore(maxConcurrencyModThreads);
//        this.timerHeartbeat.scheduleAtFixedRate(new HeartBeat(), 0, heartbeatInterval);
//        this.timerScanJob.scheduleAtFixedRate(new JobScan(), 0, heartbeatInterval);
    }

    public void startTask(VisualizationAbstractProcessor processor) {
        if (queued_processors.containsKey(processor.getKey())) {
            log.debug("Processor is in the queue: {}", processor.getKey());
            return;
        }

        //Execute processor with thread pool
        processor.init(this, visualizationDirectoryManager, wctCoordinatorClient);
        Future<Boolean> futureResult = thread_pool.submit(processor);

        //Cache the current running
        ProcessorHandler handler = new ProcessorHandler(processor, futureResult);
        queued_processors.put(processor.getKey(), handler);
    }

    public void finalise(VisualizationAbstractProcessor processor) {
        //Remove existing process firstly to avoid deadlock
        queued_processors.remove(processor.getKey());

        if (processor.getProcessorStage().equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_MODIFYING)) {
            wctCoordinatorClient.notifyModificationComplete(processor.getTargetInstanceId(), processor.getHarvestResultNumber());
            //Move the current metadata to history fold to avoid duplicated execution
            PatchUtil.modifier.moveJob2History(visualizationDirectoryManager.getBaseDir(), processor.getTargetInstanceId(), processor.getHarvestResultNumber());
        } else if (processor.getProcessorStage().equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_INDEXING)) {
            wctCoordinatorClient.finaliseIndex(processor.getTargetInstanceId(), processor.getHarvestResultNumber());
            //Move the current metadata to history fold to avoid duplicated execution
            PatchUtil.indexer.moveJob2History(visualizationDirectoryManager.getBaseDir(), processor.getTargetInstanceId(), processor.getHarvestResultNumber());
        }
    }

    public boolean pauseTask(String processorType, long targetInstanceId, int harvestResultNumber) {
        String key = getKey(targetInstanceId, harvestResultNumber);
        VisualizationAbstractProcessor processor = getProcessor(key);
        if (processor != null) {
            processor.pauseTask();
            return true;
        }
        return false;
    }

    public boolean resumeTask(String processorType, long targetInstanceId, int harvestResultNumber) {
        String key = getKey(targetInstanceId, harvestResultNumber);
        VisualizationAbstractProcessor processor = getProcessor(key);
        if (processor != null) {
            processor.resumeTask();
            return true;
        }
        return false;
    }

    public boolean terminateTask(String processorType, long targetInstanceId, int harvestResultNumber) {
        String key = getKey(targetInstanceId, harvestResultNumber);
        ProcessorHandler handler = queued_processors.get(key);
        if (handler != null) {
            return handler.future.cancel(true);
        }
//
//        VisualizationAbstractProcessor processor = getProcessor(key);
//        if (processor != null) {
//            processor.terminateTask();
//            return true;
//        }
        return false;
    }

    public boolean deleteTask(String processorType, long targetInstanceId, int harvestResultNumber) {
        String key = getKey(targetInstanceId, harvestResultNumber);
        VisualizationAbstractProcessor processor = getProcessor(key);
        if (processor != null) {
            processor.deleteTask();
            return true;
        }
        return false;
    }

    public VisualizationProgressBar getProgress(long targetInstanceId, int harvestResultNumber) {
        String key = getKey(targetInstanceId, harvestResultNumber);
        VisualizationAbstractProcessor processor = getProcessor(key);
        if (processor != null) {
            return processor.getProgress();
        }
        return null;
    }

    public VisualizationAbstractProcessor getProcessor(String key) {
        ProcessorHandler handler = queued_processors.get(key);
        if (handler == null) {
            return null;
        } else {
            return handler.processor;
        }
    }

    /* There is no concurrency processors with the same targetInstanceId and harvestResultNumber
    , but with different processType*/
    public static String getKey(long targetInstanceId, int harvestResultNumber) {
        return String.format("key_%d_%d", targetInstanceId, harvestResultNumber);
    }


    /*Heartbeat report task, to send current status of all processors to WCT*/
    class HeartBeat extends TimerTask {
        @Override
        public void run() {
            try {
                List<HarvestResultDTO> list = new ArrayList<>();
                queued_processors.values().forEach(handler -> {
                    int status = HarvestResult.STATUS_UNASSESSED;

                    long runningDuration = handler.getRunningDuration();
                    if (runningDuration > max_running_duration && !handler.future.isDone() && !handler.future.isCancelled()) {
                        handler.future.cancel(true);
                        status = HarvestResult.STATUS_TERMINATED;
                    } else if (handler.future.isDone()) {
                        status = HarvestResult.STATUS_FINISHED;
                    } else if (!handler.future.isCancelled()) {
                        status = HarvestResult.STATUS_TERMINATED;
                    }

                    if (status != HarvestResult.STATUS_UNASSESSED) {
                        HarvestResultDTO harvestResultDTO = new HarvestResultDTO();
                        harvestResultDTO.setState(handler.processor.getState());
                        harvestResultDTO.setStatus(status);
                        list.add(harvestResultDTO);
                    }
                });

                list.forEach(dto -> {
                    String key = getKey(dto.getTargetInstanceOid(), dto.getHarvestNumber());
                    queued_processors.remove(key);
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
                List<VisualizationAbstractCommandApply> modifyingJobs = PatchUtil.modifier.listPatchJob(visualizationDirectoryManager.getBaseDir());
                modifyingJobs.forEach(cmd -> {
                    String key = getKey(cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
                    try {
                        VisualizationAbstractProcessor processor = new PruneAndImportProcessor((PruneAndImportCommandApply) cmd);
                        startTask(processor);
                    } catch (DigitalAssetStoreException e) {
                        log.error(e.getMessage());
                    }
                });

                List<VisualizationAbstractCommandApply> indexingJobs = PatchUtil.indexer.listPatchJob(visualizationDirectoryManager.getBaseDir());
                indexingJobs.forEach(cmd -> {
                    String key = getKey(cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
                    try {
                        VisualizationAbstractProcessor processor = new ResourceExtractorProcessor(db_pool, cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
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

    static class ProcessorHandler {
        public long startTime = System.currentTimeMillis();
        public VisualizationAbstractProcessor processor;
        public Future<Boolean> future;

        public ProcessorHandler(VisualizationAbstractProcessor processor, Future<Boolean> future) {
            this.processor = processor;
            this.future = future;
        }

        public long getRunningDuration() {
            return System.currentTimeMillis() - startTime;
        }
    }
}
