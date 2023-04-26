package org.webcurator.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.webcurator.domain.Pagination;
import org.webcurator.domain.TargetDAO;
import org.webcurator.domain.model.core.*;

import java.util.*;

/**
 * Handlers for the targets endpoint
 */
@RestController
@RequestMapping(path = "/api/{version}/targets")
public class Targets {


    private static final int DEFAULT_PAGE_LIMIT = 10;
    private static final String DEFAULT_SORT_BY = "name,asc";

    // Response field names that are used more than once
    private static final String SORT_BY_FIELD = "sortBy";
    private static final String OFFSET_FIELD = "offset";
    private static final String LIMIT_FIELD = "limit";
    private static final String OVERRIDE_ID_FIELD = "id";
    private static final String OVERRIDE_VALUE_FIELD = "value";
    private static final String OVERRIDE_UNIT_FIELD = "unit";
    private static final String OVERRIDE_ENABLED_FIELD = "enabled";

    // names of target subsections
    private static final String TARGET_GENERAL = "general";
    private static final String TARGET_SEEDS = "seeds";
    private static final String TARGET_PROFILE = "profile";
    private static final String TARGET_SCHEDULE = "schedule";
    private static final String TARGET_ANNOTATIONS = "annotations";
    private static final String TARGET_DESCRIPTION = "description";
    private static final String TARGET_GROUPS = "groups";
    private static final String TARGET_ACCESS = "access";

    private static final Log logger = LogFactory.getLog(Targets.class);


    @Autowired
    private TargetDAO targetDAO;

    @GetMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> get(@RequestBody(required = false) SearchParams searchParams) {
        if (searchParams == null) {
            searchParams = new SearchParams();
        }
        Filter filter = searchParams.getFilter();
        Integer offset = searchParams.getOffset();
        Integer limit = searchParams.getLimit();
        String sortBy = searchParams.getSortBy();
        try {
            SearchResult searchResult = search(filter, offset, limit, sortBy);
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("filter", filter);
            responseMap.put("targets", searchResult.targetSummaries);
            if (sortBy != null) {
                responseMap.put(SORT_BY_FIELD, sortBy);
            } else {
                responseMap.put(SORT_BY_FIELD, DEFAULT_SORT_BY);
            }
            if (limit != null) {
                responseMap.put(LIMIT_FIELD, limit);
            } else {
                responseMap.put(LIMIT_FIELD, DEFAULT_PAGE_LIMIT);
            }
            if (offset != null) {
                responseMap.put(OFFSET_FIELD, offset);
            } else {
                responseMap.put(OFFSET_FIELD, 0);
            }
            responseMap.put("amount", searchResult.amount);
            ResponseEntity<HashMap<String, Object>> response = ResponseEntity.ok().body(responseMap);
            return response;
        } catch (BadRequestError e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET handler for individual targets and target sections
     */
    @GetMapping(path = {"/{id}", "/{id}/{section}"})
    public ResponseEntity<?> get(@PathVariable long id, @PathVariable(required = false) String section) {
        Target target = targetDAO.load(id, true);
        if (target == null) {
            return ResponseEntity.notFound().build();
        } else {
            if (section == null) {
                return ResponseEntity.ok().body(createTarget(target));
            }
            switch (section) {
                case TARGET_ACCESS:
                    return ResponseEntity.ok().body(createTargetAccess(target));
                case TARGET_ANNOTATIONS:
                    return ResponseEntity.ok().body(createTargetAnnotations(target));
                case TARGET_DESCRIPTION:
                    return ResponseEntity.ok().body(createTargetDescription(target));
                case TARGET_GENERAL:
                    return ResponseEntity.ok().body(createTargetGeneral(target));
                case TARGET_GROUPS:
                    return ResponseEntity.ok().body(createTargetGroups(target));
                case TARGET_PROFILE:
                    return ResponseEntity.ok().body(createTargetProfile(target));
                case TARGET_SCHEDULE:
                    return ResponseEntity.ok().body(createTargetSchedule(target));
                case TARGET_SEEDS:
                    return ResponseEntity.ok().body(createTargetSeeds(target));
                default:
                    return ResponseEntity.badRequest().body(String.format("No such target section: %s", section));
            }
        }
    }


    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        Target target = targetDAO.load(id);
        if (target == null) {
            return ResponseEntity.notFound().build();
        } else {
            if (target.getCrawls() > 0) {
                return ResponseEntity.badRequest().body("Target could not be deleted because it has related target instances");
            } else if (target.getState() != Target.STATE_REJECTED && target.getState() != Target.STATE_CANCELLED) {
                return ResponseEntity.badRequest().body("Target could not be deleted because its state is not Rejected or Cancelled");
            } else {
                targetDAO.delete(target);
                return ResponseEntity.ok().build();
            }
        }
    }


    /**
     * Handle the actual search using the old Target DAO search API
     */
    private SearchResult search(Filter filter, Integer offset, Integer limit, String sortBy) throws BadRequestError {

        // defaults
        if (limit == null) {
            limit = DEFAULT_PAGE_LIMIT;
        }
        if (offset == null) {
            offset = 0;
        }

        // magic to comply with the sort spec of the TargetDao API
        String magicSortStringForDao = null;
        if (sortBy != null) {
            String[] sortSpec = sortBy.split(",");
            if (sortSpec.length == 2) {
                if (sortSpec[0].trim().equalsIgnoreCase("name")) {
                    magicSortStringForDao = "name";
                } else if (sortSpec[0].trim().equalsIgnoreCase("creationDate")) {
                    magicSortStringForDao = "date";
                }
                if (magicSortStringForDao != null && (sortSpec[1].equalsIgnoreCase("asc") || sortSpec[1].equalsIgnoreCase("desc"))) {
                    magicSortStringForDao += sortSpec[1];
                }
            }
            if (magicSortStringForDao == null) {
                throw new BadRequestError("Unsupported or malformed sort spec: " + sortBy);
            }
        }

        if (limit < 1) {
            throw new BadRequestError("Limit must be positive");
        }
        if (offset < 0) {
            throw new BadRequestError("Offset may not be negative");
        }
        // The TargetDao API only supports offsets that are a multiple of limit
        int pageNumber = offset / limit;

        Pagination pagination = targetDAO.search(pageNumber, limit, filter.targetId, filter.name,
                filter.states, filter.seed, filter.userId, filter.agency, filter.groupName,
                filter.nonDisplayOnly, magicSortStringForDao, filter.description);
        List<HashMap<String, Object>> targetSummaries = new ArrayList<>();
        for (Target t : (List<Target>) pagination.getList()) {
            targetSummaries.add(createTargetSummary(t));
        }
        return new SearchResult(pagination.getTotal(), targetSummaries);
    }

    /**
     * Create the summary target info used for search results
     */
    private HashMap<String, Object> createTargetSummary(Target t) {
        HashMap<String, Object> targetSummary = new HashMap<>();
        targetSummary.put("id", t.getOid());
        targetSummary.put("creationDate", t.getCreationDate());
        targetSummary.put("name", t.getName());
        targetSummary.put("agency", t.getOwner().getAgency().getName());
        targetSummary.put("owner", t.getOwner().getNiceName());
        targetSummary.put("state", t.getState());
        ArrayList<HashMap<String, Object>> seeds = new ArrayList<>();
        for (Seed s : t.getSeeds()) {
            HashMap<String, Object> seed = new HashMap<>();
            seed.put("seed", s.getSeed());
            seed.put("primary", s.isPrimary());
            seeds.add(seed);
        }
        targetSummary.put("seeds", seeds);
        return targetSummary;
    }

    /**
     * Create the full target info used for retrieval of individual targets
     */
    private HashMap<String, Object> createTarget(Target t) {
        HashMap<String, Object> target = new HashMap<>();
        target.put(TARGET_GENERAL, createTargetGeneral(t));
        target.put(TARGET_SEEDS, createTargetSeeds(t));
        target.put(TARGET_PROFILE, createTargetProfile(t));
        target.put(TARGET_SCHEDULE, createTargetSchedule(t));
        target.put(TARGET_ANNOTATIONS, createTargetAnnotations(t));
        target.put(TARGET_DESCRIPTION, createTargetDescription(t));
        target.put(TARGET_GROUPS, createTargetGroups(t));
        target.put(TARGET_ACCESS, createTargetAccess(t));
        return target;
    }

    /**
     * Create the general section of an individual target
     */
    private HashMap<String, Object> createTargetGeneral(Target t) {
        HashMap<String, Object> general = new HashMap<>();
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
        return general;
    }

    /**
     * Create the seeds section of an individual target
     */
    private ArrayList<HashMap<String, Object>> createTargetSeeds(Target t) {
        ArrayList<HashMap<String, Object>> seeds = new ArrayList<>();
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
        return seeds;
    }

    /**
     * Create the profile section of an individual target
     */
    private HashMap<String, Object> createTargetProfile(Target t) {
        Profile p = t.getProfile();
        HashMap<String, Object> profile = new HashMap<>();
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
        return profile;
    }

    /**
     * Create the schedule section of an individual target
     */
    private HashMap<String, Object> createTargetSchedule(Target t) {
        HashMap<String, Object> schedule = new HashMap<>();
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
        return schedule;
    }

    /**
     * Create the annotations section of an individual target
     */
    private HashMap<String, Object> createTargetAnnotations(Target t) {
        HashMap<String, Object> annotations = new HashMap<>();
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
        return annotations;
    }

    /**
     * Create the description section of an individual target
     */
    private HashMap<String, Object> createTargetDescription(Target t) {
        HashMap<String, Object> description = new HashMap<>();
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
        return description;
    }

    /**
     * Create the groups section of an individual target
     */
    private ArrayList<HashMap<String, Object>> createTargetGroups(Target t) {
        ArrayList<HashMap<String, Object>> groups = new ArrayList<>();
        for (GroupMember m : t.getParents()) {
            HashMap<String, Object> group = new HashMap<>();
            group.put("id", m.getParent().getOid());
            group.put("name", m.getParent().getName());
            groups.add(group);
        }
        return groups;
    }

    /**
     * Create the access section of an individual target
     */
    private HashMap<String, Object> createTargetAccess(Target t) {
        HashMap<String, Object> access = new HashMap<>();
        access.put("accessZone", t.getAccessZone());
        access.put("accessZoneText", t.getAccessZoneText());
        access.put("displayChangeReason", t.getDisplayChangeReason());
        access.put("displayNote", t.getDisplayNote());
        return access;
    }


    private class BadRequestError extends Exception {
        BadRequestError(String msg) {
            super(msg);
        }
    }


    /**
     * POJO that the framework maps the JSON query data into
     */
    private static class SearchParams {
        private Filter filter;
        private Integer offset;
        private Integer limit;
        private String sortBy;

        SearchParams() {
            filter = new Filter();
        }

        public Filter getFilter() {
            return filter;
        }

        public void setFilter(Filter filter) {
            this.filter = filter;
        }

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public String getSortBy() {
            return sortBy;
        }

        public void setSortBy(String sortBy) {
            this.sortBy = sortBy;
        }
    }

    /**
     * Wrapper for the search filter
     */
    private static class Filter {
        private Long targetId;
        private String name;
        private String seed;
        private String agency;
        private String userId;
        private String description;
        private String groupName;
        private boolean nonDisplayOnly;
        private Set<Integer> states;

        public Long getTargetId() {
            return targetId;
        }

        public void setTargetId(Long targetId) {
            this.targetId = targetId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSeed() {
            return seed;
        }

        public void setSeed(String seed) {
            this.seed = seed;
        }

        public String getAgency() {
            return agency;
        }

        public void setAgency(String agency) {
            this.agency = agency;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public boolean isNonDisplayOnly() {
            return nonDisplayOnly;
        }

        public void setNonDisplayOnly(boolean nonDisplayOnly) {
            this.nonDisplayOnly = nonDisplayOnly;
        }

        public Set<Integer> getStates() {
            return states;
        }

        public void setStates(Set<Integer> states) {
            this.states = states;
        }
    }

    /**
     * Wraps search result data: a count of the total number of hits and a list of target summaries
     * for the current result page
     */
    private class SearchResult {
        public long amount;
        public List<HashMap<String, Object>> targetSummaries;

        SearchResult(long amount, List<HashMap<String, Object>> targetSummaries) {
            this.amount = amount;
            this.targetSummaries = targetSummaries;
        }
    }

}
