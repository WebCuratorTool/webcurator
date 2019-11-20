package org.webcurator.ui.target.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.webcurator.ui.util.TabbedController;

import javax.annotation.PostConstruct;

@Controller
@RequestMapping("/curator/target/schedule.html")
public class TargetEditScheduleController extends EditScheduleController {
    @Autowired
    private ApplicationContext context;

    @PostConstruct
    protected void init() {
        setContextSessionKey("targetEditorContext");
        setScheduleEditPrivilege("ADD_SCHEDULE_TO_TARGET");
        setTargetController((TabbedController) context.getBean("tabbedTargetController"));
        setViewPrefix("target");
    }
}
