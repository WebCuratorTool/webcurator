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
import org.webcurator.rest.dto.TargetDTO;

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
    public ResponseEntity<?> post(@RequestBody TargetDTO targetDTO) {
        return null;
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
