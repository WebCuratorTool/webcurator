package org.webcurator.ui.target.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.webcurator.ui.common.controller.ProfileBasicCredentialsController;
import org.webcurator.ui.common.validation.ProfilesBasicCredentialsValidator;
import org.webcurator.ui.util.OverrideGetter;

import javax.annotation.PostConstruct;

@Controller
@RequestMapping(value = "/curator/target/target-basic-credentials.html")
public class BasicCredentialsTargetController extends ProfileBasicCredentialsController {
    @PostConstruct
    protected void init() {
        setTabbedController(context.getBean("targetController", TabbedTargetController.class));
        setOverrideGetter(context.getBean("targetOverrideGetter", OverrideGetter.class));
        setUrlPrefix("target");
        setValidator(new ProfilesBasicCredentialsValidator());
    }
}
