package org.webcurator.ui.target.controller;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
@RequestMapping("/curator/target/target.html")
public class TargetController extends TabbedTargetController {
    @Override
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return super.handleRequestInternal(request, response);
    }
}
