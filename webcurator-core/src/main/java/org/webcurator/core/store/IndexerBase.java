package org.webcurator.core.store;

import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.domain.model.core.HarvestResultDTO;

// TODO Note that the spring boot application needs @EnableRetry for the @Retryable to work.
public abstract class IndexerBase implements RunnableIndex {
    private static final Logger log = LoggerFactory.getLogger(IndexerBase.class);
    private boolean defaultIndexer = false;
    private Mode mode = Mode.INDEX;
    private CompletableFuture<Boolean> future;
    protected boolean isRunning = true;
    protected HarvestResultDTO result;
    protected File directory;

    public class ARCFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".arc") ||
                    name.toLowerCase().endsWith(".arc.gz") ||
                    name.toLowerCase().endsWith(".warc") ||
                    name.toLowerCase().endsWith(".warc.gz");
        }
    }

    @Override
    public void initialise(HarvestResultDTO result, File directory) {
        this.result = result;
        this.directory = directory;
    }

    public IndexerBase() {
        super();
    }

    protected IndexerBase(IndexerBase original) {
        this.defaultIndexer = original.defaultIndexer;
    }

    public HarvestResultDTO getResult() {
        return result;
    }

    @Override
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public CompletableFuture<Boolean> submitAsync() {
        this.future = CompletableFuture.supplyAsync(() -> {
            Long harvestResultOid = begin();
            try {
                if (mode == Mode.REMOVE) {
                    removeIndex(harvestResultOid);
                } else {
                    indexFiles(harvestResultOid);
                }
                return true;
            } catch (Exception ex) {
                log.error("Failed to index or remove index: {}", harvestResultOid, ex);
                return false;
            }
        });
        return this.future;
    }

    @Override
    public boolean cancel() {
        this.isRunning = false;
        this.close();
        if (!this.future.isCancelled()) {
            boolean ret = this.future.cancel(true);
            log.info("The indexer: {} is canceled with status: {}", this.getName(), ret);
            return ret;
        }
        return true;
    }

    @Override
    public boolean isDone() {
        boolean done = future.isDone();
        if (done) {
            log.info("{}, is done, {} {}", this.getName(), result.getTargetInstanceOid(), result.getHarvestNumber());
        } else {
            log.debug("{}, is running, {} {}", this.getName(), result.getTargetInstanceOid(), result.getHarvestNumber());
        }
        return done;
    }

    @Override
    public boolean getValue() {
        try {
            return this.future.get();
        } catch (InterruptedException | ExecutionException ex) {
            log.error("{}, failed to get value: {} {}", this.getName(), result.getTargetInstanceOid(), result.getHarvestNumber(), ex);
            return false;
        }
    }

    @Override
    public void removeIndex(Long harvestResultOid) {
        //Default implementation is to do nothing
    }

}
