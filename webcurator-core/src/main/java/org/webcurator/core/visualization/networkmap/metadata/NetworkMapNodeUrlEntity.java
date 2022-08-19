package org.webcurator.core.visualization.networkmap.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity
public class NetworkMapNodeUrlEntity extends BasicNode{
    public static final int SEED_TYPE_PRIMARY = 0;
    public static final int SEED_TYPE_SECONDARY = 1;
    public static final int SEED_TYPE_OTHER = 2;

    @SecondaryKey(relate = Relationship.ONE_TO_ONE)
    protected String url;

    protected long parentId = -1;
    protected long parentPathId = -1;

    protected long offset;
    protected long fetchTimeMs; //ms: time used to download the page
    protected String fileName;

    protected List<Long> outlinks = new ArrayList<>();
    protected List<NetworkMapNodeUrlEntity> children = new ArrayList<>();

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

    public List<NetworkMapNodeUrlEntity> getChildren() {
        return children;
    }

    public void setChildren(List<NetworkMapNodeUrlEntity> children) {
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
    public void copy(NetworkMapNodeUrlEntity that) {
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
        this.children.forEach(NetworkMapNodeUrlEntity::clear);
        this.children.clear();
    }

    @JsonIgnore
    public void putChild(NetworkMapNodeUrlEntity e) {
        this.children.add(e);
    }
}
