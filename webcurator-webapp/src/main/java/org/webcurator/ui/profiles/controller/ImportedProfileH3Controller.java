package org.webcurator.ui.profiles.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.ui.profiles.command.DefaultCommand;
import org.webcurator.ui.util.TabConfig;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class ImportedProfileH3Controller extends ProfileController {
    @Autowired
    private ApplicationContext context;

    @PostConstruct
    protected void init() {
        setDefaultCommandClass(DefaultCommand.class);
        setTabConfig((TabConfig) context.getBean("importedProfileH3TabConfig"));
    }

    @Override
    @RequestMapping(path = "/curator/profiles/imported-profilesH3.html", method = {RequestMethod.GET, RequestMethod.POST})
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return super.handleRequestInternal(request, response);
    }

}
