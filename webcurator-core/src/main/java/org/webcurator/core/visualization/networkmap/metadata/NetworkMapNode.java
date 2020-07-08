package org.webcurator.core.visualization.networkmap.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.webcurator.core.visualization.networkmap.NetworkMapDomainSuffix;
import org.webcurator.core.util.ApplicationContextFactory;
import org.webcurator.core.util.URLResolverFunc;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public class NetworkMapNode {
    public static final int SEED_TYPE_PRIMARY = 0;
    public static final int SEED_TYPE_SECONDARY = 1;
    public static final int SEED_TYPE_OTHER = 2;

    private static NetworkMapDomainSuffix topDomainParser = null;

    static {
        ApplicationContext ctx = ApplicationContextFactory.getApplicationContext();
        if (ctx != null) {
            topDomainParser = ctx.getBean(NetworkMapDomainSuffix.class);
        } else {
            topDomainParser = new NetworkMapDomainSuffix();
        }
    }

    protected long id;
    protected String url;
    protected String domain;
    protected String topDomain;
    @JsonIgnore
    protected String viaUrl;
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

    @JsonIgnore
    protected boolean hasOutlinks; //the number of outlinks>0
    @JsonIgnore
    protected boolean requestParseFlag = false;
    @JsonIgnore
    protected boolean responseParseFlag = false;
    @JsonIgnore
    protected boolean metadataParseFlag = false;

    protected List<Long> outlinks = new ArrayList<>();
    protected List<NetworkMapNode> children = new ArrayList<>();

    protected String title;

    public NetworkMapNode() {
    }

    public NetworkMapNode(long id) {
        this.id = id;
    }

    @JsonIgnore
    public void clear() {
        this.outlinks.clear();
        this.children.forEach(NetworkMapNode::clear);
        this.children.clear();
    }

    @JsonIgnore
    public void addOutlink(NetworkMapNode outlink) {
        if (this.id != outlink.getId() && !this.outlinks.contains(outlink.getId())) {
            this.outlinks.add(outlink.getId());
            this.accumulate(outlink.getStatusCode(), outlink.getContentLength(), outlink.getContentType());
        }
    }

    @JsonIgnore
    public void addOutlink(long linkId) {
        if (this.id != linkId && !this.outlinks.contains(linkId)) {
            this.outlinks.add(linkId);
        }
    }

    @JsonIgnore
    public void increaseTotUrls(int totUrls) {
        this.totUrls += totUrls;
    }

    @JsonIgnore
    public void increaseTotSuccess(int totSuccess) {
        this.totSuccess += totSuccess;
    }

    @JsonIgnore
    public void increaseTotFailed(int totFailed) {
        this.totFailed += totFailed;
    }

    @JsonIgnore
    public void increaseTotSize(long totSize) {
        this.totSize += totSize;
    }

    @JsonIgnore
    public void accumulate(int statusCode, long contentLength, String contentType) {
        this.increaseTotSize(contentLength);
        if (isSuccess(statusCode)) {
            this.increaseTotSuccess(1);
        } else {
            this.increaseTotFailed(1);
        }
        this.increaseTotUrls(1);
    }

    @JsonIgnore
    public void accumulate(List<NetworkMapNode> list) {
        if (list == null) {
            return;
        }

        list.forEach(e -> {
            this.accumulate(e.getStatusCode(), e.getContentLength(), e.getContentType());
        });
    }

    @JsonIgnore
    public void putChild(NetworkMapNode e) {
        this.children.add(e);
    }

    @JsonIgnore
    public void clearChildren() {
        this.children.clear(); //Not clear the grand children
    }

    @JsonIgnore
    public boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 400;
    }

    @JsonIgnore
    public boolean isFinished() {
        if (fileName.indexOf("mod~import~file") > 0) { //For warc files imported from local files, there are only response records
            return responseParseFlag;
        } else {
            return requestParseFlag && responseParseFlag && metadataParseFlag;
        }
    }

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

    @JsonIgnore
    public void setUrlAndDomain(String url) {
        this.url = url;
        String host = URLResolverFunc.url2domain(url);
        if (host == null) {
            this.domain = "Unknown";
            this.topDomain = "Unknown";
        } else {
            this.domain = host;
            this.topDomain = topDomainParser.getTopDomainName(host);
        }
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

    @JsonIgnore
    public String getViaUrl() {
        return viaUrl;
    }

    @JsonIgnore
    public void setViaUrl(String viaUrl) {
        this.viaUrl = viaUrl;
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

    @JsonIgnore
    public boolean isHasOutlinks() {
        return hasOutlinks;
    }

    @JsonIgnore
    public void setHasOutlinks(boolean hasOutlinks) {
        this.hasOutlinks = hasOutlinks;
    }

    @JsonIgnore
    public boolean isRequestParseFlag() {
        return requestParseFlag;
    }

    @JsonIgnore
    public void setRequestParseFlag(boolean requestParseFlag) {
        this.requestParseFlag = requestParseFlag;
    }

    @JsonIgnore
    public boolean isResponseParseFlag() {
        return responseParseFlag;
    }

    @JsonIgnore
    public void setResponseParseFlag(boolean responseParseFlag) {
        this.responseParseFlag = responseParseFlag;
    }

    @JsonIgnore
    public boolean isMetadataParseFlag() {
        return metadataParseFlag;
    }

    @JsonIgnore
    public void setMetadataParseFlag(boolean metadataParseFlag) {
        this.metadataParseFlag = metadataParseFlag;
    }

    public List<Long> getOutlinks() {
        return outlinks;
    }

    public void setOutlinks(List<Long> outlinks) {
        this.outlinks = outlinks;
    }

    public List<NetworkMapNode> getChildren() {
        return children;
    }

    public void setChildren(List<NetworkMapNode> children) {
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


    public static NetworkMapNode getNodeEntity(String json) {
        if (json == null) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, NetworkMapNode.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String obj2Json(Object obj) {
        String json = "{}";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            json = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }

    @JsonIgnore
    public String getUnlString() {
        String strOutlinks = outlinks.stream().map(outlink -> {
            return Long.toString(outlink);
        }).collect(Collectors.joining(","));
        return String.format("%d %s %s %s %d %d %d %d %d %d %d %s %s %d %d %d %s %b [%s]",
                id, url, domain, topDomain, seedType, totUrls, totSuccess, totFailed, totSize,
                domainId, contentLength, contentType, statusCode, parentId, offset, fetchTimeMs,
                fileName, isSeed, strOutlinks);
    }
}
