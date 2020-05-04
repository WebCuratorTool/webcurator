package org.webcurator.ui.groups.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.webcurator.ui.common.controller.ProfileFormCredentialsController;
import org.webcurator.ui.common.validation.ProfilesFormCredentialsValidator;
import org.webcurator.ui.util.OverrideGetter;
import org.webcurator.ui.util.TabbedController;

import javax.annotation.PostConstruct;

@Controller
@RequestMapping("/curator/target/group-form-credentials.html")
public class FormCredentialsGroupController extends ProfileFormCredentialsController {
    @PostConstruct
    protected void init() {
        setTabbedController((TabbedController) context.getBean("groupsController"));
        setOverrideGetter((OverrideGetter) context.getBean("groupOverrideGetter"));
        setUrlPrefix("group");
        setValidator(new ProfilesFormCredentialsValidator());
    }

}
