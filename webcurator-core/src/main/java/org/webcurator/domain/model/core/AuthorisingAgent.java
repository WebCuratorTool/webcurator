/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.webcurator.domain.model.core;

import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import javax.persistence.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents an Agent responsible for granting permission to harvest a site and
 * provide permission for access.
 * <p>
 * An authorising agent may directly own or manage the website being harvested,
 * or may be something like Government legislation that is offerring blanket
 * permission to a Library to harvest content within a particular domain
 * space.
 */
// lazy="false"
@Entity
@Table(name = "AUTHORISING_AGENT")
public class AuthorisingAgent extends AbstractIdentityObject implements Annotatable {
    /**
     * The database oid
     */
    @Id
    @NotNull
    @Column(name = "AA_OID")
    // Note: From the Hibernate 4.2 documentation:
    // The Hibernate team has always felt such a construct as fundamentally wrong.
    // Try hard to fix your data model before using this feature.
    @TableGenerator(name = "SharedTableIdGenerator",
            table = "ID_GENERATOR",
            pkColumnName = "IG_TYPE",
            valueColumnName = "IG_VALUE",
            pkColumnValue = "General",
            allocationSize = 1) // 50 is the default
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SharedTableIdGenerator")
    private Long oid;
    /**
     * The name of the agent.
     */
    @Size(max=255)
    @Column(name = "AA_NAME")
    private String name;
    /**
     * A description of the agent.
     */
    @Size(max=2048)
    @Column(name = "AA_DESC")
    private String description;
    /**
     * The name of the contact within the agency.
     */
    @Size(max=255)
    @Column(name = "AA_CONTACT")
    private String contact;
    /**
     * The phone number for the contact.
     */
    @Size(max=32)
    @Column(name = "AA_PHONE_NUMBER")
    private String phoneNumber;
    /**
     * The e-mail address for the contact.
     */
    @Size(max=255)
    @Column(name = "AA_EMAIL")
    private String email;
    /**
     * The mailing address for the contact.
     */
    @Size(max=2048)
    @Column(name = "AA_ADRESS")
    private String address;


    /**
     * The list of annotations.
     */
    @Transient
    private List<Annotation> annotations = new LinkedList<Annotation>();
    /**
     * The list of deleted annotations.
     */
    @Transient
    private List<Annotation> deletedAnnotations = new LinkedList<Annotation>();
    /**
     * True if the annotations have been loaded
     */
    @Transient
    private boolean annotationsSet = false;
    /**
     * Flag to state if the annotations have been sorted
     */
    @Transient
    private boolean annotationsSorted = false;

    /**
     * No-arg constructor for Hibernate
     */
    protected AuthorisingAgent() {
    }

    /**
     * Get the OID of the Authorising Agent.
     *
     * @return Returns the oid.
     */
    public Long getOid() {
        return oid;
    }

    /**
     * Sets the OID of the object.
     *
     * @param oid The oid to set.
     */
    public void setOid(Long oid) {
        this.oid = oid;
    }

    /**
     * Gets the name of the Authorising Agent.
     *
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the authorising agent.
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        if (name != null) {
            this.name = name.trim();
        } else {
            this.name = null;
        }
    }


    /**
     * Returns the mailing address.
     *
     * @return Returns the address.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the mailing address.
     *
     * @param address The address to set.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Returns the contact name.
     *
     * @return Returns the contact.
     */
    public String getContact() {
        return contact;
    }

    /**
     * Set the name of the contact.
     *
     * @param contact The contact to set.
     */
    public void setContact(String contact) {
        this.contact = contact;
    }

    /**
     * Gets the e-mail address for the contact.
     *
     * @return Returns the email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the e-mail address for the contact.
     *
     * @param email The email to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the phone number for the contact.
     *
     * @return Returns the phoneNumber.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the phone number for the contact.
     *
     * @param phoneNumber The phoneNumber to set.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Gets the description of the authorising agency. Possibly used to
     * indicate information such as "this is government legislation".
     *
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the authorising agent.
     *
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * Two AuhorisingAgent objects are equivalent if their names are the same.
     * Two agents with the a null name are "identical".
     */
    public boolean equals(Object o) {
        if (!(o instanceof AuthorisingAgent)) {
            return false;
        } else {
            AuthorisingAgent agent2 = (AuthorisingAgent) o;

            return (agent2.getName() == null && this.getName() == null) ||
                    (this.getName() != null && this.getName().equals(agent2.getName()));
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return name == null ? 0 : name.hashCode();
    }


    /* (non-Javadoc)
     * @see Annotatable#addAnnotation(Annotation). */
    public void addAnnotation(Annotation annotation) {
        annotations.add(annotation);
        annotationsSorted = false;
    }

    /* (non-Javadoc)
     * @see Annotatable#getAnnotation(int). */
    public Annotation getAnnotation(int index) {
        return annotations.get(index);
    }

    /* (non-Javadoc)
     * @see Annotatable#deleteAnnotation(int). */
    public void deleteAnnotation(int index) {
        Annotation annotation = annotations.get(index);
        if (annotation != null) {
            deletedAnnotations.add(annotation);
            annotations.remove(index);
        }
    }

    /* (non-Javadoc)
     * @see Annotatable#getAnnotations(). */
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    /* (non-Javadoc)
     * @see Annotatable#getDeletedAnnotations(). */
    public List<Annotation> getDeletedAnnotations() {
        return deletedAnnotations;
    }

    /* (non-Javadoc)
     * @see org.webcurator.domain.model.core.Annotatable#setAnnotations(java.util.List)
     */
    public void setAnnotations(List<Annotation> aAnnotations) {
        annotations = aAnnotations;
        deletedAnnotations.clear();
        annotationsSet = true;
        annotationsSorted = false;
    }

    /* (non-Javadoc)
     * @see org.webcurator.domain.model.core.Annotatable#isAnnotationsSet()
     */
    public boolean isAnnotationsSet() {
        return annotationsSet;
    }

    /*(non-Javadoc)
     * @see org.webcurator.domain.model.core.Annotatable#getSortedAnnotations()
     */
    public List<Annotation> getSortedAnnotations() {
        if (!annotationsSorted) {
            sortAnnotations();
        }
        return getAnnotations();
    }

    /*(non-Javadoc)
     * @see org.webcurator.domain.model.core.Annotatable#sortAnnotations()
     */
    public void sortAnnotations() {
        Collections.sort(annotations);
        annotationsSorted = true;
    }

    /**
     * This is a comparator to compare two authorisng agents.
     *
     * @author bbeaumont
     */
    public static class AuthorisingAgentComparator implements Comparator<AuthorisingAgent> {
        public int compare(AuthorisingAgent o1, AuthorisingAgent o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }
}
