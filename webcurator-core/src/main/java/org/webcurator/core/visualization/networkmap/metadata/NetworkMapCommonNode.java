package org.webcurator.core.visualization.networkmap.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.webcurator.common.util.Utils;

public class NetworkMapCommonNode {
    protected long id = -1;
    /////////////////////////////////////////////////////////////////////////////////////////
    // 1. Domain: the total items of all urls contained in this domain.
    // 2. URL: the total items of all urls directly link to this url and the url itself
    protected int totUrls = 0;
    protected int totSuccess = 0;
    protected int totFailed = 0;
    protected long totSize = 0;
    ///////////////////////////////////////////////////////////////////////////////////////////

    protected int outlinkNum=0;

    protected long domainId = -1; //default: no domain
    protected long contentLength;
    protected String contentType;
    protected int statusCode;

    protected boolean isSeed = false; //true: if url equals seed or domain contains seed url.
    protected int seedType = -1;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getOutlinkNum() {
        return outlinkNum;
    }

    public void setOutlinkNum(int outlinkNum) {
        this.outlinkNum = outlinkNum;
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
    }

    public String getContentType() {
        if (Utils.isEmpty(this.contentType) || contentType.equalsIgnoreCase("null")) {
            return "unknown";
        }
        return contentType;
    }

    public void setContentType(String contentType) {
        if (Utils.isEmpty(contentType) || contentType.equalsIgnoreCase("null")) {
            this.contentType = "unknown";
        } else {
            int idxSemicolon = contentType.indexOf(';');
            if (idxSemicolon > 0) {
                this.contentType = contentType.substring(0, idxSemicolon);
            } else {
                this.contentType = contentType;
            }
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
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

    @JsonIgnore
    public boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 400;
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
    public void accumulate() {
        this.accumulate(this.statusCode, this.contentLength, this.contentType);
    }

    @JsonIgnore
    public void copy(NetworkMapCommonNode that) {
        this.id = that.id;
        this.contentLength = that.contentLength;
        this.contentType = that.contentType;
        this.statusCode = that.statusCode;
        this.domainId = that.domainId;
        this.isSeed = that.isSeed;
        this.seedType = that.seedType;
        this.totFailed = that.totFailed;
        this.totSize = that.totSize;
        this.totSuccess = that.totSuccess;
        this.totUrls = that.totUrls;
        this.outlinkNum=that.outlinkNum;
    }

    @JsonIgnore
    public void setZero() {
        this.totUrls = 0;
        this.totSuccess = 0;
        this.totFailed = 0;
        this.totSize = 0;
    }

    @JsonIgnore
    public void accumulate(NetworkMapCommonNode node) {
        this.increaseTotSize(node.getTotSize());
        this.increaseTotSuccess(node.getTotSuccess());
        this.increaseTotFailed(node.getTotFailed());
        this.increaseTotUrls(node.getTotUrls());
    }
}
