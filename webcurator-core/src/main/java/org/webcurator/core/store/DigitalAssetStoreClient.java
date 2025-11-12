package org.webcurator.core.store;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Bytes;
import org.apache.commons.httpclient.Header;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.core.util.WctUtils;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.modification.metadata.ModifyResult;
import org.webcurator.domain.model.core.*;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;


@SuppressWarnings("all")
// TODO Note that the spring boot application needs @EnableRetry for the @Retryable to work.
public class DigitalAssetStoreClient extends AbstractRestClient implements DigitalAssetStore {
    /* The way to upload warcs, logs and reports to store component */
    private String fileUploadMode;
    private String harvestBaseUrl;

    public DigitalAssetStoreClient(String baseUrl, RestTemplateBuilder restTemplateBuilder) {
        super(baseUrl, restTemplateBuilder);
    }

    @Override
    public void save(String targetInstanceName, String directory, Path path) throws DigitalAssetStoreException {
        try {
            File file = path.toFile();
            if (!file.exists() || !file.isFile()) {
                throw new DigitalAssetStoreException("File does not exist: " + path);
            }

            DigitalAssetStoreHarvestSaveDTO dto = new DigitalAssetStoreHarvestSaveDTO();
            dto.setFileUploadMode(fileUploadMode);
            dto.setTargetInstanceName(targetInstanceName);
            dto.setDirectory(directory);
            dto.setFilePath(file.getAbsolutePath());
            dto.setHarvestBaseUrl(harvestBaseUrl);

            HttpEntity<String> requestBody = this.createHttpRequestEntity(dto);

            RestTemplate restTemplate = restTemplateBuilder.build();
            restTemplate.postForEntity(getUrl(DigitalAssetStorePaths.SAVE), requestBody, Void.class);
        } catch (Exception e) {
            log.error("Save file failed", e);
            throw new DigitalAssetStoreException(e);
        }

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
            WctUtils.copy(connection.getInputStream(), Files.newOutputStream(file.toPath()));
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
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                request, Void.class);
    }

    /**
     * @see DigitalAssetStore#purgeAbortedTargetInstances(List<String>).
     */
    public void purgeAbortedTargetInstances(List<String> targetInstanceNames) throws DigitalAssetStoreException {
        HttpEntity<String> request = this.createHttpRequestEntity(targetInstanceNames);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.PURGE_ABORTED_TARGET_INSTANCES));

        RestTemplate restTemplate = restTemplateBuilder.build();
        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                request, Void.class);
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
                request, Void.class);
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
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(), request, Void.class);
    }

    public void initiateRemoveIndexes(HarvestResultDTO harvestResult) throws DigitalAssetStoreException {
        HttpEntity<String> request = this.createHttpRequestEntity(harvestResult);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.INITIATE_REMOVE_INDEXES));

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(), request, Void.class);
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
        String urlPrefix = baseUrl;
        response.setUrlForCustomDepositForm(urlPrefix + customDepositFormURL);
    }

    @Override
    public ModifyResult initialPruneAndImport(ModifyApplyCommand cmd) {
        ModifyResult result = new ModifyResult();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(org.webcurator.core.visualization.VisualizationConstants.PATH_APPLY_PRUNE_IMPORT));
        URI uri = uriComponentsBuilder.build().toUri();

        HttpEntity<String> request = createHttpRequestEntity(cmd);
        RestTemplate restTemplate = getRestTemplateBuilder().build();
        result = restTemplate.postForObject(uri, request, ModifyResult.class);

        return result;
    }


    public String getFileUploadMode() {
        return fileUploadMode;
    }

    public void setFileUploadMode(String fileUploadMode) {
        //Take copy as the default file transfering type
        if (fileUploadMode == null || !fileUploadMode.equalsIgnoreCase(FILE_UPLOAD_MODE_STREAM)) {
            this.fileUploadMode = FILE_UPLOAD_MODE_COPY;
        } else {
            this.fileUploadMode = FILE_UPLOAD_MODE_STREAM;
        }
    }

    public String getHarvestBaseUrl() {
        return harvestBaseUrl;
    }

    public void setHarvestBaseUrl(String harvestBaseUrl) {
        this.harvestBaseUrl = harvestBaseUrl;
    }

    @Override
    public void abortIndexing(HarvestResultDTO dto) {
        HttpEntity<String> request = this.createHttpRequestEntity(dto);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.ABORT_INDEXING));

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(), request, Void.class);
    }

    @Override
    public void abortPruneAndImport(HarvestResultDTO dto) {
        HttpEntity<String> request = this.createHttpRequestEntity(dto);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.ABORT_PRUNE_IMPORT));

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(), request, Void.class);
    }
}
