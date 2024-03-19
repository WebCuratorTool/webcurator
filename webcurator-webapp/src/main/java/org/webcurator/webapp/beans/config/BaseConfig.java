package org.webcurator.webapp.beans.config;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
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
import org.webcurator.auth.AuthorityManager;
import org.webcurator.common.util.DateUtils;
import org.webcurator.core.admin.PermissionTemplateManager;
import org.webcurator.core.agency.AgencyUserManager;
import org.webcurator.core.archive.ArchiveAdapter;
import org.webcurator.core.archive.SipBuilder;
import org.webcurator.core.check.CheckProcessor;
import org.webcurator.core.check.Checker;
import org.webcurator.core.check.CoreCheckNotifier;
import org.webcurator.core.common.Environment;
import org.webcurator.core.common.EnvironmentFactory;
import org.webcurator.core.coordinator.HarvestResultManager;
import org.webcurator.core.coordinator.WctCoordinator;
import org.webcurator.core.harvester.agent.HarvestAgentFactory;
import org.webcurator.core.harvester.coordinator.HarvestAgentManager;
import org.webcurator.core.harvester.coordinator.HarvestLogManager;
import org.webcurator.core.harvester.coordinator.HarvestQaManager;
import org.webcurator.core.harvester.coordinator.PatchingHarvestLogManager;
import org.webcurator.core.notification.InTrayManager;
import org.webcurator.core.notification.MailServer;
import org.webcurator.core.permissionmapping.HierPermMappingDAO;
import org.webcurator.core.permissionmapping.HierarchicalPermissionMappingStrategy;
import org.webcurator.core.permissionmapping.PermMappingSiteListener;
import org.webcurator.core.permissionmapping.PermissionMappingStrategy;
import org.webcurator.core.profiles.PolitenessOptions;
import org.webcurator.core.profiles.ProfileManager;
import org.webcurator.core.reader.LogReader;
import org.webcurator.core.reader.LogReaderClient;
import org.webcurator.core.reader.LogReaderImpl;
import org.webcurator.core.report.LogonDurationDAO;
import org.webcurator.core.rules.QaRecommendationService;
import org.webcurator.core.scheduler.ScheduleJob;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.core.sites.SiteManager;
import org.webcurator.core.sites.SiteManagerListener;
import org.webcurator.core.store.DigitalAssetStoreClient;
import org.webcurator.core.store.DigitalAssetStoreFactory;
import org.webcurator.core.targets.TargetManager;
import org.webcurator.core.targets.TargetManager2;
import org.webcurator.core.util.ApplicationContextFactory;
import org.webcurator.core.util.AuditDAOUtil;
import org.webcurator.core.util.LockManager;
import org.webcurator.core.visualization.VisualizationDirectoryManager;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClientRemote;
import org.webcurator.domain.*;
import org.webcurator.domain.model.core.BusinessObjectFactory;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.SchedulePattern;
import org.webcurator.ui.tools.controller.HarvestResourceUrlMapper;
import org.webcurator.ui.tools.controller.QualityReviewToolControllerAttribute;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Contains configuration that used to be found in {@code wct-core.xml}. This
 * is part of the change to move to using annotations for Spring instead of
 * XML files.
 */
@SuppressWarnings("all")
@Configuration
@EnableTransactionManagement
@ComponentScan("org.webcurator.core.coordinator")
public class BaseConfig {
    private static Logger LOGGER = LoggerFactory.getLogger(BaseConfig.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

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

    @Value("${targetManager.harvestNowDelay}")
    private int harvestNowDelay;

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

    @Value("${core.base.dir}")
    private String baseDir;

    @Autowired
    private ListsConfig listsConfig;

    @Autowired
    private WctCoordinator wctCoordinator;

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public VisualizationDirectoryManager visualizationManager() {
        return new VisualizationDirectoryManager(baseDir, "", "");
    }

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
        if (!hibernateDialect.toLowerCase().contains("mysql")) {
            hibernateProperties.setProperty("hibernate.default_schema", hibernateDefaultSchema);
        }
        hibernateProperties.setProperty("hibernate.transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory");
        hibernateProperties.setProperty("hibernate.enable_lazy_load_no_trans", "true");

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
        HibernateTransactionManager hibernateTransactionManager = new HibernateTransactionManager(sessionFactory().getObject());
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
    public LogReader logReader() {
        LogReader bean = new LogReaderImpl();
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
    public DigitalAssetStoreFactory digitalAssetStoreFactory() {
        DigitalAssetStoreFactory bean = new DigitalAssetStoreFactory();
        bean.setDAS(digitalAssetStore());
        bean.setLogReader(new LogReaderClient(digitalAssetStoreBaseUrl, restTemplateBuilder));
        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public NetworkMapClient networkMapClientReomote() {
        return new NetworkMapClientRemote(digitalAssetStoreBaseUrl, restTemplateBuilder);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public QaRecommendationService qaRecommendationService() {
        QaRecommendationService bean = new QaRecommendationService();
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
        bean.setTargetInstanceManager(targetInstanceManager());

        return bean;
    }

    @Bean
    public TargetInstanceDAO targetInstanceDao() {
        TargetInstanceDAO bean = new TargetInstanceDAO();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());
        bean.setAuditor(audit());

        return bean;
    }

    @Bean
    public UserRoleDAO userRoleDAO() {
        UserRoleDAO bean = new UserRoleDAO();
        bean.setSessionFactory(sessionFactory().getObject());
        //bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public RejReasonDAO rejReasonDAO() {
        RejReasonDAO bean = new RejReasonDAO();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public IndicatorDAO indicatorDAO() {
        IndicatorDAO bean = new IndicatorDAO();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public IndicatorCriteriaDAO indicatorCriteriaDAO() {
        IndicatorCriteriaDAO bean = new IndicatorCriteriaDAO();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public IndicatorReportLineDAO indicatorReportLineDAO() {
        IndicatorReportLineDAO bean = new IndicatorReportLineDAO();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public FlagDAO flagDAO() {
        FlagDAO bean = new FlagDAO();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public TargetDAO targetDao() {
        TargetDAO bean = new TargetDAO();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public SiteDAO siteDao() {
        SiteDAO bean = new SiteDAO();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public PermissionDAO permissionDAO() {
        PermissionDAO bean = new PermissionDAO();
        bean.setSessionFactory(sessionFactory().getObject());
        return bean;
    }

    @Bean
    public ProfileDAO profileDao() {
        ProfileDAO bean = new ProfileDAO();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public InTrayDAO inTrayDao() {
        InTrayDAO bean = new InTrayDAO();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public HeatmapDAO heatmapConfigDao() {
        HeatmapDAO bean = new HeatmapDAO();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    public PermissionTemplateDAO permissionTemplateDao() {
        PermissionTemplateDAO bean = new PermissionTemplateDAO();
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
    public HarvestResultManager harvestResultManager() {
        HarvestResultManager bean = new HarvestResultManager();
        bean.setTargetInstanceManager(targetInstanceManager());
        bean.setNetworkMapClient(networkMapClientReomote());
        return bean;
    }

    @Bean
    public HarvestAgentManager harvestAgentManager() {
        HarvestAgentManager bean = new HarvestAgentManager();
        bean.setHarvestAgentFactory(harvestAgentFactory());
        bean.setTargetInstanceManager(targetInstanceManager());
        bean.setTargetInstanceDao(targetInstanceDao());
        bean.setHarvestResultManager(harvestResultManager());
        return bean;
    }

    @Bean
    public HarvestLogManager harvestLogManager() {
        HarvestLogManager bean = new HarvestLogManager();
        bean.setHarvestAgentManagerImpl(harvestAgentManager());
        bean.setDigitalAssetStoreFactory(digitalAssetStoreFactory());

        return bean;
    }

    @Bean(name = HarvestResult.PATCH_STAGE_TYPE_CRAWLING)
    public PatchingHarvestLogManager patchingHarvestLogManagerNormal() {
        PatchingHarvestLogManager bean = new PatchingHarvestLogManager();
        bean.setHarvestAgentManager(harvestAgentManager());
        bean.setDigitalAssetStoreFactory(digitalAssetStoreFactory());
        bean.setType(HarvestResult.PATCH_STAGE_TYPE_CRAWLING);
        return bean;
    }

    @Bean(name = HarvestResult.PATCH_STAGE_TYPE_MODIFYING)
    public PatchingHarvestLogManager patchingHarvestLogManagerModification() {
        PatchingHarvestLogManager bean = new PatchingHarvestLogManager();
        bean.setHarvestAgentManager(harvestAgentManager());
        bean.setDigitalAssetStoreFactory(digitalAssetStoreFactory());
        bean.setType(HarvestResult.PATCH_STAGE_TYPE_MODIFYING);
        return bean;
    }

    @Bean(name = HarvestResult.PATCH_STAGE_TYPE_INDEXING)
    public PatchingHarvestLogManager patchingHarvestLogManagerIndex() {
        PatchingHarvestLogManager bean = new PatchingHarvestLogManager();
        bean.setHarvestAgentManager(harvestAgentManager());
        bean.setDigitalAssetStoreFactory(digitalAssetStoreFactory());
        bean.setType(HarvestResult.PATCH_STAGE_TYPE_INDEXING);
        return bean;
    }

    @Bean
    public HarvestQaManager harvestQaManager() {
        HarvestQaManager bean = new HarvestQaManager();
        bean.setTargetInstanceManager(targetInstanceManager());
        bean.setTargetInstanceDao(targetInstanceDao());
        bean.setAutoQAUrl(autoQAUrl);
        bean.setEnableQaModule(enableQaModule);
        bean.setAutoPrunedNote(autoPrunedNote);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public TargetInstanceManager targetInstanceManager() {
        TargetInstanceManager bean = new TargetInstanceManager();
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
    public SiteManager siteManager() {
        SiteManager bean = new SiteManager();
        bean.setSiteDao(siteDao());
        bean.setAnnotationDAO(annotationDao());

        PermMappingSiteListener permMappingSiteListener = new PermMappingSiteListener();
        permMappingSiteListener.setStrategy(permissionMappingStrategy());
        List<SiteManagerListener> permMappingSiteListenerList = new ArrayList<>(
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
    @Primary // Make sure we only get the subclass TargetManager2 when we explicitly request it
    public TargetManager targetManager() {
        TargetManager bean = new TargetManager();
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
    public TargetManager2 targetManager2() {
        TargetManager2 bean = new TargetManager2();
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
        bean.setHarvestNowDelay(harvestNowDelay);

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
    public LogonDurationDAO logonDuration() {
        LogonDurationDAO bean = new LogonDurationDAO();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public HarvestAgentFactory harvestAgentFactory() {
        return new HarvestAgentFactory();
    }

    @Bean
    public Environment environmentWCT() {
        Environment bean = new Environment();
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
    public HierPermMappingDAO permMappingDao() {
        HierPermMappingDAO bean = new HierPermMappingDAO();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public JobDetail processScheduleJob() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("wctCoordinator", wctCoordinator);

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
    @Lazy(false)
    public SchedulerFactoryBean schedulerFactory() {
        SchedulerFactoryBean bean = new SchedulerFactoryBean();

        bean.setJobDetails(processScheduleJob(),
                purgeDigitalAssetsJob().getObject(), purgeAbortedTargetInstancesJob().getObject(),
                groupExpiryJob().getObject(), createNewTargetInstancesJob().getObject());

        bean.setTriggers(processScheduleTrigger(), purgeDigitalAssetsTrigger(),
                purgeAbortedTargetInstancesTrigger(), groupExpiryJobTrigger(), createNewTargetInstancesTrigger());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public AgencyUserManager agencyUserManager() {
        AgencyUserManager bean = new AgencyUserManager();
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
    public AuthorityManager authorityManager() {
        return new AuthorityManager();
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
    public AnnotationDAO annotationDao() {
        AnnotationDAO bean = new AnnotationDAO();
        bean.setSessionFactory(sessionFactory().getObject());
        bean.setTxTemplate(transactionTemplate());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public MailServer mailServer() {
        Properties properties = new Properties();
        properties.put("mail.transport.protocol", mailProtocol);
        properties.put("mail.smtp.host", mailServerSmtpHost);
        properties.put("mail.smtp.port", mailSmtpPort);

        MailServer bean = new MailServer(properties);

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
//        checksList.add(bandwidthChecker());

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
        bean.setTargetObject(wctCoordinator);
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
        bean.setTargetObject(wctCoordinator);
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
    public InTrayManager inTrayManager() {
        InTrayManager bean = new InTrayManager();
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
    public PermissionTemplateManager permissionTemplateManager() {
        PermissionTemplateManager bean = new PermissionTemplateManager();
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
    public ArchiveAdapter archiveAdapter() {
        ArchiveAdapter bean = new ArchiveAdapter();
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
