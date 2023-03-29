package org.webcurator.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.webcurator.domain.Pagination;
import org.webcurator.domain.TargetDAO;
import org.webcurator.domain.model.auth.Privilege;
import org.webcurator.domain.model.core.Seed;
import org.webcurator.domain.model.core.Target;
import org.webcurator.rest.auth.SessionManager;

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

    private static Log logger = LogFactory.getLog(Targets.class);

    @Autowired
    SessionManager sessionManager;

    @Autowired
    private TargetDAO targetDAO;

    @GetMapping(path = "")
    public ResponseEntity<?> get(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                @RequestParam(required = false) Long targetId, @RequestParam(required = false) String name,
                                @RequestParam(required = false) String seed, @RequestParam(required = false) String agency,
                                @RequestParam(required = false) String userId, @RequestParam(required = false) String description,
                                @RequestParam(required = false) String groupName, @RequestParam(required = false) boolean nonDisplayOnly,
                                @RequestParam(required = false) Set<Integer> states, @RequestParam(required = false) String sortBy,
                                @RequestParam(required = false) Integer offset, @RequestParam(required = false) Integer limit) {

        // First the authorization stuff
        SessionManager.AuthorizationResult authorizationResult = sessionManager.authorize(authorizationHeader, Privilege.LOGIN);
        if (authorizationResult.failed) {
            return ResponseEntity.status(authorizationResult.status).body(authorizationResult.message);
        }

        // The actual filtering
        Filter filter = new Filter(targetId, name, seed, agency, userId, description, groupName, nonDisplayOnly, states);
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
            return badRequest(e.getMessage());
        }
    }

    @GetMapping(path = "/{targetId}")
    public ResponseEntity<?> get(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                 @PathVariable long targetId) {

        // First the authorization stuff
        SessionManager.AuthorizationResult authorizationResult = sessionManager.authorize(authorizationHeader, Privilege.LOGIN);
        if (authorizationResult.failed) {
            return ResponseEntity.status(authorizationResult.status).body(authorizationResult.message);
        }

        // TODO Implement retrieval of more complete set of data pertaining to a target
        Target target = targetDAO.load(targetId);
        if (target == null) {
            return ResponseEntity.notFound().build();
        } else {
            TargetFull targetFull = new TargetFull(target);
            ResponseEntity<TargetFull> response = ResponseEntity.ok().body(targetFull);
            return response;
        }
    }

    @DeleteMapping(path = "/{targetId}")
    public ResponseEntity<?> delete(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                 @PathVariable long targetId) {
        // First the authorization stuff
        SessionManager.AuthorizationResult authorizationResult = sessionManager.authorize(authorizationHeader, Privilege.DELETE_TARGET);
        if (authorizationResult.failed) {
            return ResponseEntity.status(authorizationResult.status).body(authorizationResult.message);
        }

        Target target = targetDAO.load(targetId);
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

    private SearchResult search(Filter filter, Integer offset, Integer limit, String sortBy) throws BadRequestError {

        // defaults
        if (limit == null) { limit = DEFAULT_PAGE_LIMIT; }
        if (offset == null) { offset = 0; }

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

        // The TargetDao API only supports offsets that are a multiple of limit
        if (limit < 1) {
            throw new BadRequestError("Limit must be positive");
        }
        if (offset < 0) {
            throw new BadRequestError("Offset may not be negative");
        }
        int pageNumber = offset / limit;

        Pagination pagination = targetDAO.search(pageNumber, limit, filter.targetId, filter.name,
                filter.states, filter.seed, filter.userId, filter.agency, filter.groupName,
                filter.nonDisplayOnly, magicSortStringForDao, filter.description);
        List<TargetSummary> targetSummaries = new ArrayList<>();
        for (Target t : (List<Target>)pagination.getList()) {
            targetSummaries.add(new TargetSummary(t));
        }
        return new SearchResult(pagination.getTotal(), targetSummaries);
    }


    private ResponseEntity<?> badRequest(String msg) {
        return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body(msg);
    }


    private class BadRequestError extends Exception {
        BadRequestError(String msg) {
            super(msg);
        }
    }


    /**
     * Wrapper for the search filter
     */
    private class Filter {
        public Long targetId;
        public String name;
        public String seed;
        public String agency;
        public String userId;
        public String description;
        public String groupName;
        public boolean nonDisplayOnly;
        public Set<Integer> states;

        Filter(Long targetId, String name, String seed, String agency, String userId, String description,
                String groupName, boolean nonDisplayOnly, Set<Integer> states) {
            this.targetId = targetId;
            this.name = name;
            this.seed = seed;
            this.agency = agency;
            this.userId = userId;
            this.description = description;
            this.groupName = groupName;
            this.nonDisplayOnly = nonDisplayOnly;
            this.states = states;
        }
    }

    /**
     * Wraps all info (that we want to share) about a target, gets directly mapped to JSON
     */
    private class TargetFull extends TargetSummary {
        TargetFull(Target t) {
            super(t);
        }
    }


    /**
     * Wraps search result data: a count of the total number of hits and a list of target summaries
     */
    private class SearchResult {
        public long amount;
        public List<TargetSummary> targetSummaries;
        SearchResult(long amount, List<TargetSummary> targetSummaries) {
            this.amount = amount;
            this.targetSummaries = targetSummaries;
        }
    }

    /**
     * Wraps summary info about a target, gets directly mapped to JSON
     */
    private class TargetSummary {

        public long targetId;
        public Date creationDate;
        public String name;
        public String agency;
        public String owner;
        public int state;
        public List<HashMap<String, String>> seeds;

        TargetSummary(Target t) {
            this.targetId = t.getOid();
            this.creationDate = t.getCreationDate();
            this.name = t.getName();
            this.agency = t.getOwner().getAgency().getName();
            this.owner = t.getOwner().getNiceName();
            this.state = t.getState();
            seeds = new ArrayList<>();
            for (Seed s : t.getSeeds()) {
                HashMap<String, String> seed = new HashMap<>();
                seed.put("seed", s.getSeed());
                seed.put("primary", Boolean.toString(s.isPrimary()));
                seeds.add(seed);
            }
        }
    }
}
