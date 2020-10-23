package org.webcurator.ui.tools.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.Header;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.webcurator.common.util.Utils;
import org.webcurator.core.coordinator.HarvestResultManager;
import org.webcurator.core.coordinator.WctCoordinator;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.harvester.coordinator.HarvestAgentManager;
import org.webcurator.core.harvester.coordinator.PatchingHarvestLogManager;
import org.webcurator.core.store.DigitalAssetStore;
import org.webcurator.core.util.PatchUtil;
import org.webcurator.core.visualization.VisualizationAbstractApplyCommand;
import org.webcurator.core.visualization.VisualizationConstants;
import org.webcurator.core.visualization.VisualizationDirectoryManager;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.modification.metadata.ModifyRow;
import org.webcurator.core.visualization.modification.metadata.ModifyRowMetadata;
import org.webcurator.core.visualization.networkmap.metadata.NetworkDbVersionDTO;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeDTO;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;
import org.webcurator.domain.TargetInstanceDAO;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.HarvestResultDTO;
import org.webcurator.domain.model.core.LogFilePropertiesDTO;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.ui.target.command.PatchingProgressCommand;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component("harvestModificationHandler")
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class HarvestModificationHandler {
    private static final Pattern p = Pattern.compile("\\/(\\d+)\\/(.*)");
    private static final Pattern CHARSET_PATTERN = Pattern.compile(";\\s+charset=([A-Za-z0-9].[A-Za-z0-9_\\-\\.:]*)");
    private static final Charset CHARSET_LATIN_1 = StandardCharsets.UTF_8; // StandardCharsets.ISO_8859_1;

    private static final Logger log = LoggerFactory.getLogger(HarvestModificationHandler.class);
    @Autowired
    private TargetInstanceDAO targetInstanceDAO;

    @Autowired
    private WctCoordinator wctCoordinator;

    @Autowired
    private HarvestAgentManager harvestAgentManager;

    @Autowired
    private DigitalAssetStore digitalAssetStore;

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
    private HarvestResultManager harvestResultManager;

    @Autowired
    private VisualizationDirectoryManager directoryManager;

    @Value("${core.base.dir}")
    private String baseDir;

    @Value("${qualityReviewToolController.archiveUrl}")
    private String openWayBack;

    @Autowired
    private BrowseHelper browseHelper;

    public void clickStart(long targetInstanceId, int harvestResultNumber) throws WCTRuntimeException, DigitalAssetStoreException {
        HarvestResultDTO hr = harvestResultManager.getHarvestResultDTO(targetInstanceId, harvestResultNumber);
        if (hr.getStatus() != HarvestResult.STATUS_SCHEDULED) {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
            ModifyApplyCommand cmd = (ModifyApplyCommand) PatchUtil.modifier.readPatchJob(directoryManager.getBaseDir(), targetInstanceId, harvestResultNumber);
            wctCoordinator.patchHarvest(cmd);
        } else if (hr.getState() == HarvestResult.STATE_MODIFYING) {
            wctCoordinator.pushPruneAndImport(targetInstanceId, harvestResultNumber);
        } else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            digitalAssetStore.initiateIndexing(hr);
        } else {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }
    }

    public void clickPause(long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException, WCTRuntimeException {
        HarvestResultDTO hr = harvestResultManager.getHarvestResultDTO(targetInstanceId, harvestResultNumber);
        if (hr.getStatus() != HarvestResult.STATUS_RUNNING) {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
            harvestAgentManager.pausePatching(PatchUtil.getPatchJobName(targetInstanceId, harvestResultNumber));

            //Change the status of Harvest Result
            hr.setStatus(HarvestResult.STATUS_PAUSED);
        } else if (hr.getState() == HarvestResult.STATE_MODIFYING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, "pause", targetInstanceId, harvestResultNumber);
        } else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_INDEXING, "pause", targetInstanceId, harvestResultNumber);
        } else {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }
    }

    public void clickResume(long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException, WCTRuntimeException {
        HarvestResultDTO hr = harvestResultManager.getHarvestResultDTO(targetInstanceId, harvestResultNumber);
        if (hr.getStatus() != HarvestResult.STATUS_PAUSED) {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
            harvestAgentManager.resumePatching(PatchUtil.getPatchJobName(targetInstanceId, harvestResultNumber));
            //Change the status of Harvest Result
            hr.setStatus(HarvestResult.STATUS_PAUSED);
        } else if (hr.getState() == HarvestResult.STATE_MODIFYING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, "resume", targetInstanceId, harvestResultNumber);
        } else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_INDEXING, "resume", targetInstanceId, harvestResultNumber);
        } else {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }
    }

    public void clickTerminate(long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException, WCTRuntimeException {
        HarvestResultDTO hr = harvestResultManager.getHarvestResultDTO(targetInstanceId, harvestResultNumber);
        if (hr.getStatus() != HarvestResult.STATUS_RUNNING &&
                hr.getStatus() != HarvestResult.STATUS_PAUSED &&
                hr.getStatus() != HarvestResult.STATUS_TERMINATED) {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
            String jobName = PatchUtil.getPatchJobName(targetInstanceId, harvestResultNumber);
            harvestAgentManager.stopPatching(jobName);
            harvestAgentManager.abortPatching(jobName);
        } else if (hr.getState() == HarvestResult.STATE_MODIFYING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, "terminate", targetInstanceId, harvestResultNumber);
        } else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_INDEXING, "terminate", targetInstanceId, harvestResultNumber);
        } else {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        //Change the status of Harvest Result
        hr.setStatus(HarvestResult.STATUS_TERMINATED);
        targetInstanceDAO.save(hr);
    }

    public void clickDelete(long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException, WCTRuntimeException {
        HarvestResultDTO hr = harvestResultManager.getHarvestResultDTO(targetInstanceId, harvestResultNumber);
        if (hr.getStatus() != HarvestResult.STATUS_SCHEDULED && hr.getStatus() != HarvestResult.STATUS_TERMINATED) {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
            String jobName = PatchUtil.getPatchJobName(targetInstanceId, harvestResultNumber);
            harvestAgentManager.abortPatching(jobName);
            List<String> jobList = new ArrayList<>();
            jobList.add(jobName);
            harvestAgentManager.purgeAbortedTargetInstances(jobList);
        } else if (hr.getState() == HarvestResult.STATE_MODIFYING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, "delete", targetInstanceId, harvestResultNumber);
        } else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_INDEXING, "delete", targetInstanceId, harvestResultNumber);
        } else {
            throw new WCTRuntimeException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        TargetInstance ti = targetInstanceDAO.load(targetInstanceId);

        //Delete the selected Harvest Result
        List<HarvestResult> hrList = ti.getHarvestResults();
        for (int i = 0; i < hrList.size(); i++) {
            if (hrList.get(i).getHarvestNumber() == hr.getHarvestNumber()) {
                hrList.remove(i);
                break;
            }
        }
        targetInstanceDAO.delete(hr);

        //Change the state of Target Instance to 'Harvested'
        if (ti.getPatchingHarvestResults().size() == 0) {
            ti.setState(TargetInstance.STATE_HARVESTED);
        }
        targetInstanceDAO.save(ti);
    }

    public Map<String, Object> getHarvestResultViewData(long targetInstanceId, long harvestResultId, int harvestResultNumber) throws IOException, NoSuchAlgorithmException {
        Map<String, Object> result = new HashMap<>();
        final HarvestResultDTO hrDTO;
        try {
            hrDTO = harvestResultManager.getHarvestResultDTO(targetInstanceId, harvestResultNumber);
        } catch (WCTRuntimeException e) {
            log.error(e.getMessage());
            result.put("respCode", 1);
            result.put("respMsg", e.getMessage());
            return result;
        }

        PatchingProgressCommand progress = new PatchingProgressCommand();
        progress.setPercentageSchedule(100);
        progress.setPercentageHarvest(hrDTO.getCrawlingProgressPercentage(networkMapClient));
        progress.setPercentageModify(hrDTO.getModifyingProgressPercentage(networkMapClient));
        progress.setPercentageIndex(hrDTO.getIndexingProgressPercentage(networkMapClient));

        VisualizationAbstractApplyCommand cmd = PatchUtil.modifier.readPatchJob(baseDir, targetInstanceId, harvestResultNumber);
        if (cmd == null) {
            cmd = PatchUtil.modifier.readHistoryPatchJob(baseDir, targetInstanceId, harvestResultNumber);
        }

        ModifyApplyCommand pruneAndImportCommandApply = null;
        if (cmd != null) {
            pruneAndImportCommandApply = (ModifyApplyCommand) cmd;
        } else {
            pruneAndImportCommandApply = new ModifyApplyCommand();
        }

        Map<String, ModifyRowMetadata> mapToBePruned = new HashMap<>();
        Map<String, ModifyRowMetadata> mapToBeImportedByFile = new HashMap<>();
        Map<String, ModifyRowMetadata> mapToBeImportedByURL = new HashMap<>();
        pruneAndImportCommandApply.getDataset().forEach(e -> {
            e.setRespCode(VisualizationConstants.RESP_CODE_INDEX_NOT_EXIST);
            if (e.getOption().equalsIgnoreCase("prune")) {
                mapToBePruned.put(e.getUrl(), e);
            } else if (e.getOption().equalsIgnoreCase("file")) {
                mapToBeImportedByFile.put(e.getUrl(), e);
            } else if (e.getOption().equalsIgnoreCase("url")) {
                mapToBeImportedByURL.put(e.getUrl(), e);
            }
        });

        /*Appended indexed results*/
        if (hrDTO.getState() == HarvestResult.STATE_UNASSESSED) {
            Map<String, Boolean> mapIndexedUrlNodes = getIndexedUrlNodes(targetInstanceId, harvestResultNumber, pruneAndImportCommandApply);
            appendIndexedResult(mapIndexedUrlNodes, mapToBeImportedByFile);
            appendIndexedResult(mapIndexedUrlNodes, mapToBeImportedByURL);

            mapToBePruned.forEach((k, v) -> {
                if (!mapIndexedUrlNodes.containsKey(k)) {
                    v.setRespCode(VisualizationConstants.RESP_CODE_SUCCESS);
                } else if ((mapToBeImportedByFile.containsKey(k) && mapToBeImportedByFile.get(k).getRespCode() == VisualizationConstants.RESP_CODE_SUCCESS) ||
                        (mapToBeImportedByURL.containsKey(k) && mapToBeImportedByURL.get(k).getRespCode() == VisualizationConstants.RESP_CODE_SUCCESS)) {
                    v.setRespCode(VisualizationConstants.RESP_CODE_SUCCESS);
                } else {
                    v.setRespCode(VisualizationConstants.RESP_CODE_INDEX_NOT_EXIST);
                }
            });
        }

        List<LogFilePropertiesDTO> logsCrawling = new ArrayList<>();
        List<LogFilePropertiesDTO> logsModifying = new ArrayList<>();
        List<LogFilePropertiesDTO> logsIndexing = new ArrayList<>();
        try {
            logsCrawling = patchingHarvestLogManager.listLogFileAttributes(hrDTO.getTargetInstanceOid(), hrDTO.getHarvestNumber(), HarvestResult.STATE_CRAWLING);
            logsModifying = patchingHarvestLogManagerModification.listLogFileAttributes(hrDTO.getTargetInstanceOid(), hrDTO.getHarvestNumber(), HarvestResult.STATE_MODIFYING);
            logsIndexing = patchingHarvestLogManagerIndex.listLogFileAttributes(hrDTO.getTargetInstanceOid(), hrDTO.getHarvestNumber(), HarvestResult.STATE_INDEXING);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        HarvestResult hr = targetInstanceDAO.getHarvestResult(harvestResultId);
        result.put("respCode", 0);
        result.put("respMsg", "Success");
        result.put("targetInstanceOid", targetInstanceId);
        result.put("harvestResultNumber", harvestResultNumber);
        result.put("derivedHarvestNumber", hr.getDerivedFrom());
        result.put("createdOwner", hr.getCreatedBy().getFullName());
        result.put("createdDate", hr.getCreationDate());
        result.put("hrState", hrDTO.getState());
        result.put("hrStatus", hrDTO.getStatus());

        result.put("progress", progress);

        putDataWithDigest("listToBePruned", mapToBePruned.values(), result);
        putDataWithDigest("listToBeImportedByFile", mapToBeImportedByFile.values(), result);
        putDataWithDigest("listToBeImportedByURL", mapToBeImportedByURL.values(), result);
        putDataWithDigest("logsCrawling", logsCrawling, result);
        putDataWithDigest("logsModifying", logsModifying, result);
        putDataWithDigest("logsIndexing", logsIndexing, result);
        return result;
    }

    public List<HarvestResultDTO> getDerivedHarvestResults(long targetInstanceId, long harvestResultId, int harvestResultNumber) {
        List<HarvestResultDTO> result = new ArrayList<>();
        TargetInstance ti = targetInstanceDAO.load(targetInstanceId);
        if (ti != null) {
            ti.getDerivedHarvestResults(harvestResultNumber).forEach(hr -> {
                HarvestResultDTO hrDTO = harvestResultManager.getHarvestResultDTO(targetInstanceId, hr.getHarvestNumber());
                result.add(hrDTO);
            });
        }

        return result;
    }

    private Map<String, Boolean> getIndexedUrlNodes(long targetInstanceId, int harvestResultNumber, ModifyApplyCommand cmd) throws JsonProcessingException {
        Map<String, Boolean> mapIndexedUrlNodes = new HashMap<>();
        HarvestResultDTO hrDTO = harvestResultManager.getHarvestResultDTO(targetInstanceId, harvestResultNumber);
        if (hrDTO.getState() == HarvestResult.STATE_CRAWLING ||
                hrDTO.getState() == HarvestResult.STATE_MODIFYING ||
                (hrDTO.getState() == HarvestResult.STATE_INDEXING && hrDTO.getStatus() != HarvestResult.STATUS_FINISHED)) {
            return mapIndexedUrlNodes;
        }

        List<String> listQueryUrlStatus = cmd.getDataset().stream().map(ModifyRowMetadata::getUrl).collect(Collectors.toList());
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

    private void appendIndexedResult(Map<String, Boolean> mapIndexedUrlNodes, Map<String, ModifyRowMetadata> mapTargetUrlNodes) {
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

    public void handleDownload(Long hrOid, String url, HttpServletRequest req, HttpServletResponse res) throws IOException, DigitalAssetStoreException {
        url = new String(Base64.getDecoder().decode(url));

        // Build a command with the items from the URL.
        // Load the HarvestResourceDTO from the quality review facade.
        HarvestResult hr = targetInstanceDAO.getHarvestResult(hrOid);
        if (hr == null) {        // If the resource is not found, go to an error page.
            log.error("Resource not found: {}", url);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        TargetInstance ti = hr.getTargetInstance();
        if (ti == null) {        // If the resource is not found, go to an error page.
            log.error("Resource not found: {}", url);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        List<Header> headers = new ArrayList<>();
        try {        // catch any DigitalAssetStoreException and log assumptions
            headers = digitalAssetStore.getHeaders(ti.getOid(), hr.getHarvestNumber(), url);
        } catch (Exception e) {
            log.error("Unexpected exception encountered when retrieving WARC headers for ti " + ti.getOid());
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

        String strStatusCode = getHeaderValue(headers, "HTTP-RESPONSE-STATUS-CODE");
        if (headers.size() == 0 || Utils.isEmpty(strStatusCode)) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        int statusCode = Integer.parseInt(strStatusCode);

        // Send the headers for a redirect.
        if (statusCode == HttpServletResponse.SC_MOVED_TEMPORARILY || statusCode == HttpServletResponse.SC_MOVED_PERMANENTLY) {
            res.setStatus(statusCode);
            String location = getHeaderValue(headers, "Location");
            if (!Utils.isEmpty(location) && !location.startsWith("http")) {
                location = url + location;
            }
            String encodedLocation = Base64.getEncoder().encodeToString(location.getBytes());
            res.setHeader("Location", String.format("/curator/tools/browse/%d/?url=%s", hrOid, encodedLocation));
        } else {
            // Get the content type.
            res.setHeader("Content-Type", getHeaderValue(headers, "Content-Type"));
            Path path = digitalAssetStore.getResource(ti.getOid(), hr.getHarvestNumber(), url);
            IOUtils.copy(Files.newInputStream(path), res.getOutputStream());
        }
    }


    public void handleBrowse(Long hrOid, String url, HttpServletRequest req, HttpServletResponse res) throws IOException, DigitalAssetStoreException {
        String baseUrl = new String(Base64.getDecoder().decode(url));

        // Build a command with the items from the URL.
        // Load the HarvestResourceDTO from the quality review facade.
        HarvestResult hr = targetInstanceDAO.getHarvestResult(hrOid);
        if (hr == null) {        // If the resource is not found, go to an error page.
            log.error("Resource not found: {}", baseUrl);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        TargetInstance ti = hr.getTargetInstance();
        if (ti == null) {        // If the resource is not found, go to an error page.
            log.error("Resource not found: {}", baseUrl);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        List<Header> headers = new ArrayList<>();
        try {        // catch any DigitalAssetStoreException and log assumptions
            headers = digitalAssetStore.getHeaders(ti.getOid(), hr.getHarvestNumber(), baseUrl);
        } catch (Exception e) {
            log.error("Unexpected exception encountered when retrieving WARC headers for ti " + ti.getOid());
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

        // Get the content type.
        String realContentType = getHeaderValue(headers, "Content-Type");
        String simpleContentType = this.getSimpleContentType(realContentType);

        String charset = null;
        if (realContentType != null) {
            Matcher charsetMatcher = CHARSET_PATTERN.matcher(realContentType);
            if (charsetMatcher.find()) {
                charset = charsetMatcher.group(1);
                log.debug("Desired charset: " + charset + " for " + baseUrl);
            } else {
                log.debug("No charset for: " + baseUrl);
                charset = CHARSET_LATIN_1.name();
                realContentType += ";charset=" + charset;
            }
        }

        String strStatusCode = getHeaderValue(headers, "HTTP-RESPONSE-STATUS-CODE");
        if (headers.size() == 0 || Utils.isEmpty(strStatusCode)) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        int statusCode = Integer.parseInt(getHeaderValue(headers, "HTTP-RESPONSE-STATUS-CODE"));
        // Send the headers for a redirect.
        if (statusCode == HttpServletResponse.SC_MOVED_TEMPORARILY || statusCode == HttpServletResponse.SC_MOVED_PERMANENTLY) {
            res.setStatus(statusCode);
            String location = getHeaderValue(headers, "Location");
            if (!Utils.isEmpty(location) && !location.startsWith("http")) {
                location = baseUrl + location;
            }
            String encodedLocation = Base64.getEncoder().encodeToString(location.getBytes());
            res.setHeader("Location", browseHelper.getResourcePrefix(hrOid) + encodedLocation);
            return;
        }

        // Get the content type.
        res.setHeader("Content-Type", getHeaderValue(headers, "Content-Type"));

        Path path = digitalAssetStore.getResource(ti.getOid(), hr.getHarvestNumber(), baseUrl);
        if (!browseHelper.isReplaceable(simpleContentType)) {
            IOUtils.copy(Files.newInputStream(path), res.getOutputStream());
            path.toFile().delete();
            return;
        }

        int fileLength = (int) path.toFile().length();
        byte[] buf = new byte[fileLength];
        IOUtils.read(Files.newInputStream(path), buf);
        path.toFile().delete();

        StringBuilder content = new StringBuilder(new String(buf));

        Pattern baseUrlGetter = BrowseHelper.getTagMagixPattern("BASE", "HREF");
        Matcher m = baseUrlGetter.matcher(content);
        if (m.find()) {
            String u = m.group(1);
            if (u.startsWith("\"") && u.endsWith("\"") || u.startsWith("'") && u.endsWith("'")) {
                // Ensure the detected Base HREF is not commented
                // out (unusual case, but we have seen it).
                int lastEndComment = content.lastIndexOf("-->", m.start());
                int lastStartComment = content.lastIndexOf("<!--", m.start());
                if (lastStartComment < 0 || lastEndComment > lastStartComment) {
                    baseUrl = u.substring(1, u.length() - 1);
                }
            }
        }
        browseHelper.fix(content, simpleContentType, hrOid, baseUrl);

        res.getOutputStream().write(content.toString().getBytes());
    }

    private String getHeaderValue(List<Header> headers, String key) {
        if (headers != null) {
            for (Header h : headers) {
                if (key.equalsIgnoreCase(h.getName())) {
                    return h.getValue().trim();
                }
            }
        }
        return null;
    }

    /**
     * Get everything before the semi-colon.
     *
     * @param realContentType The full content type from the Heritrix ARC file.
     * @return The part of the content type before the semi-colon.
     */
    private String getSimpleContentType(String realContentType) {
        return (realContentType == null || realContentType.indexOf(';') < 0) ? realContentType
                : realContentType.substring(0, realContentType.indexOf(';'));
    }

    public Map<String, String> getGlobalSettings(long targetInstanceId, long harvestResultId, int harvestResultNumber) {
        NetworkMapResult resultDbVersion = networkMapClient.getDbVersion(targetInstanceId, harvestResultNumber);
        NetworkDbVersionDTO versionDTO = networkMapClient.getDbVersionDTO(resultDbVersion.getPayload());
        Map<String, String> map = new HashMap<>();
        map.put("retrieveResult", Integer.toString(versionDTO.getRetrieveResult()));
        map.put("globalVersion", versionDTO.getGlobalVersion());
        map.put("currentVersion", versionDTO.getCurrentVersion());
        map.put("openWayBack", openWayBack);
        return map;
    }


    public List<BulkImportFileRow> bulkImportParse(long targetInstanceId, int harvestResultNumber, ModifyRow cmd) throws IOException, DigitalAssetStoreException {
        int idx = cmd.getContent().indexOf("base64");
        if (idx < 0) {
            log.error("Not a base64 encoded stream");
            return null;
        }
        byte[] doc = Base64.getDecoder().decode(cmd.getContent().substring(idx + 7));
        ByteArrayInputStream docInputStream = new ByteArrayInputStream(doc);

        Workbook workbook = new XSSFWorkbook(docInputStream);
        Sheet sheet = workbook.getSheetAt(0);

        List<BulkImportFileRow> importFileRows = new ArrayList<>();
        int i = 0;
        Map<Integer, String> headerIndex = new HashMap<>();
        for (Row row : sheet) {
            int col = 0;

            if (i == 0) {
                for (Cell cell : row) {
                    String colValue = cell.getStringCellValue();
                    headerIndex.put(col, colValue);
                    col++;
                }

            } else {
                BulkImportFileRow bulkImportFileRowObject = new BulkImportFileRow();
                for (Cell cell : row) {
                    String colKey = headerIndex.get(col);
                    String colValue = cell.getStringCellValue();
                    BulkImportFileRow.setValue(bulkImportFileRowObject, colKey, colValue);
                    col++;
                }
                importFileRows.add(bulkImportFileRowObject);
            }

            i++;
        }

        for (BulkImportFileRow row : importFileRows) {
            if (Utils.isEmpty(row.getTarget())) {
                row.setRespCode(-1);
                continue;
            }

            NetworkMapResult networkMapResult = networkMapClient.getUrlByName(targetInstanceId, harvestResultNumber, row.getTarget());

            String err = String.format("Could not find NetworkMapNode with targetInstanceId=%d, harvestResultNumber=%d, resourceUrl=%s", targetInstanceId, harvestResultNumber, row.getTarget());
            if (networkMapResult == null || networkMapResult.getRspCode() != 0) {
                log.warn(err);
                row.setRespCode(-1);
                continue;
            }

            String json = (String) networkMapResult.getPayload();
            NetworkMapNodeDTO node = networkMapClient.getNodeEntity(json);
            if (node == null) {
                log.warn(err);
                row.setRespCode(-1);
            } else {
                row.copy(node);
                row.setRespCode(0);

                node.clear();
            }
        }

        return importFileRows;
    }
}

class BulkImportFileRow extends NetworkMapNodeDTO {
    private String option;
    private String target;
    private String modificationDateMode;
    private String modificationDateTime;
    private int respCode;

    @JsonIgnore
    public static void setValue(BulkImportFileRow row, String key, String value) {
        if (Utils.isEmpty(key) || Utils.isEmpty(value)) {
            return;
        }

        if (key.trim().equalsIgnoreCase("option")) {
            row.option = value.toUpperCase();
        } else if (key.trim().equalsIgnoreCase("target")) {
            row.target = value;
        } else if (key.trim().equalsIgnoreCase("modificationDateMode")) {
            row.modificationDateMode = value.toUpperCase();
        } else if (key.trim().equalsIgnoreCase("modificationDateTime")) {
            row.modificationDateTime = value;
        }
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getModificationDateMode() {
        return modificationDateMode;
    }

    public void setModificationDateMode(String modificationDateMode) {
        this.modificationDateMode = modificationDateMode;
    }

    public String getModificationDateTime() {
        return modificationDateTime;
    }

    public void setModificationDateTime(String modificationDateTime) {
        this.modificationDateTime = modificationDateTime;
    }

    public int getRespCode() {
        return respCode;
    }

    public void setRespCode(int respCode) {
        this.respCode = respCode;
    }
}