package org.webcurator.visualization.app;

public class VisHarvestResult {
    private long hrId;
    private int hrNumber;
    private boolean isIndexed;

    public long getHrId() {
        return hrId;
    }

    public void setHrId(long hrId) {
        this.hrId = hrId;
    }

    public int getHrNumber() {
        return hrNumber;
    }

    public void setHrNumber(int hrNumber) {
        this.hrNumber = hrNumber;
    }

    public boolean isIndexed() {
        return isIndexed;
    }

    public void setIndexed(boolean indexed) {
        isIndexed = indexed;
    }
}
