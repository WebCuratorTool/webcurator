package org.webcurator.rest.dto;

import org.webcurator.domain.model.core.Schedule;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

public class ScheduleDTO {
    long id;
    @NotBlank(message = "cron is required")
    String cron;
    @NotNull(message = "startDate is required")
    Date startDate;
    Date endDate;
    @NotNull(message = "type is required")
    @Min(value = -7, message = "invalid schedule type: value should be between -7 and 1")
    @Max(value = 1, message = "invalid schedule type: value should be between -7 and 1")
    Integer type;
    @NotNull(message = "nextExecutionDate is required")
    Date nextExecutionDate;
    Date lastProcessedDate;
    @NotNull(message = "owner is required")
    String owner;

    public ScheduleDTO() {
    }

    public ScheduleDTO(Schedule schedule) {
        id = schedule.getOid();
        // We only support classic cron, without the prepended SECONDS field used by Quartz
        cron = schedule.getCronPatternWithoutSeconds();
        startDate = schedule.getStartDate();
        endDate = schedule.getEndDate();
        type = schedule.getScheduleType();
        nextExecutionDate = schedule.getNextExecutionDate();
        lastProcessedDate = schedule.getLastProcessedDate();
        owner = schedule.getOwningUser().getUsername();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Date getNextExecutionDate() {
        return nextExecutionDate;
    }

    public void setNextExecutionDate(Date nextExecutionDate) {
        this.nextExecutionDate = nextExecutionDate;
    }

    public Date getLastProcessedDate() {
        return lastProcessedDate;
    }

    public void setLastProcessedDate(Date lastProcessedDate) {
        this.lastProcessedDate = lastProcessedDate;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
