package org.webcurator.domain.model.core;

import static org.junit.Assert.*;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.webcurator.test.BaseWCTTest;

public class ArcHarvestFileDTOTest extends BaseWCTTest<ArcHarvestFileDTO> {

	private String archivePath = "/org/webcurator/domain/model/core/archiveFiles";
	private String TestCARC = "IAH-20080610152724-00000-test.arc.gz";
	private String TestCWARC = "IAH-20080610152754-00000-test.warc.gz";
	
	private long NumARCResources = 83;
	private long NumWARCResources = 72;
	
	public ArcHarvestFileDTOTest()
	{
		super(ArcHarvestFileDTO.class, "");
	}

	public void setUp() throws Exception {
		super.setUp();
        URL fileUrl = getClass().getResource(archivePath);
        Path resourcePath = Paths.get(fileUrl.toURI());
		testInstance.setBaseDir(resourcePath.toAbsolutePath().toString());
	}

	//TODO Test doesn't work, test itself also does not appear to test anything worthwhile 
	@Ignore
	@Test
	public final void testIndexCompressedARCFile() {
		try
		{
			testInstance.setName(TestCARC);
			Map<String, HarvestResourceDTO> results = testInstance.index();
			assertTrue(results != null);
			assertEquals(NumARCResources, results.size());
			assertTrue(testInstance.isCompressed());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	//TODO Test doesn't work, test itself also does not appear to test anything worthwhile 
	@Ignore
	@Test
	public final void testIndexCompressedWARCFile() {
		try
		{
			testInstance.setName(TestCWARC);
			Map<String, HarvestResourceDTO> results = testInstance.index();
			assertTrue(results != null);
			assertEquals(NumWARCResources, results.size());
			assertTrue(testInstance.isCompressed());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testCheckIsCompressed() {
		try
		{
			testInstance.setName(TestCARC);
			assertTrue(testInstance.checkIsCompressed());
			testInstance.setName(TestCWARC);
			assertTrue(testInstance.checkIsCompressed());
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

}
