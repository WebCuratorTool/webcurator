package org.webcurator.core.visualization.networkmap.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NetworkMapController implements NetworkMapService {
    @Autowired
    private NetworkMapClient client;

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_COMMON, method = {RequestMethod.POST, RequestMethod.GET})
    public String get(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("key") String key) {
        return client.get(job, harvestResultNumber, key);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_NODE, method = {RequestMethod.POST, RequestMethod.GET})
    public String getNode(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("id") long id) {
        return client.getNode(job, harvestResultNumber, id);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_OUTLINKS, method = {RequestMethod.POST, RequestMethod.GET})
    public String getOutlinks(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("id") long id) {
        return client.getOutlinks(job, harvestResultNumber, id);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_CHILDREN, method = {RequestMethod.POST, RequestMethod.GET})
    public String getChildren(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("id") long id) {
        return client.getChildren(job, harvestResultNumber, id);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_ALL_DOMAINS, method = {RequestMethod.POST, RequestMethod.GET})
    public String getAllDomains(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber) {
        return client.getAllDomains(job, harvestResultNumber);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_ROOT_URLS, method = {RequestMethod.POST, RequestMethod.GET})
    public String getSeedUrls(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber) {
        return client.getSeedUrls(job, harvestResultNumber);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_MALFORMED_URLS, method = {RequestMethod.POST, RequestMethod.GET})
    public String getMalformedUrls(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber) {
        return client.getMalformedUrls(job, harvestResultNumber);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_SEARCH_URLS, method = {RequestMethod.POST, RequestMethod.GET})
    public String searchUrl(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestBody NetworkMapServiceSearchCommand searchCommand) {
        return client.searchUrl(job, harvestResultNumber, searchCommand);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_HOP_PATH, method = {RequestMethod.POST, RequestMethod.GET})
    public String getHopPath(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("id") long id) {
        return client.getHopPath(job, harvestResultNumber, id);
    }

    @Override
    @RequestMapping(path = NetworkMapServicePath.PATH_GET_HIERARCHY_URLS, method = {RequestMethod.POST, RequestMethod.GET})
    public String getHierarchy(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestBody List<Long> ids) {
        return client.getHierarchy(job, harvestResultNumber, ids);
    }
}
