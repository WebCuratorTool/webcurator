package org.webcurator.ui.tools.controller;


import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.scheduler.MockTargetInstanceManager;
import org.webcurator.core.targets.MockTargetManager;
import org.webcurator.domain.MockTargetInstanceDAO;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.test.BaseWCTTest;
import org.webcurator.ui.tools.command.QualityReviewToolCommand;

import static org.junit.Assert.*;


public class QualityReviewToolControllerTest extends BaseWCTTest<QualityReviewToolController> {

	public QualityReviewToolControllerTest()
	{
		super(QualityReviewToolController.class, "/org/webcurator/ui/tools/controller/QualityReviewToolControllerTest.xml");
	}
	AuthorityManager authorityManager;

    QualityReviewToolControllerAttribute mockQRTCA = new QualityReviewToolControllerAttribute();

    Long tiId = 5000L;

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
            assertNotNull(mav);
            assertEquals("quality-review-toc", mav.getViewName());
            List<Map<String, String>> seeds = (List<Map<String, String>>) mav.getModel().get(QualityReviewToolCommand.MDL_SEEDS);
            assertEquals(1, seeds.size());
            Map<String, String> sme = seeds.get(0);
            assertEquals("http://www.oakleigh.co.uk/", sme.get("seedUrl"));
//            assertEquals("curator/tools/browse/111000/?url=" + Base64.getEncoder().encodeToString("http://www.oakleigh.co.uk/".getBytes()), sme.getBrowseUrl());
//            assertEquals("http://test?url=http://www.oakleigh.co.uk/", sme.getAccessUrl());
        } catch (Exception e) {
            String message = e.getClass().toString() + " - " + e.getMessage();
            log.debug(message);
            fail(message);
        }
    }

    @Test
    public final void testHandleWithAccessToolDiasbled() {
        try {
            mockQRTCA.setEnableBrowseTool(true);
            mockQRTCA.setEnableAccessTool(false);
            ReflectionTestUtils.setField(testInstance, "attr", mockQRTCA);

            ModelAndView mav = getHandelMav();
            assertNotNull(mav);
            assertEquals("quality-review-toc", mav.getViewName());
            List<Map<String, String>> seeds = (List<Map<String, String>>) mav.getModel().get(QualityReviewToolCommand.MDL_SEEDS);
            TargetInstance ti = mockQRTCA.targetInstanceManager.getTargetInstance(tiId);
            assertEquals(ti.getSeedHistory().size(), seeds.size());
            Map<String, String> sme = seeds.get(0);
            assertEquals("http://www.oakleigh.co.uk/", sme.get("seedUrl"));
//            assertEquals("curator/tools/browse/111000/?url=" + Base64.getEncoder().encodeToString("http://www.oakleigh.co.uk/".getBytes()), sme.getBrowseUrl());
//            assertEquals("", sme.getAccessUrl());

        } catch (Exception e) {
            String message = e.getClass().toString() + " - " + e.getMessage();
            log.debug(message);
            fail(message);
        }
    }

    @Test
    public final void testHandleWithBrowseToolDiasbled() {
        try {
            mockQRTCA.setEnableBrowseTool(false);
            mockQRTCA.setEnableAccessTool(true);
            ReflectionTestUtils.setField(testInstance, "attr", mockQRTCA);

            ModelAndView mav = getHandelMav();
            assertNotNull(mav);
            assertEquals("quality-review-toc", mav.getViewName());
            List<Map<String, String>> seeds = (List<Map<String, String>>) mav.getModel().get(QualityReviewToolCommand.MDL_SEEDS);
            assertEquals(1, seeds.size());

            Map<String, String> sme = seeds.get(0);
            assertEquals("http://www.oakleigh.co.uk/", sme.get("seedUrl"));
//            assertEquals("", sme.getBrowseUrl());
//            assertEquals("http://test?url=http://www.oakleigh.co.uk/", sme.getAccessUrl());

        } catch (Exception e) {
            String message = e.getClass().toString() + " - " + e.getMessage();
            log.debug(message);
            fail(message);
        }
    }

    private ModelAndView getHandelMav() throws Exception {
        QualityReviewToolCommand comm = new QualityReviewToolCommand();
        comm.setHarvestResultId(111000L);
        comm.setTargetInstanceOid(tiId);
        ModelAndView mav = testInstance.postHandle(comm);
        return mav;
    }


}
