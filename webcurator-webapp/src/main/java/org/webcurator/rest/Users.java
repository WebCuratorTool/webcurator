package org.webcurator.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.webcurator.domain.UserRoleDAO;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.domain.model.auth.Role;
import org.webcurator.domain.model.auth.User;
import org.webcurator.rest.common.BadRequestError;
import org.webcurator.rest.common.FailureResponse;
import org.webcurator.rest.common.Utils;
import org.webcurator.rest.dto.TargetDTO;
import org.webcurator.rest.dto.UserDTO;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Handlers for the users endpoint
 */
@RestController
@RequestMapping(path = "/api/{version}/users")
public class Users {

    @Autowired
    UserRoleDAO userRoleDAO;

    @Autowired
    PasswordEncoder passwordEncoder;

    private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private Validator validator = factory.getValidator();


    @GetMapping(path = "")
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
            return FailureResponse.error(HttpStatus.BAD_REQUEST, String.format("Failed to search the users. Error: %s", e.getMessage()));
        }
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity get(@PathVariable long id) {

        // FIXME Authorize

        User user = userRoleDAO.getUserByOid(id);
        if (user == null) {
            return FailureResponse.error(HttpStatus.NOT_FOUND,
                    String.format("Failed to retrieve user. Error: user with id %s does not exist", id));
        }

        return ResponseEntity.ok(new UserDTO(user));

    }


    @PostMapping(path = "")
    public ResponseEntity post(@RequestBody UserDTO userDTO, HttpServletRequest request) {

        // FIXME Authorize

        User user = new User();

        if (userRoleDAO.getUserByName(userDTO.getUserName()) != null) {
            return FailureResponse.error(HttpStatus.BAD_REQUEST,
                    String.format("Failed to create user. Error: user with username %s already exists", userDTO.getUserName()));
        }

        // Validate DTO
        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
        if (!violations.isEmpty()) {
            // Return the first violation we find
            ConstraintViolation<UserDTO> constraintViolation = violations.iterator().next();
            String errMsg = constraintViolation.getPropertyPath() + ": " + constraintViolation.getMessage();
            return FailureResponse.error(HttpStatus.BAD_REQUEST, errMsg);
        }

        user.setUsername(userDTO.getUserName());
        user.setEmail(userDTO.getEmail());
        user.setNotificationsByEmail(userDTO.isNotificationsByEmail());
        user.setTasksByEmail(userDTO.isTasksByEmail());
        user.setTitle(userDTO.getTitle());
        user.setFirstname(userDTO.getFirstName());
        user.setLastname(userDTO.getLastName());
        user.setActive(userDTO.isActive());
        if (!userDTO.isExternalAuth()) {
            // password will have to be reset when this newly created user logs in
            user.setForcePasswordChange(true);
        }
        user.setExternalAuth(userDTO.isExternalAuth());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setPhone(userDTO.getPhone());
        user.setAddress(userDTO.getAddress());
        Set<Role> roles = new HashSet<>();
        for (UserDTO.Role r : userDTO.getRoles()) {
            Role role = userRoleDAO.getRoleByOid(r.getId());
            if (role == null) {
                return FailureResponse.error(HttpStatus.BAD_REQUEST,
                        String.format("Failed to create user. Error: role %s does not exist", r.getName()));
            }
            roles.add(role);
        }
        user.setRoles(roles);
        Agency agency = userRoleDAO.getAgencyByName(userDTO.getAgency());
        if (agency == null) {
            return FailureResponse.error(HttpStatus.BAD_REQUEST,
                    String.format("Failed to create user. Error: agency %s does not exist", userDTO.getAgency()));
        }
        user.setAgency(agency);
        user.setDeactivateDate(userDTO.getDeactivateDate());
        user.setNotifyOnGeneral(userDTO.isNotifyOnGeneral());
        user.setNotifyOnHarvestWarnings(userDTO.isNotifyOnHarvestWarnings());

        try {
            userRoleDAO.saveOrUpdate(user);
        } catch (Exception e) {
            return FailureResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Failed to persist user. Error: %s",
                    e.getMessage()));
        }

        // Finally, return 201 with a URL pointing to a representation of the newly created user
        try {
            String userUrl = request.getRequestURL().toString();
            if (!userUrl.endsWith("/")) {
                userUrl += "/";
            }
            userUrl += user.getOid();
            return ResponseEntity.created(new URI(userUrl)).build();
        } catch (URISyntaxException e) {
            String errMsg = String.format("Malformed User URL. Error: %s", e.getMessage());
            return FailureResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, errMsg);
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
            List<String> roles = new ArrayList<>();
            for (Role r : u.getRoles()) {
                roles.add(r.getName());
            }
            HashMap<String, Object> user = new HashMap<>();
            user.put("id", u.getOid());
            user.put("name", u.getUsername());
            user.put("firstName", u.getFirstname());
            user.put("lastName", u.getLastname());
            user.put("email", u.getEmail());
            user.put("agency", u.getAgency().getName());
            user.put("isActive", u.isActive());
            user.put("roles", roles);
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
     * Wraps search result data: a count of the total number of hits and a list of users
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
