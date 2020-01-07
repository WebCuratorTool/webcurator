package org.webcurator.core.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Bytes;
import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.rest.RestClientResponseHandler;
import org.webcurator.domain.model.core.ArcHarvestResultDTO;
import org.webcurator.domain.model.core.CustomDepositFormCriteriaDTO;
import org.webcurator.domain.model.core.CustomDepositFormResultDTO;
import org.webcurator.domain.model.core.HarvestResourceDTO;
import org.webcurator.domain.model.core.HarvestResultDTO;
import org.webcurator.domain.model.core.harvester.store.HarvestStoreDTO;

import javax.activation.DataHandler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

// TODO Note that the spring boot application needs @EnableRetry for the @Retryable to work.
public class DigitalAssetStoreClient implements DigitalAssetStore, DigitalAssetStoreConfig {

    /**
     * the logger.
     */
    private static Log log = LogFactory.getLog(DigitalAssetStoreClient.class);
    /**
     * the host name or ip-address for the das.
     */
    private String host = "localhost";
    /**
     * the port number for the das.
     */
    private int port = 8080;

    private final RestTemplateBuilder restTemplateBuilder;

    public DigitalAssetStoreClient(String host, int port, RestTemplateBuilder restTemplateBuilder) {
        this.host = host;
        this.port = port;
        this.restTemplateBuilder = restTemplateBuilder;
        restTemplateBuilder.errorHandler(new RestClientResponseHandler())
                .setConnectTimeout(Duration.ofSeconds(15L));
    }

    public String baseUrl() {
        return "http://" + host + ":" + port;
    }

    public String getUrl(String appendUrl) {
        return baseUrl() + appendUrl;
    }

    private void internalSave(String targetInstanceName, HarvestStoreDTO requestBody) throws DigitalAssetStoreException{
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonStr = objectMapper.writeValueAsString(requestBody);
            log.debug(jsonStr);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<String>(jsonStr.toString(), headers);

            Map<String, String> pathVariables = ImmutableMap.of("target-instance-name", targetInstanceName);
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.SAVE));
            URI uri=uriComponentsBuilder.buildAndExpand(pathVariables).toUri();
            restTemplate.postForObject(uri, request, Void.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new DigitalAssetStoreException(e);
        }
    }

    @Override
    public void save(String targetInstanceName, String directory, Path path) throws DigitalAssetStoreException {
        HarvestStoreDTO requestBody=new HarvestStoreDTO();
        requestBody.setDirectory(directory);
        requestBody.setPathFromPath(path);

        internalSave(targetInstanceName, requestBody);
    }

    @Override
    // Assuming that without any values set will keep infinitely retrying
    @Retryable(maxAttempts = Integer.MAX_VALUE, backoff = @Backoff(delay = 30_000L))
    public void save(String targetInstanceName, Path path) throws DigitalAssetStoreException {
        HarvestStoreDTO requestBody=new HarvestStoreDTO();
        requestBody.setPathFromPath(path);

        internalSave(targetInstanceName, requestBody);
    }

    @Override
    public void save(String targetInstanceName, String directory, List<Path> paths) throws DigitalAssetStoreException {
        HarvestStoreDTO requestBody=new HarvestStoreDTO();
        requestBody.setDirectory(directory);
        requestBody.setPathsFromPath(paths);

        internalSave(targetInstanceName, requestBody);
    }

    @Override
    public void save(String targetInstanceName, List<Path> paths) throws DigitalAssetStoreException {
        HarvestStoreDTO requestBody=new HarvestStoreDTO();
        requestBody.setPathsFromPath(paths);

        internalSave(targetInstanceName, requestBody);
    }

    public Path getResource(String targetInstanceName, int harvestResultNumber, HarvestResourceDTO resource) throws DigitalAssetStoreException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = null;
        try {
            jsonStr = objectMapper.writeValueAsString(resource);
            log.debug(jsonStr);
        } catch (JsonProcessingException e) {
            log.error(e);
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<String>(jsonStr.toString(), headers);

        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.RESOURCE))
                .queryParam("harvest-result-number", harvestResultNumber);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        Map<String, String> pathVariables = ImmutableMap.of("target-instance-name", targetInstanceName);
        DataHandler dataHandler = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),request, DataHandler.class);

        FileOutputStream fos = null;
        try {
            File file = File.createTempFile("wctd", "tmp");
            fos = new FileOutputStream(file);
            dataHandler.writeTo(fos);
            return file.toPath();
        } catch (IOException ex) {
            throw new DigitalAssetStoreException("Failed to get resource for " + targetInstanceName + " " +
                    harvestResultNumber + ": " + ex.getMessage(), ex);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                throw new DigitalAssetStoreException("Failed to get resource for " + targetInstanceName + " " +
                        harvestResultNumber + ": " + ex.getMessage(), ex);
            }
        }
    }

    public List<Header> getHeaders(String targetInstanceName, int harvestResultNumber, HarvestResourceDTO resource)
            throws DigitalAssetStoreException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = null;
        try {
            jsonStr = objectMapper.writeValueAsString(resource);
            log.debug(jsonStr);
        } catch (JsonProcessingException e) {
            log.error(e);
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<String>(jsonStr.toString(), headers);

        RestTemplate restTemplate = restTemplateBuilder.build();
        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.HEADERS))
                .queryParam("harvest-result-number", harvestResultNumber)
                .queryParam("resource", resource);

        Map<String, String> pathVariables = ImmutableMap.of("target-instance-name", targetInstanceName);
        ResponseEntity<List<Header>> listResponse = restTemplate.exchange(
                uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                HttpMethod.POST, request, new ParameterizedTypeReference<List<Header>>() {
                });

        return listResponse.getBody();
    }

    public HarvestResultDTO copyAndPrune(String targetInstanceName, int originalHarvestResultNumber, int newHarvestResultNumber,
                                         List<String> urisToDelete, List<HarvestResourceDTO> harvestResourcesToImport)
            throws DigitalAssetStoreException {
        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.COPY_AND_PRUNE))
                .queryParam("original-harvest-result-number", originalHarvestResultNumber)
                .queryParam("new-harvest-result-number", newHarvestResultNumber)
                .queryParam("uris-to-delete", urisToDelete)
                .queryParam("harvest-resources-to-import", harvestResourcesToImport);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        Map<String, String> pathVariables = ImmutableMap.of("target-instance-name", targetInstanceName);
        HarvestResultDTO harvestResultDTO = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, HarvestResultDTO.class);

        return harvestResultDTO;
    }

    /**
     * @see DigitalAssetStore#purge(List<String>).
     */
    public void purge(List<String> targetInstanceNames) throws DigitalAssetStoreException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = null;
        try {
            jsonStr = objectMapper.writeValueAsString(targetInstanceNames);
            log.debug(jsonStr);
        } catch (JsonProcessingException e) {
            log.error(e);
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<String>(jsonStr.toString(), headers);

        RestTemplate restTemplate = restTemplateBuilder.build();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.PURGE));

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                request, Boolean.class);
    }

    /**
     * @see DigitalAssetStore#purgeAbortedTargetInstances(List<String>).
     */
    public void purgeAbortedTargetInstances(List<String> targetInstanceNames) throws DigitalAssetStoreException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = null;
        try {
            jsonStr = objectMapper.writeValueAsString(targetInstanceNames);
            log.debug(jsonStr);
        } catch (JsonProcessingException e) {
            log.error(e);
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<String>(jsonStr.toString(), headers);

        RestTemplate restTemplate = restTemplateBuilder.build();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.PURGE_ABORTED_TARGET_INSTANCES));

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                request, Boolean.class);
    }

    public void submitToArchive(String targetInstanceOid, String sip, Map xAttributes, int harvestNumber)
            throws DigitalAssetStoreException {
        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.ARCHIVE))
                .queryParam("sip", sip)
                .queryParam("x-attributes", xAttributes)
                .queryParam("harvest-number", harvestNumber);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        Map<String, String> pathVariables = ImmutableMap.of("target-instance-oid", targetInstanceOid);
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Boolean.class);
    }

    public byte[] getSmallResource(String targetInstanceName, int harvestResultNumber, HarvestResourceDTO resource) throws DigitalAssetStoreException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = null;
        try {
            jsonStr = objectMapper.writeValueAsString(resource);
            log.debug(jsonStr);
        } catch (JsonProcessingException e) {
            log.error(e);
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<String>(jsonStr.toString(), headers);

        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.SMALL_RESOURCE))
                .queryParam("harvest-result-number", harvestResultNumber);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        Map<String, String> pathVariables = ImmutableMap.of("target-instance-name", targetInstanceName);
        ResponseEntity<List<Byte>> listResponse = restTemplate.exchange(
                uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                HttpMethod.POST, request, new ParameterizedTypeReference<List<Byte>>() {
                });

        return Bytes.toArray(listResponse.getBody());
    }

    public void initiateIndexing(ArcHarvestResultDTO harvestResult) throws DigitalAssetStoreException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = null;
        try {
            jsonStr = objectMapper.writeValueAsString(harvestResult);
            log.debug(jsonStr);
        } catch (JsonProcessingException e) {
            log.error(e);
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<String>(jsonStr.toString(), headers);

        RestTemplate restTemplate = restTemplateBuilder.build();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.INITIATE_INDEXING));

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(), request, Boolean.class);
    }

    public void initiateRemoveIndexes(ArcHarvestResultDTO harvestResult) throws DigitalAssetStoreException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = null;
        try {
            jsonStr = objectMapper.writeValueAsString(harvestResult);
            log.debug(jsonStr);
        } catch (JsonProcessingException e) {
            log.error(e);
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<String>(jsonStr.toString(), headers);

        RestTemplate restTemplate = restTemplateBuilder.build();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.INITIATE_REMOVE_INDEXES));

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
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
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = null;
        try {
            jsonStr = objectMapper.writeValueAsString(criteria);
            log.debug(jsonStr);
        } catch (JsonProcessingException e) {
            log.error(e);
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<String>(jsonStr.toString(), headers);

        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.CUSTOM_DEPOSIT_FORM_DETAILS));

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException, currently thrown as WCTRuntimeException.
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

    public String toString() {
        return this.getClass().getName() + "@" + host + ":" + port;
    }

    public String getHost() {
        return host;
    }

    /**
     * @param host The host to set.
     */
    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    /**
     * @param port The port to set.
     */
    public void setPort(int port) {
        this.port = port;
    }
}
