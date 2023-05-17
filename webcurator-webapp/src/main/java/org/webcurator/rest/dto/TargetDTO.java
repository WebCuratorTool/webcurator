package org.webcurator.rest.dto;

import org.webcurator.domain.model.core.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TargetDTO {

    private static final String OVERRIDE_ID_FIELD = "id";
    private static final String OVERRIDE_VALUE_FIELD = "value";
    private static final String OVERRIDE_UNIT_FIELD = "unit";
    private static final String OVERRIDE_ENABLED_FIELD = "enabled";

    HashMap<String, Object> general = new HashMap<>();
    HashMap<String, Object> schedule = new HashMap<>();
    HashMap<String, Object> access = new HashMap<>();
    List<HashMap<String, Object>> seeds = new ArrayList<>();
    HashMap<String, Object> profile = new HashMap<>();
    HashMap<String, Object> annotations = new HashMap<>();
    HashMap<String, Object> description = new HashMap<>();
    List<HashMap<String, Object>> groups = new ArrayList<>();

    public TargetDTO(Target target) {
        populateGeneral(target);
        populateSchedule(target);
        populateAccess(target);
        populateSeeds(target);
        populateProfile(target);
        populateAnnotations(target);
        populateDescription(target);
        populateGroups(target);
    }

    private void populateGeneral(Target t) {
        general.put("id", t.getOid());
        general.put("creationDate", t.getCreationDate());
        general.put("name", t.getName());
        general.put("description", t.getDescription());
        general.put("referenceNumber", t.getReferenceNumber());
        general.put("runOnApproval", t.isRunOnApproval());
        general.put("owner", t.getOwner().getNiceName());
        general.put("state", t.getState());
        general.put("referenceCrawl", t.isAutoDenoteReferenceCrawl());
        general.put("requestToArchivists", t.getRequestToArchivists());
    }

    private void populateSchedule(Target t) {
        schedule.put("harvestOptimization", t.isAllowOptimize());
        ArrayList<HashMap<String, Object>> schedules = new ArrayList<>();
        for (Schedule s : t.getSchedules()) {
            HashMap<String, Object> sched = new HashMap<>();
            sched.put("id", s.getOid());
            sched.put("cron", s.getCronPattern());
            sched.put("startDate", s.getStartDate());
            sched.put("endDate", s.getEndDate());
            sched.put("type", s.getScheduleType());
            sched.put("nextExecutionDate", s.getNextExecutionDate());
            sched.put("lastProcessedDate", s.getLastProcessedDate());
            // FIXME in the current UI the "nice name" is used, but maybe we should use the userId
            // FIXME everywhere instead and leave the translation to "nice name" up to the client?
            sched.put("owner", s.getOwningUser().getNiceName());
            schedules.add(sched);
        }
        schedule.put("schedules", schedules);
    }

    private void populateAccess(Target t) {
        access.put("accessZone", t.getAccessZone());
        access.put("accessZoneText", t.getAccessZoneText());
        access.put("displayChangeReason", t.getDisplayChangeReason());
        access.put("displayNote", t.getDisplayNote());
    }

    private void populateSeeds(Target t) {
        for (Seed s : t.getSeeds()) {
            HashMap<String, Object> seed = new HashMap<>();
            seed.put("id", s.getOid().toString());
            seed.put("seed", s.getSeed());
            seed.put("primary", Boolean.toString(s.isPrimary()));
            ArrayList<Long> authorisations = new ArrayList<>();
            for (Permission p : s.getPermissions()) {
                // FIXME This is the identifier shown in the UI, but maybe we will need to use p.getOid()?
                authorisations.add(p.getSite().getOid());
            }
            seed.put("authorisations", authorisations);
            seeds.add(seed);
        }
    }

    private void populateProfile(Target t) {
        Profile p = t.getProfile();
        profile.put("harvesterType", p.getHarvesterType());
        profile.put("id", p.getOid());
        profile.put("imported", p.isImported());
        profile.put("name", p.getName());
        // FIXME what do we do in the case of an imported profile (which doesn't have overrides, but is overridden entirely by a new profile)?
        ProfileOverrides o = t.getProfileOverrides();
        ArrayList<HashMap<String, Object>> overrides = new ArrayList<>();
        // It might seem tempting to use reflection to condense all this code into one loop, but please don't:
        // it will be unreadable and it will introduce tight coupling between the API and the ProfileOverrides class
        if (p.isHeritrix3Profile()) {
            HashMap<String, Object> documentLimit = new HashMap<>();
            documentLimit.put(OVERRIDE_ID_FIELD, "documentLimit");
            documentLimit.put(OVERRIDE_VALUE_FIELD, o.getH3DocumentLimit());
            documentLimit.put(OVERRIDE_ENABLED_FIELD, o.isOverrideH3DocumentLimit());
            overrides.add(documentLimit);
            HashMap<String, Object> dataLimit = new HashMap<>();
            dataLimit.put(OVERRIDE_ID_FIELD, "dataLimit");
            dataLimit.put(OVERRIDE_VALUE_FIELD, o.getH3DataLimit());
            dataLimit.put(OVERRIDE_UNIT_FIELD, o.getH3DataLimitUnit());
            dataLimit.put(OVERRIDE_ENABLED_FIELD, o.isOverrideH3DataLimit());
            overrides.add(dataLimit);
            HashMap<String, Object> timeLimit = new HashMap<>();
            timeLimit.put(OVERRIDE_ID_FIELD, "timeLimit");
            timeLimit.put(OVERRIDE_VALUE_FIELD, o.getH3TimeLimit());
            timeLimit.put(OVERRIDE_UNIT_FIELD, o.getH3TimeLimitUnit());
            timeLimit.put(OVERRIDE_ENABLED_FIELD, o.isOverrideH3TimeLimit());
            overrides.add(timeLimit);
            HashMap<String, Object> maxPathDepth = new HashMap<>();
            maxPathDepth.put(OVERRIDE_ID_FIELD, "maxPathDepth");
            maxPathDepth.put(OVERRIDE_VALUE_FIELD, o.getH3MaxPathDepth());
            maxPathDepth.put(OVERRIDE_ENABLED_FIELD, o.isOverrideH3MaxPathDepth());
            overrides.add(maxPathDepth);
            HashMap<String, Object> maxHops = new HashMap<>();
            maxHops.put(OVERRIDE_ID_FIELD, "maxHops");
            maxHops.put(OVERRIDE_VALUE_FIELD, o.getH3MaxHops());
            maxHops.put(OVERRIDE_ENABLED_FIELD, o.isOverrideH3MaxHops());
            overrides.add(maxHops);
            HashMap<String, Object> maxTransitiveHops = new HashMap<>();
            maxTransitiveHops.put(OVERRIDE_ID_FIELD, "maxTransitiveHops");
            maxTransitiveHops.put(OVERRIDE_VALUE_FIELD, o.getH3MaxTransitiveHops());
            maxTransitiveHops.put(OVERRIDE_ENABLED_FIELD, o.isOverrideH3MaxTransitiveHops());
            overrides.add(maxTransitiveHops);
            HashMap<String, Object> ignoreRobots = new HashMap<>();
            ignoreRobots.put(OVERRIDE_ID_FIELD, "ignoreRobots");
            ignoreRobots.put(OVERRIDE_VALUE_FIELD, o.isH3IgnoreRobots());
            ignoreRobots.put(OVERRIDE_ENABLED_FIELD, o.isOverrideH3IgnoreRobots());
            overrides.add(ignoreRobots);
            HashMap<String, Object> extractJs = new HashMap<>();
            extractJs.put(OVERRIDE_ID_FIELD, "extractJs");
            extractJs.put(OVERRIDE_VALUE_FIELD, o.isH3ExtractJs());
            extractJs.put(OVERRIDE_ENABLED_FIELD, o.isOverrideH3ExtractJs());
            overrides.add(extractJs);
            HashMap<String, Object> ignoreCookies = new HashMap<>();
            ignoreCookies.put(OVERRIDE_ID_FIELD, "ignoreCookies");
            ignoreCookies.put(OVERRIDE_VALUE_FIELD, o.isH3IgnoreCookies());
            ignoreCookies.put(OVERRIDE_ENABLED_FIELD, o.isOverrideH3IgnoreCookies());
            overrides.add(ignoreCookies);
            HashMap<String, Object> blockedUrls = new HashMap<>();
            blockedUrls.put(OVERRIDE_ID_FIELD, "blockedUrls");
            blockedUrls.put(OVERRIDE_VALUE_FIELD, o.getH3BlockedUrls());
            blockedUrls.put(OVERRIDE_ENABLED_FIELD, o.isOverrideH3BlockedUrls());
            overrides.add(blockedUrls);
            HashMap<String, Object> includedUrls = new HashMap<>();
            includedUrls.put(OVERRIDE_ID_FIELD, "includedUrls");
            includedUrls.put(OVERRIDE_VALUE_FIELD, o.getH3IncludedUrls());
            includedUrls.put(OVERRIDE_ENABLED_FIELD, o.isOverrideH3IncludedUrls());
            overrides.add(includedUrls);
        } else { // Legacy H1 profile overrides
            HashMap<String, Object> robotsHonouringPolicy = new HashMap<>();
            robotsHonouringPolicy.put(OVERRIDE_ID_FIELD, "robotsHonouringPolicy");
            robotsHonouringPolicy.put(OVERRIDE_VALUE_FIELD, o.getRobotsHonouringPolicy());
            robotsHonouringPolicy.put(OVERRIDE_ENABLED_FIELD, o.isOverrideRobotsHonouringPolicy());
            overrides.add(robotsHonouringPolicy);
            HashMap<String, Object> maxTimeSec = new HashMap<>();
            maxTimeSec.put(OVERRIDE_ID_FIELD, "maxTimeSec");
            maxTimeSec.put(OVERRIDE_VALUE_FIELD, o.getMaxTimeSec());
            maxTimeSec.put(OVERRIDE_ENABLED_FIELD, o.isOverrideMaxTimeSec());
            overrides.add(maxTimeSec);
            HashMap<String, Object> maxBytesDownload = new HashMap<>();
            maxBytesDownload.put(OVERRIDE_ID_FIELD, "maxBytesDownload");
            maxBytesDownload.put(OVERRIDE_VALUE_FIELD, o.getMaxBytesDownload());
            maxBytesDownload.put(OVERRIDE_ENABLED_FIELD, o.isOverrideMaxBytesDownload());
            overrides.add(maxBytesDownload);
            HashMap<String, Object> maxHarvestDocuments = new HashMap<>();
            maxHarvestDocuments.put(OVERRIDE_ID_FIELD, "maxHarvestDocuments");
            maxHarvestDocuments.put(OVERRIDE_VALUE_FIELD, o.getMaxHarvestDocuments());
            maxHarvestDocuments.put(OVERRIDE_ENABLED_FIELD, o.isOverrideMaxHarvestDocuments());
            overrides.add(maxHarvestDocuments);
            HashMap<String, Object> maxPathDepth = new HashMap<>();
            maxPathDepth.put(OVERRIDE_ID_FIELD, "maxPathDepth");
            maxPathDepth.put(OVERRIDE_VALUE_FIELD, o.getMaxPathDepth());
            maxPathDepth.put(OVERRIDE_ENABLED_FIELD, o.isOverrideMaxPathDepth());
            overrides.add(maxPathDepth);
            HashMap<String, Object> maxLinkHops = new HashMap<>();
            maxLinkHops.put(OVERRIDE_ID_FIELD, "maxLinkHops");
            maxLinkHops.put(OVERRIDE_VALUE_FIELD, o.getMaxLinkHops());
            maxLinkHops.put(OVERRIDE_ENABLED_FIELD, o.isOverrideMaxLinkHops());
            overrides.add(maxLinkHops);
            HashMap<String, Object> excludeFilters = new HashMap<>();
            excludeFilters.put(OVERRIDE_ID_FIELD, "excludeFilters");
            excludeFilters.put(OVERRIDE_VALUE_FIELD, o.getExcludeUriFilters());
            excludeFilters.put(OVERRIDE_ENABLED_FIELD, o.isOverrideExcludeUriFilters());
            overrides.add(excludeFilters);
            HashMap<String, Object> includeFilters = new HashMap<>();
            includeFilters.put(OVERRIDE_ID_FIELD, "includeFilters");
            includeFilters.put(OVERRIDE_VALUE_FIELD, o.getIncludeUriFilters());
            includeFilters.put(OVERRIDE_ENABLED_FIELD, o.isOverrideIncludeUriFilters());
            overrides.add(includeFilters);
            HashMap<String, Object> excludedMimeTypes = new HashMap<>();
            excludedMimeTypes.put(OVERRIDE_ID_FIELD, "excludedMimeTypes");
            excludedMimeTypes.put(OVERRIDE_VALUE_FIELD, o.getExcludedMimeTypes());
            excludedMimeTypes.put(OVERRIDE_ENABLED_FIELD, o.isOverrideExcludedMimeTypes());
            HashMap<String, Object> credentials = new HashMap<>();
            credentials.put(OVERRIDE_ID_FIELD, "credentials");
            credentials.put(OVERRIDE_VALUE_FIELD, o.getCredentials());
            credentials.put(OVERRIDE_ENABLED_FIELD, o.isOverrideCredentials());
            overrides.add(credentials);
        }
        profile.put("overrides", overrides);
    }

    private void populateAnnotations(Target t) {
        HashMap<String, Object> selection = new HashMap<>();
        selection.put("date", t.getSelectionDate());
        selection.put("type", t.getSelectionType());
        selection.put("note", t.getSelectionNote());
        annotations.put("selection", selection);
        annotations.put("evalutionNote", t.getEvaluationNote());
        annotations.put("harvestType", t.getHarvestType());
        ArrayList<HashMap<String, Object>> annotationList = new ArrayList<>();
        for (Annotation a : t.getAnnotations()) {
            HashMap<String, Object> annotation = new HashMap<>();
            annotation.put("date", a.getDate());
            annotation.put("user", a.getUser().getNiceName()); // FIXME we use "owner" elsewhere...
            annotation.put("note", a.getNote());
            annotation.put("alert", a.isAlertable());
            annotationList.add(annotation);
        }
        annotations.put("annotations", annotationList);
    }

    private void populateDescription(Target t) {
        DublinCore metadata = t.getDublinCoreMetaData();
        description.put("identifier", metadata.getIdentifier());
        description.put("description", metadata.getDescription());
        description.put("subject", metadata.getSubject());
        description.put("creator", metadata.getCreator());
        description.put("publisher", metadata.getPublisher());
        description.put("contributor", metadata.getContributor());
        description.put("type", metadata.getType());
        description.put("format", metadata.getFormat());
        description.put("source", metadata.getSource());
        description.put("language", metadata.getLanguage());
        description.put("relation", metadata.getRelation());
        description.put("coverage", metadata.getCoverage());
        description.put("issn", metadata.getIssn());
        description.put("isbn", metadata.getIsbn());
    }

    private void populateGroups(Target t) {
        for (GroupMember m : t.getParents()) {
            HashMap<String, Object> group = new HashMap<>();
            group.put("id", m.getParent().getOid());
            group.put("name", m.getParent().getName());
            groups.add(group);
        }
    }

    public HashMap<String, Object> getGeneral() {
        return general;
    }

    public void setGeneral(HashMap<String, Object> general) {
        this.general = general;
    }

    public HashMap<String, Object> getSchedule() {
        return schedule;
    }

    public void setSchedule(HashMap<String, Object> schedule) {
        this.schedule = schedule;
    }

    public HashMap<String, Object> getAccess() {
        return access;
    }

    public void setAccess(HashMap<String, Object> access) {
        this.access = access;
    }

    public List<HashMap<String, Object>> getSeeds() {
        return seeds;
    }

    public void setSeeds(List<HashMap<String, Object>> seeds) {
        this.seeds = seeds;
    }

    public HashMap<String, Object> getProfile() {
        return profile;
    }

    public void setProfile(HashMap<String, Object> profile) {
        this.profile = profile;
    }

    public HashMap<String, Object> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(HashMap<String, Object> annotations) {
        this.annotations = annotations;
    }

    public HashMap<String, Object> getDescription() {
        return description;
    }

    public void setDescription(HashMap<String, Object> description) {
        this.description = description;
    }

    public List<HashMap<String, Object>> getGroups() {
        return groups;
    }

    public void setGroups(List<HashMap<String, Object>> groups) {
        this.groups = groups;
    }
}
