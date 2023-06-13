package org.webcurator.core.visualization.networkmap.bdb;

import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapDomain;

import java.util.ArrayList;
import java.util.List;

public class RepoNetworkNodeDomain extends RepoNetworkNodeBasic {
    private static final String STORE_NAME = "domain";
    private PrimaryIndex<Long, NetworkMapDomain> primaryId;

    public RepoNetworkNodeDomain(Environment env, boolean creatable) {
        super(env, creatable, STORE_NAME);
        this.primaryId = store.getPrimaryIndex(Long.class, NetworkMapDomain.class);
    }

    public NetworkMapDomain insert(NetworkMapDomain entity) {
        entity.setId(this.nextId());
        return  primaryId.put(entity);
    }

    public NetworkMapDomain update(NetworkMapDomain entity) throws NullPointerException {
        if (entity.getId() < 0) {
            throw new NullPointerException("The ID of domain entity is null, can not be updated.");
        }
        return primaryId.put(entity);
    }

    public NetworkMapDomain getById(Long id) {
        return primaryId.get(id);
    }

    public boolean deleteById(Long id) {
        return primaryId.delete(id);
    }

    public List<Long> getAllEntities() {
        List<Long> list = new ArrayList<>();
        EntityCursor<NetworkMapDomain> cursor = this.primaryId.entities();
        for (NetworkMapDomain node : cursor) {
            list.add(node.getId());
        }
        cursor.close();
        return list;
    }

    public EntityCursor<NetworkMapDomain> openCursor() {
        return this.primaryId.entities();
    }

    public void close() {
        this.store.close();
    }
}
