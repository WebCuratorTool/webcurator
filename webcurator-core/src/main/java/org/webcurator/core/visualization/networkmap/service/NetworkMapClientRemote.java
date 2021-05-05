package org.webcurator.core.visualization.networkmap.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.core.visualization.VisualizationConstants;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapUrl;

import java.net.URI;
import java.util.List;

public class NetworkMapClientRemote extends AbstractRestClient implements NetworkMapClient {
    public NetworkMapClientRemote(String baseUrl, RestTemplateBuilder restTemplateBuilder) {
        super(baseUrl, restTemplateBuilder);
    }

    @Override
    public NetworkMapResult initialIndex(long job, int harvestResultNumber) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_INITIAL_INDEX))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        NetworkMapResult result;
        result = restTemplate.postForObject(uri, null, NetworkMapResult.class);
        return result;
    }

    @Override
    public NetworkMapResult getDbVersion(long job, int harvestResultNumber) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_DB_VERSION))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        NetworkMapResult result;
        result = restTemplate.postForObject(uri, null, NetworkMapResult.class);
        return result;
    }

//    @Override
//    public NetworkMapResult get(long job, int harvestResultNumber, String key) {
//        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_COMMON))
//                .queryParam("job", job)
//                .queryParam("harvestResultNumber", harvestResultNumber)
//                .queryParam("key", key);
//        URI uri = uriComponentsBuilder.build().toUri();
//
//        RestTemplate restTemplate = restTemplateBuilder.build();
//
//        NetworkMapResult result;
//        result = restTemplate.postForObject(uri, null, NetworkMapResult.class);
//        return result;
//    }

    @Override
    public NetworkMapResult searchUrl2CascadePaths(long job, int harvestResultNumber, String title, NetworkMapServiceSearchCommand searchCommand) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_URLS_CASCADED_BY_PATH))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("title", title);
        URI uri = uriComponentsBuilder.build().toUri();

        HttpEntity<String> request = createHttpRequestEntity(searchCommand);
        RestTemplate restTemplate = restTemplateBuilder.build();

        NetworkMapResult result;
        result = restTemplate.postForObject(uri, request, NetworkMapResult.class);
        return result;
    }

    @Override
    public NetworkMapResult getNode(long job, int harvestResultNumber, long id) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_NODE))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("id", id);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        NetworkMapResult result;
        result = restTemplate.postForObject(uri, null, NetworkMapResult.class);
        return result;
    }

    @Override
    public NetworkMapResult getOutlinks(long job, int harvestResultNumber, long id) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_OUTLINKS))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("id", id);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        NetworkMapResult result;
        result = restTemplate.postForObject(uri, null, NetworkMapResult.class);
        return result;
    }

    @Override
    public NetworkMapResult getChildren(long job, int harvestResultNumber, long id) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_CHILDREN))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("id", id);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        NetworkMapResult result;
        result = restTemplate.postForObject(uri, null, NetworkMapResult.class);
        return result;
    }

    @Override
    public NetworkMapResult getAllDomains(long job, int harvestResultNumber) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_ALL_DOMAINS))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        NetworkMapResult result;
        result = restTemplate.postForObject(uri, null, NetworkMapResult.class);
        return result;
    }

    @Override
    public NetworkMapResult getSeedUrls(long job, int harvestResultNumber) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_ROOT_URLS))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        NetworkMapResult result;
        result = restTemplate.postForObject(uri, null, NetworkMapResult.class);
        return result;
    }

    @Override
    public NetworkMapResult getMalformedUrls(long job, int harvestResultNumber) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_MALFORMED_URLS))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        NetworkMapResult result;
        result = restTemplate.postForObject(uri, null, NetworkMapResult.class);
        return result;
    }

    @Override
    public NetworkMapResult searchUrl(long job, int harvestResultNumber, NetworkMapServiceSearchCommand searchCommand) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_SEARCH_URLS))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        HttpEntity<String> request = createHttpRequestEntity(searchCommand);
        RestTemplate restTemplate = restTemplateBuilder.build();

        NetworkMapResult result;
        result = restTemplate.postForObject(uri, request, NetworkMapResult.class);
        return result;
    }

    @Override
    public NetworkMapResult searchUrlNames(long job, int harvestResultNumber, String substring) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_SEARCH_URL_NAMES))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("substring", substring);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

//        ResponseEntity<String> json = restTemplate.postForEntity(uri, null, String.class);
//        return getArrayListOfNetworkMapNode(json.getBody());

        NetworkMapResult result;
        result = restTemplate.postForObject(uri, null, NetworkMapResult.class);
        return result;
    }

    @Override
    public NetworkMapResult getHopPath(long job, int harvestResultNumber, long id) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_HOP_PATH))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("id", id);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        NetworkMapResult result;
        result = restTemplate.postForObject(uri, null, NetworkMapResult.class);
        return result;
    }

    @Override
    public NetworkMapResult getHierarchy(long job, int harvestResultNumber, List<Long> ids) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_HIERARCHY_URLS))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        HttpEntity<String> request = createHttpRequestEntity(ids);
        RestTemplate restTemplate = restTemplateBuilder.build();

        NetworkMapResult result;
        result = restTemplate.postForObject(uri, request, NetworkMapResult.class);
        return result;
    }

    @Override
    public NetworkMapResult getUrlByName(long job, int harvestResultNumber, NetworkMapUrl url) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_URL_BY_NAME))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        NetworkMapResult result;
        result = restTemplate.postForObject(uri, this.createHttpRequestEntity(url), NetworkMapResult.class);
        return result;
    }

    @Override
    public NetworkMapResult getUrlsByNames(long job, int harvestResultNumber, List<String> urlNameList) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_URLS_BY_NAMES))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();
        HttpEntity<String> request = createHttpRequestEntity(urlNameList);
        RestTemplate restTemplate = restTemplateBuilder.build();

        NetworkMapResult result;
        result = restTemplate.postForObject(uri, request, NetworkMapResult.class);
        return result;
    }

    @Override
    public NetworkMapResult getProgress(long job, int harvestResultNumber) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_PROGRESS))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        NetworkMapResult result;
        result = restTemplate.postForObject(uri, null, NetworkMapResult.class);
        return result;
    }

    @Override
    public NetworkMapResult getProcessingHarvestResultDTO(long job, int harvestResultNumber) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_PROCESSING_HARVEST_RESULT))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        NetworkMapResult result;
        result = restTemplate.postForObject(uri, null, NetworkMapResult.class);
        return result;
    }
}
