package org.webcurator.core.visualization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.coordinator.WctCoordinatorClient;
import org.webcurator.core.util.ApplicationContextFactory;
import org.webcurator.core.util.PatchUtil;
import org.webcurator.core.util.WctUtils;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;
import org.webcurator.core.visualization.networkmap.service.NetworkMapService;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.HarvestResultDTO;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class VisualizationProcessorManager {
    protected static final Logger log = LoggerFactory.getLogger(VisualizationProcessorManager.class);
    private final Map<String, ProcessorHandler> queued_processors = new Hashtable<>();
    private final ExecutorService thread_pool;
    private final VisualizationDirectoryManager visualizationDirectoryManager;
    private final WctCoordinatorClient wctCoordinatorClient;

    public VisualizationProcessorManager(VisualizationDirectoryManager visualizationDirectoryManager,
                                         WctCoordinatorClient wctCoordinatorClient,
                                         int maxConcurrencyModThreads) {
        this.visualizationDirectoryManager = visualizationDirectoryManager;
        this.wctCoordinatorClient = wctCoordinatorClient;
        this.thread_pool = Executors.newFixedThreadPool(maxConcurrencyModThreads);
    }

    public void initTask(VisualizationAbstractProcessor processor) throws IOException {
        //Execute processor with thread pool
        NetworkMapService networkMapClient = Objects.requireNonNull(ApplicationContextFactory.getApplicationContext()).getBean(NetworkMapClient.class);
        processor.init(this, visualizationDirectoryManager, wctCoordinatorClient, networkMapClient);
    }

    public void startTask(VisualizationAbstractProcessor processor) throws IOException {
        if (queued_processors.containsKey(processor.getKey())) {
            log.debug("Processor is in the queue: {}", processor.getKey());
            return;
        }

        this.initTask(processor);

        Future<Boolean> futureResult = thread_pool.submit(processor);

        //Cache the current running
        ProcessorHandler handler = new ProcessorHandler(processor, futureResult);
        queued_processors.put(processor.getKey(), handler);
    }

    public void finalise(VisualizationAbstractProcessor processor) {
        log.info("Process finished: {}-{}, {}, {}", processor.getTargetInstanceId(), processor.getHarvestResultNumber(), processor.getProcessorStage(), processor.getStatus());
        //Remove existing process firstly to avoid deadlock
        queued_processors.remove(processor.getKey());

        if (processor.getProcessorStage().equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_MODIFYING)) {
            if (processor.getStatus() == HarvestResult.STATUS_FINISHED) {
                wctCoordinatorClient.notifyModificationComplete(processor.getTargetInstanceId(), processor.getHarvestResultNumber());
            }
            //Move the current metadata to history fold to avoid duplicated execution
            PatchUtil.modifier.moveJob2History(visualizationDirectoryManager.getBaseDir(), processor.getTargetInstanceId(), processor.getHarvestResultNumber());
        } else if (processor.getProcessorStage().equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_INDEXING)) {
            /*
             * Put the indexing finalise process in Store Service to cover both visualization and indexer
             * */
            //            if (processor.getStatus() == HarvestResult.STATUS_FINISHED) {
            //                wctCoordinatorClient.finaliseIndex(processor.getTargetInstanceId(), processor.getHarvestResultNumber());
            //            }
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

        VisualizationAbstractProcessor processor = getProcessor(key);
        if (processor != null) {
            processor.terminateTask();
            while (!processor.isFinished()) {
                log.debug("Waiting for to be finished, targetInstanceId={}, harvestResultNumber={}", targetInstanceId, harvestResultNumber);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    log.error("Failed sleep", e);
                    return false;
                }
            }
        }

        ProcessorHandler handler = queued_processors.get(key);
        if (handler != null) {
            handler.future.cancel(true);
        }

        queued_processors.remove(key);

        return true;
    }

    public boolean deleteTask(String processorType, long targetInstanceId, int harvestResultNumber) {
        this.terminateTask(processorType, targetInstanceId, harvestResultNumber);

        //Delete basic files: warc files and indexes
        BDBNetworkMapPool pool = Objects.requireNonNull(ApplicationContextFactory.getApplicationContext()).getBean(BDBNetworkMapPool.class);
        pool.close(targetInstanceId, harvestResultNumber); //Close the BDB instance when it is available

        File dirOfHarvestFiles = new File(visualizationDirectoryManager.getBaseDir(), String.format("%d%s%d", targetInstanceId, File.separator, harvestResultNumber));
        WctUtils.cleanDirectory(dirOfHarvestFiles);

        //Delete logs and reports
        WctUtils.deleteFile(visualizationDirectoryManager.getBaseLogDir(targetInstanceId), visualizationDirectoryManager.getPatchLogFileName(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, harvestResultNumber));
        WctUtils.deleteFile(visualizationDirectoryManager.getBaseReportDir(targetInstanceId), visualizationDirectoryManager.getPatchReportFileName(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, harvestResultNumber));
        WctUtils.deleteFile(visualizationDirectoryManager.getBaseLogDir(targetInstanceId), visualizationDirectoryManager.getPatchLogFileName(HarvestResult.PATCH_STAGE_TYPE_INDEXING, harvestResultNumber));
        WctUtils.deleteFile(visualizationDirectoryManager.getBaseReportDir(targetInstanceId), visualizationDirectoryManager.getPatchReportFileName(HarvestResult.PATCH_STAGE_TYPE_INDEXING, harvestResultNumber));

        //Delete command json files
        PatchUtil.modifier.deleteJob(visualizationDirectoryManager.getBaseDir(), targetInstanceId, harvestResultNumber);
        PatchUtil.modifier.deleteHistoryJob(visualizationDirectoryManager.getBaseDir(), targetInstanceId, harvestResultNumber);
        PatchUtil.indexer.deleteJob(visualizationDirectoryManager.getBaseDir(), targetInstanceId, harvestResultNumber);
        PatchUtil.indexer.deleteHistoryJob(visualizationDirectoryManager.getBaseDir(), targetInstanceId, harvestResultNumber);
        return true;
    }

    public VisualizationProgressBar getProgress(long targetInstanceId, int harvestResultNumber) {
        String key = getKey(targetInstanceId, harvestResultNumber);
        VisualizationAbstractProcessor processor = getProcessor(key);
        if (processor != null) {
            return processor.getProgress();
        }
        return null;
    }

    public HarvestResultDTO getHarvestResultDTO(long targetInstanceId, int harvestResultNumber) {
        String key = getKey(targetInstanceId, harvestResultNumber);
        VisualizationAbstractProcessor processor = getProcessor(key);
        if (processor != null) {
            return processor.getHarvestResultDTO();
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
