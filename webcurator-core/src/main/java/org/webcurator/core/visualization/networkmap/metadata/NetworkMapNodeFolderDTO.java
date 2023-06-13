package org.webcurator.core.visualization.networkmap.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class NetworkMapNodeFolderDTO extends BasicNode {
    public static final int VIEW_TYPE_STRUCT = 1;
    public static final int VIEW_TYPE_DOMAIN = 2;

    private int viewType = VIEW_TYPE_DOMAIN;

    private boolean lazy = false;
    private boolean folder = false;
    private boolean virtual = true;

    private String title;

    @JsonIgnore
    private String url;

    private List<NetworkMapNodeFolderDTO> children = new ArrayList<>();

    @JsonIgnore
    private boolean handled = false;

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isLazy() {
        return lazy;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    public boolean isFolder() {
        return folder;
    }

    public void setFolder(boolean folder) {
        this.folder = folder;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public void setVirtual(boolean virtual) {
        this.virtual = virtual;
    }

    public static int getViewTypeStruct() {
        return VIEW_TYPE_STRUCT;
    }

    public static int getViewTypeDomain() {
        return VIEW_TYPE_DOMAIN;
    }

    @JsonIgnore
    public boolean isHandled() {
        return handled;
    }

    @JsonIgnore
    public void setHandled(boolean handled) {
        this.handled = handled;
    }

    public List<NetworkMapNodeFolderDTO> getChildren() {
        return children;
    }

    public void setChildren(List<NetworkMapNodeFolderDTO> children) {
        this.children = children;
    }

    @JsonIgnore
    public String getUrl() {
        return url;
    }

    @JsonIgnore
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonIgnore
    public void copy(NetworkMapNodeFolderDTO that) {
        super.copy(that);
        this.viewType = that.viewType;
        this.title = that.title;
        this.url = that.url;
    }

    @JsonIgnore
    public void clear() {
        this.children.forEach(NetworkMapNodeFolderDTO::clear);
        this.children.clear();
    }
}
