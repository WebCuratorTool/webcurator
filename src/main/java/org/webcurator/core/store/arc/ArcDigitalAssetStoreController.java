package org.webcurator.core.store.arc;

import org.apache.commons.httpclient.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.store.DigitalAssetStore;
import org.webcurator.core.store.DigitalAssetStorePaths;
import org.webcurator.domain.model.core.ArcHarvestResultDTO;
import org.webcurator.domain.model.core.CustomDepositFormCriteriaDTO;
import org.webcurator.domain.model.core.CustomDepositFormResultDTO;
import org.webcurator.domain.model.core.HarvestResourceDTO;
import org.webcurator.domain.model.core.HarvestResultDTO;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ArcDigitalAssetStoreController implements DigitalAssetStore {

    @Autowired
    @Qualifier("arcDigitalAssetStoreService")
    private ArcDigitalAssetStoreService arcDigitalAssetStoreService;

    @Override
    @GetMapping(path = DigitalAssetStorePaths.RESOURCE)
    public Path getResource(@PathVariable(value = "target-instance-name") String targetInstanceName,
                            @RequestParam(value = "harvest-result-number") int harvestResultNumber,
                            @RequestParam(value = "resource") HarvestResourceDTO resource) throws DigitalAssetStoreException {
        return arcDigitalAssetStoreService.getResource(targetInstanceName, harvestResultNumber, resource);
    }

    @Override
    @GetMapping(path = DigitalAssetStorePaths.SMALL_RESOURCE)
    public byte[] getSmallResource(@PathVariable(value = "target-instance-name") String targetInstanceName,
                                   @RequestParam(value = "harvest-result-number") int harvestResultNumber, HarvestResourceDTO resource) throws DigitalAssetStoreException {
        return arcDigitalAssetStoreService.getSmallResource(targetInstanceName, harvestResultNumber, resource);
    }

    @Override
    @GetMapping(path = DigitalAssetStorePaths.HEADERS)
    public List<Header> getHeaders(@PathVariable(value = "target-instance-name") String targetInstanceName,
                                   @RequestParam(value = "harvest-result-number") int harvestResultNumber,
                                   @RequestParam(value = "resource") HarvestResourceDTO resource) throws DigitalAssetStoreException {
        return arcDigitalAssetStoreService.getHeaders(targetInstanceName, harvestResultNumber, resource);
    }

    @Override
    @PostMapping(path = DigitalAssetStorePaths.SAVE)
    public void save(@PathVariable(value = "target-instance-name") String targetInstanceName,
                     @RequestParam(value = "paths") List<Path> paths) throws DigitalAssetStoreException {
        arcDigitalAssetStoreService.save(targetInstanceName, paths);

    }

    @Override
    @PostMapping(path = DigitalAssetStorePaths.SAVE)
    public void save(@PathVariable(value = "target-instance-name") String targetInstanceName,
                     @RequestParam(value = "path") Path path) throws DigitalAssetStoreException {
        arcDigitalAssetStoreService.save(targetInstanceName, path);
    }

    @Override
    @PostMapping(path = DigitalAssetStorePaths.SAVE)
    public void save(@PathVariable(value = "target-instance-name") String targetInstanceName,
                     @RequestParam(value = "directory") String directory,
                     @RequestParam(value = "paths") List<Path> paths) throws DigitalAssetStoreException {
        arcDigitalAssetStoreService.save(targetInstanceName, directory, paths);
    }

    @Override
    @PostMapping(path = DigitalAssetStorePaths.SAVE)
    public void save(@PathVariable(value = "target-instance-name") String targetInstanceName,
                     @RequestParam(value = "directory") String directory,
                     @RequestParam(value = "path") Path path) throws DigitalAssetStoreException {
        arcDigitalAssetStoreService.save(targetInstanceName, directory, path);
    }

    @Override
    @PostMapping(path = DigitalAssetStorePaths.COPY_AND_PRUNE)
    public HarvestResultDTO copyAndPrune(@PathVariable(value = "target-instance-name") String targetInstanceName,
                                         @RequestParam(value = "original-harvest-result-number") int originalHarvestResultNumber,
                                         @RequestParam(value = "new-harvest-result-number") int newHarvestResultNumber,
                                         @RequestParam(value = "uris-to-delete") List<String> urisToDelete,
                                         @RequestParam(value = "harvest-resources-to-import") List<HarvestResourceDTO> harvestResourcesToImport) throws DigitalAssetStoreException {
        return arcDigitalAssetStoreService.copyAndPrune(targetInstanceName, originalHarvestResultNumber,
                newHarvestResultNumber, urisToDelete, harvestResourcesToImport);
    }

    @Override
    @PostMapping(path = DigitalAssetStorePaths.INITIATE_INDEXING)
    public void initiateIndexing(@RequestParam(value = "harvest-result") ArcHarvestResultDTO harvestResult) throws DigitalAssetStoreException {
        arcDigitalAssetStoreService.initiateIndexing(harvestResult);

    }

    @Override
    @PostMapping(path = DigitalAssetStorePaths.INITIATE_REMOVE_INDEXES)
    public void initiateRemoveIndexes(@RequestParam(value = "harvest-result") ArcHarvestResultDTO harvestResult) throws DigitalAssetStoreException {
        arcDigitalAssetStoreService.initiateRemoveIndexes(harvestResult);
    }

    @Override
    @PostMapping(path = DigitalAssetStorePaths.CHECK_INDEXING)
    public Boolean checkIndexing(@RequestParam(value = "harvest-result-oid") Long harvestResultOid) throws DigitalAssetStoreException {
        return arcDigitalAssetStoreService.checkIndexing(harvestResultOid);
    }

    @Override
    @PostMapping(path = DigitalAssetStorePaths.ARCHIVE)
    public void submitToArchive(@PathVariable(value = "target-instance-oid") String targetInstanceOid,
                                @RequestParam(value = "sip") String sip,
                                @RequestParam(value = "x-attributes") Map xAttributes,
                                @RequestParam(value = "harvest-number") int harvestNumber) throws DigitalAssetStoreException {
        arcDigitalAssetStoreService.submitToArchive(targetInstanceOid, sip, xAttributes, harvestNumber);
    }

    @Override
    @PostMapping(path = DigitalAssetStorePaths.PURGE)
    public void purge(@RequestParam(value = "target-instance-names") List<String> targetInstanceNames) throws DigitalAssetStoreException {
        arcDigitalAssetStoreService.purge(targetInstanceNames);
    }

    @Override
    @PostMapping(path = DigitalAssetStorePaths.PURGE_ABORTED_TARGET_INSTANCES)
    public void purgeAbortedTargetInstances(@RequestParam(value = "target-instance-names") List<String> targetInstanceNames)
            throws DigitalAssetStoreException {
        arcDigitalAssetStoreService.purgeAbortedTargetInstances(targetInstanceNames);
    }

    @Override
    @GetMapping(path = DigitalAssetStorePaths.GET_CUSTOM_DEPOSIT_FORM_DETAILS)
    public CustomDepositFormResultDTO getCustomDepositFormDetails(@RequestParam(value = "custom-deposit-form-criteria") CustomDepositFormCriteriaDTO criteria)
            throws DigitalAssetStoreException {
        return arcDigitalAssetStoreService.getCustomDepositFormDetails(criteria);
    }
}
