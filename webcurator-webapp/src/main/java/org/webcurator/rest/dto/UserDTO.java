package org.webcurator.rest.dto;

import org.webcurator.domain.model.auth.User;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class UserDTO {

    private Long id;
    private String userName;
    private String email;
    private boolean notificationsByEmail;
    private boolean tasksByEmail;
    private String title;
    private String firstName;
    private String lastName;
    private boolean active;
    private boolean forcePasswordChange; // FIXME Set this to true upon user creation if externalAuth is false
    private boolean externalAuth;
    private String password; // write-only
    private String phone;
    private String address;
    private Set<Role> roles = new HashSet<>();
    private String agency;
    private Date deactivateDate;
    private boolean notifyOnGeneral;
    private boolean notifyOnHarvestWarnings;

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

    public boolean isNotificationsByEmail() {
        return notificationsByEmail;
    }

    public void setNotificationsByEmail(boolean notificationsByEmail) {
        this.notificationsByEmail = notificationsByEmail;
    }

    public boolean isTasksByEmail() {
        return tasksByEmail;
    }

    public void setTasksByEmail(boolean tasksByEmail) {
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isForcePasswordChange() {
        return forcePasswordChange;
    }

    public void setForcePasswordChange(boolean forcePasswordChange) {
        this.forcePasswordChange = forcePasswordChange;
    }

    public boolean isExternalAuth() {
        return externalAuth;
    }

    public void setExternalAuth(boolean externalAuth) {
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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
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

    public boolean isNotifyOnGeneral() {
        return notifyOnGeneral;
    }

    public void setNotifyOnGeneral(boolean notifyOnGeneral) {
        this.notifyOnGeneral = notifyOnGeneral;
    }

    public boolean isNotifyOnHarvestWarnings() {
        return notifyOnHarvestWarnings;
    }

    public void setNotifyOnHarvestWarnings(boolean notifyOnHarvestWarnings) {
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
