package org.webcurator.rest.dto;

import org.hibernate.validator.constraints.URL;
import org.webcurator.domain.model.auth.Agency;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;

/**
 * This class is used for mapping between the Agency entity and the JSON representation of an agency in the API.
 */
public class AgencyDTO {

    private long id;
    @NotBlank(message = "Name is required")
    @Max(value = 80, message = "Max length for name is 80 characters")
    private String name;
    @NotBlank(message = "Address is required")
    @Max(value = 255, message = "Max length for address is 255 characters")
    private String address;
    private String phone;
    @URL
    private String agencyUrl;
    @URL
    private String agencyLogoUrl;
    @Email
    private String email;
    private String fax;
    private Boolean showTasks;
    private String defaultDescriptionType;


    public AgencyDTO() {}

    public AgencyDTO(Agency agency) {
        id = agency.getOid();
        name = agency.getName();
        address = agency.getAddress();
        phone = agency.getPhone();
        agencyUrl = agency.getAgencyURL();
        agencyLogoUrl = agency.getAgencyLogoURL();
        email = agency.getEmail();
        fax = agency.getFax();
        showTasks = agency.getShowTasks();
        defaultDescriptionType = agency.getDefaultDescriptionType();
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAgencyUrl() {
        return agencyUrl;
    }

    public void setAgencyUrl(String agencyUrl) {
        this.agencyUrl = agencyUrl;
    }

    public String getAgencyLogoUrl() {
        return agencyLogoUrl;
    }

    public void setAgencyLogoUrl(String agencyLogoUrl) {
        this.agencyLogoUrl = agencyLogoUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public Boolean getShowTasks() {
        return showTasks;
    }

    public void setShowTasks(Boolean showTasks) {
        this.showTasks = showTasks;
    }

    public String getDefaultDescriptionType() {
        return defaultDescriptionType;
    }

    public void setDefaultDescriptionType(String defaultDescriptionType) {
        this.defaultDescriptionType = defaultDescriptionType;
    }
}
