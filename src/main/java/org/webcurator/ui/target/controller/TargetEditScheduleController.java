package org.webcurator.ui.target.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.ui.target.command.TargetSchedulesCommand;
import org.webcurator.ui.util.TabbedController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
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

    @InitBinder
    public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request,binder);
    }

    @RequestMapping(value = "/curator/target/schedule.html",method = {RequestMethod.GET,RequestMethod.POST})
    protected ModelAndView handle(@Validated @ModelAttribute("targetSchedulesCommand") TargetSchedulesCommand command,
                                  HttpServletRequest request, HttpServletResponse response, BindingResult bindingResult)            throws Exception {
        return  super.handle(command,request,response,bindingResult);
    }
}
