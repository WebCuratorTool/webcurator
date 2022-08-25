package org.webcurator.core.visualization.networkmap.service;

import com.sleepycat.persist.EntityCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.common.util.Utils;
import org.webcurator.core.visualization.networkmap.bdb.BDBRepoHolder;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeFolderDTO;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeFolderEntity;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeUrlEntity;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class NetworkMapCascadePath {
    private static final Logger log = LoggerFactory.getLogger(NetworkMapCascadePath.class);
    private static final String ROOT_FOLDER_NAME = "All";

    /**
     * Change the recursive method to a loop to ignore stackoverflow problem
     *
     * @param db: the db of the target instance
     * @return long:  the ID of the root folder
     */
    public static long classifyTreePaths(BDBRepoHolder db) {
        EntityCursor<NetworkMapNodeUrlEntity> urlCursor = db.openUrlCursor();
        Map<Integer, Map<String, NetworkMapNodeFolderEntity>> mapAllLayers = new HashMap<>();
        int maxDepth = 0;
        int num = 0;

        //Populate the leaves
        for (NetworkMapNodeUrlEntity urlEntity : urlCursor) {
            int depth = getUrlDepth(urlEntity.getUrl());
            String folderName = getFolderNameFromUrlDepth(urlEntity.getUrl(), depth);
            if (depth <= 0 || folderName == null) {
                log.error("Unknown url: {}", urlEntity.getUrl());
                continue;
            }
            if (!mapAllLayers.containsKey(depth)) {
                Map<String, NetworkMapNodeFolderEntity> map = new HashMap<>();
                mapAllLayers.put(depth, map);
            }
            Map<String, NetworkMapNodeFolderEntity> tmpLayerMap = mapAllLayers.get(depth);

            if (!tmpLayerMap.containsKey(folderName)) {
                NetworkMapNodeFolderEntity folderEntity = new NetworkMapNodeFolderEntity();
                tmpLayerMap.put(folderName, folderEntity);
            }
            NetworkMapNodeFolderEntity folderEntity = tmpLayerMap.get(folderName);
            folderEntity.addSubUrl(urlEntity);
            folderEntity.setTitle(folderName);
            folderEntity.accumulate(urlEntity.getStatusCode(), urlEntity.getContentLength(), urlEntity.getContentType());
            urlEntity.clear();
            maxDepth = Math.max(maxDepth, depth);

            num++;

            if (num % 1000 == 0) {
                log.debug("Processing: {}", num);
            }
        }
        urlCursor.close();

        //Join the folder by layers based on the folder name
        AtomicLong atomicIdGeneratorFolder = new AtomicLong(0);
        for (int depth = maxDepth; depth >= 1; depth--) {
            Map<String, NetworkMapNodeFolderEntity> layerMap = mapAllLayers.computeIfAbsent(depth, k -> new HashMap<>());
            Map<String, NetworkMapNodeFolderEntity> parentLayerMap = mapAllLayers.computeIfAbsent(depth - 1, k -> new HashMap<>());

            for (String folderName : layerMap.keySet()) {
                String parentFolderName = getFolderNameFromUrlDepth(folderName, depth - 1);
                log.debug("Depth: {}, parentFolderName={}", depth, parentFolderName);

                NetworkMapNodeFolderEntity currFolderEntity = layerMap.get(folderName);
                NetworkMapNodeFolderEntity parentNode = parentLayerMap.get(parentFolderName);
                if (parentNode == null) {
                    parentNode = new NetworkMapNodeFolderEntity();
                    parentNode.setTitle(parentFolderName);
                    parentLayerMap.put(parentFolderName, parentNode);
                }

                if (currFolderEntity.getSubFolderList().size() + currFolderEntity.getSubUrlList().size() == 1) {
                    //Raise the children of the current node to it's parent's node, and delete the current entity
                    parentNode.getSubUrlList().addAll(currFolderEntity.getSubUrlList());
                    parentNode.getSubFolderList().addAll(currFolderEntity.getSubFolderList());
                    parentNode.accumulate(currFolderEntity);
                    db.tblFolder.deleteById(currFolderEntity.getId());
                } else {
                    currFolderEntity.setId(atomicIdGeneratorFolder.incrementAndGet());
                    db.updateFolder(currFolderEntity);
                    parentNode.accumulate(currFolderEntity);
                    parentNode.addSubFolder(currFolderEntity);
                }
            }
            layerMap.values().forEach(NetworkMapNodeFolderEntity::clear);
            layerMap.clear();
        }

        // Get the first level as the root nodes
        Map<String, NetworkMapNodeFolderEntity> rootFolders = mapAllLayers.computeIfAbsent(0, k -> new HashMap<>());
        if (!rootFolders.containsKey(ROOT_FOLDER_NAME)) {
            log.error("Failed to build the folder tree, the root folders is empty.");
            return -1;
        }
        NetworkMapNodeFolderEntity rootFolder = rootFolders.get(ROOT_FOLDER_NAME);
        rootFolder.setId(atomicIdGeneratorFolder.incrementAndGet());
        db.updateFolder(rootFolder);
        log.debug("classified, id of the root folder: {}", rootFolder.getId());
        return rootFolder.getId();
    }

    public static int getUrlDepth(String url) {
        int depth = 0;
        if (Utils.isEmpty(url)) {
            return depth;
        }

        int lenParentTitle = url.indexOf("://");
        if (lenParentTitle > 0) {
            lenParentTitle += 3;
        } else {
            return depth;
        }

        while (true) {
            int nextSlashPosition = url.indexOf('/', lenParentTitle + 1);
            if (nextSlashPosition < 0) {
                break;
            }
            depth += 1;
            lenParentTitle = nextSlashPosition;
        }

        while (true) {
            int questionMarkPosition = url.indexOf('?', lenParentTitle + 1);
            if (questionMarkPosition < 0) {
                break;
            }
            depth += 1;
            lenParentTitle = questionMarkPosition;
        }

        while (true) {
            int requestParamSepPosition = url.indexOf('&', lenParentTitle + 1);
            if (requestParamSepPosition < 0) {
                break;
            }
            depth += 1;
            lenParentTitle = requestParamSepPosition;
        }
        return depth;
    }

    public static String getFolderNameFromUrlDepth(String url, int targetDepth) {
        int depth = 0;
        if (targetDepth == 0 || Utils.isEmpty(url)) {
            return ROOT_FOLDER_NAME;
        }

        int lenParentTitle = url.indexOf("://");
        if (lenParentTitle > 0) {
            lenParentTitle += 3;
        } else {
            return null;
        }

        while (true) {
            int nextSlashPosition = url.indexOf('/', lenParentTitle + 1);
            if (nextSlashPosition < 0) {
                break;
            }
            depth += 1;
            if (depth == targetDepth) {
                return url.substring(0, nextSlashPosition);
            }
            lenParentTitle = nextSlashPosition;
        }

        while (true) {
            int questionMarkPosition = url.indexOf('?', lenParentTitle + 1);
            if (questionMarkPosition < 0) {
                break;
            }
            depth += 1;
            if (depth == targetDepth) {
                return url.substring(0, questionMarkPosition);
            }
            lenParentTitle = questionMarkPosition;
        }

        while (true) {
            int requestParamSepPosition = url.indexOf('&', lenParentTitle + 1);
            if (requestParamSepPosition < 0) {
                break;
            }
            depth += 1;
            if (depth == targetDepth) {
                return url.substring(0, requestParamSepPosition);
            }
            lenParentTitle = requestParamSepPosition;
        }
        return url;
    }


    /**
     * Keep this method for small number of nodes
     *
     * @param rootTreeNode: the folder nodes to be classified based on foldername
     */
    public void classifyTreePaths(NetworkMapNodeFolderDTO rootTreeNode) {
        log.debug("classifyTreeViewByPathNames: title={}, url={}", rootTreeNode.getTitle(), rootTreeNode.getUrl());

        //The terminal node
        if (rootTreeNode.getChildren().size() == 1) {
            rootTreeNode.copy(rootTreeNode.getChildren().get(0));
            rootTreeNode.setTitle(rootTreeNode.getUrl());
            rootTreeNode.setFolder(false);
            rootTreeNode.setLazy(false);
            rootTreeNode.getChildren().clear();
            return;
        }

        //Calculate the title
        final String currentParentTitle = rootTreeNode.getTitle();

        rootTreeNode.getChildren().forEach(node -> {
            String title = getNextTitle(currentParentTitle, node.getUrl());
            node.setTitle(title);
        });


        Map<String, List<NetworkMapNodeFolderDTO>> mapClassifiedByTitle = rootTreeNode.getChildren().stream().collect(Collectors.groupingBy(NetworkMapNodeFolderDTO::getTitle));
        rootTreeNode.getChildren().clear();
        for (String subTreeNodeTitle : mapClassifiedByTitle.keySet()) {
            List<NetworkMapNodeFolderDTO> subTreeNodeChildren = mapClassifiedByTitle.get(subTreeNodeTitle);

            NetworkMapNodeFolderDTO subTreeNode = new NetworkMapNodeFolderDTO();
            subTreeNode.setTitle(subTreeNodeTitle);
            subTreeNode.setChildren(subTreeNodeChildren);

            rootTreeNode.getChildren().add(subTreeNode);

            //Need to recurse because the title changed
            this.classifyTreePaths(subTreeNode);
        }
        mapClassifiedByTitle.clear();


        //Uplift
        if (rootTreeNode.getChildren().size() == 1) {
            NetworkMapNodeFolderDTO subTreeNode = rootTreeNode.getChildren().get(0);
            rootTreeNode.copy(subTreeNode);
            rootTreeNode.setChildren(subTreeNode.getChildren());
        }

        //Summarize total capacity
        this.statisticTreeNodes(rootTreeNode);

        rootTreeNode.setFolder(rootTreeNode.getChildren().size() > 0);
        rootTreeNode.setLazy(true);

        //Sort the return result
        rootTreeNode.getChildren().sort(Comparator.comparing(NetworkMapNodeFolderDTO::getTitle));
    }

    public String getNextTitle(String parentTile, String url) {
        if (Utils.isEmpty(url)) {
            return url;
        }

        int lenParentTitle;
        if (Utils.isEmpty(parentTile)) {
            lenParentTitle = url.indexOf("://");
            if (lenParentTitle > 0) {
                lenParentTitle += 3;
            } else {
                lenParentTitle = 0;
            }
        } else {
            lenParentTitle = parentTile.length();
        }

        int nextSlashPosition = url.indexOf('/', lenParentTitle + 1);
        if (nextSlashPosition > 0) {
            return url.substring(0, nextSlashPosition + 1);
        }
        int questionMarkPosition = url.indexOf('?', lenParentTitle + 1);
        if (questionMarkPosition > 0) {
            return url.substring(0, questionMarkPosition + 1);
        }
        int requestParamSepPosition = url.indexOf('&', lenParentTitle + 1);
        if (requestParamSepPosition > 0) {
            return url.substring(0, requestParamSepPosition + 1);
        }

        return url;
    }


    public void summarize(NetworkMapNodeFolderDTO rootTreeNode) {
        if (rootTreeNode.getChildren().size() == 0) {
            rootTreeNode.setFolder(false);
            rootTreeNode.setLazy(false);
            rootTreeNode.setZero();
            rootTreeNode.accumulate();
            return;
        }

        rootTreeNode.getChildren().forEach(this::summarize);
        this.statisticTreeNodes(rootTreeNode);

        rootTreeNode.setFolder(true);
        rootTreeNode.setLazy(false);
    }


    public void statisticTreeNodes(final NetworkMapNodeFolderDTO node) {
        node.setZero();
        node.getChildren().forEach(node::accumulate);
    }
}
