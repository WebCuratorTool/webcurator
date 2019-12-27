package org.webcurator.webapp.beans.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.*;
import org.springframework.orm.hibernate5.support.OpenSessionInViewInterceptor;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesView;
import org.webcurator.common.ui.profiles.renderers.GeneralOnlyRendererFilter;
import org.webcurator.core.harvester.coordinator.HarvestCoordinator;
import org.webcurator.ui.groups.command.GeneralCommand;
import org.webcurator.ui.groups.command.GroupAnnotationCommand;
import org.webcurator.ui.groups.command.MemberOfCommand;
import org.webcurator.ui.groups.command.MembersCommand;
import org.webcurator.ui.groups.controller.*;
import org.webcurator.ui.groups.validator.GeneralValidator;
import org.webcurator.ui.groups.validator.GroupAnnotationValidator;
import org.webcurator.ui.groups.validator.MembersValidator;
import org.webcurator.ui.profiles.command.Heritrix3ProfileCommand;
import org.webcurator.ui.profiles.command.ImportedHeritrix3ProfileCommand;
import org.webcurator.ui.profiles.controller.Heritrix3ProfileHandler;
import org.webcurator.ui.profiles.controller.HeritrixProfileHandler;
import org.webcurator.ui.profiles.controller.ImportedHeritrix3ProfileHandler;
import org.webcurator.ui.profiles.controller.ProfileGeneralHandler;
import org.webcurator.ui.profiles.validator.Heritrix3ProfileValidator;
import org.webcurator.ui.profiles.validator.ImportedHeritrix3ProfileValidator;
import org.webcurator.ui.profiles.validator.ProfileGeneralValidator;
import org.webcurator.ui.site.command.SiteAuthorisingAgencyCommand;
import org.webcurator.ui.site.command.SiteCommand;
import org.webcurator.ui.site.command.SitePermissionCommand;
import org.webcurator.ui.site.command.UrlCommand;
import org.webcurator.ui.site.controller.SiteAuthorisingAgencyHandler;
import org.webcurator.ui.site.controller.SiteGeneralHandler;
import org.webcurator.ui.site.controller.SitePermissionHandler;
import org.webcurator.ui.site.controller.SiteUrlHandler;
import org.webcurator.ui.site.validator.SiteURLsValidator;
import org.webcurator.ui.site.validator.SiteValidator;
import org.webcurator.ui.target.command.*;
import org.webcurator.ui.target.controller.*;
import org.webcurator.ui.target.validator.*;
import org.webcurator.ui.util.EmptyCommand;
import org.webcurator.ui.util.OverrideGetter;
import org.webcurator.ui.util.Tab;
import org.webcurator.ui.util.TabConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Contains configuration that used to be found in {@code wct-core-servlet.xml}. This
 * is part of the change to move to using annotations for Spring instead of
 * XML files.
 *
 */
@Configuration
@PropertySource(value = "classpath:wct-webapp.properties")
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
    private TargetSeedsValidator targetSeedsValidator;

    @Autowired
    private TargetGeneralValidator targetGeneralValidator;

    @Autowired
    private TargetAccessValidator targetAccessValidator;

    @Autowired
    private ImportedHeritrix3ProfileValidator importedHeritrix3ProfileValidator;

    @Autowired
    private HarvestCoordinator harvestCoordinator;

    @Autowired
    private Heritrix3ProfileHandler heritrix3ProfileHandler;

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public SimpleUrlHandlerMapping simpleUrlMapping() {
        SimpleUrlHandlerMapping bean = new SimpleUrlHandlerMapping();
        bean.setInterceptors(new Object[] { openSessionInViewInterceptor() });

        // Make sure this overrides Spring's default handler
        bean.setOrder(0);

        Properties mappings = new Properties();
//        mappings.put("/curator/site/site.html", "siteController");
//        mappings.put("/curator/site/search.html", "siteSearchController");
//        mappings.put("/curator/site/agencies.html", "siteAgencyController");
//        mappings.put("/curator/site/site-auth-agency-search.html", "siteAgencySearchController");
//        mappings.put("/curator/site/permissions.html", "sitePermissionController");
//        mappings.put("/curator/site/transfer.html", "transferSeedsController");
//        mappings.put("/curator/site/generate.html", "generatePermissionTemplateController");
        //mappings.put("/curator/agent/harvest-agent.html", "manageHarvestAgentController");
        //mappings.put("/curator/agent/bandwidth-restrictions.html", "bandwidthRestrictionsController");
        mappings.put("/curator/tools/treetool.html", "treeToolController");
        mappings.put("/curator/tools/treetoolAJAX.html", "treeToolControllerAJAX");
        mappings.put("/curator/tools/harvest-history.html", "harvestHistoryController");
        //mappings.put("/curator/credentials/reset-password.html", "resetPasswordController");
        mappings.put("/curator/target/quality-review-toc.html", "qualityReviewToolController");
        mappings.put("/curator/target/deposit-form-envelope.html", "customDepositFormController");
        mappings.put("/curator/logout.html", "logoutController");
        mappings.put("/curator/home.html", "homeController");
        //mappings.put("/curator/target/queue.html", "queueController");
        // mapped via @RequestMapping mappings.put("/curator/target/qatisummary.html", "qaTiSummaryController");
        mappings.put("/curator/target/qa-indicator-report.html", "qaIndicatorReportController");
        mappings.put("/curator/target/qa-indicator-robots-report.html", "qaIndicatorRobotsReportController");
        //mappings.put("/curator/target/annotation-ajax.html", "annotationAjaxController");
//        mappings.put("/curator/target/target-instance.html", "tabbedTargetInstanceController");
        //mappings.put("/curator/target/harvest-now.html", "harvestNowController");
        //mappings.put("/curator/target/target.html", "targetController");
//        mappings.put("/curator/target/search.html", "targetSearchController");
        //mappings.put("/curator/target/schedule.html", "targetEditScheduleController");
        //mappings.put("/curator/targets/add-parents.html", "addParentsController");
        //mappings.put("/curator/admin/role.html", "roleController");
        //mappings.put("/curator/profiles/profiles.html", "profileController");
        //mappings.put("/curator/profiles/profilesH3.html", "profileH3Controller");
        //mappings.put("/curator/profiles/imported-profilesH3.html", "importedProfileH3Controller");
        //mappings.put("/curator/profiles/profiletargets.html", "profileTargetsController");
        //mappings.put("/curator/profiles/list.html", "profileListController");
        //mappings.put("/curator/profiles/view.html", "profileViewController");
        //mappings.put("/curator/profiles/delete.html", "profileDeleteController");
        //mappings.put("/curator/profiles/make-default.html", "makeDefaultProfileController");
        mappings.put("/curator/admin/rejreason.html", "rejReasonController");
        mappings.put("/curator/admin/create-rejreason.html", "createRejReasonController");
        mappings.put("/curator/admin/qaindicators.html", "qaIndicatorController");
        mappings.put("/curator/admin/create-qaindicator.html", "createQaIndicatorController");
        mappings.put("/curator/admin/flags.html", "flagController");
        mappings.put("/curator/admin/create-flag.html", "createFlagController");
        //mappings.put("/curator/admin/user.html", "userController");
        //mappings.put("/curator/admin/create-user.html", "createUserController");
        //mappings.put("/curator/admin/associate-userroles.html", "associateUserRoleController");
        //mappings.put("/curator/admin/agency.html", "agencyController");
        //mappings.put("/curator/admin/change-password.html", "changePasswordController");
        //mappings.put("/curator/admin/management.html", "managementController");
        //mappings.put("/curator/admin/templates.html", "templateController");
        //mappings.put("/curator/target/log-viewer.html", "logReaderController");
        //mappings.put("/curator/target/content-viewer.html", "contentReaderController");
        mappings.put("/curator/target/live-content-retriever.html", "liveContentRetrieverController");
        //mappings.put("/curator/target/aqa-viewer.html", "aqaReaderController");
        //mappings.put("/curator/target/log-retriever.html", "logRetrieverController");
        //mappings.put("/curator/target/show-hop-path.html", "showHopPathController");
        //mappings.put("/curator/target/permission-popup.html", "permissionPopupController");
        //mappings.put("/curator/target/target-basic-credentials.html", "basicCredentialsControllerTarget");
        //mappings.put("/curator/target/target-form-credentials.html", "formCredentialsControllerTarget");
        //mappings.put("/curator/target/ti-basic-credentials.html", "basicCredentialsControllerTargetInstance");
        //mappings.put("/curator/target/ti-form-credentials.html", "formCredentialsControllerTargetInstance");
        mappings.put("/curator/target/h3ScriptConsole.html", "h3ScriptConsoleController");
        mappings.put("/curator/target/h3ScriptFile.html", "h3ScriptFileController");
//        mappings.put("/curator/intray/intray.html", "inTrayController");
        mappings.put("/curator/report/report.html", "reportController");
        mappings.put("/curator/report/report-preview.html", "reportPreviewController");
        mappings.put("/curator/report/report-save.html", "reportSaveController");
        mappings.put("/curator/report/report-email.html", "reportEmailController");
//        mappings.put("/curator/groups/search.html", "groupSearchController");
        //mappings.put("/curator/groups/groups.html", "groupsController");
        mappings.put("/curator/groups/add-members.html", "addMembersController");
        //mappings.put("/curator/groups/schedule.html", "groupsEditScheduleController");
        //mappings.put("/curator/groups/add-parents.html", "groupAddParentsController");
        mappings.put("/curator/groups/move-targets.html", "moveTargetsController");
        //mappings.put("/curator/archive/submit.html", "submitToArchiveController");
        mappings.put("/curator/archive/test.html", "testArchiveController");
        //mappings.put("/curator/target/group-basic-credentials.html", "basicCredentialsControllerGroup");
        //mappings.put("/curator/target/group-form-credentials.html", "formCredentialsControllerGroup");
        //mappings.put("/curator/target/ti-harvest-now.html", "assignToHarvesterController");
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
    public SimpleMappingExceptionResolver exceptionResolver() {
        SimpleMappingExceptionResolver bean = new SimpleMappingExceptionResolver();
        bean.setDefaultErrorView("Error");

        Properties exceptionMappings = new Properties();
        exceptionMappings.setProperty("org.springframework.orm.hibernate5.HibernateObjectRetrievalFailureException",
                "NoObjectFound");
        // TODO Is this property even used?
        exceptionMappings.setProperty("org.springframework.web.multipart.MaxUploadSizeExceededException",
                "max-file-size-exceeded");
        bean.setExceptionMappings(exceptionMappings);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
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
        bean.setHarvestCoordinator(harvestCoordinator);
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
        bean.setHarvestCoordinator(harvestCoordinator);

        return bean;
    }

    public TargetInstanceLogsHandler targetInstanceLogsHandler() {
        TargetInstanceLogsHandler bean = new TargetInstanceLogsHandler();
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setHarvestCoordinator(harvestCoordinator);

        return bean;
    }

    public TargetInstanceResultHandler targetInstanceResultHandler() {
        TargetInstanceResultHandler bean = new TargetInstanceResultHandler();
        bean.setTargetInstanceManager(baseConfig.targetInstanceManager());
        bean.setHarvestCoordinator(harvestCoordinator);
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
    public OpenSessionInViewInterceptor openSessionInViewInterceptor() {
        OpenSessionInViewInterceptor bean = new OpenSessionInViewInterceptor();
        bean.setSessionFactory(baseConfig.sessionFactory().getObject());

        return bean;
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
    public TabConfig targetTabConfig() {
        TabConfig bean = new TabConfig();
        bean.setViewName("target");

        List<Tab> tabs = new ArrayList<>();

        Tab theTab = new Tab();
        theTab.setPageId("GENERAL");
        theTab.setTitle("general");
        theTab.setJsp("../target-general.jsp");
        theTab.setCommandClass(TargetGeneralCommand.class);
        theTab.setValidator(targetGeneralValidator);
        theTab.setTabHandler(targetGeneralHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("SEEDS");
        theTab.setTitle("seeds");
        theTab.setJsp("../target-seeds.jsp");
        theTab.setCommandClass(SeedsCommand.class);
        theTab.setValidator(targetSeedsValidator);
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
//        theTab.setValidator(new TargetAnnotationValidator());
        theTab.setTabHandler(targetGroupsHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("ACCESS");
        theTab.setTitle("access");
        theTab.setJsp("../target-access.jsp");
        theTab.setCommandClass(TargetAccessCommand.class);
        theTab.setValidator(targetAccessValidator);
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
        bean.setValidator(targetSeedsValidator);
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
    public ProfilesBasicCredentialsValidator basicCredentialsValidatorti() {
        return new ProfilesBasicCredentialsValidator();
    }

    @Bean
    public ProfilesFormCredentialsValidator formCredentialsValidatorti() {
        return new ProfilesFormCredentialsValidator();
    }

    @Bean
    public ProfilesBasicCredentialsValidator basicCredentialsValidatorGroup() {
        return new ProfilesBasicCredentialsValidator();
    }

    @Bean
    public ProfilesFormCredentialsValidator formCredentialsValidatorGroup() {
        return new ProfilesFormCredentialsValidator();
    }

    // Groups Controller and Tab Configuration

    @Bean
    public OverrideGetter groupOverrideGetter() {
        OverrideGetter bean = new OverrideGetter();
        bean.setOverrideableType("Target Group");

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
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
        theTab.setValidator(targetAccessValidator);
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
    public org.webcurator.ui.target.validator.AddParentsValidator targetAddParentsValidator() {
        return new org.webcurator.ui.target.validator.AddParentsValidator();
    }

    // Profile Controller and Tab Configuration

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public TabConfig profileTabConfig() {
        TabConfig bean = new TabConfig();
        bean.setViewName("profile");

        List<Tab> tabs = new ArrayList<>();

        Tab theTab = new Tab();
        theTab.setPageId("GENERAL");
        theTab.setTitle("general");
        theTab.setJsp("../profile-general.jsp");
        theTab.setCommandClass(org.webcurator.ui.profiles.command.GeneralCommand.class);
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
    public TabConfig profileH3TabConfig() {
        TabConfig bean = new TabConfig();
        bean.setViewName("profileH3");

        List<Tab> tabs = new ArrayList<>();

        Tab theTab = new Tab();
        theTab.setPageId("GENERAL");
        theTab.setTitle("general");
        theTab.setJsp("../profile-general.jsp");
        theTab.setCommandClass(org.webcurator.ui.profiles.command.GeneralCommand.class);
        theTab.setValidator(new ProfileGeneralValidator());
        theTab.setTabHandler(new ProfileGeneralHandler());
        tabs.add(theTab);

        theTab = new Tab();

        tabs.add(theTab);
        theTab.setPageId("SCOPE");
        theTab.setTitle("scope");
        theTab.setJsp("../profileH3-scope.jsp");
        theTab.setCommandClass(Heritrix3ProfileCommand.class);
        theTab.setValidator(new Heritrix3ProfileValidator());
        theTab.setTabHandler(heritrix3ProfileHandler);
        bean.setTabs(tabs);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public TabConfig importedProfileH3TabConfig() {
        TabConfig bean = new TabConfig();
        bean.setViewName("imported-profileH3");

        List<Tab> tabs = new ArrayList<>();

        Tab theTab = new Tab();
        theTab.setPageId("GENERAL");
        theTab.setTitle("general");
        theTab.setJsp("../profile-general.jsp");
        theTab.setCommandClass(org.webcurator.ui.profiles.command.GeneralCommand.class);
        theTab.setValidator(new ProfileGeneralValidator());
        theTab.setTabHandler(new ProfileGeneralHandler());
        tabs.add(theTab);

        theTab = new Tab();
        theTab.setPageId("SCOPE-IMPORTED");
        theTab.setTitle("scope");
        theTab.setJsp("../imported-profileH3-scope.jsp");
        theTab.setCommandClass(ImportedHeritrix3ProfileCommand.class);
        theTab.setValidator(importedHeritrix3ProfileValidator);
        theTab.setTabHandler(new ImportedHeritrix3ProfileHandler());
        tabs.add(theTab);

        bean.setTabs(tabs);

        return bean;
    }

}
