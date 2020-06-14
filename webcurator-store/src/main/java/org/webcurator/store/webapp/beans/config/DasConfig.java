package org.webcurator.store.webapp.beans.config;

import nz.govt.natlib.ndha.wctdpsdepositor.CustomDepositField;
import nz.govt.natlib.ndha.wctdpsdepositor.CustomDepositFormMapping;
import org.archive.io.CDXIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.webcurator.core.archive.Archive;
import org.webcurator.core.archive.dps.DPSArchive;
import org.webcurator.core.archive.file.FileArchive;
import org.webcurator.core.archive.oms.OMSArchive;
import org.webcurator.core.visualization.VisualizationManager;
import org.webcurator.core.visualization.modification.PruneAndImportProcessor;
import org.webcurator.core.visualization.networkmap.NetworkMapDomainSuffix;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClientLocal;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;
import org.webcurator.core.reader.LogReaderImpl;
import org.webcurator.core.store.*;
import org.webcurator.core.store.arc.*;
import org.webcurator.core.util.ApplicationContextFactory;
import org.webcurator.core.util.WebServiceEndPoint;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.*;

/**
 * Contains configuration that used to be found in {@code wct-das.xml}. This
 * is part of the change to move to using annotations for Spring instead of
 * XML files.
 */
@SuppressWarnings("all")
@Configuration
@PropertySource(value = "classpath:wct-das.properties")
public class DasConfig {
    private static Logger LOGGER = LoggerFactory.getLogger(DasConfig.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Value("${wctCoreWsEndpoint.scheme}")
    private String wctCoreWsEndpointScheme;

    @Value("${wctCoreWsEndpoint.host}")
    private String wctCoreWsEndpointHost;

    @Value("${wctCoreWsEndpoint.port}")
    private int wctCoreWsEndpointPort;

    // the base directory for the arc store
    @Value("${arcDigitalAssetStoreService.baseDir}")
    private String arcDigitalAssetStoreServiceBaseDir;

    // TODO need to confirm that this will actually instantiate the archive
    @Value("${arcDigitalAssetStoreService.archive}")
    private String arcDigitalAssetStoreServiceArchive;

    @Value("${arcDigitalAssetStoreService.dasFileMover}")
    private String arcDigitalAssetStoreServiceDasFileMover;

    @Value("${arcDigitalAssetStoreService.pageImagePrefix}")
    private String arcDigitalAssetStoreServicePageImagePrefix;

    @Value("${arcDigitalAssetStoreService.aqaReportPrefix}")
    private String arcDigitalAssetStoreServiceAqaReportPrefix;

    @Value("${waybackIndexer.enabled}")
    private boolean waybackIndexerEnabled;

    // Frequency of checks on the merged folder (milliseconds).
    @Value("${waybackIndexer.waittime}")
    private long waybackIndexerWaitTime;

    // Time to wait for the file to be indexed before giving up (milliseconds).
    @Value("${waybackIndexer.timeout}")
    private long waybackIndexerTimeout;

    // Location of the folder Wayback is watching for auto indexing.
    @Value("${waybackIndexer.waybackInputFolder}")
    private String waybackIndexerWaybackInputFolder;

    // Location of the folder where Wayback places merged indexes.
    @Value("${waybackIndexer.waybackMergedFolder}")
    private String waybackIndexerWaybackMergedFolder;

    // Location of the folder where Wayback places failed indexes.
    @Value("${waybackIndexer.waybackFailedFolder}")
    private String waybackIndexerWaybackFailedFolder;

    @Value("${crawlLogIndexer.enabled}")
    private boolean crawlLogIndexerEnabled;

    // Logs sub-folder name.
    @Value("${crawlLogIndexer.logsSubFolder}")
    private String crawlLogIndexerLogsSubFolder;

    // Name of the crawl.log file.
    @Value("${crawlLogIndexer.crawlLogFileName}")
    private String crawlLogIndexerCrawlLogFileName;

    // Name of the stripped crawl.log file.
    @Value("${crawlLogIndexer.strippedLogFileName}")
    private String crawlLogIndexerStrippedLogFileName;

    // ame of the sorted crawl.log file.
    @Value("${crawlLogIndexer.sortedLogFileName}")
    private String crawlLogIndexerSortedLogFileName;

    @Value("${cdxIndexer.enabled}")
    private boolean cdxIndexerEnabled;

    @Value("${fileArchive.archiveRepository}")
    private String fileArchiveArchiveRepository;

    @Value("${fileArchive.archiveLogReportFiles}")
    private String fileArchiveArchiveLogReportFiles;

    @Value("${fileArchive.archiveLogDirectory}")
    private String fileArchiveArchiveLogDirectory;

    @Value("${fileArchive.archiveReportDirectory}")
    private String fileArchiveArchiveReportDirectory;

    @Value("${fileArchive.archiveArcDirectory}")
    private String fileArchiveArchiveArcDirectory;

    @Value("${omsArchive.archiveLogReportFiles}")
    private String omsArchiveArchiveLogReportFiles;

    @Value("${omsArchive.url}")
    private String omsArchiveUrl;

    @Value("${omsArchive.partSize}")
    private int omsArchivePartSize;

    @Value("${omsArchive.ilsTapuhiFlag}")
    private String omsArchiveIlsTapuhiFlag;

    @Value("${omsArchive.collectionType}")
    private String omsArchiveCollectionType;

    @Value("${omsArchive.objectType}")
    private String omsArchiveObjectType;

    @Value("${omsArchive.agencyResponsible}")
    private String omsArchiveAgencyResponsible;

    @Value("${omsArchive.instanceRole}")
    private String omsArchiveInstanceRole;

    @Value("${omsArchive.instanceCaptureSystem}")
    private String omsArchiveInstanceCaptureSystem;

    @Value("${omsArchive.instanceType}")
    private String omsArchiveInstanceType;

    @Value("${omsArchive.user_group}")
    private int omsArchiveUserGroup;

    @Value("${omsArchive.user}")
    private String omsArchiveUser;

    @Value("${omsArchive.password}")
    private String omsArchivePassword;

    @Value("${dpsArchive.pdsUrl}")
    private String dpsArchivePdsUrl;

    @Value("${dpsArchive.ftpHost}")
    private String dpsArchiveFtpHost;

    @Value("${dpsArchive.ftpUserName}")
    private String dpsArchiveFtpUserName;

    @Value("${dpsArchive.ftpPassword}")
    private String dpsArchiveFtpPassword;

    @Value("${dpsArchive.ftpDirectory}")
    private String dpsArchiveFtpDirectory;

    @Value("${dpsArchive.dpsUserInstitution}")
    private String dpsArchiveDpsUserInstitution;

    @Value("${dpsArchive.dpsUserName}")
    private String dpsArchiveDpsUserName;

    @Value("${dpsArchive.dpsUserPassword}")
    private String dpsArchiveDpsUserPassword;

    @Value("${dpsArchive.materialFlowId}")
    private String dpsArchiveMaterialFlowId;

    @Value("${dpsArchive.producerId}")
    private String dpsArchiveProducerId;

    @Value("${dpsArchive.depositServerBaseUrl}")
    private String dpsArchiveDepositServerBaseUrl;

    @Value("${dpsArchive.producerWsdlRelativePath}")
    private String dpsArchiveProducerWsdlRelativePath;

    @Value("${dpsArchive.depositWsdlRelativePath}")
    private String dpsArchiveDepositWsdlRelativePath;

    @Value("${dpsArchive.htmlSerials.agencyNames}")
    private String dpsArchiveHtmlSerialsAgencyNames;

    @Value("${dpsArchive.htmlSerials.targetDCTypes}")
    private String dpsArchiveHtmlSerialsTargetDCTypes;

    @Value("${dpsArchive.htmlSerials.materialFlowIds}")
    private String dpsArchiveHtmlSerialsMaterialFlowIds;

    @Value("${dpsArchive.htmlSerials.producerIds}")
    private String dpsArchiveHtmlSerialsProducerIds;

    @Value("${dpsArchive.htmlSerials.ieEntityTypes}")
    private String dpsArchiveHtmlSerialsIeEntityTypes;

    @Value("${dpsArchive.htmlSerials.customDepositFormURLs}")
    private String dpsArchiveHtmlSerialsCustomDepositFormURLs;

    @Value("${dpsArchive.dnx_open_access}")
    private String dpsArchiveDnxOpenAccess;

    @Value("${dpsArchive.dnx_published_restricted}")
    private String dpsArchiveDnxPublishedRestricted;

    @Value("${dpsArchive.dnx_unpublished_restricted_location}")
    private String dpsArchiveDnxUnpublishedRestrictedLocation;

    @Value("${dpsArchive.dnx_unpublished_restricted_person}")
    private String dpsArchiveDnxUnpublishedRestrictedPerson;

    @Value("${dpsArchive.cmsSection}")
    private String dpsArchiveCmsSection;

    @Value("${dpsArchive.cmsSystem}")
    private String dpsArchiveCmsSystem;

    @Value("${dpsArchive.webHarvest.customTargetDCTypes}")
    private String dpsArchiveWebHarvestCustomTargetDCTypes;

    @Value("${dpsArchive.webHarvest.customerMaterialFlowIds}")
    private String dpsArchiveWebHarvestCustomerMaterialFlowIds;

    @Value("${dpsArchive.webHarvest.customerProducerIds}")
    private String dpsArchiveWebHarvestCustomerProducerIds;

    @Value("${dpsArchive.webHarvest.customIeEntityTypes}")
    private String dpsArchiveWebHarvestCustomIeEntityTypes;

    @Value("${dpsArchive.webHarvest.customDCTitleSource}")
    private String dpsArchiveWebHarvestCustomDCTitleSource;

    @Value("${dpsArchive.htmlSerials.restrictAgencyType}")
    private String dpsArchiveHtmlSerialsRestrictAgencyType;

    @Value("${server.port}")
    private String wctStorePort;

    @Value("${arcDigitalAssetStoreService.modThreads.max}")
    private int maxConcurrencyModThreads;

    @Autowired
    private VisualizationManager visualizationManager;

    @PostConstruct
    public void init() {
        ApplicationContextFactory.setApplicationContext(applicationContext);

        visualizationManager.setUploadDir(arcDigitalAssetStoreServiceBaseDir + File.separator + "uploadedFiles");
        visualizationManager.setBaseDir(arcDigitalAssetStoreServiceBaseDir);
        visualizationManager.setLogsDir(Constants.DIR_LOGS);
        visualizationManager.setReportsDir(Constants.DIR_REPORTS);

        PruneAndImportProcessor.setMaxConcurrencyModThreads(maxConcurrencyModThreads);
    }

    @Bean
    public WebServiceEndPoint wctCoreWsEndpoint() {
        WebServiceEndPoint bean = new WebServiceEndPoint();
        bean.setSchema(wctCoreWsEndpointScheme);
        bean.setHost(wctCoreWsEndpointHost);
        bean.setPort(wctCoreWsEndpointPort);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false) // lazy-init="default", but no default has been set for wct-das.xml
    public ArcDigitalAssetStoreService arcDigitalAssetStoreService() {
        ArcDigitalAssetStoreService bean = new ArcDigitalAssetStoreService(restTemplateBuilder);
        bean.setBaseDir(arcDigitalAssetStoreServiceBaseDir);
        bean.setIndexer(indexer());
        bean.setWsEndPoint(wctCoreWsEndpoint());
        bean.setDasFileMover(createDasFileMover());
        bean.setPageImagePrefix(arcDigitalAssetStoreServicePageImagePrefix);
        bean.setAqaReportPrefix(arcDigitalAssetStoreServiceAqaReportPrefix);
        bean.setFileArchive(createFileArchive());
        bean.setVisualizationManager(visualizationManager);
        return bean;
    }

    @Bean
    public FilterRegistrationBean filterRegistration() {

        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new CustomDepositFormFilter());
        registration.addUrlPatterns("/customDepositForms/*");
        registration.setOrder(1);
        return registration;
    }

    public DasFileMover createDasFileMover() {
        DasFileMover dasFileMover = null;
        if ("inputStreamDasFileMover".equalsIgnoreCase(arcDigitalAssetStoreServiceDasFileMover)) {
            dasFileMover = new InputStreamDasFileMover();
        } else if ("renameDasFileMover".equalsIgnoreCase(arcDigitalAssetStoreServiceDasFileMover)) {
            dasFileMover = new RenameDasFileMover();
        } else {
            LOGGER.warn("Unrecognized DasFileMover (unable to create), arcDigitalAssetStoreService.dasFileMover=" +
                    arcDigitalAssetStoreServiceDasFileMover);
        }
        return dasFileMover;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false) // lazy-init="default", but no default has been set for wct-das.xml
    // The archive type is one of: fileArchive, omsArchive, dpsArchive.
    public Archive arcDasArchive() {
        Archive bean = null;
        String archiveType = arcDigitalAssetStoreServiceArchive;
        if ("fileArchive".equalsIgnoreCase(archiveType)) {
            bean = createFileArchive();
        } else if ("omsArchive".equalsIgnoreCase(archiveType)) {
            bean = createOmsArchive();
        } else if ("dpsArchive".equalsIgnoreCase(archiveType)) {
            bean = createDpsArchive();
        } else {
            LOGGER.debug("Instantiating Archive class for name=" + archiveType);
            if (archiveType.trim().length() > 0) {
                try {
                    Class<?> clazz = Class.forName(archiveType);
                    Object archiveInstance = clazz.newInstance();
                    bean = (Archive) archiveInstance;
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                    LOGGER.error("Unable to instantiate archive by type/class=" + archiveType, e);
                }
            }
        }
        return bean;
    }

    @SuppressWarnings("unchecked")
    @Bean
    public Indexer indexer() {
        Indexer bean = new Indexer();
        ListFactoryBean runnableIndexers = runnableIndexers();

        try {
            List<RunnableIndex> list = (List<RunnableIndex>) (List<?>) runnableIndexers.getObject();
            bean.setIndexers(list);
        } catch (Exception e) {
            // This is to avoid an 'throws Exception' in the method signature, which would percolate to all related
            // beans. If the bean cannot be instantiated/populated properly, the application should not be able to start.
            LOGGER.error("indexer unable to setIndexers with: " + runnableIndexers, e);
            throw new RuntimeException(e);
        }

        return bean;
    }

    @Bean
    public ListFactoryBean runnableIndexers() {
        ListFactoryBean bean = new ListFactoryBean();

        List<RunnableIndex> sourceList = new ArrayList<>();
        sourceList.add(wctIndexer());
        sourceList.add(waybackIndexer());
        sourceList.add(crawlLogIndexer());
        sourceList.add(cdxIndexer());

        bean.setSourceList(sourceList);

        return bean;
    }

    @Bean
    public WCTIndexer wctIndexer() {
        WCTIndexer bean = new WCTIndexer(wctCoreWsEndpointScheme, wctCoreWsEndpointHost, wctCoreWsEndpointPort, restTemplateBuilder);
        bean.setWsEndPoint(wctCoreWsEndpoint());

        return bean;
    }

    @Bean
    public WaybackIndexer waybackIndexer() {
        WaybackIndexer bean = new WaybackIndexer();
        bean.setEnabled(waybackIndexerEnabled);
        bean.setWsEndPoint(wctCoreWsEndpoint());
        bean.setWaittime(waybackIndexerWaitTime);
        bean.setTimeout(waybackIndexerTimeout);
        bean.setWaybackInputFolder(waybackIndexerWaybackInputFolder);
        bean.setWaybackMergedFolder(waybackIndexerWaybackMergedFolder);
        bean.setWaybackFailedFolder(waybackIndexerWaybackFailedFolder);

        return bean;
    }

    @Bean
    public CrawlLogIndexer crawlLogIndexer() {
        CrawlLogIndexer bean = new CrawlLogIndexer();
        bean.setEnabled(crawlLogIndexerEnabled);
        bean.setWsEndPoint(wctCoreWsEndpoint());
        bean.setLogsSubFolder(crawlLogIndexerLogsSubFolder);
        bean.setCrawlLogFileName(crawlLogIndexerCrawlLogFileName);
        bean.setStrippedLogFileName(crawlLogIndexerStrippedLogFileName);
        bean.setSortedLogFileName(crawlLogIndexerSortedLogFileName);

        return bean;
    }

    @Bean
    public CDXIndexer cdxIndexer() {
        CDXIndexer bean = new CDXIndexer();
        bean.setEnabled(cdxIndexerEnabled);
        bean.setWsEndPoint(wctCoreWsEndpoint());

        return bean;
    }

    // A File Mover that uses InputStreams to copy files in chunks. Will work successfully
    // across different filesystems.
    @Bean
    public InputStreamDasFileMover inputStreamDasFileMover() {
        return new InputStreamDasFileMover();
    }

    // A File Mover that simply renames the file. This will be fast but is NOT guaranteed to
    // move files between file systems.
    @Bean
    public RenameDasFileMover renameDasFileMover() {
        return new RenameDasFileMover();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false) // lazy-init="default", but no default has been set for wct-das.xml
    public LogReaderImpl logReader() {
        LogReaderImpl bean = new LogReaderImpl();
        bean.setLogProvider(arcDigitalAssetStoreService());

        return bean;
    }

    private FileArchive createFileArchive() {
        FileArchive bean = new FileArchive();
        bean.setArchiveRepository(fileArchiveArchiveRepository);
        bean.setArchiveLogReportFiles(fileArchiveArchiveLogReportFiles);
        bean.setArchiveLogDirectory(fileArchiveArchiveLogDirectory);
        bean.setArchiveArcDirectory(fileArchiveArchiveArcDirectory);
        bean.setArchiveReportDirectory(fileArchiveArchiveReportDirectory);

        return bean;
    }

    private OMSArchive createOmsArchive() {
        OMSArchive bean = new OMSArchive();
        bean.setArchiveLogReportFiles(omsArchiveArchiveLogReportFiles);
        bean.setUrl(omsArchiveUrl);
        bean.setPartSize(omsArchivePartSize);
        bean.setIlsTapuhiFlag(omsArchiveIlsTapuhiFlag);
        bean.setCollectionType(omsArchiveCollectionType);
        bean.setObjectType(omsArchiveObjectType);
        bean.setAgencyResponsible(omsArchiveAgencyResponsible);
        bean.setInstanceRole(omsArchiveInstanceRole);
        bean.setInstanceCaptureSystem(omsArchiveInstanceCaptureSystem);
        bean.setInstanceType(omsArchiveInstanceType);
        bean.setUser_group(omsArchiveUserGroup);
        bean.setUser(omsArchiveUser);
        bean.setPassword(omsArchivePassword);

        return bean;
    }

    // Configuration parameters for the Submit-To-Rosetta module which submits a harvest into Ex Libris Rosetta System
    // (a.k.a. DPS, the Digital Preservation System).
    private DPSArchive createDpsArchive() {
        DPSArchive bean = new DPSArchive();
        bean.setPdsUrl(dpsArchivePdsUrl);
        bean.setFtpHost(dpsArchiveFtpHost);
        bean.setFtpUserName(dpsArchiveFtpUserName);
        bean.setFtpPassword(dpsArchiveFtpPassword);

        // TODO Note wct-das.xml does not have this element commented out, so this comment may no longer apply.
        // "ftpDirectory" is an optional parameter. If not provided, the harvest files will be FTPed to the home directory
        // of the FTP user. If this parameter is provided, the files will be FTPed to this directory instead. However,
        // it is your responsibility to ensure that the FTP user has the write permission to this directory.
        //
        // Uncomment the XML element below if this parameter needs to be used in your installation.
        bean.setFtpDirectory(dpsArchiveFtpDirectory);

        bean.setDpsUserInstitution(dpsArchiveDpsUserInstitution);
        bean.setDpsUserName(dpsArchiveDpsUserName);
        bean.setDpsUserPassword(dpsArchiveDpsUserPassword);
        bean.setMaterialFlowId(dpsArchiveMaterialFlowId);
        bean.setProducerId(dpsArchiveProducerId);
        bean.setDepositServerBaseUrl(dpsArchiveDepositServerBaseUrl);
        bean.setProducerWsdlRelativePath(dpsArchiveProducerWsdlRelativePath);
        bean.setDepositWsdlRelativePath(dpsArchiveDepositWsdlRelativePath);
        bean.setAgenciesResponsibleForHtmlSerials(dpsArchiveHtmlSerialsAgencyNames);
        bean.setTargetDCTypesOfHtmlSerials(dpsArchiveHtmlSerialsTargetDCTypes);
        bean.setMaterialFlowsOfHtmlSerials(dpsArchiveHtmlSerialsMaterialFlowIds);
        bean.setProducerIdsOfHtmlSerials(dpsArchiveHtmlSerialsProducerIds);
        bean.setIeEntityTypesOfHtmlSerials(dpsArchiveHtmlSerialsIeEntityTypes);
        bean.setCustomDepositFormURLsForHtmlSerialIngest(dpsArchiveHtmlSerialsCustomDepositFormURLs);
        bean.setCustomDepositFormMapping(customDepositFormFieldMappings());
        bean.setOmsOpenAccess(dpsArchiveDnxOpenAccess);
        bean.setOmsPublishedRestricted(dpsArchiveDnxPublishedRestricted);
        bean.setOmsUnpublishedRestrictedByLocation(dpsArchiveDnxUnpublishedRestrictedLocation);
        bean.setOmsUnpublishedRestrictedByPersion(dpsArchiveDnxUnpublishedRestrictedPerson);
        bean.setCmsSection(dpsArchiveCmsSection);
        bean.setCmsSystem(dpsArchiveCmsSystem);
        bean.setTargetDCTypesOfCustomWebHarvest(dpsArchiveWebHarvestCustomTargetDCTypes);
        bean.setMaterialFlowsOfCustomWebHarvest(dpsArchiveWebHarvestCustomerMaterialFlowIds);
        bean.setProducerIdsOfCustomWebHarvest(dpsArchiveWebHarvestCustomerProducerIds);
        bean.setIeEntityTypesOfCustomWebHarvest(dpsArchiveWebHarvestCustomIeEntityTypes);
        bean.setDCTitleSourceOfCustomWebHarvest(dpsArchiveWebHarvestCustomDCTitleSource);
        bean.setRestrictHTMLSerialAgenciesToHTMLSerialTypes(dpsArchiveHtmlSerialsRestrictAgencyType);

        return bean;
    }

//    @Bean
//    public CustomDepositField customDepositField() {
//        CustomDepositField bean = new CustomDepositField();
//        // The label given to the field on the form
//        bean.setFormFieldLabel("customDepositForm_bibliographicCitation");
//        // A reference used when passing this value from WCT-Store to WCT-SubmitToRosetta
//        bean.setFieldReference("DctermsBibliographicCitation");
//        // The DC/DCTerms label used in the mets.xml
//        bean.setDcFieldLabel("bibliographicCitation");
//
//        return bean;
//    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false) // lazy-init="default", but no default has been set for wct-das.xml
    public CustomDepositFormMapping customDepositFormFieldMappings() {
        CustomDepositFormMapping bean = new CustomDepositFormMapping();
        Map<String, List<CustomDepositField>> customDepositFormFieldMaps = new HashMap<>();

        List<CustomDepositField> rosettaList = new ArrayList<>();
        rosettaList.add(depositFieldDctermsBibliographicCitation());
        rosettaList.add(depositFieldDctermsAvailable());
        customDepositFormFieldMaps.put("http://localhost:" + wctStorePort + "/customDepositForms/rosetta_custom_deposit_form.jsp", rosettaList);

        List<CustomDepositField> almaList = new ArrayList<>();
        almaList.add(depositFieldVolume());
        almaList.add(depositFieldIssue());
        almaList.add(depositFieldNumber());
        almaList.add(depositFieldYear());
        almaList.add(depositFieldMonth());
        almaList.add(depositFieldDay());
        customDepositFormFieldMaps.put("http://localhost:" + wctStorePort + "/customDepositForms/rosetta_alma_custom_deposit_form.jsp", almaList);

        bean.setCustomDepositFormFieldMaps(customDepositFormFieldMaps);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public BDBNetworkMapPool getBDBDatabasePool() {
        BDBNetworkMapPool pool = new BDBNetworkMapPool(arcDigitalAssetStoreServiceBaseDir);
        return pool;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public NetworkMapClient getNetworkMapLocalClient() {
        return new NetworkMapClientLocal(getBDBDatabasePool());
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public NetworkMapDomainSuffix getNetworkMapDomainSuffix() {
        NetworkMapDomainSuffix suffixParser = new NetworkMapDomainSuffix();
        Resource resource = new ClassPathResource("public_suffix_list.dat");

        try {
            suffixParser.init(resource.getFile().getAbsolutePath());
        } catch (Exception e) {
            LOGGER.error("Load domain suffix file failed.", e);
        }

        return suffixParser;
    }

    public CustomDepositField depositFieldDctermsBibliographicCitation() {
        CustomDepositField bean = new CustomDepositField();
        bean.setFormFieldLabel("customDepositForm_bibliographicCitation");
        bean.setFieldReference("DctermsBibliographicCitation");
        bean.setDcFieldLabel("bibliographicCitation");
        bean.setMandatory(true);

        return bean;
    }

    public CustomDepositField depositFieldDctermsAvailable() {
        CustomDepositField bean = new CustomDepositField();
        bean.setFormFieldLabel("customDepositForm_dctermsAvailable");
        bean.setFieldReference("DctermsAvailable");
        bean.setDcFieldLabel("available");
        bean.setMandatory(true);

        return bean;
    }

    public CustomDepositField depositFieldVolume() {
        CustomDepositField bean = new CustomDepositField();
        bean.setFormFieldLabel("customDepositForm_volume");
        bean.setFieldReference("dctermsBibliographicCitation");
        bean.setDcFieldLabel("bibliographicCitation");
        bean.setDcFieldType("dcterms");
        bean.setMandatory(false);

        return bean;
    }

    public CustomDepositField depositFieldIssue() {
        CustomDepositField bean = new CustomDepositField();
        bean.setFormFieldLabel("customDepositForm_issue");
        bean.setFieldReference("dctermsIssued");
        bean.setDcFieldLabel("issue");
        bean.setDcFieldType("dcterms");
        bean.setMandatory(false);

        return bean;
    }

    public CustomDepositField depositFieldNumber() {
        CustomDepositField bean = new CustomDepositField();
        bean.setFormFieldLabel("customDepositForm_number");
        bean.setFieldReference("dctermsAaccrualPeriodicity");
        bean.setDcFieldLabel("accrualPeriodicity");
        bean.setDcFieldType("dcterms");
        bean.setMandatory(false);

        return bean;
    }

    public CustomDepositField depositFieldYear() {
        CustomDepositField bean = new CustomDepositField();
        bean.setFormFieldLabel("customDepositForm_year");
        bean.setFieldReference("dcDate");
        bean.setDcFieldLabel("date");
        bean.setDcFieldType("dc");
        bean.setMandatory(false);

        return bean;
    }

    public CustomDepositField depositFieldMonth() {
        CustomDepositField bean = new CustomDepositField();
        bean.setFormFieldLabel("customDepositForm_month");
        bean.setFieldReference("dctermsAvailable");
        bean.setDcFieldLabel("available");
        bean.setDcFieldType("dcterms");
        bean.setMandatory(false);

        return bean;
    }

    public CustomDepositField depositFieldDay() {
        CustomDepositField bean = new CustomDepositField();
        bean.setFormFieldLabel("customDepositForm_day");
        bean.setFieldReference("dcCoverage");
        bean.setDcFieldLabel("coverage");
        bean.setDcFieldType("dc");
        bean.setMandatory(false);

        return bean;
    }
}
