package org.webcurator.core.screenshot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.webcurator.domain.model.core.SeedHistoryDTO;

import java.util.ArrayList;
import java.util.List;

public class ScreenshotIdentifierCommand {
    private List<SeedHistoryDTO> seeds = new ArrayList<>();
    private long tiOid;
    private ScreenshotType screenshotType = ScreenshotType.live;
    private int harvestNumber;

    public List<SeedHistoryDTO> getSeeds() {
        return seeds;
    }

    public void setSeeds(List<SeedHistoryDTO> seeds) {
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

    public int getHarvestNumber() {
        return harvestNumber;
    }

    public void setHarvestNumber(int harvestNumber) {
        this.harvestNumber = harvestNumber;
    }

    @Override
    @JsonIgnore
    public String toString() {
        StringBuffer joined_seeds = new StringBuffer();
        this.seeds.forEach(seed -> joined_seeds.append(seed.getSeed()).append(","));
        return String.format("seed=%s tiOid=%d, screenshotType=%s, harvestNumber=%d", joined_seeds, this.tiOid, this.screenshotType.name(), this.harvestNumber);
    }

    @JsonIgnore
    public void addSeed(SeedHistoryDTO seed) {
        this.seeds.add(seed);
    }
}
