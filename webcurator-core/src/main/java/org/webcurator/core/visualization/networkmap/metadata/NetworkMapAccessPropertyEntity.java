package org.webcurator.core.visualization.networkmap.metadata;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
public class NetworkMapAccessPropertyEntity {
    private static final String DB_VERSION = "4.1.0";

    @PrimaryKey
    protected Long id = 0L;

    protected String version = DB_VERSION;
    protected List<Long> seedUrlIDs = new ArrayList<>();
    protected List<Long> malformedUrlIDs = new ArrayList<>();
    protected String rootDomain;
    protected Long rootFolderNode;
    protected Map<String, Long> warcFileIdMaps = new HashMap<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRootDomain() {
        return rootDomain;
    }

    public void setRootDomain(String rootDomain) {
        this.rootDomain = rootDomain;
    }

    public List<Long> getSeedUrlIDs() {
        return seedUrlIDs;
    }

    public void setSeedUrlIDs(List<Long> seedUrlIDs) {
        this.seedUrlIDs = seedUrlIDs;
    }

    public List<Long> getMalformedUrlIDs() {
        return malformedUrlIDs;
    }

    public void setMalformedUrlIDs(List<Long> malformedUrlIDs) {
        this.malformedUrlIDs = malformedUrlIDs;
    }

    public Long getRootFolderNode() {
        return rootFolderNode;
    }

    public void setRootFolderNode(Long rootFolderNode) {
        this.rootFolderNode = rootFolderNode;
    }

    public Map<String, Long> getWarcFileIdMaps() {
        return warcFileIdMaps;
    }

    public void setWarcFileIdMaps(Map<String, Long> warcFileIdMaps) {
        this.warcFileIdMaps = warcFileIdMaps;
    }

    public static String getDbVersion() {
        return DB_VERSION;
    }
}
