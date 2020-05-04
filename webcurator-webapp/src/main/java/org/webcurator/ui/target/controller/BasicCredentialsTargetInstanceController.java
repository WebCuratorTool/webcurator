package org.webcurator.ui.target.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.webcurator.ui.common.controller.ProfileBasicCredentialsController;
import org.webcurator.ui.common.validation.ProfilesBasicCredentialsValidator;
import org.webcurator.ui.util.OverrideGetter;

import javax.annotation.PostConstruct;

@Controller
@RequestMapping(value = "/curator/target/ti-basic-credentials.html")
public class BasicCredentialsTargetInstanceController extends ProfileBasicCredentialsController {
    @PostConstruct
    protected void init() {
        setTabbedController(context.getBean("tabbedTargetInstanceController", TabbedTargetInstanceController.class));
        setOverrideGetter(context.getBean("targetInstanceOverrideGetter", OverrideGetter.class));
        setUrlPrefix("ti");
        setValidator(context.getBean("basicCredentialsValidatorti", ProfilesBasicCredentialsValidator.class));
    }
}
