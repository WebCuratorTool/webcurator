package org.webcurator.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
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
import org.webcurator.domain.model.dto.GroupMemberDTO;
import org.webcurator.rest.dto.TargetDTO;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static org.webcurator.rest.dto.TargetDTO.Profile.OverrideWithUnit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
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

    @Autowired
    private BusinessObjectFactory businessObjectFactory;


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
            return ResponseEntity.badRequest().body(errorMessage(e.getMessage()));
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
                    return ResponseEntity.badRequest().body(errorMessage(String.format("No such target section: %s", section)));
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
                return ResponseEntity.badRequest().body(errorMessage("Target could not be deleted because it has related target instances"));
            } else if (target.getState() != Target.STATE_REJECTED && target.getState() != Target.STATE_CANCELLED) {
                return ResponseEntity.badRequest().body(errorMessage("Target could not be deleted because its state is not Rejected or Cancelled"));
            } else {
                targetDAO.delete(target);
                return ResponseEntity.ok().build();
            }
        }
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> post(@Valid @RequestBody TargetDTO targetDTO, HttpServletRequest request) {
        String ownerStr = targetDTO.getGeneral().getOwner();
        User owner = userRoleDAO.getUserByName(ownerStr);
        if (owner == null) {
            return ResponseEntity.badRequest().body(errorMessage(
                    String.format("Owner with username %s unknown", ownerStr)));
        }
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

        if (targetDTO.getSchedule() != null) {
            Set<Schedule> schedules = new HashSet<>();
            target.setAllowOptimize(targetDTO.getSchedule().isHarvestOptimization());
            for (TargetDTO.Scheduling.Schedule s : targetDTO.getSchedule().getSchedules()) {
                Schedule schedule = businessObjectFactory.newSchedule(target);
                // we support classic cron, without the prepended SECONDS field expected by Quartz
                String cronExpression = "0 " + s.getCron();
                try {
                    new CronExpression(cronExpression);
                } catch (ParseException ex) {
                    return ResponseEntity.badRequest().body(String.format("Invalid cron expression: %s", ex.getMessage()));
                }
                schedule.setCronPattern(cronExpression);
                schedule.setStartDate(s.getStartDate());
                schedule.setEndDate(s.getEndDate());
                schedule.setScheduleType(s.getType());
                owner = userRoleDAO.getUserByName(s.getOwner());
                if (owner == null) {
                    return ResponseEntity.badRequest().body(errorMessage(String.format("Owner with username %s unknown",
                            targetDTO.getGeneral().getOwner())));
                }
                schedule.setOwningUser(owner);
                schedules.add(schedule);
            }
            target.setSchedules(schedules);
        }

        if (targetDTO.getAccess() != null) {
            target.setAccessZone(targetDTO.getAccess().getAccessZone());
            target.setDisplayChangeReason(targetDTO.getAccess().getDisplayChangeReason());
            target.setDisplayNote(targetDTO.getAccess().getDisplayNote());
        }

        if (targetDTO.getSeeds() != null) {
            Set<Seed> seeds = new HashSet<>();
            for (TargetDTO.Seed s : targetDTO.getSeeds()) {
                Seed seed = businessObjectFactory.newSeed(target);
                seed.setSeed(s.getSeed());
                seed.setPrimary(s.getPrimary());
                Set<Permission> permissions = new HashSet<>();
                for (long authorisation : s.getAuthorisations()) {
                    try {
                        Permission p = siteDAO.loadPermission(authorisation);
                        permissions.add(p);
                    } catch (ObjectNotFoundException e) {
                        return ResponseEntity.badRequest().body(errorMessage(
                                String.format("Uknown authorisation: %s", authorisation)));
                    }
                }
                seed.setPermissions(permissions);
                seeds.add(seed);
            }
            target.setSeeds(seeds);
        }

        if (targetDTO.getProfile() != null) {
            long profileId = targetDTO.getProfile().getId();
            Profile profile = profileDAO.get(profileId);
            if (profile == null) {
                return ResponseEntity.badRequest().body(errorMessage(String.format("Profile with id %s does not exist", profileId)));
            }
            if (!profile.isHeritrix3Profile()) {
                return ResponseEntity.badRequest().body(errorMessage("Only Heritrix v3 profiles are supported"));
            }
            target.setProfile(profile);

            ProfileOverrides profileOverrides = new ProfileOverrides();
            ArrayList<TargetDTO.Profile.Override> overrides = targetDTO.getProfile().getOverrides();
            if (!profile.isImported() && overrides.isEmpty()) {
                return ResponseEntity.badRequest().body(errorMessage("A target with a non-imported profile requires profile overrides"));
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
                    return ResponseEntity.internalServerError().body(errorMessage(e.getMessage()));
                }
            }
            target.setOverrides(profileOverrides);
        } else {
            target.setProfile(profileDAO.getDefaultProfile(owner.getAgency()));
        }

        // FIXME The List of Annotations is transient so I have no idea how they're supposed to be persisted (and indeed, this does not work)
        if (targetDTO.getAnnotations() != null) {
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
                String userName = a.getUser();
                User user = userRoleDAO.getUserByName(userName);
                if (user == null) {
                    return ResponseEntity.badRequest().body(errorMessage(String.format("User %s does not exist", userName)));
                }
                annotation.setUser(user);
                annotation.setAlertable(a.getAlert());
                annotation.setObjectType(Target.class.getName());
                annotation.setObjectOid(null);
                annotations.add(annotation);
            }
            target.setAnnotations(annotations);
        }

        if (targetDTO.getDescription() != null) {
            DublinCore metadata = new DublinCore();
            metadata.setIdentifier(targetDTO.getDescription().getIdentifier());
            metadata.setDescription(targetDTO.getDescription().getDescription());
            metadata.setSubject(targetDTO.getDescription().getSubject());
            metadata.setCreator(targetDTO.getDescription().getCreator());
            metadata.setPublisher(targetDTO.getDescription().getPublisher());
            metadata.setType(targetDTO.getDescription().getType());
            metadata.setFormat(targetDTO.getDescription().getFormat());
            metadata.setSource(targetDTO.getDescription().getSource());
            metadata.setLanguage(targetDTO.getDescription().getLanguage());
            metadata.setRelation(targetDTO.getDescription().getRelation());
            metadata.setCoverage(targetDTO.getDescription().getCoverage());
            metadata.setIssn(targetDTO.getDescription().getIssn());
            metadata.setIsbn(targetDTO.getDescription().getIsbn());
            target.setDublinCoreMetaData(metadata);
        }

        // FIXME handle saving of group membership
//        if (targetDTO.getGroups() != null) {
//            List<GroupMemberDTO> groups = new ArrayList<>();
//            for (TargetDTO.Group g : targetDTO.getGroups()) {
//                GroupMemberDTO groupMemberDTO = new GroupMemberDTO(g.getId(), target.getOid());
//                groups.add(groupMemberDTO);
//            }
//            targetDAO.save(target, groups);
//        }

        // Finally persist the target
        try {
            targetDAO.save(target);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(errorMessage(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(errorMessage(e.getMessage()));
        }

        try {
            String targetUrl = request.getRequestURL().toString();
            if (!targetUrl.endsWith("/")) {
                targetUrl += "/";
            }
            targetUrl += target.getOid();
            return ResponseEntity.created(new URI(targetUrl)).build();
        } catch (URISyntaxException e) {
            return ResponseEntity.internalServerError().body(errorMessage(e.getMessage()));
        }
    }

    /**
     * Handler used by the validation API to generate error messages
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public HashMap<String, Map<String, String>> errorMessage(MethodArgumentNotValidException ex) {
        HashMap<String, Map<String, String>> map = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        map.put("Error", errors);
        return map;
    }

    /**
     * Error message formatter used in situations other than exceptions thrown by the validation API
     */
    private HashMap<String, Object> errorMessage(Object msg) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("Error", msg);
        return map;
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
