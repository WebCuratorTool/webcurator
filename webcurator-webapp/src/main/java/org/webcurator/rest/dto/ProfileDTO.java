package org.webcurator.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.webcurator.core.harvester.HarvesterType;
import org.webcurator.domain.model.core.Profile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class ProfileDTO {

    private long id;
    @NotNull(message = "name is required")
    private String name;
    private String description;
    @NotNull(message = "profile is required")
    private String profile;
    private int level = 1;
    private int state = Profile.STATUS_ACTIVE;
    @JsonProperty("default")
    private boolean isDefault = false;
    @NotNull(message = "agency is required")
    private String agency;
    @Pattern(regexp = "HERITRIX3", message = "only harvesterType HERITRIX3 is supported")
    private String harvesterType = HarvesterType.HERITRIX3.name();
    @NotNull(message = "dataLimitUnit is required")
    @Pattern(regexp = "B|KB|MB|GB", message = "dataLimitUnit should be one of B, KB, MB and GB")
    private String dataLimitUnit;
    @NotNull(message = "maxFileSizeUnit is required")
    @Pattern(regexp = "B|KB|MB|GB", message = "maxFileSizeUnit should be one of B, KB, MB and GB")
    private String maxFileSizeUnit;
    @NotNull(message = "imported is required")
    private boolean imported = false;

    public ProfileDTO() {}

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
