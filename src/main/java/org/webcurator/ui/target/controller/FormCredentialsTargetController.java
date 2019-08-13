package org.webcurator.ui.target.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.webcurator.ui.util.OverrideGetter;
import org.webcurator.ui.util.TabbedController;

import javax.annotation.PostConstruct;

@Controller
@RequestMapping("/curator/target/target-form-credentials.html")
public class FormCredentialsTargetController extends ProfileFormCredentialsController {
    @Autowired
    private ApplicationContext context;

    @PostConstruct
    protected void init() {
        setTabbedController((TabbedController) context.getBean("tabbedTargetController"));
        setOverrideGetter((OverrideGetter) context.getBean("targetOverrideGetter"));
        setUrlPrefix("target");
    }

}
