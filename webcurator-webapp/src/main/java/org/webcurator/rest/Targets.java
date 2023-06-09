package org.webcurator.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.webcurator.domain.*;
import org.webcurator.domain.model.auth.User;
import org.webcurator.domain.model.core.*;
import org.webcurator.rest.dto.TargetDTO;

import javax.validation.Valid;

import static org.webcurator.rest.dto.TargetDTO.Profile.OverrideWithUnit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
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

    private static final Log logger = LogFactory.getLog(Targets.class);

    @Autowired
    private TargetDAO targetDAO;

    @Autowired
    private UserRoleDAO userRoleDAO;

    @Autowired
    private ProfileDAO profileDAO;

    @Autowired
    private SiteDAO siteDAO;


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
            TargetDTO targetDTO = new TargetDTO(target);
            if (section == null) {
                // Return the entire target
                return ResponseEntity.ok().body(targetDTO);
            }
            switch (section) {
                case "access":
                    return ResponseEntity.ok().body(targetDTO.getAccess());
                case "annotation":
                    return ResponseEntity.ok().body(targetDTO.getAnnotations());
                case "description":
                    return ResponseEntity.ok().body(targetDTO.getDescription());
                case "general":
                    return ResponseEntity.ok().body(targetDTO.getGeneral());
                case "groups":
                    return ResponseEntity.ok().body(targetDTO.getGroups());
                case "profile":
                    return ResponseEntity.ok().body(targetDTO.getProfile());
                case "schedule":
                    return ResponseEntity.ok().body(targetDTO.getSchedule());
                case "seeds":
                    return ResponseEntity.ok().body(targetDTO.getSeeds());
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

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> post(@Valid @RequestBody TargetDTO targetDTO) {
        // FIXME deal with non-existent user
        User owner = userRoleDAO.getUserByName(targetDTO.getGeneral().getOwner());
        Target target = new Target();
        target.setCreationDate(new Date());
        target.setName(targetDTO.getGeneral().getName());
        target.setDescription(targetDTO.getGeneral().getDescription());
        target.setReferenceNumber(targetDTO.getGeneral().getReferenceNumber());
        target.setRunOnApproval(targetDTO.getGeneral().isRunOnApproval());
        target.setUseAQA(targetDTO.getGeneral().isAutomatedQA());
        target.setOwner(owner);
        target.setState(targetDTO.getGeneral().getState());
        target.setAutoPrune(targetDTO.getGeneral().isAutoPrune());
        target.setAutoDenoteReferenceCrawl(targetDTO.getGeneral().isReferenceCrawl());
        target.setRequestToArchivists(targetDTO.getGeneral().getRequestToArchivists());

        Set<Schedule> schedules = new HashSet<>();
        target.setAllowOptimize(targetDTO.getSchedule().isHarvestOptimization());
        for (TargetDTO.Scheduling.Schedule s : targetDTO.getSchedule().getSchedules()) {
            Schedule schedule = new Schedule();
            // FIXME validate cron expression
            schedule.setCronPattern(s.getCron());
            schedule.setStartDate(s.getStartDate());
            schedule.setEndDate(s.getEndDate());
            schedule.setScheduleType(s.getType());
            schedule.setOwningUser(owner);
        }
        target.setSchedules(schedules);

        target.setAccessZone(targetDTO.getAccess().getAccessZone());
        target.setDisplayChangeReason(targetDTO.getAccess().getDisplayChangeReason());
        target.setDisplayNote(targetDTO.getAccess().getDisplayNote());

        ArrayList<Seed> seeds = new ArrayList<>();
        for (TargetDTO.Seed s : targetDTO.getSeeds()) {
            Seed seed = new Seed();
            seed.setSeed(s.getSeed());
            seed.setPrimary(s.getPrimary());
            Set<Permission> permissions = new HashSet<>();
            for (long authorisation : s.getAuthorisations()) {
                // FIXME deal with non-existent permission
                Permission p = siteDAO.loadPermission(authorisation);
                permissions.add(p);
            }
            seed.setPermissions(permissions);
        }

        // FIXME deal with non-existent profile
        Profile profile = profileDAO.load(targetDTO.getProfile().getId());
        if (!profile.isHeritrix3Profile()) {
            return ResponseEntity.badRequest().body("Only Heritrix v3 profiles are supported");
        }
        target.setProfile(profile);

        ProfileOverrides profileOverrides = new ProfileOverrides();
        ArrayList<TargetDTO.Profile.Override> overrides = targetDTO.getProfile().getOverrides();
        if (!profile.isImported() && overrides.isEmpty()) {
            return ResponseEntity.badRequest().body("A target with a non-imported profile requires profile overrides");
        }
        // Use reflection to fill out the elaborate yet consistently named ProfileOverrides
        for (TargetDTO.Profile.Override override : overrides) {
            String id = override.getId();
            id = id.substring(0, 1).toUpperCase() + id.substring(1); // camel case
            String methodNameSetValue = "setH3" + id;
            String methodNameSetEnabled = "setOverrideH3" + id;
            String methodNameSetUnit = "setH3" + id + "Unit";
            try {
                if (override.getValue() != null) {
                    Class valueClass = override.getValue().getClass();
                    Object value = override.getValue();
                    if (value instanceof Integer) { // Spring assumes Integer where it should be Long
                        valueClass = Long.class;
                        value = Long.valueOf((Integer) value);
                    }
                    if (value instanceof Boolean) { // Boolean setters use primitive type
                        valueClass = boolean.class;
                        value = Boolean.valueOf((Boolean) value);
                    }
                    if (value instanceof ArrayList) { // List setters use the interface class
                        valueClass = List.class;
                    }
                    Method setValue = ProfileOverrides.class.getMethod(methodNameSetValue, valueClass);
                    setValue.invoke(profileOverrides, value);
                }
                Method setEnabled = ProfileOverrides.class.getMethod(methodNameSetEnabled, boolean.class);
                setEnabled.invoke(profileOverrides, override.getEnabled());
                if (override instanceof TargetDTO.Profile.OverrideWithUnit) {
                    if (((OverrideWithUnit) override).getUnit() != null) {
                        Method setUnit = ProfileOverrides.class.getMethod(methodNameSetUnit, ((OverrideWithUnit) override).getUnit().getClass());
                        setUnit.invoke(profileOverrides, ((OverrideWithUnit) override).getUnit());
                    }
                }
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                return ResponseEntity.internalServerError().body(e.getMessage());
            }
        }
        target.setOverrides(profileOverrides);

        target.setSelectionDate(targetDTO.getAnnotations().getSelection().getDate());
        target.setSelectionNote(targetDTO.getAnnotations().getSelection().getNote());
        target.setSelectionType(targetDTO.getAnnotations().getSelection().getType());
        target.setEvaluationNote(targetDTO.getAnnotations().getEvaluationNote());
        target.setHarvestType(targetDTO.getAnnotations().getHarvestType());
        ArrayList<Annotation> annotations = new ArrayList<>();
        for (TargetDTO.Annotations.Annotation a : targetDTO.getAnnotations().getAnnotations()) {
            Annotation annotation = new Annotation();
            annotation.setDate(a.getDate());
            annotation.setNote(a.getNote());
            // FIXME deal with non-existent user
            annotation.setUser(userRoleDAO.getUserByName(a.getUser()));
            annotation.setAlertable(a.isAlert());
            annotations.add(annotation);
        }
        target.setAnnotations(annotations);

        DublinCore metadata = new DublinCore();
        metadata.setIdentifier(targetDTO.getDescription().getIdentifier());
        metadata.setDescription(targetDTO.getDescription().getDescription());
        metadata.setSubject(targetDTO.getDescription().getSubject());
        metadata.setCreator(targetDTO.getDescription().getCreator());
        metadata.setPublisher(targetDTO.getDescription().getPublisher());
        metadata.setType(targetDTO.getDescription().getType());
        metadata.setFormat(targetDTO.getDescription().getFormat());
        metadata.setSource(targetDTO.getDescription().getSource());
        metadata.setRelation(targetDTO.getDescription().getRelation());
        metadata.setCoverage(targetDTO.getDescription().getCoverage());
        metadata.setIssn(targetDTO.getDescription().getIssn());
        metadata.setIsbn(targetDTO.getDescription().getIsbn());
        target.setDublinCoreMetaData(metadata);

        Set<GroupMember> groups = new HashSet<>();
        for (TargetDTO.Group g : targetDTO.getGroups()) {
            GroupMember groupMember = new GroupMember();
            groupMember.setOid(g.getId());
            groups.add(groupMember);
        }
        target.setParents(groups);

        try {
            targetDAO.save(target);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }

        try {
            String targetUrl = "http://localhost:8080/wct/api/v1/targets/" + target.getOid(); // FIXME hardcoded URL
            return ResponseEntity.created(new URI(targetUrl)).build();
        } catch (URISyntaxException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
                                                String fieldName = ((FieldError) error).getField();
                                                String errorMessage = error.getDefaultMessage();
                                                errors.put(fieldName, errorMessage);
                                            });
        return errors;
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
            targetSummaries.add(getTargetSummary(t));
        }
        return new SearchResult(pagination.getTotal(), targetSummaries);
    }

    /**
     * Create the summary target info used for search results
     */
    private HashMap<String, Object> getTargetSummary(Target t) {
        HashMap<String, Object> targetSummary = new HashMap<>();
        targetSummary.put("id", t.getOid());
        targetSummary.put("creationDate", t.getCreationDate());
        targetSummary.put("name", t.getName());
        targetSummary.put("agency", t.getOwner().getAgency().getName());
        targetSummary.put("owner", t.getOwner().getUsername());
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
