package org.webcurator.rest;

import org.hibernate.exception.ConstraintViolationException;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.webcurator.core.targets.TargetManager2;
import org.webcurator.core.util.WctUtils;
import org.webcurator.domain.*;
import org.webcurator.domain.model.auth.User;
import org.webcurator.domain.model.core.*;
import org.webcurator.domain.model.dto.GroupMemberDTO;
import org.webcurator.rest.common.BadRequestError;
import org.webcurator.rest.common.Utils;
import org.webcurator.rest.dto.GroupDTO;
import org.webcurator.rest.dto.ProfileDTO;
import org.webcurator.rest.dto.ScheduleDTO;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.lang.reflect.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Handlers for the groups endpoint
 */
@RestController
@RequestMapping(path = "/api/{version}/groups")
public class Groups {

    private static final int DEFAULT_PAGE_LIMIT = 10;

    // Response field names that are used more than once
    private static final String OFFSET_FIELD = "offset";
    private static final String LIMIT_FIELD = "limit";

    private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private Validator validator = factory.getValidator();

    @Autowired
    TargetDAO targetDAO;

    @Autowired
    AnnotationDAO annotationDAO;

    @Autowired
    UserRoleDAO userRoleDAO;

    @Autowired
    ProfileDAO profileDAO;

    @Autowired
    BusinessObjectFactory businessObjectFactory;

    @Autowired
    TargetManager2 targetManager;

    private Map<Integer, String> stateMap = new TreeMap();

    public Groups() {
        stateMap.put(TargetGroup.STATE_PENDING, "Pending");
        stateMap.put(TargetGroup.STATE_ACTIVE, "Active");
        stateMap.put(TargetGroup.STATE_INACTIVE, "Inactive");
    }

    /**
     * Handler for search
     */
    @GetMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity get(@RequestBody(required = false) SearchParams searchParams) {
       if (searchParams == null) {
            searchParams = new SearchParams();
        }
        Filter filter = searchParams.getFilter();
        Integer offset = searchParams.getOffset();
        Integer limit = searchParams.getLimit();
        try {
            SearchResult searchResult = search(filter, offset, limit);
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("filter", filter);
            responseMap.put("groups", searchResult.groups);
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
     * Handler for retrieval of individual groups
     */
    @GetMapping(path = {"/{id}", "/{id}/{section}"})
    public ResponseEntity get(@PathVariable long id, @PathVariable(required = false) String section) {
        TargetGroup targetGroup = targetDAO.loadGroup(id);
        if (targetGroup == null) {
            return ResponseEntity.notFound().build();
        }
        // Annotations are managed differently from normal associated entities
        targetGroup.setAnnotations(annotationDAO.loadAnnotations(WctUtils.getPrefixClassName(targetGroup.getClass()), id));
        GroupDTO groupDTO = new GroupDTO(targetGroup);
        if (section == null) {
            return ResponseEntity.ok().body(groupDTO);
        }
        switch (section) {
            case "general":
                return ResponseEntity.ok().body(groupDTO.getGeneral());
            case "members":
                return ResponseEntity.ok().body(groupDTO.getMembers());
            case "memberOf":
                return ResponseEntity.ok().body(groupDTO.getMemberOf());
            case "profile":
                return ResponseEntity.ok().body(groupDTO.getProfile());
            case "schedules":
                return ResponseEntity.ok().body(groupDTO.getSchedules());
            case "annotations":
                return ResponseEntity.ok().body(groupDTO.getAnnotations());
            case "description":
                return ResponseEntity.ok().body(groupDTO.getDescription());
            case "access":
                return ResponseEntity.ok().body(groupDTO.getAccess());
            default:
                return ResponseEntity.badRequest().body(Utils.errorMessage(String.format("No such group section: %s", section)));
        }
    }


    /**
     * Handler for adding new groups
     */
    @PostMapping(path = "")
    public ResponseEntity post(@RequestBody GroupDTO groupDTO, HttpServletRequest request) {
        TargetGroup targetGroup = new TargetGroup();
        try {
            upsert(targetGroup, groupDTO);
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
            targetUrl += targetGroup.getOid();
            return ResponseEntity.created(new URI(targetUrl)).build();
        } catch (URISyntaxException e) {
            return ResponseEntity.internalServerError().body(Utils.errorMessage(e.getMessage()));
        }
    }


    /**
     * Handler for updating individual groups
     */
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> put(@PathVariable long id, @RequestBody HashMap<String, Object> groupMap, HttpServletRequest request) {
        TargetGroup targetGroup = targetDAO.loadGroup(id, true);
        if (targetGroup == null) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(String.format("Target with id %s does not exist", id)));
        }
        // Annotations are managed differently from normal associated entities
        targetGroup.setAnnotations(annotationDAO.loadAnnotations(WctUtils.getPrefixClassName(targetGroup.getClass()), id));
        // Create DTO based on the current data in the database
        GroupDTO groupDTO = new GroupDTO(targetGroup);
        // Then update the DTO with data from the supplied JSON
        try {
            Utils.mapToDTO(groupMap, groupDTO);
        } catch (BadRequestError e) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Utils.errorMessage(e.getMessage()));
        }

        // Validate updated DTO
        Set<ConstraintViolation<GroupDTO>> violations = validator.validate(groupDTO);
        if (!violations.isEmpty()) {
            // Return the first violation we find
            ConstraintViolation<GroupDTO> constraintViolation = violations.iterator().next();
            String message = constraintViolation.getPropertyPath() + ": " + constraintViolation.getMessage();
            return ResponseEntity.badRequest().body(Utils.errorMessage(message));
        }

        // Finally, map the DTO to the entity and update the database
        try {
            upsert(targetGroup, groupDTO);
            return ResponseEntity.ok().build();
        } catch (BadRequestError e) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Utils.errorMessage(e.getMessage()));
        }
    }


    private void upsert(TargetGroup group, GroupDTO groupDTO) throws BadRequestError {
        String ownerStr = groupDTO.getGeneral().getOwner();
        User owner = userRoleDAO.getUserByName(ownerStr);
        if (owner == null) {
            throw new BadRequestError(String.format("Owner with username %s unknown", ownerStr));
        }
        if (group.isNew()) {
            group.setCreationDate(new Date());
        }
        group.setName(groupDTO.getGeneral().getName());
        group.setDescription(groupDTO.getGeneral().getDescription());
        group.setReferenceNumber(groupDTO.getGeneral().getReferenceNumber());
        group.setType(groupDTO.getGeneral().getType());
        group.setOwner(owner);
        group.setOwnershipMetaData(groupDTO.getGeneral().getOwnerInfo());
        group.setFromDate(groupDTO.getGeneral().getDateFrom());
        group.setToDate(groupDTO.getGeneral().getDateTo());
        group.setSipType(groupDTO.getGeneral().getSipType());

        if (groupDTO.getProfile() != null) {
            long profileId = groupDTO.getProfile().getId();
            Profile profile = profileDAO.get(profileId);
            if (profile == null) {
                throw new BadRequestError(String.format("Profile with id %s does not exist", profileId));
            }
            if (!profile.isHeritrix3Profile()) {
                throw new BadRequestError("Only Heritrix v3 profiles are supported");
            }
            group.setProfile(profile);

            ProfileOverrides profileOverrides = group.getProfileOverrides();
            List<ProfileDTO.Override> overrides = groupDTO.getProfile().getOverrides();
            if (!profile.isImported() && overrides.isEmpty()) {
                throw new BadRequestError("A group with a non-imported profile requires profile overrides");
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
            group.setProfile(profileDAO.getDefaultProfile(owner.getAgency()));
        }


        if (!groupDTO.getSchedules().isEmpty()) {
            Set<Schedule> schedules = new HashSet<>();
            for (ScheduleDTO s : groupDTO.getSchedules()) {
                Schedule schedule = businessObjectFactory.newSchedule(group);
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
            group.getSchedules().clear();
            group.getSchedules().addAll(schedules);
        }

         if (!groupDTO.getAnnotations().isEmpty()) {
            if (!group.isNew()) {
                group.getDeletedAnnotations().addAll( // Make sure existing annotations are removed
                        annotationDAO.loadAnnotations(WctUtils.getPrefixClassName(group.getClass()), group.getOid()));
            }
            for (GroupDTO.Annotation a : groupDTO.getAnnotations()) {
                Annotation annotation = new Annotation();
                annotation.setDate(a.getDate());
                annotation.setNote(a.getNote());
                String userName = a.getUser();
                User user = userRoleDAO.getUserByName(userName);
                if (user == null) {
                    throw new BadRequestError(String.format("User %s does not exist", userName));
                }
                annotation.setUser(user);
                annotation.setAlertable(false);
                annotation.setObjectType(TargetGroup.class.getName());
                annotation.setObjectOid(group.getOid());
                group.addAnnotation(annotation);
            }
        }
         if (groupDTO.getDescription() != null) {
            DublinCore metadata = new DublinCore();
            metadata.setIdentifier(groupDTO.getDescription().getIdentifier());
            metadata.setDescription(groupDTO.getDescription().getDescription());
            metadata.setSubject(groupDTO.getDescription().getSubject());
            metadata.setCreator(groupDTO.getDescription().getCreator());
            metadata.setPublisher(groupDTO.getDescription().getPublisher());
            metadata.setContributor(groupDTO.getDescription().getContributor());
            metadata.setType(groupDTO.getDescription().getType());
            metadata.setFormat(groupDTO.getDescription().getFormat());
            metadata.setSource(groupDTO.getDescription().getSource());
            metadata.setLanguage(groupDTO.getDescription().getLanguage());
            metadata.setRelation(groupDTO.getDescription().getRelation());
            metadata.setCoverage(groupDTO.getDescription().getCoverage());
            metadata.setIssn(groupDTO.getDescription().getIssn());
            metadata.setIsbn(groupDTO.getDescription().getIsbn());
            group.setDublinCoreMetaData(metadata);
        }

        if (groupDTO.getAccess() != null) {
            group.setDisplayTarget(groupDTO.getAccess().getDisplayTarget());
            group.setAccessZone(groupDTO.getAccess().getAccessZone());
            group.setDisplayChangeReason(groupDTO.getAccess().getDisplayChangeReason());
            group.setDisplayNote(groupDTO.getAccess().getDisplayNote());
        }

        try {
            targetManager.save(group);
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
     * Handler for adding individual group members
     */
    @PostMapping(path = "/{id}/members/{memberId}")
    public ResponseEntity add(@PathVariable long id, @PathVariable long memberId) {
        TargetGroup targetGroup = targetDAO.loadGroup(id);
        if (targetGroup == null) {
            return ResponseEntity.notFound().build();
        }
        AbstractTarget abstractTarget = targetDAO.load(memberId);
        if (abstractTarget == null) {
            abstractTarget = targetDAO.loadGroup(memberId);
            if (abstractTarget == null) {
                return ResponseEntity.badRequest().body(Utils.errorMessage(String.format("No group or target with id %s exists", memberId)));
            }
        }
        for (GroupMember groupMember : targetGroup.getChildren()) {
            if (groupMember.getChild().getOid() == memberId) {
                return ResponseEntity.badRequest().body(Utils.errorMessage(String.format("Group already contains member with id %s", memberId)));
            }
        }
        GroupMemberDTO member = new GroupMemberDTO(targetGroup, abstractTarget);
        targetGroup.getNewChildren().add(member);
        try {
            targetDAO.save(targetGroup, true, null);
        } catch (Exception e) {
            ResponseEntity.internalServerError().body(Utils.errorMessage(String.format("Error while trying to persist group member: %s", e.getMessage())));
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Returns an overview of all possible group states
     */
    @GetMapping(path = "/states")
    public ResponseEntity getStates() {
        return ResponseEntity.ok().body(stateMap);
    }


    /**
     * Handle the actual search using the old Target DAO search API
     */
    private SearchResult search(Filter filter, Integer offset, Integer limit) throws BadRequestError {

        // defaults
        if (limit == null) {
            limit = DEFAULT_PAGE_LIMIT;
        }
        if (offset == null) {
            offset = 0;
        }

        if (limit < 1) {
            throw new BadRequestError("Limit must be positive");
        }
        if (offset < 0) {
            throw new BadRequestError("Offset may not be negative");
        }
        // The TargetDao API only supports offsets that are a multiple of limit
        int pageNumber = offset / limit;

        Pagination pagination = targetDAO.searchGroups(pageNumber, limit, filter.groupId, filter.name, filter.states,
                filter.userId, filter.agency, filter.memberOf, filter.type, filter.nonDisplayOnly);
        List<HashMap<String, Object>> groups = new ArrayList<>();
        for (TargetGroup g : (List<TargetGroup>) pagination.getList()) {
            HashMap<String, Object> group = new HashMap<>();
            group.put("id", g.getOid());
            group.put("name", g.getName());
            group.put("type", g.getType());
            group.put("agency", g.getOwningUser().getAgency().getName());
            group.put("owner", g.getOwningUser().getUsername());
            group.put("state", g.getState());
            groups.add(group);
        }
        return new SearchResult(pagination.getTotal(), groups);
    }

    /**
     * POJO that the framework maps the JSON query data into
     */
    private static class SearchParams {
        private Filter filter;
        private Integer offset;
        private Integer limit;

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

    }

    /**
     * Wrapper for the search filter
     */
    private static class Filter {
        private Long groupId;
        private String name;
        private String agency;
        private String userId;
        private String memberOf;
        private String type;
        private boolean nonDisplayOnly;
        private Set<Integer> states;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public boolean isNonDisplayOnly() {
            return nonDisplayOnly;
        }

        public void setNonDisplayOnly(boolean nonDisplayOnly) {
            this.nonDisplayOnly = nonDisplayOnly;
        }

        public Long getGroupId() {
            return groupId;
        }

        public void setGroupId(Long groupId) {
            this.groupId = groupId;
        }

        public String getMemberOf() {
            return memberOf;
        }

        public void setMemberOf(String memberOf) {
            this.memberOf = memberOf;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Set<Integer> getStates() {
            return states;
        }

        public void setStates(Set<Integer> states) {
            this.states = states;
        }
    }

    /**
     * Wraps search result data: a count of the total number of hits and a list of groups
     * for the current result page
     */
    private class SearchResult {
        public long amount;
        public List<HashMap<String, Object>> groups;

        SearchResult(long amount, List<HashMap<String, Object>> groups) {
            this.amount = amount;
            this.groups = groups;
        }
    }

}
