package org.webcurator.ui.groups.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.webcurator.ui.target.controller.EditScheduleController;
import org.webcurator.ui.util.TabbedController;

import javax.annotation.PostConstruct;

@Controller
@RequestMapping("/curator/groups/schedule.html")
public class GroupsEditScheduleController extends EditScheduleController {
    @Autowired
    private ApplicationContext context;

    @PostConstruct
    protected void init() {
        setContextSessionKey("groupEditorContext");
        setScheduleEditPrivilege("MANAGE_GROUP_SCHEDULE");
        setTargetController((TabbedController) context.getBean("groupsController"));
        setViewPrefix("groups");
    }
}
