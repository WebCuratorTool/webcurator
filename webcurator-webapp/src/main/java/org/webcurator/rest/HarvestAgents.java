package org.webcurator.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.webcurator.core.coordinator.WctCoordinator;
import org.webcurator.domain.UserRoleDAO;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Handlers for the harvest-agents endpoint
 */
@RestController
@RequestMapping(path = "/api/{version}/harvest-agents")
public class HarvestAgents {


    @Autowired
    private WctCoordinator wctCoordinator;

    @GetMapping(path = "")
    public ResponseEntity get() {

        HashMap<String, HarvestAgentStatusDTO> harvestAgentStatusDTOs = wctCoordinator.getHarvestAgents();
        List<HashMap> harvestAgents = new ArrayList<>();
        for (String name : harvestAgentStatusDTOs.keySet()) {
            HarvestAgentStatusDTO harvestAgentStatusDTO = harvestAgentStatusDTOs.get(name);
            HashMap<String, Object> harvestAgent = new HashMap<>();
            harvestAgent.put("name", name);
            harvestAgent.put("maxHarvests", harvestAgentStatusDTO.getMaxHarvests());
            harvestAgent.put("currentHarvests", harvestAgentStatusDTO.getHarvesterStatusCount());
            harvestAgents.add(harvestAgent);
        }
        return ResponseEntity.ok(harvestAgents);
    }

}
