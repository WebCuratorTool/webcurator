package org.webcurator.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.webcurator.core.coordinator.WctCoordinator;
import org.webcurator.core.harvester.coordinator.HarvestAgentManager;
import org.webcurator.core.harvester.coordinator.HarvestLogManager;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.core.store.DigitalAssetStoreClient;
import org.webcurator.core.util.WctUtils;
import org.webcurator.domain.*;
import org.webcurator.domain.model.auth.User;
import org.webcurator.domain.model.core.*;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;
import org.webcurator.rest.common.BadRequestError;
import org.webcurator.rest.common.Utils;
import org.webcurator.rest.dto.TargetInstanceDTO;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.*;

/**
 * Handlers for the target-instances endpoint
 */
@RestController
@RequestMapping(path = "/api/{version}/target-instances")
public class TargetInstances {


    private static final int DEFAULT_PAGE_LIMIT = 10;
    private static final String DEFAULT_SORT_BY = "name,asc";

    // Response field names that are used more than once
    private static final String SORT_BY_FIELD = "sortBy";
    private static final String OFFSET_FIELD = "offset";
    private static final String LIMIT_FIELD = "limit";

    private static final Log logger = LogFactory.getLog(TargetInstances.class);

    private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private Validator validator = factory.getValidator();

    @Autowired
    private TargetInstanceDAO targetInstanceDAO;

    @Autowired
    private UserRoleDAO userRoleDAO;

    @Autowired
    private AnnotationDAO annotationDAO;

    @Autowired
    private HarvestAgentManager harvestAgentManager;

    @Autowired
    private WctCoordinator wctCoordinator;

    @Autowired
    private FlagDAO flagDAO;

    @Autowired
    private HarvestLogManager harvestLogManager;

    @Autowired
    private TargetInstanceManager targetInstanceManager;

    @Autowired
    private DigitalAssetStoreClient digitalAssetStoreClient;

    // The back end uses Strings, but the API should use numerical state values, so we need this look-up table
    public static Map<Integer, String> stateMap;

    public static Map<Integer, String> harvestResultStateMap;

    static {
        stateMap = new TreeMap<>();
        stateMap.put(1, TargetInstance.STATE_SCHEDULED);
        stateMap.put(2, TargetInstance.STATE_QUEUED);
        stateMap.put(3, TargetInstance.STATE_RUNNING);
        stateMap.put(4, TargetInstance.STATE_PAUSED);
        stateMap.put(5, TargetInstance.STATE_HARVESTED);
        stateMap.put(6, TargetInstance.STATE_ABORTED);
        stateMap.put(7, TargetInstance.STATE_ENDORSED);
        stateMap.put(8, TargetInstance.STATE_REJECTED);
        stateMap.put(9, TargetInstance.STATE_ARCHIVED);
        stateMap.put(10, TargetInstance.STATE_ARCHIVING);
        stateMap.put(11, TargetInstance.STATE_PATCHING);

        harvestResultStateMap = new TreeMap<>();
        harvestResultStateMap.put(HarvestResult.STATE_UNASSESSED, "Unassessed");
        harvestResultStateMap.put(HarvestResult.STATE_ENDORSED, "Endorsed");
        harvestResultStateMap.put(HarvestResult.STATE_REJECTED, "Rejected");
        harvestResultStateMap.put(HarvestResult.STATE_INDEXING, "Indexing");
        harvestResultStateMap.put(HarvestResult.STATE_ABORTED, "Aborted");
        harvestResultStateMap.put(HarvestResult.STATE_CRAWLING, "Crawling");
        harvestResultStateMap.put(HarvestResult.STATE_MODIFYING, "Modifying");
    }

    public TargetInstances() {
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
            responseMap.put("targetInstances", searchResult.targetInstanceSummaries);
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
     * GET handler for individual target instances and target instance sections
     */
    @GetMapping(path = {"/{id}", "/{id}/{section}"})
    public ResponseEntity<?> get(@PathVariable long id, @PathVariable(required = false) String section) {
        TargetInstance targetInstance = targetInstanceDAO.load(id);
        if (targetInstance == null) {
            return ResponseEntity.notFound().build();
        }
        // Annotations are managed differently from normal associated entities
        targetInstance.setAnnotations(annotationDAO.loadAnnotations(WctUtils.getPrefixClassName(targetInstance.getClass()), id));

        TargetInstanceDTO targetInstanceDTO = new TargetInstanceDTO(targetInstance);

        // Information about logs is retrieved via a separate log manager component
        List<LogFilePropertiesDTO> logsProperties;
        try {
            logsProperties = harvestLogManager.listLogFileAttributes(targetInstance);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Utils.errorMessage(
                                            String.format("Error getting log file info from store or agent, message: %s",
                                                          e.getMessage())));
        }
        List<TargetInstanceDTO.Log> logs = new ArrayList<>();
        for (LogFilePropertiesDTO l : logsProperties) {
            TargetInstanceDTO.Log log = new TargetInstanceDTO.Log();
            log.setLogFile(l.getName());
            log.setLocation(l.getPath());
            log.setSize(l.getLengthString());
            logs.add(log);
        }
        targetInstanceDTO.setLogs(logs);

        if (section == null) {
            // Return the entire target instance
            return ResponseEntity.ok().body(targetInstanceDTO);
        }
        switch (section) {
            case "annotations":
                return ResponseEntity.ok().body(targetInstanceDTO.getAnnotations());
            case "display":
                return ResponseEntity.ok().body(targetInstanceDTO.getDisplay());
            case "general":
                return ResponseEntity.ok().body(targetInstanceDTO.getGeneral());
            case "harvest-results":
                return ResponseEntity.ok().body(targetInstanceDTO.getHarvestResults());
            case "harvest-state":
                return ResponseEntity.ok().body(targetInstanceDTO.getHarvestState());
            case "logs":
                return ResponseEntity.ok().body(targetInstanceDTO.getLogs());
            case "profile":
                return ResponseEntity.ok().body(targetInstanceDTO.getProfile());
            default:
                return ResponseEntity.badRequest().body(Utils.errorMessage(String.format("No such target section: %s", section)));
        }
    }


    /**
     * Handler for patching harvest results (used by the harvest analysis and patching functionality)
     */
    @PutMapping(path = "/{id}/patch-harvest", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> patchHarvest(@PathVariable long id, @RequestBody HarvestParams harvestParams) {
        String harvestAgentName = harvestParams.getHarvestAgentName();
        Long harvestResultId = harvestParams.getHarvestResultId();
        if (harvestAgentName == null) {
           return ResponseEntity.badRequest().body(Utils.errorMessage("Parameter harvestAgentName is required"));
        }
        if (harvestResultId == null) {
            return ResponseEntity.badRequest().body(Utils.errorMessage("Parameter harvestResultId is required"));
        }
        TargetInstance targetInstance = targetInstanceDAO.load(id);
        if (targetInstance == null) {
            return ResponseEntity.notFound().build();
        }
        if (!targetInstance.getState().equals(TargetInstance.STATE_PATCHING)) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(
                    String.format("Cannot patch a harvest result unless its target instance has state %s",
                                                                                    TargetInstance.STATE_PATCHING)));
        }
        if (wctCoordinator.isQueuePaused()) {
            return ResponseEntity.badRequest().body(Utils.errorMessage("Cannot patch harvest: the queue is paused"));
        }
        HarvestAgentStatusDTO harvestAgentStatusDTO = wctCoordinator.getHarvestAgents().get(harvestAgentName);
        if (harvestAgentStatusDTO == null) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(String.format("No harvest agent named %s", harvestAgentName)));
        }
        if (!harvestAgentStatusDTO.isAcceptTasks()) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(String.format("Harvest agent %s is not accepting tasks",
                                                                                        harvestAgentName)));
        }
        HarvestResult harvestResult = targetInstanceDAO.getHarvestResult(harvestResultId);
        if (harvestResult == null) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(String.format("Could not find harvest result with id %d", harvestResultId)));
        }
        wctCoordinator.patchHarvest(targetInstance, harvestResult, harvestAgentStatusDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * Handler for starting individual target instances
     */
    @PutMapping(path = "/{id}/start", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> start(@PathVariable long id, @RequestBody HarvestParams harvestParams) {
        String harvestAgentName = harvestParams.getHarvestAgentName();
        if (harvestAgentName == null) {
           return ResponseEntity.badRequest().body(Utils.errorMessage("Parameter harvestAgentName is required"));
        }
        TargetInstance targetInstance = targetInstanceDAO.load(id);
        if (targetInstance == null) {
            return ResponseEntity.notFound().build();
        }
        if (!targetInstance.getState().equals(TargetInstance.STATE_SCHEDULED)) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(
                                                String.format("Cannot start a target instance unless it has state %s",
                                                                TargetInstance.STATE_SCHEDULED)));
        }
        if (wctCoordinator.isQueuePaused()) {
            return ResponseEntity.badRequest().body(Utils.errorMessage("Cannot start harvest: the queue is paused"));
        }
        HarvestAgentStatusDTO harvestAgentStatusDTO = wctCoordinator.getHarvestAgents().get(harvestAgentName);
        if (harvestAgentStatusDTO == null) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(String.format("No harvest agent named %s", harvestAgentName)));
        }
        if (!harvestAgentStatusDTO.isAcceptTasks()) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(String.format("Harvest agent %s is not accepting tasks",
                                                                                        harvestAgentName)));
        }
        wctCoordinator.harvest(targetInstance, harvestAgentStatusDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * Handler for pausing individual target instances
     */
    @PutMapping(path = "/{id}/pause")
    public ResponseEntity<?> pause(@PathVariable long id) {
        TargetInstance targetInstance = targetInstanceDAO.load(id);
        if (targetInstance == null) {
            return ResponseEntity.notFound().build();
        }
        if (!targetInstance.getState().equals(TargetInstance.STATE_RUNNING)) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(
                                                String.format("Cannot pause a target instance unless it has state %s",
                                                                TargetInstance.STATE_RUNNING)));
        }
        harvestAgentManager.pause(targetInstance);
        return ResponseEntity.ok().build();
    }

    /**
     * Handler for aborting individual target instances
     */
    @PutMapping(path = "/{id}/abort")
    public ResponseEntity<?> abort(@PathVariable long id) {
        TargetInstance targetInstance = targetInstanceDAO.load(id);
        if (targetInstance == null) {
            return ResponseEntity.notFound().build();
        }
        if (!(targetInstance.getState().equals(TargetInstance.STATE_RUNNING) ||
                                                    targetInstance.getState().equals(TargetInstance.STATE_PAUSED) ||
                                                    targetInstance.getState().equals(TargetInstance.STATE_STOPPING))) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(
                    String.format("Cannot abort a target instance unless it has state %s, %s or %s",
                            TargetInstance.STATE_RUNNING, TargetInstance.STATE_PAUSED, TargetInstance.STATE_STOPPING)));
        }
        harvestAgentManager.abort(targetInstance);
        return ResponseEntity.ok().build();
    }

    /**
     * Handler for stopping individual target instances
     */
    @PutMapping(path = "/{id}/stop")
    public ResponseEntity<?> stop(@PathVariable long id) {
        TargetInstance targetInstance = targetInstanceDAO.load(id);
        if (targetInstance == null) {
            return ResponseEntity.notFound().build();
        }
        if (!targetInstance.getState().equals(TargetInstance.STATE_RUNNING)) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(
                                                String.format("Cannot stop a target instance unless it has state %s",
                                                                TargetInstance.STATE_RUNNING)));
        }
        harvestAgentManager.stop(targetInstance);
        return ResponseEntity.ok().build();
    }

    /**
     * Handler for resuming individual target instances
     */
    @PutMapping(path = "/{id}/resume")
    public ResponseEntity<?> resume(@PathVariable long id) {
        TargetInstance targetInstance = targetInstanceDAO.load(id);
        if (targetInstance == null) {
            return ResponseEntity.notFound().build();
        }
        if (!targetInstance.getState().equals(TargetInstance.STATE_PAUSED)) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(
                                                String.format("Cannot resume a target instance unless it has state %s",
                                                                TargetInstance.STATE_PAUSED)));
        }
        harvestAgentManager.resume(targetInstance);
        return ResponseEntity.ok().build();
    }

    /**
     * Handler for deleting individual target instances
     */
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        TargetInstance targetInstance = targetInstanceDAO.load(id);
        if (targetInstance == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            if (targetInstance.getState().equals(TargetInstance.STATE_QUEUED) || targetInstance.getState().equals(TargetInstance.STATE_SCHEDULED)) {
                targetInstance.setTarget(null); // make sure Hibernate does not delete the target
                targetInstanceDAO.delete(targetInstance);
                annotationDAO.deleteAnnotations(annotationDAO.loadAnnotations(WctUtils.getPrefixClassName(targetInstance.getClass()), id));
            } else {
                return ResponseEntity.badRequest().body(Utils.errorMessage(String.format("Target instance could not be deleted, because it is in state %s",
                        targetInstance.getState())));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Utils.errorMessage(e.getMessage()));
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Handler for updating individual target instances
     */
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> put(@PathVariable long id, @RequestBody TargetInstanceDTO targetInstanceDTO, HttpServletRequest request) {

        TargetInstance targetInstance = targetInstanceDAO.load(id);
        if (targetInstance == null) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(String.format("Target instance with id %s does not exist", id)));
        }

        // Annotations are managed differently from normal associated entities
        targetInstance.setAnnotations(annotationDAO.loadAnnotations(WctUtils.getPrefixClassName(targetInstance.getClass()), id));

        if (targetInstanceDTO.getGeneral() != null) {
            String owner = targetInstanceDTO.getGeneral().getOwner();
            Long flagId = targetInstanceDTO.getGeneral().getFlagId();
            if (owner != null) {
                User user = userRoleDAO.getUserByName(owner);
                if (user == null) {
                    return ResponseEntity.badRequest().body(Utils.errorMessage(String.format("User %s does not exist", owner)));
                }
                targetInstance.setOwner(user);
            }
            if (flagId != null) {
                Flag flag = flagDAO.getFlagByOid(flagId);
                if (flag == null) {
                    return ResponseEntity.badRequest().body(Utils.errorMessage(String.format("Flag with id %d does not exist", flagId)));
                }
                targetInstance.setFlag(flag);
            }
        }

        if (targetInstanceDTO.getHarvestResults() != null) {
            for (TargetInstanceDTO.HarvestResult h : targetInstanceDTO.getHarvestResults()) {
                if (h.getNumber() == null) {
                    return ResponseEntity.badRequest().body(Utils.errorMessage(
                                                            "Missing required attribute harvestResult.number"));
                }
                HarvestResult harvestResult = targetInstance.getHarvestResult(h.getNumber());
                if (harvestResult == null) {
                    return ResponseEntity.badRequest().body(Utils.errorMessage(
                                         String.format("Target instance %d does not have a harvest result with number %d",
                                                        id, h.getNumber())));
                }
                if (h.getState() == HarvestResult.STATE_ENDORSED || h.getState() == HarvestResult.STATE_REJECTED) {
                    harvestResult.setState(h.getState());
                } else {
                    return ResponseEntity.badRequest().body(Utils.errorMessage(String.format("State may only be %d or %d",
                                                        HarvestResult.STATE_ENDORSED, HarvestResult.STATE_REJECTED)));
                }
            }
        }

        if (targetInstanceDTO.getAnnotations() != null) {
            targetInstance.getDeletedAnnotations().addAll( // Make sure existing annotations are removed
                        annotationDAO.loadAnnotations(WctUtils.getPrefixClassName(targetInstance.getClass()),
                                                    targetInstance.getOid()));
            for (TargetInstanceDTO.Annotation a : targetInstanceDTO.getAnnotations()) {
                Annotation annotation = new Annotation();
                annotation.setDate(a.getDate());
                annotation.setNote(a.getNote());
                String userName = a.getUser();
                User user = userRoleDAO.getUserByName(userName);
                if (user == null) {
                    return ResponseEntity.badRequest().body(Utils.errorMessage(String.format("Unknown user %s", userName)));
                }
                annotation.setUser(user);
                annotation.setAlertable(a.getAlert());
                annotation.setObjectType(TargetInstance.class.getName());
                annotation.setObjectOid(targetInstance.getOid());
                targetInstance.addAnnotation(annotation);
            }
        }

        if (targetInstanceDTO.getDisplay() != null) {
            Boolean displayTargetInstance = targetInstanceDTO.getDisplay().getDisplayTargetInstance();
            String displayChangeReason = targetInstanceDTO.getDisplay().getDisplayChangeReason();
            String displayNote = targetInstanceDTO.getDisplay().getDisplayNote();
            if (displayTargetInstance != null) {
                targetInstance.setDisplay(displayTargetInstance);
            }
            if (displayChangeReason != null) {
                targetInstance.setDisplayChangeReason(displayChangeReason);
            }
            if (displayNote != null) {
                targetInstance.setDisplayNote(displayNote);
            }
        }

        try {
            targetInstanceManager.save(targetInstance);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Utils.errorMessage(e.getMessage()));
        }
    }

    /**
     * Returns an overview of all possible target instance states
     */
    @GetMapping(path = "/states")
    public ResponseEntity getStates() {
        return ResponseEntity.ok().body(stateMap);
    }

    /**
     * Returns an overview of all possible harvest result states
     */
    @GetMapping(path = "/harvest-result-states")
    public ResponseEntity getHarvestResultStates() {
        return ResponseEntity.ok().body(harvestResultStateMap);
    }

    /**
     * Handler used by the validation API to generate error messages
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public HashMap<String, Object> errorMessage(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return Utils.errorMessage(errors);
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

        // magic to comply with the sort spec of the TargetInstanceDao API
        String magicSortStringForDao = null;
        if (sortBy != null) {
            String[] sortSpec = sortBy.split(",");
            if (sortSpec.length == 2) {
                String sortField = sortSpec[0].trim();
                if (sortField.equalsIgnoreCase("name")) {
                    magicSortStringForDao = "name";
                } else if (sortField.equalsIgnoreCase("harvestDate")) {
                    magicSortStringForDao = "date";
                } else if (sortField.equalsIgnoreCase("runTime")) {
                    magicSortStringForDao = "elapsedtime";
                } else if (sortField.equalsIgnoreCase("dataDownloaded")) {
                    magicSortStringForDao = "datadownloaded";
                } else if (sortField.equalsIgnoreCase("amountUrls")) {
                    magicSortStringForDao = "urlssucceeded";
                } else if (sortField.equalsIgnoreCase("percentageFailed")) {
                    magicSortStringForDao = "percentageurlsfailed";
                } else if (sortField.equalsIgnoreCase("amountCrawls")) {
                    magicSortStringForDao = "crawls";
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

        TargetInstanceCriteria targetInstanceCriteria = new TargetInstanceCriteria();
        targetInstanceCriteria.setTargetSearchOid(filter.targetInstanceId);
        targetInstanceCriteria.setFrom(filter.from);
        targetInstanceCriteria.setTo(filter.to);
        targetInstanceCriteria.setName(filter.name);
        targetInstanceCriteria.setAgency(filter.agency);
        targetInstanceCriteria.setOwner(filter.userId);
        targetInstanceCriteria.setNondisplayonly(filter.nonDisplayOnly);
        targetInstanceCriteria.setStates(getStateStrings(filter.states));
        targetInstanceCriteria.setRecommendationFilter(filter.qaRecommendations);
        Flag flag = null;
        if (filter.flagId != null) {
            flag = flagDAO.getFlagByOid(filter.flagId);
            if (flag == null) {
                throw new BadRequestError(String.format("Flag with id %s does not exist", filter.flagId));
            }
        }
        targetInstanceCriteria.setFlag(flag);
        targetInstanceCriteria.setTargetSearchOid(filter.targetId);
        targetInstanceCriteria.setSortorder(magicSortStringForDao);
        Pagination pagination = targetInstanceDAO.search(targetInstanceCriteria, pageNumber, limit);
        List<HashMap<String, Object>> targetInstanceSummaries = new ArrayList<>();
        for (TargetInstance t : (List<TargetInstance>) pagination.getList()) {
            targetInstanceSummaries.add(getTargetInstanceSummary(t));
        }
        return new SearchResult(pagination.getTotal(), targetInstanceSummaries);
    }

    /**
     * Create the summary target instance info used for search results
     */
    private HashMap<String, Object> getTargetInstanceSummary(TargetInstance t) {
        HashMap<String, Object> targetInstanceSummary = new HashMap<>();
        targetInstanceSummary.put("id", t.getOid());
        // TODO Implement thumbnail reference if/when the screenshot functionality gets released
        targetInstanceSummary.put("thumbnail", null);
        targetInstanceSummary.put("harvestDate", t.getSortOrderDate());
        targetInstanceSummary.put("name", t.getTarget().getName());
        targetInstanceSummary.put("agency", t.getOwner().getAgency().getName());
        targetInstanceSummary.put("owner", t.getOwner().getUsername());
        targetInstanceSummary.put("state", getStateNumber(t.getState()));
        String runTime = null;
        String dataDownloaded = null;
        Long amountUrls = null;
        Float percentageFailed = null;
        if (t.getStatus() != null) {
            runTime = t.getStatus().getElapsedTimeString();
            dataDownloaded = t.getStatus().getDataDownloadedString();
            amountUrls = t.getStatus().getUrlsSucceeded();
            percentageFailed = t.getStatus().getPercentageUrlsFailed();
        }
        targetInstanceSummary.put("runTime", runTime);
        targetInstanceSummary.put("dataDownloaded", dataDownloaded);
        targetInstanceSummary.put("amountUrls", amountUrls);
        targetInstanceSummary.put("percentageFailed", percentageFailed);
        targetInstanceSummary.put("amountCrawls", t.getTarget().getCrawls());
        targetInstanceSummary.put("qaRecommendation", t.getRecommendation());
        Long flagId = null;
        if (t.getFlag() != null) {
            flagId = t.getFlag().getOid();
        }
        targetInstanceSummary.put("flagId", flagId);
        return targetInstanceSummary;
    }

    /**
     * Convert a set of numerical states as used by the API to a set of state names as understood by the old search API
     */
    private Set<String> getStateStrings(Set<Integer> states) throws BadRequestError {
        Set<String> stateStrings = new HashSet<>();
        if (states != null) {
            for (Integer s : states) {
                if (stateMap.containsKey(s)) {
                    stateStrings.add(stateMap.get(s));
                } else {
                    throw new BadRequestError("Unknown state: " + s);
                }
            }
        }
        return stateStrings;
    }

    /**
     * Return the number corresponding to a given state String
     */
    private Integer getStateNumber(String state) {
        for (Integer s : stateMap.keySet()) {
            if (stateMap.get(s).equals(state)) {
                return s;
            }
        }
        throw new RuntimeException("Encountered unknown state " + state);
    }


    /**
     * POJO that the framework maps JSON PUT data for /start and /patch-harvest into
     */
    private static class HarvestParams {
        private String harvestAgentName;
        private Long harvestResultId;

        public String getHarvestAgentName() {
            return harvestAgentName;
        }

        public void setHarvestAgentName(String harvestAgentName) {
            this.harvestAgentName = harvestAgentName;
        }

        public Long getHarvestResultId() {
            return harvestResultId;
        }

        public void setHarvestResultId(Long harvestResultId) {
            this.harvestResultId = harvestResultId;
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
        private Long targetInstanceId;
        private Date from;
        private Date to;
        private String name;
        private String agency;
        private String userId;
        private boolean nonDisplayOnly;
        private Set<Integer> states;
        private Set<String> qaRecommendations; // FIXME the spec says these are supposed to be numbers
        private Long flagId;
        private Long targetId;

        public Long getTargetInstanceId() {
            return targetInstanceId;
        }

        public void setTargetInstanceId(Long targetInstanceId) {
            this.targetInstanceId = targetInstanceId;
        }

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

        public Set<Integer> getStates() {
            return states;
        }

        public void setStates(Set<Integer> states) {
            this.states = states;
        }

        public Date getFrom() {
            return from;
        }

        public void setFrom(Date from) {
            this.from = from;
        }

        public Date getTo() {
            return to;
        }

        public void setTo(Date to) {
            this.to = to;
        }

        public Set<String> getQaRecommendations() {
            return qaRecommendations;
        }

        public void setQaRecommendations(Set<String> qaRecommendations) {
            this.qaRecommendations = qaRecommendations;
        }

        public Long getFlagId() {
            return flagId;
        }

        public void setFlagId(Long flagId) {
            this.flagId = flagId;
        }

        public Long getTargetId() {
            return targetId;
        }

        public void setTargetId(Long targetId) {
            this.targetId = targetId;
        }
    }

    /**
     * Wraps search result data: a count of the total number of hits and a list of target instance summaries
     * for the current result page
     */
    private class SearchResult {
        public long amount;
        public List<HashMap<String, Object>> targetInstanceSummaries;

        SearchResult(long amount, List<HashMap<String, Object>> targetInstancesSummaries) {
            this.amount = amount;
            this.targetInstanceSummaries = targetInstancesSummaries;
        }
    }

}
