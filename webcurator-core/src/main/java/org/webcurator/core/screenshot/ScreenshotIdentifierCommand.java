package org.webcurator.core.screenshot;

public class ScreenshotIdentifierCommand {
    private String seed;
    private long tiOid;
    private ScreenshotType screenshotType;
    private long seedOid;
    private int harvestNumber;
    private String timestamp;

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public long getTiOid() {
        return tiOid;
    }

    public void setTiOid(long tiOid) {
        this.tiOid = tiOid;
    }

    public ScreenshotType getScreenshotType() {
        return screenshotType;
    }

    public void setScreenshotType(ScreenshotType screenshotType) {
        this.screenshotType = screenshotType;
    }

    public long getSeedOid() {
        return seedOid;
    }

    public void setSeedOid(long seedOid) {
        this.seedOid = seedOid;
    }

    public int getHarvestNumber() {
        return harvestNumber;
    }

    public void setHarvestNumber(int harvestNumber) {
        this.harvestNumber = harvestNumber;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        String str = String.format("seed=%s, tiOid=%d, screenshotType=%s, seedOid=%d, harvestNumber=%d, timestamp=%s",
                this.seed, this.tiOid, this.screenshotType.name(), this.seedOid, this.harvestNumber, this.timestamp);
        return str;
    }
}
