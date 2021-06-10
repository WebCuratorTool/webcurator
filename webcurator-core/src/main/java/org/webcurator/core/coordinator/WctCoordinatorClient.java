package org.webcurator.core.coordinator;

import com.google.common.collect.ImmutableMap;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.core.util.WctUtils;
import org.webcurator.domain.model.core.HarvestResultDTO;
import org.webcurator.domain.model.core.SeedHistoryDTO;
import org.webcurator.domain.model.dto.SeedHistorySetDTO;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class WctCoordinatorClient extends AbstractRestClient {
    public WctCoordinatorClient(String baseUrl, RestTemplateBuilder restTemplateBuilder) {
        super(baseUrl, restTemplateBuilder);
    }

    public void completeArchiving(Long targetInstanceOid, String archiveIID) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(WctCoordinatorPaths.COMPLETE_ARCHIVING))
                .queryParam("archive-id", archiveIID);

        Map<String, Long> pathVariables = ImmutableMap.of("target-instance-oid", targetInstanceOid);
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Void.class);
    }

    public void failedArchiving(Long targetInstanceOid, String message) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(WctCoordinatorPaths.FAILED_ARCHIVING))
                .queryParam("message", message);

        Map<String, Long> pathVariables = ImmutableMap.of("target-instance-oid", targetInstanceOid);
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Void.class);
    }

    public void notifyModificationComplete(long targetInstanceId, int harvestResultNumber) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(WctCoordinatorPaths.MODIFICATION_COMPLETE_PRUNE_IMPORT))
                .queryParam("targetInstanceOid", targetInstanceId)
                .queryParam("harvestNumber", harvestResultNumber);
        RestTemplate restTemplate = restTemplateBuilder.build();
        URI uri = uriComponentsBuilder.build().toUri();
        restTemplate.getForObject(uri, Void.class);
    }

    public Set<SeedHistoryDTO> getSeedUrls(long targetInstanceId, int harvestResultNumber) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(WctCoordinatorPaths.TARGET_INSTANCE_HISTORY_SEED))
                .queryParam("targetInstanceOid", targetInstanceId)
                .queryParam("harvestNumber", harvestResultNumber);
        RestTemplate restTemplate = restTemplateBuilder.build();
        URI uri = uriComponentsBuilder.build().toUri();
        ResponseEntity<SeedHistorySetDTO> seedHistorySetDTO = restTemplate.getForEntity(uri, SeedHistorySetDTO.class);
        return Objects.requireNonNull(seedHistorySetDTO.getBody()).getSeeds();
    }

    public void dasHeartBeat(List<HarvestResultDTO> list) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(WctCoordinatorPaths.DIGITAL_ASSET_STORE_HEARTBEAT));
        HttpEntity<String> entity = this.createHttpRequestEntity(list);
        RestTemplate restTemplate = restTemplateBuilder.build();
        URI uri = uriComponentsBuilder.build().toUri();
        restTemplate.postForObject(uri, entity, Void.class);
    }

    public File getDownloadFileURL(long job, int harvestResultNumber, String fileName, File downloadedFile) throws IOException {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(WctCoordinatorPaths.MODIFICATION_DOWNLOAD_IMPORTED_FILE))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("fileName", fileName);
        URI uri = uriComponentsBuilder.build().toUri();

        URL url = uri.toURL();
        URLConnection conn = url.openConnection();

        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadedFile));
        InputStream inputStream = conn.getInputStream();
        WctUtils.copy(inputStream, outputStream);

        return downloadedFile;
    }

    public void finaliseIndex(long targetInstanceId, int harvestNumber) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(WctCoordinatorPaths.FINALISE_INDEX))
                .queryParam("targetInstanceId", targetInstanceId)
                .queryParam("harvestNumber", harvestNumber);
        RestTemplate restTemplate = restTemplateBuilder.build();
        URI uri = uriComponentsBuilder.build().toUri();
        restTemplate.postForObject(uri, null, Void.class);
    }

    public void dasUpdateHarvestResultStatus(HarvestResultDTO hrDTO) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(WctCoordinatorPaths.DIGITAL_ASSET_STORE_UPDATE_HR_STATUS));
        HttpEntity<String> entity = this.createHttpRequestEntity(hrDTO);
        RestTemplate restTemplate = restTemplateBuilder.build();
        URI uri = uriComponentsBuilder.build().toUri();
        restTemplate.postForObject(uri, entity, Void.class);
    }
}
