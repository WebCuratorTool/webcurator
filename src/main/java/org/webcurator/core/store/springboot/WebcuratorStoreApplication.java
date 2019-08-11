package org.webcurator.core.store.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "org.webcurator.store", "org.webcurator.core.rest" })
public class WebcuratorStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebcuratorStoreApplication.class, args);
    }
}
