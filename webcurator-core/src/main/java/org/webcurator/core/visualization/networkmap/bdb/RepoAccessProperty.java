package org.webcurator.core.visualization.networkmap.bdb;

import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapAccessPropertyEntity;

public class RepoAccessProperty {
    private static final String STORE_NAME = "access-property";

    protected long SINGLE_INSTANCE_ID = 0;
    protected EntityStore store = null;

    private final PrimaryIndex<Long, NetworkMapAccessPropertyEntity> primaryIndexById;

    public RepoAccessProperty(Environment env, boolean creatable) {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(creatable);
        storeConfig.setTransactional(true);
        this.store = new EntityStore(env, STORE_NAME, storeConfig);
        this.primaryIndexById = this.store.getPrimaryIndex(Long.class, NetworkMapAccessPropertyEntity.class);
    }

    public NetworkMapAccessPropertyEntity save(NetworkMapAccessPropertyEntity entity) {
        entity.setId(SINGLE_INSTANCE_ID);
        primaryIndexById.put(entity);
        return entity;
    }

    public NetworkMapAccessPropertyEntity get() {
        return primaryIndexById.get(SINGLE_INSTANCE_ID);
    }

    public void close() {
        if (this.store != null) {
            this.store.close();
        }
    }
}
