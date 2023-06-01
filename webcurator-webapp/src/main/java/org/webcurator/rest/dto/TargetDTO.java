package org.webcurator.rest.dto;

import org.webcurator.domain.model.core.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class is used for mapping between the Target entity and the JSON representation of a target in the API.
 */
public class TargetDTO {

    General general;
    Scheduling schedule;
    Access access;
    ArrayList<Seed> seeds;
    Profile profile;
    Annotations annotations;
    Description description;
    ArrayList<Group> groups;

    public TargetDTO() {
    }

    public TargetDTO(Target target) {
        general = new General(target);
        schedule = new Scheduling(target);
        access = new Access(target);
        seeds = new ArrayList<>();
        for (org.webcurator.domain.model.core.Seed s : target.getSeeds()) {
            seeds.add(new Seed(s));
        }
        profile = new Profile(target);
        annotations = new Annotations(target);
        description = new Description(target);
        groups = new ArrayList<>();
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

    public void setSeeds(ArrayList<Seed> seeds) {
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

    public ArrayList<Group> getGroups() {
        return groups;
    }

    public void setGroups(ArrayList<Group> groups) {
        this.groups = groups;
    }

    public static class General {
        long id;
        Date creationDate;
        String name;
        String description;
        String referenceNumber;
        boolean runOnApproval;
        String owner;
        Integer state;
        boolean referenceCrawl;
        String requestToArchivists;

        public General() {
        }

        public General(Target target) {
            id = target.getOid();
            creationDate = target.getCreationDate();
            name = target.getName();
            description = target.getDescription();
            referenceNumber = target.getReferenceNumber();
            runOnApproval = target.isRunOnApproval();
            owner = target.getOwner().getUsername();
            state = target.getState();
            referenceCrawl = target.isAutoDenoteReferenceCrawl();
            requestToArchivists = target.getRequestToArchivists();
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public Date getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(Date creationDate) {
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

        public boolean isRunOnApproval() {
            return runOnApproval;
        }

        public void setRunOnApproval(boolean runOnApproval) {
            this.runOnApproval = runOnApproval;
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

        public boolean isReferenceCrawl() {
            return referenceCrawl;
        }

        public void setReferenceCrawl(boolean referenceCrawl) {
            this.referenceCrawl = referenceCrawl;
        }

        public String getRequestToArchivists() {
            return requestToArchivists;
        }

        public void setRequestToArchivists(String requestToArchivists) {
            this.requestToArchivists = requestToArchivists;
        }
    }

    public static class Scheduling {

        boolean harvestOptimization;
        ArrayList<Schedule> schedules;

        public Scheduling() {
        }

        public Scheduling(Target target) {
            harvestOptimization = target.isAllowOptimize();
            schedules = new ArrayList<>();
            for (org.webcurator.domain.model.core.Schedule s : target.getSchedules()) {
                Schedule schedule = new Schedule();
                schedule.setId(s.getOid());
                schedule.setCron(s.getCronPattern());
                schedule.setStartDate(s.getStartDate());
                schedule.setEndDate(s.getEndDate());
                schedule.setType(s.getScheduleType());
                schedule.setNextExecutionDate(s.getNextExecutionDate());
                schedule.setLastProcessedDate(s.getLastProcessedDate());
                // FIXME in the current UI the "nice name" is used, but maybe we should use the userId
                // FIXME everywhere instead and leave the translation to "nice name" up to the client? <-- Yes!
                schedule.setOwner(s.getOwningUser().getUsername());
                schedules.add(schedule);
            }
        }

        public boolean isHarvestOptimization() {
            return harvestOptimization;
        }

        public void setHarvestOptimization(boolean harvestOptimization) {
            this.harvestOptimization = harvestOptimization;
        }

        public ArrayList<Schedule> getSchedules() {
            return schedules;
        }

        public void setSchedules(ArrayList<Schedule> schedules) {
            this.schedules = schedules;
        }

        public static class Schedule {
            long id;
            String cron;
            Date startDate;
            Date endDate;
            int type;
            Date nextExecutionDate;
            Date lastProcessedDate;
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

            public int getType() {
                return type;
            }

            public void setType(int type) {
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
        int accessZone;
        String accessZoneText;
        String displayChangeReason;
        String displayNote;

        public Access() {
        }

        public Access(Target target) {
            accessZone = target.getAccessZone();
            accessZoneText = target.getAccessZoneText();
            displayChangeReason = target.getDisplayChangeReason();
            displayNote = target.getDisplayNote();
        }

        public int getAccessZone() {
            return accessZone;
        }

        public void setAccessZone(int accessZone) {
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
        String seed;
        boolean primary;
        ArrayList<Long> authorisations;

        public Seed() {
        }

        public Seed(org.webcurator.domain.model.core.Seed s) {
            id = s.getOid();
            seed = s.getSeed();
            primary = s.isPrimary();
            authorisations = new ArrayList<>();
            for (Permission p : s.getPermissions()) {
                authorisations.add(p.getOid());
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

        public boolean isPrimary() {
            return primary;
        }

        public void setPrimary(boolean primary) {
            this.primary = primary;
        }

        public ArrayList<Long> getAuthorisations() {
            return authorisations;
        }

        public void setAuthorisations(ArrayList<Long> authorisations) {
            this.authorisations = authorisations;
        }
    }

    public static class Profile {

        String harvesterType;
        long id;
        boolean imported;
        String name;
        ArrayList<Override> overrides;


        public Profile() {
        }

        public String getHarvesterType() {
            return harvesterType;
        }

        public void setHarvesterType(String harvesterType) {
            this.harvesterType = harvesterType;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
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

        public ArrayList<Override> getOverrides() {
            return overrides;
        }

        public void setOverrides(ArrayList<Override> overrides) {
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
            overrides = new ArrayList<>();
            if (p.isHeritrix3Profile()) {
                Override documentLimit = new Override();
                documentLimit.setId("documentLimit");
                documentLimit.setValue(o.getH3DocumentLimit());
                documentLimit.setEnabled(o.isOverrideH3DocumentLimit());
                overrides.add(documentLimit);
                OverrideWithUnit dataLimit = new OverrideWithUnit();
                dataLimit.setId("dataLimit");
                dataLimit.setValue(o.getH3DataLimit());
                dataLimit.setUnit(o.getH3DataLimitUnit());
                dataLimit.setEnabled(o.isOverrideH3DataLimit());
                overrides.add(dataLimit);
                OverrideWithUnit timeLimit = new OverrideWithUnit();
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
            boolean enabled;

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

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        }

        public static class OverrideWithUnit extends Override {
            String unit;

            public OverrideWithUnit() {
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

        Selection selection;
        String evaluationNote;
        String harvestType;
        ArrayList<Annotation> annotations;

        public Annotations() {}

        public Annotations(Target target) {
            selection = new Selection();
            selection.setDate(target.getSelectionDate());
            selection.setNote(target.getSelectionNote());
            selection.setType(target.getSelectionType());
            evaluationNote = target.getEvaluationNote();
            harvestType = target.getHarvestType();
            annotations = new ArrayList<>();
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

        public ArrayList<Annotation> getAnnotations() {
            return annotations;
        }

        public void setAnnotations(ArrayList<Annotation> annotations) {
            this.annotations = annotations;
        }

        public static class Selection {
            Date date;
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
            boolean alert;

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

            public boolean isAlert() {
                return alert;
            }

            public void setAlert(boolean alert) {
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
        String source;
        String relation;
        String coverage;
        String issn;
        String isbn;

        public Description() {}

        public Description(Target target) {
            DublinCore metadata = target.getDublinCoreMetaData();
            identifier = metadata.getIdentifier();
            description = metadata.getDescription();
            subject = metadata.getSubject();
            creator = metadata.getCreator();
            publisher = metadata.getPublisher();
            type = metadata.getType();
            format = metadata.getFormat();
            source = metadata.getSource();
            relation = metadata.getRelation();
            coverage = metadata.getCoverage();
            issn = metadata.getIssn();
            isbn = metadata.getIsbn();
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

        public String getRelation() {
            return relation;
        }

        public void setRelation(String relation) {
            this.relation = relation;
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
        long id;
        String name;

        public Group() {}

        public Group(GroupMember m) {
            id = m.getParent().getOid();
            name = m.getParent().getName();
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
    }

}

