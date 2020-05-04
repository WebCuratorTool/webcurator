package org.webcurator.webapp.beans.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Contains configuration that used to be found in {@code security-userdn-list.xml}. This
 * is part of the change to move to using annotations for Spring instead of
 * XML files.
 */
@Configuration
public class SecurityUserDnListConfig {
    private static Logger LOGGER = LoggerFactory.getLogger(SecurityUserDnListConfig.class);

    @Bean
    public ListFactoryBean userDnList() {
        ListFactoryBean bean = new ListFactoryBean();
        bean.setTargetListClass(ArrayList.class);

        // TODO Add values to this list to allow more distinguished name patterns.
        //      If preferable, ldap.dn can be removed when there is more than one value to avoid confusion.
        List<String> sourceList = new ArrayList<String>(Arrays.asList("${ldap.dn}",
                "cn={0},OU=WCT users,DC=webcurator,DC=org") //,
                // TODO CONFIGURATION
                //"cn={0},OU=WCT users,DC=webcurator,DC=org",
                //"cn={0},OU=WCT users,DC=webcurator,DC=org"
        );
        bean.setSourceList(sourceList);

        return bean;
    }
}
