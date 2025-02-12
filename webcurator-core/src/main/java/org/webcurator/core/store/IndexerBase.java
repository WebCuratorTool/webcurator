package org.webcurator.core.store;

import java.io.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.domain.model.core.HarvestResultDTO;

// TODO Note that the spring boot application needs @EnableRetry for the @Retryable to work.
public abstract class IndexerBase implements RunnableIndex {
    private static final Logger log = LoggerFactory.getLogger(IndexerBase.class);

    private boolean defaultIndexer = false;
    private Mode mode = Mode.INDEX;

    public class ARCFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".arc") ||
                    name.toLowerCase().endsWith(".arc.gz") ||
                    name.toLowerCase().endsWith(".warc") ||
                    name.toLowerCase().endsWith(".warc.gz");
        }
    }

    public IndexerBase() {
        super();
    }

    protected IndexerBase(IndexerBase original) {
        this.defaultIndexer = original.defaultIndexer;
    }

    protected abstract HarvestResultDTO getResult();

    @Override
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public Boolean call() {
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
    }

    @Override
    public void removeIndex(Long harvestResultOid) {
        //Default implementation is to do nothing
    }

}
