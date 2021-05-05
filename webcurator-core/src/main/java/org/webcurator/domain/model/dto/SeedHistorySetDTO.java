package org.webcurator.domain.model.dto;

import org.webcurator.domain.model.core.SeedHistoryDTO;

import java.util.HashSet;
import java.util.Set;

public class SeedHistorySetDTO {
    Set<SeedHistoryDTO> seeds = new HashSet<>();
    Long targetInstanceId;

    public Set<SeedHistoryDTO> getSeeds() {
        return seeds;
    }

    public void setSeeds(Set<SeedHistoryDTO> seeds) {
        this.seeds = seeds;
    }

    public Long getTargetInstanceId() {
        return targetInstanceId;
    }

    public void setTargetInstanceId(Long targetInstanceId) {
        this.targetInstanceId = targetInstanceId;
    }
}
