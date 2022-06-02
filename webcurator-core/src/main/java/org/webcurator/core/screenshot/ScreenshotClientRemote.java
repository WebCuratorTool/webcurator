package org.webcurator.core.screenshot;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.core.store.DigitalAssetStorePaths;

public class ScreenshotClientRemote extends AbstractRestClient implements ScreenshotClient {
    public ScreenshotClientRemote(String baseUrl, RestTemplateBuilder restTemplateBuilder) {
        super(baseUrl, restTemplateBuilder);
    }

    @Override
    public Boolean createScreenshots(ScreenshotIdentifierCommand identifiers) throws DigitalAssetStoreException {
        HttpEntity<String> request = this.createHttpRequestEntity(identifiers);
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.CREATE_SCREENSHOT));
        RestTemplate restTemplate = restTemplateBuilder.build();

        return restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                request, Boolean.class);
    }
}
