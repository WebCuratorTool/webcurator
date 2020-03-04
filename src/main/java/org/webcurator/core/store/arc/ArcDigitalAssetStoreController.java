package org.webcurator.core.store.arc;

import org.apache.commons.httpclient.Header;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.store.DigitalAssetStore;
import org.webcurator.core.store.DigitalAssetStorePaths;
import org.webcurator.domain.model.core.*;
import org.webcurator.domain.model.core.harvester.store.HarvestStoreCopyAndPruneDTO;
import org.webcurator.domain.model.core.harvester.store.HarvestStoreDTO;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ArcDigitalAssetStoreController implements DigitalAssetStore {
    Logger log= LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("arcDigitalAssetStoreService")
    private ArcDigitalAssetStoreService arcDigitalAssetStoreService;

    @PostMapping(path = DigitalAssetStorePaths.RESOURCE, produces = "application/octet-stream")
    public @ResponseBody byte[] getResourceExternal(@PathVariable(value = "target-instance-name") String targetInstanceName,
                            @RequestParam(value = "harvest-result-number") int harvestResultNumber,
                            @RequestBody ArcHarvestResourceDTO resource) throws DigitalAssetStoreException {
        log.debug("Get resource, target-instance-name: {}, harvest-result-number: {}", targetInstanceName, harvestResultNumber);
        Path path =  getResource(targetInstanceName, harvestResultNumber, resource);
        byte[] buf = null;
        try {
            buf = IOUtils.toByteArray(path.toUri());
        }catch (IOException e){
            log.error(e.getMessage());
        }
        return buf;
    }

    @Override
    public Path getResource(String targetInstanceName, int harvestResultNumber, HarvestResourceDTO resource) throws DigitalAssetStoreException {
        return arcDigitalAssetStoreService.getResource(targetInstanceName, harvestResultNumber, resource);
    }

    @PostMapping(path = DigitalAssetStorePaths.SMALL_RESOURCE)
    public byte[] getSmallResourceExternal(@PathVariable(value = "target-instance-name") String targetInstanceName,
                                   @RequestParam(value = "harvest-result-number") int harvestResultNumber,
                                   @RequestBody ArcHarvestResourceDTO resource) throws DigitalAssetStoreException {
        log.debug("Get small resource, target-instance-name: {}, harvest-result-number: {}", targetInstanceName, harvestResultNumber);
        return getSmallResource(targetInstanceName, harvestResultNumber, resource);
    }

    @Override
    public byte[] getSmallResource(String targetInstanceName, int harvestResultNumber, HarvestResourceDTO resource) throws DigitalAssetStoreException {
        return arcDigitalAssetStoreService.getSmallResource(targetInstanceName, harvestResultNumber, resource);
    }

    @PostMapping(path = DigitalAssetStorePaths.HEADERS)
    public List<Header> getHeadersExternal(@PathVariable(value = "target-instance-name") String targetInstanceName,
                                   @RequestParam(value = "harvest-result-number") int harvestResultNumber,
                                   @RequestBody ArcHarvestResourceDTO resource) throws DigitalAssetStoreException {
        log.debug("Get headers, target-instance-name: {}, harvest-result-number: {}", targetInstanceName, harvestResultNumber);
        return getHeaders(targetInstanceName, harvestResultNumber, resource);
    }

    @Override
    public List<Header> getHeaders(String targetInstanceName, int harvestResultNumber, HarvestResourceDTO resource) throws DigitalAssetStoreException {
        return arcDigitalAssetStoreService.getHeaders(targetInstanceName, harvestResultNumber, resource);
    }

    @PostMapping(path = DigitalAssetStorePaths.COPY_AND_PRUNE)
    public HarvestResultDTO copyAndPruneExternal(@PathVariable(value = "target-instance-name") String targetInstanceName,
                                         @RequestParam(value = "original-harvest-result-number") int originalHarvestResultNumber,
                                         @RequestParam(value = "new-harvest-result-number") int newHarvestResultNumber,
                                         @RequestBody HarvestStoreCopyAndPruneDTO dto) throws DigitalAssetStoreException {
        log.debug("Copy and prune, target-instance-name: {}, original-harvest-result-number: {}, new-harvest-result-number: {}",targetInstanceName, originalHarvestResultNumber, newHarvestResultNumber);
        List<HarvestResourceDTO> harvestStoreDTOS=dto.getHarvestResourcesToImport().stream().map(hsDto->{return (HarvestResourceDTO)hsDto;}).collect(Collectors.toList());
        return copyAndPrune(targetInstanceName, originalHarvestResultNumber, newHarvestResultNumber, dto.getUrisToDelete(), harvestStoreDTOS);
    }

    @Override
    public HarvestResultDTO copyAndPrune(String targetInstanceName, int originalHarvestResultNumber, int newHarvestResultNumber, List<String> urisToDelete, List<HarvestResourceDTO> harvestResourcesToImport) throws DigitalAssetStoreException {
        return arcDigitalAssetStoreService.copyAndPrune(targetInstanceName, originalHarvestResultNumber, newHarvestResultNumber, urisToDelete, harvestResourcesToImport);
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
        String sip = (String)xAttributes.get("sip");
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

    @RequestMapping(path = DigitalAssetStorePaths.SAVE, method = {RequestMethod.POST, RequestMethod.GET})
    public void saveExternal(@PathVariable(value = "target-instance-name") String targetInstanceName,
                     @RequestBody HarvestStoreDTO requestBody) throws DigitalAssetStoreException{
        log.debug("Save harvest, target-instance-name: {}", targetInstanceName);
        if(requestBody.getDirectory()==null){
            if(requestBody.getPathFromPath()!=null){
                save(targetInstanceName, requestBody.getPathFromPath());
            }else if(requestBody.getPathsFromPath()!=null){
                save(targetInstanceName, requestBody.getPathsFromPath());
            }
        }else{
            if(requestBody.getPathFromPath()!=null){
                save(targetInstanceName,requestBody.getDirectory(), requestBody.getPathFromPath());
            }else if(requestBody.getPathsFromPath()!=null){
                save(targetInstanceName,requestBody.getDirectory(), requestBody.getPathsFromPath());
            }
        }
    }

    @Override
    public void save( String targetInstanceName, List<Path> paths) throws DigitalAssetStoreException {
        log.debug("Save, targetInstanceName: {}, paths: {}", targetInstanceName, Arrays.toString(paths.toArray()));
        arcDigitalAssetStoreService.save(targetInstanceName, paths);
    }

    @Override
    public void save(String targetInstanceName, Path path) throws DigitalAssetStoreException {
        log.debug("Save, targetInstanceName: {}, path: {}", targetInstanceName, path.toFile().getAbsolutePath());
        arcDigitalAssetStoreService.save(targetInstanceName, path);
    }

    @Override
    public void save(String targetInstanceName, String directory, List<Path> paths) throws DigitalAssetStoreException {
        log.debug("Save, targetInstanceName: {}, directory: {}, paths: {}", targetInstanceName,directory, Arrays.toString(paths.toArray()));
        arcDigitalAssetStoreService.save(targetInstanceName, directory, paths);
    }

    @Override
    public void save(String targetInstanceName, String directory, Path path) throws DigitalAssetStoreException {
        log.debug("Save, targetInstanceName: {}, directory: {}, path: {}", targetInstanceName, directory, path.toFile().getAbsolutePath());
        arcDigitalAssetStoreService.save(targetInstanceName, directory, path);
    }


}
