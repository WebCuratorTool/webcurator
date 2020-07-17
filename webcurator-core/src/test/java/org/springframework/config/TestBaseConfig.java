package org.springframework.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.webcurator.core.common.Environment;
import org.webcurator.core.common.EnvironmentFactory;
import org.webcurator.core.common.EnvironmentImpl;
import org.webcurator.core.harvester.agent.HarvestAgentFactory;
import org.webcurator.core.harvester.agent.HarvestAgentFactoryImpl;
import org.webcurator.core.harvester.coordinator.*;
import org.webcurator.core.util.ApplicationContextFactory;
import org.webcurator.domain.TargetDAO;
import org.webcurator.domain.TargetDAOImpl;
import org.webcurator.domain.TargetInstanceDAO;
import org.webcurator.domain.TargetInstanceDAOImpl;

@TestConfiguration
public class TestBaseConfig {
    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public Environment environmentWCT() {
        EnvironmentImpl bean = new EnvironmentImpl();
        bean.setDaysToSchedule(1);
        bean.setSchedulesPerBatch(1000);
        bean.setApplicationVersion("Test");
        bean.setHeritrixVersion("Heritrix 3.1.4");

        //Init environment
        EnvironmentFactory.setEnvironment(bean);
        ApplicationContextFactory.setApplicationContext(applicationContext);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public HarvestAgentFactory harvestAgentFactory() {
        HarvestAgentFactoryImpl bean = new HarvestAgentFactoryImpl();
        return bean;
    }

    @Bean
    public HarvestAgentManager harvestAgentManager() {
        HarvestAgentManagerImpl bean = new HarvestAgentManagerImpl();
        bean.setHarvestAgentFactory(harvestAgentFactory());
        return bean;
    }

    @Bean
    public TargetInstanceDAO targetInstanceDao() {
        TargetInstanceDAOImpl bean = new TargetInstanceDAOImpl();
//        bean.setSessionFactory(sessionFactory().getObject());
//        bean.setTxTemplate(transactionTemplate());
//        bean.setAuditor(audit());

        return bean;
    }

    @Bean
    public HarvestBandwidthManagerImpl harvestBandwidthManager() {
        HarvestBandwidthManagerImpl bean = new HarvestBandwidthManagerImpl();
        bean.setHarvestAgentManager(harvestAgentManager());
        bean.setTargetInstanceDao(targetInstanceDao());
//        bean.setHarvestCoordinatorDao(harvestCoordinatorDao());
//        bean.setMinimumBandwidth(minimumBandwidth);
//        bean.setMaxBandwidthPercent(maxBandwidthPercent);
//        bean.setAuditor(audit());

        return bean;
    }

    @Bean
    public HarvestLogManager harvestLogManager() {
        HarvestLogManagerImpl bean = new HarvestLogManagerImpl();
        bean.setHarvestAgentManager(harvestAgentManager());
//        bean.setDigitalAssetStoreFactory(digitalAssetStoreFactory());

        return bean;
    }
}
