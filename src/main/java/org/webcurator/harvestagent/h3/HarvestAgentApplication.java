package org.webcurator.harvestagent.h3;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.webcurator.core.harvester.coordinator.HarvestAgentListenerService;
import org.webcurator.core.harvester.coordinator.HarvestCoordinatorImpl;

import java.util.Arrays;

@SpringBootApplication
@ComponentScan(basePackages = { "org.webcurator.harvestagent.h3", "org.webcurator.core.harvester", "org.webcurator.core.rest" },
        // HarvestAgentListenerService should be running on webcurator-webapp.
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = { HarvestAgentListenerService.class, HarvestCoordinatorImpl.class })
)
public class HarvestAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(HarvestAgentApplication.class, args);
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
        };
    }

}
