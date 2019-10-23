package org.webcurator.harvestagent.h3.webapp.beans.config;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.webcurator.core.check.*;
import org.webcurator.core.harvester.agent.HarvestAgentH3;
import org.webcurator.core.harvester.agent.Heritrix3WrapperConfiguration;
import org.webcurator.core.harvester.agent.schedule.HarvestAgentHeartBeatJob;
import org.webcurator.core.harvester.agent.schedule.HarvestCompleteConfig;
import org.webcurator.core.harvester.coordinator.HarvestCoordinatorNotifier;
import org.webcurator.core.reader.LogReaderImpl;
import org.webcurator.core.store.DigitalAssetStore;
import org.webcurator.core.store.DigitalAssetStoreClient;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Contains configuration that used to be found in {@code wct-agent.xml}. This
 * is part of the change to move to using annotations for Spring instead of
 * XML files.
 */
@Configuration
@PropertySource(value = "classpath:wct-agent.properties")
public class AgentConfig {
    private static Logger LOGGER = LoggerFactory.getLogger(AgentConfig.class);

    // Name of the directory where the temporary harvest data is stored.
    @Value("${harvestAgent.baseHarvestDirectory}")
    private String harvestAgentBaseHarvestDirectory;

    // Agent host name or ip address that the core knows about.
    @Value("${harvestAgent.host}")
    private String harvestAgentHost;

    // The max number of harvest to be run concurrently on this agent.
    @Value("${harvestAgent.maxHarvests}")
    private int harvestAgentMaxHarvests;

    // The port the agent is listening on for http connections.
    @Value("${harvestAgent.port}")
    private int harvestAgentPort;

    // The name of the harvest agent web service.
    @Value("${harvestAgent.service}")
    private String harvestAgentService;

    // The name of the harvest agent log reader web service.
    @Value("${harvestAgent.logReaderService}")
    private String harvestAgentLogReaderService;

    // The name of the agent. Must be unique.
    @Value("${harvestAgent.name}")
    private String harvestAgentName;

    // The note to send with the harvest result.
    @Value("${harvestAgent.provenanceNote}")
    private String harvestAgentProvenanceNote;

    // The number of alerts that occur before a notification is sent.
    @Value("${harvestAgent.alertThreshold}")
    private int harvestAgentAlertThreshold;

    // H3 instance host name or ip address for the H3 agent.
    @Value("${h3Wrapper.host}")
    private String h3WrapperHost;

    // The port the H3 instance is listening on for http connections.
    @Value("${h3Wrapper.port}")
    private int h3WrapperPort;

    // The full path and filename for the keystore file.
    @Value("${h3Wrapper.keyStoreFile}")
    private File h3WrapperKeyStoreFile;

    // The password for the keystore file.
    @Value("${h3Wrapper.keyStorePassword}")
    private String h3WrapperKeyStorePassword;

    // The userName for the H3 instance.
    @Value("${h3Wrapper.userName}")
    private String h3WrapperUserName;

    // The password for the H3 instance.
    @Value("{h3Wrapper.password}")
    private String h3WrapperPassword;

    // The name of the core harvest agent listener web service.
    @Value("${harvestCoordinatorNotifier.service}")
    private String harvestCoordinatorNotifierService;

    // The host name or ip address of the core.
    @Value("${harvestCoordinatorNotifier.host}")
    private String harvestCoordinatorNotifierHost;

    // The port that the core is listening on for http connections.
    @Value("${harvestCoordinatorNotifier.port}")
    private int harvestCoordinatorNotifierPort;

    @Value("${harvestAgent.attemptHarvestRecovery}")
    private String harvestAgentAttemptHarvestRecovery;

    // The name of the digital asset store web service.
    @Value("${digitalAssetStore.service}")
    private String digitalAssetStoreService;

    // The host name or ip address of the digital asset store.
    @Value("${digitalAssetStore.host}")
    private String digitalAssetStoreHost;

    // The port that the digital asset store is listening on for http connections.
    @Value("${digitalAssetStore.port}")
    private int digitalAssetStorePort;

    // Delay before running the job measured in milliseconds.
    @Value("${heartbeatTrigger.startDelay}")
    private long heartbeatTriggerStartDelay;

    // Repeat every xx milliseconds.
    @Value("${heartbeatTrigger.repeatInterval}")
    private long heartbeatTriggerRepeatInterval;

    // Number of retries before increasing the wait time for level 1 to level 2.
    @Value("${harvestCompleteConfig.levelRetryBand}")
    private int harvestCompleteConfigLevelRetryBand;

    // Number of seconds to wait after a failure to complete a harvest (level 1).
    @Value("${harvestCompleteConfig.waitOnFailureLevelOneSecs}")
    private int harvestCompleteConfigWaitOnFailureLevelOneSecs;

    // Number of seconds to wait after a failure to complete a harvest (level 2).
    @Value("${harvestCompleteConfig.waitOnFailureLevelTwoSecs}")
    private int harvestCompleteConfigWaitOnFailureLevelTwoSecs;

    // Number of seconds to wait after the harvester says it is finished.
    @Value("${harvestCompleteConfig.waitOnCompleteSeconds}")
    private int harvestCompleteConfigWaitOnCompleteSeconds;

    // The amount of memory in KB that can be used before a warning notification is sent.
    @Value("${memoryChecker.warnThreshold}")
    private int memoryCheckerWarnThreshold;

    // The amount of memory in KB that can be used before an error notification is sent.
    @Value("${memoryChecker.errorThreshold}")
    private int memoryCheckerErrorThreshold;

    // The minimum percentage of processor available before a warning notification is sent.
    @Value("${processorCheck.warnThreshold}")
    private int processorCheckWarnThreshold;

    // The minimum percentage of processor available before an error notification is sent.
    @Value("${processorCheck.errorThreshold}")
    private int processorCheckErrorThreshold ;

    // The percentage of disk used before a warning notification is sent.
    @Value("${diskSpaceChecker.warnThreshold}")
    private int diskSpaceCheckerWarnThreshold;

    // The percentage of disk used before an error notification is sent.
    @Value("${diskSpaceChecker.errorThreshold}")
    private int diskSpaceCheckerErrorThreshold;

    // Start delay measured in milliseconds.
    @Value("${checkProcessorTrigger.startDelay}")
    private long checkProcessorTriggerStartDelay;

    // Repeat every xx milliseconds.
    @Value("${checkProcessorTrigger.repeatInterval}")
    private long checkProcessorTriggerRepeatInterval;

    @PostConstruct
    public void postConstruct() {
        // Avoid circular bean dependencies
        harvestCoordinatorNotifier().setAgent(harvestAgent());
        harvestAgent().setHarvestCoordinatorNotifier(harvestCoordinatorNotifier());
    }


    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false) // lazy-init="default" and default-lazy-init="false"
    public HarvestAgentH3 harvestAgent() {
        HarvestAgentH3 bean = new HarvestAgentH3();
        bean.setBaseHarvestDirectory(harvestAgentBaseHarvestDirectory);
        bean.setHost(harvestAgentHost);
        bean.setMaxHarvests(harvestAgentMaxHarvests);
        bean.setPort(harvestAgentPort);
        bean.setService(harvestAgentService);
        bean.setLogReaderService(harvestAgentLogReaderService);
        bean.setName(harvestAgentName);
        bean.setProvenanceNote(harvestAgentProvenanceNote);
        bean.setAlertThreshold(harvestAgentAlertThreshold);
        bean.setAllowedAgencies(new ArrayList());
        bean.setDigitalAssetStore(digitalAssetStore());
        //bean.setHarvestCoordinatorNotifier(harvestCoordinatorNotifier());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false) // lazy-init="default" and default-lazy-init="false"
    @Autowired(required = false) // autowire="default" and default-autowire="no"
    public Heritrix3WrapperConfiguration heritrix3WrapperConfiguration() {
        Heritrix3WrapperConfiguration bean = new Heritrix3WrapperConfiguration();
        bean.setHost(h3WrapperHost);
        bean.setPort(h3WrapperPort);
        bean.setKeyStoreFile(h3WrapperKeyStoreFile);
        bean.setKeyStorePassword(h3WrapperKeyStorePassword);
        bean.setUserName(h3WrapperUserName);
        bean.setPassword(h3WrapperPassword);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false) // lazy-init="default" and default-lazy-init="false"
    @Autowired(required = false) // autowire="default" and default-autowire="no"
    public HarvestCoordinatorNotifier harvestCoordinatorNotifier() {
        HarvestCoordinatorNotifier bean = new HarvestCoordinatorNotifier();
        bean.setHost(harvestCoordinatorNotifierHost);
        bean.setPort(harvestCoordinatorNotifierPort);
        //bean.setAgent(harvestAgent());
        bean.setAttemptRecovery(harvestAgentAttemptHarvestRecovery);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false) // lazy-init="default" and default-lazy-init="false"
    public DigitalAssetStore digitalAssetStore() {
        DigitalAssetStoreClient bean = new DigitalAssetStoreClient(digitalAssetStoreHost, digitalAssetStorePort,
                new RestTemplateBuilder());
        return bean;
    }

    @Bean
    public JobDetail heartbeatJob() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("harvestAgent", harvestAgent());
        jobDataMap.put("notifier", harvestCoordinatorNotifier());

        JobDetail bean = JobBuilder.newJob(HarvestAgentHeartBeatJob.class)
                .withIdentity("HeartBeat", "HeartBeatGroup")
                .usingJobData(jobDataMap)
                .build();

        return bean;
    }

    @Bean
    @DependsOn("heartbeatJob")
    public Trigger heartbeatTrigger() {
        Date startTime = new Date(System.currentTimeMillis() + heartbeatTriggerStartDelay);
        Trigger bean = TriggerBuilder.newTrigger()
                .withIdentity("HeartBeatTrigger", "HeartBeatTriggerGroup")
                .forJob(heartbeatJob())
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMilliseconds(heartbeatTriggerRepeatInterval))
                .startAt(startTime)
                .build();

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false) // lazy-init="default" and default-lazy-init="false"
    public SchedulerFactoryBean schedulerFactory() {
        SchedulerFactoryBean bean = new SchedulerFactoryBean();
        bean.setTriggers(heartbeatTrigger(), checkProcessorTrigger());

        return bean;
    }

    @Bean
    public LogReaderImpl logReader() {
        LogReaderImpl bean = new LogReaderImpl();
        bean.setLogProvider(harvestAgent());

        return bean;
    }

    @Bean
    public HarvestCompleteConfig harvestCompleteConfig() {
        HarvestCompleteConfig bean = new HarvestCompleteConfig();
        bean.setLevelRetryBand(harvestCompleteConfigLevelRetryBand);
        bean.setWaitOnFailureLevelOneSecs(harvestCompleteConfigWaitOnFailureLevelOneSecs);
        bean.setWaitOnFailureLevelTwoSecs(harvestCompleteConfigWaitOnFailureLevelTwoSecs);
        bean.setWaitOnCompleteSeconds(harvestCompleteConfigWaitOnCompleteSeconds);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false) // lazy-init="default" and default-lazy-init="false"
    public MemoryChecker memoryChecker() {
        MemoryChecker bean = new MemoryChecker();
        bean.setWarnThreshold(memoryCheckerWarnThreshold);
        bean.setErrorThreshold(memoryCheckerErrorThreshold);
        bean.setCheckType("Memory");
        bean.setNotificationSubject("Agent");
        bean.setNotifier(harvestCoordinatorNotifier());

        return bean;
    }

    // The following memory checker replaces the one above where Harvest Agents are to stop processing new harvests
    // when memory exceeds the warning level, and restart processing new harvests when memory drops below the
    // warning level.
    // TODO CONFIGURATION This bean was commented out in wct-agent.xml, which means it could be switched by changing configuration.
    // Note that in the configuration this is the same bean name "memoryChecker". So it might be better to have a
    // properties setting that switches them back and forth.
    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false) // lazy-init="default" and default-lazy-init="false"
    public HarvestAgentMemoryChecker harvestAgentMemoryChecker() {
        HarvestAgentMemoryChecker bean = new HarvestAgentMemoryChecker();
        bean.setWarnThreshold(memoryCheckerWarnThreshold);
        bean.setErrorThreshold(memoryCheckerErrorThreshold);
        bean.setCheckType("Memory");
        bean.setNotificationSubject("Agent");
        bean.setNotifier(harvestCoordinatorNotifier());
        bean.setHarvestAgent(harvestAgent());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false) // lazy-init="default" and default-lazy-init="false"
    public ProcessorCheck processorCheck() {
        ProcessorCheck bean = new ProcessorCheck();

        // TODO CONFIGURATION This must be runtime configurable
        // The pattern and command properties can be set the default setup is for linux sar -u
        // For a solaris box use this pattern to get the processor data for linux remove this line.
        bean.setPattern("(?m)\\w+:\\w+:\\w+\\s+\\S+\\s+\\S+\\s+\\S+\\s+(\\S+)$");

        bean.setWarnThreshold(processorCheckWarnThreshold);
        bean.setErrorThreshold(processorCheckErrorThreshold);
        bean.setCheckType("Processor");
        bean.setNotificationSubject("Agent");
        bean.setNotifier(harvestCoordinatorNotifier());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false) // lazy-init="default" and default-lazy-init="false"
    public DiskSpaceChecker diskSpaceChecker() {
        // List of disk mounts to check.
        ArrayList<String> diskMountsToCheck = new ArrayList<>();
        diskMountsToCheck.add("/");

        DiskSpaceChecker bean = new DiskSpaceChecker(diskSpaceCheckerWarnThreshold, diskSpaceCheckerErrorThreshold,
                diskMountsToCheck);

        bean.setCheckType("Disk Space");
        bean.setNotificationSubject("Agent");
        bean.setNotifier(harvestCoordinatorNotifier());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false) // lazy-init="default" and default-lazy-init="false"
    public CheckProcessor checkProcessor() {
        CheckProcessor bean = new CheckProcessor();

        List<Checker> checksList = new ArrayList<>();
        checksList.add(memoryChecker());
        checksList.add(diskSpaceChecker());
        bean.setChecks(checksList);

        return bean;
    }

    @Bean
    public MethodInvokingJobDetailFactoryBean checkProcessorJob() {
        MethodInvokingJobDetailFactoryBean bean = new MethodInvokingJobDetailFactoryBean();
        bean.setTargetObject(checkProcessor());
        bean.setTargetMethod("check");

        return bean;
    }

    @Bean
    public Trigger checkProcessorTrigger() {
        Date startTime = new Date(System.currentTimeMillis() + checkProcessorTriggerStartDelay);
        Trigger bean = TriggerBuilder.newTrigger()
                .withIdentity("CheckProcessorTrigger", "CheckProcessorTriggerGroup")
                .forJob(checkProcessorJob().getObject())
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMilliseconds(checkProcessorTriggerRepeatInterval))
                .startAt(startTime)
                .build();

        return bean;
    }
}
