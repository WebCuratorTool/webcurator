package org.webcurator.rest.dto;

import org.webcurator.domain.model.core.AuthorisingAgent;
import org.webcurator.domain.model.core.Permission;
import org.webcurator.domain.model.core.PermissionExclusion;
import org.webcurator.domain.model.core.UrlPattern;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PermissionDTO {
    private Long id;
    private Date startDate;
    private Date endDate;
    private Integer status;
    private List<String> urlPatterns;
    private Long harvestAuthorisationId;
    private String accessStatus;
    private String copyrightStatement;
    private String copyRightUrl;
    private String authResponse;
    private Date openAccessDate;
    private AuthorisingAgent authorisingAgent;
    private Boolean quickPick;
    private List<Annotation> annotations;
    private String displayName;
    private List<Exclusion> exclusions;

    public PermissionDTO() {}

    public PermissionDTO(Permission permission) {
        id = permission.getOid();
        startDate = permission.getStartDate();
        endDate = permission.getEndDate();
        status = permission.getStatus();
        urlPatterns = new ArrayList<>();
        for (UrlPattern p : permission.getUrls()) {
            urlPatterns.add(p.getPattern());
        }
        authorisingAgent = new AuthorisingAgent(permission.getAuthorisingAgent());
        harvestAuthorisationId = permission.getSite().getOid();
        accessStatus = permission.getAccessStatus();
        copyrightStatement = permission.getCopyrightStatement();
        copyRightUrl = permission.getCopyrightUrl();
        authResponse = permission.getAuthResponse();
        openAccessDate = permission.getOpenAccessDate();
        quickPick = permission.isQuickPick();
        annotations = new ArrayList<>();
        for (org.webcurator.domain.model.core.Annotation a : permission.getAnnotations()) {
            Annotation annotation = new Annotation();
            annotation.setDate(a.getDate());
            annotation.setUser(a.getUser().getUsername());
            annotation.setNote((a.getNote()));
            annotations.add(annotation);
        }
        displayName = permission.getDisplayName();
        exclusions = new ArrayList<>();
        for (PermissionExclusion x : permission.getExclusions()) {
            Exclusion exclusion = new Exclusion();
            exclusion.setUrl(x.getUrl());
            exclusion.setReason(x.getReason());
            exclusions.add(exclusion);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<String> getUrlPatterns() {
        return urlPatterns;
    }

    public void setUrlPatterns(List<String> urlPatterns) {
        this.urlPatterns = urlPatterns;
    }

    public Long getHarvestAuthorisationId() {
        return harvestAuthorisationId;
    }

    public void setHarvestAuthorisationId(Long harvestAuthorisationId) {
        this.harvestAuthorisationId = harvestAuthorisationId;
    }

    public String getAccessStatus() {
        return accessStatus;
    }

    public void setAccessStatus(String accessStatus) {
        this.accessStatus = accessStatus;
    }

    public String getCopyrightStatement() {
        return copyrightStatement;
    }

    public void setCopyrightStatement(String copyrightStatement) {
        this.copyrightStatement = copyrightStatement;
    }

    public String getCopyRightUrl() {
        return copyRightUrl;
    }

    public void setCopyRightUrl(String copyRightUrl) {
        this.copyRightUrl = copyRightUrl;
    }

    public String getAuthResponse() {
        return authResponse;
    }

    public void setAuthResponse(String authResponse) {
        this.authResponse = authResponse;
    }

    public Date getOpenAccessDate() {
        return openAccessDate;
    }

    public void setOpenAccessDate(Date openAccessDate) {
        this.openAccessDate = openAccessDate;
    }

    public AuthorisingAgent getAuthorisingAgent() {
        return authorisingAgent;
    }

    public void setAuthorisingAgent(AuthorisingAgent authorisingAgent) {
        this.authorisingAgent = authorisingAgent;
    }

    public Boolean getQuickPick() {
        return quickPick;
    }

    public void setQuickPick(Boolean quickPick) {
        this.quickPick = quickPick;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<Exclusion> getExclusions() {
        return exclusions;
    }

    public void setExclusions(List<Exclusion> exclusions) {
        this.exclusions = exclusions;
    }

    public static class Exclusion {
        String url;
        String reason;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public static class Annotation {
        Date date;
        String user;
        String note;

        public Annotation() {}

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }

    public static class AuthorisingAgent {
        private Long id;
        private String name;

        public AuthorisingAgent(org.webcurator.domain.model.core.AuthorisingAgent authorisingAgent) {
            this.id = authorisingAgent.getOid();
            this.name = authorisingAgent.getName();
        }

        public AuthorisingAgent() {}

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
