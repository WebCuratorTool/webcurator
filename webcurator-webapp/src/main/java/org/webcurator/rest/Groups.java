package org.webcurator.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.webcurator.domain.Pagination;
import org.webcurator.domain.TargetDAO;
import org.webcurator.domain.model.core.TargetGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    @Autowired
    TargetDAO targetDAO;

    @GetMapping(path = "")
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


    // TODO Maybe refactor the search stuff (this was mostly copied over from Targets.java)?

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

        Pagination pagination = targetDAO.searchGroups(pageNumber, limit, filter.groupId, filter.name,
                filter.userId, filter.agency, filter.memberOf, filter.type, filter.nonDisplayOnly);
        List<HashMap<String, Object>> groups = new ArrayList<>();
        for (TargetGroup g : (List<TargetGroup>) pagination.getList()) {
            HashMap<String, Object> group = new HashMap<>();
            group.put("id", g.getOid());
            group.put("name", g.getName());
            group.put("type", g.getType());
            group.put("agency", g.getOwningUser().getAgency().getName());
            group.put("owner", g.getOwningUser().getUsername());
            group.put("status", g.getState());
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
        private Long groupId;
        private String name;
        private String agency;
        private String userId;
        private String memberOf;
        private String type;
        private boolean nonDisplayOnly;

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
    }

    /**
     * Wraps search result data: a count of the total number of hits and a list of target summaries
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