package org.webcurator.domain.model.core;


import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "ABSTRACT_TARGET_SUMMARY_VIEW")
//@NamedQueries({
//        @NamedQuery(name = "org.webcurator.domain.model.core.AbstractTargetSummaryView.getTargetSummaries", query = "select AT_OID, AT_NAME, AT_DESC, AT_STATE, AT_CREATION_DATE, AT_DISPLAY_TARGET, USR_OID, USR_USERNAME, USR_FIRSTNAME, USR_LASTNAME, AGC_OID, AGC_NAME, SEED_IDS, SEED_NAMES, SEED_PRIMARIES from ABSTRACT_TARGET_SUMMARY_VIEW"),
//})
public class AbstractTargetSummaryView {
//    public static final String QRY_GET_TARGET_SUMMARIES = "org.webcurator.domain.model.core.AbstractTargetSummaryView.getTargetSummaries";
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

    @Column(name = "SEED_IDS")
    private String seedIDs;

    @Column(name = "SEED_NAMES")
    private String seedNames;

    @Column(name = "SEED_PRIMARIES")
    private String seedPrimaries;

    @Column(name = "GROUP_NAMES")
    private String groupNames;

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

    public String getSeedIDs() {
        return seedIDs;
    }

    public void setSeedIDs(String seedIDs) {
        this.seedIDs = seedIDs;
    }

    public String getSeedNames() {
        return seedNames;
    }

    public void setSeedNames(String seedNames) {
        this.seedNames = seedNames;
    }

    public String getSeedPrimaries() {
        return seedPrimaries;
    }

    public void setSeedPrimaries(String seedPrimaries) {
        this.seedPrimaries = seedPrimaries;
    }

    public String getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(String groupNames) {
        this.groupNames = groupNames;
    }
}