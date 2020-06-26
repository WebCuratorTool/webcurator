package org.webcurator.core.visualization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class VisualizationProcessorQueue {
    protected static final Logger log = LoggerFactory.getLogger(VisualizationProcessorQueue.class);

    private Semaphore max_processors_lock = new Semaphore(3);
    // private final List<String> queued_keys = new LinkedList<>();
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
                    processor.process();
                } catch (Throwable e) {
                    log.error(e.getMessage());
                } finally {
                    max_processors_lock.release();
                    // queued_keys.remove(0);
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

    public void setMaxProcessors(int max) {
        max_processors_lock = new Semaphore(max);
    }

    public static String getKey(String processorType, long targetInstanceId, int harvestResultNumber) {
        return String.format("%s_%d_%d", processorType, targetInstanceId, harvestResultNumber);
    }
}
