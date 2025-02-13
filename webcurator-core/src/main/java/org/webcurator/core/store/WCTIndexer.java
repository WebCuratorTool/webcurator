package org.webcurator.core.store;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.visualization.VisualizationProcessorManager;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessor;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessorWarc;
import org.webcurator.domain.model.core.*;

// TODO Note that the spring boot application needs @EnableRetry for the @Retryable to work.
public class WCTIndexer extends IndexerBase {
    private final static Logger log = LoggerFactory.getLogger(WCTIndexer.class);

    private HarvestResultDTO result;
    private File directory;
    private boolean doCreate = false;
    private boolean enabled = false;
    private BDBNetworkMapPool pool;
    private VisualizationProcessorManager visProcessorManager;

    public WCTIndexer() {
        super();
    }

    protected WCTIndexer(WCTIndexer original) {
        super(original);
        this.result = original.result;
        this.directory = original.directory;
        this.doCreate = original.doCreate;
        this.enabled = original.enabled;
        this.pool = original.pool;
        this.visProcessorManager = original.visProcessorManager;
    }

    @Override
    public Long begin() {
        return this.getResult().getOid();
    }

    @Override
    public void indexFiles(Long harvestResultOid) {
        // Step 2. Save the Index for each file.
        log.info("Generating indexes for: {}", getResult().getTargetInstanceOid());
        if (this.pool == null) {
            log.error("Exit with error: pool is null");
            return;
        }
        IndexProcessor indexer = null;
        try {
            indexer = new IndexProcessorWarc(this.pool, getResult().getTargetInstanceOid(), getResult().getHarvestNumber());
            if (visProcessorManager.executeTask(indexer)) {
                log.info("Indexing is finished: {}", directory);
            } else {
                log.error("Failed to index, {}", directory);
            }
        } catch (DigitalAssetStoreException e) {
            log.error("Failed to create directory: {}", directory, e);
        } catch (IOException e) {
            log.error("Failed to start the vis indexing, {}", directory, e);
        } finally {
            if (indexer != null) {
                indexer.clear();
            }
        }
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
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public BDBNetworkMapPool getPool() {
        return pool;
    }

    public void setPool(BDBNetworkMapPool pool) {
        this.pool = pool;
    }

    public VisualizationProcessorManager getVisProcessorManager() {
        return visProcessorManager;
    }

    public void setVisProcessorManager(VisualizationProcessorManager visProcessorManager) {
        this.visProcessorManager = visProcessorManager;
    }
}

