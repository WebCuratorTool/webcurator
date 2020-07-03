package org.webcurator.core.harvester.coordinator;

import org.webcurator.domain.model.core.HarvestResultDTO;

public interface IndexerService {
    
    /**
     * Create a HarvestResult on the server in the Indexing state.
     * @param harvestResultDTO A DTO for the Harvest Result.
     * @return The OID of the Harvest Result.
     */
    Long createHarvestResult(HarvestResultDTO harvestResultDTO);


    /**
     * Finalise the index by marking it as complete.
     */
    void finaliseIndex(long targetInstanceId, int harvestNumber);
    
    /**
     * Notification that AQA is complete.
     */
    void notifyAQAComplete(String aqaId);
}
