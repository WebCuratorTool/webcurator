package org.webcurator.ui.target.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.webcurator.ui.util.OverrideGetter;
import org.webcurator.ui.util.TabbedController;

import javax.annotation.PostConstruct;

@Controller
@RequestMapping("/curator/target/ti-form-credentials.html")
public class FormCredentialsTargetInstanceController extends ProfileFormCredentialsController {
    @Autowired
    private ApplicationContext context;

    @PostConstruct
    protected void init() {
        setTabbedController((TabbedController) context.getBean("tabbedTargetInstanceController"));
        setOverrideGetter((OverrideGetter) context.getBean("targetInstanceOverrideGetter"));
        setUrlPrefix("ti");
    }

}
