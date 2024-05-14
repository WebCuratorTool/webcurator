package org.webcurator.rest.dto;

import org.webcurator.domain.model.core.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class is used for mapping between the Target entity and the JSON representation of a target in the API.
 */
public class TargetDTO {

    @Valid
    @NotNull(message = "General section is required")
    General general;
    @Valid
    Scheduling schedule;
    @Valid
    AccessDTO access;
    @Valid
    List<Seed> seeds = new ArrayList<>();
    @Valid
    ProfileDTO profile;
    @Valid
    Annotations annotations;
    @Valid
    DescriptionDTO description;
    @Valid
    List<Group> groups = new ArrayList<>();

    public TargetDTO() {
    }

    public TargetDTO(Target target) {
        general = new General(target);
        schedule = new Scheduling(target);
        access = new AccessDTO(target);
        for (org.webcurator.domain.model.core.Seed s : target.getSeeds()) {
            seeds.add(new Seed(s));
        }
        profile = new ProfileDTO(target);
        annotations = new Annotations(target);
        description = new DescriptionDTO(target.getDublinCoreMetaData());
        for (GroupMember m: target.getParents()) {
            groups.add(new Group(m));
        }
    }

    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
    }

    public Scheduling getSchedule() {
        return schedule;
    }

    public void setSchedule(Scheduling schedules) {
        this.schedule = schedules;
    }

    public AccessDTO getAccess() {
        return access;
    }

    public void setAccess(AccessDTO access) {
        this.access = access;
    }

    public List<Seed> getSeeds() {
        return seeds;
    }

    public void setSeeds(List<Seed> seeds) {
        this.seeds = seeds;
    }

    public ProfileDTO getProfile() {
        return profile;
    }

    public void setProfile(ProfileDTO profile) {
        this.profile = profile;
    }

    public Annotations getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Annotations annotations) {
        this.annotations = annotations;
    }

    public DescriptionDTO getDescription() {
        return description;
    }

    public void setDescription(DescriptionDTO description) {
        this.description = description;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public static class General {
        long id;
        Date creationDate;
        @NotBlank(message = "name is required")
        String name;
        String description;
        String referenceNumber;
        @NotNull(message = "runOnApproval is required")
        Boolean runOnApproval = false;
        @NotNull(message = "automatedQA is required")
        Boolean automatedQA = false;
        @NotBlank(message = "owner is required")
        String owner;
        @NotNull(message = "state is required")
        @Min(value = 1, message = "invalid state: value should be between 1 and 7")
        @Max(value = 7, message = "invalid state: value should be between 1 and 7")
        Integer state;
        @NotNull(message = "autoPrune is required")
        Boolean autoPrune = false;
        @NotNull(message = "referenceCrawl is required")
        Boolean referenceCrawl = false;
        String requestToArchivists;

        public General() {
        }

        public General(Target target) {
            id = target.getOid();
            creationDate = target.getCreationDate();
            name = target.getName();
            description = target.getDescription();
            referenceNumber = target.getReferenceNumber();
            runOnApproval = target.isRunOnApproval();
            automatedQA = target.isUseAQA();
            owner = target.getOwner().getUsername();
            state = target.getState();
            autoPrune = target.isAutoPrune();
            referenceCrawl = target.isAutoDenoteReferenceCrawl();
            requestToArchivists = target.getRequestToArchivists();
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
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

        public Boolean getRunOnApproval() {
            return runOnApproval;
        }

        public void setRunOnApproval(Boolean runOnApproval) {
            this.runOnApproval = runOnApproval;
        }

        public Boolean getAutomatedQA() {
            return automatedQA;
        }

        public void setAutomatedQA(Boolean automatedQA) {
            this.automatedQA = automatedQA;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public Integer getState() {
            return state;
        }

        public void setState(Integer state) {
            this.state = state;
        }

        public Boolean getAutoPrune() {
            return autoPrune;
        }

        public void setAutoPrune(Boolean autoPrune) {
            this.autoPrune = autoPrune;
        }

        public Boolean getReferenceCrawl() {
            return referenceCrawl;
        }

        public void setReferenceCrawl(Boolean referenceCrawl) {
            this.referenceCrawl = referenceCrawl;
        }

        public String getRequestToArchivists() {
            return requestToArchivists;
        }

        public void setRequestToArchivists(String requestToArchivists) {
            this.requestToArchivists = requestToArchivists;
        }
    }

    public static class Scheduling {

        Boolean harvestNow;
        Boolean harvestOptimization;
        @Valid
        List<ScheduleDTO> schedules = new ArrayList<>();

        public Scheduling() {
        }

        public Scheduling(Target target) {
            harvestNow = target.isHarvestNow();
            harvestOptimization = target.isAllowOptimize();
            for (org.webcurator.domain.model.core.Schedule s : target.getSchedules()) {
                ScheduleDTO schedule = new ScheduleDTO(s);
                schedules.add(schedule);
            }
        }

        public Boolean getHarvestNow() {
            return harvestNow;
        }

        public void setHarvestNow(Boolean harvestNow) {
            this.harvestNow = harvestNow;
        }

        public Boolean getHarvestOptimization() {
            return harvestOptimization;
        }

        public void setHarvestOptimization(Boolean harvestOptimization) {
            this.harvestOptimization = harvestOptimization;
        }

        public List<ScheduleDTO> getSchedules() {
            return schedules;
        }

        public void setSchedules(List<ScheduleDTO> schedules) {
            this.schedules = schedules;
        }

    }

    public static class Seed {
        long id;
        @NotBlank(message = "seed is required")
        String seed;
        @NotNull(message = "primary is required")
        Boolean primary;
        @NotEmpty(message = "authorisations may not be empty")
        List<Long> authorisations = new ArrayList<>();

        public Seed() {
        }

        public Seed(org.webcurator.domain.model.core.Seed s) {
            id = s.getOid();
            seed = s.getSeed();
            primary = s.isPrimary();
            for (Permission p : s.getPermissions()) {
                Long authorisation = p.getSite().getOid();
                if (!authorisations.contains(authorisation)) {
                    authorisations.add(p.getSite().getOid());
                }
            }
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getSeed() {
            return seed;
        }

        public void setSeed(String seed) {
            this.seed = seed;
        }

        public Boolean getPrimary() {
            return primary;
        }

        public void setPrimary(Boolean primary) {
            this.primary = primary;
        }

        public List<Long> getAuthorisations() {
            return authorisations;
        }

        public void setAuthorisations(List<Long> authorisations) {
            this.authorisations = authorisations;
        }
    }

    public static class Annotations {

        @Valid
        @NotNull(message = "selection is required")
        Selection selection;
        String evaluationNote;
        @Pattern(regexp = "Subject|Event|Theme", message = "invalid harvestType")
        String harvestType;
        @Valid
        List<Annotation> annotations = new ArrayList<>();

        public Annotations() {}

        public Annotations(Target target) {
            selection = new Selection();
            selection.setDate(target.getSelectionDate());
            selection.setNote(target.getSelectionNote());
            selection.setType(target.getSelectionType());
            evaluationNote = target.getEvaluationNote();
            harvestType = target.getHarvestType();
            for (org.webcurator.domain.model.core.Annotation a : target.getAnnotations()) {
                Annotation annotation = new Annotation();
                annotation.setDate(a.getDate());
                annotation.setNote(a.getNote());
                annotation.setUser(a.getUser().getUsername());
                annotation.setAlert(a.isAlertable());
                annotations.add(annotation);
            }
        }

        public Selection getSelection() {
            return selection;
        }

        public void setSelection(Selection selection) {
            this.selection = selection;
        }

        public String getEvaluationNote() {
            return evaluationNote;
        }

        public void setEvaluationNote(String evaluationNote) {
            this.evaluationNote = evaluationNote;
        }

        public String getHarvestType() {
            return harvestType;
        }

        public void setHarvestType(String harvestType) {
            this.harvestType = harvestType;
        }

        public List<Annotation> getAnnotations() {
            return annotations;
        }

        public void setAnnotations(List<Annotation> annotations) {
            this.annotations = annotations;
        }

        public static class Selection {
            Date date;
            @Pattern(regexp = "Producer type|Publication type|Collection|Area|Other collections",
                    message = "invalid selection type")
            String type;
            String note;

            public Selection() {}

            public Date getDate() {
                return date;
            }

            public void setDate(Date date) {
                this.date = date;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getNote() {
                return note;
            }

            public void setNote(String note) {
                this.note = note;
            }
        }

        public static class Annotation {
            Date date;
            String user;
            String note;
            Boolean alert;

            public Annotation() {}

            public Date getDate() {
                return date;
            }

            public void setDate(Date date) {
                this.date = date;
            }

            public String getUser() {
                return user;
            }

            public void setUser(String user) {
                this.user = user;
            }

            public String getNote() {
                return note;
            }

            public void setNote(String note) {
                this.note = note;
            }

            public Boolean getAlert() {
                return alert;
            }

            public void setAlert(Boolean alert) {
                this.alert = alert;
            }
        }
    }

    public static class Group {
        @NotNull(message = "id is required")
        Long id;
        String name;

        public Group() {}

        public Group(GroupMember m) {
            id = m.getParent().getOid();
            name = m.getParent().getName();
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}

