package org.webcurator.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.webcurator.core.targets.TargetManager2;
import org.webcurator.core.util.WctUtils;
import org.webcurator.domain.AnnotationDAO;
import org.webcurator.domain.Pagination;
import org.webcurator.domain.model.core.Permission;
import org.webcurator.domain.model.core.Target;
import org.webcurator.domain.model.core.UrlPattern;
import org.webcurator.rest.common.Utils;
import org.webcurator.rest.dto.PermissionDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Handlers for the permissions endpoint
 */
@RestController
@RequestMapping(path = "/api/{version}/permissions")
public class Permissions {

    @Autowired
    AnnotationDAO annotationDAO;

    @Autowired
    TargetManager2 targetManager;

    @GetMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity get(@RequestBody(required = false) SearchParams searchParams) {
       if (searchParams == null) {
           return ResponseEntity.badRequest().body(Utils.errorMessage("Expected a filter parameter with 'targetId' field"));
       }
       Filter filter = searchParams.getFilter();
       if (filter == null || filter.targetId == null) {
          return ResponseEntity.badRequest().body(Utils.errorMessage("Expected a filter parameter with 'targetId' field"));
       }

       Target target = targetManager.load(filter.targetId, true);
       if (target == null) {
           return ResponseEntity.badRequest().body(Utils.errorMessage(String.format("Target %d does not exist",
                                                                                                filter.targetId)));
       }

       Pagination pagination;
       try {
           pagination = targetManager.findPermissionsByUrl(target, filter.url, searchParams.getPage());
       } catch (Exception e) {
           return ResponseEntity.internalServerError().body(Utils.errorMessage(e.getMessage()));
       }

       List<HashMap<String, Object>> permissions = new ArrayList<>();
       for (Permission p : (List<Permission>)pagination.getList()) {
           HashMap<String, Object> permission = new HashMap<>();
           permission.put("id", p.getOid());
           permission.put("startDate", p.getStartDate());
           permission.put("endDate", p.getEndDate());
           List<String> urls = new ArrayList<>();
           for (UrlPattern u : p.getUrls()) {
                urls.add(u.getPattern());
           }
           permission.put("urlPatterns", urls);
           permission.put("harvestAuthorisationId", p.getSite().getOid());
           permissions.add(permission);
       }

       HashMap<String, Object> responseMap = new HashMap<>();
       responseMap.put("filter", filter);
       responseMap.put("permissions", permissions);
       responseMap.put("amount", pagination.getTotal());

       return ResponseEntity.ok().body(responseMap);

    }

    @GetMapping(path = "/{id}")
    public ResponseEntity get(@PathVariable Long id) {
        Permission permission = targetManager.loadPermission(id);
        if (permission == null) {
            return ResponseEntity.notFound().build();
        }
        permission.setAnnotations(annotationDAO.loadAnnotations(WctUtils.getPrefixClassName(permission.getClass()), id));
        PermissionDTO permissionDTO = new PermissionDTO(permission);
        return ResponseEntity.ok().body(permissionDTO);
    }

    /**
     * POJO that the framework maps the JSON query data into
     */
    private static class SearchParams {
        private Filter filter;
        private Integer page;

        SearchParams() {
            filter = new Filter();
            page = 0;
        }

        public Filter getFilter() {
            return filter;
        }

        public void setFilter(Filter filter) {
            this.filter = filter;
        }

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }
    }

    /**
     * Wrapper for the search filter
     */
    private static class Filter {
        private Long targetId;
        private String url = "";

        public Long getTargetId() {
            return targetId;
        }

        public void setTargetId(Long targetId) {
            this.targetId = targetId;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

}
