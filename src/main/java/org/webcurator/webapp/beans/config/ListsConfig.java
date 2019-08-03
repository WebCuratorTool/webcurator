package org.webcurator.webapp.beans.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.webcurator.core.common.WCTTreeSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains configuration that used to be found in {@code wct-core-lists.xml}. This
 * is part of the change to move to using annotations for Spring instead of
 * XML files.
 */
@Configuration
public class ListsConfig {

    @Value("${groupTypes.subgroup}")
    private String groupTypesSubgroup;

    @Bean
    public List accessStatusList() {
        List bean = new ArrayList();
        bean.add("Open (unrestricted) access");
        bean.add("Limited to 3 concurrent users");
        bean.add("Restricted by location");
        bean.add("Restricted by person");
        bean.add("Embargoed");

        return bean;
    }

    // The values in this map should be one of:
    // ACR_OPA (open access)
    // ACR_ONS (restricted by number of concurrent users)
    // ACR_OSR (restricted to a site)
    // ACR_RES (authorized users only)
    //
    // These are converted to Rosetta policy codes in OmsCodeToMetsMapping
    @Bean
    public Map accessStatusMap() {
        Map bean = new HashMap();
        bean.put("Open (unrestricted) access", "ACR_OPA");
        bean.put("Limited to 3 concurrent users", "ACR_OSR");
        bean.put("Restricted by location", "ACR_ONS");
        bean.put("Restricted by person", "ACR_RES");

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public WCTTreeSet dublinCoreTypesList() {
        List<String> initialList = new ArrayList<>();
        initialList.add("");
        initialList.add("Collection");
        initialList.add("Dataset");
        initialList.add("Event");
        initialList.add("Image");
        initialList.add("Interactive Resource");
        initialList.add("Moving Image");
        initialList.add("Physical Object");
        initialList.add("Service");
        initialList.add("Software");
        initialList.add("Sound");
        initialList.add("Still Image");
        initialList.add("Text");
        initialList.add("eSerial");

        WCTTreeSet bean = new WCTTreeSet(initialList, 50);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public WCTTreeSet selectionTypesList() {
        List<String> initialList = new ArrayList<>();
        initialList.add("Producer type");
        initialList.add("Publication type");
        initialList.add("Collection");
        initialList.add("Area");
        initialList.add("Other collections");

        WCTTreeSet bean = new WCTTreeSet(initialList, 50);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public WCTTreeSet harvestTypesList() {
        List<String> initialList = new ArrayList<>();
        initialList.add("Subject");
        initialList.add("Event");
        initialList.add("Theme");

        WCTTreeSet bean = new WCTTreeSet(initialList, 50);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public WCTTreeSet groupTypesList() {
        List<String> initialList = new ArrayList<>();
        initialList.add("");
        initialList.add("Collection");
        initialList.add("Subject");
        initialList.add("Thematic");
        initialList.add("Event");
        initialList.add("Functional");
        initialList.add(groupTypesSubgroup);

        WCTTreeSet bean = new WCTTreeSet(initialList, 50);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public WCTTreeSet subGroupParentTypesList() {
        List<String> initialList = new ArrayList<>();
        initialList.add("Collection");
        initialList.add("Thematic");
        initialList.add("Event");

        WCTTreeSet bean = new WCTTreeSet(initialList, 50);

        return bean;
    }
}
