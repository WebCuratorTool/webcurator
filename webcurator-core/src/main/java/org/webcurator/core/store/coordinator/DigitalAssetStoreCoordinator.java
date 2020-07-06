package org.webcurator.core.store.coordinator;

import org.webcurator.core.harvester.coordinator.IndexerService;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.core.visualization.modification.service.PruneAndImportService;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.TargetInstance;

import java.io.IOException;
import java.io.OutputStream;

public interface DigitalAssetStoreCoordinator extends PruneAndImportService, IndexerService, DigitalAssetStoreListener {

    boolean pushPruneAndImport(long targetInstanceId, int harvestResultNumber);
    
    /**
     * Complete the Archiving process
     */
    void completeArchiving(Long targetInstanceOid, String archiveIID);

    /**
     * Failed to complete the Archiving process
     */
    void failedArchiving(Long targetInstanceOid, String message);

    /**
     * Force re-indexing of the specified HarvestResult
     *
     * @param aArcHarvestResult The result to re-index.
     * @return true if a reIndex was initialised
     */
    Boolean reIndexHarvestResult(HarvestResult aArcHarvestResult);

    /**
     * Remove indexes for the target instance. Most indexers do no action, however
     * a wayback indexer needs to remove arc files from the local wayback BDB index
     *
     * @param ti The target instance to remove the indexes for
     * @return void
     */
    void removeIndexes(TargetInstance ti);

    void removeIndexes(HarvestResult hr);

    public void runQaRecommentationService(TargetInstance ti);
}
