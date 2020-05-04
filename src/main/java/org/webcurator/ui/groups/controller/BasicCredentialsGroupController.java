package org.webcurator.ui.groups.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.webcurator.ui.common.controller.ProfileBasicCredentialsController;
import org.webcurator.ui.common.validation.ProfilesBasicCredentialsValidator;
import org.webcurator.ui.util.OverrideGetter;

import javax.annotation.PostConstruct;

@Controller
@RequestMapping(value = "/curator/target/group-basic-credentials.html")
public class BasicCredentialsGroupController extends ProfileBasicCredentialsController {
    @PostConstruct
    protected void init() {
        setTabbedController(context.getBean("groupsController", TabbedGroupController.class));
        setOverrideGetter(context.getBean("groupOverrideGetter", OverrideGetter.class));
        setUrlPrefix("group");
        setValidator(context.getBean("basicCredentialsValidatorGroup", ProfilesBasicCredentialsValidator.class));
    }
}
