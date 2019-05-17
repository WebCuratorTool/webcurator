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

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;


/**
 * Represents a child/parent relationship between an AbstractTarget and a
 * TargetGroup.
 * 
 * @author bbeaumont
 */
@Entity
@Table(name = "GROUP_MEMBER")
@NamedQueries({
		@NamedQuery(name = "org.webcurator.domain.model.core.GroupMember.getMembers",
				query = "select new org.webcurator.domain.model.dto.GroupMemberDTO(gm.oid, gm.parent.oid, gm.parent.name, gm.parent.owner.username, gm.parent.owner.agency.name, gm.child.oid, gm.child.name, gm.child.objectType, gm.child.owner.username, gm.child.owner.agency.name) from GroupMember gm where gm.parent.oid = :parentOid ORDER BY UPPER(gm.child.name)"),
		@NamedQuery(name = "org.webcurator.domain.model.core.GroupMember.cntMembers",
				query = "select count(*) from GroupMember gm where gm.parent.oid = :parentOid"),
		@NamedQuery(name = "org.webcurator.domain.model.core.GroupMember.getParents",
				query = "select new org.webcurator.domain.model.dto.GroupMemberDTO(gm.oid, gm.parent.oid, gm.parent.name, gm.parent.owner.username, gm.parent.owner.agency.name, gm.child.oid, gm.child.name, gm.child.objectType, gm.child.owner.username, gm.child.owner.agency.name) from GroupMember gm where gm.child.oid = :childOid ORDER BY UPPER(gm.parent.name)"),
		@NamedQuery(name = "org.webcurator.domain.model.core.GroupMember.cntParents",
				query = "select count(*) from GroupMember gm where gm.child.oid = :childOid"),
		@NamedQuery(name = "org.webcurator.domain.model.core.GroupMember.getMemberStates",
				query = "select gm.child.state from GroupMember gm where gm.parent.oid = :parentOid")
})
public class GroupMember {
	/** The query identifier to get the GroupMemberDTO child objects */
	public static final String QUERY_GET_MEMBERS = "org.webcurator.domain.model.core.GroupMember.getMembers";
	public static final String QUERY_CNT_MEMBERS = "org.webcurator.domain.model.core.GroupMember.cntMembers";
	
	/** The query identifier to get the GroupMemberDTO parent objects */
	public static final String QUERY_GET_PARENTS = "org.webcurator.domain.model.core.GroupMember.getParents";
	public static final String QUERY_CNT_PARENTS = "org.webcurator.domain.model.core.GroupMember.cntParents";	

	/** The query identifier to get the GroupMemberDTO member states */
	public static final String QUERY_GET_MEMBERSTATES = "org.webcurator.domain.model.core.GroupMember.getMemberStates";
	
	/** The database oid */
	@Id
	@Column(name="AT_OID", nullable =  false)
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
	private Long oid = null;
	/** The parent of the member */
	@ManyToOne
	@JoinColumn(name = "GM_PARENT_ID", foreignKey = @ForeignKey(name = "FK_GM_PARENT_ID"))
	private TargetGroup parent = null;
	/** The child */
	@ManyToOne
	@JoinColumn(name = "GM_CHILD_ID", foreignKey = @ForeignKey(name = "FK_GM_CHILD_ID"))
	private AbstractTarget child = null;

	
    /**
     * Get the OID of the GroupMember.
     * @return Returns the oid.
     */
    public Long getOid() {
        return oid;
    }

    /**
     * Set the OID of the GroupMember.
     * @param aOid The oid to set.
     */
    public void setOid(Long aOid) {
        this.oid = aOid;
    }	
	
	/**
	 * Get the child of the relationship.
	 * @return Returns the child.
	 */
	public AbstractTarget getChild() {
		return child;
	}
	/**
	 * Set the child of the relationship.
	 * @param child The child to set.
	 */
	public void setChild(AbstractTarget child) {
		this.child = child;
	}
	
	/**
	 * Get the parent of the relationship.
	 * @return Returns the parent.
	 */
	public TargetGroup getParent() {
		return parent;
	}
	
	/**
	 * Set the parent of the relationship.
	 * @param parent The parent to set.
	 */
	public void setParent(TargetGroup parent) {
		this.parent = parent;
	}

}
