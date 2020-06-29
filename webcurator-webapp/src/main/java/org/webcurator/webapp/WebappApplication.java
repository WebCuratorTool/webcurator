package org.webcurator.webapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Arrays;

@SpringBootApplication
@ComponentScan(basePackages = {"org.webcurator.webapp",
        "org.webcurator.ui",
        "org.webcurator.core.coordinator",
        "org.webcurator.core.harvester.coordinator",
        "org.webcurator.core.rest",
        "org.webcurator.core.reader",
        "org.webcurator.core.harvester.agent.HarvestAgentClient",
        "org.webcurator.core.visualization.networkmap.service.NetworkMapController",
        "org.webcurator.domain.VisualizationImportedFileDAOImpl"}//,
        // Put any exclusions here.
        //excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ClassToExclude.class)
)
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class WebappApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(WebappApplication.class, args);
        } catch (Throwable e) {
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
        };
    }
}
