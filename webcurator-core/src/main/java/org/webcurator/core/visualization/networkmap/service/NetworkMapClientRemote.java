package org.webcurator.core.visualization.networkmap.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.core.visualization.VisualizationConstants;

import java.net.URI;
import java.util.List;

public class NetworkMapClientRemote extends AbstractRestClient implements NetworkMapClient {
    public NetworkMapClientRemote(String scheme, String host, int port, RestTemplateBuilder restTemplateBuilder) {
        super(scheme, host, port, restTemplateBuilder);
    }

    @Override
    public String get(long job, int harvestResultNumber, String key) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_COMMON))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("key", key);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.postForObject(uri, null, String.class);
        return result;
    }

    @Override
    public String getNode(long job, int harvestResultNumber, long id) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_NODE))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("id", id);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.postForObject(uri, null, String.class);
        return result;
    }

    @Override
    public String getOutlinks(long job, int harvestResultNumber, long id) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_OUTLINKS))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("id", id);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.postForObject(uri, null, String.class);
        return result;
    }

    @Override
    public String getChildren(long job, int harvestResultNumber, long id) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_CHILDREN))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("id", id);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.postForObject(uri, null, String.class);
        return result;
    }

    @Override
    public String getAllDomains(long job, int harvestResultNumber) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_ALL_DOMAINS))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.postForObject(uri, null, String.class);
        return result;
    }

    @Override
    public String getSeedUrls(long job, int harvestResultNumber) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_ROOT_URLS))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.postForObject(uri, null, String.class);
        return result;
    }

    @Override
    public String getMalformedUrls(long job, int harvestResultNumber) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_MALFORMED_URLS))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.postForObject(uri, null, String.class);
        return result;
    }

    @Override
    public String searchUrl(long job, int harvestResultNumber, NetworkMapServiceSearchCommand searchCommand) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_SEARCH_URLS))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        HttpEntity<String> request = createHttpRequestEntity(searchCommand);
        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.postForObject(uri, request, String.class);
        return result;
    }

    @Override
    public List<String> searchUrlNames(long job, int harvestResultNumber, String substring) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_SEARCH_URL_NAMES))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("substring", substring);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        ResponseEntity<String> json = restTemplate.postForEntity(uri, null, String.class);

        return getArrayListOfNetworkMapNode(json.getBody());
    }

    @Override
    public String getHopPath(long job, int harvestResultNumber, long id) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_HOP_PATH))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("id", id);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.postForObject(uri, null, String.class);
        return result;
    }

    @Override
    public String getHierarchy(long job, int harvestResultNumber, List<Long> ids) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_HIERARCHY_URLS))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        HttpEntity<String> request = createHttpRequestEntity(ids);
        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.postForObject(uri, request, String.class);
        return result;
    }

    @Override
    public String getUrlByName(long job, int harvestResultNumber, String urlName) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(VisualizationConstants.PATH_GET_URL_BY_NAME))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("urlName", urlName);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        String result;
        result = restTemplate.postForObject(uri, null, String.class);
        return result;
    }
}
