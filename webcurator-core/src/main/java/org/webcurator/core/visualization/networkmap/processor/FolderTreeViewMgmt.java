package org.webcurator.core.visualization.networkmap.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.bdb.BDBRepoHolder;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapAccessPropertyEntity;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeFolderDTO;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeFolderEntity;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeUrlEntity;

import java.util.ArrayList;
import java.util.List;

public class FolderTreeViewMgmt {
    private final static Logger log = LoggerFactory.getLogger(FolderTreeViewMgmt.class);
    private final BDBNetworkMapPool pool;

    public FolderTreeViewMgmt(BDBNetworkMapPool pool) {
        this.pool = pool;
    }

    public List<NetworkMapNodeFolderEntity> queryRootFolderList(long job, int harvestResultNumber) {
        BDBRepoHolder db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return null;
        }

        NetworkMapAccessPropertyEntity accProp = db.getAccProp();
        if (accProp == null) {
            return null;
        }

        Long rootFolderEntityId = accProp.getRootFolderNode();
        return queryFolderList(job, harvestResultNumber, rootFolderEntityId);
    }

    public List<NetworkMapNodeFolderEntity> queryFolderListWithTitle(long job, int harvestResultNumber, String title) {
        BDBRepoHolder db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return null;
        }
        NetworkMapNodeFolderEntity parentFolderEntity = db.getFolderByTitle(title);
        if (parentFolderEntity == null) {
            return null;
        }
        return queryFolderList(job, harvestResultNumber, parentFolderEntity);
    }

    public List<NetworkMapNodeFolderEntity> queryFolderList(long job, int harvestResultNumber, long parentFolderId) {
        BDBRepoHolder db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return null;
        }
        NetworkMapNodeFolderEntity parentFolderEntity = db.getFolderById(parentFolderId);
        return queryFolderList(job, harvestResultNumber, parentFolderEntity);
    }

    public List<NetworkMapNodeFolderEntity> queryFolderList(long job, int harvestResultNumber, NetworkMapNodeFolderEntity parentFolderEntity) {
        if (parentFolderEntity == null) {
            return null;
        }

        BDBRepoHolder db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return null;
        }
        List<NetworkMapNodeFolderEntity> listFolderDTO = new ArrayList<>();

        for (long id : parentFolderEntity.getSubFolderList()) {
            NetworkMapNodeFolderEntity folderEntity = db.getFolderById(id);
            folderEntity.setLazy(true);
            listFolderDTO.add(folderEntity);
        }

        for (long id : parentFolderEntity.getSubUrlList()) {
            NetworkMapNodeUrlEntity urlEntity = db.getUrlById(id);
            NetworkMapNodeFolderEntity folderEntity = new NetworkMapNodeFolderEntity();
            folderEntity.copy(urlEntity);
            folderEntity.setTitle(urlEntity.getUrl());
            folderEntity.setLazy(true);
            listFolderDTO.add(folderEntity);
        }
        return listFolderDTO;
    }

    public NetworkMapNodeFolderDTO createFolderTreeView(List<NetworkMapNodeUrlEntity> allNetworkMapNodes) {
        NetworkMapNodeFolderDTO rootTreeNode = new NetworkMapNodeFolderDTO();
        for (NetworkMapNodeUrlEntity urlEntity : allNetworkMapNodes) {
            NetworkMapNodeFolderDTO treeNode = new NetworkMapNodeFolderDTO();
            treeNode.copy(urlEntity);
            treeNode.setUrl(urlEntity.getUrl());
            treeNode.setTitle(urlEntity.getUrl());
            rootTreeNode.getChildren().add(treeNode);
            urlEntity.clear();
        }

        FolderTreeViewGenerator cascadePathProcessor = new FolderTreeViewGenerator();
        cascadePathProcessor.classifyTreePaths(rootTreeNode);
        return rootTreeNode;
    }
}
