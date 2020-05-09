package org.webcurator.core.networkmap.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.rest.AbstractRestClient;

import java.net.URI;
import java.util.List;

public class PruneAndImportRemoteClient extends AbstractRestClient implements PruneAndImportService {
    public PruneAndImportRemoteClient(String scheme, String host, int port, RestTemplateBuilder restTemplateBuilder) {
        super(scheme, host, port, restTemplateBuilder);
    }

    @Override
    public PruneAndImportCommandRowMetadata uploadFile(long job, int harvestResultNumber, String fileName, boolean replaceFlag, byte[] doc) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(PruneAndImportServicePath.PATH_UPLOAD_FILE))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("fileName", fileName)
                .queryParam("replaceFlag", replaceFlag);
        URI uri = uriComponentsBuilder.build().toUri();

        HttpEntity<Object> request = new HttpEntity<>(doc);
        RestTemplate restTemplate = restTemplateBuilder.build();

        PruneAndImportCommandRowMetadata result;
        result = restTemplate.postForObject(uri, request, PruneAndImportCommandRowMetadata.class);
        return result;
    }

    @Override
    public PruneAndImportCommandRow downloadFile(long job, int harvestResultNumber, String fileName) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(PruneAndImportServicePath.PATH_DOWNLOAD_FILE))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("fileName", fileName);
        URI uri = uriComponentsBuilder.build().toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();

        PruneAndImportCommandRow result;
        result = restTemplate.getForObject(uri, PruneAndImportCommandRow.class);
        return result;
    }

    @Override
    public PruneAndImportCommandResult checkFiles(long job, int harvestResultNumber, List<PruneAndImportCommandRowMetadata> items) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(PruneAndImportServicePath.PATH_CHECK_FILES))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        HttpEntity<String> request = createHttpRequestEntity(items);
        RestTemplate restTemplate = restTemplateBuilder.build();

        PruneAndImportCommandResult result;
        result = restTemplate.postForObject(uri, request, PruneAndImportCommandResult.class);
        return result;
    }

    @Override
    public PruneAndImportCommandResult pruneAndImport(long job, int harvestResultNumber, int newHarvestResultNumber, List<PruneAndImportCommandRowMetadata> dataset) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(PruneAndImportServicePath.PATH_APPLY_PRUNE_IMPORT))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("newHarvestResultNumber", newHarvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        HttpEntity<String> request = createHttpRequestEntity(dataset);
        RestTemplate restTemplate = restTemplateBuilder.build();

        PruneAndImportCommandResult result;
        result = restTemplate.postForObject(uri, request, PruneAndImportCommandResult.class);
        return result;
    }
}
