package org.webcurator.core.screenshot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.webcurator.domain.model.core.SeedHistory;

import java.util.ArrayList;
import java.util.List;

public class ScreenshotIdentifierCommand {
    private List<SeedHistory> seeds = new ArrayList<>();
    private long tiOid;
    private ScreenshotType screenshotType;
    private long seedOid;
    private int harvestNumber;
    private String timestamp;

    public List<SeedHistory> getSeeds() {
        return seeds;
    }

    public void setSeeds(List<SeedHistory> seeds) {
        this.seeds = seeds;
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
    @JsonIgnore
    public String toString() {
        StringBuffer joined_seeds = new StringBuffer();
        this.seeds.forEach(seed -> {
            joined_seeds.append(seed.getSeed()).append(",");
        });
        String str = String.format("seed=%s tiOid=%d, screenshotType=%s, seedOid=%d, harvestNumber=%d, timestamp=%s",
                joined_seeds.toString(), this.tiOid, this.screenshotType.name(), this.seedOid, this.harvestNumber, this.timestamp);
        return str;
    }
}
