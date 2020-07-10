package org.webcurator.core.store;

import java.io.File;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessor;
import org.webcurator.core.coordinator.WctCoordinatorPaths;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessorWarc;
import org.webcurator.domain.model.core.*;

// TODO Note that the spring boot application needs @EnableRetry for the @Retryable to work.
public class WCTIndexer extends IndexerBase {
    private final static Logger log = LoggerFactory.getLogger(WCTIndexer.class);

    private HarvestResultDTO result;
    private File directory;
    private boolean doCreate = false;
    private BDBNetworkMapPool pool;

    public WCTIndexer(RestTemplateBuilder restTemplateBuilder) {
        super(restTemplateBuilder);
    }

    public WCTIndexer(String scheme, String host, int port, RestTemplateBuilder restTemplateBuilder) {
        super(scheme, host, port, restTemplateBuilder);
    }

    protected WCTIndexer(WCTIndexer original) {
        super(original);
    }

    @Retryable(maxAttempts = Integer.MAX_VALUE, backoff = @Backoff(delay = 30_000L))
    protected Long createIndex() {
        Long harvestResultOid = Long.MIN_VALUE;
        // Step 1. Save the Harvest Result to the database.
        log.info("Initialising index for job " + getResult().getTargetInstanceOid());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonStr = objectMapper.writeValueAsString(getResult());
            log.debug(jsonStr);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<String>(jsonStr.toString(), headers);

            RestTemplate restTemplate = restTemplateBuilder.build();

            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(WctCoordinatorPaths.CREATE_HARVEST_RESULT));

            harvestResultOid = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(), request, Long.class);
            log.info("Initialised index for job " + getResult().getTargetInstanceOid());
        } catch (JsonProcessingException e) {
            log.error("Parsing json failed: {}", e.getMessage());
        }
        return harvestResultOid;
    }

    @Override
    public Long begin() {
        Long harvestResultOid = null;
        if (doCreate) {
            harvestResultOid = this.createIndex();
            log.debug("Created new Harvest Result: " + harvestResultOid);
        } else {
            log.debug("Using Harvest Result " + getResult().getOid());
            harvestResultOid = getResult().getOid();
        }

        return harvestResultOid;
    }

    @Override
    public void indexFiles(Long harvestResultOid) {
        // Step 2. Save the Index for each file.
        log.info("Generating indexes for " + getResult().getTargetInstanceOid());
        IndexProcessor indexer = null;
        try {
            indexer = new IndexProcessorWarc(pool, getResult().getTargetInstanceOid(), getResult().getHarvestNumber());
        } catch (DigitalAssetStoreException e) {
            log.error("Failed to create directory: {}", directory);
            return;
        } finally {
            if (indexer != null) {
                indexer.clear();
            }
        }

        try {
            indexer.processInternal();
        } catch (Exception e) {
            log.error("Failed to index files: {}", directory);
            return;
        } finally {
            indexer.clear();
        }

        log.info("Completed indexing for job " + getResult().getTargetInstanceOid());
    }

    @Override
    public String getName() {
        return getClass().getCanonicalName();
    }

    public void setDoCreate(boolean doCreate) {
        this.doCreate = doCreate;
    }

    @Override
    public void initialise(HarvestResultDTO result, File directory) {
        this.result = result;
        this.directory = directory;
    }

    @Override
    protected HarvestResultDTO getResult() {
        return result;
    }

    @Override
    public RunnableIndex getCopy() {
        return new WCTIndexer(this);
    }

    @Override
    public boolean isEnabled() {
        //WCT indexer is always enabled
        return true;
    }

    public void setBDBNetworkMapPool(BDBNetworkMapPool pool) {
        this.pool = pool;
    }
}

