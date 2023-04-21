package org.webcurator.core.visualization.browser;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Bytes;
import org.apache.commons.httpclient.Header;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.core.store.DigitalAssetStorePaths;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

public class VisWayBackClientRemote extends AbstractRestClient implements VisWayBackClient {
    public VisWayBackClientRemote(String baseUrl, RestTemplateBuilder restTemplateBuilder) {
        super(baseUrl, restTemplateBuilder);
    }

    @Override
    public Path getResource(long targetInstanceId, int harvestResultNumber, String resourceUrl) throws DigitalAssetStoreException {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.RESOURCE))
                .queryParam("harvest-result-number", harvestResultNumber)
                .queryParam("resource-url", URLEncoder.encode(resourceUrl));
        Map<String, Long> pathVariables = ImmutableMap.of("target-instance-id", targetInstanceId);
        try {
            URL url = uriComponentsBuilder.buildAndExpand(pathVariables).toUri().toURL();
            URLConnection connection = url.openConnection();
            File file = File.createTempFile("wctd", "tmp");
            Files.copy(connection.getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return file.toPath();
        } catch (IOException ex) {
            throw new DigitalAssetStoreException("Failed to get resource for " + targetInstanceId + " " + harvestResultNumber + ": " + ex.getMessage(), ex);
        }
    }

    @Override
    public byte[] getSmallResource(long targetInstanceId, int harvestResultNumber, String resourceUrl) throws DigitalAssetStoreException {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.SMALL_RESOURCE))
                .queryParam("harvest-result-number", harvestResultNumber)
                .queryParam("resource-url", URLEncoder.encode(resourceUrl));
        Map<String, Long> pathVariables = ImmutableMap.of("target-instance-id", targetInstanceId);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        RestTemplate restTemplate = restTemplateBuilder.build();
        ResponseEntity<List<Byte>> listResponse = restTemplate.exchange(
                uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                HttpMethod.POST, null, new ParameterizedTypeReference<List<Byte>>() {
                });

        return Bytes.toArray(listResponse.getBody());
    }

    @Override
    public List<Header> getHeaders(long targetInstanceId, int harvestResultNumber, String resourceUrl) throws DigitalAssetStoreException {
        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.HEADERS))
                .queryParam("harvest-result-number", harvestResultNumber)
                .queryParam("resource-url", URLEncoder.encode(resourceUrl));
        Map<String, Long> pathVariables = ImmutableMap.of("target-instance-id", targetInstanceId);
        URI uri = uriComponentsBuilder.buildAndExpand(pathVariables).toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();
        ResponseEntity<List<Header>> listResponse = restTemplate.exchange(uri, HttpMethod.POST, null,
                new ParameterizedTypeReference<List<Header>>() {
                });
        return listResponse.getBody();
    }
}
