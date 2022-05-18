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
import org.webcurator.domain.model.core.Target;

import java.util.ArrayList;
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
            targetSummaries.add(new TargetSummary(t.getOid(), t.getSeeds().iterator().next().getSeed()));
        }
        ResponseEntity<List<TargetSummary>> response = ResponseEntity.ok(targetSummaries);
        return response;
    }


    /**
     * Wraps summary info about a target, gets directly mapped to JSON
     */
    private class TargetSummary {

        private long targetId;
        private String primarySeed;

        public TargetSummary(long targetId, String primarySeed) {
            this.targetId = targetId;
            this.primarySeed = primarySeed;
        }

        public long getTargetId() {
            return targetId;
        }

        public void setTargetId(long targetId) {
            this.targetId = targetId;
        }

        public String getPrimarySeed() {
            return primarySeed;
        }

        public void setPrimarySeed(String primarySeed) {
            this.primarySeed = primarySeed;
        }
    }
}
