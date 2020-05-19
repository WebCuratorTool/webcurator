package org.webcurator.core.visualization.modification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.webcurator.core.visualization.VisualizationManager;
import org.webcurator.core.visualization.modification.*;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandResult;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRow;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRowMetadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@SuppressWarnings("unused")
@Component("pruneAndImportClient")
public class PruneAndImportClient implements PruneAndImportService {
    @Autowired
    private VisualizationManager visualizationManager;

    @Override
    public PruneAndImportCommandRowMetadata uploadFile(long job, int harvestResultNumber, PruneAndImportCommandRow cmd) {
        PruneAndImportCommandRowMetadata metadata = cmd.getMetadata();
        File uploadedFilePath = new File(visualizationManager.getUploadDir(), metadata.getName());
        if (uploadedFilePath.exists()) {
            if (metadata.isReplaceFlag()) {
                uploadedFilePath.deleteOnExit();
            } else {
                metadata.setRespCode(FILE_EXIST_YES);
                metadata.setRespMsg(String.format("File %s has been exist, return without replacement.", metadata.getName()));
                return metadata;
            }
        }

        try {
            byte[] doc = Base64.getDecoder().decode(cmd.getContent().replace("data:image/png;base64,", ""));
            Files.write(uploadedFilePath.toPath(), doc);
        } catch (IOException e) {
            log.error(e.getMessage());
            metadata.setRespCode(RESP_CODE_ERROR_FILE_IO);
            metadata.setRespMsg("Failed to write upload file to " + uploadedFilePath.getAbsolutePath());
            return metadata;
        }

        metadata.setRespCode(FILE_EXIST_YES);
        metadata.setRespMsg("OK");
        return metadata;
    }

    @Override
    public PruneAndImportCommandRow downloadFile(long job, int harvestResultNumber, String fileName) {
        PruneAndImportCommandRow result = new PruneAndImportCommandRow();
        File uploadedFilePath = new File(visualizationManager.getUploadDir(), fileName);
        try {
            result.setContent(new String(Files.readAllBytes(uploadedFilePath.toPath())));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return result;
    }

    @Override
    public PruneAndImportCommandResult checkFiles(long job, int harvestResultNumber, List<PruneAndImportCommandRowMetadata> items) {
        PruneAndImportCommandResult result = new PruneAndImportCommandResult();
        result.setRespCode(FILE_EXIST_YES);
        result.setRespMsg("OK");
        items.forEach(e -> {
            if (e.getOption().equalsIgnoreCase("file")) {
                File uploadedFilePath = new File(visualizationManager.getUploadDir(), e.getName());
                if (uploadedFilePath.exists()) {
                    e.setRespCode(FILE_EXIST_YES); //Exist
                    e.setRespMsg("OK");
                } else {
                    e.setRespCode(FILE_EXIST_NO); //Not exist
                    e.setRespMsg("File is not uploaded");
                    result.setRespCode(FILE_EXIST_NO);
                    result.setRespMsg("Not all files are uploaded");
                }
            } else {
                e.setRespCode(FILE_EXIST_YES); //For source urls, consider to exist
                e.setRespMsg("OK");
            }
        });

        result.setMetadataDataset(items);

        return result;
    }

    @Override
    public PruneAndImportCommandResult pruneAndImport(PruneAndImportCommandApply cmd) {
        PruneAndImportProcessor p = new PruneAndImportProcessor(visualizationManager.getUploadDir(), visualizationManager.getBaseDir(), cmd);
        new Thread(p).start();

        PruneAndImportCommandResult result = new PruneAndImportCommandResult();
        result.setRespCode(RESP_CODE_SUCCESS);
        result.setRespMsg("Modification task is accepted");
        return result;
    }
}
