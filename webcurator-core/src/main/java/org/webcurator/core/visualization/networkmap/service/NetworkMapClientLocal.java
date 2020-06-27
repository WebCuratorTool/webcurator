package org.webcurator.core.visualization.networkmap.service;

import org.webcurator.core.visualization.VisualizationProcessorQueue;
import org.webcurator.core.visualization.VisualizationProgressBar;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNode;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NetworkMapClientLocal implements NetworkMapClient {
    private final BDBNetworkMapPool pool;
    private final VisualizationProcessorQueue visualizationProcessorQueue;

    public NetworkMapClientLocal(BDBNetworkMapPool pool, VisualizationProcessorQueue visualizationProcessorQueue) {
        this.pool = pool;
        this.visualizationProcessorQueue = visualizationProcessorQueue;
    }

    @Override
    public NetworkMapResult get(long job, int harvestResultNumber, String key) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }
        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(db.get(key));
        return result;
    }

    @Override
    public NetworkMapResult getNode(long job, int harvestResultNumber, long id) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }
        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(db.get(id));
        return result;
    }

    @Override
    public NetworkMapResult getOutlinks(long job, int harvestResultNumber, long id) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }

        NetworkMapNode parentNode = this.getNodeEntity(db.get(id));
        if (parentNode == null) {
            return NetworkMapResult.getNodeNotExistResult("Could not find parent node, id: " + id);
        }

        NetworkMapResult result = new NetworkMapResult();
        String outlinks = combineUrlResultFromArrayIDs(job, harvestResultNumber, parentNode.getOutlinks());
        parentNode.clear();

        result.setPayload(outlinks);
        return result;
    }

    @Override
    public NetworkMapResult getChildren(long job, int harvestResultNumber, long id) {
        //TODO
        return new NetworkMapResult();
    }

    @Override
    public NetworkMapResult getAllDomains(long job, int harvestResultNumber) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }

        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(db.get(BDBNetworkMap.PATH_GROUP_BY_DOMAIN));
        return result;
    }

    @Override
    public NetworkMapResult getSeedUrls(long job, int harvestResultNumber) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }

        List<Long> ids = this.getArrayList(db.get(BDBNetworkMap.PATH_ROOT_URLS));
        if (ids == null) {
            return NetworkMapResult.getNodeNotExistResult("Could not find seed urls");
        }
        String seedUrls = combineUrlResultFromArrayIDs(job, harvestResultNumber, ids);
        ids.clear();

        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(seedUrls);
        return result;
    }

    @Override
    public NetworkMapResult getMalformedUrls(long job, int harvestResultNumber) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }

        List<Long> ids = this.getArrayList(db.get(BDBNetworkMap.PATH_MALFORMED_URLS));
        if (ids == null) {
            return NetworkMapResult.getNodeNotExistResult("Could not find malformed urls");
        }
        String malformedUrls = combineUrlResultFromArrayIDs(job, harvestResultNumber, ids);
        ids.clear();

        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(malformedUrls);
        return result;
    }

    @Override
    public NetworkMapResult searchUrl(long job, int harvestResultNumber, NetworkMapServiceSearchCommand searchCommand) {
        if (searchCommand == null) {
            return NetworkMapResult.getBadRequestResult("Search command could not be null");
        }

        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }

        final List<String> urls = new ArrayList<>();

        List<Long> ids = this.getArrayList(db.get(BDBNetworkMap.PATH_ROOT_URLS));
        searchUrlInternal(job, harvestResultNumber, db, searchCommand, ids, urls);

        String json = this.obj2Json(urls);
        urls.clear();

        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(json);
        return result;
    }

    @Override
    public NetworkMapResult searchUrlNames(long job, int harvestResultNumber, String substring) {
        if (substring == null) {
            return NetworkMapResult.getBadRequestResult("substring could not be null");
        }

        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }

        List<String> urlNameList = db.searchKeys(substring);
        String json = obj2Json(urlNameList);

        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(json);
        return result;
    }

    private void searchUrlInternal(long job, int harvestResultNumber, BDBNetworkMap db, NetworkMapServiceSearchCommand searchCommand, List<Long> linkIds, final List<String> result) {
        if (linkIds == null) {
            return;
        }

        for (long id : linkIds) {
            String urlStr = db.get(id);
            if (urlStr == null) {
                log.warn("Null value: job={}, harvestResultNumber={}, nodeId={}", job, harvestResultNumber, id);
                continue;
            }
            NetworkMapNode urlNode = getNodeEntity(urlStr);
            if (isIncluded(urlNode, searchCommand)) {
                result.add(urlStr);
            }

            searchUrlInternal(job, harvestResultNumber, db, searchCommand, urlNode.getOutlinks(), result);
        }
    }

    @Override
    public NetworkMapResult getHopPath(long job, int harvestResultNumber, long id) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }

        List<NetworkMapNode> listHopPath = new ArrayList<>();
        NetworkMapNode curNode = this.getNodeEntity(db.get(id));
        while (curNode != null) {
            listHopPath.add(curNode);
            long parentId = curNode.getParentId();
            if (parentId <= 0) {
                break;
            }
            curNode = this.getNodeEntity(db.get(parentId));
        }
        String json = this.obj2Json(listHopPath);

        listHopPath.forEach(NetworkMapNode::clear);
        listHopPath.clear();

        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(json);
        return result;
    }

    @Override
    public NetworkMapResult getHierarchy(long job, int harvestResultNumber, List<Long> ids) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }

        List<NetworkMapNode> hierarchyLinks = new ArrayList<>();
        for (long urlId : ids) {
            NetworkMapNode node = getNodeEntity(db.get(urlId));
            if (node == null) {
                continue;
            }
            hierarchyLinks.add(node);
            for (long outlinkId : node.getOutlinks()) {
                NetworkMapNode outlink = getNodeEntity(db.get(outlinkId));
                node.putChild(outlink);
            }
        }

        String json = this.obj2Json(hierarchyLinks);
        hierarchyLinks.forEach(NetworkMapNode::clear);
        hierarchyLinks.clear();

        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(json);
        return result;
    }

    @Override
    public NetworkMapResult getUrlByName(long job, int harvestResultNumber, String urlName) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }

        String keyId = db.get(urlName);
        if (keyId == null) {
            return NetworkMapResult.getNodeNotExistResult(String.format("Can not find Url Node: %d, %d, %s", job, harvestResultNumber, urlName));
        }

        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(db.get(keyId));
        return result;
    }

//    private void combineHierarchy(BDBNetworkMap db, Map<Long, NetworkMapNode> walkedNodes, long urlId) {
//        if (walkedNodes.containsKey(urlId)) {
//            return;
//        }
//
//        NetworkMapNode node = getNodeEntity(db.get(urlId));
//        if (node == null) {
//            return;
//        } else {
//            walkedNodes.put(urlId, node);
//        }
//
//        combineHierarchy(db, walkedNodes, node.getParentId());
//    }

    private String combineUrlResultFromArrayIDs(long job, int harvestResultNumber, List<Long> ids) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);

        if (ids == null || db == null) {
            return null;
        }

        final List<String> result = new ArrayList<>();
        ids.forEach(childId -> {
            String childStr = db.get(childId);
            if (childStr != null) {
                result.add(childStr);
            }
        });

        String json = this.obj2Json(result);
        result.clear();

        return json;
    }

    private boolean isIncluded(NetworkMapNode node, NetworkMapServiceSearchCommand searchCommand) {
        String domainName = searchCommand.getDomainLevel() != null && searchCommand.getDomainLevel().equals("high") ? node.getTopDomain() : node.getDomain();
        return isIncludedByDomainName(domainName, searchCommand) &&
                isIncludedByUrlName(node.getUrl(), searchCommand) &&
                isIncludedByContentType(node.getContentType(), searchCommand) &&
                isIncludedByStatusCode(node.getStatusCode(), searchCommand.getStatusCodes());
    }

    private boolean isIncludedByDomainName(String domainName, NetworkMapServiceSearchCommand searchCommand) {
        if (searchCommand.getDomainNames() == null || searchCommand.getDomainNames().size() == 0) {
            return true;
        }

        List<SearchCommandItem> domainNameCondition = searchCommand.getDomainNames().stream().map(SearchCommandItem::new).collect(Collectors.toList());
        for (SearchCommandItem e : domainNameCondition) {
            if (e.match(domainName)) {
                return true;
            }
        }

        return false;
    }

    private boolean isIncludedByUrlName(String urlName, NetworkMapServiceSearchCommand searchCommand) {
        if (searchCommand.getUrlNames() == null || searchCommand.getUrlNames().size() == 0) {
            return true;
        }

        List<SearchCommandItem> urlNameCondition = searchCommand.getUrlNames().stream().map(SearchCommandItem::new).collect(Collectors.toList());
        for (SearchCommandItem e : urlNameCondition) {
            if (e.match(urlName)) {
                return true;
            }
        }

        return false;
    }

    private boolean isIncludedByContentType(String contentType, NetworkMapServiceSearchCommand searchCommand) {
        if (searchCommand.getContentTypes() == null || searchCommand.getContentTypes().size() == 0) {
            return true;
        }

        List<SearchCommandItem> contentTypeCondition = searchCommand.getContentTypes().stream().map(SearchCommandItem::new).collect(Collectors.toList());
        for (SearchCommandItem e : contentTypeCondition) {
            if (e.match(contentType)) {
                return true;
            }
        }

        return false;
    }

    private boolean isIncludedByStatusCode(int statusCode, List<Integer> statusCodeCondition) {
        if (statusCodeCondition == null || statusCodeCondition.size() == 0) {
            return true;
        }

        for (int e : statusCodeCondition) {
            if (e == statusCode) {
                return true;
            }
        }

        return false;
    }

    @Override
    public VisualizationProgressBar getProgress(long targetInstanceId, int harvestResultNumber) {
        return visualizationProcessorQueue.getProgress(targetInstanceId, harvestResultNumber);
    }
}

class SearchCommandItem {
    private final static int STAR_TYPE_NONE = 0;
    private final static int STAR_TYPE_LEFT = 1;
    private final static int STAR_TYPE_RIGHT = 2;
    private final static int STAR_TYPE_BOTH = 3;
    private final int starType;
    private final String condition;

    public SearchCommandItem(String condition) {
        if (condition.startsWith("*") && condition.endsWith("*")) {
            starType = STAR_TYPE_BOTH;
        } else if (condition.startsWith("*")) {
            starType = STAR_TYPE_LEFT;
        } else if (condition.endsWith("*")) {
            starType = STAR_TYPE_RIGHT;
        } else {
            starType = STAR_TYPE_NONE;
        }

        this.condition = condition.replaceAll("\\*", "");
    }

    public boolean match(String e) {
        switch (starType) {
            case STAR_TYPE_NONE:
                return e.equals(this.condition);
            case STAR_TYPE_LEFT:
                return e.endsWith(this.condition);
            case STAR_TYPE_RIGHT:
                return e.startsWith(this.condition);
            case STAR_TYPE_BOTH:
                return e.contains(this.condition);
            default:
                return false;
        }
    }
}