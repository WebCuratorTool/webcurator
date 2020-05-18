package org.webcurator.domain;

import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.webcurator.domain.model.core.HarvestResourceDTO;
import org.webcurator.domain.model.visualization.VisualizationImportedFile;

import javax.xml.crypto.Data;
import java.util.List;

@Transactional
public class VisualizationImportedFileDAOImpl extends HibernateDaoSupport implements VisualizationImportedFileDAO {
    private static final Logger log = LoggerFactory.getLogger(VisualizationImportedFileDAOImpl.class);

    @Override
    public void save(Object obj) {
        try {
            HibernateTemplate hibernateTemplate = getHibernateTemplate();
            if (hibernateTemplate == null) {
                log.error("Failed to get a hibernate template");
                return;
            }
            hibernateTemplate.execute(
                    session -> {
                        log.debug("Before Saving Object");
                        session.saveOrUpdate(obj);
                        log.debug("After Saving Object");
                        return null;
                    }
            );
        } catch (DataAccessException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public VisualizationImportedFile findImportedFile(String fileName) {
//        HibernateTemplate hibernateTemplate = getHibernateTemplate();
//        if (hibernateTemplate == null) {
//            log.error("Failed to get a hibernate template");
//            return null;
//        }
//        hibernateTemplate.execute(
//                session -> {
//                    Query q = session.createQuery("select new org.webcurator.domain.model.visualization.VisualizationImportedFile(vif.result.targetInstance.oid, ahr.result.harvestNumber, ahr.oid, ahr.name, ahr.length, ahr.resourceOffset, ahr.resourceLength, ahr.arcFileName, ahr.statusCode, ahr.compressed) from org.webcurator.domain.model.core.ArcHarvestResource ahr where ahr.result.oid=?1");
//                    q.setParameter(1, harvestResultOid);
//                    List<HarvestResourceDTO> resources = q.list();
//
//                    return resources;
//                }
//        );
        return null;
    }

    @Override
    public void deleteImportedFile(VisualizationImportedFile importedFile) {
        try {
            HibernateTemplate hibernateTemplate = getHibernateTemplate();
            if (hibernateTemplate == null) {
                log.error("Failed to get a hibernate template");
                return;
            }
            hibernateTemplate.execute(
                    session -> {
                        log.debug("Before Deleting Object");
                        session.delete(importedFile);
                        log.debug("After Deleting Object");
                        return null;
                    }
            );
        } catch (DataAccessException e) {
            log.error(e.getMessage());
        }
    }
}
