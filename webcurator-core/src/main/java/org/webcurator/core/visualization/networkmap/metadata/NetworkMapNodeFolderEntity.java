package org.webcurator.core.visualization.networkmap.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity
public class NetworkMapNodeFolderEntity extends BasicNode {
    private long parentPathId;

    @SecondaryKey(relate = Relationship.ONE_TO_ONE)
    private String title;

    private List<Long> subFolderList = new ArrayList<>();
    private List<Long> subUrlList = new ArrayList<>();

    public long getParentPathId() {
        return parentPathId;
    }

    public void setParentPathId(long parentPathId) {
        this.parentPathId = parentPathId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Long> getSubFolderList() {
        return subFolderList;
    }

    public void setSubFolderList(List<Long> subFolderList) {
        this.subFolderList = subFolderList;
    }

    public List<Long> getSubUrlList() {
        return subUrlList;
    }

    public void setSubUrlList(List<Long> subUrlList) {
        this.subUrlList = subUrlList;
    }

    @JsonIgnore
    public void addSubFolder(BasicNode node) {
        this.subFolderList.add(node.getId());
    }

    @JsonIgnore
    public void addSubUrl(BasicNode node) {
        this.subUrlList.add(node.getId());
    }
}
