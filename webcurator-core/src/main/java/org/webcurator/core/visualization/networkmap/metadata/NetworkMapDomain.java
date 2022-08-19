package org.webcurator.core.visualization.networkmap.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Entity
public class NetworkMapDomain {
    public static final int DOMAIN_NAME_LEVEL_ROOT = 0; //All
    public static final int DOMAIN_NAME_LEVEL_HIGH = 1;
    public static final int DOMAIN_NAME_LEVEL_LOWER = 2;
    public static final int DOMAIN_NAME_LEVEL_STAT = 9;


    @PrimaryKey
    private long id;
    private String title;
    private String contentType;
    private int statusCode;

    private long parentId;
    private String parentTile;
    private boolean seed;
    private int level;

    /////////////////////////////////////////////////////////////////////////////////////////
    // 1. Domain: the total items of all urls contained in this domain.
    // 2. URL: the total items of all urls directly link to this url and the url itself
    private int totUrls = 0;
    private int totSuccess = 0;
    private int totFailed = 0;
    private long totSize = 0;
    ///////////////////////////////////////////////////////////////////////////////////////////


    private List<Long> outlinks = new ArrayList<>();

    private Map<String, NetworkMapDomain> children = new HashMap<>(); //Group by sub domains

    private List<NetworkMapDomain> statData = new ArrayList<>();

    public NetworkMapDomain() {
    }

    public NetworkMapDomain(int level, long id) {
        this.level = level;
        this.id = id;
    }

    @JsonIgnore
    public void addOutlink(long linkId) {
        if (this.id != linkId && !this.outlinks.contains(linkId)) {
            this.outlinks.add(linkId);
        }
    }

    @JsonIgnore
    public void addChildren(Collection<NetworkMapNodeUrlDTO> list, AtomicLong idGenerator, NetworkMapDomainManager domainManager) {
        Map<String, List<NetworkMapNodeUrlDTO>> mapGroupByDomainTitle = new HashMap<>();

        if (this.level == DOMAIN_NAME_LEVEL_ROOT) {
            mapGroupByDomainTitle = list.stream().collect(Collectors.groupingBy(NetworkMapNodeUrlDTO::getTopDomain));
        } else if (this.level == DOMAIN_NAME_LEVEL_HIGH) {
            mapGroupByDomainTitle = list.stream().collect(Collectors.groupingBy(NetworkMapNodeUrlDTO::getDomain));
        } else {
            return;
        }

        mapGroupByDomainTitle.forEach((k, v) -> {
            NetworkMapDomain domain = new NetworkMapDomain(this.level + 1, idGenerator.incrementAndGet());
            domain.setTitle(k);
            domain.setParentId(this.getId());
            domain.setParentTile(this.getTitle());
            domainManager.pushDomain(domain);//creating index for outlink adding
            domain.addChildren(v, idGenerator, domainManager);
            domain.addStatData(v);
            this.children.put(domain.getTitle(), domain);
            v.clear();
        });
        mapGroupByDomainTitle.clear();
    }

    @JsonIgnore
    public void addStatData(Collection<NetworkMapNodeUrlDTO> list) {
        this.accumulate(list);

        Map<String, List<NetworkMapNodeUrlDTO>> mapGroupByContentType = list.stream().collect(Collectors.groupingBy(NetworkMapNodeUrlDTO::getContentType));
        mapGroupByContentType.forEach((contentType, contentTypeList) -> {
            Map<Integer, List<NetworkMapNodeUrlDTO>> mapGroupByStatusCode = contentTypeList.stream().collect(Collectors.groupingBy(NetworkMapNodeUrlDTO::getStatusCode));
            mapGroupByStatusCode.forEach((k, v) -> {
                NetworkMapDomain domain = new NetworkMapDomain(DOMAIN_NAME_LEVEL_STAT, 0);
                domain.setTitle(this.getTitle());
                domain.setParentId(this.getId());
                domain.setParentTile(this.getTitle());
                domain.setContentType(contentType);
                domain.setStatusCode(k);
                domain.accumulate(v);
                this.statData.add(domain);
                v.clear();
            });
            mapGroupByStatusCode.clear();
            contentTypeList.clear();
        });
        mapGroupByContentType.clear();
    }

    @JsonIgnore
    private void accumulate(Collection<NetworkMapNodeUrlDTO> list) {
        list.forEach(e -> {
            if (e.isSuccess(e.getStatusCode())) {
                this.totSuccess += 1;
            } else {
                this.totFailed += 1;
            }
            this.totUrls += 1;
            this.totSize += e.getContentLength();
        });
    }

    @JsonIgnore
    public void clear() {
        this.outlinks.clear();
        this.children.values().forEach(NetworkMapDomain::clear);
        this.children.clear();
        this.statData.forEach(NetworkMapDomain::clear);
        this.statData.clear();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public String getParentTile() {
        return parentTile;
    }

    public void setParentTile(String parentTile) {
        this.parentTile = parentTile;
    }

    public boolean isSeed() {
        return seed;
    }

    public void setSeed(boolean seed) {
        this.seed = seed;
    }

    public int getTotUrls() {
        return totUrls;
    }

    public void setTotUrls(int totUrls) {
        this.totUrls = totUrls;
    }

    public int getTotSuccess() {
        return totSuccess;
    }

    public void setTotSuccess(int totSuccess) {
        this.totSuccess = totSuccess;
    }

    public int getTotFailed() {
        return totFailed;
    }

    public void setTotFailed(int totFailed) {
        this.totFailed = totFailed;
    }

    public long getTotSize() {
        return totSize;
    }

    public void setTotSize(long totSize) {
        this.totSize = totSize;
    }

    public List<Long> getOutlinks() {
        return outlinks;
    }

    public void setOutlinks(List<Long> outlinks) {
        this.outlinks = outlinks;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Collection<NetworkMapDomain> getChildren() {
        return children.values();
    }

    public void setChildren(Collection<NetworkMapDomain> children) {
        children.forEach(child -> {
            this.children.put(child.getTitle(), child);
        });
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<NetworkMapDomain> getStatData() {
        return statData;
    }

    public void setStatData(List<NetworkMapDomain> statData) {
        this.statData = statData;
    }
}
