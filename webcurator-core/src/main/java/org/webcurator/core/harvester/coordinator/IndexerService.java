package org.webcurator.core.harvester.coordinator;

import java.util.Collection;

import org.webcurator.domain.model.core.ArcHarvestFileDTO;
import org.webcurator.domain.model.core.ArcHarvestResourceDTO;
import org.webcurator.domain.model.core.ArcIndexResultDTO;
import org.webcurator.domain.model.core.HarvestResultDTO;

public interface IndexerService {
    
    /**
     * Create a HarvestResult on the server in the Indexing state.
     * @param harvestResultDTO A DTO for the Harvest Result.
     * @return The OID of the Harvest Result.
     */
    Long createHarvestResult(HarvestResultDTO harvestResultDTO);


    /**
     * Add an ArcHarvestFile to a HarvestResult.
     * @param arcIndexResultDTO  The OID of the HarvestResult and the ArcHarvestFile DTO.
     */
    void addToHarvestResult(Long harvestResultOid, ArcIndexResultDTO arcIndexResultDTO);
    
    /**
     * Finalise the index by marking it as complete.
     */
    void finaliseIndex(Long harvestResultOid);
    
    /**
     * Notification that AQA is complete.
     */
    void notifyAQAComplete(String aqaId);
    
    
    void addHarvestResources(Long harvestResultOid, Collection<ArcHarvestResourceDTO> harvestResources);
}
