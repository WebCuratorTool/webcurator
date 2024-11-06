package org.webcurator.domain.model.view;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ABSTRACT_TARGET_SUMMARY_VIEW")
public class ViewEntityTargetSummary {
    @Id
    @Column(name = "AT_OID")
    private Long oid;

    @Column(name = "AT_NAME")
    private String name;

    @Column(name = "AT_DESC")
    private String desc;

    @Column(name = "AT_STATE")
    private String state;

    @Column(name = "AT_CREATION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @Column(name = "AT_DISPLAY_TARGET")
    private boolean displayTarget = true;

    @Column(name = "USR_OID")
    private Long usrOid;

    @Column(name = "USR_USERNAME")
    private String userName;

    @Column(name = "USR_FIRSTNAME")
    private String userFirstName;

    @Column(name = "USR_LASTNAME")
    private String userLastName;

    @Column(name = "AGC_OID")
    private Long agcOid;

    @Column(name = "AGC_NAME")
    private String agcName;

    @Column(name = "GROUP_NAMES")
    private String groupNames;

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true) // default fetch type is LAZY
    @JoinColumn(name = "S_TARGET_ID")
    private Set<ViewEntitySeed> seeds = new HashSet<>();

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

    public String getState() {
        return state;
    }

    public void setState(String state) {
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

    public String getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(String groupNames) {
        this.groupNames = groupNames;
    }

    public Set<ViewEntitySeed> getSeeds() {
        return seeds;
    }

    public void setSeeds(Set<ViewEntitySeed> seeds) {
        this.seeds = seeds;
    }
}
