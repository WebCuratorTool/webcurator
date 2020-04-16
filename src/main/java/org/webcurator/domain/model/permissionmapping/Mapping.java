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
package org.webcurator.domain.model.permissionmapping;

import org.webcurator.core.permissionmapping.HierarchicalPermissionMappingStrategy;
import org.webcurator.domain.model.core.Permission;
import org.webcurator.domain.model.core.UrlPattern;

import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import javax.persistence.*;


/**
 * The Mapping class records mappings between UrlPatterns and
 * Permissions based on effective base domains. The goal is to
 * provide fast lookups for the HierarchicalPermissionMappingStrategy.
 *
 * @author bbeaumont
 */
// lazy="true"
@Entity
@Table(name = "URL_PERMISSION_MAPPING")
@NamedQueries({
        @NamedQuery(name = "org.webcurator.domain.model.permissionmapping.Mapping.LIST",
                query = "from Mapping where domain=?1"),
        @NamedQuery(name = "org.webcurator.domain.model.permissionmapping.Mapping.FETCH",
                query = "from Mapping where oid=?1"),
        @NamedQuery(name = "org.webcurator.domain.model.permissionmapping.Mapping.DELETE",
                query = "delete from Mapping m where m.urlPatternId = :urlPatternId and m.permissionId = :permissionId"),
        @NamedQuery(name = "org.webcurator.domain.model.permissionmapping.Mapping.DELETE_BY_PERMISSION",
                query = "delete from Mapping m where m.permissionId = :permissionId")
})
public class Mapping {
    /**
     * Query identifier for fetching Mapping by oid
     */
    public static final String QUERY_BY_OID = "org.webcurator.domain.model.permissionmapping.Mapping.FETCH";
    /**
     * Query identifier for listing Mappings by domain
     */
    public static final String QUERY_BY_DOMAIN = "org.webcurator.domain.model.permissionmapping.Mapping.LIST";
    /**
     * Query identifier for deleting mappings for a given URL Pattern and Permission
     */
    public static final String DELETE = "org.webcurator.domain.model.permissionmapping.Mapping.DELETE";
    /**
     * Query identifier for deleting all mappings related to the specified site
     */
    public static final String DELETE_BY_PERMISSION = "org.webcurator.domain.model.permissionmapping.Mapping.DELETE_BY_PERMISSION";

    /**
     * The Oid
     */
    @Id
    @NotNull
    @Column(name = "UPM_OID")
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

    @Column(name = "UPM_PERMISSION_ID")
    private Long permissionId;

    @Column(name = "UPM_URL_PATTERN_ID")
    private Long urlPatternId;

//    /**
//     * The UrlPattern
//     */
//    @ManyToOne(cascade = CascadeType.PERSIST)
//    @JoinColumn(name = "UPM_URL_PATTERN_ID")
//    private UrlPattern urlPattern;
//    /**
//     * The Permission
//     */
//    @ManyToOne(cascade = CascadeType.PERSIST)
//    @JoinColumn(name = "UPM_PERMISSION_ID")
//    private Permission permission;

    /**
     * The calculate base domain
     */
    @Size(max=1024)
    @Column(name = "UPM_DOMAIN")
    private String domain;

    /**
     * Private constructor for Hibernate.
     */
    private Mapping() {
    }

    /**
     * Standard constructor for WCT usage.
     *
     * @param urlPattern The UrlPattern.
     * @param permission The Permission.
     */
    public Mapping(UrlPattern urlPattern, Permission permission) {
        if (urlPattern != null) {
//            this.urlPattern = urlPattern;
            this.urlPatternId = urlPattern.getOid();
            this.domain = HierarchicalPermissionMappingStrategy.calculateDomain(urlPattern.getPattern());
        }

        if (permission != null) {
            this.permissionId = permission.getOid();
//            this.permission = permission;
        }
    }

    /**
     * Get the OID of the Mapping.
     *
     * @return Returns the oid.
     */
    public Long getOid() {
        return oid;
    }

    /**
     * Hibernate method to set the OID.
     *
     * @param aOid The oid to set.
     */
    public void setOid(Long aOid) {
        this.oid = aOid;
    }


//    /**
//     * Returns the permission.
//     *
//     * @return Returns the permission.
//     */
//    public Permission getPermission() {
//        return permission;
//    }
//
//    /**
//     * Sets the permission. Private as this should only
//     * be called from Hibernate.
//     *
//     * @param permission The permission to set.
//     */
//    @SuppressWarnings("unused")
//    private void setPermission(Permission permission) {
//        this.permission = permission;
//    }
//
//    /**
//     * Returns the UrlPattern
//     *
//     * @return Returns the urlPattern.
//     */
//    public UrlPattern getUrlPattern() {
//        return urlPattern;
//    }
//
//    /**
//     * Sets the UrlPattern. Private as this should only be called
//     * from Hibernate.
//     *
//     * @param urlPattern The urlPattern to set.
//     */
//    @SuppressWarnings("unused")
//    private void setUrlPattern(UrlPattern urlPattern) {
//        this.urlPattern = urlPattern;
//    }

	public Long getPermissionId() {
		return permissionId;
	}

	public void setPermissionId(Long permissionId) {
		this.permissionId = permissionId;
	}

	public Long getUrlPatternId() {
		return urlPatternId;
	}

	public void setUrlPatternId(Long urlPatternId) {
		this.urlPatternId = urlPatternId;
	}

    /**
     * Gets the effective base domain.
     *
     * @return Returns the effective base domain.
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Sets the effective base domain. Private as this should only
     * be called from Hibernate.
     *
     * @param domain The domain to set.
     */
    @SuppressWarnings("unused")
    private void setDomain(String domain) {
        this.domain = domain;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
//        result = PRIME * result + ((permission == null) ? 0 : permission.getOid().hashCode());
//        result = PRIME * result + ((urlPattern == null) ? 0 : urlPattern.getOid().hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
//        final Mapping other = (Mapping) obj;
//        if (permission == null) {
//            if (other.permission != null)
//                return false;
//        } else if (!permission.getOid().equals(other.permission.getOid()))
//            return false;
//        if (urlPattern == null) {
//            if (other.urlPattern != null)
//                return false;
//        } else if (!urlPattern.getOid().equals(other.urlPattern.getOid()))
//            return false;
        return true;
    }
}
