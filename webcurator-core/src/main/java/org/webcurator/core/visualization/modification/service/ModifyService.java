package org.webcurator.core.visualization.modification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.modification.metadata.ModifyResult;
import org.webcurator.core.visualization.modification.metadata.ModifyRowFullData;
import org.webcurator.core.visualization.VisualizationServiceInterface;

import java.util.List;

public interface ModifyService extends VisualizationServiceInterface {
    Logger log = LoggerFactory.getLogger(ModifyService.class);

    ModifyRowFullData uploadFile(long job, int harvestResultNumber, ModifyRowFullData cmd);

    ModifyResult checkFiles(long job, int harvestResultNumber, List<ModifyRowFullData> items);

    ModifyResult applyPruneAndImport(ModifyApplyCommand cmd);
}
