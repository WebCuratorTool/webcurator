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

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractView;
import org.webcurator.core.archive.ArchiveAdapter;
import org.webcurator.core.archive.SipBuilder;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.core.targets.TargetManager;

/**
 * The Controller for managing the archiving of target instances.
 * @author aparker
 */
@Controller
public class TestArchiveController extends ArchiveController {
    @Value("${heritrix.version}")
    private String testHeritrixVersion;

    @Autowired
    private ApplicationContext context;

    @PostConstruct
    protected void init() {
        // NOTE inherits from ArchiveController
        // But none if its access methods are public, so we're just going to replicate the values that get set
        // as 'parent' is not replicated in Spring beans annotations.
        // TODO These may be autowired in the superclass. Check after Spring annotation conversions are complete.
        setTargetInstanceManager((TargetInstanceManager) context.getBean("targetInstanceManager"));
        setTargetManager((TargetManager) context.getBean("targetManager"));
        setSipBuilder((SipBuilder) context.getBean("sipBuilder"));
        setArchiveAdapter((ArchiveAdapter) context.getBean("archiveAdapter"));
        setHeritrixVersion("Heritrix" + testHeritrixVersion);
        setWebCuratorUrl("http://dia-nz.github.io/webcurator/schemata/webcuratortool-1.0.dtd");
    }

	protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView(new XmlView());
		int harvestNumber = 1;
		String xmlData = buildSip(request, response, harvestNumber);

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
