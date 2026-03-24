package org.webcurator.rest;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.webcurator.core.harvester.agent.HarvestAgent;
import org.webcurator.core.harvester.agent.HarvestAgentClient;
import org.webcurator.core.harvester.agent.HarvestAgentFactory;
import org.webcurator.core.harvester.coordinator.HarvestAgentManager;
import org.webcurator.domain.ProfileDAO;
import org.webcurator.domain.UserRoleDAO;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.domain.model.core.Profile;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;
import org.webcurator.rest.common.BadRequestError;
import org.webcurator.rest.common.FailureResponse;
import org.webcurator.rest.dto.ProfileDTO;
import org.webcurator.rest.dto.TargetDTO;
import org.webcurator.ui.util.HarvestAgentUtil;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Handlers for the profiles endpoint
 */
@RestController
@RequestMapping(path = "/api/{version}/profiles")
public class Profiles {

    @Autowired
    ProfileDAO profileDAO;

    @Autowired
    UserRoleDAO userRoleDAO;

    @Autowired
    HarvestAgentManager harvestAgentManager;

    private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private Validator validator = factory.getValidator();

    private static final Map<Integer, String> states;

    static {
        states = new TreeMap<>();
        states.put(Profile.STATUS_INACTIVE, "Inactive");
        states.put(Profile.STATUS_ACTIVE, "Active");
        states.put(Profile.STATUS_LOCKED, "Locked");
    }


    @GetMapping(path = "/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        // FIXME implement check for VIEW_PROFILES privilege here
        Profile profile = profileDAO.get(id);
        if (profile == null) {
            return FailureResponse.error(HttpStatus.NOT_FOUND, String.format("Profile with id %s does not exist", id));
        }
        return ResponseEntity.ok(new ProfileDTO(profile));
    }

    @GetMapping(path = "")
    public ResponseEntity<?> get(@RequestBody(required = false) SearchParams searchParams) {
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
            return ResponseEntity.ok().body(responseMap);
        } catch (BadRequestError e) {
            return FailureResponse.error(HttpStatus.BAD_REQUEST, String.format("Failed to search the profiles, Error: %s", e.getMessage()));
        }
    }

    @GetMapping(path = "/states")
    public ResponseEntity<?> getStates() {
        return ResponseEntity.ok().body(states);
    }


    @PostMapping(path = "")
    public ResponseEntity post(@RequestBody ProfileDTO profileDTO, HttpServletRequest request) {

        // FIXME implement check for MANAGE_PROFILES privilege here

        // Validate submitted DTO
        Set<ConstraintViolation<ProfileDTO>> violations = validator.validate(profileDTO);
        if (!violations.isEmpty()) {
            // Return the first violation we find
            ConstraintViolation<ProfileDTO> constraintViolation = violations.iterator().next();
            String errMsg = constraintViolation.getPropertyPath() + ": " + constraintViolation.getMessage();
            return FailureResponse.error(HttpStatus.BAD_REQUEST, errMsg);
        }

        Profile profile = new Profile();
        profile.setProfile(profileDTO.getProfile());
        profile.setDefaultProfile(profileDTO.isDefault());
        profile.setDescription(profileDTO.getDescription());
        profile.setImported(profileDTO.isImported());
        profile.setName(profileDTO.getName());
        profile.setDataLimitUnit(profileDTO.getDataLimitUnit());
        profile.setMaxFileSizeUnit(profileDTO.getMaxFileSizeUnit());
        profile.setHarvesterType(profileDTO.getHarvesterType());
        Agency agency = userRoleDAO.getAgencyByName(profileDTO.getAgency());
        if (agency == null) {
            return FailureResponse.error(HttpStatus.BAD_REQUEST, String.format("Failed to save profile. Error: unkown agency %s",
                    profileDTO.getAgency()));
        }
        profile.setOwningAgency(agency);
        profile.setRequiredLevel(profileDTO.getLevel());

        // Validate the profile XML (using one of the available harvest agents)
        HarvestAgentStatusDTO harvestAgentStatusDTO = harvestAgentManager.getHarvester(profileDTO.getAgency(), profileDTO.getHarvesterType());
        if (harvestAgentStatusDTO == null) {
            return FailureResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save profile. Error: could not validate profile XML");
        }
        HarvestAgent harvestAgent = new HarvestAgentClient(harvestAgentStatusDTO.getBaseUrl(), new RestTemplateBuilder());
        if (!harvestAgent.isValidProfile(profileDTO.getProfile())) {
            return FailureResponse.error(HttpStatus.BAD_REQUEST, "Failed to save profile. Error: profile XML is not valid");
        }

        // Finally persist the new profile
        profileDAO.saveOrUpdate(profile);

        // Return "created" response with the URL representation of the new profile
        try {
            String profileUrl = request.getRequestURL().toString();
            if (!profileUrl.endsWith("/")) {
                profileUrl += "/";
            }
            profileUrl += profile.getOid();
            return ResponseEntity.created(new URI(profileUrl)).build();
        } catch (URISyntaxException e) {
            String errMsg = String.format("Malformed Profile URL. Error: %s", e.getMessage());
            return FailureResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, errMsg);
        }

    }

    /**
     * Handle the actual search using the old DAO API
     */
    private SearchResult search(Filter filter) throws BadRequestError {
        List<HashMap<String, Object>> profiles = new ArrayList<>();
        List<org.webcurator.domain.model.dto.ProfileDTO> result;
        if (filter.agency != null) {
            result = profileDAO.getAgencyNameDTOs(filter.agency, !filter.showOnlyActive, filter.type);
        } else {
            result = profileDAO.getDTOs(!filter.showOnlyActive, filter.type);
        }

        /*
         * Note: if we decide to return the full profile here, and the user does not have
         * the privilege VIEW_PROFILES or MANAGE_PROFILES, we should withhold the full
         * profile content for profiles not belonging to the user's agency (perhaps with
         * an indication that it's not being shown due to insufficient privileges)
         */
        for (org.webcurator.domain.model.dto.ProfileDTO p : result) {
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
