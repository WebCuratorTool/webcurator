package org.webcurator.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.webcurator.webapp", "org.webcurator.ui", "org.webcurator.core.harvester.coordinator",
        "org.webcurator.core.rest", "org.webcurator.core.reader", "org.webcurator.core.harvester.agent.HarvestAgentClient"}//,
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
}
