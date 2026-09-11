package org.webcurator.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.webcurator.domain.model.core.HeatmapConfig;

import jakarta.transaction.Transactional;

@Transactional
public class HeatmapDAO {

	private Log log = LogFactory.getLog(HeatmapDAO.class);
	private TransactionTemplate txTemplate = null;

	private SessionFactory sessionFactory;

	public Map<String, HeatmapConfig> getHeatmapConfigurations() {
		Map<String, HeatmapConfig> result = new HashMap<>();
		List<HeatmapConfig> configurations = currentSession().createNamedQuery(HeatmapConfig.QUERY_ALL, HeatmapConfig.class)
					.list();
		for (HeatmapConfig config : configurations) {
			result.put(config.getName(), config);
		}
		return result;
	}

	public void saveOrUpdate(final HeatmapConfig config) {
		if (log.isDebugEnabled()) {
			log.debug("Saving " + config.getClass().getName());
		}
		txTemplate.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus ts) {
				try {
					currentSession().saveOrUpdate(config);
				} catch (Exception ex) {
					ts.setRollbackOnly();
				}
				return null;
			}
		});

	}

	/**
	 * @param txTemplate
	 *            The txTemplate to set.
	 */
	public void setTxTemplate(TransactionTemplate txTemplate) {
		this.txTemplate = txTemplate;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	private Session currentSession() {
		return sessionFactory.getCurrentSession();
	}

	public HeatmapConfig getConfigByOid(final Long oid) {
		Query<HeatmapConfig> query = currentSession().createNamedQuery(HeatmapConfig.QRY_GET_CONFIG_BY_OID, HeatmapConfig.class);
		query.setParameter(1, oid, Long.class);
		return query.uniqueResult();
	}

}
