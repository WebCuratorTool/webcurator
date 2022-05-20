package org.webcurator.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.webcurator.domain.Pagination;
import org.webcurator.domain.TargetDAOImpl;
import org.webcurator.domain.model.core.Seed;
import org.webcurator.domain.model.core.Target;

import java.util.*;

/**
 * Handlers for the targets endpoint
 */
@RestController
@RequestMapping(path = "/api/{version}/targets")
public class Targets {


    private static Log logger = LogFactory.getLog(Targets.class);

    @Autowired
    private TargetDAOImpl targetDAO;

    @GetMapping(path = "")
    public ResponseEntity<?> get(@RequestParam(required = false) Long targetId, @RequestParam(required = false) String name,
                                 @RequestParam(required = false) String seed, @RequestParam(required = false) String agency,
                                 @RequestParam(required = false) String userId, @RequestParam(required = false) String description,
                                 @RequestParam(required = false) String groupName, @RequestParam(required = false) boolean nonDisplayOnly,
                                 @RequestParam(required = false) Set<Integer> states, @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) Long offset, @RequestParam(required = false) Integer limit) {
        Filter filter = new Filter(targetId, name, seed, agency, userId, description, groupName, nonDisplayOnly, states);
        List<TargetSummary> targetSummaries = search(filter, offset, limit, sortBy);
        ResponseEntity<List<TargetSummary>> response = ResponseEntity.ok(targetSummaries);
        return response;
    }

    @GetMapping(path = "/{targetId}")
    public ResponseEntity<?> get(@RequestParam int targetId) {
        return null;
    }

    private List<TargetSummary> search(Filter filter, Long offset, Integer limit, String sortBy) {

        // Sort magic to comply with the TargetDao API
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
            // TODO send bad request if the sortBy param was not what we expected
            if (magicSortStringForDao == null) {}
        }

        Pagination pagination = targetDAO.search(0, 10, filter.targetId, filter.name,
                filter.states, filter.seed, filter.userId, filter.agency, filter.groupName,
                filter.nonDisplayOnly, magicSortStringForDao, filter.description);
        List<TargetSummary> targetSummaries = new ArrayList<>();
        for (Target t : (List<Target>)pagination.getList()) {
            targetSummaries.add(new TargetSummary(t));
        }
        return targetSummaries;
    }

    /**
     * Wrapper for the search filter
     */
    private class Filter {
        Long targetId;
        String name;
        String seed;
        String agency;
        String userId;
        String description; // Spec; General.Description
        String groupName; // Spec: Group.Name
        boolean nonDisplayOnly; // Spec: Access.AccessZone as a human-readable string
        Set<Integer> states; // Spec: General.State as human-readable string

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
     * Wraps summary info about a target, gets directly mapped to JSON
     */
    private class TargetSummary {

        public long targetId;
        public Date creationDate;
        public String name;
        public String agency;
        public String owner;
        // FIXME we don't want the front-end to map integers to human-readable states (as happens in the JSPs)
        public int status;
        public List<HashMap<String, String>> seeds;

        TargetSummary(Target t) {
            this.targetId = t.getOid();
            this.creationDate = t.getCreationDate();
            this.name = t.getName();
            this.agency = t.getOwner().getAgency().getName();
            this.owner = t.getOwner().getNiceName();
            this.status = t.getState();
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
