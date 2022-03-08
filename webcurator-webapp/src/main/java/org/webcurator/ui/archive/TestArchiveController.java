/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.webcurator.ui.archive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * The Controller for managing the archiving of target instances.
 * @author aparker
 */
@Controller
public class TestArchiveController{
	@Value("${heritrix.version}")
	private String testHeritrixVersion;

	@Autowired
	private ApplicationContext context;

	@Autowired
	private ArchiveController archiveController;

	@PostConstruct
	protected void init() {
		// NOTE inherits from ArchiveController
		// But none if its access methods are public, so we're just going to replicate the values that get set
		// as 'parent' is not replicated in Spring beans annotations.
		archiveController.setHeritrixVersion("Heritrix" + testHeritrixVersion);
		archiveController.setWebCuratorUrl("http://dia-nz.github.io/webcurator/schemata/webcuratortool-1.0.dtd");
	}

	@RequestMapping(value = "/curator/archive/test.html", method = {RequestMethod.POST, RequestMethod.GET})
	protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView(new XmlView());
		int harvestNumber = 1;
		String xmlData = archiveController.buildSip(request, response, harvestNumber);

		mav.addObject("xml", xmlData);
		return mav;
	}


	public static class XmlView extends AbstractView {

		@Override
		protected void renderMergedOutputModel(Map stuff, HttpServletRequest request, HttpServletResponse response) throws Exception {
			String xml = (String) stuff.get("xml");
			response.setContentType("text/xml");
			response.getWriter().print(xml);
			response.getWriter().print("</mets:mets>");
		}

		@Override
		public void render(Map stuff, HttpServletRequest request, HttpServletResponse response) throws Exception {
			// TODO Auto-generated method stub
			renderMergedOutputModel(stuff, request, response);
		}

	}
}
