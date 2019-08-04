package org.webcurator.ui.target.controller;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.test.BaseWCTTest;
import org.webcurator.ui.archive.ArchiveCommand;
import org.webcurator.common.Constants;

public class CustomDepositFormControllerTest extends BaseWCTTest<CustomDepositFormController>{

	public CustomDepositFormControllerTest()
	{
		super(CustomDepositFormController.class, null);
	}

	@Test
	public void testHandleHttpServletRequestHttpServletResponseObjectBindException() {
		ArchiveCommand comm = new ArchiveCommand();
		comm.setHarvestResultNumber(1);
		comm.setTargetInstanceID(5001);

		ModelAndView mav = null;;
		try {
			mav = testInstance.handle(1L, 5001L);
		} catch (Exception e) {
			fail(e.getMessage());
			return;
		}
		assertNotNull(mav);
		assertEquals("deposit-form-envelope", mav.getViewName());
		assertNotNull(mav.getModel().get(Constants.GBL_CMD_DATA));
		assertEquals(1, ((ArchiveCommand)mav.getModel().get(Constants.GBL_CMD_DATA)).getHarvestResultNumber());
		assertEquals(5001, ((ArchiveCommand)mav.getModel().get(Constants.GBL_CMD_DATA)).getTargetInstanceID());
		assertNull(mav.getModel().get("hasErrors"));
	}

}
