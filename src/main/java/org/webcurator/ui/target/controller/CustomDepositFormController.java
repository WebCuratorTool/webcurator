package org.webcurator.ui.target.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.ui.archive.ArchiveCommand;
import org.webcurator.common.ui.Constants;

@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
@CrossOrigin
@RequestMapping("curator/target/deposit-form-envelope.html")
public class CustomDepositFormController {
    /** the logger. */
    private Log log;

	public CustomDepositFormController() {
        log = LogFactory.getLog(getClass());
	}

	@RequestMapping(method = {RequestMethod.POST, RequestMethod.GET})
	protected ModelAndView handle(@RequestParam("harvestResultNumber") Long harvestResultNumber,
								  @RequestParam("targetInstanceID") Long targetInstanceId) throws Exception {
		ArchiveCommand command = new ArchiveCommand();
		command.setTargetInstanceID(targetInstanceId.intValue());
		ModelAndView mav = new ModelAndView("deposit-form-envelope");
		mav.addObject(Constants.GBL_CMD_DATA, command);
		return mav;
	}
}
