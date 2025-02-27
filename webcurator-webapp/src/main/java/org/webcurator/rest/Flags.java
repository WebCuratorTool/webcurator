package org.webcurator.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.webcurator.domain.FlagDAO;
import org.webcurator.domain.UserRoleDAO;
import org.webcurator.domain.model.auth.User;
import org.webcurator.domain.model.core.Flag;
import org.webcurator.rest.common.BadRequestError;
import org.webcurator.rest.common.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Handlers for the flags endpoint
 */
@RestController
@RequestMapping(path = "/api/{version}/flags")
public class Flags {

    @Autowired
    FlagDAO flagDAO;

    @GetMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity get(@RequestBody(required = false) SearchParams searchParams) {
        if (searchParams == null) {
            searchParams = new SearchParams();
        }
        Filter filter = searchParams.getFilter();
        try {
            SearchResult searchResult = search(filter);
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("filter", filter);
            responseMap.put("flags", searchResult.flags);
            responseMap.put("amount", searchResult.amount);
            ResponseEntity<HashMap<String, Object>> response = ResponseEntity.ok().body(responseMap);
            return response;
        } catch (BadRequestError e) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(e.getMessage()));
        }
    }


    /**
     * Handle the actual search using the old DAO API
     */
    private SearchResult search(Filter filter) throws BadRequestError {
        List<HashMap<String, Object>> flags = new ArrayList<>();
        List<Flag> result;
        if (filter.agency == null) {
            result = flagDAO.getFlags();
        } else {
            result = flagDAO.getFlagsByAgencyName(filter.agency);
        }
        for (Flag f : result) {
            HashMap<String, Object> flag = new HashMap<>();
            flag.put("id", f.getOid());
            flag.put("name", f.getName());
            flag.put("rgb", f.getRgb());
            flag.put("agency", f.getAgency().getName());
            flags.add(flag);
        }
        return new SearchResult(flags.size(), flags);
    }


    /**
     * POJO that the framework maps the JSON query data into
     */
    private static class SearchParams {
        private Filter filter;
        SearchParams() {
            filter = new Filter();
        }
        public Filter getFilter() {
            return filter;
        }
        public void setFilter(Filter filter) {
            this.filter = filter;
        }
    }

    /**
     * Wrapper for the search filter
     */
    private static class Filter {
        private String agency;
        public String getAgency() {
            return agency;
        }
        public void setAgency(String agency) {
            this.agency = agency;
        }
    }

    /**
     * Wraps search result data: a count of the total number of hits and a list of users
     */
    private class SearchResult {
        public long amount;
        public List<HashMap<String, Object>> flags;

        SearchResult(long amount, List<HashMap<String, Object>> flags) {
            this.amount = amount;
            this.flags = flags;
        }
    }

}
