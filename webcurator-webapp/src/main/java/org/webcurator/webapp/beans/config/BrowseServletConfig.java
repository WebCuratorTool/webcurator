package org.webcurator.webapp.beans.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate5.support.OpenSessionInViewInterceptor;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.webcurator.core.visualization.browser.BrowseHelper;

import java.util.*;

/**
 * Contains configuration that used to be found in {@code wct-browse-servlet.xml}. This
 * is part of the change to move to using annotations for Spring instead of
 * XML files.
 */
@Configuration
public class BrowseServletConfig {

    @Value("${browseHelper.prefix}")
    private String browseHelperPrefix;

    @Value("${browse.double_escape}")
    private boolean browseDoubleEscape;

    @Autowired
    private BaseConfig baseConfig;

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public OpenSessionInViewInterceptor openSessionInViewInterceptorBrowse() {
        OpenSessionInViewInterceptor bean = new OpenSessionInViewInterceptor();
        bean.setSessionFactory(baseConfig.sessionFactory().getObject());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public BrowseHelper browseHelper() {
        return BrowseHelper.browseHelper(browseHelperPrefix, browseDoubleEscape);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public SimpleUrlHandlerMapping simpleUrlMappingBrowse() {
        SimpleUrlHandlerMapping bean = new SimpleUrlHandlerMapping();
        bean.setInterceptors(new Object[]{openSessionInViewInterceptorBrowse()});

        Properties properties = new Properties();
        properties.setProperty("**", "browseController");
        bean.setMappings(properties);

        return bean;
    }
}
