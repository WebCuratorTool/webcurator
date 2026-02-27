package org.webcurator.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.webcurator.domain.model.core.Profile;

public class ProfileDTO {

    // FIXME validations
    private long id;
    private String description;
    private String profile;
    private int level;
    private int state;
    @JsonProperty("default")
    private boolean isDefault;
    private String agency;
    private String harvesterType;
    private String dataLimitUnit;
    private String maxFileSizeUnit;
    private boolean imported;

    public ProfileDTO(Profile prof) {
       id = prof.getOid();
       description = prof.getDescription();
       level = prof.getRequiredLevel();
       state = prof.getStatus();
       isDefault = prof.isDefaultProfile();
       agency = prof.getOwningAgency().getName();
       harvesterType = prof.getHarvesterType();
       dataLimitUnit = prof.getDataLimitUnit();
       maxFileSizeUnit = prof.getMaxFileSizeUnit();
       imported = prof.isImported();
       profile = prof.getProfile();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public String getHarvesterType() {
        return harvesterType;
    }

    public void setHarvesterType(String harvesterType) {
        this.harvesterType = harvesterType;
    }

    public String getDataLimitUnit() {
        return dataLimitUnit;
    }

    public void setDataLimitUnit(String dataLimitUnit) {
        this.dataLimitUnit = dataLimitUnit;
    }

    public String getMaxFileSizeUnit() {
        return maxFileSizeUnit;
    }

    public void setMaxFileSizeUnit(String maxFileSizeUnit) {
        this.maxFileSizeUnit = maxFileSizeUnit;
    }

    public boolean isImported() {
        return imported;
    }

    public void setImported(boolean imported) {
        this.imported = imported;
    }
}
