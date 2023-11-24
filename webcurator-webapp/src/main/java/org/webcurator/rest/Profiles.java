package org.webcurator.rest;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.webcurator.domain.ProfileDAO;
import org.webcurator.domain.model.dto.ProfileDTO;
import org.webcurator.rest.common.BadRequestError;
import org.webcurator.rest.common.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Handlers for the profiles endpoint
 */
@RestController
@RequestMapping(path = "/api/{version}/profiles")
public class Profiles {

    @Autowired
    ProfileDAO profileDAO;


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
            responseMap.put("profiles", searchResult.profiles);
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
        List<HashMap<String, Object>>profiles = new ArrayList<>();
        List<ProfileDTO> result;
        if (filter.agency != null) {
            result = profileDAO.getAgencyNameDTOs(filter.agency, !filter.showOnlyActive, filter.type);
        } else {
            result = profileDAO.getDTOs(!filter.showOnlyActive, filter.type);
        }
        for (ProfileDTO p : result) {
            HashMap<String, Object> profile = new HashMap<>();
            profile.put("id", p.getOid());
            profile.put("name", p.getName());
            profile.put("default", p.isDefaultProfile());
            profile.put("description", p.getDescription());
            profile.put("type", p.getHarvesterType());
            profile.put("state", p.getStatus());
            profile.put("agency", p.getOwningAgency().getName());
            profiles.add(profile);
        }
        return new SearchResult(profiles.size(), profiles);
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
        private boolean showOnlyActive = true;
        private String agency;
        private String type;

        public boolean isShowOnlyActive() {
            return showOnlyActive;
        }

        public void setShowOnlyActive(boolean showOnlyActive) {
            this.showOnlyActive = showOnlyActive;
        }

        public String getAgency() {
            return agency;
        }

        public void setAgency(String agency) {
            this.agency = agency;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    /**
     * Wraps search result data: a count of the total number of hits and a list of profiles
     */
    private class SearchResult {
        public long amount;
        public List<HashMap<String, Object>> profiles;

        SearchResult(long amount, List<HashMap<String, Object>> profiles) {
            this.amount = amount;
            this.profiles = profiles;
        }
    }


}
