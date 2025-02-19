package org.webcurator.core.visualization.networkmap.bdb;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeUrlEntity;

import java.util.ArrayList;
import java.util.List;

public class RepoNetworkNodeUrl extends RepoNetworkNodeBasic {
    private static final String STORE_NAME = "url";
    public PrimaryIndex<Long, NetworkMapNodeUrlEntity> primaryId;
    public SecondaryIndex<String, Long, NetworkMapNodeUrlEntity> secondaryIndexByUrlName;

    public RepoNetworkNodeUrl(Environment env, boolean creatable) {
        super(env, creatable, STORE_NAME);
        this.primaryId = store.getPrimaryIndex(Long.class, NetworkMapNodeUrlEntity.class);
        this.secondaryIndexByUrlName = store.getSecondaryIndex(this.primaryId, String.class, "url");
    }

    public NetworkMapNodeUrlEntity insert(NetworkMapNodeUrlEntity entity) {
        entity.setId(this.nextId());
        try {
            return primaryId.put(entity);
        } catch (Exception e) {
            log.warn("Duplicated data: {}", entity.getTitle(), e);
        }
        return null;
    }

    public NetworkMapNodeUrlEntity update(NetworkMapNodeUrlEntity entity) throws NullPointerException {
        if (entity.getId() < 0) {
            throw new NullPointerException("The ID of url entity is null, can not be updated.");
        }
        return primaryId.put(entity);
    }

    public NetworkMapNodeUrlEntity getById(Long id) {
        return primaryId.get(id);
    }

    public NetworkMapNodeUrlEntity getUrlByName(String urlName) {
        return secondaryIndexByUrlName.get(urlName);
    }

    public boolean deleteById(Long id) {
        return primaryId.delete(id);
    }

    public List<Long> getAllEntities() {
        List<Long> list = new ArrayList<>();
        EntityCursor<NetworkMapNodeUrlEntity> cursor = this.primaryId.entities();
        for (NetworkMapNodeUrlEntity node : cursor) {
            list.add(node.getId());
        }
        cursor.close();
        return list;
    }

    public EntityCursor<NetworkMapNodeUrlEntity> openCursor() {
        return this.primaryId.entities();
    }

    public void close() {
        if (this.store != null) {
            this.store.close();
        }
    }
}
