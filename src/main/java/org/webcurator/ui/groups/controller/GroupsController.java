package org.webcurator.ui.groups.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.webcurator.ui.groups.command.DefaultCommand;
import org.webcurator.ui.util.TabConfig;

import javax.annotation.PostConstruct;

@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
@RequestMapping("/curator/groups/groups.html")
public class GroupsController extends TabbedGroupController {
    @Autowired
    private ApplicationContext context;

    @PostConstruct
    protected void init() {
        setDefaultCommandClass(DefaultCommand.class);
        setTabConfig((TabConfig) context.getBean("groupsTabConfig"));
        setSearchController((GroupSearchController) context.getBean("groupSearchController"));
    }

}
