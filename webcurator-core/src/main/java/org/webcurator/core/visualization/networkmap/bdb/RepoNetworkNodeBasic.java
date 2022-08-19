package org.webcurator.core.visualization.networkmap.bdb;

import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class RepoNetworkNodeBasic {
    protected static final Logger log = LoggerFactory.getLogger(RepoNetworkNodeBasic.class);
    protected AtomicLong currId = new AtomicLong(0);
    protected EntityStore store;

    public RepoNetworkNodeBasic(Environment env, boolean creatable, String storeName) {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(creatable);
        storeConfig.setTransactional(true);

        this.store = new EntityStore(env, storeName, storeConfig);
    }

    public Long nextId() {
        return currId.getAndIncrement();
    }
}
