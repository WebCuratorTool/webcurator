package org.webcurator.ui.groups.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.ui.target.command.TargetSchedulesCommand;
import org.webcurator.ui.target.controller.EditScheduleController;
import org.webcurator.ui.util.TabbedController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    @InitBinder
    public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request,binder);
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.GET})
    protected ModelAndView handle(@Validated @ModelAttribute("targetSchedulesCommand") TargetSchedulesCommand command,
                                  HttpServletRequest request, HttpServletResponse response, BindingResult bindingResult)
            throws Exception {
        return super.handle(command, request, response, bindingResult);
    }
}
