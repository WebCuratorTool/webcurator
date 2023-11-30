package org.webcurator.rest.dto;

import org.webcurator.domain.model.core.*;
import org.webcurator.rest.TargetInstances;

import java.util.*;

/**
 * This class is used for mapping between the Target entity and the JSON representation of a target instance in the API.
 */
public class TargetInstanceDTO {

    General general;
    Profile profile;
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
        profile = new Profile(targetInstance);
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

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
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
        Date scheduleStartDate;
        Date actualStartDate;
        String priority;
        String owner;
        String agency;
        Integer state;
        Boolean automatedQA = false;
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

    public static class Profile {

        String harvesterType;
        Long id;
        Boolean imported;
        String name;
        List<Override> overrides = new ArrayList<>();

        public Profile() {
        }

        public String getHarvesterType() {
            return harvesterType;
        }

        public void setHarvesterType(String harvesterType) {
            this.harvesterType = harvesterType;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Boolean getImported() {
            return imported;
        }

        public void setImported(Boolean imported) {
            this.imported = imported;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Override> getOverrides() {
            return overrides;
        }

        public void setOverrides(List<Override> overrides) {
            this.overrides = overrides;
        }

        public Profile(TargetInstance targetInstance) {

            org.webcurator.domain.model.core.Profile p = targetInstance.getProfile();
            harvesterType = p.getHarvesterType();
            id = p.getOid();
            imported = p.isImported();
            name = p.getName();

            // FIXME what do we do in the case of an imported profile (which doesn't have overrides, but is overridden entirely by a new profile)?
            ProfileOverrides o = targetInstance.getProfileOverrides();
            if (p.isHeritrix3Profile()) {
                Override documentLimit = new Override();
                documentLimit.setId("documentLimit");
                documentLimit.setValue(o.getH3DocumentLimit());
                documentLimit.setEnabled(o.isOverrideH3DocumentLimit());
                overrides.add(documentLimit);
                Override dataLimit = new Override();
                dataLimit.setId("dataLimit");
                dataLimit.setValue(o.getH3DataLimit());
                dataLimit.setUnit(o.getH3DataLimitUnit());
                dataLimit.setEnabled(o.isOverrideH3DataLimit());
                overrides.add(dataLimit);
                Override timeLimit = new Override();
                timeLimit.setId("timeLimit");
                timeLimit.setValue(o.getH3TimeLimit());
                timeLimit.setUnit(o.getH3TimeLimitUnit());
                timeLimit.setEnabled(o.isOverrideH3TimeLimit());
                overrides.add(timeLimit);
                Override maxPathDepth = new Override();
                maxPathDepth.setId("maxPathDepth");
                maxPathDepth.setValue(o.getH3MaxPathDepth());
                maxPathDepth.setEnabled(o.isOverrideH3MaxPathDepth());
                overrides.add(maxPathDepth);
                Override maxHops = new Override();
                maxHops.setId("maxHops");
                maxHops.setValue(o.getH3MaxHops());
                maxHops.setEnabled(o.isOverrideH3MaxHops());
                overrides.add(maxHops);
                Override maxTransitiveHops = new Override();
                maxTransitiveHops.setId("maxTransitiveHops");
                maxTransitiveHops.setValue(o.getH3MaxTransitiveHops());
                maxTransitiveHops.setEnabled(o.isOverrideH3MaxTransitiveHops());
                overrides.add(maxTransitiveHops);
                Override ignoreRobots = new Override();
                ignoreRobots.setId("ignoreRobots");
                ignoreRobots.setValue(o.isH3IgnoreRobots());
                ignoreRobots.setEnabled(o.isOverrideH3IgnoreRobots());
                overrides.add(ignoreRobots);
                Override extractJs = new Override();
                extractJs.setId("extractJs");
                extractJs.setValue(o.isH3ExtractJs());
                extractJs.setEnabled(o.isOverrideH3ExtractJs());
                overrides.add(extractJs);
                Override ignoreCookies = new Override();
                ignoreCookies.setId("ignoreCookies");
                ignoreCookies.setValue(o.isH3IgnoreCookies());
                ignoreCookies.setEnabled(o.isOverrideH3IgnoreCookies());
                overrides.add(ignoreCookies);
                Override blockedUrls = new Override();
                blockedUrls.setId("blockedUrls");
                blockedUrls.setValue(o.getH3BlockedUrls());
                blockedUrls.setEnabled(o.isOverrideH3BlockedUrls());
                overrides.add(blockedUrls);
                Override includedUrls = new Override();
                includedUrls.setId("includedUrls");
                includedUrls.setValue(o.getH3IncludedUrls());
                includedUrls.setEnabled(o.isOverrideH3IncludedUrls());
                overrides.add(includedUrls);
            } else { // Legacy H1 overrides
                Override robotsHonouringPolicy = new Override();
                robotsHonouringPolicy.setId("robotsHonouringPolicy");
                robotsHonouringPolicy.setValue(o.getRobotsHonouringPolicy());
                robotsHonouringPolicy.setEnabled(o.isOverrideRobotsHonouringPolicy());
                overrides.add(robotsHonouringPolicy);
                Override maxTimeSec = new Override();
                maxTimeSec.setId("maxTimeSec");
                maxTimeSec.setValue(o.getMaxTimeSec());
                maxTimeSec.setEnabled(o.isOverrideMaxTimeSec());
                overrides.add(maxTimeSec);
                Override maxBytesDownload = new Override();
                maxBytesDownload.setId("maxBytesDownload");
                maxBytesDownload.setValue(o.getMaxBytesDownload());
                maxBytesDownload.setEnabled(o.isOverrideMaxBytesDownload());
                overrides.add(maxBytesDownload);
                Override maxHarvestDocuments = new Override();
                maxHarvestDocuments.setId("maxHarvestDocuments");
                maxHarvestDocuments.setValue(o.getMaxHarvestDocuments());
                maxHarvestDocuments.setEnabled(o.isOverrideMaxHarvestDocuments());
                overrides.add(maxHarvestDocuments);
                Override maxPathDepth = new Override();
                maxPathDepth.setId("maxPathDepth");
                maxPathDepth.setValue(o.getMaxPathDepth());
                maxPathDepth.setEnabled(o.isOverrideMaxPathDepth());
                overrides.add(maxPathDepth);
                Override maxLinkHops = new Override();
                maxLinkHops.setId("maxLinkHops");
                maxLinkHops.setValue(o.getMaxLinkHops());
                maxLinkHops.setEnabled(o.isOverrideMaxLinkHops());
                overrides.add(maxLinkHops);
                Override excludeFilters = new Override();
                excludeFilters.setId("excludeFilters");
                excludeFilters.setValue(o.getExcludeUriFilters());
                excludeFilters.setEnabled(o.isOverrideExcludeUriFilters());
                overrides.add(excludeFilters);
                Override includeFilters = new Override();
                includeFilters.setId("includeFilters");
                includeFilters.setValue(o.getIncludeUriFilters());
                includeFilters.setEnabled(o.isOverrideIncludeUriFilters());
                overrides.add(includeFilters);
                Override excludedMimeTypes = new Override();
                excludedMimeTypes.setId("excludedMimeTypes");
                excludedMimeTypes.setValue(o.getExcludedMimeTypes());
                excludedMimeTypes.setEnabled(o.isOverrideExcludedMimeTypes());
                overrides.add(excludedMimeTypes);
                Override credentials = new Override();
                credentials.setId("credentials");
                credentials.setValue(o.getCredentials());
                credentials.setEnabled(o.isOverrideCredentials());
                overrides.add(credentials);
            }
        }

        public static class Override {
            String id;
            Object value;
            Boolean enabled;
            String unit;

            public Override() {
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public Object getValue() {
                return value;
            }

            public void setValue(Object value) {
                this.value = value;
            }

            public Boolean getEnabled() {
                return enabled;
            }

            public void setEnabled(Boolean enabled) {
                this.enabled = enabled;
            }

            public String getUnit() {
                return unit;
            }

            public void setUnit(String unit) {
                this.unit = unit;
            }
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

