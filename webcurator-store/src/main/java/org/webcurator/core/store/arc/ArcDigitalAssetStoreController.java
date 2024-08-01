package org.webcurator.core.store.arc;

import org.apache.commons.httpclient.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.store.DigitalAssetStore;
import org.webcurator.core.store.DigitalAssetStorePaths;
import org.webcurator.core.store.HarvestDTO;
import org.webcurator.core.util.WctUtils;
import org.webcurator.core.visualization.VisualizationConstants;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.modification.metadata.ModifyResult;
import org.webcurator.domain.model.core.CustomDepositFormCriteriaDTO;
import org.webcurator.domain.model.core.CustomDepositFormResultDTO;
import org.webcurator.domain.model.core.HarvestResultDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class ArcDigitalAssetStoreController implements DigitalAssetStore {
    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("arcDigitalAssetStoreService")
    private ArcDigitalAssetStoreService arcDigitalAssetStoreService;

    //    @PostMapping(path = DigitalAssetStorePaths.RESOURCE, produces = "application/octet-stream")
//    public @ResponseBody
    @RequestMapping(path = DigitalAssetStorePaths.RESOURCE, method = {RequestMethod.POST, RequestMethod.GET})
    void getResourceExternal(@PathVariable(value = "target-instance-id") long targetInstanceId,
                             @RequestParam(value = "harvest-result-number") int harvestResultNumber,
                             @RequestParam(value = "resource-url") String resourceUrl,
                             HttpServletRequest req,
                             HttpServletResponse rsp) throws DigitalAssetStoreException {
        log.debug("Get resource, target-instance-id: {}, harvest-result-number: {}, resource-url: {}", targetInstanceId, harvestResultNumber, resourceUrl);
        try {
            Path path = getResource(targetInstanceId, harvestResultNumber, URLDecoder.decode(resourceUrl));
            if (path == null) {
                return;
            }
            WctUtils.copy(Files.newInputStream(path), rsp.getOutputStream());
            Files.deleteIfExists(path);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    @Override
    public Path getResource(long targetInstanceId, int harvestResultNumber, String resourceUrl) throws DigitalAssetStoreException {
        return arcDigitalAssetStoreService.getResource(targetInstanceId, harvestResultNumber, resourceUrl);
    }

    @PostMapping(path = DigitalAssetStorePaths.SMALL_RESOURCE)
    public byte[] getSmallResourceExternal(@PathVariable(value = "target-instance-id") long targetInstanceId,
                                           @RequestParam(value = "harvest-result-number") int harvestResultNumber,
                                           @RequestParam(value = "resource-url") String resourceUrl) throws DigitalAssetStoreException {
        log.debug("Get resource, target-instance-id: {}, harvest-result-number: {}, resource-url: {}", targetInstanceId, harvestResultNumber, resourceUrl);
        try {
            return getSmallResource(targetInstanceId, harvestResultNumber, URLDecoder.decode(resourceUrl));
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return null;
    }

    @Override
    public byte[] getSmallResource(long targetInstanceId, int harvestResultNumber, String resourceUrl) throws DigitalAssetStoreException {
        return arcDigitalAssetStoreService.getSmallResource(targetInstanceId, harvestResultNumber, resourceUrl);
    }

    @PostMapping(path = DigitalAssetStorePaths.HEADERS)
    public List<Header> getHeadersExternal(@PathVariable(value = "target-instance-id") long targetInstanceId,
                                           @RequestParam(value = "harvest-result-number") int harvestResultNumber,
                                           @RequestParam(value = "resource-url") String resourceUrl) throws DigitalAssetStoreException {
        log.debug("Get resource, target-instance-id: {}, harvest-result-number: {}, resource-url: {}", targetInstanceId, harvestResultNumber, resourceUrl);
        try {
            return getHeaders(targetInstanceId, harvestResultNumber, URLDecoder.decode(resourceUrl));
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return null;
    }

    @Override
    public List<Header> getHeaders(long targetInstanceId, int harvestResultNumber, String resourceUrl) throws DigitalAssetStoreException {
        return arcDigitalAssetStoreService.getHeaders(targetInstanceId, harvestResultNumber, resourceUrl);
    }

    @Override
    @PostMapping(path = DigitalAssetStorePaths.INITIATE_INDEXING)
    public void initiateIndexing(@RequestBody HarvestResultDTO harvestResult) throws DigitalAssetStoreException {
        log.debug("Initial indexing");
        arcDigitalAssetStoreService.initiateIndexing(harvestResult);
    }

    @Override
    @PostMapping(path = DigitalAssetStorePaths.INITIATE_REMOVE_INDEXES)
    public void initiateRemoveIndexes(@RequestBody HarvestResultDTO harvestResult) throws DigitalAssetStoreException {
        log.debug("Initial remove indexing");
        arcDigitalAssetStoreService.initiateRemoveIndexes(harvestResult);
    }

    @Override
    @PostMapping(path = DigitalAssetStorePaths.CHECK_INDEXING)
    public Boolean checkIndexing(@RequestParam(value = "harvest-result-oid") Long harvestResultOid) throws DigitalAssetStoreException {
        log.debug("Check indexing, harvestResultOid: {}", harvestResultOid);
        return arcDigitalAssetStoreService.checkIndexing(harvestResultOid);
    }

    @Override
    public void submitToArchive(String targetInstanceOid, String sip, Map xAttributes, int harvestNumber) throws DigitalAssetStoreException {
        log.debug("Submit to archive, target-instance-oid: {}, sip: {}, harvest-number: {}", targetInstanceOid, sip, harvestNumber);
        arcDigitalAssetStoreService.submitToArchive(targetInstanceOid, sip, xAttributes, harvestNumber);
    }

    @PostMapping(path = DigitalAssetStorePaths.ARCHIVE)
    public void submitToArchive(@PathVariable(value = "target-instance-oid") String targetInstanceOid,
                                @RequestParam(value = "harvest-number") int harvestNumber,
                                @RequestBody Map xAttributes) throws DigitalAssetStoreException {
        String sip = (String) xAttributes.get("sip");
        xAttributes.remove("sip");
        this.submitToArchive(targetInstanceOid, sip, xAttributes, harvestNumber);
    }

    @Override
    @PostMapping(path = DigitalAssetStorePaths.PURGE)
    public void purge(@RequestBody List<String> targetInstanceNames) throws DigitalAssetStoreException {
        log.debug("Purge target instances, targetInstanceNames: {}", Arrays.toString(targetInstanceNames.toArray()));
        arcDigitalAssetStoreService.purge(targetInstanceNames);
    }

    @Override
    @PostMapping(path = DigitalAssetStorePaths.PURGE_ABORTED_TARGET_INSTANCES)
    public void purgeAbortedTargetInstances(@RequestBody List<String> targetInstanceNames)
            throws DigitalAssetStoreException {
        log.debug("Purge aborted target instances, targetInstanceNames: {}", Arrays.toString(targetInstanceNames.toArray()));
        arcDigitalAssetStoreService.purgeAbortedTargetInstances(targetInstanceNames);
    }

    @Override
    @PostMapping(path = DigitalAssetStorePaths.CUSTOM_DEPOSIT_FORM_DETAILS)
    public CustomDepositFormResultDTO getCustomDepositFormDetails(@RequestBody CustomDepositFormCriteriaDTO criteria)
            throws DigitalAssetStoreException {
        log.debug("Get custom deposit form details");
        return arcDigitalAssetStoreService.getCustomDepositFormDetails(criteria);
    }

//    @RequestMapping(path = DigitalAssetStorePaths.SAVE, method = {RequestMethod.POST, RequestMethod.GET})
//    public void save(@RequestBody Object request) throws DigitalAssetStoreException {
//        if (request == null) {
//            throw new DigitalAssetStoreException("Request without a payload");
//        } else if (request instanceof List) {
//            try {
//                List<HarvestDTO> dtos = (List<HarvestDTO>) request;
//                save(dtos);
//            } catch (ClassCastException e) {
//                throw new DigitalAssetStoreException("Request with unrecognized payload", e);
//            }
//        } else if (request instanceof HarvestDTO) {
//            save((HarvestDTO) request);
//        } else {
//            throw new DigitalAssetStoreException("Request with unrecognized payload");
//        }
//    }

    @Override
    @RequestMapping(path = DigitalAssetStorePaths.SAVE, method = {RequestMethod.POST, RequestMethod.GET})
    public void save(@RequestBody List<HarvestDTO> dtos) throws DigitalAssetStoreException {
        arcDigitalAssetStoreService.save(dtos);
    }

//    @Override
//    public void save(@RequestBody HarvestDTO dto) throws DigitalAssetStoreException {
//        log.debug("Save harvest, {}", dto.toString());
//        arcDigitalAssetStoreService.save(dto);
//    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_APPLY_PRUNE_IMPORT, method = RequestMethod.POST)
    public ModifyResult initialPruneAndImport(@RequestBody ModifyApplyCommand cmd) {
        return arcDigitalAssetStoreService.initialPruneAndImport(cmd);
    }

    @Override
    @RequestMapping(path = DigitalAssetStorePaths.OPERATE_HARVEST_RESULT_MODIFICATION, method = RequestMethod.POST)
    public void operateHarvestResultModification(@RequestParam("stage") String stage, @RequestParam("command") String command, @RequestParam("targetInstanceId") long targetInstanceId, @RequestParam("harvestNumber") int harvestNumber) throws DigitalAssetStoreException {
        arcDigitalAssetStoreService.operateHarvestResultModification(stage, command, targetInstanceId, harvestNumber);
    }
}
