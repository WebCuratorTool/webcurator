package org.webcurator.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.webcurator.domain.FlagDAOImpl;
import org.webcurator.domain.Pagination;
import org.webcurator.domain.TargetInstanceCriteria;
import org.webcurator.domain.TargetInstanceDAOImpl;
import org.webcurator.domain.model.core.*;


import java.util.*;

@RestController
public class RestTargetInstanceQueue {
    private static final Log logger = LogFactory.getLog(RestTargetInstanceQueue.class);

    @Autowired
    private TargetInstanceDAOImpl targetInstanceDAO;

    @Autowired
    private FlagDAOImpl flagDAO;

    @RequestMapping(path = "/api/{version}/queue", method = {RequestMethod.GET, RequestMethod.POST})
    public List<TargetInstanceDTO> post(@RequestBody Filter filter) throws BadRequestError {
        Pagination pagination = search(filter);
        List<TargetInstanceDTO> ret = new ArrayList<>();
        for (Object obj : pagination.getList()) {
            TargetInstanceDTO ti = new TargetInstanceDTO((TargetInstance) obj);
//            long oid = ti.getOid();
            ret.add(ti);
        }
        return ret;
    }


//    @GetMapping(path = "/{targetId}")
//    public ResponseEntity<?> get(@RequestParam int targetId) {
//        return null;
//    }


    private Pagination search(Filter filter) throws BadRequestError {
        // defaults
        int limit = 10;
        if (filter.limit != null) {
            limit = filter.limit;
        }

        // defaults
        int offset = 0;
        if (filter.offset != null) {
            offset = filter.offset;
        }

        // magic to comply with the sort spec of the TargetDao API
        String magicSortStringForDao = null;
        String sortBy = filter.sortBy;
        if (sortBy != null) {
            String[] sortSpec = sortBy.split(",");
            if (sortSpec.length == 2) {
                if (sortSpec[0].trim().equalsIgnoreCase("targetName")) {
                    magicSortStringForDao = "targetName";
                } else if (sortSpec[0].trim().equalsIgnoreCase("creationDate")) {
                    magicSortStringForDao = "date";
                }
                if (magicSortStringForDao != null && (sortSpec[1].equalsIgnoreCase("asc") || sortSpec[1].equalsIgnoreCase("desc"))) {
                    magicSortStringForDao += sortSpec[1];
                }
            }
            if (magicSortStringForDao == null) {
                throw new BadRequestError("Unsupported or malformed sort spec: " + sortBy);
            }
        }

        // The TargetDao API only supports offsets that are a multiple of limit
        if (limit < 1) {
            throw new BadRequestError("Limit must be positive");
        }
        if (offset < 0) {
            throw new BadRequestError("Offset may not be negative");
        }
        int pageNumber = offset / limit;

        TargetInstanceCriteria tiCriteria = new TargetInstanceCriteria();
        if (filter.flagId <= 0) {
            tiCriteria.setFlagged(false);
        } else {
            tiCriteria.setFlagged(true);
            Flag flag = flagDAO.getFlagByOid(filter.flagId);
            tiCriteria.setFlag(flag);
        }

        tiCriteria.setName(filter.targetName);
        tiCriteria.setTargetSearchOid(filter.targetInstanceId);
        tiCriteria.setSortorder(filter.sortBy);

        return targetInstanceDAO.search(tiCriteria, offset, limit);
    }


    private ResponseEntity<?> badRequest(String msg) {
        return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body(msg);
    }


    private static class BadRequestError extends Exception {
        BadRequestError(String msg) {
            super(msg);
        }
    }


    /**
     * Wrapper for the search filter
     */
    private static class Filter {
        public Long targetInstanceId;
        public String targetName;
        public Long flagId;

        public Integer offset;
        public Integer limit;
        public String sortBy;


        public Long getTargetInstanceId() {
            return targetInstanceId;
        }

        public void setTargetInstanceId(Long targetInstanceId) {
            this.targetInstanceId = targetInstanceId;
        }

        public String getTargetName() {
            return targetName;
        }

        public void setTargetName(String targetName) {
            this.targetName = targetName;
        }

        public Long getFlagId() {
            return flagId;
        }

        public void setFlagId(Long flagId) {
            this.flagId = flagId;
        }

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public String getSortBy() {
            return sortBy;
        }

        public void setSortBy(String sortBy) {
            this.sortBy = sortBy;
        }
    }

    private static class FlagDTO {

    }

    private static class TargetInstanceDTO {
        public TargetInstanceDTO(TargetInstance ti) {
            this.oid = ti.getOid();

            this.flagged = ti.getFlagged();
            this.flag = ti.getFlag();

            if (ti.getTarget() != null) {
                this.targetOid = ti.getTarget().getOid();
                this.targetName = ti.getTarget().getName();
            }

            this.sortOrderDate = ti.getSortOrderDate();
            this.state = ti.getState();
            if (ti.getOwner() != null) {
                this.ownerOid = ti.getOwner().getOid();
                this.ownerNiceName = ti.getOwner().getNiceName();
            }

            if (ti.getStatus() != null) {
                this.statusElapsedTime = ti.getStatus().getElapsedTime();
                this.statusDataDownloadedString = ti.getStatus().getDataDownloadedString();
            }
        }

        private Long oid;

        private boolean flagged;
        private Flag flag;

        private Long targetOid;
        private String targetName;

        private Date sortOrderDate;
        private String state;

        private Long ownerOid;
        private String ownerNiceName;

        private Long statusElapsedTime;
        private String statusDataDownloadedString;

        public Long getOid() {
            return oid;
        }

        public void setOid(Long oid) {
            this.oid = oid;
        }

        public boolean isFlagged() {
            return flagged;
        }

        public void setFlagged(boolean flagged) {
            this.flagged = flagged;
        }

        public Flag getFlag() {
            return flag;
        }

        public void setFlag(Flag flag) {
            this.flag = flag;
        }

        public Long getTargetOid() {
            return targetOid;
        }

        public void setTargetOid(Long targetOid) {
            this.targetOid = targetOid;
        }

        public String getTargetName() {
            return targetName;
        }

        public void setTargetName(String targetName) {
            this.targetName = targetName;
        }

        public Date getSortOrderDate() {
            return sortOrderDate;
        }

        public void setSortOrderDate(Date sortOrderDate) {
            this.sortOrderDate = sortOrderDate;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public Long getOwnerOid() {
            return ownerOid;
        }

        public void setOwnerOid(Long ownerOid) {
            this.ownerOid = ownerOid;
        }

        public String getOwnerNiceName() {
            return ownerNiceName;
        }

        public void setOwnerNiceName(String ownerNiceName) {
            this.ownerNiceName = ownerNiceName;
        }

        public Long getStatusElapsedTime() {
            return statusElapsedTime;
        }

        public void setStatusElapsedTime(Long statusElapsedTime) {
            this.statusElapsedTime = statusElapsedTime;
        }

        public String getStatusDataDownloadedString() {
            return statusDataDownloadedString;
        }

        public void setStatusDataDownloadedString(String statusDataDownloadedString) {
            this.statusDataDownloadedString = statusDataDownloadedString;
        }
    }
}
