package org.webcurator.domain;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.webcurator.domain.model.visualization.VisualizationImportedFile;

import java.util.List;

@Transactional
public class VisualizationImportedFileDAOImpl implements VisualizationImportedFileDAO {
    private static final Logger log = LoggerFactory.getLogger(VisualizationImportedFileDAOImpl.class);

    private SessionFactory sessionFactory;

    @Override
    public void save(Object obj) {
        sessionFactory.getCurrentSession().saveOrUpdate(obj);
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
    }

    @Override
    public void deleteImportedFile(VisualizationImportedFile importedFile) {
        sessionFactory.getCurrentSession().delete(importedFile);
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
