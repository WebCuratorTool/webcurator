package org.webcurator.core.visualization.networkmap.metadata;

import java.util.HashMap;
import java.util.Map;

public class NetworkMapDomainManager {
    private Map<String, NetworkMapDomain> indexDomainMap = new HashMap<>();

    public void pushDomain(NetworkMapDomain domain) {
        if (domain.getLevel() == NetworkMapDomain.DOMAIN_NAME_LEVEL_HIGH) {
            pushHighDomain(domain);
        } else if (domain.getLevel() == NetworkMapDomain.DOMAIN_NAME_LEVEL_LOWER) {
            pushLowerDomain(domain);
        }
    }

    public void pushHighDomain(NetworkMapDomain domain) {
        this.indexDomainMap.put("high_" + domain.getTitle(), domain);
    }

    public void pushLowerDomain(NetworkMapDomain domain) {
        this.indexDomainMap.put("lower_" + domain.getTitle(), domain);
    }

    public NetworkMapDomain getHighDomain(String key) {
        return this.indexDomainMap.get("high_" + key);
    }

    public NetworkMapDomain getLowerDomain(String key) {
        return this.indexDomainMap.get("lower_" + key);
    }

    public NetworkMapDomain getHighDomain(NetworkMapNode node) {
        return this.indexDomainMap.get("high_" + node.getTopDomain());
    }

    public NetworkMapDomain getLowerDomain(NetworkMapNode node) {
        return this.indexDomainMap.get("lower_" + node.getDomain());
    }

    public void clear() {
        this.indexDomainMap.values().forEach(NetworkMapDomain::clear);
        this.indexDomainMap.clear();
    }
}
