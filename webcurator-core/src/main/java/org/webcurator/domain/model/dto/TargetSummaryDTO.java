package org.webcurator.domain.model.dto;

import org.webcurator.domain.model.core.SeedDTO;

import java.util.Date;
import java.util.List;

public class TargetSummaryDTO {
    private Long oid;
    private String name;
    private String desc;
    private Integer state;
    private Date creationDate;
    private boolean displayTarget = true;
    private Long usrOid;
    private String userName;
    private String userFirstName;
    private String userLastName;
    private Long agcOid;
    private String agcName;
    private List<SeedDTO> seeds;

    public Long getOid() {
        return oid;
    }

    public void setOid(Long oid) {
        this.oid = oid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isDisplayTarget() {
        return displayTarget;
    }

    public void setDisplayTarget(boolean displayTarget) {
        this.displayTarget = displayTarget;
    }

    public Long getUsrOid() {
        return usrOid;
    }

    public void setUsrOid(Long usrOid) {
        this.usrOid = usrOid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public Long getAgcOid() {
        return agcOid;
    }

    public void setAgcOid(Long agcOid) {
        this.agcOid = agcOid;
    }

    public String getAgcName() {
        return agcName;
    }

    public void setAgcName(String agcName) {
        this.agcName = agcName;
    }

    public List<SeedDTO> getSeeds() {
        return seeds;
    }

    public void setSeeds(List<SeedDTO> seeds) {
        this.seeds = seeds;
    }
}
