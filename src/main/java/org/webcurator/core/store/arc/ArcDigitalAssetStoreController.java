package org.webcurator.core.store.arc;

import org.apache.commons.httpclient.Header;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.webcurator.core.archive.dps.DPSArchive;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.store.DigitalAssetStore;
import org.webcurator.core.store.DigitalAssetStorePaths;
import org.webcurator.core.util.ApplicationContextFactory;
import org.webcurator.domain.model.core.*;
import org.webcurator.domain.model.core.harvester.store.HarvestStoreCopyAndPruneDTO;
import org.webcurator.domain.model.core.harvester.store.HarvestStoreDTO;

import javax.servlet.http.HttpServletResponse;
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

    @CrossOrigin
    @PostMapping(path = "/digital-asset-store/rosettaInterface")
    @ResponseBody
    public String rosettaInterface(@RequestParam(value = "query") String query,
                                   @RequestParam(value = "producerAgent") String producerAgent,
                                   @RequestParam(value = "producerId") String producerId,
                                   @RequestParam(value = "producer", required = false) String producer,
                                   @RequestParam(value = "fromCache", required = false) String fromCache,
                                   @RequestParam(value = "targetDcType", required = false) String targetDcType,
                                   @RequestParam(value = "agentPassword", required = false) String agentPassword,
                                   @RequestParam(value = "milliSeconds", required = false) String milliseconds,
                                   HttpServletResponse response) throws Exception {

        String body = "";
        DPSArchive dpsArchive = ApplicationContextFactory.getApplicationContext().getBean(DPSArchive.class);
        response.addHeader("Cache-Control", "no-store");

        if ("getProducerName".equals(query)) {

            DPSArchive.DepData prod = dpsArchive.getProducer(producerAgent, producerId);

            if (prod == null) {
                body = "<b>Preset producer does not match available producers for the user " + producerAgent + " in Rosetta</b>";
            }
            else {
                body = "<b>" + prod.description + " (ID: " + prod.id + ")</b>" +
                        "<input title=\"Producer\" name=\"customDepositForm_producerId\" type=\"hidden\" value=\"" + prod.id + "\"/>";
            }
        } else if ("getProducers".equals(query)) {
            boolean fromCacheBool = Boolean.parseBoolean(fromCache);
            DPSArchive.DepData[] producers = dpsArchive.getProducer(producerAgent,fromCacheBool);
            if (producers == null) {
                body = "<b>No producers available in Rosetta for the user " + producerAgent + "</b>";
            } else {

                body = "<b>Select a producer for agent " + producerAgent + "</b>" +
                        "(<a title=\"Retrieves a fresh list of producers from Rosetta for the agent\" style=\"text-decoration:underline;\"" +
                        "href=\"#\" onClick=\"javascript: return getProducers(false);\">Retrieve the list from Rosetta</a>)" +
                        "<p></p>" +
                        "<select title=\"Producer\" name=\"customDepositForm_producerId\" size=\"10\" style=\"width:100%;\">";

                for (int i = 0; i < producers.length; i++) {
                    String prodId = producers[i].id;
                    body = body + "<option title=\"Producer-" + prodId + "\" value=\"" + prodId + "\">" + producers[i].description + " (ID:" +  prodId + ")</option>";
                }

                body = body + "</select>";
            }
        }  else if ("validateProducerAgent".equals(query)) {
            boolean status = dpsArchive.validateProducerAgentName(producerAgent);
            if (!status) {
                body = "Producer agent name " + producerAgent + " is invalid in Rosetta.";
            } else {
                status = dpsArchive.isLoginSuccessful(producerAgent, agentPassword);
                if (!status) {
                    body = "Unable to login.  Password may be invalid.";
                } else {
                    body = "Producer agent name and password are valid.";
                }
            }
        } else if ("validateMaterialFlowAssociation".equals(query)) {
            boolean status = dpsArchive.validateMaterialFlowAssociation(producer, targetDcType);
            if (!status) {
                body = "Producer is not associated with the right material flow.  Please correct this in Rosetta.";
            } else {
                body = "Producer is associated with the right material flow.";
            }
        } else if ("sleep".equals(query)) {
            try {
                long ms = Long.parseLong(milliseconds);
                Thread.sleep(ms);
                body = "Slept over successfully for " + milliseconds + " milliseconds";
            } catch (Exception ex) {
                body = "Sleep was interrupted by exception " + ex + ".";
            }
        }
        return body;
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
