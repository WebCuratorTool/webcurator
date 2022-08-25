package org.webcurator.ui.tools.controller;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.scheduler.MockTargetInstanceManager;
import org.webcurator.core.targets.MockTargetManager;
import org.webcurator.domain.MockTargetInstanceDAO;
import org.webcurator.test.BaseWCTTest;
import org.webcurator.ui.tools.command.QualityReviewToolCommand;


public class QualityReviewToolControllerTest extends BaseWCTTest<QualityReviewToolController>{

	public QualityReviewToolControllerTest()
	{
		super(QualityReviewToolController.class, "/org/webcurator/ui/tools/controller/QualityReviewToolControllerTest.xml");
	}
	AuthorityManager authorityManager;

	QualityReviewToolControllerAttribute mockQRTCA = new QualityReviewToolControllerAttribute();

    //Override BaseWCTTest setup method
	public void setUp() throws Exception {
		//call the overridden method as well
		super.setUp();

		mockQRTCA.setArchiveUrl("archiveURL");
		mockQRTCA.setTargetManager(new MockTargetManager(testFile));
		mockQRTCA.setTargetInstanceDao(new MockTargetInstanceDAO(testFile));
		mockQRTCA.setTargetInstanceManager(new MockTargetInstanceManager(testFile));
		HarvestResourceUrlMapper harvestResourceUrlMapper = new HarvestResourceUrlMapper();
		harvestResourceUrlMapper.setUrlMap("http://test?url={$HarvestResource.Name}");
		mockQRTCA.setHarvestResourceUrlMapper(harvestResourceUrlMapper);
	}



	@Test
	public final void testHandle() {
		try {
			mockQRTCA.setEnableBrowseTool(true);
			mockQRTCA.setEnableAccessTool(true);
			ReflectionTestUtils.setField(testInstance, "attr", mockQRTCA);

			ModelAndView mav = getHandelMav();
			assertTrue(mav != null);
			assertTrue(mav.getViewName().equals("quality-review-toc"));
			List<SeedMapElement> seeds = (List<SeedMapElement>)mav.getModel().get(QualityReviewToolCommand.MDL_SEEDS);
			assertTrue(seeds.size() == 1 );
			Iterator<SeedMapElement> it = seeds.iterator();
			assertTrue(it.hasNext());
			SeedMapElement sme = it.next();
			assertEquals("http://www.oakleigh.co.uk/", sme.getSeed());
			assertEquals("curator/tools/browse/111000/?url="+ Base64.getEncoder().encodeToString("http://www.oakleigh.co.uk/".getBytes()), sme.getBrowseUrl());
			assertEquals("http://test?url=http://www.oakleigh.co.uk/", sme.getAccessUrl());
		}
		catch (Exception e)
		{
			String message = e.getClass().toString() + " - " + e.getMessage();
			log.debug(message);
			fail(message);
		}
	}

	@Test
	public final void testHandleWithAccessToolDiasbled() {
		try
		{
			mockQRTCA.setEnableBrowseTool(true);
			mockQRTCA.setEnableAccessTool(false);
			ReflectionTestUtils.setField(testInstance, "attr", mockQRTCA);

			ModelAndView mav = getHandelMav();
			assertTrue(mav != null);
			assertTrue(mav.getViewName().equals("quality-review-toc"));
			List<SeedMapElement> seeds = (List<SeedMapElement>)mav.getModel().get(QualityReviewToolCommand.MDL_SEEDS);
			assertTrue(seeds.size() == 1 );
			Iterator<SeedMapElement> it = seeds.iterator();
			assertTrue(it.hasNext());
			SeedMapElement sme = it.next();
			assertEquals("http://www.oakleigh.co.uk/", sme.getSeed());
			assertEquals("curator/tools/browse/111000/?url="+ Base64.getEncoder().encodeToString("http://www.oakleigh.co.uk/".getBytes()), sme.getBrowseUrl());
			assertEquals("", sme.getAccessUrl());

		}
		catch (Exception e)
		{
			String message = e.getClass().toString() + " - " + e.getMessage();
			log.debug(message);
			fail(message);
		}
	}

	@Test
	public final void testHandleWithBrowseToolDiasbled() {
		try
		{
			mockQRTCA.setEnableBrowseTool(false);
			mockQRTCA.setEnableAccessTool(true);
			ReflectionTestUtils.setField(testInstance, "attr", mockQRTCA);

			ModelAndView mav = getHandelMav();
			assertTrue(mav != null);
			assertTrue(mav.getViewName().equals("quality-review-toc"));
			List<SeedMapElement> seeds = (List<SeedMapElement>)mav.getModel().get(QualityReviewToolCommand.MDL_SEEDS);
			assertTrue(seeds.size() == 1 );
			Iterator<SeedMapElement> it = seeds.iterator();
			assertTrue(it.hasNext());
			SeedMapElement sme = it.next();
			assertEquals("http://www.oakleigh.co.uk/", sme.getSeed());
			assertEquals("", sme.getBrowseUrl());
			assertEquals("http://test?url=http://www.oakleigh.co.uk/", sme.getAccessUrl());

		}
		catch (Exception e)
		{
			String message = e.getClass().toString() + " - " + e.getMessage();
			log.debug(message);
			fail(message);
		}
	}

	private ModelAndView getHandelMav() throws Exception {
		QualityReviewToolCommand comm = new QualityReviewToolCommand();
		comm.setHarvestResultId(111000L);
		comm.setTargetInstanceOid(5000L);

		ModelAndView mav = testInstance.postHandle(comm);
		return mav;
	}


}
