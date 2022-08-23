package org.webcurator.core.visualization.networkmap.bdb;

import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeFolderEntity;

import java.util.ArrayList;
import java.util.List;

public class RepoNetworkNodeFolder extends RepoNetworkNodeBasic {
    private static final String STORE_NAME = "folder";
    private PrimaryIndex<Long, NetworkMapNodeFolderEntity> primaryId;
    private SecondaryIndex<String, Long, NetworkMapNodeFolderEntity> secondaryIndexByTitle;

    public RepoNetworkNodeFolder(Environment env, boolean creatable) {
        super(env, creatable, STORE_NAME);
        this.primaryId = store.getPrimaryIndex(Long.class, NetworkMapNodeFolderEntity.class);
        this.secondaryIndexByTitle = store.getSecondaryIndex(this.primaryId, String.class, "title");

    }

    public NetworkMapNodeFolderEntity insert(NetworkMapNodeFolderEntity entity) {
        entity.setId(this.nextId());
        return primaryId.put(entity);
    }

    public NetworkMapNodeFolderEntity update(NetworkMapNodeFolderEntity entity) throws NullPointerException {
        if (entity.getId() < 0) {
            throw new NullPointerException("The ID of folder entity is null, can not be updated.");
        }
        return primaryId.put(entity);
    }

    public NetworkMapNodeFolderEntity getById(Long id) {
        return primaryId.get(id);
    }

    public NetworkMapNodeFolderEntity getByTitle(String title) {
        return secondaryIndexByTitle.get(title);
    }

    public boolean deleteById(Long id) {
        return primaryId.delete(id);
    }

    public List<Long> getAllEntities() {
        List<Long> list = new ArrayList<>();
        EntityCursor<NetworkMapNodeFolderEntity> cursor = this.primaryId.entities();
        for (NetworkMapNodeFolderEntity node : cursor) {
            list.add(node.getId());
        }
        cursor.close();
        return list;
    }

    public EntityCursor<NetworkMapNodeFolderEntity> openCursor() {
        return this.primaryId.entities();
    }

    public void close() {
        this.store.close();
    }
}
