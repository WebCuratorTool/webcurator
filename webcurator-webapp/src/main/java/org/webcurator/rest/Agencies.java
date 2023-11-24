package org.webcurator.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.webcurator.domain.UserRoleDAO;
import org.webcurator.domain.model.auth.Agency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Handlers for the agencies endpoint
 */
@RestController
@RequestMapping(path = "/api/{version}/agencies")
public class Agencies {

    @Autowired
    UserRoleDAO userRoleDAO;

    @GetMapping(path = "")
    public ResponseEntity get() {

        List<HashMap> agencies = new ArrayList<>();
        List<Agency> agencyEntities = userRoleDAO.getAgencies();
        for (Agency a : agencyEntities) {
            HashMap<String, Object> agency = new HashMap<>();
            agency.put("id", a.getOid());
            agency.put("name", a.getName());
            agency.put("address", a.getAddress());
            agencies.add(agency);
        }
        return ResponseEntity.ok(agencies);
    }

}
