package org.webcurator.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.webcurator.core.targets.TargetManager2;
import org.webcurator.core.util.WctUtils;
import org.webcurator.domain.*;
import org.webcurator.domain.model.auth.User;
import org.webcurator.domain.model.core.*;
import org.webcurator.domain.model.dto.GroupMemberDTO;
import org.webcurator.rest.common.BadRequestError;
import org.webcurator.rest.common.Utils;
import org.webcurator.rest.dto.ProfileDTO;
import org.webcurator.rest.dto.ScheduleDTO;
import org.webcurator.rest.dto.TargetDTO;

import javax.servlet.http.HttpServletRequest;
import javax.validation.*;
import java.lang.reflect.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private Validator validator = factory.getValidator();

    @Autowired
    private TargetDAO targetDAO;

    @Autowired
    private TargetManager2 targetManager;

    @Autowired
    private UserRoleDAO userRoleDAO;

    @Autowired
    private ProfileDAO profileDAO;

    @Autowired
    private SiteDAO siteDAO;

    @Autowired
    private AnnotationDAO annotationDAO;

    @Autowired
    private BusinessObjectFactory businessObjectFactory;

    private Map<Integer, String> stateMap = new TreeMap<>();

    public Targets() {
        stateMap.put(Target.STATE_PENDING, "Pending");
        stateMap.put(Target.STATE_REINSTATED, "Reinstated");
        stateMap.put(Target.STATE_NOMINATED, "Nominated");
        stateMap.put(Target.STATE_REJECTED, "Rejected");
        stateMap.put(Target.STATE_APPROVED, "Approved");
        stateMap.put(Target.STATE_CANCELLED, "Cancelled");
        stateMap.put(Target.STATE_COMPLETED, "Completed");
    }

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
            return ResponseEntity.badRequest().body(Utils.errorMessage(e.getMessage()));
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
        }
        // Annotations are managed differently from normal associated entities
        target.setAnnotations(annotationDAO.loadAnnotations(WctUtils.getPrefixClassName(target.getClass()), id));
        TargetDTO targetDTO = new TargetDTO(target);
        if (section == null) {
            // Return the entire target
            return ResponseEntity.ok().body(targetDTO);
        }
        switch (section) {
            case "access":
                return ResponseEntity.ok().body(targetDTO.getAccess());
            case "annotations":
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
                return ResponseEntity.badRequest().body(Utils.errorMessage(String.format("No such target section: %s", section)));
        }
    }

    /**
     * Handler for deleting individual targets
     */
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        Target target = targetDAO.load(id);
        if (target == null) {
            return ResponseEntity.notFound().build();
        } else {
            if (target.getCrawls() > 0) {
                return ResponseEntity.badRequest().body(Utils.errorMessage("Target could not be deleted because it has related target instances"));
            } else if (target.getState() != Target.STATE_REJECTED && target.getState() != Target.STATE_CANCELLED) {
                return ResponseEntity.badRequest().body(Utils.errorMessage("Target could not be deleted because its state is not Rejected or Cancelled"));
            } else {
                targetDAO.delete(target);
                // Annotations are managed differently from normal associated entities
                annotationDAO.deleteAnnotations(annotationDAO.loadAnnotations(WctUtils.getPrefixClassName(target.getClass()), id));
                return ResponseEntity.ok().build();
            }
        }
    }

    /**
     * Handler for creating new targets
     */
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> post(@Valid @RequestBody TargetDTO targetDTO, HttpServletRequest request) {
        Target target = new Target();
        try {
            upsert(target, targetDTO);
        } catch (BadRequestError e) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Utils.errorMessage(e.getMessage()));
        }

        try {
            String targetUrl = request.getRequestURL().toString();
            if (!targetUrl.endsWith("/")) {
                targetUrl += "/";
            }
            targetUrl += target.getOid();
            return ResponseEntity.created(new URI(targetUrl)).build();
        } catch (URISyntaxException e) {
            return ResponseEntity.internalServerError().body(Utils.errorMessage(e.getMessage()));
        }
    }

    /**
     * Handler for updating individual targets
     */
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> put(@PathVariable long id, @RequestBody HashMap<String, Object> targetMap, HttpServletRequest request) {
        Target target = targetDAO.load(id, true);
        if (target == null) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(String.format("Target with id %s does not exist", id)));
        }
        // Annotations are managed differently from normal associated entities
        target.setAnnotations(annotationDAO.loadAnnotations(WctUtils.getPrefixClassName(target.getClass()), id));
        // Create DTO based on the current data in the database
        TargetDTO targetDTO = new TargetDTO(target);
        // Then update the DTO with data from the supplied JSON
        try {
            Utils.mapToDTO(targetMap, targetDTO);
        } catch (BadRequestError e) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Utils.errorMessage(e.getMessage()));
        }

        // Validate updated DTO
        Set<ConstraintViolation<TargetDTO>> violations = validator.validate(targetDTO);
        if (!violations.isEmpty()) {
            // Return the first violation we find
            ConstraintViolation<TargetDTO> constraintViolation = violations.iterator().next();
            String message = constraintViolation.getPropertyPath() + ": " + constraintViolation.getMessage();
            return ResponseEntity.badRequest().body(Utils.errorMessage(message));
        }

        // Finally, map the DTO to the entity and update the database
        try {
            upsert(target, targetDTO);
            return ResponseEntity.ok().build();
        } catch (BadRequestError e) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Utils.errorMessage(e.getMessage()));
        }
    }

    /**
     * Returns an overview of all possible target states
     */
    @GetMapping(path = "/states")
    public ResponseEntity getStates() {
        return ResponseEntity.ok().body(stateMap);
    }

    /**
     * Returns an overview of all possible schedule types
     */
    @GetMapping(path = "/schedule-types")
    public ResponseEntity getScheduleTypes() {
        Map<Integer, String> scheduleTypes = new TreeMap<>();
        scheduleTypes.put(Schedule.TYPE_ANNUALLY, "Annually");
        scheduleTypes.put(Schedule.TYPE_HALF_YEARLY, "Half-yearly");
        scheduleTypes.put(Schedule.TYPE_QUARTERLY, "Quarterly");
        scheduleTypes.put(Schedule.TYPE_BI_MONTHLY, "Bimonthly");
        scheduleTypes.put(Schedule.TYPE_MONTHLY, "Monthly");
        scheduleTypes.put(Schedule.TYPE_WEEKLY, "Weekly");
        scheduleTypes.put(Schedule.TYPE_DAILY, "Daily");
        scheduleTypes.put(Schedule.CUSTOM_SCHEDULE, "Custom");
        scheduleTypes.put(1, "Every Monday at 9:00pm"); // This is a special schedule added at config time, apparently
        return ResponseEntity.ok().body(scheduleTypes);
    }


    /**
     * The actual mapping of TargetDTO to Target and upsert of the latter
     */
    private void upsert(Target target, TargetDTO targetDTO) throws BadRequestError {

        String ownerStr = targetDTO.getGeneral().getOwner();
        User owner = userRoleDAO.getUserByName(ownerStr);
        if (owner == null) {
            throw new BadRequestError(String.format("Owner with username %s unknown", ownerStr));
        }
        if (target.isNew()) {
            target.setCreationDate(new Date());
        }
        target.setName(targetDTO.getGeneral().getName());
        target.setDescription(targetDTO.getGeneral().getDescription());
        target.setReferenceNumber(targetDTO.getGeneral().getReferenceNumber());
        target.setRunOnApproval(targetDTO.getGeneral().getRunOnApproval());
        target.setUseAQA(targetDTO.getGeneral().getAutomatedQA());
        target.setOwner(owner);
        target.setState(targetDTO.getGeneral().getState());
        target.setAutoPrune(targetDTO.getGeneral().getAutoPrune());
        target.setAutoDenoteReferenceCrawl(targetDTO.getGeneral().getReferenceCrawl());
        target.setRequestToArchivists(targetDTO.getGeneral().getRequestToArchivists());

        if (targetDTO.getSchedule() != null) {
            Set<Schedule> schedules = new HashSet<>();
            if (targetDTO.getSchedule().getHarvestOptimization() != null) {
                target.setAllowOptimize(targetDTO.getSchedule().getHarvestOptimization());
            }
            if (targetDTO.getSchedule().getHarvestNow() != null) {
                if (targetDTO.getSchedule().getHarvestNow() && targetDTO.getGeneral().getState() != Target.STATE_APPROVED) {
                    throw new BadRequestError("Cannot harvest now, since the target is not in state 'Approved'");
                }
                target.setHarvestNow(targetDTO.getSchedule().getHarvestNow());
            }
            for (ScheduleDTO s : targetDTO.getSchedule().getSchedules()) {
                Schedule schedule = businessObjectFactory.newSchedule(target);
                String cronExpression = s.getCron();
                // we support classic cron, without the prepended SECONDS field expected by Quartz
                cronExpression = "0 " + s.getCron();
                try {
                    new CronExpression(cronExpression);
                } catch (ParseException ex) {
                    throw new BadRequestError(String.format("Invalid cron expression: %s", ex.getMessage()));
                }
                schedule.setCronPattern(cronExpression);
                schedule.setStartDate(s.getStartDate());
                schedule.setEndDate(s.getEndDate());
                schedule.setScheduleType(s.getType());
                User scheduleOwner = userRoleDAO.getUserByName(s.getOwner());
                if (scheduleOwner == null) {
                    throw new BadRequestError(String.format("Owner with username %s unknown", s.getOwner()));
                }
                schedule.setOwningUser(owner);
                schedules.add(schedule);
            }
            target.getSchedules().clear();
            target.getSchedules().addAll(schedules);
        }

        if (targetDTO.getAccess() != null) {
            target.setDisplayTarget(targetDTO.getAccess().getDisplayTarget());
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
                        Site site = siteDAO.load(authorisation);
                        for (Permission p : site.getPermissions()) {
                            permissions.add(p);
                        }
                    } catch (ObjectNotFoundException e) {
                        throw new BadRequestError(String.format("Uknown authorisation: %s", authorisation));
                    }
                }
                seed.setPermissions(permissions);
                seeds.add(seed);
            }
            target.getSeeds().clear();
            target.getSeeds().addAll(seeds);
        }

        if (targetDTO.getProfile() != null) {
            long profileId = targetDTO.getProfile().getId();
            Profile profile = profileDAO.get(profileId);
            if (profile == null) {
                throw new BadRequestError(String.format("Profile with id %s does not exist", profileId));
            }
            if (!profile.isHeritrix3Profile()) {
                throw new BadRequestError("Only Heritrix v3 profiles are supported");
            }
            target.setProfile(profile);

            ProfileOverrides profileOverrides = target.getProfileOverrides();
            List<ProfileDTO.Override> overrides = targetDTO.getProfile().getOverrides();
            if (!profile.isImported() && overrides.isEmpty()) {
                throw new BadRequestError("A target with a non-imported profile requires profile overrides");
            }
            // Use reflection to fill out the elaborate yet consistently named ProfileOverrides
            for (ProfileDTO.Override override : overrides) {
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
                        if (value instanceof List) { // List setters use the interface class
                            valueClass = List.class;
                        }
                        Method setValue = ProfileOverrides.class.getMethod(methodNameSetValue, valueClass);
                        setValue.invoke(profileOverrides, value);
                    }
                    Method setEnabled = ProfileOverrides.class.getMethod(methodNameSetEnabled, boolean.class);
                    setEnabled.invoke(profileOverrides, override.getEnabled());
                    if (override.getUnit() != null) {
                        try {
                            Method setUnit = ProfileOverrides.class.getMethod(methodNameSetUnit, override.getUnit().getClass());
                            setUnit.invoke(profileOverrides, override.getUnit());
                        } catch (NoSuchMethodException e) {
                            throw new BadRequestError(String.format("Unit %s does not exist or has the wrong type", override.getUnit()));
                        }
                    }
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    throw new BadRequestError(String.format("Bad override with id %s", override.getId()));
                }
            }
        } else {
            target.setProfile(profileDAO.getDefaultProfile(owner.getAgency()));
        }

        if (targetDTO.getAnnotations() != null) {
            target.setSelectionNote(targetDTO.getAnnotations().getSelection().getNote());
            target.setSelectionType(targetDTO.getAnnotations().getSelection().getType());
            target.setEvaluationNote(targetDTO.getAnnotations().getEvaluationNote());
            target.setHarvestType(targetDTO.getAnnotations().getHarvestType());
            if (!target.isNew()) {
                target.getDeletedAnnotations().addAll( // Make sure existing annotations are removed
                        annotationDAO.loadAnnotations(WctUtils.getPrefixClassName(target.getClass()), target.getOid()));
            }
            for (TargetDTO.Annotations.Annotation a : targetDTO.getAnnotations().getAnnotations()) {
                Annotation annotation = new Annotation();
                annotation.setDate(a.getDate());
                annotation.setNote(a.getNote());
                String userName = a.getUser();
                User user = userRoleDAO.getUserByName(userName);
                if (user == null) {
                    throw new BadRequestError(String.format("User %s does not exist", userName));
                }
                annotation.setUser(user);
                annotation.setAlertable(a.getAlert());
                annotation.setObjectType(Target.class.getName());
                annotation.setObjectOid(target.getOid());
                target.addAnnotation(annotation);
            }
        }

        if (targetDTO.getDescription() != null) {
            DublinCore metadata = new DublinCore();
            metadata.setIdentifier(targetDTO.getDescription().getIdentifier());
            metadata.setDescription(targetDTO.getDescription().getDescription());
            metadata.setSubject(targetDTO.getDescription().getSubject());
            metadata.setCreator(targetDTO.getDescription().getCreator());
            metadata.setPublisher(targetDTO.getDescription().getPublisher());
            metadata.setContributor(targetDTO.getDescription().getContributor());
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

        // Persist the target
        try {
            if (targetDTO.getGroups() != null) {
                target.getParents().clear(); // Remove existing parents
                List<GroupMemberDTO> groupMemberDTOs = new ArrayList<>();
                for (TargetDTO.Group g : targetDTO.getGroups()) {
                    if (targetDAO.loadGroup(g.getId()) == null) {
                        throw(new BadRequestError(String.format("Group %d does not exist", g.getId()))) ;
                    }
                    GroupMemberDTO groupMemberDTO = new GroupMemberDTO(g.getId(), target.getOid());
                    groupMemberDTO.setSaveState(GroupMemberDTO.SAVE_STATE.NEW);
                    groupMemberDTOs.add(groupMemberDTO);
                }
                targetManager.save(target, groupMemberDTOs);
            } else {
                targetManager.save(target);
            }
        } catch (DataIntegrityViolationException e) {
            String msg = e.getMessage();
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof ConstraintViolationException) {
                if (((ConstraintViolationException) cause).getSQLException() != null) {
                    msg = "Database constraint violation, details: " + ((ConstraintViolationException) cause).getSQLException().getMessage();
                }
            }
            throw new BadRequestError(msg);
        }
    }


    /**
     * Handler used by the validation API to generate error messages
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public HashMap<String, Object> errorMessage(MethodArgumentNotValidException ex) {
        String msg = null;
        // Return the first error (typically there will only be one anyway)
        if (ex.getBindingResult().getErrorCount() > 0) {
            FieldError error = (FieldError) ex.getBindingResult().getAllErrors().get(0);
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            msg = fieldName + ": " + errorMessage;
        }
        return Utils.errorMessage(msg);
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
