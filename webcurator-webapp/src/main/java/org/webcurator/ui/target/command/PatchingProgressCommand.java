package org.webcurator.ui.target.command;

public class PatchingProgressCommand {
    public static final int STAGE_SCHEDULED = 0;
    public static final int STAGE_CRAWLING = 1;
    public static final int STAGE_MODIFYING = 2;
    public static final int STAGE_INDEXING = 3;
    public static final int STAGE_FiNiSHED = 4;

    public int stage;
    public int percentageSchedule;
    public int percentageHarvest;
    public int percentageModify;
    public int percentageIndex;

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

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
