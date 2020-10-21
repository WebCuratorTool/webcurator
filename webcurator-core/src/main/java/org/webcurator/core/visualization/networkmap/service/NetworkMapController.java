package org.webcurator.core.visualization.networkmap.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.webcurator.core.visualization.VisualizationConstants;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;

import java.util.List;

@RestController
public class NetworkMapController implements NetworkMapService {
    @Autowired
    private NetworkMapClient client;

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_INITIAL_INDEX, method = {RequestMethod.POST}, produces = "application/json")
    public NetworkMapResult initialIndex(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber) {
        NetworkMapResult result = null;
        try {
            result = client.initialIndex(job, harvestResultNumber);
        } catch (Throwable e) {
            result = NetworkMapResult.getSystemError();
        }
        return result;
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_GET_DB_VERSION, method = {RequestMethod.POST}, produces = "application/json")
    public NetworkMapResult getDbVersion(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber) {
        NetworkMapResult result = null;
        try {
            result = client.getDbVersion(job, harvestResultNumber);
        } catch (Throwable e) {
            result = NetworkMapResult.getSystemError();
        }
        return result;
    }

//    @Override
//    @RequestMapping(path = VisualizationConstants.PATH_GET_COMMON, method = {RequestMethod.POST}, produces = "application/json")
//    public NetworkMapResult get(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("key") String key) {
//        return client.get(job, harvestResultNumber, key);
//    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_GET_NODE, method = {RequestMethod.POST}, produces = "application/json")
    public NetworkMapResult getNode(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("id") long id) {
        return client.getNode(job, harvestResultNumber, id);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_GET_OUTLINKS, method = {RequestMethod.POST}, produces = "application/json")
    public NetworkMapResult getOutlinks(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("id") long id) {
        return client.getOutlinks(job, harvestResultNumber, id);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_GET_URLS_CASCADED_BY_PATH, method = {RequestMethod.POST}, produces = "application/json")
    public NetworkMapResult searchUrl2CascadePaths(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("title") String title, @RequestBody NetworkMapServiceSearchCommand searchCommand) {
        return client.searchUrl2CascadePaths(job, harvestResultNumber, title, searchCommand);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_GET_CHILDREN, method = {RequestMethod.POST}, produces = "application/json")
    public NetworkMapResult getChildren(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("id") long id) {
        return client.getChildren(job, harvestResultNumber, id);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_GET_ALL_DOMAINS, method = {RequestMethod.POST}, produces = "application/json")
    public NetworkMapResult getAllDomains(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber) {
        return client.getAllDomains(job, harvestResultNumber);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_GET_ROOT_URLS, method = {RequestMethod.POST}, produces = "application/json")
    public NetworkMapResult getSeedUrls(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber) {
        return client.getSeedUrls(job, harvestResultNumber);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_GET_MALFORMED_URLS, method = {RequestMethod.POST}, produces = "application/json")
    public NetworkMapResult getMalformedUrls(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber) {
        return client.getMalformedUrls(job, harvestResultNumber);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_SEARCH_URLS, method = {RequestMethod.POST}, produces = "application/json")
    public NetworkMapResult searchUrl(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestBody NetworkMapServiceSearchCommand searchCommand) {
        return client.searchUrl(job, harvestResultNumber, searchCommand);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_SEARCH_URL_NAMES, method = {RequestMethod.POST}, produces = "application/json")
    public NetworkMapResult searchUrlNames(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("substring") String substring) {
        return null;
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_GET_HOP_PATH, method = {RequestMethod.POST}, produces = "application/json")
    public NetworkMapResult getHopPath(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("id") long id) {
        return client.getHopPath(job, harvestResultNumber, id);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_GET_HIERARCHY_URLS, method = {RequestMethod.POST}, produces = "application/json")
    public NetworkMapResult getHierarchy(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestBody List<Long> ids) {
        return client.getHierarchy(job, harvestResultNumber, ids);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_GET_URL_BY_NAME, method = {RequestMethod.POST}, produces = "application/json")
    public NetworkMapResult getUrlByName(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("urlName") String urlName) {
        return client.getUrlByName(job, harvestResultNumber, urlName);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_GET_URLS_BY_NAMES, method = {RequestMethod.POST}, produces = "application/json")
    public NetworkMapResult getUrlsByNames(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestBody List<String> urlNameList) {
        return client.getUrlsByNames(job, harvestResultNumber, urlNameList);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_GET_PROGRESS, method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json")
    public NetworkMapResult getProgress(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber) {
        return client.getProgress(job, harvestResultNumber);
    }

    @Override
    @RequestMapping(path = VisualizationConstants.PATH_GET_PROCESSING_HARVEST_RESULT, method = {RequestMethod.POST}, produces = "application/json")
    public NetworkMapResult getProcessingHarvestResultDTO(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber) {
        return client.getProcessingHarvestResultDTO(job, harvestResultNumber);
    }
}
