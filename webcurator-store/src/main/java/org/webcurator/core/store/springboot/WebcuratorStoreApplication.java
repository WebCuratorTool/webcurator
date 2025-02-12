package org.webcurator.core.store.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.webcurator.core.coordinator.WctCoordinator;
import org.webcurator.core.harvester.coordinator.HarvestAgentListenerService;
import org.webcurator.core.store.arc.ArcDigitalAssetStoreService;
import org.webcurator.core.visualization.VisualizationProgressView;
import org.webcurator.domain.model.core.HarvestResultDTO;

import java.util.Arrays;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"org.webcurator.store",
        "org.webcurator.core.harvester",
        "org.webcurator.core.rest",
        "org.webcurator.core.reader",
        "org.webcurator.core.store.arc",
        "org.webcurator.core.visualization",
        "org.webcurator.core.screenshot"},
// HarvestAgentListenerService should be running on webcurator-webapp.
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {HarvestAgentListenerService.class, WctCoordinator.class})
)
public class WebcuratorStoreApplication {
    @Autowired
    private ArcDigitalAssetStoreService arcDigitalAssetStoreService;

    public static void main(String[] args) {
        try {
            SpringApplication.run(WebcuratorStoreApplication.class, args);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            // Note that this is just here for debugging purposes. It can be deleted at any time.
            System.out.println("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }

            HarvestResultDTO hr = new HarvestResultDTO();
            hr.setOid(1L);
            hr.setHarvestNumber(1);
            hr.setTargetInstanceOid(1000365177L);
            hr.setProgressView(new VisualizationProgressView());
            arcDigitalAssetStoreService.initiateIndexing(hr);
            System.out.println("Launched an indexing job");
        };
    }
}
