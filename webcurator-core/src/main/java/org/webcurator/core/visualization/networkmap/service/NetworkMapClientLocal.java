package org.webcurator.core.visualization.networkmap.service;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.visualization.VisualizationAbstractProcessor;
import org.webcurator.core.visualization.VisualizationProcessorManager;
import org.webcurator.core.visualization.VisualizationProgressBar;
import org.webcurator.core.visualization.VisualizationProgressView;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMap;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeDTO;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessorWarc;
import org.webcurator.domain.model.core.HarvestResultDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
        String unlString = db.get(id);
        NetworkMapNodeDTO networkMapNode = this.unlString2NetworkMapNode(unlString);
        result.setPayload(obj2Json(networkMapNode));
        return result;
    }

    @Override
    public NetworkMapResult getOutlinks(long job, int harvestResultNumber, long id) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }

        NetworkMapNodeDTO parentNode = this.unlString2NetworkMapNode(db.get(id));
        if (parentNode == null) {
            return NetworkMapResult.getDataNotExistResult("Could not find parent node, id: " + id);
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

        List<Long> ids = this.getArrayList(db.get(BDBNetworkMap.PATH_MALFORMED_URLS));
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

        CompiledSearchCommand compiledSearchCommand = CompiledSearchCommand.getInstance(searchCommand);

        long startTime = System.currentTimeMillis();

        final List<NetworkMapNodeDTO> urls = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.openCursor();
            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();
            while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                String keyString = new String(foundKey.getData());
                if (keyString.matches(regex)) {
                    String dataString = new String(foundData.getData());
                    if (compiledSearchCommand.isInclude(dataString)) {
                        NetworkMapNodeDTO networkMapNode = this.unlString2NetworkMapNode(dataString);
                        urls.add(networkMapNode);
                    }
                    log.debug(dataString);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        long endTime = System.currentTimeMillis();
        log.debug("Search URLs time used: " + (endTime - startTime));

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

    @Override
    public NetworkMapResult getHopPath(long job, int harvestResultNumber, long id) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }

        List<NetworkMapNodeDTO> listHopPath = new ArrayList<>();
        NetworkMapNodeDTO curNode = this.unlString2NetworkMapNode(db.get(id));
        while (curNode != null) {
            listHopPath.add(curNode);
            long parentId = curNode.getParentId();
            if (parentId <= 0) {
                break;
            }
            curNode = this.unlString2NetworkMapNode(db.get(parentId));
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
            NetworkMapNodeDTO node = this.unlString2NetworkMapNode(db.get(urlId));
            if (node == null) {
                continue;
            }
            hierarchyLinks.add(node);
            for (long outlinkId : node.getOutlinks()) {
                NetworkMapNodeDTO outlink = this.unlString2NetworkMapNode(db.get(outlinkId));
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
    public NetworkMapResult getUrlByName(long job, int harvestResultNumber, String urlName) {
        BDBNetworkMap db = pool.getInstance(job, harvestResultNumber);
        if (db == null) {
            return NetworkMapResult.getDBMissingErrorResult();
        }

        String keyId = db.get(urlName);
        if (keyId == null) {
            return NetworkMapResult.getDataNotExistResult(String.format("Can not find Url Node: %d, %d, %s", job, harvestResultNumber, urlName));
        }

        String unlString = db.get(keyId);
        NetworkMapNodeDTO networkMapNode = this.unlString2NetworkMapNode(unlString);

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
            String keyId = db.get(urlName);
            if (keyId == null) {
                continue;
            }

            String nodeStr = db.get(keyId);
            if (nodeStr == null) {
                continue;
            }
            NetworkMapNodeDTO node = this.unlString2NetworkMapNode(nodeStr);
            urlList.add(node);
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
            String childStr = db.get(childId);
            NetworkMapNodeDTO networkMapNode = this.unlString2NetworkMapNode(childStr);
            if (networkMapNode != null) {
                result.add(networkMapNode);
            }
        });

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
        String[] items = unl.split(" ");
        if (items.length != NetworkMapClientLocal.MAX_URL_UNL_FIELDS_COUNT) {
            return false;
        }

        String domainName = domainLevel != null && domainLevel.equals("high") ? items[3] : items[2];
        return isIncludedByDomainName(domainName) &&
                isIncludedByUrlName(items[1]) &&
                isIncludedByContentType(items[11]) &&
                isIncludedByStatusCode(Integer.parseInt(items[12]));
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