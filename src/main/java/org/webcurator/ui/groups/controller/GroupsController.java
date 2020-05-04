package org.webcurator.ui.groups.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.ui.groups.command.DefaultCommand;
import org.webcurator.ui.util.TabConfig;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class GroupsController extends TabbedGroupController{
    @Autowired
    private ApplicationContext ctx;

    @PostConstruct
    protected void init() {
        setDefaultCommandClass(DefaultCommand.class);
        setTabConfig((TabConfig) ctx.getBean("groupsTabConfig"));
    }

    @Override
    @RequestMapping(path = "/curator/groups/groups.html", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return super.handleRequestInternal(request, response);
    }
}
