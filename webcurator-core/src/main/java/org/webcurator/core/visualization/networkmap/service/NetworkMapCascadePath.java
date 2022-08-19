package org.webcurator.core.visualization.networkmap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.common.util.Utils;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeFolderDTO;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NetworkMapCascadePath {
    private static final Logger log = LoggerFactory.getLogger(NetworkMapCascadePath.class);

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
