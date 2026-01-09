package org.webcurator.serv;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class StaticFileReaderController {
    @RequestMapping(value = { "/{path:[^\\.]*}", "/**/{path:[^\\.]*}" })
    public String forward() {
        // Forward to index.html so that route is handled by Vue
        return "forward:/index.html";
    }
}