package org.webcurator.visualization.app;

import java.util.ArrayList;
import java.util.List;

public class VisTargetInstance {
    private long tiId;
    private List<VisHarvestResult> hrList = new ArrayList<>();

    public long getTiId() {
        return tiId;
    }

    public void setTiId(long tiId) {
        this.tiId = tiId;
    }

    public List<VisHarvestResult> getHrList() {
        return hrList;
    }

    public void setHrList(List<VisHarvestResult> hrList) {
        this.hrList = hrList;
    }
}
