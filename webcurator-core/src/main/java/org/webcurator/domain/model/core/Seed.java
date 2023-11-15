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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Defines a seed that is within the Target.
 *
 * @author bbeaumont
 */
// lazy="false"
@Entity
@Table(name = "SEED")
@NamedQueries(
        @NamedQuery(name = "org.webcurator.domain.model.core.Seed.Target.oid",query = "from Seed s where s.target.oid = :targetOid")
)
public class Seed extends AbstractIdentityObject {
    public final static String QUERY_SEED_BY_TARGET_ID = "org.webcurator.domain.model.core.Seed.Target.oid";
    /**
     * The unique ID of the seed
     **/
    @Id
    @NotNull
    @Column(name = "S_OID")
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
     * The seed itself
     **/
    @Size(max=1024)
    @Column(name = "S_SEED")
    private String seed;
    /**
     * The seed's target
     **/
    @ManyToOne
    @JoinColumn(name = "S_TARGET_ID")
    private Target target;
    /**
     * The set of related permissions
     */
    @ManyToMany(cascade = {CascadeType.REFRESH})
    @JoinTable(name = "SEED_PERMISSION",
            joinColumns = {@JoinColumn(name = "SP_SEED_ID")},
            inverseJoinColumns = {@JoinColumn(name = "SP_PERMISSION_ID")})
    private Set<Permission> permissions = new HashSet<Permission>();
    /**
     * Sets if the seed is primary or secondary.
     */
    @Column(name = "S_PRIMARY")
    private boolean primary;

    /**
     * Protected constructor to prevent instantiation by non-application
     * components. This class should be instantiated by the
     * BusinessObjectFactory.
     */
    protected Seed() {
    }


    /**
     * Returns the database OID of the seed.
     *
     * @return Returns the oid.
     */
    public Long getOid() {
        return oid;
    }

    /**
     * Set the OID of the seed.
     *
     * @param anOid The OID.
     */
    public void setOid(Long anOid) {
        this.oid = anOid;
    }

    /**
     * Gets the seed URL.
     *
     * @return Returns the seed.
     */
    public String getSeed() {
        return seed;
    }

    public String getUrlEncodedSeed() {
        try {
            return URLEncoder.encode(this.seed, "UTF-8");
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return this.seed;
    }

    /**
     * Sets the Seed URL.
     *
     * @param seed The seed to set.
     */
    public void setSeed(String seed) {
        this.seed = seed;
    }

    /**
     * Get the Target to which this seed belongs.
     *
     * @return Returns the target.
     */
    public Target getTarget() {
        return target;
    }

    /**
     * Set the Target to which this seed belongs.
     *
     * @param aTarget The target to set.
     */
    public void setTarget(Target aTarget) {
        this.target = aTarget;
    }

    /**
     * Get the set of permissions associated with this seed.
     */
    public Set<Permission> getPermissions() {
        return permissions;
    }

    /**
     * Set the permissions associated with this seed.
     *
     * @param permissions The permissions to set.
     */
    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    /**
     * Checks if the seed is approved for harvest at the given time. The seed
     * is approved if:
     * <ul>
     *   <li>There is at least one permission;</li>
     *   <li>At least one of those permissions is an approved permission;</li>
     *   <li>It is not attached to any denied permissions;</li>
     * </ul>
     *
     * @param dt The date/time to check that the seed is approved.
     */
    public boolean isApproved(Date dt) {
        boolean approved = false;

        for (Permission p : permissions) {
            if (p.containsTime(dt)) {
                if (p.getStatus() == Permission.STATUS_REJECTED) {
                    return false;
                } else if (p.getStatus() == Permission.STATUS_APPROVED) {
                    approved = true;
                }
            }
        }

        return approved;
    }

    /**
     * Checks if the seed is approved for harvest at the given time. The seed
     * is approved if:
     * <ul>
     *   <li>There is at least one permission;</li>
     *   <li>At least one of those permissions is an approved permission;</li>
     *   <li>It is not attached to any denied permissions;</li>
     * </ul>
     *
     * @param dt The date/time to check that the seed is approved.
     */
    public boolean isHarvestable(Date dt) {
        boolean approved = false;
        for (Permission p : permissions) {
            if (p.containsTime(dt)) {
                if (p.getStatus() != Permission.STATUS_APPROVED) {
                    return false;
                } else {
                    approved = true;
                }
            }
        }

        return approved;
    }

    /**
     * Checks if the seed is defined as a primary seed.
     *
     * @return true if primary; otherwise false.
     */
    public boolean isPrimary() {
        return primary;
    }


    /**
     * Sets whether this seed should be primary.
     *
     * @param primary true to set as primary; otherwise false.
     */
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }


    /**
     * Add a permission to the seed.
     *
     * @param aPermission The permission to add.
     */
    public void addPermission(Permission aPermission) {
        permissions.add(aPermission);
        target.setDirty(true);
    }

    /**
     * Remove a permission from the seed.
     *
     * @param aPermission The permission to remove.
     */
    public void removePermission(Permission aPermission) {
        permissions.remove(aPermission);
        target.setDirty(true);
    }
}
