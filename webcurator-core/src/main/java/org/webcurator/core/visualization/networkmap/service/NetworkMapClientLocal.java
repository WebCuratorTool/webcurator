package org.webcurator.core.visualization.networkmap.service;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import org.webcurator.common.util.Utils;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.util.URLResolverFunc;
import org.webcurator.core.visualization.VisualizationAbstractProcessor;
import org.webcurator.core.visualization.VisualizationProcessorManager;
import org.webcurator.core.visualization.VisualizationProgressBar;
import org.webcurator.core.visualization.VisualizationProgressView;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.metadata.*;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessorWarc;
import org.webcurator.domain.model.core.HarvestResultDTO;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class NetworkMapClientLocal implements NetworkMapClient {
    private final static String regex = "\\d+";
    private final BDBNetworkMapPool pool;
    private final VisualizationProcessorManager visualizationProcessorManager;

    public NetworkMapClientLocal(BDBNetworkMapPool pool, VisualizationProcessorManager visualizationProcessorManager) {
        this.pool = pool;
        this.visualizationProcessorManager = visualizationProcessorManager;
    }

    @Override
    public NetworkMapResult initialIndex(long job, int harvestResultNumber) {
        VisualizationAbstractProcessor processor = null;
        try {
            processor = new IndexProcessorWarc(pool, job, harvestResultNumber);
            visualizationProcessorManager.startTask(processor);
        } catch (DigitalAssetStoreException | IOException e) {
            return NetworkMapResult.getInitialExtractorFailedResult();
        }
        return NetworkMapResult.getSuccessResult();
    }

    @Override
    public NetworkMapResult getDbVersion(long job, int harvestResultNumber) {
        NetworkDbVersionDTO versionDTO = new NetworkDbVersionDTO();
        versionDTO.setRetrieveResult(NetworkDbVersionDTO.RESULT_SUCCESS);
        versionDTO.setGlobalVersion(pool.getDbVersion());

        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        String currentVersion = "0.0.0";
        if (db == null) {
            versionDTO.setRetrieveResult(NetworkDbVersionDTO.RESULT_DB_NOT_EXIT);
        } else {
            String version = db.getDbVersionStamp();
            currentVersion = version == null ? "0.0.0" : version;
        }

        if (!currentVersion.equalsIgnoreCase(pool.getDbVersion())) {
            versionDTO.setRetrieveResult(NetworkDbVersionDTO.RESULT_NEED_REINDEX);
        }
        versionDTO.setCurrentVersion(currentVersion);

        String payload = this.obj2Json(versionDTO);

        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(payload);

        return result;
    }

//    @Override
//    public NetworkMapResult get(long job, int harvestResultNumber, String key) {
//        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
//        if (db == null) {
//            return NetworkMapResult.getDBMissingErrorResult();
//        }
//        NetworkMapResult result = new NetworkMapResult();
//        result.setPayload(db.get(key));
//        return result;
//    }

    @Override
    public NetworkMapResult getNode(long job, int harvestResultNumber, long id) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }
        NetworkMapResult result = new NetworkMapResult();
        NetworkMapNodeDTO networkMapNode = db.getUrl(id);
        result.setPayload(obj2Json(networkMapNode));
        return result;
    }

    @Override
    public NetworkMapResult getOutlinks(long job, int harvestResultNumber, long parentId) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }

        NetworkMapNodeDTO parentNode = db.getUrl(parentId);
        if (parentNode == null) {
            return NetworkMapResult.getDataNotExistResult("Could not find parent node, id: " + parentId);
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
    public NetworkMapResult searchUrl2CascadePaths(long job, int harvestResultNumber, String title, NetworkMapServiceSearchCommand searchCommand) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }
        if (searchCommand == null) {
            searchCommand = new NetworkMapServiceSearchCommand();
        }

        if (!Utils.isEmpty(title)) {
            title = new String(Base64.getDecoder().decode(title));
        }

        NetworkMapTreeNodeDTO rootTreeNode = this.searchUrlTreeNodes(job, harvestResultNumber, searchCommand);
        if (rootTreeNode == null) {
            return NetworkMapResult.getDataNotExistResult();
        }

//        NetworkMapCascadePath networkMapCascadePathProcessor = new NetworkMapCascadePath();
//        networkMapCascadePathProcessor.classifyTreePaths(rootTreeNode);

        List<NetworkMapTreeNodeDTO> returnedTreeNodes = new ArrayList<>();
        if (Utils.isEmpty(rootTreeNode.getTitle()) && rootTreeNode.getChildren().size() == 0) {
            returnedTreeNodes.add(rootTreeNode);
        } else {
            returnedTreeNodes = rootTreeNode.getChildren();
        }

        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(this.obj2Json(returnedTreeNodes));
        return result;
    }


    private NetworkMapTreeNodeDTO statisticTreeNodes(List<NetworkMapTreeNodeDTO> treeNodeDTOS) {
        final NetworkMapTreeNodeDTO result = new NetworkMapTreeNodeDTO();
        treeNodeDTOS.forEach(node -> {
            result.accumulate(node.getStatusCode(), node.getContentLength(), node.getContentType());
        });

        return result;
    }

    @Override
    public NetworkMapResult getAllDomains(long job, int harvestResultNumber) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }

        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(db.getRootDomainString());
        return result;
    }

    @Override
    public NetworkMapResult getSeedUrls(long job, int harvestResultNumber) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }

        List<Long> ids = db.getRootUrlIdList();
        if (ids == null) {
            return NetworkMapResult.getDataNotExistResult("Could not find seed urls");
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

        List<Long> ids = db.getMalformedUrlIdList();
        if (ids == null) {
            return NetworkMapResult.getDataNotExistResult("Could not find malformed urls");
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

        List<NetworkMapNodeDTO> urls = searchUrlDTOs(db, job, harvestResultNumber, searchCommand);

        String json = this.obj2Json(urls);
        urls.clear();

        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(json);
        return result;
    }

    public NetworkMapTreeNodeDTO searchUrlTreeNodes(long job, int harvestResultNumber, NetworkMapServiceSearchCommand searchCommand) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return null;
        }

        Map<Long, NetworkMapTreeNodeDTO> map = new HashMap<>();
        NetworkMapTreeNodeDTO rootTreeNode = new NetworkMapTreeNodeDTO();
        map.put((long) -1, rootTreeNode);

        List<NetworkMapNodeDTO> allNetworkMapNodes = this.searchUrlDTOs(job, harvestResultNumber, searchCommand);
        for (NetworkMapNodeDTO networkMapNode : allNetworkMapNodes) {
            NetworkMapTreeNodeDTO treeNode = new NetworkMapTreeNodeDTO();
            treeNode.copy(networkMapNode);
            treeNode.setUrl(networkMapNode.getUrl());
            treeNode.setTitle(networkMapNode.getUrl());
            long parentPathId = networkMapNode.getParentPathId();
            while (true) {
                NetworkMapTreeNodeDTO parentTreeNode;
                if (map.containsKey(parentPathId)) {
                    parentTreeNode = map.get(parentPathId);
                    parentTreeNode.getChildren().add(treeNode);
                    break;
                }

                NetworkMapTreeViewPath path = db.getTreeViewPath(parentPathId);
                if (path == null) {
                    log.error("parentPath does not exist, parentPathId={}", parentPathId);
                    break;
                }
                parentTreeNode = new NetworkMapTreeNodeDTO();
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

    public List<NetworkMapNodeDTO> searchUrlDTOs(long job, int harvestResultNumber, NetworkMapServiceSearchCommand searchCommand) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return null;
        }

        return searchUrlDTOs(db, job, harvestResultNumber, searchCommand);
    }

    public List<NetworkMapNodeDTO> searchUrlDTOs(BDBNetworkMap db, long job, int harvestResultNumber, NetworkMapServiceSearchCommand searchCommand) {
        CompiledSearchCommand compiledSearchCommand = CompiledSearchCommand.getInstance(searchCommand);

        long startTime = System.currentTimeMillis();

        final List<NetworkMapNodeDTO> urls = new ArrayList<>();
        long maxUrlSize = db.getUrlCount();
        for (long urlId = 0; urlId < maxUrlSize; urlId++) {
            String unl = db.getUrlString(urlId);
            if (Utils.isEmpty(unl)) {
                continue;
            }
            if (!compiledSearchCommand.isInclude(unl)) {
                continue;
            }
            NetworkMapNodeDTO networkMapNode = new NetworkMapNodeDTO();
            try {
                networkMapNode.toObjectFromUnl(unl);
            } catch (Exception e) {
                log.error("Failed to initial NetworkMapNodeDTO from unl: {}", unl, e);
                continue;
            }
            urls.add(networkMapNode);
        }

        long endTime = System.currentTimeMillis();

        log.debug("Search URLs time used: " + (endTime - startTime));
        return urls;
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

        List<String> urlNameList = new ArrayList<>();

        // Open the cursor.
        Cursor cursor = db.openCursor();
        DatabaseEntry foundKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();
        while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            String keyString = new String(foundKey.getData());
            if (!keyString.startsWith("url_")) {
                continue;
            }
            keyString = keyString.substring(4);

            if (keyString.toUpperCase().contains(substring.toUpperCase())) {
                urlNameList.add(keyString);
            }
        }
        //Close the cursor
        cursor.close();

        String json = obj2Json(urlNameList);

        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(json);
        return result;
    }

    @Override
    public NetworkMapResult getHopPath(long job, int harvestResultNumber, long id) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }

        List<NetworkMapNodeDTO> listHopPath = new ArrayList<>();
        NetworkMapNodeDTO curNode = db.getUrl(id);
        while (curNode != null) {
            listHopPath.add(curNode);
            long parentId = curNode.getParentId();
            if (parentId <= 0) {
                break;
            }
            curNode = db.getUrl(parentId);
        }
        String json = this.obj2Json(listHopPath);

        listHopPath.forEach(NetworkMapNodeDTO::clear);
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

        List<NetworkMapNodeDTO> hierarchyLinks = new ArrayList<>();
        for (long urlId : ids) {
            NetworkMapNodeDTO node = db.getUrl(urlId);
            if (node == null) {
                continue;
            }
            hierarchyLinks.add(node);
            for (long outlinkId : node.getOutlinks()) {
                NetworkMapNodeDTO outlink = db.getUrl(outlinkId);
                node.putChild(outlink);
            }
        }

        String json = this.obj2Json(hierarchyLinks);
        hierarchyLinks.forEach(NetworkMapNodeDTO::clear);
        hierarchyLinks.clear();

        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(json);
        return result;
    }

    @Override
    public NetworkMapResult getUrlByName(long job, int harvestResultNumber, NetworkMapUrl url) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }

        String urlName = url.getUrlName();
        NetworkMapNodeDTO networkMapNode = db.getUrlByUrlName(urlName);
        if (networkMapNode == null) {
            return NetworkMapResult.getDataNotExistResult();
        }

        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(this.obj2Json(networkMapNode));
        return result;
    }

    @Override
    public NetworkMapResult getUrlsByNames(long job, int harvestResultNumber, List<String> urlNameList) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }

        if (urlNameList == null) {
            return NetworkMapResult.getBadRequestResult();
        }

        List<NetworkMapNodeDTO> urlList = new ArrayList<>();
        for (String urlName : urlNameList) {
            NetworkMapNodeDTO node = db.getUrlByUrlName(urlName);
            if (node!=null){
                urlList.add(node);
            }
        }
        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(obj2Json(urlList));
        return result;
    }

    private String combineUrlResultFromArrayIDs(long job, int harvestResultNumber, List<Long> ids) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);

        if (ids == null || db == null) {
            return null;
        }

        final List<NetworkMapNodeDTO> result = new ArrayList<>();
        ids.forEach(childId -> {
            NetworkMapNodeDTO networkMapNode = db.getUrl(childId);
            if (networkMapNode != null) {
                result.add(networkMapNode);
            }
        });

        result.sort(Comparator.comparing(NetworkMapNodeDTO::getUrl));

        String json = this.obj2Json(result);
        result.clear();

        return json;
    }

    @Override
    public NetworkMapResult getProgress(long job, int harvestResultNumber) {
        VisualizationProgressBar progressBar = visualizationProcessorManager.getProgress(job, harvestResultNumber);
        if (progressBar == null) {
            return NetworkMapResult.getDataNotExistResult();
        }

        VisualizationProgressView progressView = new VisualizationProgressView(progressBar);
        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(this.obj2Json(progressView));
        return result;
    }

    @Override
    public NetworkMapResult getProcessingHarvestResultDTO(long job, int harvestResultNumber) {
        HarvestResultDTO hrDTO = visualizationProcessorManager.getHarvestResultDTO(job, harvestResultNumber);
        if (hrDTO == null) {
            return NetworkMapResult.getDataNotExistResult();
        }
        NetworkMapResult result = new NetworkMapResult();
        result.setPayload(this.obj2Json(hrDTO));
        return result;
    }
}

class CompiledSearchCommand {
    String domainLevel;
    List<SearchCommandItem> domainNameCondition = new ArrayList<>();
    List<SearchCommandItem> urlNameCondition = new ArrayList<>();
    List<SearchCommandItem> contentTypeCondition = new ArrayList<>();
    List<Integer> statusCodeCondition = new ArrayList<>();

    private CompiledSearchCommand(String domainLevel) {
        this.domainLevel = domainLevel;
    }

    public static CompiledSearchCommand getInstance(NetworkMapServiceSearchCommand searchCommand) {
        CompiledSearchCommand compiledSearchCommand = new CompiledSearchCommand(searchCommand.getDomainLevel());

        if (searchCommand.getDomainNames() != null && searchCommand.getDomainNames().size() > 0) {
            compiledSearchCommand.domainNameCondition = searchCommand.getDomainNames().stream().map(SearchCommandItem::new).collect(Collectors.toList());
        }

        if (searchCommand.getUrlNames() != null && searchCommand.getUrlNames().size() > 0) {
            compiledSearchCommand.urlNameCondition = searchCommand.getUrlNames().stream().map(SearchCommandItem::new).collect(Collectors.toList());
        }

        if (searchCommand.getContentTypes() != null && searchCommand.getContentTypes().size() > 0) {
            compiledSearchCommand.contentTypeCondition = searchCommand.getContentTypes().stream().map(SearchCommandItem::new).collect(Collectors.toList());
        }

        if (searchCommand.getStatusCodes() != null && searchCommand.getStatusCodes().size() > 0) {
            compiledSearchCommand.statusCodeCondition = new ArrayList<>(searchCommand.getStatusCodes());
        }
        return compiledSearchCommand;
    }

    public boolean isInclude(String unl) {
        String[] items = unl.split(NetworkMapNodeDTO.UNL_FIELDS_SEPARATOR);
        if (items.length != NetworkMapNodeDTO.UNL_FIELDS_COUNT_MAX) {
            return false;
        }

        boolean isInclude = isIncludedByUrlName(items[1]) &&
                isIncludedByContentType(items[9]) &&
                isIncludedByStatusCode(Integer.parseInt(items[10]));

        if (!isInclude) {
            return false;
        }

        String domainName = domainLevel != null && domainLevel.equals("high") ? NetworkMapNode.getTopDomainName(items[1]) : URLResolverFunc.url2domain(items[1]);
        return isIncludedByDomainName(domainName);
    }

    private boolean isIncludedByDomainName(String domainName) {
        if (domainNameCondition.size() == 0) {
            return true;
        }

        for (SearchCommandItem e : domainNameCondition) {
            if (e.match(domainName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isIncludedByUrlName(String urlName) {
        if (urlNameCondition.size() == 0) {
            return true;
        }

        for (SearchCommandItem e : urlNameCondition) {
            if (e.match(urlName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isIncludedByContentType(String contentType) {
        if (contentTypeCondition.size() == 0) {
            return true;
        }

        for (SearchCommandItem e : contentTypeCondition) {
            if (e.match(contentType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isIncludedByStatusCode(int statusCode) {
        if (statusCodeCondition.size() == 0) {
            return true;
        }

        for (Integer e : statusCodeCondition) {
            if (e == statusCode) {
                return true;
            }
        }
        return false;
    }

    public String getDomainLevel() {
        return domainLevel;
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