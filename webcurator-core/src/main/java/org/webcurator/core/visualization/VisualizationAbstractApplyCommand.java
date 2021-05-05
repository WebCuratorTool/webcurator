package org.webcurator.core.visualization;

public class VisualizationAbstractApplyCommand {
    private long targetInstanceId;
    private long harvestResultId;
    private int harvestResultNumber;
    private int newHarvestResultNumber;

    public long getTargetInstanceId() {
        return targetInstanceId;
    }

    public void setTargetInstanceId(long targetInstanceId) {
        this.targetInstanceId = targetInstanceId;
    }

    public long getHarvestResultId() {
        return harvestResultId;
    }

    public void setHarvestResultId(long harvestResultId) {
        this.harvestResultId = harvestResultId;
    }

    public int getHarvestResultNumber() {
        return harvestResultNumber;
    }

    public void setHarvestResultNumber(int harvestResultNumber) {
        this.harvestResultNumber = harvestResultNumber;
    }

    public int getNewHarvestResultNumber() {
        return newHarvestResultNumber;
    }

    public void setNewHarvestResultNumber(int newHarvestResultNumber) {
        this.newHarvestResultNumber = newHarvestResultNumber;
    }
}
