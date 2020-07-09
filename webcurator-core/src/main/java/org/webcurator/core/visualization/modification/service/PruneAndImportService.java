package org.webcurator.core.visualization.modification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandResult;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRow;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRowMetadata;
import org.webcurator.core.visualization.VisualizationServiceInterface;

import java.util.List;

public interface PruneAndImportService extends VisualizationServiceInterface {
    Logger log = LoggerFactory.getLogger(PruneAndImportService.class);

    PruneAndImportCommandRowMetadata uploadFile(long job, int harvestResultNumber, PruneAndImportCommandRow cmd);

    PruneAndImportCommandResult checkFiles(long job, int harvestResultNumber, List<PruneAndImportCommandRowMetadata> items);

    PruneAndImportCommandResult applyPruneAndImport(PruneAndImportCommandApply cmd);
}
