package org.webcurator.serv;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.webcurator.rest.common.FailureResponse;

import javax.servlet.http.HttpServletRequest;

@RestController
public class UnknownRestApiController {
    @RequestMapping(value = {"/api/{path:[^\\.]*}", "/api/**/{path:[^\\.]*}"})
    public ResponseEntity<?> request(HttpServletRequest req) {
        String contentUri = req.getContextPath();
        String url = req.getRequestURI().substring(contentUri.length());
        return FailureResponse.error(HttpStatus.NOT_FOUND, url + " does not exist", url);
    }
}
