package org.webcurator.core.store;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Bytes;
import org.apache.commons.httpclient.Header;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandResult;
import org.webcurator.domain.model.core.*;
import org.webcurator.domain.model.core.harvester.store.HarvestStoreDTO;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
// TODO Note that the spring boot application needs @EnableRetry for the @Retryable to work.
public class DigitalAssetStoreClient extends AbstractRestClient implements DigitalAssetStore {
    public DigitalAssetStoreClient(String scheme, String host, int port, RestTemplateBuilder restTemplateBuilder) {
        super(scheme, host, port, restTemplateBuilder);
    }

    private void internalSave(String targetInstanceName, HarvestStoreDTO harvestStoreDTO) throws DigitalAssetStoreException {
        try {
            HttpEntity<String> request = this.createHttpRequestEntity(harvestStoreDTO);

            Map<String, String> pathVariables = ImmutableMap.of("target-instance-name", targetInstanceName);
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.SAVE));
            URI uri = uriComponentsBuilder.buildAndExpand(pathVariables).toUri();

            RestTemplate restTemplate = restTemplateBuilder.build();
            restTemplate.postForObject(uri, request, Void.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new DigitalAssetStoreException(e);
        }
    }

    @Override
    public void save(String targetInstanceName, String directory, Path path) throws DigitalAssetStoreException {
        HarvestStoreDTO requestBody = new HarvestStoreDTO();
        requestBody.setDirectory(directory);
        requestBody.setPathFromPath(path);

        internalSave(targetInstanceName, requestBody);
    }

    @Override
    // Assuming that without any values set will keep infinitely retrying
    @Retryable(maxAttempts = Integer.MAX_VALUE, backoff = @Backoff(delay = 30_000L))
    public void save(String targetInstanceName, Path path) throws DigitalAssetStoreException {
        HarvestStoreDTO requestBody = new HarvestStoreDTO();
        requestBody.setPathFromPath(path);

        internalSave(targetInstanceName, requestBody);
    }

    @Override
    public void save(String targetInstanceName, String directory, List<Path> paths) throws DigitalAssetStoreException {
        HarvestStoreDTO requestBody = new HarvestStoreDTO();
        requestBody.setDirectory(directory);
        requestBody.setPathsFromPath(paths);

        internalSave(targetInstanceName, requestBody);
    }

    @Override
    public void save(String targetInstanceName, List<Path> paths) throws DigitalAssetStoreException {
        HarvestStoreDTO requestBody = new HarvestStoreDTO();
        requestBody.setPathsFromPath(paths);

        internalSave(targetInstanceName, requestBody);
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
    public List<Header> getHeaders(long targetInstanceId, int harvestResultNumber, String resourceUrl)
            throws DigitalAssetStoreException {
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

    /**
     * @see DigitalAssetStore#purge(List<String>).
     */
    public void purge(List<String> targetInstanceNames) throws DigitalAssetStoreException {
        HttpEntity<String> request = this.createHttpRequestEntity(targetInstanceNames);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.PURGE));

        RestTemplate restTemplate = restTemplateBuilder.build();
        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                request, Boolean.class);
    }

    /**
     * @see DigitalAssetStore#purgeAbortedTargetInstances(List<String>).
     */
    public void purgeAbortedTargetInstances(List<String> targetInstanceNames) throws DigitalAssetStoreException {
        HttpEntity<String> request = this.createHttpRequestEntity(targetInstanceNames);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.PURGE_ABORTED_TARGET_INSTANCES));

        RestTemplate restTemplate = restTemplateBuilder.build();
        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                request, Boolean.class);
    }

    public void submitToArchive(String targetInstanceOid, String sip, Map xAttributes, int harvestNumber)
            throws DigitalAssetStoreException {
        xAttributes.put("sip", sip);
        HttpEntity<String> request = this.createHttpRequestEntity(xAttributes);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.ARCHIVE))
                .queryParam("harvest-number", harvestNumber);
        Map<String, String> pathVariables = ImmutableMap.of("target-instance-oid", targetInstanceOid);

        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                request, Boolean.class);
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

    public void initiateIndexing(HarvestResultDTO harvestResult) throws DigitalAssetStoreException {
        HttpEntity<String> request = this.createHttpRequestEntity(harvestResult);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.INITIATE_INDEXING));

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        RestTemplate restTemplate = restTemplateBuilder.build();
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(), request, Boolean.class);
    }

    public void initiateRemoveIndexes(HarvestResultDTO harvestResult) throws DigitalAssetStoreException {
        HttpEntity<String> request = this.createHttpRequestEntity(harvestResult);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.INITIATE_REMOVE_INDEXES));

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        RestTemplate restTemplate = restTemplateBuilder.build();
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(), request, Boolean.class);
    }

    public Boolean checkIndexing(Long harvestResultOid) throws DigitalAssetStoreException {
        RestTemplate restTemplate = restTemplateBuilder.build();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.CHECK_INDEXING))
                .queryParam("harvest-result-oid", harvestResultOid);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                null, Boolean.class);

        return result;
    }

    public CustomDepositFormResultDTO getCustomDepositFormDetails(CustomDepositFormCriteriaDTO criteria) throws DigitalAssetStoreException {
        HttpEntity<String> request = this.createHttpRequestEntity(criteria);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.CUSTOM_DEPOSIT_FORM_DETAILS));

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        RestTemplate restTemplate = restTemplateBuilder.build();
        CustomDepositFormResultDTO customDepositFormResultDTO = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(), request, CustomDepositFormResultDTO.class);

        toAbsoluteUrl(customDepositFormResultDTO);

        return customDepositFormResultDTO;
    }

    /**
     * Convert the custom deposit form URL into an absolute URL using the host and
     * port configured for WCT digital asset store. The assumption is that if the
     * custom form URL is a relative URL, then it is hosted in the same web container
     * that hosts the DAS. If it is already an absolute URL, this method doesn't change
     * it.
     *
     * @param response
     */
    protected void toAbsoluteUrl(CustomDepositFormResultDTO response) {
        if (response == null) {
            return;
        }
        String customDepositFormURL = response.getUrlForCustomDepositForm();
        if (customDepositFormURL == null || customDepositFormURL.startsWith("http://") ||
                customDepositFormURL.startsWith("https://")) {
            return;
        }
        if (!customDepositFormURL.startsWith("/")) {
            customDepositFormURL = "/" + customDepositFormURL;
        }
        String urlPrefix = "http://" + getHost() + ":" + getPort();
        response.setUrlForCustomDepositForm(urlPrefix + customDepositFormURL);
    }

    @Override
    public PruneAndImportCommandResult initialPruneAndImport(PruneAndImportCommandApply cmd) {
        PruneAndImportCommandResult result = new PruneAndImportCommandResult();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(org.webcurator.core.visualization.VisualizationConstants.PATH_APPLY_PRUNE_IMPORT));
        URI uri = uriComponentsBuilder.build().toUri();

        HttpEntity<String> request = createHttpRequestEntity(cmd);
        RestTemplate restTemplate = getRestTemplateBuilder().build();
        result = restTemplate.postForObject(uri, request, PruneAndImportCommandResult.class);

        return result;
    }

    @Override
    public void operateHarvestResultModification(String stage, String command, long targetInstanceId, int harvestNumber) throws DigitalAssetStoreException {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.OPERATE_HARVEST_RESULT_MODIFICATION))
                .queryParam("stage", stage)
                .queryParam("command", command)
                .queryParam("targetInstanceId", targetInstanceId)
                .queryParam("harvestNumber", harvestNumber);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(), null, Void.class);
    }
}
