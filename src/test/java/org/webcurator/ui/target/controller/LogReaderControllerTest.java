package org.webcurator.ui.target.controller;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.test.*;
import org.webcurator.common.Constants;
import org.webcurator.ui.target.command.LogReaderCommand;
import org.webcurator.core.harvester.coordinator.*;
import org.webcurator.core.scheduler.*;
import org.webcurator.domain.model.core.*;

public class LogReaderControllerTest extends BaseWCTTest<LogReaderController>{

	private TargetInstanceManager tim = null;
	private HarvestCoordinator hc = null;

	public LogReaderControllerTest()
	{
		super(LogReaderController.class,
				"src/test/java/org/webcurator/ui/target/controller/LogReaderControllerTest.xml");
	}

	public void setUp() throws Exception
	{
		super.setUp();
		tim = new MockTargetInstanceManager(testFile);
		hc = new MockHarvestCoordinator();
	}

	private int countReturnedLines(String[] result)
	{
		assertNotNull(result);
		assertTrue(result.length == 2);
		if(result[0].equals(""))
		{
			return 0;
		}
		else
		{
			String[] lines = result[0].split("\n");
			return lines.length;
		}
	}

	@Test
	public final void testHandleHead() {
		try
		{
			testInstance.setTargetInstanceManager(tim);
			testInstance.setHarvestCoordinator(hc);

			LogReaderCommand aCmd = new LogReaderCommand();
			TargetInstance ti = tim.getTargetInstance(5000L);

			aCmd.setTargetInstanceOid(ti.getOid());
			aCmd.setLogFileName("crawl.log");
			aCmd.setFilterType(LogReaderCommand.VALUE_HEAD);
			aCmd.setShowLineNumbers(true);

			BindingResult bindingResult = new BindException(aCmd, "LogReaderCommand");

			ModelAndView mav = testInstance.handle(aCmd, bindingResult);
			assertTrue(mav != null);
			assertNotNull((String[])mav.getModel().get(LogReaderCommand.MDL_LINES));
			String[] result = (String[])mav.getModel().get(LogReaderCommand.MDL_LINES);
			assertTrue(countReturnedLines(result) == 700);
			assertEquals(result[0].substring(0,3), "1. ");
			assertTrue(Constants.VIEW_LOG_READER.equals(mav.getViewName()));
			assertFalse(bindingResult.hasErrors());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testHandleTail() {
		try
		{
			testInstance.setTargetInstanceManager(tim);
			testInstance.setHarvestCoordinator(hc);

			LogReaderCommand aCmd = new LogReaderCommand();
			TargetInstance ti = tim.getTargetInstance(5000L);

			aCmd.setTargetInstanceOid(ti.getOid());
			aCmd.setLogFileName("crawl.log");
			aCmd.setFilterType(LogReaderCommand.VALUE_TAIL);
			aCmd.setShowLineNumbers(true);

            BindingResult bindingResult = new BindException(aCmd, "LogReaderCommand");

			ModelAndView mav = testInstance.handle(aCmd, bindingResult);
			assertTrue(mav != null);
			assertNotNull((String[])mav.getModel().get(LogReaderCommand.MDL_LINES));
			String[] result = (String[])mav.getModel().get(LogReaderCommand.MDL_LINES);
			assertTrue(countReturnedLines(result) == 700);
			assertEquals(result[0].substring(0,6), "4602. ");
			assertTrue(Constants.VIEW_LOG_READER.equals(mav.getViewName()));
			assertFalse(bindingResult.hasErrors());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testHandleFromLine() {
		try
		{
			testInstance.setTargetInstanceManager(tim);
			testInstance.setHarvestCoordinator(hc);

			LogReaderCommand aCmd = new LogReaderCommand();
			TargetInstance ti = tim.getTargetInstance(5000L);

			aCmd.setTargetInstanceOid(ti.getOid());
			aCmd.setLogFileName("crawl.log");
			aCmd.setFilterType(LogReaderCommand.VALUE_FROM_LINE);
			aCmd.setFilter("5000");
			aCmd.setShowLineNumbers(true);

            BindingResult bindingResult = new BindException(aCmd, "LogReaderCommand");

			ModelAndView mav = testInstance.handle(aCmd, bindingResult);
			assertTrue(mav != null);
			assertNotNull((String[])mav.getModel().get(LogReaderCommand.MDL_LINES));
			String[] result = (String[])mav.getModel().get(LogReaderCommand.MDL_LINES);
			assertTrue(countReturnedLines(result) == 302);
			assertEquals(result[0].substring(0,6), "5000. ");
			assertTrue(Constants.VIEW_LOG_READER.equals(mav.getViewName()));
			assertFalse(bindingResult.hasErrors());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testHandleTimestamp1() {
		try
		{
			testInstance.setTargetInstanceManager(tim);
			testInstance.setHarvestCoordinator(hc);

			LogReaderCommand aCmd = new LogReaderCommand();
			TargetInstance ti = tim.getTargetInstance(5000L);

			aCmd.setTargetInstanceOid(ti.getOid());
			aCmd.setLogFileName("crawl.log");
			aCmd.setFilterType(LogReaderCommand.VALUE_TIMESTAMP);
			aCmd.setFilter("2008-06-18T06:25:29");
			aCmd.setShowLineNumbers(true);

            BindingResult bindingResult = new BindException(aCmd, "LogReaderCommand");

			ModelAndView mav = testInstance.handle(aCmd, bindingResult);
			assertTrue(mav != null);
			assertNotNull((String[])mav.getModel().get(LogReaderCommand.MDL_LINES));
			String[] result = (String[])mav.getModel().get(LogReaderCommand.MDL_LINES);
			assertTrue(countReturnedLines(result) == 4);
			assertEquals(result[0].substring(0,6), "5298. ");
			assertTrue(Constants.VIEW_LOG_READER.equals(mav.getViewName()));
			assertFalse(bindingResult.hasErrors());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testHandleTimestamp2() {
		try
		{
			testInstance.setTargetInstanceManager(tim);
			testInstance.setHarvestCoordinator(hc);

			LogReaderCommand aCmd = new LogReaderCommand();
			TargetInstance ti = tim.getTargetInstance(5000L);

			aCmd.setTargetInstanceOid(ti.getOid());
			aCmd.setLogFileName("crawl.log");
			aCmd.setFilterType(LogReaderCommand.VALUE_TIMESTAMP);
			aCmd.setFilter("2008-06-18");
			aCmd.setShowLineNumbers(true);

            BindingResult bindingResult = new BindException(aCmd, "LogReaderCommand");

			ModelAndView mav = testInstance.handle(aCmd, bindingResult);
			assertTrue(mav != null);
			assertNotNull((String[])mav.getModel().get(LogReaderCommand.MDL_LINES));
			String[] result = (String[])mav.getModel().get(LogReaderCommand.MDL_LINES);
			assertTrue(countReturnedLines(result) == 700);
			assertEquals(result[0].substring(0,3), "1. ");
			assertTrue(Constants.VIEW_LOG_READER.equals(mav.getViewName()));
			assertFalse(bindingResult.hasErrors());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testHandleTimestamp3() {
		try
		{
			testInstance.setTargetInstanceManager(tim);
			testInstance.setHarvestCoordinator(hc);

			LogReaderCommand aCmd = new LogReaderCommand();
			TargetInstance ti = tim.getTargetInstance(5000L);

			aCmd.setTargetInstanceOid(ti.getOid());
			aCmd.setLogFileName("crawl.log");
			aCmd.setFilterType(LogReaderCommand.VALUE_TIMESTAMP);
			aCmd.setFilter("18/06/2008 06:25:29");
			aCmd.setShowLineNumbers(true);

            BindingResult bindingResult = new BindException(aCmd, "LogReaderCommand");

			ModelAndView mav = testInstance.handle(aCmd, bindingResult);
			assertTrue(mav != null);
			assertNotNull((String[])mav.getModel().get(LogReaderCommand.MDL_LINES));
			String[] result = (String[])mav.getModel().get(LogReaderCommand.MDL_LINES);
			assertTrue(countReturnedLines(result) == 4);
			assertEquals(result[0].substring(0,6), "5298. ");
			assertTrue(Constants.VIEW_LOG_READER.equals(mav.getViewName()));
			assertFalse(bindingResult.hasErrors());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testHandleTimestamp4() {
		try
		{
			testInstance.setTargetInstanceManager(tim);
			testInstance.setHarvestCoordinator(hc);

			LogReaderCommand aCmd = new LogReaderCommand();
			TargetInstance ti = tim.getTargetInstance(5000L);

			aCmd.setTargetInstanceOid(ti.getOid());
			aCmd.setLogFileName("crawl.log");
			aCmd.setFilterType(LogReaderCommand.VALUE_TIMESTAMP);
			aCmd.setFilter("18/06/2008");
			aCmd.setShowLineNumbers(true);

            BindingResult bindingResult = new BindException(aCmd, "LogReaderCommand");

			ModelAndView mav = testInstance.handle(aCmd, bindingResult);
			assertTrue(mav != null);
			assertNotNull((String[])mav.getModel().get(LogReaderCommand.MDL_LINES));
			String[] result = (String[])mav.getModel().get(LogReaderCommand.MDL_LINES);
			assertTrue(countReturnedLines(result) == 700);
			assertEquals(result[0].substring(0,3), "1. ");
			assertTrue(Constants.VIEW_LOG_READER.equals(mav.getViewName()));
			assertFalse(bindingResult.hasErrors());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testHandleTimestamp5() {
		try
		{
			testInstance.setTargetInstanceManager(tim);
			testInstance.setHarvestCoordinator(hc);

			LogReaderCommand aCmd = new LogReaderCommand();
			TargetInstance ti = tim.getTargetInstance(5000L);

			aCmd.setTargetInstanceOid(ti.getOid());
			aCmd.setLogFileName("crawl.log");
			aCmd.setFilterType(LogReaderCommand.VALUE_TIMESTAMP);
			aCmd.setFilter("20080618062529");
			aCmd.setShowLineNumbers(true);

            BindingResult bindingResult = new BindException(aCmd, "LogReaderCommand");

			ModelAndView mav = testInstance.handle(aCmd, bindingResult);
			assertTrue(mav != null);
			assertNotNull((String[])mav.getModel().get(LogReaderCommand.MDL_LINES));
			String[] result = (String[])mav.getModel().get(LogReaderCommand.MDL_LINES);
			assertTrue(countReturnedLines(result) == 4);
			assertEquals(result[0].substring(0,6), "5298. ");
			assertTrue(Constants.VIEW_LOG_READER.equals(mav.getViewName()));
			assertFalse(bindingResult.hasErrors());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testHandleTimestamp6() {
		try
		{
			testInstance.setTargetInstanceManager(tim);
			testInstance.setHarvestCoordinator(hc);

			LogReaderCommand aCmd = new LogReaderCommand();
			TargetInstance ti = tim.getTargetInstance(5000L);

			aCmd.setTargetInstanceOid(ti.getOid());
			aCmd.setLogFileName("crawl.log");
			aCmd.setFilterType(LogReaderCommand.VALUE_TIMESTAMP);
			aCmd.setFilter("bad format");

            BindingResult bindingResult = new BindException(aCmd, "LogReaderCommand");

			ModelAndView mav = testInstance.handle(aCmd, bindingResult);
			assertTrue(mav != null);
			assertNotNull((String[])mav.getModel().get(LogReaderCommand.MDL_LINES));
			String[] result = (String[])mav.getModel().get(LogReaderCommand.MDL_LINES);
			assertTrue(countReturnedLines(result) == 0);
			assertTrue(Constants.VIEW_LOG_READER.equals(mav.getViewName()));
			assertNotNull((String)mav.getModel().get(Constants.MESSAGE_TEXT));
			assertTrue(((String)mav.getModel().get(Constants.MESSAGE_TEXT)).equals("bad format is not a valid date/time format"));
			assertFalse(bindingResult.hasErrors());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testHandleRegexMatch() {
		try
		{
			testInstance.setTargetInstanceManager(tim);
			testInstance.setHarvestCoordinator(hc);

			LogReaderCommand aCmd = new LogReaderCommand();
			TargetInstance ti = tim.getTargetInstance(5000L);

			aCmd.setTargetInstanceOid(ti.getOid());
			aCmd.setLogFileName("crawl.log");
			aCmd.setFilter(".*.http://us.geocities.com/everardus.geo/protrudilogo.jpg.*");
			aCmd.setFilterType(LogReaderCommand.VALUE_REGEX_MATCH);
			aCmd.setShowLineNumbers(false);

            BindingResult bindingResult = new BindException(aCmd, "LogReaderCommand");

			ModelAndView mav = testInstance.handle(aCmd, bindingResult);
			assertTrue(mav != null);
			assertNotNull((String[])mav.getModel().get(LogReaderCommand.MDL_LINES));
			String[] result = (String[])mav.getModel().get(LogReaderCommand.MDL_LINES);
			assertTrue(countReturnedLines(result) == 1);
			assertFalse("1. ".equals(result[0].substring(0,3)));
			assertTrue(Constants.VIEW_LOG_READER.equals(mav.getViewName()));
			assertFalse(bindingResult.hasErrors());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testHandleRegexMatchLineNumbers() {
		try
		{
			testInstance.setTargetInstanceManager(tim);
			testInstance.setHarvestCoordinator(hc);

			LogReaderCommand aCmd = new LogReaderCommand();
			TargetInstance ti = tim.getTargetInstance(5000L);

			aCmd.setTargetInstanceOid(ti.getOid());
			aCmd.setLogFileName("crawl.log");
			aCmd.setFilter(".*.http://us.geocities.com/everardus.geo/protrudilogo.jpg.*");
			aCmd.setFilterType(LogReaderCommand.VALUE_REGEX_MATCH);
			aCmd.setShowLineNumbers(true);

            BindingResult bindingResult = new BindException(aCmd, "LogReaderCommand");

			ModelAndView mav = testInstance.handle(aCmd, bindingResult);
			assertTrue(mav != null);
			assertNotNull((String[])mav.getModel().get(LogReaderCommand.MDL_LINES));
			String[] result = (String[])mav.getModel().get(LogReaderCommand.MDL_LINES);
			assertTrue(countReturnedLines(result) == 1);
			assertEquals(result[0].substring(0,6), "5280. ");
			assertTrue(Constants.VIEW_LOG_READER.equals(mav.getViewName()));
			assertFalse(bindingResult.hasErrors());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testHandleRegexContain() {
		try
		{
			testInstance.setTargetInstanceManager(tim);
			testInstance.setHarvestCoordinator(hc);

			LogReaderCommand aCmd = new LogReaderCommand();
			TargetInstance ti = tim.getTargetInstance(5000L);

			aCmd.setTargetInstanceOid(ti.getOid());
			aCmd.setLogFileName("crawl.log");
			aCmd.setFilter(".*.http://us.geocities.com/everardus.geo/protrudilogo.jpg.*");
			aCmd.setFilterType(LogReaderCommand.VALUE_REGEX_CONTAIN);
			aCmd.setShowLineNumbers(true);

            BindingResult bindingResult = new BindException(aCmd, "LogReaderCommand");

			ModelAndView mav = testInstance.handle(aCmd, bindingResult);
			assertTrue(mav != null);
			assertNotNull((String[])mav.getModel().get(LogReaderCommand.MDL_LINES));
			String[] result = (String[])mav.getModel().get(LogReaderCommand.MDL_LINES);
			assertTrue(countReturnedLines(result) == 22);
			assertEquals(result[0].substring(0,6), "5280. ");
			assertTrue(Constants.VIEW_LOG_READER.equals(mav.getViewName()));
			assertFalse(bindingResult.hasErrors());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testHandleRegexIndent() {
		try
		{
			testInstance.setTargetInstanceManager(tim);
			testInstance.setHarvestCoordinator(hc);

			LogReaderCommand aCmd = new LogReaderCommand();
			TargetInstance ti = tim.getTargetInstance(5000L);

			aCmd.setTargetInstanceOid(ti.getOid());
			aCmd.setLogFileName("local-errors.log");
			aCmd.setFilter(".*SocketTimeoutException.*");
			aCmd.setFilterType(LogReaderCommand.VALUE_REGEX_INDENT);
			aCmd.setShowLineNumbers(true);

            BindingResult bindingResult = new BindException(aCmd, "LogReaderCommand");

			ModelAndView mav = testInstance.handle(aCmd, bindingResult);
			assertTrue(mav != null);
			assertNotNull((String[])mav.getModel().get(LogReaderCommand.MDL_LINES));
			String[] result = (String[])mav.getModel().get(LogReaderCommand.MDL_LINES);
			assertTrue(countReturnedLines(result) == 24);
			assertEquals(result[0].substring(0,4), "21. ");
			assertTrue(Constants.VIEW_LOG_READER.equals(mav.getViewName()));
			assertFalse(bindingResult.hasErrors());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testSetHarvestCoordinator() {
		try
		{
			testInstance.setHarvestCoordinator(hc);
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testSetTargetInstanceManager() {
		try
		{
			testInstance.setTargetInstanceManager(tim);
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

}
