package org.webcurator.core.visualization;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class VisualizationProgressBar {
    private final Map<String, ProgressItem> items = new HashMap<>();
    private String stage;

    public VisualizationProgressBar(String stage) {
        this.stage = stage;
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
