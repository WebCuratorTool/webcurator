package org.webcurator.webapp.beans.config;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.webcurator.auth.AuthorityManagerImpl;
import org.webcurator.common.util.DateUtils;
import org.webcurator.core.admin.PermissionTemplateManagerImpl;
import org.webcurator.core.agency.AgencyUserManagerImpl;
import org.webcurator.core.archive.ArchiveAdapterImpl;
import org.webcurator.core.archive.SipBuilder;
import org.webcurator.core.check.BandwidthChecker;
import org.webcurator.core.check.CheckProcessor;
import org.webcurator.core.check.Checker;
import org.webcurator.core.check.CoreCheckNotifier;
import org.webcurator.core.common.Environment;
import org.webcurator.core.common.EnvironmentFactory;
import org.webcurator.core.common.EnvironmentImpl;
import org.webcurator.core.common.TreeToolControllerAttribute;
import org.webcurator.core.harvester.agent.HarvestAgentFactoryImpl;
import org.webcurator.core.harvester.coordinator.*;
import org.webcurator.core.notification.InTrayManagerImpl;
import org.webcurator.core.notification.MailServerImpl;
import org.webcurator.core.permissionmapping.HierPermMappingDAOImpl;
import org.webcurator.core.permissionmapping.HierarchicalPermissionMappingStrategy;
import org.webcurator.core.permissionmapping.PermMappingSiteListener;
import org.webcurator.core.permissionmapping.PermissionMappingStrategy;
import org.webcurator.core.profiles.PolitenessOptions;
import org.webcurator.core.profiles.ProfileManager;
import org.webcurator.core.reader.LogReader;
import org.webcurator.core.reader.LogReaderClient;
import org.webcurator.core.reader.LogReaderImpl;
import org.webcurator.core.report.LogonDurationDAOImpl;
import org.webcurator.core.rules.QaRecommendationServiceImpl;
import org.webcurator.core.scheduler.ScheduleJob;
import org.webcurator.core.scheduler.TargetInstanceManagerImpl;
import org.webcurator.core.sites.SiteManagerImpl;
import org.webcurator.core.sites.SiteManagerListener;
import org.webcurator.core.store.DigitalAssetStoreClient;
import org.webcurator.core.store.DigitalAssetStoreFactoryImpl;
import org.webcurator.core.store.tools.QualityReviewFacade;
import org.webcurator.core.targets.TargetManagerImpl;
import org.webcurator.core.util.ApplicationContextFactory;
import org.webcurator.core.util.AuditDAOUtil;
import org.webcurator.core.util.LockManager;
import org.webcurator.domain.*;
import org.webcurator.domain.model.core.BusinessObjectFactory;
import org.webcurator.domain.model.core.SchedulePattern;
import org.webcurator.ui.tools.controller.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;

/**
 * Contains configuration that used to be found in {@code wct-core.xml}. This
 * is part of the change to move to using annotations for Spring instead of
 * XML files.
 */
@SuppressWarnings("all")
@Configuration
@EnableTransactionManagement
public class BaseConfig {
    private static Logger LOGGER = LoggerFactory.getLogger(BaseConfig.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private  RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${spring.datasource.driver-class-name}")
    private String datasourceDriverClassName;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${hibernate.dialect}")
    private String hibernateDialect;

    @Value("${hibernate.show_sql}")
    private String hibernateShowSql;

    @Value("${hibernate.default_schema}")
    private String hibernateDefaultSchema;

    @Value("${digitalAssetStore.baseUrl}")
    private String digitalAssetStoreBaseUrl;

    @Value("${harvestCoordinator.minimumBandwidth}")
    private int minimumBandwidth;

    @Value("${harvestCoordinator.maxBandwidthPercent}")
    private int maxBandwidthPercent;

    @Value("${harvestCoordinator.autoQAUrl}")
    private String autoQAUrl;

    @Value("${queueController.enableQaModule}")
    private boolean enableQaModule;

    @Value("${queueController.autoPrunedNote}")
    private String autoPrunedNote;

    @Value("${targetInstanceManager.storeSeedHistory}")
    private boolean storeSeedHistory;

    @Value("${targetManager.allowMultiplePrimarySeeds}")
    private boolean allowMultiplePrimarySeeds;

    @Value("${groupTypes.subgroup}")
    private String groupTypesSubgroup;

    @Value("${harvestAgentFactory.daysToSchedule}")
    private int harvestAgentDaysToSchedule;

    @Value("${createNewTargetInstancesTrigger.schedulesPerBatch}")
    private int targetInstancesTriggerSchedulesPerBatch;

    @Value("${project.version}")
    private String projectVersion;

    @Value("${heritrix.version}")
    private String heritrixVersion;

    @Value("${processScheduleTrigger.startDelay}")
    private long processScheduleTriggerStartDelay;

    @Value("${processScheduleTrigger.repeatInterval}")
    private long processScheduleTriggerRepeatInterval;

    @Value("${bandwidthCheckTrigger.startDelay}")
    private long bandwidthCheckTriggerStartDelay;

    @Value("${bandwidthCheckTrigger.repeatInterval}")
    private long bandwidthCheckTriggerRepeatInterval;

    @Value("${mail.protocol}")
    private String mailProtocol;

    @Value("${mailServer.smtp.host}")
    private String mailServerSmtpHost;

    @Value("${mail.smtp.port}")
    private String mailSmtpPort;

    @Value("${bandwidthChecker.warnThreshold}")
    private long bandwidthCheckerWarnThreshold;

    @Value("${bandwidthChecker.errorThreshold}")
    private long bandwidthCheckerErrorThreshold;

    @Value("${checkProcessorTrigger.startDelay}")
    private long checkProcessorTriggerStartDelay;

    @Value("${checkProcessorTrigger.repeatInterval}")
    private long checkProcessorTriggerRepeatInterval;

    @Value("${purgeDigitalAssetsTrigger.repeatInterval}")
    private long purgeDigitalAssetsTriggerRepeatInterval;

    @Value("${purgeAbortedTargetInstancesTrigger.repeatInterval}")
    private long purgeAbortedTargetInstancesTriggerRepeatInterval;

    @Value("${inTrayManager.sender}")
    private String inTrayManagerSender;

    @Value("${inTrayManager.wctBaseUrl}")
    private String inTrayManagerWctBaseUrl;

    @Value("${groupExpiryJobTrigger.startDelay}")
    private long groupExpiryJobTriggerStartDelay;

    @Value("${groupExpiryJobTrigger.repeatInterval}")
    private long groupExpiryJobTriggerRepeatInterval;

    @Value("${createNewTargetInstancesTrigger.startDelay}")
    private long createNewTargetInstancesTriggerStartDelay;

    @Value("${createNewTargetInstancesTrigger.repeatInterval}")
    private long createNewTargetInstancesTriggerRepeatInterval;

    @Value("${archiveAdapter.targetReferenceMandatory}")
    private boolean archiveAdapterTargetReferenceMandatory;

    @Value("${groupSearchController.defaultSearchOnAgencyOnly}")
    private boolean groupSearchControllerDefaultSearchOnAgencyOnly;

    @Value("${groupTypes.subgroupSeparator}")
    private String groupTypesSubgroupSeparator;

    @Value("${harvestResourceUrlMapper.urlMap}")
    private String harvestResourceUrlMapperUrlMap;

    @Value("${qualityReviewToolController.enableAccessTool}")
    private boolean qualityReviewToolControllerEnableAccessTool;

    @Value("${digitalAssetStoreServer.uploadedFilesDir}")
    private String digitalAssetStoreServerUploadedFilesDir;

    @Value("${harvestCoordinator.autoQAUrl}")
    private String harvestCoordinatorAutoQAUrl;

    @Value("${qualityReviewToolController.archiveUrl}")
    private String qualityReviewToolControllerArchiveUrl;

    @Value("${qualityReviewToolController.archiveName}")
    private String qualityReviewToolControllerArchiveName;

    @Value("${qualityReviewToolController.archive.alternative}")
    private String qualityReviewToolControllerArchiveUrlAlternative;

    @Value("${qualityReviewToolController.archive.alternative.name}")
    private String qualityReviewToolControllerArchiveAlternativeName;

    @Value("${qualityReviewToolController.enableBrowseTool}")
    private boolean qualityReviewToolControllerEnableBrowseTool;

    @Value("${qualityReviewToolController.webArchiveTarget}")
    private String qualityReviewToolControllerWebArchiveTarget;

    @Value("${crawlPoliteness.polite.delayFactor}")
    private double crawlPolitenessPoliteDelayFactor;

    @Value("${crawlPoliteness.polite.minDelayMs}")
    private long crawlPolitenessPoliteMinDelayMs;

    @Value("${crawlPoliteness.polite.MaxDelayMs}")
    private long crawlPolitenessPoliteMaxDelayMs;

    @Value("${crawlPoliteness.polite.respectCrawlDelayUpToSeconds}")
    private long crawlPolitenessPoliteRespectCrawlDelay;

    @Value("${crawlPoliteness.polite.maxPerHostBandwidthUsageKbSec}")
    private long crawlPolitenessPoliteMaxPerHostBandwidth;

    @Value("${crawlPoliteness.medium.delayFactor}")
    private double crawlPolitenessMediumDelayFactor;

    @Value("${crawlPoliteness.medium.minDelayMs}")
    private long crawlPolitenessMediumMinDelayMs;

    @Value("${crawlPoliteness.medium.MaxDelayMs}")
    private long crawlPolitenessMediumMaxDelayMs;

    @Value("${crawlPoliteness.medium.respectCrawlDelayUpToSeconds}")
    private long crawlPolitenessMediumRespectCrawlDelay;

    @Value("${crawlPoliteness.medium.maxPerHostBandwidthUsageKbSec}")
    private long crawlPolitenessMediumMaxPerHostBandwidth;

    @Value("${crawlPoliteness.aggressive.delayFactor}")
    private double crawlPolitenessAggressiveDelayFactor;

    @Value("${crawlPoliteness.aggressive.minDelayMs}")
    private long crawlPolitenessAggressiveMinDelayMs;

    @Value("${crawlPoliteness.aggressive.MaxDelayMs}")
    private long crawlPolitenessAggressiveMaxDelayMs;

    @Value("${crawlPoliteness.aggressive.respectCrawlDelayUpToSeconds}")
    private long crawlPolitenessAggressiveRespectCrawlDelay;

    @Value("${crawlPoliteness.aggressive.maxPerHostBandwidthUsageKbSec}")
    private long crawlPolitenessAggressiveMaxPerHostBandwidth;

    @Autowired
    private ListsConfig listsConfig;

    @Autowired
    private HarvestCoordinator harvestCoordinator;

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource bean = new ResourceBundleMessageSource();
        bean.setBasename("messages");

        return bean;
    }

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .driverClassName(datasourceDriverClassName)
                .url(datasourceUrl)
                .username(datasourceUsername)
                .password(datasourcePassword)
                .build();
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean bean = new LocalSessionFactoryBean();
        bean.setDataSource(dataSource());
        // TODO NOTE it would be better if this was a wildcard
//        Resource jarResource = new ClassPathResource("org/webcurator/**");
//        Resource jarResource = new FileSystemResource("/WEB-INF/lib/webcurator-core-3.0.0-SNAPSHOT.jar");

        //Resource jarResource = new ClassPathResource("classpath:/WEB-INF/lib/webcurator-core-3.0.0-SNAPSHOT.jar");
        //bean.setMappingJarLocations(jarResource);

        //bean.setMappingJarLocations(getHibernateConfigurationResources());
//        bean.setPackagesToScan(new String[]{"org.webcurator.domain.model","org.webcurator.domain"});
        bean.setPackagesToScan("org.webcurator.domain.model");

        Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.dialect", hibernateDialect);
        hibernateProperties.setProperty("hibernate.show_sql", hibernateShowSql);
        // Include setting of default schema, unless the database type is mysql
        if(!hibernateDialect.toLowerCase().contains("mysql")){
            hibernateProperties.setProperty("hibernate.default_schema", hibernateDefaultSchema);
        }
        hibernateProperties.setProperty("hibernate.transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory");
        hibernateProperties.setProperty("hibernate.enable_lazy_load_no_trans","true");

        bean.setHibernateProperties(hibernateProperties);

        return bean;
    }

    public Resource[] getHibernateConfigurationResources() {
        Resource[] resources = null;
        try {
            resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                    .getResources("classpath:/org/webcurator/**/*.hbm.xml");
        } catch (IOException e) {
            LOGGER.error("Unable to load hibernate classpath resources: " + e, e);
        }
        return resources;
    }

    @Bean
    @Autowired
    public HibernateTransactionManager transactionManager() {
        HibernateTransactionManager hibernateTransactionManager=new HibernateTransactionManager(sessionFactory().getObject());
//        hibernateTransactionManager.setTransactionSynchronization(AbstractPlatformTransactionManager.SYNCHRONIZATION_ALWAYS);
        return hibernateTransactionManager;
    }

//    @Bean
//    @Autowired
//    public HibernateTransactionManager transactionManager(final SessionFactory sessionFactory) {
//        final HibernateTransactionManager txManager = new HibernateTransactionManager();
//        txManager.setSessionFactory(sessionFactory);
//        return txManager;
//    }

    @Bean
    public TransactionTemplate transactionTemplate() {
        return new TransactionTemplate(transactionManager());
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public LogReader logReader(){
        LogReader bean=new LogReaderImpl();
        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public DigitalAssetStoreClient digitalAssetStore() {
        DigitalAssetStoreClient bean = new DigitalAssetStoreClient(digitalAssetStoreBaseUrl, restTemplateBuilder);
        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public DigitalAssetStoreFactoryImpl digitalAssetStoreFactory() {
        DigitalAssetStoreFactoryImpl bean = new DigitalAssetStoreFactoryImpl();
        bean.setDAS(digitalAssetStore());
        bean.setLogReader(new LogReaderClient(digitalAssetStoreBaseUrl, restTemplateBuilder));
        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public QaRecommendationServiceImpl qaRecommendationService() {
        QaRecommendationServiceImpl bean = new QaRecommendationServiceImpl();
        // The state that will be used to denote a failure within the Rules Engine (eg: an unexpected exception).
        // This state will be returned to the user as the state of the failed indicator along with the exception.
        bean.setStateFailed("Failed");

        // The advice priority is the QA recommendation in rank order, the value of each Map entry being the rank.
        Map<String, Integer> advicePriorityMap = new HashMap<>();
        advicePriorityMap.put("None", 0);
        advicePriorityMap.put("Running", 1);
        advicePriorityMap.put("Archive", 2);
        advicePriorityMap.put("Investigate", 3);
        // Delist has highest priority for a valid indicator since we know that nothing has changed (precluding any other advice).
        advicePriorityMap.put("Delist", 4);
        advicePriorityMap.put("Reject", 5);
        // Failed has the highest priority overall since any failures are unexpected.
        advicePriorityMap.put("Failed", 6);
        bean.setAdvicePriority(advicePriorityMap);

        // Globals objects used by the rules engine.
        Map<String, String> globalsMap = new HashMap<>();
        globalsMap.put("MSG_WITHIN_TOLERANCE", "The {0} indicator value of {1} is within {2}% and {3}% of reference crawl tolerance ({4} &lt;= {5} &lt;= {6})");
        globalsMap.put("MSG_OUTSIDE_TOLERANCE", "The {0} indicator value of {1} is outside {2}% and {3}% of reference crawl tolerance ({5} &lt; {4} or {5} &gt; {6})");
        globalsMap.put("MSG_EXCEEDED_UPPER_LIMIT", "The {0} indicator value of {1} has exceeded its upper limit of {2}");
        globalsMap.put("MSG_FALLEN_BELOW_LOWER_LIMIT", "The {0} indicator value of {1} has fallen below its lower limit of {2}");
        // Advice that will be returned on an indicator.
        globalsMap.put("REJECT", "Reject");
        globalsMap.put("INVESTIGATE", "Investigate");
        globalsMap.put("ARCHIVE", "Archive");
        bean.setGlobals(globalsMap);

        bean.setRulesFileName("rules.drl");
        bean.setQualityReviewFacade(qualityReviewFacade());
        //bean.setHarvestCoordinator(harvestCoordinator());
        bean.setTargetInstanceManager(targetInstanceManager());

        return bean;
    }

    @Bean
    public QualityReviewFacade qualityReviewFacade() {
        QualityReviewFacade bean = new QualityReviewFacade();
        bean.setDigialAssetStore(digitalAssetStore());
        bean.setTargetInstanceDao(targetInstanceDao());
        bean.setAuditor(audit());

        return bean;
    }

    @Bean
    public TargetInstanceDAOImpl targetInstanceDao() {
        TargetInstanceDAOImpl bean = new TargetInstanceDAOImpl();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());
        bean.setAuditor(audit());

        return bean;
    }

    @Bean
    public UserRoleDAO userRoleDAO() {
        UserRoleDAOImpl bean = new UserRoleDAOImpl();
        bean.setSessionFactory(sessionFactory().getObject());
        //bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public RejReasonDAO rejReasonDAO() {
        RejReasonDAOImpl bean = new RejReasonDAOImpl();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public IndicatorDAO indicatorDAO() {
        IndicatorDAOImpl bean = new IndicatorDAOImpl();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public IndicatorCriteriaDAO indicatorCriteriaDAO() {
        IndicatorCriteriaDAOImpl bean = new IndicatorCriteriaDAOImpl();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public IndicatorReportLineDAO indicatorReportLineDAO() {
        IndicatorReportLineDAOImpl bean = new IndicatorReportLineDAOImpl();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public FlagDAO flagDAO() {
        FlagDAOImpl bean = new FlagDAOImpl();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public TargetDAO targetDao() {
        TargetDAOImpl bean = new TargetDAOImpl();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public SiteDAO siteDao() {
        SiteDAOImpl bean = new SiteDAOImpl();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public PermissionDAO permissionDAO(){
        PermissionDAOImpl bean=new PermissionDAOImpl();
        bean.setSessionFactory(sessionFactory().getObject());
        return bean;
    }

    @Bean
    public ProfileDAO profileDao() {
        ProfileDAOImpl bean = new ProfileDAOImpl();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public InTrayDAO inTrayDao() {
        InTrayDAOImpl bean = new InTrayDAOImpl();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public HarvestCoordinatorDAOImpl harvestCoordinatorDao() {
        HarvestCoordinatorDAOImpl bean = new HarvestCoordinatorDAOImpl();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public HeatmapDAO heatmapConfigDao() {
        HeatmapDAOImpl bean = new HeatmapDAOImpl();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public PermissionTemplateDAO permissionTemplateDao() {
        PermissionTemplateDAOImpl bean = new PermissionTemplateDAOImpl();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public SipBuilder sipBuilder() {
        SipBuilder bean = new SipBuilder();
        bean.setTargetInstanceManager(targetInstanceManager());
        bean.setTargetManager(targetManager());

        return bean;
    }

    @Bean
    public HarvestAgentManagerImpl harvestAgentManager() {
        HarvestAgentManagerImpl bean = new HarvestAgentManagerImpl();
        bean.setHarvestAgentFactory(harvestAgentFactory());
        bean.setTargetInstanceManager(targetInstanceManager());
        bean.setTargetInstanceDao(targetInstanceDao());

        return bean;
    }

    @Bean
    public HarvestLogManagerImpl harvestLogManager() {
        HarvestLogManagerImpl bean = new HarvestLogManagerImpl();
        bean.setHarvestAgentManager(harvestAgentManager());
        bean.setDigitalAssetStoreFactory(digitalAssetStoreFactory());

        return bean;
    }

    @Bean
    public HarvestBandwidthManagerImpl harvestBandwidthManager() {
        HarvestBandwidthManagerImpl bean = new HarvestBandwidthManagerImpl();
        bean.setHarvestAgentManager(harvestAgentManager());
        bean.setTargetInstanceDao(targetInstanceDao());
        bean.setHarvestCoordinatorDao(harvestCoordinatorDao());
        bean.setMinimumBandwidth(minimumBandwidth);
        bean.setMaxBandwidthPercent(maxBandwidthPercent);
        bean.setAuditor(audit());

        return bean;
    }

    @Bean
    public HarvestQaManager harvestQaManager() {
        HarvestQaManagerImpl bean = new HarvestQaManagerImpl();
        bean.setTargetInstanceManager(targetInstanceManager());
        bean.setTargetInstanceDao(targetInstanceDao());
        bean.setAutoQAUrl(autoQAUrl);
        //bean.setQaRecommendationService(qaRecommendationService());
        bean.setQualityReviewFacade(qualityReviewFacade());
        bean.setEnableQaModule(enableQaModule);
        bean.setAutoPrunedNote(autoPrunedNote);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public TargetInstanceManagerImpl targetInstanceManager() {
        TargetInstanceManagerImpl bean = new TargetInstanceManagerImpl();
        bean.setTargetInstanceDao(targetInstanceDao());
        bean.setAuditor(audit());
        bean.setAnnotationDAO(annotationDao());
        bean.setIndicatorDAO(indicatorDAO());
        bean.setIndicatorCriteriaDAO(indicatorCriteriaDAO());
        bean.setIndicatorReportLineDAO(indicatorReportLineDAO());
        bean.setProfileDAO(profileDao());
        bean.setInTrayManager(inTrayManager());
        bean.setStoreSeedHistory(storeSeedHistory);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public LockManager lockManager() {
        return new LockManager();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public SiteManagerImpl siteManager() {
        SiteManagerImpl bean = new SiteManagerImpl();
        bean.setSiteDao(siteDao());
        bean.setAnnotationDAO(annotationDao());

        PermMappingSiteListener permMappingSiteListener = new PermMappingSiteListener();
        permMappingSiteListener.setStrategy(permissionMappingStrategy());
        List<SiteManagerListener> permMappingSiteListenerList= new ArrayList<>(
                Arrays.asList(permMappingSiteListener)
        );
        bean.setListeners(permMappingSiteListenerList);

        bean.setIntrayManager(inTrayManager());
        bean.setAuditor(audit());
        bean.setAgencyUserManager(agencyUserManager());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public TargetManagerImpl targetManager() {
        TargetManagerImpl bean = new TargetManagerImpl();
        bean.setTargetDao(targetDao());
        bean.setSiteDao(siteDao());
        bean.setAnnotationDAO(annotationDao());
        bean.setAuthMgr(authorityManager());
        bean.setTargetInstanceDao(targetInstanceDao());
        bean.setInstanceManager(targetInstanceManager());
        bean.setIntrayManager(inTrayManager());
        bean.setMessageSource(messageSource());
        bean.setAuditor(audit());
        bean.setBusinessObjectFactory(businessObjectFactory());
        bean.setAllowMultiplePrimarySeeds(allowMultiplePrimarySeeds);
        bean.setSubGroupParentTypesList(listsConfig.subGroupParentTypesList());
        bean.setSubGroupTypeName(groupTypesSubgroup);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public ProfileManager profileManager() {
        ProfileManager bean = new ProfileManager();
        bean.setProfileDao(profileDao());
        bean.setAuthorityManager(authorityManager());
        bean.setAuditor(audit());

        return bean;
    }

    @Bean
    public AuditDAOUtil audit() {
        AuditDAOUtil bean = new AuditDAOUtil();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public LogonDurationDAOImpl logonDuration() {
        LogonDurationDAOImpl bean = new LogonDurationDAOImpl();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public HarvestAgentFactoryImpl harvestAgentFactory() {
        return new HarvestAgentFactoryImpl();
    }

    @Bean
    public Environment environmentWCT() {
        EnvironmentImpl bean = new EnvironmentImpl();
        bean.setDaysToSchedule(harvestAgentDaysToSchedule);
        bean.setSchedulesPerBatch(targetInstancesTriggerSchedulesPerBatch);
        bean.setApplicationVersion(projectVersion);
        bean.setHeritrixVersion("Heritrix " + heritrixVersion);

        //Init environment
        EnvironmentFactory.setEnvironment(bean);
        ApplicationContextFactory.setApplicationContext(applicationContext);

        return bean;
    }

    @Bean
    public SpringSchedulePatternFactory schedulePatternFactory() {
        SpringSchedulePatternFactory bean = new SpringSchedulePatternFactory();

        SchedulePattern schedulePattern = new SchedulePattern();
        schedulePattern.setScheduleType(1);
        schedulePattern.setDescription("Every Monday at 9:00pm");
        schedulePattern.setCronPattern("00 00 21 ? * MON *");

        List<SchedulePattern> schedulePatternList = new ArrayList<>(Arrays.asList(schedulePattern));

        schedulePatternList.add(schedulePattern);

        bean.setPatterns(schedulePatternList);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public HierarchicalPermissionMappingStrategy permissionMappingStrategy() {
        HierarchicalPermissionMappingStrategy bean = new HierarchicalPermissionMappingStrategy();
        bean.setDao(permMappingDao());
        bean.setPermissionDAO(permissionDAO());
        PermissionMappingStrategy.setStrategy(bean);
        return bean;
    }

    @Bean
    public HierPermMappingDAOImpl permMappingDao() {
        HierPermMappingDAOImpl bean = new HierPermMappingDAOImpl();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public JobDetail processScheduleJob() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("harvestCoordinator", harvestCoordinator);

        JobDetail bean = JobBuilder.newJob(ScheduleJob.class)
                .withIdentity("ProcessSchedule", "ProcessScheduleGroup")
                .usingJobData(jobDataMap)
                .storeDurably(true)
                .build();

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public Trigger processScheduleTrigger() {
        // delay before running the job measured in milliseconds
        Date startTime = new Date(System.currentTimeMillis() + processScheduleTriggerStartDelay);
        Trigger bean = newTrigger()
                .withIdentity("ProcessScheduleTrigger", "ProcessScheduleTriggerGroup")
                .startAt(startTime)
                .withSchedule(simpleSchedule().repeatForever().withIntervalInMilliseconds(processScheduleTriggerRepeatInterval))
                .forJob(processScheduleJob())
                .build();

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public MethodInvokingJobDetailFactoryBean checkBandwidthTransitionsJob() {
        MethodInvokingJobDetailFactoryBean bean = new MethodInvokingJobDetailFactoryBean();
        bean.setTargetObject(harvestCoordinator);
        bean.setTargetMethod("checkForBandwidthTransition");

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public Trigger bandwidthCheckTrigger() {
        // delay before running the job measured in milliseconds
        Date startTime = new Date(System.currentTimeMillis() + bandwidthCheckTriggerStartDelay);
        Trigger bean = newTrigger()
                .withIdentity("BandwidthCheckTrigger", "BandwidthCheckTriggerGroup")
                .startAt(startTime)
                .withSchedule(simpleSchedule().repeatForever().withIntervalInMilliseconds(bandwidthCheckTriggerRepeatInterval))
                .forJob(checkBandwidthTransitionsJob().getObject())
                .build();

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public SchedulerFactoryBean schedulerFactory() {
        SchedulerFactoryBean bean = new SchedulerFactoryBean();

        bean.setJobDetails(processScheduleJob(), checkBandwidthTransitionsJob().getObject(),
                purgeDigitalAssetsJob().getObject(), purgeAbortedTargetInstancesJob().getObject(),
                groupExpiryJob().getObject(), createNewTargetInstancesJob().getObject());

        bean.setTriggers(processScheduleTrigger(), bandwidthCheckTrigger(), purgeDigitalAssetsTrigger(),
                purgeAbortedTargetInstancesTrigger(), groupExpiryJobTrigger(), createNewTargetInstancesTrigger());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public AgencyUserManagerImpl agencyUserManager() {
        AgencyUserManagerImpl bean = new AgencyUserManagerImpl();
        bean.setUserRoleDAO(userRoleDAO());
        bean.setRejReasonDAO(rejReasonDAO());
        bean.setIndicatorCriteriaDAO(indicatorCriteriaDAO());
        bean.setFlagDAO(flagDAO());
        bean.setAuditor(audit());
        bean.setAuthorityManager(authorityManager());
        bean.setProfileManager(profileManager());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public AuthorityManagerImpl authorityManager() {
        return new AuthorityManagerImpl();
    }

    @Bean
    public BusinessObjectFactory businessObjectFactory() {
        BusinessObjectFactory bean = new BusinessObjectFactory();
        bean.setProfileManager(profileManager());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public AnnotationDAOImpl annotationDao() {
        AnnotationDAOImpl bean = new AnnotationDAOImpl();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public MailServerImpl mailServer() {
        Properties properties = new Properties();
        properties.put("mail.transport.protocol", mailProtocol);
        properties.put("mail.smtp.host", mailServerSmtpHost);
        properties.put("mail.smtp.port", mailSmtpPort);

        MailServerImpl bean = new MailServerImpl(properties);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public BandwidthChecker bandwidthChecker() {
        BandwidthChecker bean = new BandwidthChecker();
        bean.setWarnThreshold(bandwidthCheckerWarnThreshold);
        bean.setErrorThreshold(bandwidthCheckerErrorThreshold);
        bean.setNotificationSubject("Core");
        bean.setCheckType("Bandwidth");
//        bean.setHarvestCoordinator(harvestCoordinator);
        bean.setHarvestAgentManager(harvestAgentManager());
        bean.setHarvestBandwidthManager(harvestBandwidthManager());
        bean.setNotifier(checkNotifier());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public CoreCheckNotifier checkNotifier() {
        CoreCheckNotifier bean = new CoreCheckNotifier();
        bean.setInTrayManager(inTrayManager());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public CheckProcessor checkProcessor() {
        CheckProcessor bean = new CheckProcessor();

        List<Checker> checksList = new ArrayList<>();
        checksList.add(bandwidthChecker());

        bean.setChecks(checksList);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public MethodInvokingJobDetailFactoryBean checkProcessorJob() {
        MethodInvokingJobDetailFactoryBean bean = new MethodInvokingJobDetailFactoryBean();
        bean.setTargetObject(checkProcessor());
        bean.setTargetMethod("check");

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public Trigger checkProcessorTrigger() {
        // delay before running the job measured in milliseconds
        Date startTime = new Date(System.currentTimeMillis() + checkProcessorTriggerStartDelay);
        Trigger bean = newTrigger()
                .withIdentity("CheckProcessorTrigger", "CheckProcessorTriggerGroup")
                .startAt(startTime)
                .withSchedule(simpleSchedule().repeatForever().withIntervalInMilliseconds(checkProcessorTriggerRepeatInterval))
                .forJob(checkProcessorJob().getObject())
                .build();

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public MethodInvokingJobDetailFactoryBean purgeDigitalAssetsJob() {
        MethodInvokingJobDetailFactoryBean bean = new MethodInvokingJobDetailFactoryBean();
        bean.setTargetObject(harvestCoordinator);
        bean.setTargetMethod("purgeDigitalAssets");

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public Trigger purgeDigitalAssetsTrigger() {
        // delay before running the job measured in milliseconds
        Date startTime = new Date(System.currentTimeMillis() + checkProcessorTriggerStartDelay);
        Trigger bean = newTrigger()
                .withIdentity("PurgeDigitalAssetsTrigger", "PurgeDigitalAssetsTriggerGroup")
                .startAt(startTime)
                .withSchedule(simpleSchedule().repeatForever().withIntervalInMilliseconds(purgeDigitalAssetsTriggerRepeatInterval))
                .forJob(purgeDigitalAssetsJob().getObject())
                .build();

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public MethodInvokingJobDetailFactoryBean purgeAbortedTargetInstancesJob() {
        MethodInvokingJobDetailFactoryBean bean = new MethodInvokingJobDetailFactoryBean();
        bean.setTargetObject(harvestCoordinator);
        bean.setTargetMethod("purgeAbortedTargetInstances");

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public Trigger purgeAbortedTargetInstancesTrigger() {
        // delay before running the job measured in milliseconds
        Date startTime = new Date(System.currentTimeMillis() + checkProcessorTriggerStartDelay);
        Trigger bean = newTrigger()
                .withIdentity("PurgeAbortedTargetInstancesTrigger", "PurgeAbortedTargetInstancesTriggerGroup")
                .startAt(startTime)
                .withSchedule(simpleSchedule().repeatForever().withIntervalInMilliseconds(purgeAbortedTargetInstancesTriggerRepeatInterval))
                .forJob(purgeAbortedTargetInstancesJob().getObject())
                .build();

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public InTrayManagerImpl inTrayManager() {
        InTrayManagerImpl bean = new InTrayManagerImpl();
        bean.setInTrayDAO(inTrayDao());
        bean.setUserRoleDAO(userRoleDAO());
        bean.setAgencyUserManager(agencyUserManager());
        bean.setMailServer(mailServer());
        bean.setAudit(audit());
        bean.setSender(inTrayManagerSender);
        bean.setMessageSource(messageSource());
        bean.setWctBaseUrl(inTrayManagerWctBaseUrl);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public PermissionTemplateManagerImpl permissionTemplateManager() {
        PermissionTemplateManagerImpl bean = new PermissionTemplateManagerImpl();
        bean.setPermissionTemplateDAO(permissionTemplateDao());
        bean.setAuthorityManager(authorityManager());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public MethodInvokingJobDetailFactoryBean groupExpiryJob() {
        MethodInvokingJobDetailFactoryBean bean = new MethodInvokingJobDetailFactoryBean();
        bean.setTargetObject(targetManager());
        bean.setTargetMethod("endDateGroups");

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public Trigger groupExpiryJobTrigger() {
        // delay before running the job measured in milliseconds
        Date startTime = new Date(System.currentTimeMillis() + groupExpiryJobTriggerStartDelay);
        Trigger bean = newTrigger()
                .withIdentity("GroupExpiryJobTrigger", "GroupExpiryJobGroup")
                .startAt(startTime)
                .withSchedule(simpleSchedule().repeatForever().withIntervalInMilliseconds(groupExpiryJobTriggerRepeatInterval))
                .forJob(groupExpiryJob().getObject())
                .build();

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public MethodInvokingJobDetailFactoryBean createNewTargetInstancesJob() {
        MethodInvokingJobDetailFactoryBean bean = new MethodInvokingJobDetailFactoryBean();
        bean.setTargetObject(targetManager());
        bean.setTargetMethod("processSchedulesJob");

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public Trigger createNewTargetInstancesTrigger() {
        // delay before running the job measured in milliseconds
        Date startTime = new Date(System.currentTimeMillis() + createNewTargetInstancesTriggerStartDelay);
        Trigger bean = newTrigger()
                .withIdentity("createNewTargetInstancesTrigger", "createNewTargetInstancesJobGroup")
                .startAt(startTime)
                .withSchedule(simpleSchedule().repeatForever().withIntervalInMilliseconds(createNewTargetInstancesTriggerRepeatInterval))
                .forJob(createNewTargetInstancesJob().getObject())
                .build();

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public ArchiveAdapterImpl archiveAdapter() {
        ArchiveAdapterImpl bean = new ArchiveAdapterImpl();
        bean.setDigitalAssetStore(digitalAssetStore());
        bean.setTargetInstanceManager(targetInstanceManager());
        bean.setTargetManager(targetManager());
        bean.setAccessStatusMap(listsConfig.accessStatusMap());
        bean.setTargetReferenceMandatory(archiveAdapterTargetReferenceMandatory);

        return bean;
    }

    @Bean
    public DateUtils dateUtils() {
        return DateUtils.get();
    }

    @Bean
    public HarvestResourceUrlMapper harvestResourceUrlMapper() {
        HarvestResourceUrlMapper bean = new HarvestResourceUrlMapper();
        bean.setUrlMap(harvestResourceUrlMapperUrlMap);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public TreeToolControllerAttribute treeToolControllerAttribute() {
        TreeToolControllerAttribute bean = new TreeToolControllerAttribute();
        bean.setEnableAccessTool(qualityReviewToolControllerEnableAccessTool);
        bean.setUploadedFilesDir(digitalAssetStoreServerUploadedFilesDir);
        bean.setAutoQAUrl(harvestCoordinatorAutoQAUrl);
        return bean;
    }


    //TODO: uncheck
//    @Bean
//    @Scope(BeanDefinition.SCOPE_SINGLETON)
//    @Lazy(false)
//    public TreeToolControllerAJAX treeToolControllerAJAX() {
//        TreeToolControllerAJAX bean = new TreeToolControllerAJAX();
//        bean.setQualityReviewFacade(qualityReviewFacade());
//        bean.setHarvestResourceUrlMapper(harvestResourceUrlMapper());
//        return bean;
//    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public QualityReviewToolControllerAttribute qualityReviewToolControllerAttribute() {
        QualityReviewToolControllerAttribute bean = new QualityReviewToolControllerAttribute();
        bean.setTargetInstanceManager(targetInstanceManager());
        bean.setTargetManager(targetManager());
        bean.setArchiveUrl(qualityReviewToolControllerArchiveUrl);
        bean.setArchiveName(qualityReviewToolControllerArchiveName);
        bean.setArchiveUrlAlternative(qualityReviewToolControllerArchiveUrlAlternative);
        bean.setArchiveUrlAlternativeName(qualityReviewToolControllerArchiveAlternativeName);
        bean.setHarvestResourceUrlMapper(harvestResourceUrlMapper());
        bean.setTargetInstanceDao(targetInstanceDao());
        bean.setEnableBrowseTool(qualityReviewToolControllerEnableBrowseTool);
        bean.setEnableAccessTool(qualityReviewToolControllerEnableAccessTool);
        bean.setWebArchiveTarget(qualityReviewToolControllerWebArchiveTarget);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public PolitenessOptions politePolitenessOptions() {
        // Delay Factor, Min Delay milliseconds, Max Delay milliseconds,
        // Respect crawl delay up to seconds, Max per host bandwidth usage kb/sec
        return new PolitenessOptions(crawlPolitenessPoliteDelayFactor, crawlPolitenessPoliteMinDelayMs, crawlPolitenessPoliteMaxDelayMs, crawlPolitenessPoliteRespectCrawlDelay, crawlPolitenessPoliteMaxPerHostBandwidth);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public PolitenessOptions mediumPolitenessOptions() {
        // Delay Factor, Min Delay milliseconds, Max Delay milliseconds,
        // Respect crawl delay up to seconds, Max per host bandwidth usage kb/sec
        return new PolitenessOptions(crawlPolitenessMediumDelayFactor, crawlPolitenessMediumMinDelayMs, crawlPolitenessMediumMaxDelayMs, crawlPolitenessMediumRespectCrawlDelay, crawlPolitenessMediumMaxPerHostBandwidth);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public PolitenessOptions aggressivePolitenessOptions() {
        // Delay Factor, Min Delay milliseconds, Max Delay milliseconds,
        // Respect crawl delay up to seconds, Max per host bandwidth usage kb/sec
        return new PolitenessOptions(crawlPolitenessAggressiveDelayFactor, crawlPolitenessAggressiveMinDelayMs, crawlPolitenessAggressiveMaxDelayMs, crawlPolitenessAggressiveRespectCrawlDelay, crawlPolitenessAggressiveMaxPerHostBandwidth);
    }
}
