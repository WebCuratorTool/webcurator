package org.webcurator.core.visualization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.coordinator.WctCoordinatorClient;
import org.webcurator.core.util.ApplicationContextFactory;
import org.webcurator.core.util.PatchUtil;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;
import org.webcurator.core.visualization.networkmap.service.NetworkMapService;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.HarvestResultDTO;

import java.io.IOException;
import java.util.*;

public class VisualizationProcessorManager {
    protected static final Logger log = LoggerFactory.getLogger(VisualizationProcessorManager.class);
    private final Map<String, VisualizationAbstractProcessor> queued_processors = new Hashtable<>();
    private final VisualizationDirectoryManager visualizationDirectoryManager;
    private final WctCoordinatorClient wctCoordinatorClient;

    public VisualizationProcessorManager(VisualizationDirectoryManager visualizationDirectoryManager,
                                         WctCoordinatorClient wctCoordinatorClient,
                                         int maxConcurrencyModThreads) {
        this.visualizationDirectoryManager = visualizationDirectoryManager;
        this.wctCoordinatorClient = wctCoordinatorClient;
    }

    private void initTask(VisualizationAbstractProcessor processor) throws IOException {
        //Execute processor with thread pool
        NetworkMapService networkMapClient = Objects.requireNonNull(ApplicationContextFactory.getApplicationContext()).getBean(NetworkMapClient.class);
        processor.init(visualizationDirectoryManager, wctCoordinatorClient, networkMapClient);
    }

    public boolean executeTask(VisualizationAbstractProcessor processor) throws IOException {
        if (queued_processors.containsKey(processor.getKey())) {
            log.warn("Processor is in the queue: {}", processor.getKey());
            return false;
        }

        this.initTask(processor);

        queued_processors.put(processor.getKey(), processor);
        try {
            return processor.call();
        } catch (Exception ex) {
            log.error("Process failed: {}-{}, {}, {}", processor.getTargetInstanceId(), processor.getHarvestResultNumber(), processor.getProcessorStage(), processor.getStatus());
            return false;
        } finally {
            this.finalise(processor);
            queued_processors.remove(processor.getKey());
            log.info("Process finished: {}-{}, {}, {}", processor.getTargetInstanceId(), processor.getHarvestResultNumber(), processor.getProcessorStage(), processor.getStatus());
        }
    }

    public void finalise(VisualizationAbstractProcessor processor) {
        //Remove existing process firstly to avoid deadlock
        if (processor.getProcessorStage().equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_MODIFYING)) {
            wctCoordinatorClient.notifyModificationComplete(processor.getTargetInstanceId(), processor.getHarvestResultNumber());
            //Move the current metadata to history fold to avoid duplicated execution
            PatchUtil.modifier.moveJob2History(visualizationDirectoryManager.getBaseDir(), processor.getTargetInstanceId(), processor.getHarvestResultNumber());
        } else if (processor.getProcessorStage().equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_INDEXING)) {
            PatchUtil.indexer.moveJob2History(visualizationDirectoryManager.getBaseDir(), processor.getTargetInstanceId(), processor.getHarvestResultNumber());
        }
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
        return queued_processors.get(key);
    }

    /* There is no concurrency processors with the same targetInstanceId and harvestResultNumber
    , but with different processType*/
    public static String getKey(long targetInstanceId, int harvestResultNumber) {
        return String.format("key_%d_%d", targetInstanceId, harvestResultNumber);
    }
}
