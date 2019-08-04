package org.webcurator.ui.target.controller;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.harvester.coordinator.HarvestCoordinator;
import org.webcurator.core.harvester.coordinator.MockHarvestCoordinator;
import org.webcurator.core.scheduler.MockTargetInstanceManager;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.test.BaseWCTTest;
import org.webcurator.common.Constants;
import org.webcurator.ui.target.command.LogReaderCommand;

public class AQAReaderControllerTest extends BaseWCTTest<AQAReaderController>{

	private TargetInstanceManager tim = null;
	private HarvestCoordinator hc = null;

	public AQAReaderControllerTest()
	{
		super(AQAReaderController.class,
                "/org/webcurator/ui/target/controller/LogReaderControllerTest.xml");
	}

	public void setUp() throws Exception
	{
		super.setUp();
		tim = new MockTargetInstanceManager(testFile);
		hc = new MockHarvestCoordinator();
	}


	@Test
	public final void testHandle() {
		try
		{
			testInstance.setTargetInstanceManager(tim);
			testInstance.setHarvestCoordinator(hc);

			LogReaderCommand aCmd = new LogReaderCommand();
			TargetInstance ti = tim.getTargetInstance(5000L);

			aCmd.setTargetInstanceOid(ti.getOid());
			aCmd.setLogFileName("aqa-report(1).xml");

            BindingResult bindingResult = new BindException(aCmd, "LogReaderCommand");

			ModelAndView mav = testInstance.handle(aCmd, bindingResult);
			assertTrue(mav != null);
			assertNotNull((List<AQAReaderController.AQAElement>)mav.getModel().get(LogReaderCommand.MDL_MISSINGELEMENTS));
			List<AQAReaderController.AQAElement> result = (List<AQAReaderController.AQAElement>)mav.getModel().get(LogReaderCommand.MDL_MISSINGELEMENTS);
			assertEquals(result.get(0).getUrl(), "http://images.icnetwork.co.uk/robots.txt");
			assertEquals(result.get(0).getContentFile(), "100077.txt");
			assertTrue(Constants.VIEW_AQA_READER.equals(mav.getViewName()));
			assertFalse(bindingResult.hasErrors());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

}
