package org.webcurator.core.networkmap.service;

import org.archive.format.warc.WARCConstants;
import org.archive.io.*;
import org.archive.io.arc.*;
import org.archive.io.warc.*;
import org.archive.uid.UUIDGenerator;
import org.archive.util.anvl.ANVLRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.store.Indexer;
import org.webcurator.domain.model.core.ArcHarvestFileDTO;
import org.webcurator.domain.model.core.HarvestResultDTO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PruneAndImportClientLocal implements PruneAndImportClient {
    private String fileDir; //Upload files
    private String baseDir; //Harvest WARC files
    private final int BYTE_BUFF_SIZE = 1024;

    /**
     * Arc files meta data date format.
     */
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final SimpleDateFormat writerDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    /**
     * The Indexer
     */
    private Indexer indexer = null;


    @Override
    public PruneAndImportCommandRowMetadata uploadFile(long job, int harvestResultNumber, String fileName, boolean replaceFlag, byte[] doc) {
        PruneAndImportCommandRowMetadata cmd = new PruneAndImportCommandRowMetadata();
        cmd.setName(fileName);
        File uploadedFilePath = new File(fileDir, fileName);
        if (uploadedFilePath.exists()) {
            if (replaceFlag) {
                uploadedFilePath.deleteOnExit();
            } else {
                cmd.setRespCode(FILE_EXIST_YES);
                cmd.setRespMsg(String.format("File %s has been exist, return without replacement.", fileName));
                return cmd;
            }
        }

        try {
            Files.write(uploadedFilePath.toPath(), doc);
        } catch (IOException e) {
            log.error(e.getMessage());
            cmd.setRespCode(RESP_CODE_ERROR_FILE_IO);
            cmd.setRespMsg("Failed to write upload file to " + uploadedFilePath.getAbsolutePath());
            return cmd;
        }

        cmd.setRespCode(FILE_EXIST_YES);
        cmd.setRespMsg("OK");
        return cmd;
    }

    @Override
    public PruneAndImportCommandRow downloadFile(long job, int harvestResultNumber, String fileName) {
        PruneAndImportCommandRow result = new PruneAndImportCommandRow();
        File uploadedFilePath = new File(fileDir, fileName);
        try {
            result.setContent(Files.readAllBytes(uploadedFilePath.toPath()));
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
                File uploadedFilePath = new File(fileDir, e.getName());
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
        //TODO: launch a thread to prune and import
        PruneAndImportProcessor p = new PruneAndImportProcessor(this.fileDir, this.baseDir, cmd);
        new Thread(p).start();

        PruneAndImportCommandResult result = new PruneAndImportCommandResult();
        result.setRespCode(RESP_CODE_SUCCESS);
        result.setRespMsg("Modification task is accepted");
        return result;
    }

    public String getFileDir() {
        return fileDir;
    }

    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }
}
