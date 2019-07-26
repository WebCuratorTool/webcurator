package org.webcurator.core.store;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Bytes;
import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.domain.model.core.ArcHarvestResultDTO;
import org.webcurator.domain.model.core.CustomDepositFormCriteriaDTO;
import org.webcurator.domain.model.core.CustomDepositFormResultDTO;
import org.webcurator.domain.model.core.HarvestResourceDTO;
import org.webcurator.domain.model.core.HarvestResultDTO;

import javax.activation.DataHandler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class DigitalAssetStoreClient implements DigitalAssetStore {

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

    public DigitalAssetStoreClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String baseUrl() {
        return host + ":" + port;
    }

    public String getUrl(String appendUrl) {
        return baseUrl() + appendUrl;
    }

    public void save(String targetInstanceName, String directory, Path path) throws DigitalAssetStoreException {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.SAVE))
                .queryParam("directory", directory)
                .queryParam("path", path);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException
        Map<String, String> pathVariables = ImmutableMap.of("target-instance-name", targetInstanceName);
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Boolean.class);
    }

    public void save(String targetInstanceName, Path path) throws DigitalAssetStoreException {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.SAVE))
                .queryParam("path", path);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException
        Map<String, String> pathVariables = ImmutableMap.of("target-instance-name", targetInstanceName);
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Boolean.class);
    }

    public void save(String targetInstanceName, String directory, List<Path> paths) throws DigitalAssetStoreException {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.SAVE))
                .queryParam("directory", directory)
                .queryParam("paths", paths);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException
        Map<String, String> pathVariables = ImmutableMap.of("target-instance-name", targetInstanceName);
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Boolean.class);
    }

    public void save(String targetInstanceName, List<Path> paths) throws DigitalAssetStoreException {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.SAVE))
                .queryParam("paths", paths);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException
        Map<String, String> pathVariables = ImmutableMap.of("target-instance-name", targetInstanceName);
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Boolean.class);
    }

    public Path getResource(String targetInstanceName, int harvestResultNumber, HarvestResourceDTO resource)
            throws DigitalAssetStoreException {
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.RESOURCE))
                .queryParam("harvest-result-number", harvestResultNumber)
                .queryParam("resource", resource);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException
        Map<String, String> pathVariables = ImmutableMap.of("target-instance-name", targetInstanceName);
        DataHandler dataHandler = restTemplate.getForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                DataHandler.class);

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
        RestTemplate restTemplate = new RestTemplate();
        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.HEADERS))
                .queryParam("harvest-result-number", harvestResultNumber)
                .queryParam("resource", resource);

        Map<String, String> pathVariables = ImmutableMap.of("target-instance-name", targetInstanceName);
        ResponseEntity<List<Header>> listResponse = restTemplate.exchange(
                uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Header>>() {
                });

        return listResponse.getBody();
    }

    public HarvestResultDTO copyAndPrune(String targetInstanceName, int originalHarvestResultNumber, int newHarvestResultNumber,
                                         List<String> urisToDelete, List<HarvestResourceDTO> harvestResourcesToImport)
            throws DigitalAssetStoreException {
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.COPY_AND_PRUNE))
                .queryParam("original-harvest-result-number", originalHarvestResultNumber)
                .queryParam("new-harvest-result-number", newHarvestResultNumber)
                .queryParam("uris-to-delete", urisToDelete)
                .queryParam("harvest-resources-to-import", harvestResourcesToImport);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException
        Map<String, String> pathVariables = ImmutableMap.of("target-instance-name", targetInstanceName);
        HarvestResultDTO harvestResultDTO = restTemplate.getForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                HarvestResultDTO.class);

        return harvestResultDTO;
    }

    /**
     * @see DigitalAssetStore#purge(List<String>).
     */
    public void purge(List<String> targetInstanceNames) throws DigitalAssetStoreException {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.PURGE))
                .queryParam("target-instance-names", targetInstanceNames);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                null, Boolean.class);
    }

    /**
     * @see DigitalAssetStore#purgeAbortedTargetInstances(List<String>).
     */
    public void purgeAbortedTargetInstances(List<String> targetInstanceNames) throws DigitalAssetStoreException {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.PURGE_ABORTED_TARGET_INSTANCES))
                .queryParam("target-instance-names", targetInstanceNames);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                null, Boolean.class);
    }

    public void submitToArchive(String targetInstanceOid, String sip, Map xAttributes, int harvestNumber)
            throws DigitalAssetStoreException {
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.ARCHIVE))
                .queryParam("sip", sip)
                .queryParam("x-attributes", xAttributes)
                .queryParam("harvest-number", harvestNumber);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException
        Map<String, String> pathVariables = ImmutableMap.of("target-instance-oid", targetInstanceOid);
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Boolean.class);
    }

    public byte[] getSmallResource(String targetInstanceName, int harvestResultNumber, HarvestResourceDTO resource) throws DigitalAssetStoreException {
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.SMALL_RESOURCE))
                .queryParam("harvest-result-number", harvestResultNumber)
                .queryParam("resource", resource);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException
        Map<String, String> pathVariables = ImmutableMap.of("target-instance-name", targetInstanceName);
        ResponseEntity<List<Byte>> listResponse = restTemplate.exchange(
                uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Byte>>() {
                });

        return Bytes.toArray(listResponse.getBody());
    }

    public void initiateIndexing(ArcHarvestResultDTO harvestResult) throws DigitalAssetStoreException {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.INITIATE_INDEXING))
                .queryParam("harvest-result", harvestResult);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                null, Boolean.class);
    }

    public void initiateRemoveIndexes(ArcHarvestResultDTO harvestResult) throws DigitalAssetStoreException {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.INITIATE_REMOVE_INDEXES))
                .queryParam("harvest-result", harvestResult);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                null, Boolean.class);
    }

    public Boolean checkIndexing(Long harvestResultOid) throws DigitalAssetStoreException {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.CHECK_INDEXING))
                .queryParam("harvest-result-oid", harvestResultOid);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException
        Boolean result = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                null, Boolean.class);

        return result;
    }

    public CustomDepositFormResultDTO getCustomDepositFormDetails(CustomDepositFormCriteriaDTO criteria) throws DigitalAssetStoreException {
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(DigitalAssetStorePaths.GET_CUSTOM_DEPOSIT_FORM_DETAILS))
                .queryParam("custom-deposit-form-criteria", criteria);

        // TODO Process any exceptions or 404s, etc. as DigitalAssetStoreException
        CustomDepositFormResultDTO customDepositFormResultDTO = restTemplate.getForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                CustomDepositFormResultDTO.class);

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
