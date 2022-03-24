package org.webcurator.visualization.springboot;

import org.archive.io.CDXIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.webcurator.core.visualization.VisualizationDirectoryManager;
import org.webcurator.core.visualization.VisualizationProcessorManager;
import org.webcurator.core.visualization.networkmap.NetworkMapDomainSuffix;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNode;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClientLocal;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;
import org.webcurator.core.store.*;
import org.webcurator.core.util.ApplicationContextFactory;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Contains configuration that used to be found in {@code wct-das.xml}. This
 * is part of the change to move to using annotations for Spring instead of
 * XML files.
 */
@SuppressWarnings("all")
@Configuration
public class MainConfig {
    private static Logger LOGGER = LoggerFactory.getLogger(MainConfig.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Value("${webapp.baseUrl}")
    private String wctCoreWsEndpointBaseUrl;

    // the base directory for the arc store
    @Value("${arcDigitalAssetStoreService.baseDir}")
    private String arcDigitalAssetStoreServiceBaseDir;

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

    @Value("${server.port}")
    private String wctStorePort;

    @Value("${qualify.processor.max}")
    private int maxConcurrencyModThreads;

    @Value("${qualify.heartbeat.interval}")
    private long heartbeatInterval;

    @Value("${qualify.jobscan.interval}")
    private long jobScanInterval;

    @Value(("${visualization.dbVersion}"))
    private String visualizationDbVersion;

    @PostConstruct
    public void init() {
        ApplicationContextFactory.setApplicationContext(applicationContext);
        NetworkMapNode.setTopDomainParse(networkMapDomainSuffix());
    }

    private NetworkMapDomainSuffix networkMapDomainSuffix() {
        //https://publicsuffix.org/list/public_suffix_list.dat

        NetworkMapDomainSuffix suffixParser = new NetworkMapDomainSuffix();
        Resource resource = new ClassPathResource("public_suffix_list.dat");

        Path tempDataFilePath = null;
        try {
            tempDataFilePath = Files.createTempFile("public-suffix-list", ".dat");
            tempDataFilePath.toFile().delete();
            Files.copy(resource.getInputStream(), tempDataFilePath);
            suffixParser.init(tempDataFilePath.toFile());
        } catch (Exception e) {
            LOGGER.error("Load domain suffix file failed.", e);
        } finally {
            if (tempDataFilePath != null && tempDataFilePath.toFile().exists()) {
                tempDataFilePath.toFile().delete();
            }
        }

        return suffixParser;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public VisualizationDirectoryManager visualizationManager() {
        return new VisualizationDirectoryManager(arcDigitalAssetStoreServiceBaseDir, "logs", "reports");
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public VisualizationProcessorManager visualizationProcessorQueue() {
        return new VisualizationProcessorManager(visualizationManager(),
                null,
                maxConcurrencyModThreads);
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
//        sourceList.add(wctIndexer());
        sourceList.add(waybackIndexer());
        sourceList.add(crawlLogIndexer());
        sourceList.add(cdxIndexer());

        bean.setSourceList(sourceList);

        return bean;
    }


    @Bean
    public WaybackIndexer waybackIndexer() {
        WaybackIndexer bean = new WaybackIndexer(wctCoreWsEndpointBaseUrl, restTemplateBuilder);
        bean.setEnabled(waybackIndexerEnabled);
//        bean.setWsEndPoint(wctCoreWsEndpoint());
        bean.setWaittime(waybackIndexerWaitTime);
        bean.setTimeout(waybackIndexerTimeout);
        bean.setWaybackInputFolder(waybackIndexerWaybackInputFolder);
        bean.setWaybackMergedFolder(waybackIndexerWaybackMergedFolder);
        bean.setWaybackFailedFolder(waybackIndexerWaybackFailedFolder);

        return bean;
    }

    @Bean
    public CrawlLogIndexer crawlLogIndexer() {
        CrawlLogIndexer bean = new CrawlLogIndexer(wctCoreWsEndpointBaseUrl, restTemplateBuilder);
        bean.setEnabled(crawlLogIndexerEnabled);
//        bean.setWsEndPoint(wctCoreWsEndpoint());
        bean.setLogsSubFolder(crawlLogIndexerLogsSubFolder);
        bean.setCrawlLogFileName(crawlLogIndexerCrawlLogFileName);
        bean.setStrippedLogFileName(crawlLogIndexerStrippedLogFileName);
        bean.setSortedLogFileName(crawlLogIndexerSortedLogFileName);

        return bean;
    }

    @Bean
    public CDXIndexer cdxIndexer() {
        CDXIndexer bean = new CDXIndexer(wctCoreWsEndpointBaseUrl, restTemplateBuilder);
        bean.setEnabled(cdxIndexerEnabled);
        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public BDBNetworkMapPool bdbDatabasePool() {
        BDBNetworkMapPool pool = new BDBNetworkMapPool(arcDigitalAssetStoreServiceBaseDir, visualizationDbVersion);
        return pool;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public NetworkMapClient networkMapLocalClient() {
        return new NetworkMapClientLocal(bdbDatabasePool(), visualizationProcessorQueue());
    }
}
