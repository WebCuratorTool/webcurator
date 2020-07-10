package org.webcurator.core.visualization.modification.metadata;

public class ModifyProgress {
    public int percentageSchedule;
    public int percentageHarvest;
    public int percentageModify;
    public int percentageIndex;

    public int getPercentageSchedule() {
        return percentageSchedule;
    }

    public void setPercentageSchedule(int percentageSchedule) {
        this.percentageSchedule = percentageSchedule;
    }

    public int getPercentageHarvest() {
        return percentageHarvest;
    }

    public void setPercentageHarvest(int percentageHarvest) {
        this.percentageHarvest = percentageHarvest;
    }

    public int getPercentageModify() {
        return percentageModify;
    }

    public void setPercentageModify(int percentageModify) {
        this.percentageModify = percentageModify;
    }

    public int getPercentageIndex() {
        return percentageIndex;
    }

    public void setPercentageIndex(int percentageIndex) {
        this.percentageIndex = percentageIndex;
    }
}
