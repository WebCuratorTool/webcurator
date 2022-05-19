package org.webcurator.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.webcurator.domain.Pagination;
import org.webcurator.domain.TargetDAOImpl;
import org.webcurator.domain.model.core.Seed;
import org.webcurator.domain.model.core.Target;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Handlers for the targets endpoint
 */
@RestController
@RequestMapping(path = "/api/{version}/targets/")
public class Targets {


    private static Log logger = LogFactory.getLog(Targets.class);

    @Autowired
    private TargetDAOImpl targetDAO;

    @GetMapping
    public ResponseEntity<?> find(@PathVariable String version) {
        Pagination pagination = targetDAO.search(0, 10, null, null,
                                                null, null, null, null, null,
                                                false, null, null);
        List<TargetSummary> targetSummaries = new ArrayList<>();
        for (Target t : (List<Target>)pagination.getList()) {
            targetSummaries.add(new TargetSummary(t));
        }
        ResponseEntity<List<TargetSummary>> response = ResponseEntity.ok(targetSummaries);
        return response;
    }


    /**
     * Wraps summary info about a target, gets directly mapped to JSON
     */
    private class TargetSummary {

        private long targetId;
        private Date creationDate;
        private String name;
        private String agency;
        private String owner;
        // FIXME we don't want the front-end to map integers to human-readable states (as happens in the JSPs)
        private int status;
        private List<HashMap<String, String>> seeds;

        public TargetSummary(Target t) {
            this.targetId = t.getOid();
            this.creationDate = t.getCreationDate();
            this.name = t.getName();
            this.agency = t.getOwner().getAgency().getName();
            this.owner = t.getOwner().getNiceName();
            this.status = t.getState();
            seeds = new ArrayList<>();
            for (Seed s : t.getSeeds()) {
                HashMap<String, String> seed = new HashMap<>();
                seed.put("seed", s.getSeed());
                seed.put("primary", Boolean.toString(s.isPrimary()));
                seeds.add(seed);
            }
        }


        public long getTargetId() {
            return targetId;
        }

        public void setTargetId(long targetId) {
            this.targetId = targetId;
        }

        public Date getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(Date creationDate) {
            this.creationDate = creationDate;
        }

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

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public List<HashMap<String, String>> getSeeds() {
            return seeds;
        }

        public void setSeeds(List<HashMap<String, String>> seeds) {
            this.seeds = seeds;
        }
    }
}
