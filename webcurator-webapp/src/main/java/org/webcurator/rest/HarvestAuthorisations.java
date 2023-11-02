package org.webcurator.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.webcurator.domain.Pagination;
import org.webcurator.domain.SiteCriteria;
import org.webcurator.domain.SiteDAO;
import org.webcurator.domain.model.core.AuthorisingAgent;
import org.webcurator.domain.model.core.Permission;
import org.webcurator.domain.model.core.Site;
import org.webcurator.rest.common.BadRequestError;
import org.webcurator.rest.common.Utils;

import java.util.*;

/**
 * Handlers for the harvest-authorisations endpoint
 */
@RestController
@RequestMapping(path = "/api/{version}/harvest-authorisations")
public class HarvestAuthorisations {

    private static final int DEFAULT_PAGE_LIMIT = 10;
    private static final String DEFAULT_SORT_BY = "name,asc";

    // Response field names that are used more than once
    private static final String SORT_BY_FIELD = "sortBy";
    private static final String OFFSET_FIELD = "offset";
    private static final String LIMIT_FIELD = "limit";

    @Autowired
    SiteDAO siteDAO;

    @GetMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity get(@RequestBody(required = false) SearchParams searchParams) {
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
            responseMap.put("harvestAuthorisations", searchResult.harvestAuthorisations);
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
            }   responseMap.put("amount", searchResult.amount);
            ResponseEntity<HashMap<String, Object>> response = ResponseEntity.ok().body(responseMap);
            return response;
        } catch (BadRequestError e) {
            return ResponseEntity.badRequest().body(Utils.errorMessage(e.getMessage()));
        }
    }


    /**
     * Handle the actual search using the old DAO API
     */
    private SearchResult search(Filter filter, Integer offset, Integer limit, String sortBy) throws BadRequestError {

        // defaults
        if (limit == null) {
            limit = DEFAULT_PAGE_LIMIT;
        }
        if (offset == null) {
            offset = 0;
        }

        // magic to comply with the sort spec of the SiteDAO API
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

        SiteCriteria criteria = new SiteCriteria();
        criteria.setAgency(filter.agency);
        criteria.setSearchOid(filter.harvestAuthorisationId);
        criteria.setTitle(filter.name);
        criteria.setAgentName(filter.authorisingAgent);
        criteria.setShowDisabled(filter.showDisabled);
        criteria.setUrlPattern(filter.urlPattern);
        criteria.setOrderNo(filter.orderNo);
        criteria.setStates(filter.permissionStates);
        criteria.setSortorder(magicSortStringForDao);

        Pagination pagination = siteDAO.search(criteria, pageNumber, limit);
        List<HashMap<String, Object>> harvestAuthorisationSummaries = new ArrayList<>();
        for (Site site : (List<Site>)pagination.getList()) {
            harvestAuthorisationSummaries.add(getHarvestAuthorisationSummary(site));
        }
        return new SearchResult(pagination.getTotal(), harvestAuthorisationSummaries);
    }

    private HashMap<String, Object> getHarvestAuthorisationSummary(Site site) {
        HashMap<String, Object> summary = new HashMap<>();
        summary.put("id", site.getOid());
        summary.put("creationDate", site.getCreationDate());
        summary.put("name", site.getTitle());
        Set<Long> authorisingAgents = new HashSet<>();
        for (AuthorisingAgent authorisingAgent : site.getAuthorisingAgents()) {
            authorisingAgents.add(authorisingAgent.getOid());
        }
        summary.put("authorisingAgents", authorisingAgents);
        summary.put("orderNo", site.getLibraryOrderNo());
        Set<Integer> permissionStates = new HashSet<>();
        for (Permission permission : site.getPermissions()) {
            permissionStates.add(permission.getStatus());
        }
        summary.put("permissionStates", permissionStates);
        return summary;
    }

    /**
     * POJO that the framework maps the JSON query data into
     */
    private static class SearchParams {
        private Integer offset;
        private Integer limit;
        private String sortBy;
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
        private String agency;
        private Long harvestAuthorisationId;
        private String name;
        private String authorisingAgent;
        private boolean showDisabled;
        private String urlPattern;
        private String permissionsFileReference;
        private String orderNo;
        private Set<Integer> permissionStates;
        public String getAgency() {
            return agency;
        }
        public void setAgency(String agency) {
            this.agency = agency;
        }

        public Long getHarvestAuthorisationId() {
            return harvestAuthorisationId;
        }

        public void setHarvestAuthorisationId(Long harvestAuthorisationId) {
            this.harvestAuthorisationId = harvestAuthorisationId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAuthorisingAgent() {
            return authorisingAgent;
        }

        public void setAuthorisingAgent(String authorisingAgent) {
            this.authorisingAgent = authorisingAgent;
        }

        public boolean getShowDisabled() {
            return showDisabled;
        }

        public void setShowDisabled(boolean showDisabled) {
            this.showDisabled = showDisabled;
        }

        public String getUrlPattern() {
            return urlPattern;
        }

        public void setUrlPattern(String urlPattern) {
            this.urlPattern = urlPattern;
        }

        public String getPermissionsFileReference() {
            return permissionsFileReference;
        }

        public void setPermissionsFileReference(String permissionsFileReference) {
            this.permissionsFileReference = permissionsFileReference;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public Set<Integer> getPermissionStates() {
            return permissionStates;
        }

        public void setPermissionStates(Set<Integer> permissionStates) {
            this.permissionStates = permissionStates;
        }
    }

    /**
     * Wraps search result data: a count of the total number of hits and a list of users
     * for the current result page
     */
    private class SearchResult {
        public long amount;
        public List<HashMap<String, Object>> harvestAuthorisations;

        SearchResult(long amount, List<HashMap<String, Object>> harvestAuthorisations) {
            this.amount = amount;
            this.harvestAuthorisations = harvestAuthorisations;
        }
    }

}
