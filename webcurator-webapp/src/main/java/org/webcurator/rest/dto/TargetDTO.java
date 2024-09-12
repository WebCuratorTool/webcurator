package org.webcurator.rest.dto;

//import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
//import org.springframework.format.annotation.DateTimeFormat;
import org.webcurator.domain.model.core.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
//import java.util.TimeZone;

/**
 * This class is used for mapping between the Target entity and the JSON representation of a target in the API.
 */
public class TargetDTO {
    @Valid
    @NotNull(message = "General section is required")
    General general;
    @Valid
    Scheduling schedule;
    @Valid
    Access access;
    @Valid
    List<Seed> seeds = new ArrayList<>();
    @Valid
    Profile profile;
    @Valid
    Annotations annotations;
    @Valid
    Description description;
    @Valid
    List<Group> groups = new ArrayList<>();

    public TargetDTO() {
    }

    public TargetDTO(Target target) {
        general = new General(target);
        schedule = new Scheduling(target);
        access = new Access(target);
        for (org.webcurator.domain.model.core.Seed s : target.getSeeds()) {
            seeds.add(new Seed(s));
        }
        profile = new Profile(target);
        annotations = new Annotations(target);
        description = new Description(target);
        for (GroupMember m: target.getParents()) {
            groups.add(new Group(m));
        }
    }

    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
    }

    public Scheduling getSchedule() {
        return schedule;
    }

    public void setSchedule(Scheduling schedules) {
        this.schedule = schedules;
    }

    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    public List<Seed> getSeeds() {
        return seeds;
    }

    public void setSeeds(List<Seed> seeds) {
        this.seeds = seeds;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Annotations getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Annotations annotations) {
        this.annotations = annotations;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public static class General {
        long id;

//        @JsonFormat(pattern="yyyy-MM-ddTHH:mm:ss", timezone = "UTC+0") // Return to the UI
//        @DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm:ss") // Save to the DB
//        Date creationDate;
        Long creationDate;
        @NotBlank(message = "name is required")
        String name;
        String description;
        String referenceNumber;
        @NotNull(message = "runOnApproval is required")
        Boolean runOnApproval = false;
        @NotNull(message = "automatedQA is required")
        Boolean automatedQA = false;
        @NotBlank(message = "owner is required")
        String owner;
        @NotNull(message = "state is required")
        @Min(value = 1, message = "invalid state: value should be between 1 and 7")
        @Max(value = 7, message = "invalid state: value should be between 1 and 7")
        Integer state;
        @NotNull(message = "autoPrune is required")
        Boolean autoPrune = false;
        @NotNull(message = "referenceCrawl is required")
        Boolean referenceCrawl = false;
        String requestToArchivists;
        int[] nextStates;

        public General() {
        }

        public General(Target target) {
            id = target.getOid();
            creationDate = target.getCreationDate().toInstant().toEpochMilli();
            name = target.getName();
            description = target.getDescription();
            referenceNumber = target.getReferenceNumber();
            runOnApproval = target.isRunOnApproval();
            automatedQA = target.isUseAQA();
            owner = target.getOwner().getUsername();
            state = target.getState();
            autoPrune = target.isAutoPrune();
            referenceCrawl = target.isAutoDenoteReferenceCrawl();
            requestToArchivists = target.getRequestToArchivists();
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public Long getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(Long creationDate) {
            this.creationDate = creationDate;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getReferenceNumber() {
            return referenceNumber;
        }

        public void setReferenceNumber(String referenceNumber) {
            this.referenceNumber = referenceNumber;
        }

        public Boolean getRunOnApproval() {
            return runOnApproval;
        }

        public void setRunOnApproval(Boolean runOnApproval) {
            this.runOnApproval = runOnApproval;
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

        public Boolean getAutoPrune() {
            return autoPrune;
        }

        public void setAutoPrune(Boolean autoPrune) {
            this.autoPrune = autoPrune;
        }

        public Boolean getReferenceCrawl() {
            return referenceCrawl;
        }

        public void setReferenceCrawl(Boolean referenceCrawl) {
            this.referenceCrawl = referenceCrawl;
        }

        public String getRequestToArchivists() {
            return requestToArchivists;
        }

        public void setRequestToArchivists(String requestToArchivists) {
            this.requestToArchivists = requestToArchivists;
        }

        public int[] getNextStates() {
            return nextStates;
        }

        public void setNextStates(int[] nextStates) {
            this.nextStates = nextStates;
        }
    }

    public static class Scheduling {

        Boolean harvestNow;
        Boolean harvestOptimization;
        @Valid
        List<Schedule> schedules = new ArrayList<>();

        public Scheduling() {
        }

        public Scheduling(Target target) {
            harvestNow = target.isHarvestNow();
            harvestOptimization = target.isAllowOptimize();
            for (org.webcurator.domain.model.core.Schedule s : target.getSchedules()) {
                Schedule schedule = new Schedule();
                schedule.setId(s.getOid());
                // We only support classic cron, without the prepended SECONDS field used by Quartz
                schedule.setCron(s.getCronPatternWithoutSeconds());
                schedule.setStartDate(s.getStartDate());
                schedule.setEndDate(s.getEndDate());
                schedule.setType(s.getScheduleType());
                schedule.setNextExecutionDate(s.getNextExecutionDate());
                schedule.setLastProcessedDate(s.getLastProcessedDate());
                schedule.setOwner(s.getOwningUser().getUsername());
                schedules.add(schedule);
            }
        }

        public Boolean getHarvestNow() {
            return harvestNow;
        }

        public void setHarvestNow(Boolean harvestNow) {
            this.harvestNow = harvestNow;
        }

        public Boolean getHarvestOptimization() {
            return harvestOptimization;
        }

        public void setHarvestOptimization(Boolean harvestOptimization) {
            this.harvestOptimization = harvestOptimization;
        }

        public List<Schedule> getSchedules() {
            return schedules;
        }

        public void setSchedules(List<Schedule> schedules) {
            this.schedules = schedules;
        }

        public static class Schedule {
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

            public Schedule() {
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
    }

    public static class Access {
        @NotNull(message = "displayTarget is required")
        Boolean displayTarget;
        @NotNull(message = "accessZone is required")
        @Min(value = AbstractTarget.AccessZone.PUBLIC, message = "invalid accessZone")
        @Max(value = AbstractTarget.AccessZone.RESTRICTED, message = "invalid accessZone")
        Integer accessZone;
        String accessZoneText;
        String displayChangeReason;
        String displayNote;

        public Access() {
        }

        public Access(Target target) {
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

    public static class Seed {
        long id;
        @NotBlank(message = "seed is required")
        String seed;
        @NotNull(message = "primary is required")
        Boolean primary;
        @NotEmpty(message = "authorisations may not be empty")
        List<Authorisation> authorisations = new ArrayList<>();

        public Seed() {
        }

        public Seed(org.webcurator.domain.model.core.Seed s) {
            id = s.getOid();
            seed = s.getSeed();
            primary = s.isPrimary();
            for (Permission p : s.getPermissions()) {
                Authorisation authorisation = new Authorisation();
                authorisation.setId(p.getSite().getOid());
                authorisation.setName(p.getSite().getTitle());
                authorisation.setAgent(p.getAuthorisingAgent().getName());
                authorisation.setStartDate(p.getStartDate());
                authorisation.setEndDate(p.getEndDate());
                if (!authorisations.contains(authorisation)) {
                    authorisations.add(authorisation);
                }
            }
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getSeed() {
            return seed;
        }

        public void setSeed(String seed) {
            this.seed = seed;
        }

        public Boolean getPrimary() {
            return primary;
        }

        public void setPrimary(Boolean primary) {
            this.primary = primary;
        }

        public List<Authorisation> getAuthorisations() {
            return authorisations;
        }

        public void setAuthorisations(List<Authorisation> authorisations) {
            this.authorisations = authorisations;
        }

        public static class Authorisation {
            long id;
            String name;
            String agent;
            Date startDate;
            Date endDate;

            public Authorisation() {}

            public long getId() {
                return id;
            }

            public void setId(long id) {
                this.id = id;
            }

            public String getAgent() {
                return agent;
            }

            public void setAgent(String agent) {
                this.agent = agent;
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

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            @Override
            public boolean equals(Object other) {
                return this.id == ((Authorisation)other).id;
            }
        }
    }

    public static class Profile {

        String harvesterType;
        @NotNull(message = "id is required")
        Long id;
        Boolean imported;
        String name;
        @Valid
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

        public Profile(Target target) {

            org.webcurator.domain.model.core.Profile p = target.getProfile();
            harvesterType = p.getHarvesterType();
            id = p.getOid();
            imported = p.isImported();
            name = p.getName();

            // FIXME what do we do in the case of an imported profile (which doesn't have overrides, but is overridden entirely by a new profile)?
            ProfileOverrides o = target.getProfileOverrides();
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
            @NotBlank(message = "id is required")
            String id;
            Object value;
            @NotNull(message = "enabled is required")
            Boolean enabled;
            // FIXME we need better validation of the unit attribute
            @JsonInclude(JsonInclude.Include.NON_NULL)
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

    public static class Annotations {

        @Valid
        @NotNull(message = "selection is required")
        Selection selection;
        String evaluationNote;
        @Pattern(regexp = "Subject|Event|Theme", message = "invalid harvestType")
        String harvestType;
        @Valid
        List<Annotation> annotations = new ArrayList<>();

        public Annotations() {}

        public Annotations(Target target) {
            selection = new Selection();
            selection.setDate(target.getSelectionDate());
            selection.setNote(target.getSelectionNote());
            selection.setType(target.getSelectionType());
            evaluationNote = target.getEvaluationNote();
            harvestType = target.getHarvestType();
            for (org.webcurator.domain.model.core.Annotation a : target.getAnnotations()) {
                Annotation annotation = new Annotation();
                annotation.setDate(a.getDate());
                annotation.setNote(a.getNote());
                annotation.setUser(a.getUser().getUsername());
                annotation.setAlert(a.isAlertable());
                annotations.add(annotation);
            }
        }

        public Selection getSelection() {
            return selection;
        }

        public void setSelection(Selection selection) {
            this.selection = selection;
        }

        public String getEvaluationNote() {
            return evaluationNote;
        }

        public void setEvaluationNote(String evaluationNote) {
            this.evaluationNote = evaluationNote;
        }

        public String getHarvestType() {
            return harvestType;
        }

        public void setHarvestType(String harvestType) {
            this.harvestType = harvestType;
        }

        public List<Annotation> getAnnotations() {
            return annotations;
        }

        public void setAnnotations(List<Annotation> annotations) {
            this.annotations = annotations;
        }

        public static class Selection {
            Date date;
            @Pattern(regexp = "Producer type|Publication type|Collection|Area|Other collections",
                    message = "invalid selection type")
            String type;
            String note;

            public Selection() {}

            public Date getDate() {
                return date;
            }

            public void setDate(Date date) {
                this.date = date;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getNote() {
                return note;
            }

            public void setNote(String note) {
                this.note = note;
            }
        }

        public static class Annotation {
            Date date;
            String user;
            String note;
            Boolean alert;

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

            public Boolean getAlert() {
                return alert;
            }

            public void setAlert(Boolean alert) {
                this.alert = alert;
            }
        }
    }

    public static class Description {

        String identifier;
        String description;
        String subject;
        String creator;
        String publisher;
        String type;
        String format;
        String language;
        String source;
        String relation;
        String contributor;
        String coverage;
        String issn;
        String isbn;

        public Description() {}

        public Description(Target target) {
            DublinCore metadata = target.getDublinCoreMetaData();
            if (metadata != null) {
                identifier = metadata.getIdentifier();
                description = metadata.getDescription();
                subject = metadata.getSubject();
                creator = metadata.getCreator();
                publisher = metadata.getPublisher();
                type = metadata.getType();
                format = metadata.getFormat();
                source = metadata.getSource();
                language = metadata.getLanguage();
                relation = metadata.getRelation();
                contributor = metadata.getContributor();
                coverage = metadata.getCoverage();
                issn = metadata.getIssn();
                isbn = metadata.getIsbn();
            }
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }

        public String getPublisher() {
            return publisher;
        }

        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getRelation() {
            return relation;
        }

        public void setRelation(String relation) {
            this.relation = relation;
        }

        public String getContributor() {
            return contributor;
        }

        public void setContributor(String contributor) {
            this.contributor = contributor;
        }

        public String getCoverage() {
            return coverage;
        }

        public void setCoverage(String coverage) {
            this.coverage = coverage;
        }

        public String getIssn() {
            return issn;
        }

        public void setIssn(String issn) {
            this.issn = issn;
        }

        public String getIsbn() {
            return isbn;
        }

        public void setIsbn(String isbn) {
            this.isbn = isbn;
        }
    }

    public static class Group {
        @NotNull(message = "id is required")
        Long id;
        String name;

        public Group() {}

        public Group(GroupMember m) {
            id = m.getParent().getOid();
            name = m.getParent().getName();
        }

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

