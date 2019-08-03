package org.webcurator.ui.target.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.ui.archive.ArchiveCommand;
import org.webcurator.common.Constants;

@Controller
@RequestMapping("/curator/target/annotation-ajax.html")
public class CustomDepositFormController {
    /** the logger. */
    private Log log;
	public CustomDepositFormController() {
        log = LogFactory.getLog(getClass());
	}

	@PostMapping
	protected ModelAndView handle(@RequestParam("targetOid") Long targetOid,
								  @RequestParam("targetInstanceOid") Long targetInstanceOid) throws Exception {
		ArchiveCommand command = new ArchiveCommand();
		command.setTargetInstanceID(targetInstanceOid.intValue());
		ModelAndView mav = new ModelAndView("deposit-form-envelope");
		mav.addObject(Constants.GBL_CMD_DATA, command);
		return mav;
	}

}
