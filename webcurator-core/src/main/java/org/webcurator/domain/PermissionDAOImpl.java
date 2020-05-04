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

import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;
import org.webcurator.domain.model.core.Permission;

import javax.persistence.Query;
import java.util.List;

@Transactional
public class PermissionDAOImpl extends HibernateDaoSupport implements PermissionDAO {
    @Override
    public Permission load(long permissionOid) {
        return (Permission) getHibernateTemplate().load(Permission.class, permissionOid);
    }

    @Override
    public List<Permission> loadBySiteId(final long siteId){
        Query query=currentSession().createNamedQuery(Permission.QUERY_BY_SITE_ID);
        query.setParameter("siteId",siteId);
        return query.getResultList();
    }
}
