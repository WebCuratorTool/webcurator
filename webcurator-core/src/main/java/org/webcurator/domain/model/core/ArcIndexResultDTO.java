package org.webcurator.domain.model.core;

import java.util.List;

public class ArcIndexResultDTO {
    private long harvestResultOid;
    private List<ArcHarvestFileDTO> harvestFileDTOs;

    public long getHarvestResultOid() {
        return harvestResultOid;
    }

    public void setHarvestResultOid(long harvestResultOid) {
        this.harvestResultOid = harvestResultOid;
    }

    public List<ArcHarvestFileDTO> getHarvestFileDTOs() {
        return harvestFileDTOs;
    }

    public void setHarvestFileDTOs(List<ArcHarvestFileDTO> harvestFileDTOs) {
        this.harvestFileDTOs = harvestFileDTOs;
    }
}
