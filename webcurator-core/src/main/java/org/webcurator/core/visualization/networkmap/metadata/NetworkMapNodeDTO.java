package org.webcurator.core.visualization.networkmap.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NetworkMapNodeDTO extends NetworkMapCommonNode implements NetworkMapUnlStructure {
    public final static int UNL_FIELDS_COUNT_MAX = 18;

    public static final int SEED_TYPE_PRIMARY = 0;
    public static final int SEED_TYPE_SECONDARY = 1;
    public static final int SEED_TYPE_OTHER = 2;

    protected String url;

    protected long parentId = -1;
    protected long parentPathId = -1;

    protected long offset;
    protected long fetchTimeMs; //ms: time used to download the page
    protected String fileName;

    protected List<Long> outlinks = new ArrayList<>();
    protected List<NetworkMapNodeDTO> children = new ArrayList<>();

    protected String title;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public long getParentPathId() {
        return parentPathId;
    }

    public void setParentPathId(long parentPathId) {
        this.parentPathId = parentPathId;
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
    public void copy(NetworkMapNodeDTO that) {
        super.copy(that);
        this.url = that.url;
        this.title = that.title;
        this.parentId = that.parentId;
        this.offset = that.offset;
        this.fetchTimeMs = that.fetchTimeMs;
        this.fileName = that.fileName;
        this.outlinks = that.getOutlinks();
        this.children = that.getChildren();
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

    @JsonIgnore
    @Override
    public String toUnlString() {
        String strOutlinks = outlinks.stream().map(outlink -> Long.toString(outlink)).collect(Collectors.joining(","));
        return String.format("%d\n%s\n%d\n%d\n%d\n%d\n%d\n%d\n%d\n%s\n%s\n%d\n%d\n%d\n%s\n%b\n[%s]\n%d",
                id, url, seedType, totUrls, totSuccess, totFailed, totSize,
                domainId, contentLength, contentType, statusCode, parentId, offset, fetchTimeMs,
                fileName, isSeed, strOutlinks, parentPathId);
    }

    @JsonIgnore
    @Override
    public void toObjectFromUnl(String unl) throws Exception {
        if (unl == null) {
            throw new Exception("Unl could not be null.");
        }
        String[] items = unl.split(UNL_FIELDS_SEPARATOR);
        if (items.length != UNL_FIELDS_COUNT_MAX) {
            throw new Exception("Item number=" + items.length + " does not equal to UNL_FIELDS_COUNT_MAX=" + UNL_FIELDS_COUNT_MAX);
        }

        this.setId(Long.parseLong(items[0]));
        this.setUrl(items[1]);
        this.setSeedType(Integer.parseInt(items[2]));
        this.setTotUrls(Integer.parseInt(items[3]));
        this.setTotSuccess(Integer.parseInt(items[4]));
        this.setTotFailed(Integer.parseInt(items[5]));
        this.setTotSize(Integer.parseInt(items[6]));
        this.setDomainId(Integer.parseInt(items[7]));
        this.setContentLength(Long.parseLong(items[8]));
        this.setContentType(items[9]);
        this.setStatusCode(Integer.parseInt(items[10]));
        this.setParentId(Long.parseLong(items[11]));
        this.setOffset(Long.parseLong(items[12]));
        this.setFetchTimeMs(Long.parseLong(items[13]));
        this.setFileName(items[14]);
        this.setSeed(Boolean.parseBoolean(items[15]));

        String strOutlinks = items[16];
        List<Long> outlinks = new ArrayList<>();
        if (strOutlinks.length() > 2) {
            strOutlinks = strOutlinks.substring(1, strOutlinks.length() - 1);
            outlinks = Arrays.stream(strOutlinks.split(",")).map(Long::parseLong).collect(Collectors.toList());
        }
        this.setOutlinks(outlinks);
        this.setParentPathId(Long.parseLong(items[17]));
    }
}
