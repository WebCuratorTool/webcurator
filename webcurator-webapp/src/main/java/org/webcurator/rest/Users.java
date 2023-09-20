package org.webcurator.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.webcurator.domain.UserRoleDAO;
import org.webcurator.domain.model.auth.User;
import org.webcurator.rest.common.BadRequestError;
import org.webcurator.rest.common.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Handlers for the users endpoint
 */
@RestController
@RequestMapping(path = "/api/{version}/users")
public class Users {

    @Autowired
    UserRoleDAO userRoleDAO;

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
            responseMap.put("users", searchResult.users);
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
        List<HashMap<String, Object>> users = new ArrayList<>();
        List<User> result;
        if (filter.agency == null) {
            result = userRoleDAO.getUsers();
        } else {
            result = userRoleDAO.getUsers(filter.agency);
        }
        for (User u : result) {
            HashMap<String, Object> user = new HashMap<>();
            user.put("id", u.getOid());
            user.put("name", u.getUsername());
            user.put("firstName", u.getFirstname());
            user.put("lastName", u.getLastname());
            user.put("email", u.getEmail());
            user.put("agency", u.getAgency().getName());
            users.add(user);
        }
        return new SearchResult(users.size(), users);
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
     * Wraps search result data: a count of the total number of hits and a list of target summaries
     * for the current result page
     */
    private class SearchResult {
        public long amount;
        public List<HashMap<String, Object>> users;

        SearchResult(long amount, List<HashMap<String, Object>> users) {
            this.amount = amount;
            this.users = users;
        }
    }

}
