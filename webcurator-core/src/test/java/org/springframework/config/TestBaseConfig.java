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
import org.webcurator.core.harvester.agent.HarvestAgentFactory;
import org.webcurator.core.harvester.agent.HarvestAgentFactoryImpl;
import org.webcurator.core.harvester.coordinator.*;
import org.webcurator.core.util.ApplicationContextFactory;
import org.webcurator.domain.TargetInstanceDAO;

@TestConfiguration
public class TestBaseConfig {
    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public Environment environmentWCT() {
        Environment bean = new Environment();
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
        HarvestAgentManager bean = new HarvestAgentManager();
        bean.setHarvestAgentFactory(harvestAgentFactory());
        return bean;
    }

    @Bean
    public TargetInstanceDAO targetInstanceDao() {
        TargetInstanceDAO bean = new TargetInstanceDAO();
//        bean.setSessionFactory(sessionFactory().getObject());
//        bean.setTxTemplate(transactionTemplate());
//        bean.setAuditor(audit());

        return bean;
    }


    @Bean
    public HarvestLogManager harvestLogManager() {
        HarvestLogManager bean = new HarvestLogManager();
        bean.setHarvestAgentManagerImpl(harvestAgentManager());
//        bean.setDigitalAssetStoreFactory(digitalAssetStoreFactory());

        return bean;
    }
}
