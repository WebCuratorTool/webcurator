package org.webcurator.core.visualization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.coordinator.WctCoordinatorClient;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.HarvestResultDTO;

import java.util.*;
import java.util.concurrent.Semaphore;

public class VisualizationProcessorQueue extends TimerTask {
    protected static final Logger log = LoggerFactory.getLogger(VisualizationProcessorQueue.class);
    private Timer timer = null;
    private VisualizationManager visualizationManager;
    private WctCoordinatorClient wctCoordinatorClient;

    private Semaphore max_processors_lock = new Semaphore(3);
    private final Map<String, VisualizationAbstractProcessor> queued_processors = new HashMap<>();

    public void startTask(VisualizationAbstractProcessor processor) {
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

    public void setVisualizationManager(VisualizationManager visualizationManager) {
        this.visualizationManager = visualizationManager;
    }

    public void setWctCoordinatorClient(WctCoordinatorClient wctCoordinatorClient) {
        this.wctCoordinatorClient = wctCoordinatorClient;
    }

    public void setMaxConcurrencyModThreads(int maxConcurrencyModThreads) {
        this.max_processors_lock = new Semaphore(maxConcurrencyModThreads);
    }

    public void setHeartbeatInterval(long heartbeatInterval) {
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(this, 0, heartbeatInterval);
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
