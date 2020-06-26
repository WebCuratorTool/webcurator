package org.webcurator.core.visualization;

import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.domain.model.core.HarvestResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class VisualizationProgressBar {
    private final static int MAX_QUEUE_SIZE = 1024;
    private final static List<String> QUEUE_LIST = new ArrayList<>();
    private final static Map<String, VisualizationProgressBar> QUEUE_MAP = new HashMap<>();

    private Map<String, ProgressItem> items = new HashMap<>();
    private String stage;
    private long targetInstanceId;
    private int harvestResultNumber;

    private VisualizationProgressBar(String stage, long targetInstanceId, int harvestResultNumber) {
        this.stage = stage;
        this.targetInstanceId = targetInstanceId;
        this.harvestResultNumber = harvestResultNumber;
    }

    public synchronized static VisualizationProgressBar getInstance(String stage, long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException {
        String key = getKey(stage, targetInstanceId, harvestResultNumber);
        if (QUEUE_MAP.containsKey(key)) {
            return QUEUE_MAP.get(key);
        }

        //Clear history data
        while (QUEUE_LIST.size() > MAX_QUEUE_SIZE) {
            String tmpKey = QUEUE_LIST.get(0);
            QUEUE_LIST.remove(0);
            VisualizationProgressBar tmpProgressBar = QUEUE_MAP.remove(tmpKey);
            tmpProgressBar.clear();
        }

        if (!HarvestResult.PATCH_STAGE_TYPE_MODIFYING.equals(stage) && !HarvestResult.PATCH_STAGE_TYPE_INDEXING.equals(stage)) {
            throw new DigitalAssetStoreException("Unsupported stage: " + stage);
        }

        VisualizationProgressBar progressBar = new VisualizationProgressBar(stage, targetInstanceId, harvestResultNumber);
        QUEUE_MAP.put(key, progressBar);
        QUEUE_LIST.add(key);

        return progressBar;
    }

    public synchronized static void removeInstance(String stage, long targetInstanceId, int harvestResultNumber) {
        String key = getKey(stage, targetInstanceId, harvestResultNumber);
        QUEUE_LIST.remove(key);
        VisualizationProgressBar progressBar = QUEUE_MAP.remove(key);
        progressBar.clear();
    }

    public static VisualizationProgressBar getProgress(String stage, long targetInstanceId, int harvestResultNumber) {
        String key = getKey(stage, targetInstanceId, harvestResultNumber);
        if (QUEUE_MAP.containsKey(key)) {
            return QUEUE_MAP.get(key);
        }
        return null;
    }

    public static VisualizationProgressBar getProgress(long targetInstanceId, int harvestResultNumber) {
        String keyIndex = getKey(HarvestResult.PATCH_STAGE_TYPE_INDEXING, targetInstanceId, harvestResultNumber);
        if (QUEUE_MAP.containsKey(keyIndex)) {
            return QUEUE_MAP.get(keyIndex);
        }

        String keyModify = getKey(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, targetInstanceId, harvestResultNumber);
        if (QUEUE_MAP.containsKey(keyModify)) {
            return QUEUE_MAP.get(keyModify);
        }

        return null;
    }

    public static String getKey(String stage, long targetInstanceId, int harvestResultNumber) {
        return String.format("KEY_%s_%d_%d", stage, targetInstanceId, harvestResultNumber);
    }

    public String getKey() {
        return getKey(stage, targetInstanceId, harvestResultNumber);
    }

    public int getProgressPercentage() {
        AtomicLong totMaxLength = new AtomicLong(0);
        AtomicLong totCurLength = new AtomicLong(0);
        items.values().forEach(item -> {
            totMaxLength.addAndGet(item.getMaxLength());
            totCurLength.addAndGet(item.getCurLength());
        });

        if (totMaxLength.get() > 0) {
            return (int) (100 * totCurLength.get() / totMaxLength.get());
        }

        return -1;
    }

    public ProgressItem getProgressItem(String name) {
        ProgressItem item = items.get(name);
        if (item == null) {
            item = new ProgressItem(name);
            items.put(name, item);
        }
        return item;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder("Name\tMaxLength\tCurLength\tPercentage");
        items.values().forEach(item -> {
            buf.append('\n').append(item.toString());
        });
        return buf.toString();
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public Map<String, ProgressItem> getItems() {
        return items;
    }

    public void setItems(Map<String, ProgressItem> items) {
        this.items = items;
    }

    public long getTargetInstanceId() {
        return targetInstanceId;
    }

    public void setTargetInstanceId(long targetInstanceId) {
        this.targetInstanceId = targetInstanceId;
    }

    public int getHarvestResultNumber() {
        return harvestResultNumber;
    }

    public void setHarvestResultNumber(int harvestResultNumber) {
        this.harvestResultNumber = harvestResultNumber;
    }

    public void clear() {
        items.clear();
    }

    public static class ProgressItem {
        private String name;
        private long maxLength;
        private long curLength;

        public ProgressItem(String name) {
            this.name = name;
        }

        public int getProgressPercentage() {
            if (maxLength > 0) {
                return (int) (100 * curLength / maxLength);
            }
            return -1;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(long maxLength) {
            this.maxLength = maxLength;
        }

        public long getCurLength() {
            return curLength;
        }

        public void setCurLength(long curLength) {
            this.curLength = curLength;
        }

        public String toString() {
            return String.format("%s\t%d\t%d\t%d", name, maxLength, curLength, getProgressPercentage()) + "%";
        }
    }
}
