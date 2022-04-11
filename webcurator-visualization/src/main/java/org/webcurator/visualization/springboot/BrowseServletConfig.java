package org.webcurator.visualization.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.webcurator.core.visualization.browser.BrowseHelper;


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
    private MainConfig baseConfig;

    @Bean
    public BrowseHelper browseHelper() {
        return BrowseHelper.browseHelper(browseHelperPrefix,browseDoubleEscape);
    }
}