package org.webcurator.rest.dto;

import org.webcurator.domain.model.core.AbstractTarget;
import org.webcurator.domain.model.core.Target;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class AccessDTO {
    @NotNull(message = "displayTarget is required")
    Boolean displayTarget;
    @NotNull(message = "accessZone is required")
    @Min(value = AbstractTarget.AccessZone.PUBLIC, message = "invalid accessZone")
    @Max(value = AbstractTarget.AccessZone.RESTRICTED, message = "invalid accessZone")
    Integer accessZone;
    String accessZoneText;
    String displayChangeReason;
    String displayNote;

    public AccessDTO() {
    }

    public AccessDTO(AbstractTarget target) {
        displayTarget = target.isDisplayTarget();
        accessZone = target.getAccessZone();
        accessZoneText = target.getAccessZoneText();
        displayChangeReason = target.getDisplayChangeReason();
        displayNote = target.getDisplayNote();
    }

    public Boolean getDisplayTarget() {
        return displayTarget;
    }

    public void setDisplayTarget(Boolean displayTarget) {
        this.displayTarget = displayTarget;
    }

    public Integer getAccessZone() {
        return accessZone;
    }

    public void setAccessZone(Integer accessZone) {
        this.accessZone = accessZone;
    }

    public String getAccessZoneText() {
        return accessZoneText;
    }

    public void setAccessZoneText(String accessZoneText) {
        this.accessZoneText = accessZoneText;
    }

    public String getDisplayChangeReason() {
        return displayChangeReason;
    }

    public void setDisplayChangeReason(String displayChangeReason) {
        this.displayChangeReason = displayChangeReason;
    }

    public String getDisplayNote() {
        return displayNote;
    }

    public void setDisplayNote(String displayNote) {
        this.displayNote = displayNote;
    }
}
