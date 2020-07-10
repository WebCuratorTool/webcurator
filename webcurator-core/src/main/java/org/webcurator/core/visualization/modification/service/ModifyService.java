package org.webcurator.core.visualization.modification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.modification.metadata.ModifyResult;
import org.webcurator.core.visualization.modification.metadata.ModifyRow;
import org.webcurator.core.visualization.modification.metadata.ModifyRowMetadata;
import org.webcurator.core.visualization.VisualizationServiceInterface;

import java.util.List;

public interface ModifyService extends VisualizationServiceInterface {
    Logger log = LoggerFactory.getLogger(ModifyService.class);

    ModifyRowMetadata uploadFile(long job, int harvestResultNumber, ModifyRow cmd);

    ModifyResult checkFiles(long job, int harvestResultNumber, List<ModifyRowMetadata> items);

    ModifyResult applyPruneAndImport(ModifyApplyCommand cmd);
}
