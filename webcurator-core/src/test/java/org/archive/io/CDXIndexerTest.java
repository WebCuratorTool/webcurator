package org.archive.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;
import org.webcurator.domain.model.core.HarvestResultDTO;
import org.webcurator.test.BaseWCTTest;
import org.webcurator.test.WCTTestUtils;

public class CDXIndexerTest extends BaseWCTTest<CDXIndexer>{

	private String archivePath = "/org/webcurator/domain/model/core/archiveFiles";
	private String TestCARC_CDX = "IAH-20080610152724-00000-test.cdx";
	private String TestCWARC_CDX = "IAH-20080610152754-00000-test.cdx";

	private String format = "N a b";

	private long NumARCResources = 77;
	private long NumWARCResources = 72;
	
	private Long hrOid = 54321L;
	private Long tiOid = 12345L;
	private int harvestNumber = 1;
	
	public CDXIndexerTest()
	{
		super(CDXIndexer.class, "");
	}

	public void setUp() throws Exception {
		super.setUp();
		HarvestResultDTO result = new HarvestResultDTO(hrOid, tiOid, new Date(), harvestNumber, "");
		testInstance.setFormat(format);
		testInstance.initialise(result, WCTTestUtils.getResourceAsFile(archivePath));
	}

	/**
     * Generates CDX files for a warc and an arc file and verifies:
	 * * whether the expected header was generated in both cases
	 * * whether the number of lines in each CDX file matches the number of HTTP resources in the
	 *   corresponding (w)arc file
	 */
	@Test
	public final void testIndexFiles() {
		try {
			testInstance.indexFiles(testInstance.getResult().getOid());
			
			// Check whether the ARC CDX index was generated
			File cdxFile = WCTTestUtils.getResourceAsFile(archivePath+"/"+TestCARC_CDX); // new File(archivePath+"/"+TestCARC_CDX);
			assertTrue(cdxFile.exists());
	
			// Check whether the header and the number of records in the CDX matches the arc file
			int count = 0;
		    FileInputStream fstream = new FileInputStream(cdxFile);
		    DataInputStream in = new DataInputStream(fstream);
		    BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    String header = br.readLine();
		    // Verify the header
		    assertEquals(" CDX " + format, header);
		    // Count the non-header lines
		    while (br.readLine() != null)   {
		      count++;
		    }
		    in.close();
		    assertEquals(NumARCResources, count);
			cdxFile.delete();
	
			// Check whether the WARC CDX index was generated
			cdxFile = WCTTestUtils.getResourceAsFile(archivePath+"/"+TestCWARC_CDX);
			assertTrue(cdxFile.exists());
	
			// Check whether the header and the number of records in the CDX matches the warc file
			count = 0;
		    fstream = new FileInputStream(cdxFile);
		    in = new DataInputStream(fstream);
		    br = new BufferedReader(new InputStreamReader(in));
			header = br.readLine();
			// Verify the header
			assertEquals(" CDX " + format, header);
			// Count the non-header lines
			while (br.readLine() != null)   {
		      count++;
		    }
		    in.close();
		    assertEquals(NumWARCResources, count);
			cdxFile.delete();
		
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
