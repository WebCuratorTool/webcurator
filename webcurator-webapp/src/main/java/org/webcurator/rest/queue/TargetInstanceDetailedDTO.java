package org.webcurator.rest.queue;

import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.SeedHistory;
import org.webcurator.domain.model.core.TargetInstance;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TargetInstanceDetailedDTO extends TargetInstanceBriefDTO {
    public TargetInstanceDetailedDTO(TargetInstance ti) {
        super(ti);
        this.agencyName=ti.getOwner().getAgency().getName();
        this.seedHistory=ti.getSeedHistory();
        this.wctAppVersion=ti.getStatus().getApplicationVersion();
        this.priority=ti.getPriorities().get(ti.getPriority());
        this.captureSystem=ti.getStatus().getHeritrixVersion();
        this.useAutomatedQA=ti.isUseAQA();
    }

//    private Long profileId;
//    private Long overrideProfileId;
//
//    private List<HarvestResult> harvestResults;
//

    private String agencyName;
    private Set<SeedHistory> seedHistory = new HashSet<SeedHistory>();

    private String wctAppVersion;
    private String priority;
    private String captureSystem;
    private Boolean useAutomatedQA;

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    public Set<SeedHistory> getSeedHistory() {
        return seedHistory;
    }

    public void setSeedHistory(Set<SeedHistory> seedHistory) {
        this.seedHistory = seedHistory;
    }

    public String getWctAppVersion() {
        return wctAppVersion;
    }

    public void setWctAppVersion(String wctAppVersion) {
        this.wctAppVersion = wctAppVersion;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCaptureSystem() {
        return captureSystem;
    }

    public void setCaptureSystem(String captureSystem) {
        this.captureSystem = captureSystem;
    }

    public Boolean getUseAutomatedQA() {
        return useAutomatedQA;
    }

    public void setUseAutomatedQA(Boolean useAutomatedQA) {
        this.useAutomatedQA = useAutomatedQA;
    }
}