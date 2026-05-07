package org.webcurator.rest.dto;

import org.hibernate.validator.constraints.Length;
import org.webcurator.domain.model.auth.User;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserDTO {

    private Long id;
    @NotBlank(message = "userName is required")
    private String userName;
    @NotBlank(message = "email is required")
    @Email(message = "invalid email address")
    private String email;
    private Boolean notificationsByEmail = false;
    private Boolean tasksByEmail = false;
    private String title;
    @NotBlank(message = "firstName is required")
    private String firstName;
    @NotBlank(message = "lastName is required")
    private String lastName;
    private Boolean active = true;
    private Boolean forcePasswordChange = false;
    private Boolean externalAuth = false;
    @Pattern(regexp = ".*[a-z].*",
            message = "password must contain at least one uppercase, one lowercase and one numeric character")
    @Pattern(regexp = ".*[A-Z].*",
            message = "password must contain at least one uppercase, one lowercase and one numeric character")
    @Pattern(regexp = ".*[0-9].*",
            message = "password must contain at least one uppercase, one lowercase and one numeric character")
    @Length(min = 6, message = "password must have at least 6 characters")
    private String password; // write-only
    private String phone;
    @Length(max = 200, message = "address must not exceed 200 characters")
    private String address;
    private List<Role> roles = new ArrayList<>();
    @NotBlank
    private String agency;
    private Date deactivateDate;
    private Boolean notifyOnGeneral = false;
    private Boolean notifyOnHarvestWarnings = false;

    public UserDTO() {}

    public UserDTO(User user) {
        this.id = user.getOid();
        this.userName = user.getUsername();
        this.email = user.getEmail();
        this.notificationsByEmail = user.isNotificationsByEmail();
        this.tasksByEmail = user.isTasksByEmail();
        this.title = user.getTitle();
        this.firstName = user.getFirstname();
        this.lastName = user.getLastname();
        this.active = user.isActive();
        this.forcePasswordChange = user.isForcePasswordChange();
        this.externalAuth = user.isExternalAuth();
        this.phone = user.getPhone();
        this.address = user.getAddress();
        for (org.webcurator.domain.model.auth.Role r : user.getRoles()) {
            Role role = new Role();
            role.setId(r.getOid());
            role.setName(r.getName());
            roles.add(role);
        }
        this.agency = user.getAgency().getName();
        this.deactivateDate = user.getDeactivateDate();
        this.notifyOnGeneral = user.isNotifyOnGeneral();
        this.notifyOnHarvestWarnings = user.isNotifyOnHarvestWarnings();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getNotificationsByEmail() {
        return notificationsByEmail;
    }

    public void setNotificationsByEmail(Boolean notificationsByEmail) {
        this.notificationsByEmail = notificationsByEmail;
    }

    public Boolean getTasksByEmail() {
        return tasksByEmail;
    }

    public void setTasksByEmail(Boolean tasksByEmail) {
        this.tasksByEmail = tasksByEmail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getForcePasswordChange() {
        return forcePasswordChange;
    }

    public void setForcePasswordChange(Boolean forcePasswordChange) {
        this.forcePasswordChange = forcePasswordChange;
    }

    public Boolean getExternalAuth() {
        return externalAuth;
    }

    public void setExternalAuth(Boolean externalAuth) {
        this.externalAuth = externalAuth;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public Date getDeactivateDate() {
        return deactivateDate;
    }

    public void setDeactivateDate(Date deactivateDate) {
        this.deactivateDate = deactivateDate;
    }

    public Boolean getNotifyOnGeneral() {
        return notifyOnGeneral;
    }

    public void setNotifyOnGeneral(Boolean notifyOnGeneral) {
        this.notifyOnGeneral = notifyOnGeneral;
    }

    public Boolean getNotifyOnHarvestWarnings() {
        return notifyOnHarvestWarnings;
    }

    public void setNotifyOnHarvestWarnings(Boolean notifyOnHarvestWarnings) {
        this.notifyOnHarvestWarnings = notifyOnHarvestWarnings;
    }


    public static class Role {
        private Long id;
        private String name;

        public Role() {}

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
