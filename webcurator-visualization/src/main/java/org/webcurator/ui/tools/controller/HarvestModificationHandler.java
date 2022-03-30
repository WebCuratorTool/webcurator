package org.webcurator.ui.tools.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.webcurator.common.util.Utils;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.harvester.coordinator.PatchingHarvestLogManager;
import org.webcurator.core.visualization.VisualizationConstants;
import org.webcurator.core.visualization.VisualizationDirectoryManager;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.modification.metadata.ModifyResult;
import org.webcurator.core.visualization.modification.metadata.ModifyRowFullData;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeDTO;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapUrl;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.HarvestResultDTO;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Component("harvestModificationHandler")
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class HarvestModificationHandler {
    private static final Logger log = LoggerFactory.getLogger(HarvestModificationHandler.class);

    @Autowired
    @Qualifier(HarvestResult.PATCH_STAGE_TYPE_CRAWLING)
    private PatchingHarvestLogManager patchingHarvestLogManager;

    @Autowired
    @Qualifier(HarvestResult.PATCH_STAGE_TYPE_MODIFYING)
    private PatchingHarvestLogManager patchingHarvestLogManagerModification;

    @Autowired
    @Qualifier(HarvestResult.PATCH_STAGE_TYPE_INDEXING)
    private PatchingHarvestLogManager patchingHarvestLogManagerIndex;

    @Autowired
    private NetworkMapClient networkMapClient;

    @Autowired
    private VisualizationDirectoryManager directoryManager;

    @Value("${qualityReviewToolController.archiveUrl}")
    private String openWayBack;

    public List<HarvestResultDTO> getDerivedHarvestResults(long targetInstanceId, long harvestResultId, int harvestResultNumber) {
        return new ArrayList<HarvestResultDTO>();
    }

    private Map<String, Boolean> getIndexedUrlNodes(long targetInstanceId, int harvestResultNumber, ModifyApplyCommand cmd) throws IOException {
        Map<String, Boolean> mapIndexedUrlNodes = new HashMap<>();

        List<String> listQueryUrlStatus = cmd.getDataset().stream().map(ModifyRowFullData::getUrl).collect(Collectors.toList());
        NetworkMapResult urlsResult = networkMapClient.getUrlsByNames(targetInstanceId, harvestResultNumber, listQueryUrlStatus);
        if (urlsResult.getRspCode() != NetworkMapResult.RSP_CODE_SUCCESS) {
            return mapIndexedUrlNodes;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        List<NetworkMapNodeDTO> listUrlNodes = objectMapper.readValue(urlsResult.getPayload(), new TypeReference<List<NetworkMapNodeDTO>>() {
        });
        listUrlNodes.forEach(urlNode -> {
            mapIndexedUrlNodes.put(urlNode.getUrl(), true);
        });

        return mapIndexedUrlNodes;
    }

    private void appendIndexedResult(Map<String, Boolean> mapIndexedUrlNodes, Map<String, ModifyRowFullData> mapTargetUrlNodes) {
        mapTargetUrlNodes.forEach((k, v) -> {
            if (mapIndexedUrlNodes.containsKey(k)) {
                v.setRespCode(VisualizationConstants.RESP_CODE_SUCCESS);
            } else {
                v.setRespCode(VisualizationConstants.RESP_CODE_INDEX_NOT_EXIST);
            }
        });
    }

    private String getDigest(Object obj) throws NoSuchAlgorithmException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(obj);

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(json.getBytes());
        byte[] digest = md.digest();

        return new String(Base64.getEncoder().encode(digest));
    }

    private void putDataWithDigest(String key, Object data, Map<String, Object> result) throws JsonProcessingException, NoSuchAlgorithmException {
        String digest = getDigest(data);
        Map<String, Object> pair = new HashMap<>();
        pair.put("digest", digest);
        pair.put("data", data);

        result.put(key, pair);
    }

    public NetworkMapResult bulkImportParse(long targetInstanceId, int harvestResultNumber, ModifyRowFullData cmd) throws IOException, DigitalAssetStoreException {
        int idx = cmd.getUploadFileContent().indexOf("base64");
        if (idx < 0) {
            log.error("Not a base64 encoded stream");
            return NetworkMapResult.getBadRequestResult("Invalid metadata file: is not a base64 encoded stream.");
        }

        byte[] doc = Base64.getDecoder().decode(cmd.getUploadFileContent().substring(idx + 7));
        ByteArrayInputStream docInputStream = new ByteArrayInputStream(doc);

        Workbook workbook = new XSSFWorkbook(docInputStream);
        Sheet sheet = workbook.getSheetAt(0);

        List<ModifyRowFullData> importFileRows = new ArrayList<>();
        int i = 0;
        Map<Integer, String> headerIndex = new HashMap<>();
        for (Row row : sheet) {
            int col = 0;

            if (i == 0) {
                for (Cell cell : row) {
                    String colValue = getValueFromCell(cell);
                    headerIndex.put(col, colValue);
                    col++;
                }

            } else {
                ModifyRowFullData bulkImportFileRowObject = new ModifyRowFullData();
                for (Cell cell : row) {
                    String colKey = headerIndex.get(col);
                    String colValue = getValueFromCell(cell);
                    ModifyRowFullData.setValue(bulkImportFileRowObject, colKey, colValue);
                    col++;
                }
                if (Utils.isEmpty(bulkImportFileRowObject.getOption()) || Utils.isEmpty(bulkImportFileRowObject.getUrl())) {
                    log.warn("Invalid row: " + i);
                    continue;
                }

                if (!Utils.isEmpty(bulkImportFileRowObject.getOption()) && !Utils.isEmpty(bulkImportFileRowObject.getUrl())) {
                    importFileRows.add(bulkImportFileRowObject);
                }
            }

            i++;
        }

        NetworkMapResult result = NetworkMapResult.getSuccessResult();
        result.setPayload(networkMapClient.obj2Json(importFileRows));
        return result;
    }

    private String getValueFromCell(Cell cell) {
        CellType type = cell.getCellType();
        String value;
        switch (cell.getCellType()) {
            case STRING:
            case BLANK:
                value = cell.getStringCellValue();
                break;
            case NUMERIC:
                value = String.format("%f", cell.getNumericCellValue());
                break;
            case BOOLEAN:
                value = Boolean.toString(cell.getBooleanCellValue());
                break;
            default:
                value = "";
        }
        return value;
    }

    protected void exportData(long targetInstanceId, int harvestResultNumber, List<ModifyRowFullData> dataset, HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        Resource resource = new ClassPathResource("bulk-modification-template.xlsx");
        Workbook workbook = new XSSFWorkbook(resource.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        int rowIndex = 1;
        for (ModifyRowFullData rowMetadata : dataset) {
            if (Utils.isEmpty(rowMetadata.getUrl())) {
                continue;
            }

            Row rowExcel = sheet.createRow(rowIndex++);
            if (!Utils.isEmpty(rowMetadata.getOption())) {
                Cell colOption = rowExcel.createCell(0);
                colOption.setCellValue(rowMetadata.getOption());
            }

            NetworkMapUrl networkMapUrl = new NetworkMapUrl();
            networkMapUrl.setUrlName(rowMetadata.getUrl());
            NetworkMapResult result = networkMapClient.getUrlByName(targetInstanceId, harvestResultNumber, networkMapUrl);
            if (result.getRspCode() == NetworkMapResult.RSP_ERROR_DATA_NOT_EXIST) {
                Cell colExistingFlag = rowExcel.createCell(2);
                colExistingFlag.setCellValue("No");
            } else if (result.getRspCode() == NetworkMapResult.RSP_CODE_SUCCESS) {
                Cell colExistingFlag = rowExcel.createCell(2);
                colExistingFlag.setCellValue("Yes");

                NetworkMapNodeDTO nodeDTO = networkMapClient.getNodeEntity(result.getPayload());
                if (nodeDTO == null) {
                    Cell colTarget = rowExcel.createCell(1);
                    colTarget.setCellValue(rowMetadata.getUrl());
                    log.error("Could not find URL node with: {}", rowMetadata.getUrl());
                    continue;
                }

                Cell colTarget = rowExcel.createCell(1);
                colTarget.setCellValue(nodeDTO.getUrl());

                if (!Utils.isEmpty(rowMetadata.getUploadFileName())) {
                    Cell colLocalFileName = rowExcel.createCell(3);
                    colLocalFileName.setCellValue(rowMetadata.getUploadFileName());
                }

                if (!Utils.isEmpty(rowMetadata.getModifiedMode())) {
                    Cell colModifiedMode = rowExcel.createCell(4);
                    colModifiedMode.setCellValue(rowMetadata.getModifiedMode());
                }

                if (rowMetadata.getLastModifiedDate() > 0) {
                    Cell colLastModified = rowExcel.createCell(5);
                    colLastModified.setCellValue(rowMetadata.getLastModifiedDate());
                }

                Cell colContentType = rowExcel.createCell(6);
                colContentType.setCellValue(nodeDTO.getContentType());

                Cell colStatusCode = rowExcel.createCell(7);
                colStatusCode.setCellValue(nodeDTO.getStatusCode());

                Cell colContentLength = rowExcel.createCell(8);
                colContentLength.setCellValue(nodeDTO.getContentLength());

                Cell colTotUrls = rowExcel.createCell(9);
                colTotUrls.setCellValue(nodeDTO.getTotUrls());

                Cell colTotFailed = rowExcel.createCell(10);
                colTotFailed.setCellValue(nodeDTO.getTotFailed());

                Cell colTotSuccess = rowExcel.createCell(11);
                colTotSuccess.setCellValue(nodeDTO.getTotSuccess());

                Cell colTotContentLength = rowExcel.createCell(12);
                colTotContentLength.setCellValue(nodeDTO.getTotSize());
            } else {
                log.warn(result.getRspMsg());
            }
        }

        rsp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        workbook.write(rsp.getOutputStream());
        workbook.close();
    }


    public NetworkMapResult checkAndAppendModificationRows(long targetInstanceId, int harvestResultNumber, List<ModifyRowFullData> dataset) {
        if (dataset == null) {
            return NetworkMapResult.getBadRequestResult();
        }

        for (ModifyRowFullData row : dataset) {
            if (Utils.isEmpty(row.getOption()) || Utils.isEmpty(row.getUrl())) {
                return NetworkMapResult.getBadRequestResult("Option field and target field can not be empty.");
            }

            NetworkMapUrl networkMapUrl = new NetworkMapUrl();
            networkMapUrl.setUrlName(row.getUrl());
            NetworkMapResult networkMapResult = networkMapClient.getUrlByName(targetInstanceId, harvestResultNumber, networkMapUrl);

            String err = String.format("Could not find NetworkMapNode with targetInstanceId=%d, harvestResultNumber=%d, resourceUrl=%s", targetInstanceId, harvestResultNumber, row.getUrl());
            if (networkMapResult == null) {
                log.error(err);
                return NetworkMapResult.getBadRequestResult(err);
            }

            if (networkMapResult.getRspCode() == NetworkMapResult.RSP_ERROR_DATA_NOT_EXIST) {
                row.setExistingFlag(false);
                row.setUrl(row.getUrl());
            } else if (networkMapResult.getRspCode() == NetworkMapResult.RSP_CODE_SUCCESS) {
                String json = (String) networkMapResult.getPayload();
                NetworkMapNodeDTO node = networkMapClient.getNodeEntity(json);
                if (node == null) {
                    log.warn(err);
                    return NetworkMapResult.getBadRequestResult(err);
                }

                row.copy(node);
                row.setExistingFlag(true);
                row.setRespCode(0);
                node.clear();
            }
        }
        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(networkMapClient.obj2Json(dataset));
        return result;
    }

    public ModifyRowFullData uploadFile(long job, int harvestResultNumber, ModifyRowFullData cmd) {
        ModifyRowFullData result = this.saveImportedFile(job, cmd);

        return result;
    }

    private ModifyRowFullData saveImportedFile(long job, ModifyRowFullData cmd) {
        File uploadedFilePath = new File(directoryManager.getUploadDir(job));
        if (!uploadedFilePath.exists()) {
            uploadedFilePath.mkdirs();
        }

        String cacheFileName = UUID.randomUUID().toString();

        uploadedFilePath = new File(uploadedFilePath, cacheFileName);
        if (uploadedFilePath.exists()) {
            uploadedFilePath.delete();
        }
        cmd.setCachedFileName(cacheFileName);

        try {
            int idx = cmd.getUploadFileContent().indexOf("base64");
            byte[] doc = Base64.getDecoder().decode(cmd.getUploadFileContent().substring(idx + 7));
            if (uploadedFilePath.exists()) {
                Files.write(uploadedFilePath.toPath(), doc, StandardOpenOption.APPEND);
            } else {
                Files.write(uploadedFilePath.toPath(), doc, StandardOpenOption.CREATE_NEW);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            cmd.setRespCode(VisualizationConstants.RESP_CODE_ERROR_FILE_IO);
            cmd.setRespMsg("Failed to write upload file to " + uploadedFilePath.getAbsolutePath());
            return cmd;
        }

        cmd.setRespCode(VisualizationConstants.RESP_CODE_SUCCESS);
        cmd.setRespMsg("OK");
        return cmd;
    }


    public ModifyResult checkFiles(long job, int harvestResultNumber, List<ModifyRowFullData> items) {
        return this.checkFiles(job, items);
    }

    private ModifyResult checkFiles(long job, List<ModifyRowFullData> items) {
        ModifyResult result = new ModifyResult();

        /**
         * Checking do files exist and attaching properties for existing files
         */
        boolean isValid = true;
        for (ModifyRowFullData metadata : items) {
            //Ignore elements which are not 'file'
            if (!metadata.getOption().equalsIgnoreCase("FILE")) {
                continue;
            }

            //Check do files exist: walk through all elemenets
            File uploadedFilePath = new File(directoryManager.getUploadDir(job), metadata.getCachedFileName());
            if (!uploadedFilePath.exists()) {
                metadata.setRespCode(VisualizationConstants.FILE_EXIST_NO); //Not exist
                metadata.setRespMsg("File or metadata is not uploaded");
                isValid = false;
            }
        }

        if (!isValid) {
            result.setRespCode(VisualizationConstants.FILE_EXIST_NO);
            result.setRespMsg("Not all files are uploaded");
        } else {
            result.setRespCode(VisualizationConstants.RESP_CODE_FILE_EXIST);
            result.setRespMsg("OK");
        }
        result.setMetadataDataset(items);

        return result;
    }

    public ModifyResult applyPruneAndImport(ModifyApplyCommand cmd) {
        /**
         * Checking do files exist and attaching properties for existing files
         */
        ModifyResult result = this.checkFiles(cmd.getTargetInstanceId(), cmd.getDataset());
        if (result.getRespCode() != VisualizationConstants.RESP_CODE_SUCCESS &&
                result.getRespCode() != VisualizationConstants.RESP_CODE_FILE_EXIST) {
            log.error(result.getRespMsg());
            return result;
        }

        return result;
    }
}
