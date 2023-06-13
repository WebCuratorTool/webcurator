package org.webcurator.core.visualization.networkmap.service;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class NetworkMapServiceSearchCommand {
    private String domainLevel;
    private List<String> domainNames;
    private List<String> urlNames;
    private List<String> contentTypes;
    private List<Integer> statusCodes;

    public String getDomainLevel() {
        return domainLevel;
    }

    public void setDomainLevel(String domainLevel) {
        this.domainLevel = domainLevel;
    }

    public List<String> getDomainNames() {
        return domainNames;
    }

    public void setDomainNames(List<String> domainNames) {
        this.domainNames = domainNames;
    }

    public List<String> getUrlNames() {
        return urlNames;
    }

    public void setUrlNames(List<String> urlNames) {
        this.urlNames = urlNames;
    }

    public List<String> getContentTypes() {
        return contentTypes;
    }

    public void setContentTypes(List<String> contentTypes) {
        this.contentTypes = contentTypes;
    }

    public List<Integer> getStatusCodes() {
        return statusCodes;
    }

    public void setStatusCodes(List<Integer> statusCodes) {
        this.statusCodes = statusCodes;
    }

    @JsonIgnore
    public boolean isFilterable() {
        if (domainNames != null && domainNames.size() > 0) {
            return true;
        } else if (urlNames != null && urlNames.size() > 0) {
            return true;
        } else if (contentTypes != null && contentTypes.size() > 0) {
            return true;
        } else if (statusCodes != null && statusCodes.size() > 0) {
            return true;
        } else {
            return false;
        }
    }
}
