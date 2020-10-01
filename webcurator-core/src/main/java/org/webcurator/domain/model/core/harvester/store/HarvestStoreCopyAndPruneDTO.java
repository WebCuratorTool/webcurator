package org.webcurator.domain.model.core.harvester.store;

import org.webcurator.domain.model.core.HarvestResourceDTO;

import java.util.List;

public class HarvestStoreCopyAndPruneDTO {
    List<String> urisToDelete;
    List<HarvestResourceDTO> harvestResourcesToImport;

    public List<String> getUrisToDelete() {
        return urisToDelete;
    }

    public void setUrisToDelete(List<String> urisToDelete) {
        this.urisToDelete = urisToDelete;
    }

    public List<HarvestResourceDTO> getHarvestResourcesToImport() {
        return harvestResourcesToImport;
    }

    public void setHarvestResourcesToImport(List<HarvestResourceDTO> harvestResourcesToImport) {
        this.harvestResourcesToImport = harvestResourcesToImport;
    }
}
