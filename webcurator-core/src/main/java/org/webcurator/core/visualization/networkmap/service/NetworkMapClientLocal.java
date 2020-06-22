package org.webcurator.core.visualization.networkmap.service;

import org.webcurator.core.visualization.VisualizationProgressBar;
import org.webcurator.core.visualization.networkmap.WCTResourceIndexer;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NetworkMapClientLocal implements NetworkMapClient {
    private final BDBNetworkMapPool pool;

    public NetworkMapClientLocal(BDBNetworkMapPool pool) {
        this.pool = pool;
    }

    @Override
    public String get(long job, int harvestResultNumber, String key) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        return db.get(key);
    }

    @Override
    public String getNode(long job, int harvestResultNumber, long id) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        return db.get(id);
    }

    @Override
    public String getOutlinks(long job, int harvestResultNumber, long id) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);

        NetworkMapNode parentNode = this.getNodeEntity(db.get(id));
        if (parentNode == null) {
            return null;
        }

        String result = combineUrlResultFromArrayIDs(job, harvestResultNumber, parentNode.getOutlinks());
        parentNode.clear();

        return result;
    }

    @Override
    public String getChildren(long job, int harvestResultNumber, long id) {
        //TODO
        return "{}";
    }

    @Override
    public String getAllDomains(long job, int harvestResultNumber) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);

        return db.get(BDBNetworkMap.PATH_GROUP_BY_DOMAIN);
    }

    @Override
    public String getSeedUrls(long job, int harvestResultNumber) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);

        List<Long> ids = this.getArrayList(db.get(BDBNetworkMap.PATH_ROOT_URLS));
        String result = combineUrlResultFromArrayIDs(job, harvestResultNumber, ids);
        ids.clear();
        return result;
    }

    @Override
    public String getMalformedUrls(long job, int harvestResultNumber) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);

        List<Long> ids = this.getArrayList(db.get(BDBNetworkMap.PATH_MALFORMED_URLS));
        String result = combineUrlResultFromArrayIDs(job, harvestResultNumber, ids);
        ids.clear();
        return result;
    }

    @Override
    public String searchUrl(long job, int harvestResultNumber, NetworkMapServiceSearchCommand searchCommand) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (searchCommand == null || db == null) {
            return null;
        }

        final List<String> result = new ArrayList<>();

        List<Long> ids = this.getArrayList(db.get(BDBNetworkMap.PATH_ROOT_URLS));
        searchUrlInternal(job, harvestResultNumber, db, searchCommand, ids, result);

        String json = this.obj2Json(result);
        result.clear();

        return json;
    }

    @Override
    public List<String> searchUrlNames(long job, int harvestResultNumber, String substring) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null || substring == null) {
            return null;
        }

        return db.searchKeys(substring);
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
    public String getHopPath(long job, int harvestResultNumber, long id) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return null;
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

        return json;
    }

    @Override
    public String getHierarchy(long job, int harvestResultNumber, List<Long> ids) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        List<NetworkMapNode> result = new ArrayList<>();
        for (long urlId : ids) {
            NetworkMapNode node = getNodeEntity(db.get(urlId));
            if (node == null) {
                continue;
            }
            result.add(node);
            for (long outlinkId : node.getOutlinks()) {
                NetworkMapNode outlink = getNodeEntity(db.get(outlinkId));
                node.putChild(outlink);
            }
        }

        String json = this.obj2Json(result);
        result.forEach(NetworkMapNode::clear);
        result.clear();
        return json;
    }

    @Override
    public String getUrlByName(long job, int harvestResultNumber, String urlName) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        String keyId = db.get(urlName);
        if (keyId == null) {
            log.warn("Can not find Url Node: {} {} {}", job, harvestResultNumber, urlName);
            return null;
        }
        return db.get(keyId);
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
        return WCTResourceIndexer.getProgress(targetInstanceId, harvestResultNumber);
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