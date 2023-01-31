package org.webcurator.rest.queue;

import org.webcurator.domain.model.core.Flag;
import org.webcurator.domain.model.core.TargetInstance;

import java.util.Date;

public class TargetInstanceBriefDTO {
    public TargetInstanceBriefDTO(TargetInstance ti) {
        this.oid = ti.getOid();

        this.flagged = ti.getFlagged();
        this.flag = ti.getFlag();

        if (ti.getTarget() != null) {
            this.targetOid = ti.getTarget().getOid();
            this.targetName = ti.getTarget().getName();
        }

        this.sortOrderDate = ti.getSortOrderDate();
        this.state = ti.getState();
        if (ti.getOwner() != null) {
            this.ownerOid = ti.getOwner().getOid();
            this.ownerNiceName = ti.getOwner().getNiceName();
        }

        if (ti.getStatus() != null) {
            this.statusElapsedTime = ti.getStatus().getElapsedTime();
            this.statusDataDownloadedString = ti.getStatus().getDataDownloadedString();
            this.statusDownloadedUrls=ti.getStatus().getUrlsDownloaded();
            this.statusFailedUrls=ti.getStatus().getUrlsFailed();
        }
    }

    private boolean rowSelection = false;
    private Long oid;

    private boolean flagged;
    private Flag flag;

    private Long targetOid;
    private String targetName;

    private Date sortOrderDate;
    private String state;

    private Long ownerOid;
    private String ownerNiceName;

    private Long statusElapsedTime;
    private String statusDataDownloadedString;

    private Long statusDownloadedUrls;
    private Long statusFailedUrls;


    public boolean isRowSelection() {
        return rowSelection;
    }

    public void setRowSelection(boolean rowSelection) {
        this.rowSelection = rowSelection;
    }

    public Long getOid() {
        return oid;
    }

    public void setOid(Long oid) {
        this.oid = oid;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public Flag getFlag() {
        return flag;
    }

    public void setFlag(Flag flag) {
        this.flag = flag;
    }

    public Long getTargetOid() {
        return targetOid;
    }

    public void setTargetOid(Long targetOid) {
        this.targetOid = targetOid;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public Date getSortOrderDate() {
        return sortOrderDate;
    }

    public void setSortOrderDate(Date sortOrderDate) {
        this.sortOrderDate = sortOrderDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getOwnerOid() {
        return ownerOid;
    }

    public void setOwnerOid(Long ownerOid) {
        this.ownerOid = ownerOid;
    }

    public String getOwnerNiceName() {
        return ownerNiceName;
    }

    public void setOwnerNiceName(String ownerNiceName) {
        this.ownerNiceName = ownerNiceName;
    }

    public Long getStatusElapsedTime() {
        return statusElapsedTime;
    }

    public void setStatusElapsedTime(Long statusElapsedTime) {
        this.statusElapsedTime = statusElapsedTime;
    }

    public String getStatusDataDownloadedString() {
        return statusDataDownloadedString;
    }

    public void setStatusDataDownloadedString(String statusDataDownloadedString) {
        this.statusDataDownloadedString = statusDataDownloadedString;
    }

    public Long getStatusDownloadedUrls() {
        return statusDownloadedUrls;
    }

    public void setStatusDownloadedUrls(Long statusDownloadedUrls) {
        this.statusDownloadedUrls = statusDownloadedUrls;
    }

    public Long getStatusFailedUrls() {
        return statusFailedUrls;
    }

    public void setStatusFailedUrls(Long statusFailedUrls) {
        this.statusFailedUrls = statusFailedUrls;
    }
}