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

    public WCTIndexer(String baseUrl, RestTemplateBuilder restTemplateBuilder) {
        super(baseUrl, restTemplateBuilder);
    }

    protected WCTIndexer(WCTIndexer original) {
        super(original);
    }

    protected Long createIndex() {
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

            Long harvestResultOid = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(), request, Long.class);
            log.info("Initialised index for job " + getResult().getTargetInstanceOid());
            return harvestResultOid;
        } catch (JsonParseException e) {
            log.error("Parsing json failed: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Index initialisation failed: {}", e.getMessage());
            return null;
        }
    }

    private void sleep30seconds() {
        try {Thread.sleep(30*1000 /* 30seconds */);} catch (Exception e) {};
    }

    @Override
    public Long begin() {
        Long harvestResultOid = null;
        if (doCreate) {
            retry: for (int attempt = 0; attempt < 10 ; attempt++) {
                harvestResultOid = this.createIndex();
                if (harvestResultOid != null)  {
                    break retry;
                }
                sleep30seconds();
            } 
            if (harvestResultOid == null) {
                log.error("Failed to create harvestresult index after 10 attempts");
            } else {
                log.debug("Created new Harvest Result: " + harvestResultOid);
            }
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

