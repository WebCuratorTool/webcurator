package org.webcurator.webapp.beans.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.hibernate5.support.OpenSessionInViewInterceptor;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.tiles2.TilesConfigurer;
import org.springframework.web.servlet.view.tiles2.TilesView;
import org.webcurator.core.store.tools.QualityReviewFacade;
import org.webcurator.ui.admin.controller.*;
import org.webcurator.ui.admin.validator.*;
import org.webcurator.ui.agent.controller.BandwidthRestrictionsController;
import org.webcurator.ui.agent.controller.ManageHarvestAgentController;
import org.webcurator.ui.agent.validator.BandwidthRestrictionValidator;
import org.webcurator.ui.archive.ArchiveController;
import org.webcurator.ui.archive.TestArchiveController;
import org.webcurator.ui.base.LogoutController;
import org.webcurator.ui.credentials.controller.ResetPasswordController;
import org.webcurator.ui.credentials.validator.ResetPasswordValidator;
import org.webcurator.ui.groups.command.*;
import org.webcurator.ui.groups.controller.*;
import org.webcurator.ui.groups.validator.*;
import org.webcurator.ui.groups.validator.AddParentsValidator;
import org.webcurator.ui.home.controller.HomeController;
import org.webcurator.ui.intray.controller.InTrayController;
import org.webcurator.ui.management.controller.ManagementController;
import org.webcurator.ui.profiles.command.Heritrix3ProfileCommand;
import org.webcurator.ui.profiles.command.ImportedHeritrix3ProfileCommand;
import org.webcurator.ui.profiles.controller.*;
import org.webcurator.ui.profiles.renderers.GeneralOnlyRendererFilter;
import org.webcurator.ui.profiles.validator.Heritrix3ProfileValidator;
import org.webcurator.ui.profiles.validator.ImportedHeritrix3ProfileValidator;
import org.webcurator.ui.profiles.validator.ProfileGeneralValidator;
import org.webcurator.ui.report.command.ReportCommand;
import org.webcurator.ui.report.command.ReportEmailCommand;
import org.webcurator.ui.report.command.ReportPreviewCommand;
import org.webcurator.ui.report.command.ReportSaveCommand;
import org.webcurator.ui.report.controller.ReportController;
import org.webcurator.ui.report.controller.ReportEmailController;
import org.webcurator.ui.report.controller.ReportPreviewController;
import org.webcurator.ui.report.controller.ReportSaveController;
import org.webcurator.ui.report.validator.ReportValidator;
import org.webcurator.ui.site.command.*;
import org.webcurator.ui.site.controller.*;
import org.webcurator.ui.site.validator.*;
import org.webcurator.ui.target.command.*;
import org.webcurator.ui.target.controller.*;
import org.webcurator.ui.target.controller.AddParentsController;
import org.webcurator.ui.target.validator.*;
import org.webcurator.ui.tools.controller.HarvestHistoryController;
import org.webcurator.ui.util.EmptyCommand;
import org.webcurator.ui.util.OverrideGetter;
import org.webcurator.ui.util.Tab;
import org.webcurator.ui.util.TabConfig;

import java.util.*;

/**
 * Contains configuration that used to be found in {@code wct-core-servlet.xml}. This
 * is part of the change to move to using annotations for Spring instead of
 * XML files.
 *
 */
@Configuration
public class ServletConfig {

    @Value("${queueController.enableQaModule}")
    private boolean queueControllerEnableQaModule;

    // Configured width of the QA thumbnail preview.
    @Value("${queueController.thumbnailWidth}")
    private String queueControllerThumbnailWidth;

    // Configured height of the QA thumbnail preview.
    @Value("${queueController.thumbnailHeight}")
    private String queueControllerThumbnailHeight;

    @Value("${queueController.thumbnailRenderer}")
    private String queueControllerThumbnailRenderer;

    @Value("${harvestCoordinator.autoQAUrl}")
    private String harvestCoordinatorAutoQAUrl;

    @Value("${groupTypes.subgroupSeparator}")
    private String groupTypesSubgroupSeparator;

    @Value("${groupTypes.subgroup}")
    private String groupTypesSubgroup;

    @Value("${heritrix.version}")
    private String heritrixVersion;

    @Value("${h3.scriptsDirectory}")
    private String h3ScriptsDirectory;

    @Autowired
    private BaseConfig baseConfig;

    @Autowired
    private ListsConfig listsConfig;

    @Autowired
    private SecurityConfig securityConfig;

    // This method is declared static as BeanFactoryPostProcessor types need to be instatiated early. Instance methods
    // interfere with other bean lifecycle instantiations. See {@link Bean} javadoc for more details.
    @Bean
    public static PropertyPlaceholderConfigurer wctCoreServletConfigurer() {
        PropertyPlaceholderConfigurer bean = new PropertyPlaceholderConfigurer();
        bean.setLocations(new ClassPathResource("wct-core.properties"));
        bean.setIgnoreResourceNotFound(true);
        bean.setIgnoreUnresolvablePlaceholders(true);
        bean.setOrder(150);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public SimpleUrlHandlerMapping simpleUrlMapping() {
        SimpleUrlHandlerMapping bean = new SimpleUrlHandlerMapping();
        bean.setInterceptors(new Object[] { openSessionInViewInterceptor() });

        Properties mappings = new Properties();
        mappings.put("/curator/site/site.html", "siteController");
        mappings.put("/curator/site/search.html", "siteSearchController");
        mappings.put("/curator/site/agencies.html", "siteAgencyController");
        mappings.put("/curator/site/site-auth-agency-search.html", "siteAgencySearchController");
        mappings.put("/curator/site/permissions.html", "sitePermissionController");
        mappings.put("/curator/site/transfer.html", "transferSeedsController");
        mappings.put("/curator/site/generate.html", "generatePermissionTemplateController");
        mappings.put("/curator/agent/harvest-agent.html", "manageHarvestAgentController");
        mappings.put("/curator/agent/bandwidth-restrictions.html", "bandwidthRestrictionsController");
        mappings.put("/curator/tools/treetool.html", "treeToolController");
        mappings.put("/curator/tools/treetoolAJAX.html", "treeToolControllerAJAX");
        mappings.put("/curator/tools/harvest-history.html", "harvestHistoryController");
        mappings.put("/curator/credentials/reset-password.html", "resetPasswordController");
        mappings.put("/curator/target/quality-review-toc.html", "qualityReviewToolController");
        mappings.put("/curator/target/deposit-form-envelope.html", "customDepositFormController");
        mappings.put("/curator/logout.html", "logoutController");
        mappings.put("/curator/home.html", "homeController");
        mappings.put("/curator/target/queue.html", "queueController");
        mappings.put("/curator/target/qatisummary.html", "qaTiSummaryController");
        mappings.put("/curator/target/qa-indicator-report.html", "qaIndicatorReportController");
        mappings.put("/curator/target/qa-indicator-robots-report.html", "qaIndicatorRobotsReportController");
        mappings.put("/curator/target/annotation-ajax.html", "annotationAjaxController");
        mappings.put("/curator/target/target-instance.html", "tabbedTargetInstanceController");
        mappings.put("/curator/target/harvest-now.html", "harvestNowController");
        mappings.put("/curator/target/target.html", "targetController");
        mappings.put("/curator/target/search.html", "targetSearchController");
        mappings.put("/curator/target/schedule.html", "targetEditScheduleController");
        mappings.put("/curator/targets/add-parents.html", "addParentsController");
        mappings.put("/curator/admin/role.html", "roleController");
        mappings.put("/curator/profiles/profiles.html", "profileController");
        mappings.put("/curator/profiles/profilesH3.html", "profileH3Controller");
        mappings.put("/curator/profiles/imported-profilesH3.html", "importedProfileH3Controller");
        mappings.put("/curator/profiles/profiletargets.html", "profileTargetsController");
        mappings.put("/curator/profiles/list.html", "profileListController");
        mappings.put("/curator/profiles/view.html", "profileViewController");
        mappings.put("/curator/profiles/delete.html", "profileDeleteController");
        mappings.put("/curator/profiles/make-default.html", "makeDefaultProfileController");
        mappings.put("/curator/admin/rejreason.html", "rejReasonController");
        mappings.put("/curator/admin/create-rejreason.html", "createRejReasonController");
        mappings.put("/curator/admin/qaindicators.html", "qaIndicatorController");
        mappings.put("/curator/admin/create-qaindicator.html", "createQaIndicatorController");
        mappings.put("/curator/admin/flags.html", "flagController");
        mappings.put("/curator/admin/create-flag.html", "createFlagController");
        mappings.put("/curator/admin/user.html", "userController");
        mappings.put("/curator/admin/create-user.html", "createUserController");
        mappings.put("/curator/admin/associate-userroles.html", "associateUserRoleController");
        mappings.put("/curator/admin/agency.html", "agencyController");
        mappings.put("/curator/admin/change-password.html", "changePasswordController");
        mappings.put("/curator/admin/management.html", "managementController");
        mappings.put("/curator/admin/templates.html", "templateController");
        mappings.put("/curator/target/log-viewer.html", "logReaderController");
        mappings.put("/curator/target/content-viewer.html", "contentReaderController");
        mappings.put("/curator/target/live-content-retriever.html", "liveContentRetrieverController");
        mappings.put("/curator/target/aqa-viewer.html", "aqaReaderController");
        mappings.put("/curator/target/log-retriever.html", "logRetrieverController");
        mappings.put("/curator/target/show-hop-path.html", "showHopPathController");
        mappings.put("/curator/target/permission-popup.html", "permissionPopupController");
        mappings.put("/curator/target/target-basic-credentials.html", "basicCredentialsControllerTarget");
        mappings.put("/curator/target/target-form-credentials.html", "formCredentialsControllerTarget");
        mappings.put("/curator/target/ti-basic-credentials.html", "basicCredentialsControllerTargetInstance");
        mappings.put("/curator/target/ti-form-credentials.html", "formCredentialsControllerTargetInstance");
        mappings.put("/curator/target/h3ScriptConsole.html", "h3ScriptConsoleController");
        mappings.put("/curator/target/h3ScriptFile.html", "h3ScriptFileController");
        mappings.put("/curator/intray/intray.html", "inTrayController");
        mappings.put("/curator/report/report.html", "reportController");
        mappings.put("/curator/report/report-preview.html", "reportPreviewController");
        mappings.put("/curator/report/report-save.html", "reportSaveController");
        mappings.put("/curator/report/report-email.html", "reportEmailController");
        mappings.put("/curator/groups/search.html", "groupSearchController");
        mappings.put("/curator/groups/groups.html", "groupsController");
        mappings.put("/curator/groups/add-members.html", "addMembersController");
        mappings.put("/curator/groups/schedule.html", "groupsEditScheduleController");
        mappings.put("/curator/groups/add-parents.html", "groupAddParentsController");
        mappings.put("/curator/groups/move-targets.html", "moveTargetsController");
        mappings.put("/curator/archive/submit.html", "submitToArchiveController");
        mappings.put("/curator/archive/test.html", "testArchiveController");
        mappings.put("/curator/target/group-basic-credentials.html", "basicCredentialsControllerGroup");
        mappings.put("/curator/target/group-form-credentials.html", "formCredentialsControllerGroup");
        mappings.put("/curator/target/ti-harvest-now.html", "assignToHarvesterController");
        bean.setMappings(mappings);

        return bean;
    }

    @Bean
    public FixedLocaleResolver localeResolver() {
        return new FixedLocaleResolver();
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver bean = new CommonsMultipartResolver();
        bean.setMaxUploadSize(1500000);

        return bean;
    }

    @Bean
    public UrlBasedViewResolver tilesViewResolver() {
        UrlBasedViewResolver bean = new UrlBasedViewResolver();
        bean.setRequestContextAttribute("requestContext");
        bean.setViewClass(TilesView.class);

        return bean;
    }

    // Helper class to configure Tiles 2.x for the Spring Framework
    // See http://static.springsource.org/spring/docs/3.0.x/javadoc-api/org/springframework/web/servlet/view/tiles2/TilesConfigurer.html
    // The actual tiles templates are in the tiles-definitions.xml
    @Bean
    public TilesConfigurer tilesConfigurer() {
        TilesConfigurer bean = new TilesConfigurer();
        bean.setDefinitions("/WEB-INF/tiles-defs.xml");
        // property name="org.apache.tiles.factory.TilesContainerFactory" value="org.apache.tiles.factory.BasicTilesContainerFactory"

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public SimpleMappingExceptionResolver exceptionResolver() {
        SimpleMappingExceptionResolver bean = new SimpleMappingExceptionResolver();
        bean.setDefaultErrorView("Error");

        Properties exceptionMappings = new Properties();
        exceptionMappings.setProperty("org.springframework.orm.hibernate5.HibernateObjectRetrievalFailureException",
                "NoObjectFound");
        exceptionMappings.setProperty("org.springframework.web.multipart.MaxUploadSizeExceededException",
                "max-file-size-exceeeded");
        bean.setExceptionMappings(exceptionMappings);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public HomeController homeController() {
        HomeController bean = new HomeController();
        bean.setSupportedMethods("GET");
        bean.setInTrayManager(baseConfig.inTrayManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setSiteManager(baseConfig.siteManager());
        bean.setTargetManager(baseConfig.targetManager());
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setEnableQaModule(queueControllerEnableQaModule);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public SiteSearchController siteSearchController() {
        SiteSearchController bean = new SiteSearchController();
        bean.setSiteManager(baseConfig.siteManager());
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setCommandClass(SiteSearchCommand.class);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public SiteController siteController() {
        SiteController bean = new SiteController();
        bean.setSupportedMethods("GET", "POST");
        bean.setTabConfig(siteTabConfig());
        bean.setDefaultCommandClass(DefaultSiteCommand.class);
        bean.setSiteManager(baseConfig.siteManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setBusinessObjectFactory(baseConfig.businessObjectFactory());
        bean.setMessageSource(baseConfig.messageSource());
        bean.setSiteSearchController(siteSearchController());

        return bean;
    }

    @Bean
    public SiteAgencySearchController siteAgencySearchController() {
        SiteAgencySearchController bean = new SiteAgencySearchController();
        bean.setSiteController(siteController());
        bean.setSiteManager(baseConfig.siteManager());
        bean.setValidator(new SiteAgencySearchValidator());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public GeneratePermissionTemplateController generatePermissionTemplateController() {
        GeneratePermissionTemplateController bean = new GeneratePermissionTemplateController();
        bean.setSupportedMethods("GET", "POST");
        bean.setSiteManager(baseConfig.siteManager());
        bean.setMailServer(baseConfig.mailServer());
        bean.setMessageSource(baseConfig.messageSource());
        bean.setPermissionTemplateManager(baseConfig.permissionTemplateManager());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public TransferSeedsController transferSeedsController() {
        TransferSeedsController bean = new TransferSeedsController();
        bean.setTargetManager(baseConfig.targetManager());
        bean.setSiteManager(baseConfig.siteManager());
        bean.setMessageSource(baseConfig.messageSource());
        bean.setSiteController(siteController());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setValidator(new TransferSeedsValidator());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public HarvestHistoryController harvestHistoryController() {
        HarvestHistoryController bean = new HarvestHistoryController();
        bean.setSupportedMethods("GET", "POST");
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public TabConfig siteTabConfig() {
        TabConfig bean = new TabConfig();
        bean.setViewName("site");

        List<Tab> tabs = new ArrayList<>();

        Tab theTab = new Tab();
        theTab.setPageId("GENERAL");
        theTab.setTitle("general");
        theTab.setJsp("../site-general.jsp");
        theTab.setCommandClass(SiteCommand.class);
        theTab.setValidator(new SiteValidator());
        theTab.setTabHandler(siteGeneralHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("URLS");
        theTab.setTitle("url-patterns");
        theTab.setJsp("../site-urls.jsp");
        theTab.setCommandClass(UrlCommand.class);
        theTab.setValidator(siteURLsValidator());
        theTab.setTabHandler(siteUrlHandler());
        tabs.add(theTab);

        tabs.add(siteAgencyTab());
        tabs.add(sitePermissionsTab());

        bean.setTabs(tabs);

        return bean;
    }

    public SiteGeneralHandler siteGeneralHandler() {
        SiteGeneralHandler bean = new SiteGeneralHandler();
        bean.setSiteManager(baseConfig.siteManager());

        return bean;
    }

    public SiteURLsValidator siteURLsValidator() {
        SiteURLsValidator bean = new SiteURLsValidator();
        bean.setStrategy(baseConfig.permissionMappingStrategy());

        return bean;
    }

    public SiteUrlHandler siteUrlHandler() {
        SiteUrlHandler bean = new SiteUrlHandler();
        bean.setBusinessObjectFactory(baseConfig.businessObjectFactory());

        return bean;
    }

    @Bean
    public Tab siteAgencyTab() {
        Tab bean = new Tab();
        bean.setPageId("AUTHORISING_AGENCIES");
        bean.setTitle("agencies");
        bean.setJsp("../site-auth-agencies.jsp");
        bean.setCommandClass(SiteAuthorisingAgencyCommand.class);
        bean.setValidator(null);
        bean.setTabHandler(new SiteAuthorisingAgencyHandler());

        return bean;
    }

    @Bean
    public Tab sitePermissionsTab() {
        Tab bean = new Tab();
        bean.setPageId("PERMISSIONS");
        bean.setTitle("permissions");
        bean.setJsp("../site-permissions.jsp");
        bean.setCommandClass(SitePermissionCommand.class);
        bean.setValidator(null);
        bean.setTabHandler(sitePermissionHandler());

        return bean;
    }

    public SitePermissionHandler sitePermissionHandler() {
        SitePermissionHandler bean = new SitePermissionHandler();
        bean.setSiteManager(baseConfig.siteManager());
        bean.setAccessStatusList(listsConfig.accessStatusList());
        bean.setBusinessObjectFactory(baseConfig.businessObjectFactory());

        return bean;
    }

    @Bean
    public OverrideGetter targetInstanceOverrideGetter() {
        OverrideGetter bean = new OverrideGetter();
        bean.setOverrideableType("Target Instance");

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public TabConfig targetInstanceTabConfig() {
        TabConfig bean = new TabConfig();
        bean.setViewName("site");

        List<Tab> tabs = new ArrayList<>();

        Tab theTab = new Tab();
        theTab.setPageId("GENERAL");
        theTab.setTitle("general");
        theTab.setJsp("../target-instance-general.jsp");
        theTab.setCommandClass(TargetInstanceCommand.class);
        theTab.setValidator(new TargetInstanceValidator());
        theTab.setTabHandler(targetInstanceGeneralHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("PROFILE");
        theTab.setTitle("profile");
        theTab.setJsp("../target-profile.jsp");
        theTab.setCommandClass(TargetInstanceProfileCommand.class);
        theTab.setValidator(tiProfileOverridesValidator());
        theTab.setTabHandler(targetInstanceProfileHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("STATE");
        theTab.setTitle("harvest-state");
        theTab.setJsp("../target-instance-state.jsp");
        theTab.setCommandClass(TargetInstanceCommand.class);
        theTab.setValidator(null);
        theTab.setTabHandler(targetInstanceStateHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("LOGS");
        theTab.setTitle("logs");
        theTab.setJsp("../target-instance-logs.jsp");
        theTab.setCommandClass(TargetInstanceCommand.class);
        theTab.setValidator(null);
        theTab.setTabHandler(targetInstanceLogsHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("RESULTS");
        theTab.setTitle("harvest");
        theTab.setJsp("../target-instance-results.jsp");
        theTab.setCommandClass(TargetInstanceCommand.class);
        theTab.setValidator(null);
        theTab.setTabHandler(targetInstanceResultHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("ANNOTATIONS");
        theTab.setTitle("annotations");
        theTab.setJsp("../target-instance-annotations.jsp");
        theTab.setCommandClass(TargetInstanceCommand.class);
        theTab.setValidator(new TargetInstanceValidator());
        theTab.setTabHandler(targetInstanceAnnotationHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("DISPLAY");
        theTab.setTitle("display");
        theTab.setJsp("../target-instance-display.jsp");
        theTab.setCommandClass(TargetInstanceCommand.class);
        theTab.setValidator(new TargetInstanceValidator());
        theTab.setTabHandler(targetInstanceAnnotationHandler());
        tabs.add(theTab);

        bean.setTabs(tabs);

        return bean;
    }

    public TargetInstanceGeneralHandler targetInstanceGeneralHandler() {
        TargetInstanceGeneralHandler bean = new TargetInstanceGeneralHandler();
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setHarvestCoordinator(baseConfig.harvestCoordinator());
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setAutoQAUrl("${harvestCoordinator.autoQAUrl}");
        bean.setEnableQaModule(queueControllerEnableQaModule);

        return bean;
    }

    @Bean
    public ProfilesOverridesValidator tiProfileOverridesValidator() {
        return new ProfilesOverridesValidator();
    }

    public TargetInstanceProfileHandler targetInstanceProfileHandler() {
        TargetInstanceProfileHandler bean = new TargetInstanceProfileHandler();
        bean.setProfileManager(baseConfig.profileManager());
        bean.setOverrideGetter(targetInstanceOverrideGetter());
        bean.setCredentialUrlPrefix("ti");

        return bean;
    }

    public TargetInstanceStateHandler targetInstanceStateHandler() {
        TargetInstanceStateHandler bean = new TargetInstanceStateHandler();
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setHarvestCoordinator(baseConfig.harvestCoordinator());

        return bean;
    }

    public TargetInstanceLogsHandler targetInstanceLogsHandler() {
        TargetInstanceLogsHandler bean = new TargetInstanceLogsHandler();
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setHarvestCoordinator(baseConfig.harvestCoordinator());

        return bean;
    }

    public TargetInstanceResultHandler targetInstanceResultHandler() {
        TargetInstanceResultHandler bean = new TargetInstanceResultHandler();
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setHarvestCoordinator(baseConfig.harvestCoordinator());
        bean.setDigitalAssetStore(baseConfig.digitalAssetStore());
        bean.setAgencyUserManager(baseConfig.agencyUserManager());

        return bean;
    }

    public TargetInstanceAnnotationHandler targetInstanceAnnotationHandler() {
        TargetInstanceAnnotationHandler bean = new TargetInstanceAnnotationHandler();
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());

        return bean;
    }

    public TargetInstanceDisplayHandler targetInstanceDisplayHandler() {
        TargetInstanceDisplayHandler bean = new TargetInstanceDisplayHandler();
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public TabbedTargetInstanceController tabbedTargetInstanceController() {
        TabbedTargetInstanceController bean = new TabbedTargetInstanceController();
        bean.setSupportedMethods("GET", "POST");
        bean.setTabConfig(targetInstanceTabConfig());
        bean.setDefaultCommandClass(TargetInstanceCommand.class);
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setHarvestCoordinator(baseConfig.harvestCoordinator());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setQueueController(queueController());
        bean.setMessageSource(baseConfig.messageSource());
        
        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public ManageHarvestAgentController manageHarvestAgentController() {
        ManageHarvestAgentController bean = new ManageHarvestAgentController();
        bean.setSupportedMethods("GET", "POST");
        bean.setHarvestCoordinator(baseConfig.harvestCoordinator());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public BandwidthRestrictionsController bandwidthRestrictionsController() {
        BandwidthRestrictionsController bean = new BandwidthRestrictionsController();
        bean.setSupportedMethods("GET", "POST");
        bean.setHarvestBandwidthManager(baseConfig.harvestBandwidthManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setHeatmapConfigDao(baseConfig.heatmapConfigDao());
        bean.setMessageSource(baseConfig.messageSource());
        bean.setValidator(bandwidthRestrictionValidator());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public BandwidthRestrictionValidator bandwidthRestrictionValidator() {
        return new BandwidthRestrictionValidator();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public OpenSessionInViewInterceptor openSessionInViewInterceptor() {
        OpenSessionInViewInterceptor bean = new OpenSessionInViewInterceptor();
        bean.setSessionFactory(baseConfig.sessionFactory().getObject());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public LogoutController logoutController() {
        return new LogoutController();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public ResetPasswordController resetPasswordController() {
        ResetPasswordController bean = new ResetPasswordController();
        bean.setSupportedMethods("GET", "POST");
        bean.setValidator(resetPasswordValidator());
        bean.setAuthDAO(baseConfig.userRoleDAO());
        bean.setEncoder(securityConfig.passwordEncoder());
        bean.setSalt(securityConfig.saltSource());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public ResetPasswordValidator resetPasswordValidator() {
        return new ResetPasswordValidator();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public ChangePasswordController changePasswordController() {
        ChangePasswordController bean = new ChangePasswordController();
        bean.setSupportedMethods("POST");
        bean.setValidator(changePasswordValidator());
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setEncoder(securityConfig.passwordEncoder());
        bean.setSalt(securityConfig.saltSource());
        bean.setMessageSource(baseConfig.messageSource());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public ChangePasswordValidator changePasswordValidator() {
        return new ChangePasswordValidator();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public QueueController queueController() {
        QueueController bean = new QueueController();
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setHarvestCoordinator(baseConfig.harvestCoordinator());
        bean.setEnvironment(baseConfig.environmentWCT());
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setMessageSource(baseConfig.messageSource());
        bean.setEnableQaModule(queueControllerEnableQaModule);
        bean.setThumbnailWidth(queueControllerThumbnailWidth);
        bean.setThumbnailHeight(queueControllerThumbnailHeight);
        bean.setThumbnailRenderer(queueControllerThumbnailRenderer);
        bean.setHarvestResourceUrlMapper(baseConfig.harvestResourceUrlMapper());

        return bean;
    }

    @Bean
    public QualityReviewFacade qualityReviewFacade() {
        QualityReviewFacade bean = new QualityReviewFacade();
        bean.setDigialAssetStore(baseConfig.digitalAssetStore());
        bean.setTargetInstanceDao(baseConfig.targetInstanceDao());
        bean.setAuditor(baseConfig.audit());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public QaTiSummaryController qaTiSummaryController() {
        QaTiSummaryController bean = new QaTiSummaryController();
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setHarvestCoordinator(baseConfig.harvestCoordinator());
        bean.setEnvironment(baseConfig.environmentWCT());
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setBusinessObjectFactory(baseConfig.businessObjectFactory());
        bean.setTargetManager(baseConfig.targetManager());
        bean.setMessageSource(baseConfig.messageSource());
        bean.setValidator(qaTiSummaryValidator());
        bean.setDigitalAssetStore(baseConfig.digitalAssetStore());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public QaIndicatorReportController qaIndicatorReportController() {
        QaIndicatorReportController bean = new QaIndicatorReportController();
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setIndicatorDAO(baseConfig.indicatorDAO());
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setMessageSource(baseConfig.messageSource());

        // These indicators are excluded from the IndicatorReportLine report since they have an alternative
        // representation built by a separate report.
        // The indicator name is specified by the map key and the report for the indicator is the map value.
        Map<String, String> excludedIndicators = new HashMap<>();
        excludedIndicators.put("Robots.txt entries disallowed", "/curator/target/qa-indicator-robots-report.html");
        bean.setExcludedIndicators(excludedIndicators);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public QaIndicatorRobotsReportController qaIndicatorRobotsReportController() {
        QaIndicatorRobotsReportController bean = new QaIndicatorRobotsReportController();
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setIndicatorDAO(baseConfig.indicatorDAO());
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setMessageSource(baseConfig.messageSource());
        bean.setQualityReviewFacade(qualityReviewFacade());
        bean.setFileNotFoundMessage("robots.txt file not found");

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public AnnotationAjaxController annotationAjaxController() {
        AnnotationAjaxController bean = new AnnotationAjaxController();
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setTargetManager(baseConfig.targetManager());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public RoleController roleController() {
        RoleController bean = new RoleController();
        bean.setSupportedMethods("GET", "POST");
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setValidator(roleValidator());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setMessageSource(baseConfig.messageSource());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public RejReasonController rejReasonController() {
        RejReasonController bean = new RejReasonController();
        bean.setSupportedMethods("GET", "POST");
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setMessageSource(baseConfig.messageSource());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public CreateRejReasonController createRejReasonController() {
        CreateRejReasonController bean = new CreateRejReasonController();
        bean.setSupportedMethods("GET", "POST");
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setValidator(createRejReasonValidator());
        bean.setMessageSource(baseConfig.messageSource());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public QaIndicatorController qaIndicatorController() {
        QaIndicatorController bean = new QaIndicatorController();
        bean.setSupportedMethods("GET", "POST");
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setMessageSource(baseConfig.messageSource());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public FlagController flagController() {
        FlagController bean = new FlagController();
        bean.setSupportedMethods("GET", "POST");
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setMessageSource(baseConfig.messageSource());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public CreateQaIndicatorController createQaIndicatorController() {
        CreateQaIndicatorController bean = new CreateQaIndicatorController();
        bean.setSupportedMethods("GET", "POST");
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setValidator(createQaIndicatorValidator());
        bean.setMessageSource(baseConfig.messageSource());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public CreateFlagController createFlagController() {
        CreateFlagController bean = new CreateFlagController();
        bean.setSupportedMethods("GET", "POST");
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setValidator(createFlagValidator());
        bean.setMessageSource(baseConfig.messageSource());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public UserController userController() {
        UserController bean = new UserController();
        bean.setSupportedMethods("GET", "POST");
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setMessageSource(baseConfig.messageSource());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public CreateUserController createUserController() {
        CreateUserController bean = new CreateUserController();
        bean.setSupportedMethods("GET", "POST");
        bean.setSaltSource(securityConfig.saltSource());
        bean.setPasswordEncoder(securityConfig.passwordEncoder());
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setValidator(createFlagValidator());
        bean.setMessageSource(baseConfig.messageSource());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public AssociateUserRoleController associateUserRoleController() {
        AssociateUserRoleController bean = new AssociateUserRoleController();
        bean.setSupportedMethods("POST");
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setMessageSource(baseConfig.messageSource());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public AgencyController agencyController() {
        AgencyController bean = new AgencyController();
        bean.setSupportedMethods("GET", "POST");
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setValidator(agencyValidator());
        bean.setMessageSource(baseConfig.messageSource());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public CreateUserValidator createUserValidator() {
        return new CreateUserValidator();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public AgencyValidator agencyValidator() {
        return new AgencyValidator();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public RoleValidator roleValidator() {
        return new RoleValidator();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public CreateRejReasonValidator createRejReasonValidator() {
        return new CreateRejReasonValidator();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public CreateQaIndicatorValidator createQaIndicatorValidator() {
        return new CreateQaIndicatorValidator();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public CreateFlagValidator createFlagValidator() {
        return new CreateFlagValidator();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public QaTiSummaryValidator qaTiSummaryValidator() {
        return new QaTiSummaryValidator();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public HarvestNowController harvestNowController() {
        HarvestNowController bean = new HarvestNowController();
        bean.setSupportedMethods("GET", "POST");
        bean.setHarvestCoordinator(baseConfig.harvestCoordinator());
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setValidator(harvestNowValidator());
        bean.setMessageSource(baseConfig.messageSource());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public HarvestNowValidator harvestNowValidator() {
        return new HarvestNowValidator();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public TargetSearchController targetSearchController() {
        TargetSearchController bean = new TargetSearchController();
        bean.setTargetDao(baseConfig.targetDao());
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setCommandClass(TargetSearchCommand.class);
        bean.setTargetManager(baseConfig.targetManager());

        return bean;
    }

    @Bean
    public PermissionPopupController permissionPopupController() {
        PermissionPopupController bean = new PermissionPopupController();
        bean.setCommandClass(PermissionPopupCommand.class);
        bean.setTargetManager(baseConfig.targetManager());

        return bean;
    }

    //  Target Controller and Tab Configuration
    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public TabbedTargetController targetController() {
        TabbedTargetController bean = new TabbedTargetController();
        bean.setSupportedMethods("GET", "POST");
        bean.setTabConfig(targetTabConfig());
        bean.setDefaultCommandClass(TargetDefaultCommand.class);
        bean.setTargetManager(baseConfig.targetManager());
        bean.setBusinessObjectFactory(baseConfig.businessObjectFactory());
        bean.setSearchController(targetSearchController());
        bean.setAuthorityManager(baseConfig.authorityManager());

        return bean;
    }

    @Bean
    public TargetGeneralValidator targetGeneralValidator() {
        return new TargetGeneralValidator();
    }

    @Bean
    public TargetSeedsValidator targetSeedsValidator() {
        return new TargetSeedsValidator();
    }

    @Bean
    public TargetAccessValidator targetAccessValidator() {
        return new TargetAccessValidator();
    }

    @Bean
    public OverrideGetter targetOverrideGetter() {
        OverrideGetter bean = new OverrideGetter();
        bean.setOverrideableType("Target");

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public TabConfig targetTabConfig() {
        TabConfig bean = new TabConfig();
        bean.setViewName("target");

        List<Tab> tabs = new ArrayList<>();

        Tab theTab = new Tab();
        theTab.setPageId("GENERAL");
        theTab.setTitle("general");
        theTab.setJsp("../target-general.jsp");
        theTab.setCommandClass(TargetGeneralCommand.class);
        theTab.setValidator(targetGeneralValidator());
        theTab.setTabHandler(targetGeneralHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("SEEDS");
        theTab.setTitle("seeds");
        theTab.setJsp("../target-seeds.jsp");
        theTab.setCommandClass(SeedsCommand.class);
        theTab.setValidator(targetSeedsValidator());
        theTab.setTabHandler(targetSeedsHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("PROFILE");
        theTab.setTitle("profile");
        theTab.setJsp("../target-profile.jsp");
        theTab.setCommandClass(ProfileCommand.class);
        theTab.setValidator(targetProfileOverridesValidator());
        theTab.setTabHandler(targetProfileHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("SCHEDULES");
        theTab.setTitle("schedule");
        theTab.setJsp("../target-schedules.jsp");
        theTab.setCommandClass(TargetSchedulesCommand.class);
        theTab.setValidator(new TargetSchedulesValidator());
        theTab.setTabHandler(targetSchedulesHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("ANNOTATIONS");
        theTab.setTitle("annotations");
        theTab.setJsp("../target-annotations.jsp");
        theTab.setCommandClass(TargetAnnotationCommand.class);
        theTab.setValidator(new TargetAnnotationValidator());
        theTab.setTabHandler(targetAnnotationHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("DESCRIPTION");
        theTab.setTitle("description");
        theTab.setJsp("../target-description.jsp");
        theTab.setCommandClass(DescriptionCommand.class);
        theTab.setTabHandler(targetDescriptionHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("GROUPS");
        theTab.setTitle("groups");
        theTab.setJsp("../target-groups.jsp");
        theTab.setCommandClass(TargetGroupsCommand.class);
        theTab.setValidator(new TargetAnnotationValidator());
        theTab.setTabHandler(targetGroupsHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("ACCESS");
        theTab.setTitle("access");
        theTab.setJsp("../target-access.jsp");
        theTab.setCommandClass(TargetAccessCommand.class);
        theTab.setValidator(targetAccessValidator());
        theTab.setTabHandler(targetAccessHandler());
        tabs.add(theTab);

        bean.setTabs(tabs);

        return bean;
    }

    public TargetGeneralHandler targetGeneralHandler() {
        TargetGeneralHandler bean = new TargetGeneralHandler();
        bean.setUserRoleDao(baseConfig.userRoleDAO());
        bean.setTargetManager(baseConfig.targetManager());
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setAutoQAUrl(harvestCoordinatorAutoQAUrl);

        return bean;
    }

    public TargetSeedsHandler targetSeedsHandler() {
        TargetSeedsHandler bean = new TargetSeedsHandler();
        bean.setBusinessObjectFactory(baseConfig.businessObjectFactory());
        bean.setTargetManager(baseConfig.targetManager());
        bean.setValidator(targetSeedsValidator());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setMessageSource(baseConfig.messageSource());

        return bean;
    }

    @Bean
    public ProfilesOverridesValidator targetProfileOverridesValidator() {
        return new ProfilesOverridesValidator();
    }

    public TargetProfileHandler targetProfileHandler() {
        TargetProfileHandler bean = new TargetProfileHandler();
        bean.setProfileManager(baseConfig.profileManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setOverrideGetter(targetOverrideGetter());
        bean.setCredentialUrlPrefix("target");

        return bean;
    }

    public TargetSchedulesHandler targetSchedulesHandler() {
        TargetSchedulesHandler bean = new TargetSchedulesHandler();
        bean.setPatternFactory(baseConfig.schedulePatternFactory());
        bean.setBusinessObjectFactory(baseConfig.businessObjectFactory());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setTargetManager(baseConfig.targetManager());
        bean.setContextSessionKey("targetEditorContext");
        bean.setPrivilegeString("ADD_SCHEDULE_TO_TARGET");
        bean.setEditControllerUrl("curator/target/schedule.html");

        return bean;
    }

    public TargetAnnotationHandler targetAnnotationHandler() {
        TargetAnnotationHandler bean = new TargetAnnotationHandler();
        bean.setTargetManager(baseConfig.targetManager());
        bean.setSelectionTypesList(listsConfig.selectionTypesList());
        bean.setHarvestTypesList(listsConfig.harvestTypesList());

        return bean;
    }

    public TargetDescriptionHandler targetDescriptionHandler() {
        TargetDescriptionHandler bean = new TargetDescriptionHandler();
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setTypeList(listsConfig.dublinCoreTypesList());

        return bean;
    }

    public TargetGroupsHandler targetGroupsHandler() {
        TargetGroupsHandler bean = new TargetGroupsHandler();
        bean.setTargetManager(baseConfig.targetManager());
        bean.setSubGroupSeparator(groupTypesSubgroupSeparator);

        return bean;
    }

    public TargetAccessHandler targetAccessHandler() {
        TargetAccessHandler bean = new TargetAccessHandler();
        bean.setAuthorityManager(baseConfig.authorityManager());

        return bean;
    }

    @Bean
    public EditScheduleController targetEditScheduleController() {
        EditScheduleController bean = new EditScheduleController();
        bean.setPatternFactory(baseConfig.schedulePatternFactory());
        bean.setBusinessObjectFactory(baseConfig.businessObjectFactory());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setContextSessionKey("targetEditorContext");
        bean.setScheduleEditPrivilege("ADD_SCHEDULE_TO_TARGET");
        bean.setTargetController(targetController());
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setHeatmapConfigDao(baseConfig.heatmapConfigDao());
        bean.setViewPrefix("target");
        bean.setValidator(new TargetSchedulesValidator());

        return bean;
    }

    @Bean
    public ProfileBasicCredentialsController basicCredentialsControllerTarget() {
        ProfileBasicCredentialsController bean = new ProfileBasicCredentialsController();
        bean.setCommandClass(BasicCredentialsCommand.class);
        bean.setTabbedController(targetController());
        bean.setOverrideGetter(targetOverrideGetter());
        bean.setUrlPrefix("target");
        bean.setValidator(new ProfilesBasicCredentialsValidator());

        return bean;
    }

    @Bean
    public ProfileFormCredentialsController formCredentialsControllerTarget() {
        ProfileFormCredentialsController bean = new ProfileFormCredentialsController();
        bean.setCommandClass(FormCredentialsCommand.class);
        bean.setTabbedController(targetController());
        bean.setOverrideGetter(targetOverrideGetter());
        bean.setUrlPrefix("target");
        bean.setValidator(new ProfilesFormCredentialsValidator());

        return bean;
    }

    @Bean
    public ProfileBasicCredentialsController basicCredentialsControllerTargetInstance() {
        ProfileBasicCredentialsController bean = new ProfileBasicCredentialsController();
        bean.setCommandClass(BasicCredentialsCommand.class);
        bean.setTabbedController(tabbedTargetInstanceController());
        bean.setOverrideGetter(targetInstanceOverrideGetter());
        bean.setUrlPrefix("ti");
        bean.setValidator(basicCredentialsValidatorti());

        return bean;
    }

    @Bean
    public ProfilesBasicCredentialsValidator basicCredentialsValidatorti() {
        return new ProfilesBasicCredentialsValidator();
    }

    @Bean
    public ProfileFormCredentialsController formCredentialsControllerTargetInstance() {
        ProfileFormCredentialsController bean = new ProfileFormCredentialsController();
        bean.setCommandClass(FormCredentialsCommand.class);
        bean.setTabbedController(tabbedTargetInstanceController());
        bean.setOverrideGetter(targetInstanceOverrideGetter());
        bean.setUrlPrefix("ti");
        bean.setValidator(formCredentialsValidatorti());

        return bean;
    }

    @Bean
    public ProfilesFormCredentialsValidator formCredentialsValidatorti() {
        return new ProfilesFormCredentialsValidator();
    }

    @Bean
    public ProfileBasicCredentialsController basicCredentialsControllerGroup() {
        ProfileBasicCredentialsController bean = new ProfileBasicCredentialsController();
        bean.setCommandClass(BasicCredentialsCommand.class);
        bean.setTabbedController(groupsController());
        bean.setOverrideGetter(groupOverrideGetter());
        bean.setUrlPrefix("group");
        bean.setValidator(basicCredentialsValidatorGroup());

        return bean;
    }

    @Bean
    public ProfilesBasicCredentialsValidator basicCredentialsValidatorGroup() {
        return new ProfilesBasicCredentialsValidator();
    }

    @Bean
    public ProfileFormCredentialsController formCredentialsControllerGroup() {
        ProfileFormCredentialsController bean = new ProfileFormCredentialsController();
        bean.setCommandClass(FormCredentialsCommand.class);
        bean.setTabbedController(groupsController());
        bean.setOverrideGetter(groupOverrideGetter());
        bean.setUrlPrefix("group");
        bean.setValidator(formCredentialsValidatorGroup());

        return bean;
    }

    @Bean
    public ProfilesFormCredentialsValidator formCredentialsValidatorGroup() {
        return new ProfilesFormCredentialsValidator();
    }

    // Groups Controller and Tab Configuration

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public TabbedGroupController groupsController() {
        TabbedGroupController bean = new TabbedGroupController();
        bean.setSupportedMethods("GET", "POST");
        bean.setTabConfig(groupsTabConfig());
        bean.setDefaultCommandClass(DefaultCommand.class);
        bean.setTargetManager(baseConfig.targetManager());
        bean.setBusinessObjectFactory(baseConfig.businessObjectFactory());
        bean.setSearchController(baseConfig.groupSearchController());
        bean.setMessageSource(baseConfig.messageSource());
        bean.setAuthorityManager(baseConfig.authorityManager());

        return bean;
    }

    @Bean
    public OverrideGetter groupOverrideGetter() {
        OverrideGetter bean = new OverrideGetter();
        bean.setOverrideableType("Target Group");

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public TabConfig groupsTabConfig() {
        TabConfig bean = new TabConfig();
        bean.setViewName("groups");

        List<Tab> tabs = new ArrayList<>();

        Tab theTab = new Tab();
        theTab.setPageId("GENERAL");
        theTab.setTitle("general");
        theTab.setJsp("../groups-general.jsp");
        theTab.setCommandClass(GeneralCommand.class);
        theTab.setValidator(new GeneralValidator());
        theTab.setTabHandler(generalHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("MEMBERS");
        theTab.setTitle("members");
        theTab.setJsp("../groups-members.jsp");
        theTab.setCommandClass(MembersCommand.class);
        theTab.setValidator(new MembersValidator());
        theTab.setTabHandler(membersHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("MEMBEROF");
        theTab.setTitle("memberof");
        theTab.setJsp("../groups-memberof.jsp");
        theTab.setCommandClass(MemberOfCommand.class);
        theTab.setValidator(null);
        theTab.setTabHandler(memberOfHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("PROFILE");
        theTab.setTitle("profile");
        theTab.setJsp("../target-profile.jsp");
        theTab.setCommandClass(ProfileCommand.class);
        theTab.setValidator(groupsProfileOverridesValidator());
        theTab.setTabHandler(groupsProfileHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("SCHEDULES");
        theTab.setTitle("schedule");
        theTab.setJsp("../target-schedules.jsp");
        theTab.setCommandClass(TargetSchedulesCommand.class);
        theTab.setValidator(new TargetSchedulesValidator());
        theTab.setTabHandler(groupsTabTargetSchedulesHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("ANNOTATIONS");
        theTab.setTitle("annotations");
        theTab.setJsp("../group-annotations.jsp");
        theTab.setCommandClass(GroupAnnotationCommand.class);
        theTab.setValidator(new GroupAnnotationValidator());
        theTab.setTabHandler(groupAnnotationHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("DESCRIPTION");
        theTab.setTitle("description");
        theTab.setJsp("../target-description.jsp");
        theTab.setCommandClass(DescriptionCommand.class);
        theTab.setTabHandler(descriptionHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("ACCESS");
        theTab.setTitle("access");
        theTab.setJsp("../target-access.jsp");
        theTab.setCommandClass(TargetAccessCommand.class);
        theTab.setValidator(targetAccessValidator());
        theTab.setTabHandler(accessHandler());
        tabs.add(theTab);

        bean.setTabs(tabs);

        return bean;
    }

    public GeneralHandler generalHandler() {
        GeneralHandler bean = new GeneralHandler();
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setTargetManager(baseConfig.targetManager());
        bean.setGroupTypesList(listsConfig.groupTypesList());
        bean.setSubGroupTypeName(groupTypesSubgroup);
        bean.setSubGroupSeparator(groupTypesSubgroupSeparator);

        return bean;
    }

    public MembersHandler membersHandler() {
        MembersHandler bean = new MembersHandler();
        bean.setTargetManager(baseConfig.targetManager());
        bean.setSubGroupSeparator(groupTypesSubgroupSeparator);

        return bean;
    }

    public MemberOfHandler memberOfHandler() {
        MemberOfHandler bean = new MemberOfHandler();
        bean.setTargetManager(baseConfig.targetManager());
        bean.setSubGroupSeparator(groupTypesSubgroupSeparator);

        return bean;
    }

    @Bean
    public ProfilesOverridesValidator groupsProfileOverridesValidator() {
        return new ProfilesOverridesValidator();
    }

    public GroupsProfileHandler groupsProfileHandler() {
        GroupsProfileHandler bean = new GroupsProfileHandler();
        bean.setProfileManager(baseConfig.profileManager());
        bean.setOverrideGetter(groupOverrideGetter());
        bean.setCredentialUrlPrefix("group");

        return bean;
    }

    public TargetSchedulesHandler groupsTabTargetSchedulesHandler() {
        TargetSchedulesHandler bean = new TargetSchedulesHandler();
        bean.setPatternFactory(baseConfig.schedulePatternFactory());
        bean.setBusinessObjectFactory(baseConfig.businessObjectFactory());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setTargetManager(baseConfig.targetManager());
        bean.setContextSessionKey("groupEditorContext");
        bean.setPrivilegeString("MANAGE_GROUP_SCHEDULE");
        bean.setEditControllerUrl("curator/groups/schedule.html");

        return bean;
    }

    public GroupAnnotationHandler groupAnnotationHandler() {
        GroupAnnotationHandler bean = new GroupAnnotationHandler();
        bean.setTargetManager(baseConfig.targetManager());

        return bean;
    }

    public DescriptionHandler descriptionHandler() {
        DescriptionHandler bean = new DescriptionHandler();
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setTypeList(listsConfig.dublinCoreTypesList());

        return bean;
    }

    public AccessHandler accessHandler() {
        AccessHandler bean = new AccessHandler();
        bean.setAuthorityManager(baseConfig.authorityManager());

        return bean;
    }

    @Bean
    public EditScheduleController groupsEditScheduleController() {
        EditScheduleController bean = new EditScheduleController();
        bean.setPatternFactory(baseConfig.schedulePatternFactory());
        bean.setBusinessObjectFactory(baseConfig.businessObjectFactory());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setContextSessionKey("groupEditorContext");
        bean.setScheduleEditPrivilege("MANAGE_GROUP_SCHEDULE");
        bean.setTargetController(groupsController());
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setHeatmapConfigDao(baseConfig.heatmapConfigDao());
        bean.setViewPrefix("groups");
        bean.setValidator(new TargetSchedulesValidator());

        return bean;
    }

    @Bean
    public ArchiveController submitToArchiveController() {
        ArchiveController bean = new ArchiveController();
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setTargetManager(baseConfig.targetManager());
        bean.setSipBuilder(baseConfig.sipBuilder());
        bean.setArchiveAdapter(baseConfig.archiveAdapter());
        bean.setHeritrixVersion("Heritrix " + heritrixVersion);
        bean.setWebCuratorUrl("http://dia-nz.github.io/webcurator/schemata/webcuratortool-1.0.dtd");

        return bean;
    }

    @Bean
    public CustomDepositFormController customDepositFormController() {
        return new CustomDepositFormController();
    }

    @Bean
    public TestArchiveController testArchiveController() {
        TestArchiveController bean = new TestArchiveController();
        // NOTE inherits from submitToArchiveController
        // But none if its access methods are public, so we're just going to replicate the values that get set
        // as 'parent' is not replicated in Spring beans annotations.
        ArchiveController submitToArchiveController = submitToArchiveController();
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setTargetManager(baseConfig.targetManager());
        bean.setSipBuilder(baseConfig.sipBuilder());
        bean.setArchiveAdapter(baseConfig.archiveAdapter());
        bean.setHeritrixVersion("Heritrix " + heritrixVersion);
        bean.setWebCuratorUrl("http://dia-nz.github.io/webcurator/schemata/webcuratortool-1.0.dtd");

        return bean;
    }

    @Bean
    public AddMembersController addMembersController() {
        AddMembersController bean = new AddMembersController();
        bean.setTargetManager(baseConfig.targetManager());
        bean.setGroupsController(groupsController());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setValidator(addMembersValidator());

        return bean;
    }

    @Bean
    public AddMembersValidator addMembersValidator() {
        return new AddMembersValidator();
    }

    @Bean
    public org.webcurator.ui.groups.controller.AddParentsController groupAddParentsController() {
        org.webcurator.ui.groups.controller.AddParentsController bean = new org.webcurator.ui.groups.controller.AddParentsController();
        bean.setTargetManager(baseConfig.targetManager());
        bean.setGroupsController(groupsController());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setValidator(addParentsValidator());

        return bean;
    }

    @Bean
    public AddParentsValidator addParentsValidator() {
        return new AddParentsValidator();
    }

    @Bean
    public MoveTargetsController moveTargetsController() {
        MoveTargetsController bean = new MoveTargetsController();
        bean.setTargetManager(baseConfig.targetManager());
        bean.setGroupsController(groupsController());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setValidator(moveTargetsValidator());

        return bean;
    }

    @Bean
    public MoveTargetsValidator moveTargetsValidator() {
        return new MoveTargetsValidator();
    }

    @Bean
    public AddParentsController addParentsController() {
        AddParentsController bean = new AddParentsController();
        bean.setTargetManager(baseConfig.targetManager());
        bean.setTargetController(targetController());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setSubGroupSeparator(groupTypesSubgroupSeparator);
        bean.setValidator(targetAddParentsValidator());

        return bean;
    }

    @Bean
    public org.webcurator.ui.target.validator.AddParentsValidator targetAddParentsValidator() {
        return new org.webcurator.ui.target.validator.AddParentsValidator();
    }

    // Profile Controller and Tab Configuration

    @Bean
    public ProfileListController profileListController() {
        ProfileListController bean = new ProfileListController();
        bean.setSupportedMethods("GET", "POST");
        bean.setProfileManager(baseConfig.profileManager());
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setAuthorityManager(baseConfig.authorityManager());

        return bean;
    }

    @Bean
    public ProfileTargetsController profileTargetsController() {
        ProfileTargetsController bean = new ProfileTargetsController();
        bean.setSupportedMethods("GET", "POST");
        bean.setProfileManager(baseConfig.profileManager());
        bean.setAuthorityManager(baseConfig.authorityManager());
        bean.setTargetManager(baseConfig.targetManager());
        bean.setTargetDao(baseConfig.targetDao());

        return bean;
    }

    @Bean
    public ProfileViewController profileViewController() {
        ProfileViewController bean = new ProfileViewController();
        bean.setProfileManager(baseConfig.profileManager());
        bean.setAuthorityManager(baseConfig.authorityManager());

        return bean;
    }

    @Bean
    public H3ScriptConsoleController h3ScriptConsoleController() {
        // name of the directory where the h3 scripts are stored
        H3ScriptConsoleController bean = new H3ScriptConsoleController();
        bean.setH3ScriptsDirectory(h3ScriptsDirectory);
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setAuthorityManager(baseConfig.authorityManager());

        return bean;
    }

    @Bean
    public H3ScriptFileController h3ScriptFileController() {
        // name of the directory where the h3 scripts are stored
        H3ScriptFileController bean = new H3ScriptFileController();
        bean.setH3ScriptsDirectory(h3ScriptsDirectory);
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setAuthorityManager(baseConfig.authorityManager());

        return bean;
    }

    @Bean
    public DeleteProfileController profileDeleteController() {
        DeleteProfileController bean = new DeleteProfileController();
        bean.setProfileManager(baseConfig.profileManager());
        bean.setMessageSource(baseConfig.messageSource());
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setAuthorityManager(baseConfig.authorityManager());

        return bean;
    }

    @Bean
    public MakeDefaultProfileController makeDefaultProfileController() {
        MakeDefaultProfileController bean = new MakeDefaultProfileController();
        bean.setProfileManager(baseConfig.profileManager());
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setAuthorityManager(baseConfig.authorityManager());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public ProfileController profileController() {
        ProfileController bean = new ProfileController();
        bean.setSupportedMethods("GET", "POST");
        bean.setTabConfig(profileTabConfig());
        bean.setDefaultCommandClass(DefaultCommand.class);
        bean.setProfileManager(baseConfig.profileManager());
        bean.setAuthorityManager(baseConfig.authorityManager());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public ProfileController profileH3Controller() {
        ProfileController bean = new ProfileController();
        bean.setSupportedMethods("GET", "POST");
        bean.setTabConfig(profileH3TabConfig());
        bean.setDefaultCommandClass(DefaultCommand.class);
        bean.setProfileManager(baseConfig.profileManager());
        bean.setAuthorityManager(baseConfig.authorityManager());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public ProfileController importedProfileH3Controller() {
        ProfileController bean = new ProfileController();
        bean.setSupportedMethods("GET", "POST");
        bean.setTabConfig(importedProfileH3TabConfig());
        bean.setDefaultCommandClass(DefaultCommand.class);
        bean.setProfileManager(baseConfig.profileManager());
        bean.setAuthorityManager(baseConfig.authorityManager());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public TabConfig profileTabConfig() {
        TabConfig bean = new TabConfig();
        bean.setViewName("profile");

        List<Tab> tabs = new ArrayList<>();

        Tab theTab = new Tab();
        theTab.setPageId("GENERAL");
        theTab.setTitle("general");
        theTab.setJsp("../profile-general.jsp");
        theTab.setCommandClass(GeneralCommand.class);
        theTab.setValidator(new ProfileGeneralValidator());
        theTab.setTabHandler(new ProfileGeneralHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("BASE");
        theTab.setTitle("base");
        theTab.setJsp("../profile-page-view.jsp");
        theTab.setCommandClass(EmptyCommand.class);
        theTab.setValidator(null);
        HeritrixProfileHandler tabHandler = heritrixProfileHandler("/crawl-order");
        tabHandler.setRecursionFilter(new GeneralOnlyRendererFilter());
        theTab.setTabHandler(tabHandler);
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("SCOPE");
        theTab.setTitle("scope");
        theTab.setJsp("../profile-page-view.jsp");
        theTab.setCommandClass(EmptyCommand.class);
        theTab.setValidator(null);
        theTab.setTabHandler(heritrixProfileHandler("/crawl-order/scope"));
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("FRONTIER");
        theTab.setTitle("frontier");
        theTab.setJsp("../profile-page-view.jsp");
        theTab.setCommandClass(EmptyCommand.class);
        theTab.setValidator(null);
        theTab.setTabHandler(heritrixProfileHandler("/crawl-order/frontier"));
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("PREFETCH");
        theTab.setTitle("prefetchers");
        theTab.setJsp("../profile-page-view.jsp");
        theTab.setCommandClass(EmptyCommand.class);
        theTab.setValidator(null);
        theTab.setTabHandler(heritrixProfileHandler("/crawl-order/pre-fetch-processors"));
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("FETCH");
        theTab.setTitle("fetchers");
        theTab.setJsp("../profile-page-view.jsp");
        theTab.setCommandClass(EmptyCommand.class);
        theTab.setValidator(null);
        theTab.setTabHandler(heritrixProfileHandler("/crawl-order/fetch-processors"));
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("EXTRACT");
        theTab.setTitle("extractors");
        theTab.setJsp("../profile-page-view.jsp");
        theTab.setCommandClass(EmptyCommand.class);
        theTab.setValidator(null);
        theTab.setTabHandler(heritrixProfileHandler("/crawl-order/extract-processors"));
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("WRITE");
        theTab.setTitle("writers");
        theTab.setJsp("../profile-page-view.jsp");
        theTab.setCommandClass(EmptyCommand.class);
        theTab.setValidator(null);
        theTab.setTabHandler(heritrixProfileHandler("/crawl-order/write-processors"));
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("POST");
        theTab.setTitle("postprocessors");
        theTab.setJsp("../profile-page-view.jsp");
        theTab.setCommandClass(EmptyCommand.class);
        theTab.setValidator(null);
        theTab.setTabHandler(heritrixProfileHandler("/crawl-order/post-processors"));
        tabs.add(theTab);

        bean.setTabs(tabs);

        return bean;
    }

    public HeritrixProfileHandler heritrixProfileHandler(String baseAttribute) {
        HeritrixProfileHandler bean = new HeritrixProfileHandler();
        bean.setBaseAttribute(baseAttribute);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public TabConfig profileH3TabConfig() {
        TabConfig bean = new TabConfig();
        bean.setViewName("profileH3");

        List<Tab> tabs = new ArrayList<>();

        Tab theTab = new Tab();
        theTab.setPageId("GENERAL");
        theTab.setTitle("general");
        theTab.setJsp("../profile-general.jsp");
        theTab.setCommandClass(GeneralCommand.class);
        theTab.setValidator(new ProfileGeneralValidator());
        theTab.setTabHandler(new ProfileGeneralHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("SCOPE-IMPORTED");
        theTab.setTitle("scope");
        theTab.setJsp("../imported-profileH3-scope.jsp");
        theTab.setCommandClass(ImportedHeritrix3ProfileCommand.class);
        theTab.setValidator(new ImportedHeritrix3ProfileValidator());
        theTab.setTabHandler(new ImportedHeritrix3ProfileHandler());
        tabs.add(theTab);

        bean.setTabs(tabs);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public TabConfig importedProfileH3TabConfig() {
        TabConfig bean = new TabConfig();
        bean.setViewName("imported-profileH3");

        List<Tab> tabs = new ArrayList<>();

        Tab theTab = new Tab();
        theTab.setPageId("GENERAL");
        theTab.setTitle("general");
        theTab.setJsp("../profile-general.jsp");
        theTab.setCommandClass(GeneralCommand.class);
        theTab.setValidator(new ProfileGeneralValidator());
        theTab.setTabHandler(new ProfileGeneralHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("SCOPE");
        theTab.setTitle("scope");
        theTab.setJsp("../profileH3-scope.jsp");
        theTab.setCommandClass(Heritrix3ProfileCommand.class);
        theTab.setValidator(new Heritrix3ProfileValidator());
        theTab.setTabHandler(new Heritrix3ProfileHandler());
        tabs.add(theTab);

        bean.setTabs(tabs);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public LogReaderController logReaderController() {
        LogReaderController bean = new LogReaderController();
        bean.setHarvestCoordinator(baseConfig.harvestCoordinator());
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public ContentReaderController contentReaderController() {
        ContentReaderController bean = new ContentReaderController();
        bean.setHarvestLogManager(baseConfig.harvestLogManager());
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public AQAReaderController aqaReaderController() {
        AQAReaderController bean = new AQAReaderController();
        bean.setHarvestCoordinator(baseConfig.harvestCoordinator());
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public LiveContentRetrieverController liveContentRetrieverController() {
        return new LiveContentRetrieverController();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public LogRetrieverController logRetrieverController() {
        LogRetrieverController bean = new LogRetrieverController();
        bean.setHarvestLogManager(baseConfig.harvestLogManager());
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public InTrayController inTrayController() {
        InTrayController bean = new InTrayController();
        bean.setInTrayManager(baseConfig.inTrayManager());

        return bean;
    }

    // Reporting

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public ReportController reportController() {
        ReportController bean = new ReportController();
        bean.setSupportedMethods("GET", "POST");
        // TODO CONFIGURATION There doesn't seem to be a reportMngr bean anywhere...
        bean.setReportMngr(null); // in the XML it's shown as 'ref bean="reportMngr"', but that doesn't exist anywhere.
        bean.setCommandName("reportCommand");
        bean.setCommandClass(ReportCommand.class);
        bean.setValidator(new ReportValidator());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public ReportPreviewController reportPreviewController() {
        ReportPreviewController bean = new ReportPreviewController();
        bean.setSupportedMethods("GET", "POST");
        bean.setCommandName("reportPreviewCommand");
        bean.setCommandClass(ReportPreviewCommand.class);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public ReportSaveController reportSaveController() {
        ReportSaveController bean = new ReportSaveController();
        bean.setSupportedMethods("GET", "POST");
        bean.setCommandName("reportSaveCommand");
        bean.setCommandClass(ReportSaveCommand.class);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public ReportEmailController reportEmailController() {
        ReportEmailController bean = new ReportEmailController();
        bean.setSupportedMethods("GET", "POST");
        bean.setCommandName("reportEmailCommand");
        bean.setCommandClass(ReportEmailCommand.class);
        bean.setMailServer(baseConfig.mailServer());

        return bean;
    }


    // Management Controllers

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public ManagementController managementController() {
        ManagementController bean = new ManagementController();
        bean.setSupportedMethods("GET");
        bean.setEnableQaModule(queueControllerEnableQaModule);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public TemplateController templateController() {
        TemplateController bean = new TemplateController();
        bean.setSupportedMethods("GET", "POST");
        bean.setPermissionTemplateManager(baseConfig.permissionTemplateManager());
        bean.setAgencyUserManager(baseConfig.agencyUserManager());
        bean.setValidator(templateValidator());
        bean.setMessageSource(baseConfig.messageSource());
        bean.setDefaultSubject("Web Preservation Programme");

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public TemplateValidator templateValidator() {
        return new TemplateValidator();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public SiteAgencyController siteAgencyController() {
        SiteAgencyController bean = new SiteAgencyController();
        bean.setSiteController(siteController());
        bean.setBusObjFactory(baseConfig.businessObjectFactory());
        bean.setValidator(siteAgencyValidator());

        return bean;
    }

    @Bean
    public SiteAgencyValidator siteAgencyValidator() {
        SiteAgencyValidator bean = new SiteAgencyValidator();
        bean.setSiteManager(baseConfig.siteManager());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public SitePermissionController sitePermissionController() {
        SitePermissionController bean = new SitePermissionController();
        bean.setSiteController(siteController());
        bean.setBusinessObjectFactory(baseConfig.businessObjectFactory());
        bean.setValidator(sitePermissionValidator());

        return bean;
    }

    @Bean
    public SitePermissionValidator sitePermissionValidator() {
        return new SitePermissionValidator();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public AssignToHarvesterController assignToHarvesterController() {
        AssignToHarvesterController bean = new AssignToHarvesterController();
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setHarvestCoordinator(baseConfig.harvestCoordinator());

        return bean;
    }



































































































}
