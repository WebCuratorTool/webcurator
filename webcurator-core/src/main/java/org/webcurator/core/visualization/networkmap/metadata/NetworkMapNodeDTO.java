package org.webcurator.core.visualization.networkmap.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.webcurator.core.util.URLResolverFunc;

import java.util.ArrayList;
import java.util.List;

public class NetworkMapNodeDTO {
    public static final int SEED_TYPE_PRIMARY = 0;
    public static final int SEED_TYPE_SECONDARY = 1;
    public static final int SEED_TYPE_OTHER = 2;
    
    protected long id;
    protected String url;
    protected String domain;
    protected String topDomain;
    protected boolean isSeed = false; //true: if url equals seed or domain contains seed url.
    protected int seedType = -1;

    /////////////////////////////////////////////////////////////////////////////////////////
    // 1. Domain: the total items of all urls contained in this domain.
    // 2. URL: the total items of all urls directly link to this url and the url itself
    protected int totUrls = 0;
    protected int totSuccess = 0;
    protected int totFailed = 0;
    protected long totSize = 0;
    ///////////////////////////////////////////////////////////////////////////////////////////

    protected long domainId = -1; //default: no domain
    protected long contentLength;
    protected String contentType;
    protected int statusCode;
    protected long parentId = -1;
    protected long offset;
    protected long fetchTimeMs; //ms: time used to download the page
    protected String fileName;

    protected List<Long> outlinks = new ArrayList<>();
    protected List<NetworkMapNodeDTO> children = new ArrayList<>();

    protected String title;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getTopDomain() {
        return topDomain;
    }

    public void setTopDomain(String topDomain) {
        this.topDomain = topDomain;
    }

    public boolean isSeed() {
        return isSeed;
    }

    public void setSeed(boolean seed) {
        isSeed = seed;
    }

    public int getSeedType() {
        return seedType;
    }

    public void setSeedType(int seedType) {
        this.seedType = seedType;
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

    public long getDomainId() {
        return domainId;
    }

    public void setDomainId(long domainId) {
        this.domainId = domainId;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
//        this.totSize += contentLength;
    }

    public String getContentType() {
        if (contentType == null) {
            return "Unknown";
        }
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = URLResolverFunc.trimContentType(contentType);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getFetchTimeMs() {
        return fetchTimeMs;
    }

    public void setFetchTimeMs(long fetchTimeMs) {
        this.fetchTimeMs = fetchTimeMs;
    }

    public List<Long> getOutlinks() {
        return outlinks;
    }

    public void setOutlinks(List<Long> outlinks) {
        this.outlinks = outlinks;
    }

    public List<NetworkMapNodeDTO> getChildren() {
        return children;
    }

    public void setChildren(List<NetworkMapNodeDTO> children) {
        this.children = children;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @JsonIgnore
    public void clear() {
        this.outlinks.clear();
        this.children.forEach(NetworkMapNodeDTO::clear);
        this.children.clear();
    }

    @JsonIgnore
    public void putChild(NetworkMapNodeDTO e) {
        this.children.add(e);
    }
}
