package org.webcurator.rest;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.webcurator.domain.UserRoleDAO;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.domain.model.auth.Privilege;
import org.webcurator.domain.model.auth.Role;
import org.webcurator.domain.model.auth.User;
import org.webcurator.rest.auth.AuthorizationException;
import org.webcurator.rest.auth.SessionManager;
import org.webcurator.rest.common.BadRequestError;
import org.webcurator.rest.common.FailureResponse;
import org.webcurator.rest.common.Utils;
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
    SessionManager sessionManager;

    @Autowired
    UserRoleDAO userRoleDAO;

    @Autowired
    PasswordEncoder passwordEncoder;

    private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private Validator validator = factory.getValidator();


    /**
     * Search users
     */
    @GetMapping(path = "")
    public ResponseEntity get(@RequestBody(required = false) SearchParams searchParams, HttpServletRequest request) {

        // First authorize
        try {
            sessionManager.authorize(request, null, null, Privilege.MANAGE_USERS);
        } catch (AuthorizationException e) {
            return FailureResponse.error(HttpStatus.valueOf(e.getStatus()),
                    String.format("Failed to search users. Error: %s", e.getMessage()));
        }

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

    /**
     * Retrieve individual users
     */
    @GetMapping(path = "/{id}")
    public ResponseEntity get(@PathVariable long id, HttpServletRequest request) {

        // First authorize
        try {
            sessionManager.authorize(request, null, null, Privilege.MANAGE_USERS);
        } catch (AuthorizationException e) {
            return FailureResponse.error(HttpStatus.valueOf(e.getStatus()),
                    String.format("Failed to retrieve user. Error: %s", e.getMessage()));
        }

        User user = userRoleDAO.getUserByOid(id);
        if (user == null) {
            return FailureResponse.error(HttpStatus.NOT_FOUND,
                    String.format("Failed to retrieve user. Error: user with id %s does not exist", id));
        }

        return ResponseEntity.ok(new UserDTO(user));

    }


    @PostMapping(path = "")
    public ResponseEntity post(@RequestBody UserDTO userDTO, HttpServletRequest request) {

        // First authorize
        try {
            sessionManager.authorize(request, null, null, Privilege.MANAGE_USERS);
        } catch (AuthorizationException e) {
            return FailureResponse.error(HttpStatus.valueOf(e.getStatus()),
                    String.format("Failed to create user. Error: %s", e.getMessage()));
        }

        if (userRoleDAO.getUserByName(userDTO.getUserName()) != null) {
            return FailureResponse.error(HttpStatus.BAD_REQUEST,
                    String.format("Failed to create user. Error: user with username %s already exists", userDTO.getUserName()));
        }

        User user = new User();
        try {
            upsert(user, userDTO, false);
        } catch (BadRequestError e) {
            return FailureResponse.error(HttpStatus.BAD_REQUEST,
                    String.format("Failed to create user. Error: %s", e.getMessage()));
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

    @PutMapping(path = "/{id}")
    public ResponseEntity put(@PathVariable long id, @RequestBody HashMap<String, Object> userMap, HttpServletRequest request) {

        // First authorize
        try {
            sessionManager.authorize(request, null, null, Privilege.MANAGE_USERS);
        } catch (AuthorizationException e) {
            return FailureResponse.error(HttpStatus.valueOf(e.getStatus()),
                    String.format("Failed to update user. Error: %s", e.getMessage()));
        }

        User user = userRoleDAO.getUserByOid(id);
        if (user == null) {
            return FailureResponse.error(HttpStatus.NOT_FOUND,
                    String.format("Failed to update user. Error: user with id %s does not exist", id));
        }
        UserDTO userDTO = new UserDTO(user);
        try {
            Utils.mapToDTO(userMap, userDTO);
        } catch (BadRequestError e) {
            String errMsg = String.format("Failed to update user. Error: %s", e.getMessage());
            return FailureResponse.error(HttpStatus.BAD_REQUEST, errMsg);
        } catch (Exception e) {
            String errMsg = String.format("Failed to update user. Error: %s", e.getMessage());
            return FailureResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, errMsg);
        }

        try {
            upsert(user, userDTO, true);
            return ResponseEntity.ok().build();
        } catch (BadRequestError e) {
            return FailureResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Failed to update user. Error: %s",
                    e.getMessage()));
        }
    }


    @DeleteMapping(path = "/{id}")
    public ResponseEntity delete(@PathVariable long id, HttpServletRequest request) {

        // First authorize
        try {
            sessionManager.authorize(request, null, null, Privilege.MANAGE_USERS);
        } catch (AuthorizationException e) {
            return FailureResponse.error(HttpStatus.valueOf(e.getStatus()),
                    String.format("Failed to delete user. Error: %s", e.getMessage()));
        }

        User user = userRoleDAO.getUserByOid(id);
        if (user == null) {
            return FailureResponse.error(HttpStatus.NOT_FOUND,
                    String.format("Failed to delete user. Error: user with id %s does not exist", id));
        }

        user.removeAllRoles();
        try {
            userRoleDAO.delete(user);
        } catch (DataIntegrityViolationException e) {
            String msg = e.getMessage();
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException) {
                if (((ConstraintViolationException) cause).getSQLException() != null) {
                    msg = "Database constraint violation, details: " + ((ConstraintViolationException) cause).getSQLException().getMessage();
                }
            }
            return FailureResponse.error(HttpStatus.BAD_REQUEST, String.format("Failed to delete user. Error: %s", msg));
        } catch (Exception e) {
            return FailureResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Failed to delete user. Error: %s", e.getMessage()));
        }
        return ResponseEntity.ok().build();

    }


    /**
     * The actual mapping of UserDTO to User and upsert of the latter
     */
    private void upsert(User user, UserDTO userDTO, boolean isUpdate) throws BadRequestError {
        // Validate DTO
        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
        if (!violations.isEmpty()) {
            // Return the first violation we find
            ConstraintViolation<UserDTO> constraintViolation = violations.iterator().next();
            String errMsg = constraintViolation.getPropertyPath() + ": " + constraintViolation.getMessage();
            throw new BadRequestError(errMsg);
        }

        // Start mapping
        user.setUsername(userDTO.getUserName());
        user.setEmail(userDTO.getEmail());
        user.setNotificationsByEmail(userDTO.getNotificationsByEmail());
        user.setTasksByEmail(userDTO.getTasksByEmail());
        user.setTitle(userDTO.getTitle());
        user.setFirstname(userDTO.getFirstName());
        user.setLastname(userDTO.getLastName());
        user.setActive(userDTO.getActive());
        if (!(userDTO.getExternalAuth() || isUpdate)) {
            // password will have to be reset when the newly created user logs in
            user.setForcePasswordChange(true);
            if (userDTO.getPassword() == null) {
                throw new BadRequestError("Password field is required for new users");
            }
        }
        user.setExternalAuth(userDTO.getExternalAuth());
        if (userDTO.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        user.setPhone(userDTO.getPhone());
        user.setAddress(userDTO.getAddress());
        Set<Role> roles = new HashSet<>();
        for (UserDTO.Role r : userDTO.getRoles()) {
            Role role = userRoleDAO.getRoleByOid(r.getId());
            if (role == null) {
                throw new BadRequestError(String.format("Role %s does not exist", r.getName()));
            }
            roles.add(role);
        }
        user.setRoles(roles);
        Agency agency = userRoleDAO.getAgencyByName(userDTO.getAgency());
        if (agency == null) {
            throw new BadRequestError(String.format("Agency %s does not exist", userDTO.getAgency()));
        }
        user.setAgency(agency);
        user.setDeactivateDate(userDTO.getDeactivateDate());
        user.setNotifyOnGeneral(userDTO.getNotifyOnGeneral());
        user.setNotifyOnHarvestWarnings(userDTO.getNotifyOnHarvestWarnings());

        // And finally save
        try {
            userRoleDAO.saveOrUpdate(user);
        } catch (Exception e) {
            throw new BadRequestError(e.getMessage());
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
