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
package org.webcurator.domain;

import jakarta.persistence.criteria.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.webcurator.common.ui.CommandConstants;
import org.webcurator.common.ui.Constants;
import org.webcurator.common.util.Utils;
import org.webcurator.core.common.EnvironmentFactory;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.targets.PermissionCriteria;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.domain.model.auth.User;
import org.webcurator.domain.model.core.*;
import org.webcurator.domain.model.dto.AbstractTargetDTO;
import org.webcurator.domain.model.dto.GroupMemberDTO;
import org.webcurator.domain.model.dto.GroupMemberDTO.SAVE_STATE;

import java.util.*;

/**
 * The TargetDAO provides access to targets, target groups and their related objects
 * from the persistent store.
 * @author bbeaumont
 */
@SuppressWarnings("all")
@Transactional
public class TargetDAO extends BaseDAO {
    private Log log = LogFactory.getLog(TargetDAO.class);

    public void save(final Target aTarget) {
        save(aTarget, null);
    }

    public void save(final Target aTarget, final List<GroupMemberDTO> parents) {
        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {
                            log.debug("Before Saving of Target");
                            Session session=currentSession();
                            session.saveOrUpdate(aTarget);

                            if (parents != null) {
                                for (GroupMemberDTO parent : parents) {
                                    switch (parent.getSaveState()) {
                                        case NEW:
                                            GroupMember member = new GroupMember();
                                            TargetGroup grp = loadGroup(parent.getParentOid());
                                            member.setParent(grp);
                                            member.setChild(aTarget);
                                            grp.getChildren().add(member);
                                            aTarget.getParents().add(member);
                                            session.save(member);
                                            break;

                                        case DELETED:
                                            session.createQuery("delete GroupMember where child.oid = :childOid and parent.oid = :parentOid")
                                                    .setParameter("childOid", aTarget.getOid())
                                                    .setParameter("parentOid", parent.getParentOid())
                                                    .executeUpdate();
                                            break;
                                    }
                                }
                            }

                            log.debug("After Saving Target");
                        } catch (Exception ex) {
                            log.debug("Setting Rollback Only", ex);
                            ts.setRollbackOnly();
                            throw new WCTRuntimeException("Failed to save target", ex);
                        }
                        return null;
                    }
                }
        );
    }


    public void save(final Schedule aSchedule) {
        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {
                            log.debug("Before Saving of Schedule");
                            currentSession().saveOrUpdate(aSchedule);
                            log.debug("After Saving Schedule");
                        } catch (Exception ex) {
                            log.debug("Setting Rollback Only", ex);
                            ts.setRollbackOnly();
                            throw new WCTRuntimeException("Failed to save schedule", ex);
                        }
                        return null;
                    }
                }
        );
    }

    public void save(final TargetGroup aTargetGroup, final boolean withChildren, final List<GroupMemberDTO> parents) {
        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {
                            log.debug("Before Saving of TargetGroup");

                            currentSession().saveOrUpdate(aTargetGroup);

                            // Save all the new children.
                            if (withChildren) {
                                List<GroupMemberDTO> groupMemberDTOs = aTargetGroup.getNewChildren();
                                for (GroupMemberDTO dto : groupMemberDTOs) {

                                    // Only consider new items not in the "Remove Children" list.
                                    if (!aTargetGroup.getRemovedChildren().contains(dto.getChildOid())) {
                                        GroupMember member = new GroupMember();
                                        member.setParent(aTargetGroup);
                                        AbstractTarget child = loadAbstractTarget(dto.getChildOid());
                                        member.setChild(child);
                                        currentSession().save(member);
                                    }
                                }

                                // Delete all the removed children.
                                for (Long childOid : aTargetGroup.getRemovedChildren()) {
                                    currentSession().createQuery("delete GroupMember where child.oid = :childOid and parent.oid = :parentOid")
                                            .setParameter("childOid", childOid)
                                            .setParameter("parentOid", aTargetGroup.getOid())
                                            .executeUpdate();
                                }
                            }

                            if (parents != null) {
                                for (GroupMemberDTO parent : parents) {
                                    switch (parent.getSaveState()) {
                                        case NEW:
                                            GroupMember member = new GroupMember();
                                            TargetGroup grp = loadGroup(parent.getParentOid());
                                            member.setParent(grp);
                                            member.setChild(aTargetGroup);
                                            grp.getChildren().add(member);
                                            aTargetGroup.getParents().add(member);
                                            currentSession().save(member);
                                            break;

                                        case DELETED:
                                            currentSession().createQuery("delete GroupMember where child.oid = :childOid and parent.oid = :parentOid")
                                                    .setParameter("childOid", aTargetGroup.getOid())
                                                    .setParameter("parentOid", parent.getParentOid())
                                                    .executeUpdate();
                                            break;
                                    }
                                }
                            }

                            log.debug("After Saving TargetGroup");
                        } catch (Exception ex) {
                            log.debug("Setting Rollback Only", ex);
                            ts.setRollbackOnly();
                            throw new WCTRuntimeException("Failed to save Target Group", ex);
                        }
                        return null;
                    }
                }
        );
    }

    public Target load(long targetOid) {
        return load(targetOid, false);
    }

    public Target load(final long targetOid, final boolean fullyInitialise) {

        return (Target) getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session aSession) throws HibernateException {
                if (!fullyInitialise) {
                    Target aTarget = (Target) aSession.load(Target.class, targetOid);
                    aTarget.setDirty(false);
                    return aTarget;
                } else {
                    // Initialise some more items that we'll need. This is used
                    // to prevent lazy load exceptions, since we're doing things
                    // across multiple sessions.
                    Target t = (Target) aSession.load(Target.class, targetOid);

                    Hibernate.initialize(t.getSeeds());
                    Hibernate.initialize(t.getSchedules());
                    Hibernate.initialize(t.getOverrides());
                    Hibernate.initialize(t.getOverrides().getExcludeUriFilters());
                    Hibernate.initialize(t.getOverrides().getIncludeUriFilters());
                    Hibernate.initialize(t.getOverrides().getCredentials());

                    for (Seed s : t.getSeeds()) {
                        Hibernate.initialize(s.getPermissions());
                    }

                    t.setDirty(false);

                    return t;
                }
            }
        });
    }


    public Pagination getMembers(final TargetGroup aTargetGroup, final int pageNum, final int pageSize) {
        if (aTargetGroup.isNew()) {
            return new Pagination(aTargetGroup.getNewChildren(), pageNum, pageSize);
        } else {
            return (Pagination) getHibernateTemplate().execute(
                    new HibernateCallback() {
                        public Object doInHibernate(Session session) {
                            Query query = session.getNamedQuery(GroupMember.QUERY_GET_MEMBERS);
                            Query cntQuery = session.getNamedQuery(GroupMember.QUERY_CNT_MEMBERS);
                            query.setParameter("parentOid", aTargetGroup.getOid());
                            cntQuery.setParameter("parentOid", aTargetGroup.getOid());
                            Pagination pagination = new Pagination(aTargetGroup.getNewChildren(), cntQuery, query, pageNum, pageSize);
                            return pagination;
                        }
                    }
            );
        }
    }

    @SuppressWarnings("unchecked")
    public List<Integer> getSavedMemberStates(final TargetGroup aTargetGroup) {
        if (aTargetGroup.isNew()) {
            return new LinkedList<Integer>();
        } else {
            return (List<Integer>) getHibernateTemplate().execute(
                    new HibernateCallback() {
                        @SuppressWarnings("unchecked")
                        public Object doInHibernate(Session session) {
                            Query q = session.getNamedQuery(GroupMember.QUERY_GET_MEMBERSTATES);
                            q.setParameter("parentOid", aTargetGroup.getOid());
                            List<Integer> states = q.list();

                            return states;
                        }
                    }
            );
        }
    }


    @SuppressWarnings("unchecked")
    public List<GroupMemberDTO> getParents(final AbstractTarget aTarget) {
        if (aTarget.isNew()) {
            return new LinkedList<GroupMemberDTO>();
        } else {
            return (List<GroupMemberDTO>) getHibernateTemplate().execute(
                    new HibernateCallback() {
                        @SuppressWarnings("unchecked")
                        public Object doInHibernate(Session session) {
                            Query q = session.getNamedQuery(GroupMember.QUERY_GET_PARENTS);
                            q.setParameter("childOid", aTarget.getOid());
                            List<GroupMemberDTO> dtos = q.list();

                            for (GroupMemberDTO dto : dtos) {
                                dto.setSaveState(SAVE_STATE.ORIGINAL);
                            }

                            return dtos;
                        }
                    }
            );
        }
    }


    public Pagination getParents(final AbstractTarget aTarget, final int pageNum, final int pageSize) {
        if (aTarget.isNew()) {
            return new Pagination(aTarget.getNewParents(), pageNum, pageSize);
        } else {
            return (Pagination) getHibernateTemplate().execute(
                    new HibernateCallback() {
                        public Object doInHibernate(Session session) {
                            Query query = session.getNamedQuery(GroupMember.QUERY_GET_PARENTS);
                            Query cntQuery = session.getNamedQuery(GroupMember.QUERY_CNT_PARENTS);
                            query.setParameter("childOid", aTarget.getOid());
                            cntQuery.setParameter("childOid", aTarget.getOid());
                            //FIXME Need to get the new parent groups.
                            Pagination pagination = new Pagination(aTarget.getNewParents(), cntQuery, query, pageNum, pageSize);
                            return pagination;
                        }
                    }
            );
        }
    }


    public Pagination getTargetsForProfile(final int pageNumber, final int pageSize, final Long profileOid, final String agencyName) {
        return (Pagination) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {

                        CriteriaBuilder cb = session.getCriteriaBuilder();
                        CriteriaQuery<Target> query = cb.createQuery(Target.class);
                        CriteriaQuery<Long> cntQuery = cb.createQuery(Long.class);
                        Root<Target> root = query.from(Target.class);
                        Root<Target> cntRoot = cntQuery.from(Target.class);
                        query.select(root);
                        cntQuery.select(cb.count(cntRoot));

                        Predicate profileOidPredicate = cb.and(); // Set it to true by default
                        Predicate cntProfileOidPredicate = cb.and();
                        if (profileOid != null) {
                            Join<Target, Profile> profileJoin = root.join("profile");
                            profileOidPredicate = cb.equal(profileJoin.get("oid"), profileOid);
                            Join<Target, Profile> cntProfileJoin = cntRoot.join("profile");
                            cntProfileOidPredicate = cb.equal(cntProfileJoin.get("oid"), profileOid);
                        }

                        Predicate agencyNamePredicate = cb.and();
                        Predicate cntAgencyNamePredicate = cb.and();
                        if (!Utils.isEmpty(agencyName)) {
                            Join<User, Agency> agencyJoin = root.join("owner").join("agency");
                            Join<User, Agency> cntAgencyJoin = cntRoot.join("owner").join("agency");
                            agencyNamePredicate = cb.equal(agencyJoin.get("name"), agencyName);
                            cntAgencyNamePredicate = cb.equal(cntAgencyJoin.get("name"), agencyName);
                        }

                        Predicate whereClause = cb.and(profileOidPredicate, agencyNamePredicate);
                        Predicate cntWhereClause = cb.and(cntProfileOidPredicate, cntAgencyNamePredicate);
                        query.where(whereClause);
                        cntQuery.where(cntWhereClause);
                        query.orderBy(cb.asc(root.get("name")));

                        return new Pagination(session.createQuery(cntQuery), session.createQuery(query), pageNumber, pageSize);
                    }
                }
        );
    }


    public Pagination getAbstractTargetDTOsForProfile(final int pageNumber, final int pageSize, final Long profileOid) {
        return (Pagination) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {

                        Query q = session.getNamedQuery(AbstractTarget.QUERY_TARGET_DTOS_BY_PROFILE);
                        Query cq = session.getNamedQuery(AbstractTarget.QUERY_CNT_TARGET_DTOS_BY_PROFILE);
                        q.setParameter("profileoid", profileOid);
                        cq.setParameter("profileoid", profileOid);

                        return new Pagination(cq, q, pageNumber, pageSize);
                    }
                }
        );
    }


    public Pagination search(final int pageNumber, final int pageSize, final Long searchOid, final String targetName, final Set<Integer> states, final String seed, final String username, final String agencyName, final String memberOf, final boolean nondisplayonly, final String sortorder, final String description) {
        return (Pagination) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {

                        CriteriaBuilder cb = session.getCriteriaBuilder();
                        CriteriaQuery<Target> query = cb.createQuery(Target.class);
                        CriteriaQuery<Long> cntQuery = cb.createQuery(Long.class);
                        Root<Target> root = query.from(Target.class);
                        Root<Target> cntRoot = cntQuery.from(Target.class);
                        query.select(root);
                        cntQuery.select(cb.count(cntRoot));

                        query.distinct(true);
                        cntQuery.distinct(true);

                        Predicate targetNamePredicate = cb.and(); // Set it to true by default
                        Predicate cntTargetNamePredicate = cb.and();
                        if (targetName != null && !"".equals(targetName.trim())) {
                            targetNamePredicate = cb.like(root.get("name"), targetName.trim() + "%");
                            cntTargetNamePredicate = cb.like(cntRoot.get("name"), targetName.trim() + "%");
                        }

                        Predicate descriptionPredicate = cb.and();
                        Predicate cntDescriptionPredicate = cb.and();
                        if (description != null && !"".equals(description.trim())) {
                            descriptionPredicate = cb.like(root.get("description"), "%" + description.trim() + "%");
                            cntDescriptionPredicate = cb.like(cntRoot.get("description"), "%" + description.trim() + "%");
                        }

                        Predicate statesPredicate = cb.and();
                        Predicate cntStatesPredicate = cb.and();
                        if (states != null && states.size() > 0) {
                            List<Predicate> predicates = new ArrayList<>();
                            List<Predicate> cntPredicates = new ArrayList<>();
                            for (Integer i : states) {
                                predicates.add(cb.equal(root.get("state"), i));
                                cntPredicates.add(cb.equal(cntRoot.get("state"), i));
                            }
                            statesPredicate = cb.or(predicates.toArray(new Predicate[predicates.size()]));
                            cntStatesPredicate = cb.or(cntPredicates.toArray(new Predicate[cntPredicates.size()]));
                        }

                        Predicate seedPredicate = cb.and();
                        Predicate cntSeedPredicate = cb.and();
                        if (seed != null && !"".equals(seed.trim())) {
                            Join<Target, Seed> seedJoin = root.join("seeds");
                            Join<Target, Seed> cntSeedJoin = cntRoot.join("seeds");
                            seedPredicate = cb.like(seedJoin.get("seed"), seed.trim() + "%");
                            cntSeedPredicate = cb.like(cntSeedJoin.get("seed"), seed.trim() + "%");
                        }

                        Predicate userNamePredicate = cb.and();
                        Predicate cntUserNamePredicate = cb.and();
                        Join<Target, User> userJoin = null;
                        Join<Target, User> cntUserJoin = null;
                        if (!Utils.isEmpty(username)) {
                            userJoin = root.join("owner");
                            cntUserJoin = cntRoot.join("owner");
                            userNamePredicate = cb.equal(userJoin.get("username"), username);
                            cntUserNamePredicate = cb.equal(cntUserJoin.get("username"), username);
                        }

                        // Parents criteria; note that this involves a many-to-many self join
                        Predicate memberOfPredicate = cb.and();
                        Predicate cntMemberOfPredicate = cb.and();
                        if (!Utils.isEmpty(memberOf)) {
                            Join<GroupMember, Target> groupMemberJoin = root.join("parents").join("parent");
                            Join<GroupMember, Target> cntGroupMemberJoin = cntRoot.join("parents").join("parent");
                            memberOfPredicate = cb.like(groupMemberJoin.get("name"), memberOf.trim() + "%");
                            cntMemberOfPredicate = cb.like(cntGroupMemberJoin.get("name"), memberOf.trim() + "%");
                        }

                        Predicate agencyNamePredicate = cb.and();
                        Predicate cntAgencyNamePredicate = cb.and();
                        if (!Utils.isEmpty(agencyName)) {
                            if (userJoin == null) {
                                userJoin = root.join("owner");
                                cntUserJoin = cntRoot.join("owner");
                            }
                            Join<User, Agency> agencyJoin = userJoin.join("agency");
                            Join<User, Agency> cntAgencyJoin = cntUserJoin.join("agency");
                            agencyNamePredicate = cb.equal(agencyJoin.get("name"), agencyName);
                            cntAgencyNamePredicate = cb.equal(cntAgencyJoin.get("name"), agencyName);
                        }

                        Predicate searchOidPredicate = cb.and();
                        Predicate cntSearchOidPredicate = cb.and();
                        if (searchOid != null) {
                            searchOidPredicate = cb.equal(root.get("oid"), searchOid);
                            cntSearchOidPredicate = cb.equal(cntRoot.get("oid"), searchOid);
                        }

                        Predicate nondisplayonlyPredicate = cb.and();
                        Predicate cntNondisplayonlyPredicate = cb.and();
                        if (nondisplayonly) {
                            nondisplayonlyPredicate = cb.equal(root.get("displayTarget"), false);
                            cntNondisplayonlyPredicate = cb.equal(cntRoot.get("displayTarget"), false);
                        }

                        if (sortorder == null || sortorder.equals(CommandConstants.TARGET_SEARCH_COMMAND_SORT_NAME_ASC)) {
                            query.orderBy(cb.asc(root.get("name")));
                        } else if (sortorder.equals(CommandConstants.TARGET_SEARCH_COMMAND_SORT_NAME_DESC)) {
                            query.orderBy(cb.desc(root.get("name")));
                        } else if (sortorder.equals(CommandConstants.TARGET_SEARCH_COMMAND_SORT_DATE_ASC)) {
                            query.orderBy(cb.asc(root.get("creationDate")));
                        } else if (sortorder.equals(CommandConstants.TARGET_SEARCH_COMMAND_SORT_DATE_DESC)) {
                            query.orderBy(cb.desc(root.get("creationDate")));
                        }

                        Predicate whereClause = cb.and(targetNamePredicate, descriptionPredicate, statesPredicate,
                                seedPredicate, userNamePredicate, memberOfPredicate, agencyNamePredicate, searchOidPredicate,
                                nondisplayonlyPredicate);
                        Predicate cntWhereClause = cb.and(cntTargetNamePredicate, cntDescriptionPredicate, cntStatesPredicate,
                                cntSeedPredicate, cntUserNamePredicate, cntMemberOfPredicate, cntAgencyNamePredicate, cntSearchOidPredicate,
                                cntNondisplayonlyPredicate);
                        query.where(whereClause);
                        cntQuery.where(cntWhereClause);

                        return new Pagination(session.createQuery(cntQuery), session.createQuery(query), pageNumber, pageSize);
                    }
                }
        );
    }


    public Pagination searchGroups(final int pageNumber, final int pageSize, final Long searchOid, final String name, final String owner, final String agency, final String memberOf, final String groupType, final boolean nondisplayonly) {
        return (Pagination) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {

                        CriteriaBuilder cb = session.getCriteriaBuilder();
                        CriteriaQuery<Target> query = cb.createQuery(Target.class);
                        CriteriaQuery<Long> cntQuery = cb.createQuery(Long.class);
                        Root<Target> root = query.from(Target.class);
                        Root<Target> cntRoot = cntQuery.from(Target.class);
                        query.select(root);
                        cntQuery.select(cb.count(cntRoot));

                        Predicate namePredicate = cb.and(); // Set it to true by default
                        Predicate cntNamePredicate = cb.and();
                        if (name != null && !"".equals(name.trim())) {
                            namePredicate = cb.like(root.get("name"), "%" + name.trim() + "%");
                            cntNamePredicate = cb.like(cntRoot.get("name"), "%" + name.trim() + "%");
                        }

                        Predicate ownerPredicate = cb.and();
                        Predicate cntOwnerPredicate = cb.and();
                        Join<Target, User> userJoin = null;
                        Join<Target, User> cntUserJoin = null;
                        if (!Utils.isEmpty(owner)) {
                            userJoin = root.join("owner");
                            cntUserJoin = cntRoot.join("owner");
                            ownerPredicate = cb.equal(userJoin.get("username"), owner);
                            cntOwnerPredicate = cb.equal(cntUserJoin.get("username"), owner);
                        }

                        // Parents criteria; note that this involves a many-to-many self join
                        Predicate memberOfPredicate = cb.and();
                        Predicate cntMemberOfPredicate = cb.and();
                        if (!Utils.isEmpty(memberOf)) {
                            Join<GroupMember, Target> groupMemberJoin = root.join("parents").join("parent");
                            Join<GroupMember, Target> cntGroupMemberJoin = cntRoot.join("parents").join("parent");
                            memberOfPredicate = cb.like(groupMemberJoin.get("name"), memberOf.trim() + "%");
                            cntMemberOfPredicate = cb.like(cntGroupMemberJoin.get("name"), memberOf.trim() + "%");
                        }

                        Predicate groupTypePredicate = cb.and();
                        Predicate cntGroupTypePredicate = cb.and();
                        if (!Utils.isEmpty(groupType)) {
                            groupTypePredicate = cb.equal(root.get("type"), groupType);
                            cntGroupTypePredicate = cb.equal(cntRoot.get("type"), groupType);
                        }

                        Predicate agencyPredicate = cb.and();
                        Predicate cntAgencyPredicate = cb.and();
                        if (!Utils.isEmpty(agency)) {
                            if (userJoin == null) {
                                userJoin = root.join("owner");
                                cntUserJoin = cntRoot.join("owner");
                            }
                            Join<User, Agency> agencyJoin = userJoin.join("agency");
                            Join<User, Agency> cntAgencyJoin = cntUserJoin.join("agency");
                            agencyPredicate = cb.equal(agencyJoin.get("name"), agency);
                            cntAgencyPredicate = cb.equal(cntAgencyJoin.get("name"), agency);
                        }

                        Predicate searchOidPredicate = cb.and();
                        Predicate cntSearchOidPredicate = cb.and();
                        if (searchOid != null) {
                            searchOidPredicate = cb.equal(root.get("oid"), searchOid);
                            cntSearchOidPredicate = cb.equal(cntRoot.get("oid"), searchOid);
                        }

                        Predicate nondisplayonlyPredicate = cb.and();
                        Predicate cntNondisplayonlyPredicate = cb.and();
                        if (nondisplayonly) {
                            nondisplayonlyPredicate = cb.equal(root.get("displayTarget"), false);
                            cntNondisplayonlyPredicate = cb.equal(cntRoot.get("displayTarget"), false);
                        }

                        query.orderBy(cb.asc(root.get("name")));

                        Predicate whereClause = cb.and(namePredicate, ownerPredicate, memberOfPredicate, groupTypePredicate,
                                agencyPredicate, searchOidPredicate, nondisplayonlyPredicate);
                        Predicate cntWhereClause = cb.and(cntNamePredicate, cntOwnerPredicate, cntMemberOfPredicate, cntGroupTypePredicate,
                                cntAgencyPredicate, cntSearchOidPredicate, cntNondisplayonlyPredicate);
                        query.where(whereClause);
                        cntQuery.where(cntWhereClause);

                        return new Pagination(session.createQuery(cntQuery), session.createQuery(query), pageNumber, pageSize);
                    }
                }
        );
    }


    public long countTargets(final String username) {
        return (Long) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {

                        CriteriaBuilder cb = session.getCriteriaBuilder();
                        CriteriaQuery<Long> query = cb.createQuery(Long.class);
                        Root<Target> root = query.from(Target.class);
                        query.select(cb.count(root));

                        Predicate whereClause = cb.and();
                        if (!Utils.isEmpty(username)) {
                            Join<Target, User> userJoin = root.join("owner");
                            whereClause = cb.equal(userJoin.get("username"), username);
                        }
                        query.where(whereClause);
                        Long count = session.createQuery(query).uniqueResult();

                        return count;
                    }
                }
        );
    }


    public long countTargetGroups(final String username) {
        return (Long) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {
                        CriteriaBuilder cb = session.getCriteriaBuilder();
                        CriteriaQuery<Long> query = cb.createQuery(Long.class);
                        Root<TargetGroup> root = query.from(TargetGroup.class);
                        query.select(cb.count(root));

                        Predicate whereClause = cb.and();
                        if (!Utils.isEmpty(username)) {
                            Join<TargetGroup, User> userJoin = root.join("owner");
                            whereClause = cb.equal(userJoin.get("username"), username);
                        }
                        query.where(whereClause);
                        Long count = session.createQuery(query).uniqueResult();

                        return count;
                    }
                }
        );
    }


    /**
     * @param txTemplate The txTemplate to set.
     */
    public void setTxTemplate(TransactionTemplate txTemplate) {
        this.txTemplate = txTemplate;
    }


    public boolean isNameOk(AbstractTarget aTarget) {

        CriteriaBuilder cb = currentSession().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<AbstractTarget> root = query.from(AbstractTarget.class);
        query.select(cb.count(root));

        Predicate namePredicate = cb.equal(root.get("name"), aTarget.getName());

        Predicate objectTypePredicate = cb.and();
        if (aTarget instanceof TargetGroup) {
            objectTypePredicate = cb.equal(root.get("objectType"), 0);
        } else if (aTarget instanceof Target) {
            objectTypePredicate = cb.equal(root.get("objectType"), 1);
        }

        Predicate oidPredicate = cb.and();
        if (aTarget.getOid() != null) {
            oidPredicate = cb.notEqual(root.get("oid"), aTarget.getOid());
        }

        Predicate whereClause = cb.and(namePredicate, objectTypePredicate, oidPredicate);
        query.where(whereClause);
        Long count = (Long) currentSession().createQuery(query).uniqueResult();

        return count == 0L;
    }


    public Pagination getAbstractTargetDTOs(final String name, final int pageNumber, final int pageSize) {
        return (Pagination) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {

                        Query q = session.getNamedQuery(AbstractTarget.QUERY_DTO_BY_NAME);
                        Query cq = session.getNamedQuery(AbstractTarget.QUERY_CNT_DTO_BY_NAME);
                        q.setParameter(1, name);
                        cq.setParameter(1, name);

                        return new Pagination(cq, q, pageNumber, pageSize);
                    }
                }
        );
    }

    public Pagination getGroupDTOs(final String name, final int pageNumber, final int pageSize) {
        return (Pagination) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {

                        Query q = session.getNamedQuery(AbstractTarget.QUERY_GROUP_DTOS_BY_NAME);
                        Query cq = session.getNamedQuery(AbstractTarget.QUERY_CNT_GROUP_DTOS_BY_NAME);
                        q.setParameter(1, name);
                        cq.setParameter(1, name);

                        return new Pagination(cq, q, pageNumber, pageSize);
                    }
                }
        );
    }

    public Pagination getSubGroupParentDTOs(final String name, final List types, final int pageNumber, final int pageSize) {
        return (Pagination) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {

                        Query q = session.getNamedQuery(TargetGroup.QUERY_GROUP_DTOS_BY_NAME_AND_TYPE);
                        Query cq = session.getNamedQuery(TargetGroup.QUERY_CNT_GROUP_DTOS_BY_NAME_AND_TYPE);
                        q.setParameter("name", name);
                        q.setParameterList("types", types);
                        cq.setParameter("name", name);
                        cq.setParameterList("types", types);

                        return new Pagination(cq, q, pageNumber, pageSize);
                    }
                }
        );
    }

    public Pagination getNonSubGroupDTOs(final String name, final String subGroupType, final int pageNumber, final int pageSize) {
        return (Pagination) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {

                        Query q = session.getNamedQuery(AbstractTargetGroupTypeView.QUERY_NON_SUBGROUP_DTOS_BY_NAME_AND_TYPE);
                        Query cq = session.getNamedQuery(AbstractTargetGroupTypeView.QUERY_CNT_NON_SUBGROUP_DTOS_BY_NAME_AND_TYPE);
                        q.setParameter("name", name);
                        q.setParameter("subgrouptype", subGroupType);
                        cq.setParameter("name", name);
                        cq.setParameter("subgrouptype", subGroupType);

                        return new Pagination(cq, q, pageNumber, pageSize);
                    }
                }
        );
    }

    public AbstractTargetDTO loadAbstractTargetDTO(final Long oid) {
        return (AbstractTargetDTO) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {
                        return session.getNamedQuery(AbstractTarget.QUERY_DTO_BY_OID)
                                .setParameter("oid", oid)
                                .uniqueResult();
                    }
                }
        );
    }

    public TargetGroup loadGroup(long targetGroupOid) {
        return loadGroup(targetGroupOid, false);
    }

    public TargetGroup loadGroup(final long targetGroupOid, final boolean fullyInitialise) {
        return (TargetGroup) getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session aSession) throws HibernateException {
                if (!fullyInitialise) {
                    TargetGroup aTargetGroup = (TargetGroup) aSession.load(TargetGroup.class, targetGroupOid);
                    aTargetGroup.setDirty(false);
                    return aTargetGroup;
                } else {
                    // Initialise some more items that we'll need. This is used
                    // to prevent lazy load exceptions, since we're doing things
                    // across multiple sessions.
                    TargetGroup t = (TargetGroup) aSession.load(TargetGroup.class, targetGroupOid);

                    Hibernate.initialize(t.getSchedules());
                    Hibernate.initialize(t.getOverrides());
                    Hibernate.initialize(t.getOverrides().getExcludeUriFilters());
                    Hibernate.initialize(t.getOverrides().getIncludeUriFilters());
                    Hibernate.initialize(t.getOverrides().getCredentials());
                    //Hibernate.initialize(t.getChildren());

                    t.setDirty(false);

                    return t;
                }
            }
        });
    }


    public AbstractTarget loadAbstractTarget(Long oid) {
        return (AbstractTarget) getHibernateTemplate().load(AbstractTarget.class, oid);
    }

    public void refresh(Object anObject) {
        currentSession().refresh(anObject);
    }

    public TargetGroup reloadTargetGroup(Long oid) {
        // Evict the group from the session and reload.
        currentSession().evict(getHibernateTemplate().load(TargetGroup.class, oid));

        return (TargetGroup) getHibernateTemplate().load(TargetGroup.class, oid);
    }

    public Target reloadTarget(Long oid) {
        // Evict the group from the session and reload.
        currentSession().evict(getHibernateTemplate().load(Target.class, oid));

        return (Target) getHibernateTemplate().load(Target.class, oid);
    }

    public Date getLatestScheduledDate(final AbstractTarget aTarget, final Schedule aSchedule) {
        return (Date) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session aSession) {
                Query query = aSession.getNamedQuery(TargetInstance.QUERY_GET_LATEST_FOR_TARGET);
                query.setParameter("targetOid", aTarget.getOid());
                query.setParameter("scheduleOid", aSchedule.getOid());

                Date dt = (Date) query.uniqueResult();
                return dt;
            }
        });
    }


    @SuppressWarnings(value = "unchecked")
    public Set<Seed> getSeeds(final Target aTarget) {
        List<Seed> rst = (List<Seed>) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session aSession) {
                Query q = aSession.createNamedQuery(Seed.QUERY_SEED_BY_TARGET_ID, Seed.class);
                q.setParameter("targetOid", aTarget.getOid(), Long.class);
                return q.list();
            }
        });

        Set<Seed> seeds = new HashSet<Seed>();
        seeds.addAll(rst);
        return seeds;
    }


    @SuppressWarnings("unchecked")
    public Set<Seed> getSeeds(final TargetGroup aTarget, final Long agencyOid, final String subGroupTypeName) {
        Set<Seed> seeds = new HashSet<Seed>();
        seeds.addAll((Set) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session aSession) {
                TargetGroup tg = (TargetGroup) aSession.load(TargetGroup.class, aTarget.getOid());
                return getSeeds(aSession, tg);
            }

            public Set<Seed> getSeeds(Session aSession, AbstractTarget target) {
                if (target.getObjectType() == AbstractTarget.TYPE_GROUP) {
                    Set<Seed> seeds = new HashSet<Seed>();
                    for (GroupMember groupMember : ((TargetGroup) target).getChildren()) {
                        AbstractTarget child = groupMember.getChild();

                        if (child.getObjectType() == AbstractTarget.TYPE_GROUP) {
                            TargetGroup childGroup;
                            if (child instanceof TargetGroup) {
                                childGroup = (TargetGroup) child;
                            } else {
                                childGroup = (TargetGroup) aSession.load(TargetGroup.class, child.getOid());
                            }

                            //If the childGroup is a sub-group, we don't want to include the seeds from the sub-group members
                            if (!subGroupTypeName.equals(childGroup.getType())) {
                                seeds.addAll(getSeeds(aSession, childGroup));
                            }
                        } else {
                            Target childTarget;
                            if (child instanceof Target) {
                                childTarget = (Target) child;
                            } else {
                                childTarget = (Target) aSession.load(Target.class, child.getOid());
                            }

                            if (isApprovedForHarvest(childTarget) && childTarget.getOwner().getAgency().getOid().equals(agencyOid)) {
                                seeds.addAll(childTarget.getSeeds());
                            }
                        }
                    }
                    return seeds;
                } else {
                    return target.getSeeds();
                }
            }

        }));
        return seeds;
    }

    private boolean isApprovedForHarvest(Target aTarget) {

        if (aTarget.getState() == Target.STATE_APPROVED || aTarget.getState() == Target.STATE_COMPLETED) {
            boolean approved = false;
            boolean foundBadSeed = false;

            Seed seed = null;
            Set<Seed> seeds = aTarget.getSeeds();
            Iterator<Seed> it = seeds.iterator();
            while (it.hasNext()) {
                seed = (Seed) it.next();
                if (!seed.isHarvestable(new Date())) {
                    foundBadSeed = true;
                    break;
                }
            }

            if (!seeds.isEmpty() && !foundBadSeed) {
                approved = true;
            }

            return approved;
        } else {
            return false;
        }
    }

    public boolean causesLoop(TargetGroup parentOid, AbstractTarget childOid) {


        return false;
    }

    @SuppressWarnings("unchecked")
    public Set<Long> getAncestorOids(final Long childOid) {
        Map<Long, Long> duplicateValidator=new HashMap<>();
        Set<Long> parents=getAncestorOidsInternal(childOid,duplicateValidator);
        duplicateValidator.clear();
        return parents;
    }
    private Set<Long> getAncestorOidsInternal(final Long childOid, final  Map<Long, Long> duplicateValidator) {
        if (childOid == null) {
            return Collections.EMPTY_SET;
        }

        if(!duplicateValidator.containsKey(childOid.longValue())){
            duplicateValidator.put(childOid.longValue(), childOid);
        }

        Set<Long> parentOids = new HashSet<Long>();

        List<Long> immediateParents = getHibernateTemplate().execute(session ->
                session.createQuery("SELECT new java.lang.Long(gm.parent.oid) FROM GroupMember gm where gm.child.oid = :childOid")
                        .setParameter("childOid", childOid)
                        .list());

        for (Long parentOid : immediateParents) {
            if(!duplicateValidator.containsKey(parentOid.longValue())) {
                parentOids.add(parentOid);
                parentOids.addAll(getAncestorOidsInternal(parentOid, duplicateValidator));
            }
        }

        return parentOids;

    }

    @SuppressWarnings("unchecked")
    public Set<AbstractTargetDTO> getAncestorDTOs(final Long childOid) {
        Map<Long, Long> duplicateValidator=new HashMap<>();
        Set<AbstractTargetDTO> parents=getAncestorDTOsInternal(childOid,duplicateValidator);
        duplicateValidator.clear();
        return parents;
    }

    private Set<AbstractTargetDTO> getAncestorDTOsInternal(final Long childOid, final  Map<Long, Long> duplicateValidator) {
        if (childOid == null) {
            return Collections.EMPTY_SET;
        }

        if(!duplicateValidator.containsKey(childOid.longValue())) {
            duplicateValidator.put(childOid.longValue(),childOid);
        }

        Set<AbstractTargetDTO> parents = new HashSet<AbstractTargetDTO>();

        List<AbstractTargetDTO> immediateParents = getHibernateTemplate().execute(session ->
                session.createQuery("SELECT new org.webcurator.domain.model.dto.AbstractTargetDTO(t.oid, t.name, t.owner.oid, t.owner.username, t.owner.agency.name, t.state, t.profile.oid, t.objectType) FROM TargetGroup t LEFT JOIN t.children AS gm INNER JOIN gm.child AS child where child.oid = :childOid")
                        .setParameter("childOid", childOid)
                        .list());

        for (AbstractTargetDTO parent : immediateParents) {
            if(!duplicateValidator.containsKey(parent.getOid().longValue())) {
                parents.add(parent);
                parents.addAll(getAncestorDTOsInternal(parent.getOid(), duplicateValidator));
            }
        }

        return parents;
    }


    @SuppressWarnings("unchecked")
    public Set<Long> getImmediateChildrenOids(final Long parentOid) {
        if (parentOid == null) {
            return Collections.EMPTY_SET;
        } else {
            List<Long> immediateChildren = getHibernateTemplate().execute(session ->
                    session.createQuery("SELECT new java.lang.Long(gm.child.oid) FROM GroupMember gm where gm.parent.oid = :parentOid")
                            .setParameter("parentOid", parentOid)
                            .list());

            Set<Long> retval = new HashSet<Long>();
            retval.addAll(immediateChildren);
            return retval;
        }
    }

    /**
     * Find all the groups that need to be end dated.
     *
     * @return A List of groups to be end dated.
     */
    @SuppressWarnings("unchecked")
    public List<TargetGroup> findEndedGroups() {
        // TODO HIBERNATE Note the previous version. This is an attempt to convert to JPA 2.0 notation using lambdas.
        // TODO HIBERNATE The joins and query may not be set up correctly and will need to be verified.
        List<TargetGroup> results = getHibernateTemplate().execute(session -> {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<TargetGroup> criteriaQuery = builder.createQuery(TargetGroup.class);
            Root<TargetGroup> root = criteriaQuery.from(TargetGroup.class);
            Predicate notEqual = builder.notEqual(root.get("state"), TargetGroup.STATE_ACTIVE);
            Predicate lessThan = builder.lessThan(root.get("toDate"), new Date());
            root.fetch("schedules", JoinType.LEFT);
            root.fetch("parents", JoinType.LEFT);
            root.fetch("parents", JoinType.LEFT);
            criteriaQuery.select(root).where(builder.and(notEqual, lessThan));
            return session.createQuery(criteriaQuery).list();
        });
        log.debug("Found " + results.size() + " groups that need to be unscheduled");

        return results;
    }


    /**
     * Load the persisted target group SIP type from the database.
     *
     * @return oid The OID of the TargetGroup.
     */
    public Integer loadPersistedGroupSipType(final Long oid) {
        return (Integer) getHibernateTemplate().execute(session ->
                session.createQuery("SELECT new java.lang.Integer(sipType) FROM TargetGroup WHERE oid=:groupOid")
                        .setParameter("groupOid", oid)
                        .uniqueResult());
    }


    @SuppressWarnings("unchecked")
    public List<Seed> getLinkedSeeds(final Permission aPermission) {
//        return getHibernateTemplate().execute(session ->
//                session.createNamedQuery(Seed.QUERY_SEED_BY_PERMISSION_OID, Seed.class)
//                        .setParameter(1, aPermission.getOid())
//                        .list());
        List<Seed> list = new ArrayList<Seed>();
        list.addAll(aPermission.getSeeds());
        return list;
    }


    /**
     * Transfer all seeds from one permission to another.
     *
     * @param fromPermissionOid The oid of the permission record to transfer
     *                          seeds from.
     * @param toPermissionOid   The oid of the permission record to transfer
     *                          seeds to.
     */
    public void transferSeeds(final Long fromPermissionOid, final Long toPermissionOid) {
        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {
                            return currentSession().createQuery("UPDATE SEED_PERMISSION SET SP_PERMISSION_ID = :toPermissionOid WHERE SP_PERMISSION_ID = :fromPermissionOid")
                                    .setParameter("toPermissionOid", toPermissionOid)
                                    .setParameter("fromPermissionOid", fromPermissionOid)
                                    .executeUpdate();

                        } catch (Exception ex) {
                            ts.setRollbackOnly();
                            log.error("Exception transferring seeds", ex);
                            throw new WCTRuntimeException("Exception transferring seeds", ex);
                        }
                    }
                }
        );
    }

    /**
     * Basic save all method. This will save all of the objects in the
     * collection to the database but will perform nothing more than the
     * Hibernate save/cascade logic. It should not be used to save a collection
     * of targets or target groups.
     *
     * @param collection A collection of objects to be saved.
     */
    public void saveAll(final Collection collection) {
        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {
                            log.debug("Before Saving Object");
                            for (Object o : collection) {
                                currentSession().saveOrUpdate(o);
                            }
                            log.debug("After Saving Object");
                        } catch (Exception ex) {
                            log.debug("Setting Rollback Only");
                            ts.setRollbackOnly();
                        }
                        return null;
                    }
                }
        );
    }

    private boolean nullOrEmpty(String aString) {
        return aString == null || "".equals(aString.trim());
    }


    /**
     * Search the Permissions.
     *
     * @param aPermissionCriteria The criteria to use to search the permissions.
     * @return A Pagination of permission records.
     */
    public Pagination searchPermissions(final PermissionCriteria aPermissionCriteria) {
        return (Pagination) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {

                        CriteriaBuilder cb = session.getCriteriaBuilder();
                        CriteriaQuery<Permission> query = cb.createQuery(Permission.class);
                        CriteriaQuery<Long> cntQuery = cb.createQuery(Long.class);
                        Root<Permission> root = query.from(Permission.class);
                        Root<Permission> cntRoot = cntQuery.from(Permission.class);
                        query.select(root);
                        cntQuery.select(cb.count(cntRoot));

                        Predicate siteNamePredicate = cb.and(); // Set is to true by default
                        Predicate cntSiteNamePredicate = cb.and();
                        if (!nullOrEmpty(aPermissionCriteria.getSiteName())) {
                            Join<Permission, Site> siteJoin = root.join("site");
                            Join<Permission, Site> cntSiteJoin = cntRoot.join("site");
                            siteNamePredicate = cb.like(siteJoin.get("title"), aPermissionCriteria.getSiteName() + "%");
                            cntSiteNamePredicate = cb.like(cntSiteJoin.get("title"), aPermissionCriteria.getSiteName() + "%");
                        }

                        Predicate urlsPredicate = cb.and();
                        Predicate cntUrlsPredicate = cb.and();
                        if (!nullOrEmpty(aPermissionCriteria.getUrlPattern())) {
                            Join<Permission, UrlPattern> urlPatternJoin = root.join("urls");
                            Join<Permission, UrlPattern> cntUrlPatternJoin = cntRoot.join("urls");
                            urlsPredicate = cb.like(urlPatternJoin.get("pattern"), aPermissionCriteria.getUrlPattern() + "%");
                            cntUrlsPredicate = cb.like(cntUrlPatternJoin.get("pattern"), aPermissionCriteria.getUrlPattern() + "%");
                        }

                        Predicate agencyOidPredicate = cb.and();
                        Predicate cntAgencyOidPredicate = cb.and();
                        if (aPermissionCriteria.getAgencyOid() != null) {
                            Join<Permission, Agency> agencyJoin = root.join("owningAgency");
                            Join<Permission, Agency> cntAgencyJoin = cntRoot.join("owningAgency");
                            agencyOidPredicate = cb.equal(agencyJoin.get("oid"), aPermissionCriteria.getAgencyOid());
                            cntAgencyOidPredicate = cb.equal(cntAgencyJoin.get("oid"), aPermissionCriteria.getAgencyOid());
                        }

                        Predicate whereClause = cb.and(siteNamePredicate, urlsPredicate, agencyOidPredicate);
                        Predicate cntWhereClause = cb.and(cntSiteNamePredicate, cntUrlsPredicate, cntAgencyOidPredicate);
                        query.where(whereClause);
                        cntQuery.where(cntWhereClause);

                        return new Pagination(session.createQuery(cntQuery), session.createQuery(query), aPermissionCriteria.getPageNumber(), Constants.GBL_PAGE_SIZE);
                    }
                }
        );
    }


    /**
     * Delete a pending target.
     *
     * @param aTarget The Target to be deleted.
     */
    public void delete(final Target aTarget) {
        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {
                            log.debug("Before Deleting Object");
                            currentSession().delete(aTarget);
                            log.debug("Object deleted successfully");
                        } catch (Exception ex) {
                            log.error("Setting Rollback Only");
                            ts.setRollbackOnly();
                        }
                        return null;
                    }
                }
        );
    }

    /**
     * Delete a schedule
     * @param schedule
     */
    public void delete(final Schedule schedule){
        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {
                            log.debug("Before Deleting Object");
                            currentSession().delete(schedule);
                            log.debug("Object deleted successfully");
                        } catch (Exception ex) {
                            log.error("Setting Rollback Only");
                            ts.setRollbackOnly();
                        }
                        return null;
                    }
                }
        );
    }

    /**
     * Delete a TargetGroup as long as it has no Target Instances associated
     * with it.
     *
     * @param aTargetGroup The target group to delete.
     * @return true if deleted; otherwise false.
     */
    public boolean deleteGroup(final TargetGroup aTargetGroup) {
        return (Boolean) txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {
                            log.debug("Before Deleting Object");

                            // Step one - check that the target group has
                            // no target instances.
                            CriteriaBuilder cb = currentSession().getCriteriaBuilder();
                            CriteriaQuery<Long> query = cb.createQuery(Long.class);
                            Root<TargetInstance> root = query.from(TargetInstance.class);
                            Join<TargetInstance, Target> targetJoin = root.join("target");
                            Predicate whereClause = cb.equal(targetJoin.get("oid"), aTargetGroup.getOid());
                            query.select(cb.count(root));
                            query.where(whereClause);

                            Long count = currentSession().createQuery(query).uniqueResult();

                            // If there are instances, we can't delete the object.
                            if (count > 0L) {
                                log.debug("Delete failed due to target instances existing");
                                return false;
                            }

                            // There are no instances, so delete away.
                            else {
                                // Delete all links to parents and children.
                                currentSession()
                                        .createQuery("delete from GroupMember g where g.child.oid = :groupOid or g.parent.oid = :groupOid")
                                        .setParameter("groupOid", aTargetGroup.getOid())
                                        .executeUpdate();

                                // Finally delete the group.
                                currentSession().delete(aTargetGroup);

                                log.debug("Delete Successful");

                                return true;
                            }
                        } catch (Exception ex) {
                            log.error("Setting Rollback Only", ex);
                            ts.setRollbackOnly();
                            return false;
                        }

                    }
                }
        );
    }


    /**
     * Get schedules to re-run
     */
    public List<Schedule> getSchedulesToRun() {

        return (List<Schedule>) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) {

                        final Calendar cal = Calendar.getInstance();

                        cal.setTime(new Date());
                        cal.add(Calendar.DAY_OF_MONTH, EnvironmentFactory.getEnv().getDaysToSchedule());
                        cal.set(Calendar.HOUR_OF_DAY, 23);
                        cal.set(Calendar.MINUTE, 59);
                        cal.set(Calendar.SECOND, 59);
                        cal.set(Calendar.MILLISECOND, 999);

                        CriteriaBuilder cb = session.getCriteriaBuilder();
                        CriteriaQuery<Schedule> query = cb.createQuery(Schedule.class);
                        Root<Schedule> root = query.from(Schedule.class);
                        query.select(root);

                        Predicate whereClause = cb.lessThanOrEqualTo(root.get("nextScheduleAfterPeriod"), cal.getTime());

                        query.where(whereClause);
                        List<Schedule> schedules = session.createQuery(query).list();

                        for (Schedule s : schedules) {
                            if (s.getTarget() == null) {
                                System.out.println("Schedule has null target so skipping initialisation: " + s.getOid());
                                log.debug("Schedule has null target so skipping initialisation: " + s.getOid());
                            } else {
                                log.debug("Initialising target and children for schedule: " + s.getOid());
                                initTargetAndChildrenInSession(s.getTarget(), session);
                            }
                        }

                        return schedules;
                    }
                }
        );
    }

    private void initTargetAndChildrenInSession(AbstractTarget aTarget, Session session) {
        log.debug("Initialising target and children for abstract target: " + aTarget.getOid());
        if (aTarget.getObjectType() == AbstractTarget.TYPE_GROUP) {
            TargetGroup group = loadGroup(aTarget.getOid(), true);
            if (group.getSipType() == TargetGroup.MANY_SIP) {
                log.debug("Initialising a target group.");
                Hibernate.initialize(group);
                Hibernate.initialize(group.getChildren());
                for (GroupMember gm : group.getChildren()) {
                    AbstractTarget childTarget = (AbstractTarget) session.load(AbstractTarget.class, gm.getChild().getOid());
                    initTargetAndChildrenInSession(childTarget, session);
                }
            }
        } else {
            log.debug("Initialising a target.");
            Hibernate.initialize(aTarget);
        }
    }
}
