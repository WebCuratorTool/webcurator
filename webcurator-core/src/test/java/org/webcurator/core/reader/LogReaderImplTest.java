package org.webcurator.core.reader;

import static org.junit.Assert.*;

import org.junit.Test;
import org.webcurator.test.*;

import java.util.List;

public class LogReaderImplTest extends BaseWCTTest<LogReaderImpl>{

	private String baseDir = "/org/webcurator/core/reader/logs";
	public LogReaderImplTest()
	{
		super(LogReaderImpl.class, "");
	}
	
    //Override BaseWCTTest setup method
	public void setUp() throws Exception {
		//call the overridden method as well
		super.setUp();
		
		//add the extra bits
		testInstance.setLogProvider(new MockLogProvider(baseDir));
	}
	
	@Test
	public final void testCountLines() {
		Integer count = testInstance.countLines("Dummy", "crawl.log");
		assertTrue(count == 5301);
	}

	@Test
	public final void testTail() {
		List<String> results = testInstance.tail("Dummy", "crawl.log", 50);
		assertNotNull(results);
		assertTrue(results.size() == 2);
		String[] lines = results.get(0).split("\n");
		assertTrue(lines.length == 50);
	}

	@Test
	public final void testGet() {
		List<String> results = testInstance.get("Dummy", "crawl.log", 1, 50);
		assertNotNull(results);
		assertTrue(results.size() == 2);
		String[] lines = results.get(0).split("\n");
		assertTrue(lines.length == 50);
	}

	@Test
	public final void testGet2() {
		List<String> results = testInstance.get("Dummy", "crawl.log", 5300, 50);
		assertNotNull(results);
		assertTrue(results.size() == 2);
		String[] lines = results.get(0).split("\n");
		assertTrue(lines.length == 2);
	}

	@Test
	public final void testGetByRegExpr() {
		String regex = ".*.http://us.geocities.com/quasi_chick/meon.jpg.*";
		List<String> results = testInstance.getByRegularExpression("Dummy", "crawl.log", regex, "zzzzzzzzz", true, 0, 50);
		assertNotNull(results);
		assertTrue(results.size() == 2);
		String[] lines = results.get(0).split("\n");
		assertTrue(lines.length == 1);
	}

	@Test
	public final void testFindFirstLineBeginning() {
		String match = "2008-06-18T06:21:22";
		Integer result = testInstance.findFirstLineBeginning("Dummy", "crawl.log", match);
		assertNotNull(result);
		assertTrue(result.intValue() == 4656);
	}

	@Test
	public final void testFindFirstLineBeginning2() {
		String match = "Dummy";
		Integer result = testInstance.findFirstLineBeginning("Dummy", "crawl.log", match);
		assertNotNull(result);
		assertTrue(result.intValue() == -1);
	}

	@Test
	public final void testFindFirstLineContaining() {
		String regex = ".*.http://us.geocities.com/quasi_chick/meon.jpg.*";
		Integer result = testInstance.findFirstLineContaining("Dummy", "crawl.log", regex);
		assertNotNull(result);
		assertTrue(result.intValue() == 4648);
	}

	@Test
	public final void testFindFirstLineContaining2() {
		String regex = "dummy";
		Integer result = testInstance.findFirstLineContaining("Dummy", "crawl.log", regex);
		assertNotNull(result);
		assertTrue(result.intValue() == -1);
	}

	@Test
	public final void testFindFirstLineAfterTimeStamp() {
		Long timestamp = new Long(20080618062122L);
		Integer result = testInstance.findFirstLineAfterTimeStamp("Dummy", "crawl.log", timestamp);
		assertNotNull(result);
		assertTrue(result.intValue() == 4656);
	}

}
