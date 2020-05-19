package org.webcurator.domain;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.webcurator.domain.model.visualization.VisualizationImportedFile;

import java.util.List;
//extends HibernateDaoSupport
//@Component("visualizationImportedFileDAOImpl")
@Transactional
public class VisualizationImportedFileDAOImpl implements VisualizationImportedFileDAO {
    private static final Logger log = LoggerFactory.getLogger(VisualizationImportedFileDAOImpl.class);

//    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void save(Object obj) {
        sessionFactory.getCurrentSession().saveOrUpdate(obj);

//        try {
//            HibernateTemplate hibernateTemplate = getHibernateTemplate();
//            if (hibernateTemplate == null) {
//                log.error("Failed to get a hibernate template");
//                return;
//            }
//            hibernateTemplate.execute(
//                    session -> {
//                        log.debug("Before Saving Object");
//                        session.saveOrUpdate(obj);
//                        log.debug("After Saving Object");
//                        return null;
//                    }
//            );
//        } catch (DataAccessException e) {
//            log.error(e.getMessage());
//        }
    }

    @Override
    public VisualizationImportedFile findImportedFile(String fileName) {
        Query<VisualizationImportedFile> q = sessionFactory.getCurrentSession().createNamedQuery(VisualizationImportedFile.QRY_VISUALIZATION_IMPORTED_FILE_BY_NAME, VisualizationImportedFile.class);
        q.setParameter(1, fileName);
        List<VisualizationImportedFile> resources = q.list();
        if (resources.size() == 1) {
            return resources.get(0);
        } else {
            return null;
        }

//        HibernateTemplate hibernateTemplate = getHibernateTemplate();
//        if (hibernateTemplate == null) {
//            log.error("Failed to get a hibernate template");
//            return null;
//        }
//        hibernateTemplate.execute(
//                session -> {
//                    Query<VisualizationImportedFile> q = session.createNamedQuery(VisualizationImportedFile.QRY_VISUALIZATION_IMPORTED_FILE_BY_NAME, VisualizationImportedFile.class);
//                    q.setParameter(1, fileName);
//                    List<VisualizationImportedFile> resources = q.list();
//                    if (resources.size() == 1) {
//                        return resources.get(0);
//                    } else {
//                        return null;
//                    }
//                }
//        );
//        return null;
    }

    @Override
    public void deleteImportedFile(VisualizationImportedFile importedFile) {
        sessionFactory.getCurrentSession().delete(importedFile);
//        try {
//            HibernateTemplate hibernateTemplate = getHibernateTemplate();
//            if (hibernateTemplate == null) {
//                log.error("Failed to get a hibernate template");
//                return;
//            }
//            hibernateTemplate.execute(
//                    session -> {
//                        log.debug("Before Deleting Object");
//                        session.delete(importedFile);
//                        log.debug("After Deleting Object");
//                        return null;
//                    }
//            );
//        } catch (DataAccessException e) {
//            log.error(e.getMessage());
//        }
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
