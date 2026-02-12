package org.webcurator.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.webcurator.domain.model.core.HarvesterStatus;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.rest.TargetInstances;

import java.util.*;

/**
 * This class is used for mapping between the TargetInstance entity and the JSON representation of a target instance in the API.
 */
public class TargetInstanceDTO {

    General general;
    ProfileDTO profile;
    HarvestState harvestState;
    List<Log> logs;
    List<HarvestResult> harvestResults;
    List<Annotation> annotations;
    Display display;

    // The back end uses Strings, but the API should use numerical state values, so we need this look-up table
    static Map<String, Integer> reverseStateMap;
    static {
         reverseStateMap = new TreeMap<>();
         for (Integer key : TargetInstances.stateMap.keySet()) {
             reverseStateMap.put(TargetInstances.stateMap.get(key), key);
         }
    }

    public TargetInstanceDTO() {
    }

    public TargetInstanceDTO(TargetInstance targetInstance) {
        this();
        general = new General(targetInstance);
        profile = new ProfileDTO(targetInstance);
        if (targetInstance.getStatus() != null) {
            harvestState = new HarvestState(targetInstance);
        }
        harvestResults = new ArrayList<>();
        for (org.webcurator.domain.model.core.HarvestResult r : targetInstance.getHarvestResults()) {
            HarvestResult harvestResult = new HarvestResult();
            harvestResult.setId(r.getOid());
            harvestResult.setNumber(r.getHarvestNumber());
            harvestResult.setCreationDate(r.getCreationDate());
            if (r.getDerivedFrom() != 0) { // derivedFrom is actually null if this getter returns 0
                harvestResult.setDerivedFrom(r.getDerivedFrom());
            }
            harvestResult.setOwner(r.getOwningUser().getUsername());
            harvestResult.setNote(r.getProvenanceNote());
            harvestResult.setState(r.getState());
            harvestResults.add(harvestResult);
        }
        annotations = new ArrayList<>();
        for (org.webcurator.domain.model.core.Annotation a : targetInstance.getAnnotations()) {
            Annotation annotation = new Annotation();
            annotation.setDate(a.getDate());
            annotation.setNote(a.getNote());
            annotation.setUser(a.getUser().getUsername());
            annotation.setAlert(a.isAlertable());
            annotations.add(annotation);
        }
        display = new Display(targetInstance);
    }

    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
    }

    public ProfileDTO getProfile() {
        return profile;
    }

    public void setProfile(ProfileDTO profile) {
        this.profile = profile;
    }

    public HarvestState getHarvestState() {
        return harvestState;
    }

    public void setHarvestState(HarvestState harvestState) {
        this.harvestState = harvestState;
    }

    public List<Log> getLogs() {
        return logs;
    }

    public void setLogs(List<Log> logs) {
        this.logs = logs;
    }

    public List<HarvestResult> getHarvestResults() {
        return harvestResults;
    }

    public void setHarvestResults(List<HarvestResult> harvestResults) {
        this.harvestResults = harvestResults;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public Display getDisplay() {
        return display;
    }

    public void setDisplay(Display display) {
        this.display = display;
    }

    public static class General {
        long id;
        String name;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Date scheduleStartDate;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Date actualStartDate;
        String priority;
        String owner;
        String agency;
        Integer state;
        Boolean automatedQA = null;
        Integer bandwidthPercentage;
        Long flagId;

        public General() {
        }

        public General(TargetInstance targetInstance) {
            id = targetInstance.getOid();
            name = targetInstance.getTarget().getName();
            scheduleStartDate = targetInstance.getScheduledTime();
            actualStartDate = targetInstance.getActualStartTime();
            priority = targetInstance.getPriorities().get(targetInstance.getPriority());
            owner = targetInstance.getOwner().getUsername();
            agency = targetInstance.getOwningUser().getAgency().getName();
            state = reverseStateMap.get(targetInstance.getState());
            automatedQA = targetInstance.isUseAQA();
            bandwidthPercentage = targetInstance.getBandwidthPercent();
            if (targetInstance.getFlag() != null) {
                flagId = targetInstance.getFlag().getOid();
            }
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Date getScheduleStartDate() {
            return scheduleStartDate;
        }

        public void setScheduleStartDate(Date scheduleStartDate) {
            this.scheduleStartDate = scheduleStartDate;
        }

        public Date getActualStartDate() {
            return actualStartDate;
        }

        public void setActualStartDate(Date actualStartDate) {
            this.actualStartDate = actualStartDate;
        }

        public String getPriority() {
            return priority;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }

        public String getAgency() {
            return agency;
        }

        public void setAgency(String agency) {
            this.agency = agency;
        }

        public Integer getBandwidthPercentage() {
            return bandwidthPercentage;
        }

        public void setBandwidthPercentage(Integer bandwidthPercentage) {
            this.bandwidthPercentage = bandwidthPercentage;
        }

        public Long getFlagId() {
            return flagId;
        }

        public void setFlagId(Long flagId) {
            this.flagId = flagId;
        }

        public Boolean getAutomatedQA() {
            return automatedQA;
        }

        public void setAutomatedQA(Boolean automatedQA) {
            this.automatedQA = automatedQA;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public Integer getState() {
            return state;
        }

        public void setState(Integer state) {
            this.state = state;
        }

    }

    public static class HarvestState {
        String wctVersion;
        String captureSystem;
        String harvestServer;
        String job;
        String status;
        Double averageKbsPerSecond;
        Double averageUrisPerSecond;
        Long urlsDownloaded;
        Long urlsFailed;
        String dataDownloaded;
        String elapsedTime;

        public HarvestState() {}

        public HarvestState(TargetInstance targetInstance) {
            harvestServer = targetInstance.getHarvestServer();
            HarvesterStatus harvesterStatus = targetInstance.getStatus();
            if (harvesterStatus != null) {
                wctVersion = harvesterStatus.getApplicationVersion();
                captureSystem = harvesterStatus.getHeritrixVersion();
                job = harvesterStatus.getJobName();
                status = harvesterStatus.getStatus();
                averageKbsPerSecond = harvesterStatus.getAverageKBs();
                averageUrisPerSecond = harvesterStatus.getAverageURIs();
                urlsDownloaded = harvesterStatus.getUrlsDownloaded();
                urlsFailed = harvesterStatus.getUrlsFailed();
                dataDownloaded = harvesterStatus.getDataDownloadedString();
                elapsedTime = harvesterStatus.getElapsedTimeString();
            }
        }

        public String getWctVersion() {
            return wctVersion;
        }

        public void setWctVersion(String wctVersion) {
            this.wctVersion = wctVersion;
        }

        public String getCaptureSystem() {
            return captureSystem;
        }

        public void setCaptureSystem(String captureSystem) {
            this.captureSystem = captureSystem;
        }

        public String getHarvestServer() {
            return harvestServer;
        }

        public void setHarvestServer(String harvestServer) {
            this.harvestServer = harvestServer;
        }

        public String getJob() {
            return job;
        }

        public void setJob(String job) {
            this.job = job;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Double getAverageKbsPerSecond() {
            return averageKbsPerSecond;
        }

        public void setAverageKbsPerSecond(Double averageKbsPerSecond) {
            this.averageKbsPerSecond = averageKbsPerSecond;
        }

        public Double getAverageUrisPerSecond() {
            return averageUrisPerSecond;
        }

        public void setAverageUrisPerSecond(Double averageUrisPerSecond) {
            this.averageUrisPerSecond = averageUrisPerSecond;
        }

        public Long getUrlsDownloaded() {
            return urlsDownloaded;
        }

        public void setUrlsDownloaded(Long urlsDownloaded) {
            this.urlsDownloaded = urlsDownloaded;
        }

        public Long getUrlsFailed() {
            return urlsFailed;
        }

        public void setUrlsFailed(Long urlsFailed) {
            this.urlsFailed = urlsFailed;
        }

        public String getDataDownloaded() {
            return dataDownloaded;
        }

        public void setDataDownloaded(String dataDownloaded) {
            this.dataDownloaded = dataDownloaded;
        }

        public String getElapsedTime() {
            return elapsedTime;
        }

        public void setElapsedTime(String elapsedTime) {
            this.elapsedTime = elapsedTime;
        }
    }

    public static class Log {
        String logFile;
        String location;
        String size;

        public String getLogFile() {
            return logFile;
        }

        public void setLogFile(String logFile) {
            this.logFile = logFile;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }
    }

    public static class HarvestResult {
        Long id;
        Integer number;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Date creationDate;
        Integer derivedFrom;
        String owner;
        String note;
        Integer state;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }

        public Date getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(Date creationDate) {
            this.creationDate = creationDate;
        }

        public Integer getDerivedFrom() {
            return derivedFrom;
        }

        public void setDerivedFrom(Integer derivedFrom) {
            this.derivedFrom = derivedFrom;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public Integer getState() {
            return state;
        }

        public void setState(Integer state) {
            this.state = state;
        }
    }

    public static class Annotation {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Date date;
        String user;
        String note;
        Boolean alert;

        public Annotation() {
        }

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

        public Boolean getAlert() {
            return alert;
        }

        public void setAlert(Boolean alert) {
            this.alert = alert;
        }
    }

    public static class Display {
        Boolean displayTargetInstance;
        String displayChangeReason;
        String displayNote;

        public Display() {}

        public Display(TargetInstance targetInstance) {
            displayTargetInstance = targetInstance.getDisplay();
            displayChangeReason = targetInstance.getDisplayChangeReason();
            displayNote = targetInstance.getDisplayNote();
        }

        public Boolean getDisplayTargetInstance() {
            return displayTargetInstance;
        }

        public void setDisplayTargetInstance(Boolean displayTargetInstance) {
            this.displayTargetInstance = displayTargetInstance;
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

}

