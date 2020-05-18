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
    int RESP_CODE_SUCCESS = 0;
    int RESP_CODE_FILE_EXIST = 1;
    int RESP_CODE_INVALID_REQUEST = -1000;
    int RESP_CODE_ERROR_FILE_IO = -2000;
    int RESP_CODE_ERROR_NETWORK_IO = -3000;
    int RESP_CODE_ERROR_SYSTEM_ERROR = -9000;
    int FILE_EXIST_YES = 1;
    int FILE_EXIST_NO = -1;

    PruneAndImportCommandRowMetadata uploadFile(long job, int harvestResultNumber, String fileName, boolean replaceFlag, byte[] doc);

    PruneAndImportCommandRow downloadFile(long job, int harvestResultNumber, String fileName);

    PruneAndImportCommandResult checkFiles(long job, int harvestResultNumber, List<PruneAndImportCommandRowMetadata> items);

    PruneAndImportCommandResult pruneAndImport(PruneAndImportCommandApply cmd);
}
