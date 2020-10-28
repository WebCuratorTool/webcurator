package org.webcurator.core.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Bytes;
import org.apache.commons.httpclient.Header;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.domain.model.core.*;
import org.webcurator.domain.model.core.harvester.store.HarvestStoreCopyAndPruneDTO;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
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

    public Path getResource(String targetInstanceName, int harvestResultNumber, HarvestResourceDTO resource) throws DigitalAssetStoreException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = this.encode2json(resource);

        Map<String, String> pathVariables = ImmutableMap.of("target-instance-name", targetInstanceName);
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.RESOURCE))
                .queryParam("harvest-result-number", harvestResultNumber);
        try {
            URI uri = uriComponentsBuilder.buildAndExpand(pathVariables).toUri();
            HttpURLConnection urlConnection = (HttpURLConnection)uri.toURL().openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            urlConnection.setRequestProperty("Content-Type","application/json");
            urlConnection.setRequestProperty("Content-Length",Integer.toString(jsonStr.length()));
            urlConnection.getOutputStream().write(jsonStr.getBytes());

            File file = File.createTempFile("wctd", "tmp");
            IOUtils.copy(urlConnection.getInputStream(), Files.newOutputStream(file.toPath()));

            return file.toPath();
        } catch (IOException e) {
            String err = "Failed to get resource for " + targetInstanceName + " " + harvestResultNumber + ": " + e.getMessage();
            log.error(err, e);
            throw new DigitalAssetStoreException(err, e);
        }
    }

    public List<Header> getHeaders(String targetInstanceName, int harvestResultNumber, HarvestResourceDTO resource)
            throws DigitalAssetStoreException {
        HttpEntity<String> request = this.createHttpRequestEntity(resource);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.HEADERS))
                .queryParam("harvest-result-number", harvestResultNumber)
                .queryParam("resource", resource);
        Map<String, String> pathVariables = ImmutableMap.of("target-instance-name", targetInstanceName);
        URI uri = uriComponentsBuilder.buildAndExpand(pathVariables).toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();
        ResponseEntity<List<Header>> listResponse = restTemplate.exchange(uri, HttpMethod.POST, request,
                new ParameterizedTypeReference<List<Header>>() {
                });
        return listResponse.getBody();
    }

    public HarvestResultDTO copyAndPrune(String targetInstanceName, int originalHarvestResultNumber, int newHarvestResultNumber,
                                         List<String> urisToDelete, List<HarvestResourceDTO> harvestResourcesToImport) throws DigitalAssetStoreException {
        HarvestStoreCopyAndPruneDTO dto = new HarvestStoreCopyAndPruneDTO();
        dto.setUrisToDelete(urisToDelete);
        dto.setHarvestResourcesToImport(harvestResourcesToImport);

        HttpEntity<String> request = this.createHttpRequestEntity(dto);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.COPY_AND_PRUNE))
                .queryParam("original-harvest-result-number", originalHarvestResultNumber)
                .queryParam("new-harvest-result-number", newHarvestResultNumber);
        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        Map<String, String> pathVariables = ImmutableMap.of("target-instance-name", targetInstanceName);
        URI uri = uriComponentsBuilder.buildAndExpand(pathVariables).toUri();

        RestTemplate restTemplate = restTemplateBuilder.build();
        HarvestResultDTO harvestResultDTO = restTemplate.postForObject(uri, request, HarvestResultDTO.class);
        return harvestResultDTO;
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

    public byte[] getSmallResource(String targetInstanceName, int harvestResultNumber, HarvestResourceDTO resource) throws DigitalAssetStoreException {
        HttpEntity<String> request = this.createHttpRequestEntity(resource);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.SMALL_RESOURCE))
                .queryParam("harvest-result-number", harvestResultNumber);
        Map<String, String> pathVariables = ImmutableMap.of("target-instance-name", targetInstanceName);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        RestTemplate restTemplate = restTemplateBuilder.build();
        ResponseEntity<List<Byte>> listResponse = restTemplate.exchange(
                uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                HttpMethod.POST, request, new ParameterizedTypeReference<List<Byte>>() {
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
        String urlPrefix = baseUrl;
        response.setUrlForCustomDepositForm(urlPrefix + customDepositFormURL);
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
}
