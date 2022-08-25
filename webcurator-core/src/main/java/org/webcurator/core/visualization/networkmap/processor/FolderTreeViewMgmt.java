package org.webcurator.core.visualization.networkmap.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.common.util.Utils;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.bdb.BDBRepoHolder;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapAccessPropertyEntity;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeFolderDTO;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeFolderEntity;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeUrlEntity;
import org.webcurator.core.visualization.networkmap.service.NetworkMapCascadePath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderTreeViewMgmt {
    private final static Logger log = LoggerFactory.getLogger(FolderTreeViewMgmt.class);
    private final BDBNetworkMapPool pool;

    public FolderTreeViewMgmt(BDBNetworkMapPool pool) {
        this.pool = pool;
    }

    public List<NetworkMapNodeFolderDTO> queryRootFolderList(long job, int harvestResultNumber) {
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

    public List<NetworkMapNodeFolderDTO> queryFolderListWithTitle(long job, int harvestResultNumber, String title) {
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

    public List<NetworkMapNodeFolderDTO> queryFolderList(long job, int harvestResultNumber, long parentFolderId) {
        BDBRepoHolder db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return null;
        }
        NetworkMapNodeFolderEntity parentFolderEntity = db.getFolderById(parentFolderId);
        return queryFolderList(job, harvestResultNumber, parentFolderEntity);
    }

    public List<NetworkMapNodeFolderDTO> queryFolderList(long job, int harvestResultNumber, NetworkMapNodeFolderEntity parentFolderEntity) {
        if (parentFolderEntity == null) {
            return null;
        }

        BDBRepoHolder db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return null;
        }
        List<NetworkMapNodeFolderDTO> listFolderDTO = new ArrayList<>();

        for (long id : parentFolderEntity.getSubFolderList()) {
            NetworkMapNodeFolderEntity folderEntity = db.getFolderById(id);
            NetworkMapNodeFolderDTO dto = new NetworkMapNodeFolderDTO();
            dto.copy(folderEntity);
            dto.setFolder(true);
            dto.setLazy(true);
            dto.setTitle(folderEntity.getTitle());
            listFolderDTO.add(dto);
        }

        for (long id : parentFolderEntity.getSubUrlList()) {
            NetworkMapNodeUrlEntity urlEntity = db.getUrlById(id);
            NetworkMapNodeFolderDTO dto = new NetworkMapNodeFolderDTO();
            dto.copy(urlEntity);
            dto.setFolder(false);
            dto.setLazy(false);
            dto.setTitle(urlEntity.getUrl());
            dto.setZero();
            listFolderDTO.add(dto);
        }
        return listFolderDTO;
    }

    public NetworkMapNodeFolderDTO createFolderTreeView(long job, int harvestResultNumber, List<NetworkMapNodeUrlEntity> allNetworkMapNodes) {
        BDBRepoHolder db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return null;
        }

        Map<Long, NetworkMapNodeFolderDTO> map = new HashMap<>();
        NetworkMapNodeFolderDTO rootTreeNode = new NetworkMapNodeFolderDTO();
        map.put((long) -1, rootTreeNode);

        for (NetworkMapNodeUrlEntity networkMapNode : allNetworkMapNodes) {
            NetworkMapNodeFolderDTO treeNode = new NetworkMapNodeFolderDTO();
            treeNode.copy(networkMapNode);
            treeNode.setUrl(networkMapNode.getUrl());
            treeNode.setTitle(networkMapNode.getUrl());
            long parentPathId = networkMapNode.getParentPathId();
            while (true) {
                NetworkMapNodeFolderDTO parentTreeNode;
                if (map.containsKey(parentPathId)) {
                    parentTreeNode = map.get(parentPathId);
                    parentTreeNode.getChildren().add(treeNode);
                    break;
                }

                NetworkMapNodeFolderEntity path = db.getFolderById(parentPathId);
                if (path == null) {
                    log.error("parentPath does not exist, parentPathId={}", parentPathId);
                    break;
                }
                parentTreeNode = new NetworkMapNodeFolderDTO();
                parentTreeNode.setTitle(path.getTitle());
                parentTreeNode.getChildren().add(treeNode);

                map.put(parentPathId, parentTreeNode);

                parentPathId = path.getParentPathId();
                treeNode = parentTreeNode;
            }
        }
        map.clear();

        if (Utils.isEmpty(rootTreeNode.getTitle()) && rootTreeNode.getChildren().size() == 1) {
            rootTreeNode = rootTreeNode.getChildren().get(0);
        }

        NetworkMapCascadePath cascadePathProcessor = new NetworkMapCascadePath();
        cascadePathProcessor.summarize(rootTreeNode);

        return rootTreeNode;
    }
}
