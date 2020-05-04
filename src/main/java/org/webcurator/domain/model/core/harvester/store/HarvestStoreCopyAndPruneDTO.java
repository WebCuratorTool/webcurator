package org.webcurator.domain.model.core.harvester.store;

import org.webcurator.domain.model.core.ArcHarvestResourceDTO;

import java.util.List;

public class HarvestStoreCopyAndPruneDTO {
    List<String> urisToDelete;
    List<ArcHarvestResourceDTO> harvestResourcesToImport;

    public List<String> getUrisToDelete() {
        return urisToDelete;
    }

    public void setUrisToDelete(List<String> urisToDelete) {
        this.urisToDelete = urisToDelete;
    }

    public List<ArcHarvestResourceDTO> getHarvestResourcesToImport() {
        return harvestResourcesToImport;
    }

    public void setHarvestResourcesToImport(List<ArcHarvestResourceDTO> harvestResourcesToImport) {
        this.harvestResourcesToImport = harvestResourcesToImport;
    }
}
