package org.webcurator.ui.target.controller;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.test.BaseWCTTest;

public class LiveContentRetrieverControllerTest extends BaseWCTTest<LiveContentRetrieverController>{

	public LiveContentRetrieverControllerTest()
	{
		super(LiveContentRetrieverController.class,
                "/org/webcurator/ui/target/controller/LogReaderControllerTest.xml");
	}


	@Test
	@Ignore
	public final void testHandle() {
		try
		{
			MockHttpServletRequest aReq = new MockHttpServletRequest();
			MockHttpServletResponse aResp = new MockHttpServletResponse();

			ModelAndView mav = testInstance.handle("http://www.bl.uk", "test.html");
			assertTrue(mav != null);
			assertEquals(mav.getModel().size(), 0);
			assertTrue(mav.getView() instanceof AttachmentView);
			AttachmentView view = (AttachmentView)mav.getView();

			view.render(null, aReq, aResp);

			assertTrue(aResp.getHeader("Content-Disposition").toString().endsWith("test.html"));
			assertTrue(aResp.getContentAsString().contains("British Library"));
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

}
