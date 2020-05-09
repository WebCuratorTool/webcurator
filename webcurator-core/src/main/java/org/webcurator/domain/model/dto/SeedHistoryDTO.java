package org.webcurator.domain.model.dto;

import org.webcurator.domain.model.core.SeedHistory;

import java.util.Set;

public class SeedHistoryDTO {
    Set<SeedHistory> seeds;
    Long targetInstanceId;

    public Set<SeedHistory> getSeeds() {
        return seeds;
    }

    public void setSeeds(Set<SeedHistory> seeds) {
        this.seeds = seeds;
    }

    public Long getTargetInstanceId() {
        return targetInstanceId;
    }

    public void setTargetInstanceId(Long targetInstanceId) {
        this.targetInstanceId = targetInstanceId;
    }
}
