package org.webcurator.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Handlers for the target endpoint
 */
@RestController
@RequestMapping(path = "/api/{version}/targets/")
public class Targets {

    @GetMapping
    public ResponseEntity<?> find(@PathVariable String version) {
        List<String> targets = new ArrayList<>();
        String t1 = "A target, basically";
        String t2 = "Another target, basically";
        targets.add(t1);
        targets.add(t2);
        ResponseEntity<List<String>> response = ResponseEntity.ok(targets);
        return response;
    }
}
