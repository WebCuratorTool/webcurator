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
public class NetworkMapNode extends NetworkMapNodeDTO {
    private static NetworkMapDomainSuffix topDomainParser = null;

    static {
        ApplicationContext ctx = ApplicationContextFactory.getApplicationContext();
        if (ctx != null) {
            topDomainParser = ctx.getBean(NetworkMapDomainSuffix.class);
        } else {
            topDomainParser = new NetworkMapDomainSuffix();
        }
    }

    @JsonIgnore
    protected String viaUrl;
    @JsonIgnore
    protected boolean hasOutlinks; //the number of outlinks>0
    @JsonIgnore
    protected boolean requestParseFlag = false;
    @JsonIgnore
    protected boolean responseParseFlag = false;
    @JsonIgnore
    protected boolean metadataParseFlag = false;

    public NetworkMapNode() {
    }

    public NetworkMapNode(long id) {
        this.id = id;
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

    @JsonIgnore
    public String getViaUrl() {
        return viaUrl;
    }

    @JsonIgnore
    public void setViaUrl(String viaUrl) {
        this.viaUrl = viaUrl;
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
