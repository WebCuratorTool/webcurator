package org.webcurator.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.webcurator.domain.model.core.AbstractTarget;
import org.webcurator.domain.model.core.GroupMember;
import org.webcurator.domain.model.core.Schedule;
import org.webcurator.domain.model.core.TargetGroup;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GroupDTO {

    @Valid
    @NotNull(message = "General section is required")
    General general;
    @Valid
    List<Member> members = new ArrayList<>();
    @Valid
    List<Member> memberOf = new ArrayList<>();
    @Valid
    ProfileDTO profile;
    @Valid
    List<ScheduleDTO> schedules = new ArrayList<>();
    @Valid
    List<Annotation> annotations = new ArrayList<>();
    @Valid
    DescriptionDTO description;
    @Valid
    AccessDTO access;

    public GroupDTO() {}

    public GroupDTO(TargetGroup targetGroup) {
        general = new General(targetGroup);
        for (GroupMember g : targetGroup.getChildren()) {
            if (g.getChild() != null) {
                String type = g.getChild().getObjectType() == AbstractTarget.TYPE_GROUP ? "group" : "target";
                Member member = new Member(g.getChild().getOid(), g.getChild().getName(), type);
                members.add(member);
            }
        }
        for (GroupMember g : targetGroup.getParents()) {
            if (g.getParent() != null) {
                Member member = new Member(g.getParent().getOid(), g.getParent().getName(), "group");
                memberOf.add(member);
            }
        }
        profile = new ProfileDTO(targetGroup);
        for (Schedule s : targetGroup.getSchedules()) {
            ScheduleDTO scheduleDTO = new ScheduleDTO(s);
            schedules.add(scheduleDTO);
        }
        for (org.webcurator.domain.model.core.Annotation a : targetGroup.getAnnotations()) {
            Annotation annotation = new Annotation(a.getDate(), a.getNote(), a.getUser().getUsername());
            annotations.add(annotation);
        }
        description = new DescriptionDTO(targetGroup.getDublinCoreMetaData());
        access = new AccessDTO(targetGroup);
    }

    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    public List<Member> getMemberOf() {
        return memberOf;
    }

    public void setMemberOf(List<Member> memberOf) {
        this.memberOf = memberOf;
    }

    public ProfileDTO getProfile() {
        return profile;
    }

    public void setProfile(ProfileDTO profile) {
        this.profile = profile;
    }

    public List<ScheduleDTO> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<ScheduleDTO> schedules) {
        this.schedules = schedules;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public DescriptionDTO getDescription() {
        return description;
    }

    public void setDescription(DescriptionDTO description) {
        this.description = description;
    }

    public AccessDTO getAccess() {
        return access;
    }

    public void setAccess(AccessDTO access) {
        this.access = access;
    }

    public static class General {

        long id;
        String name;
        String description;
        String referenceNumber;
        String type;
        String owner;
        String ownerInfo;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Date dateFrom;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Date dateTo;
        int sipType;

        public General() {}

        public General(TargetGroup targetGroup) {
            id = targetGroup.getOid();
            name = targetGroup.getName();
            description = targetGroup.getDescription();
            type = targetGroup.getType();
            owner = targetGroup.getOwner().getUsername();
            ownerInfo = targetGroup.getOwnershipMetaData();
            dateFrom = targetGroup.getFromDate();
            dateTo = targetGroup.getToDate();
            sipType = targetGroup.getSipType();
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getReferenceNumber() {
            return referenceNumber;
        }

        public void setReferenceNumber(String referenceNumber) {
            this.referenceNumber = referenceNumber;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public String getOwnerInfo() {
            return ownerInfo;
        }

        public void setOwnerInfo(String ownerInfo) {
            this.ownerInfo = ownerInfo;
        }

        public Date getDateFrom() {
            return dateFrom;
        }

        public void setDateFrom(Date dateFrom) {
            this.dateFrom = dateFrom;
        }

        public Date getDateTo() {
            return dateTo;
        }

        public void setDateTo(Date dateTo) {
            this.dateTo = dateTo;
        }

        public int getSipType() {
            return sipType;
        }

        public void setSipType(int sipType) {
            this.sipType = sipType;
        }
    }

    public static class Member {
        long id;
        String type;
        String name;

        public Member() {}

        public Member(long id, String name, String type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Annotation {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Date date;
        String note;
        String user;

        public Annotation() {}

        public Annotation(Date date, String note, String user) {
            this.date = date;
            this.note = note;
            this.user = user;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }
    }
}
