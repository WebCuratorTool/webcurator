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
package org.webcurator.ui.tools.controller;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.webcurator.common.ui.Constants;
import org.webcurator.core.harvester.coordinator.HarvestLogManager;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.core.store.tools.QualityReviewFacade;
import org.webcurator.core.store.tools.WCTNodeTree;
import org.webcurator.core.util.XMLConverter;
import org.webcurator.ui.tools.command.WebrecorderCommand;
import org.webcurator.ui.tools.validator.TreeToolValidator;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.text.NumberFormat;

/**
 * The TreeToolController is responsible for rendering the
 * harvest web site as a tree structure.
 * @author bbeaumont
 */
@SuppressWarnings("all")
@Controller
public class WebrecorderController {

	private static Log log = LogFactory.getLog(WebrecorderController.class);

	@Autowired
	private TargetInstanceManager targetInstanceManager;


	@InitBinder
	public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		// Determine the necessary formats.
		NumberFormat nf = NumberFormat.getInstance(request.getLocale());

		// Register the binders.
		binder.registerCustomEditor(Long.class, new CustomNumberEditor(Long.class, nf, true));
		binder.registerCustomEditor(Boolean.class, "propagateDelete", new CustomBooleanEditor(true));

		// to actually be able to convert Multipart instance to byte[]
		// we have to register a custom editor (in this case the
		// ByteArrayMultipartEditor
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
		// now Spring knows how to handle multipart object and convert them
	}


	/**
	 * Shows the Patch With Webrecorder page, the starting point of a Webrecorder recording session
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(path = "/curator/tools/webrecorder.html", method = RequestMethod.GET)
	protected ModelAndView handleGet(HttpServletRequest req, WebrecorderCommand command, BindingResult bindingResult)	throws Exception {
		//TargetInstance ti = (TargetInstance) req.getSession().getAttribute("sessionTargetInstance");

		// Set up anonymous webrecorder session
        WebrecorderCommand wrCommand = setupWrSession(command.getSeedUrl());

		ModelAndView mav = new ModelAndView("webrecorder");
		mav.addObject( "command", wrCommand);

		if(bindingResult.hasErrors()){mav.addObject( Constants.GBL_ERRORS, bindingResult);}
		return mav;
	}


	/**
	 * Handles all other actions on the page
	 */
	@RequestMapping(path = "/curator/tools/webrecorder.html", method = RequestMethod.POST)
	protected ModelAndView handlePost(HttpServletRequest req, HttpServletResponse resp, WebrecorderCommand command, BindingResult bindingResult)	throws Exception {

		// redirect to Webrecorder recording page
		if (command.isAction(WebrecorderCommand.ACTION_RECORD)) {
			resp.addHeader(HttpHeaders.SET_COOKIE, command.getSessionCookie());
			resp.sendRedirect(command.getRecordingUrl());
			return null;
		}

		// TODO other cases

		ModelAndView mav = new ModelAndView("webrecorder");
		if(bindingResult.hasErrors()){mav.addObject( Constants.GBL_ERRORS, bindingResult);}
		return mav;

	}



	/**
	 * Initiates an anonymous webrecorder capture session
	 * using the supplied url as its seed
	 * @param url seed url
	 */
	private WebrecorderCommand setupWrSession(String url) throws Exception {

	    WebrecorderCommand command = new WebrecorderCommand();
	    command.setSeedUrl(url);

		// start session
		String initUrl = "http://localhost:8089/api/v1/auth/anon_user";
		RestTemplate restTemplate = new RestTemplate(); // FIXME reuse template at the instance level
		HttpEntity<String> requestEntity = new HttpEntity<String>("");
		ResponseEntity<String> responseEntity = restTemplate.exchange(initUrl, HttpMethod.POST, requestEntity, String.class);

		if (!requestEntity.getHeaders().containsKey("Set-Cookie")) {
			// TODO panic
		}
		JSONObject jsonResponse = new JSONObject(responseEntity.getBody());
		String userName = jsonResponse.getJSONObject("user").getString("username");
		String cookie = responseEntity.getHeaders().get("Set-Cookie").get(0);
		command.setUserName(userName);
		command.setSessionCookie(cookie);

		// create temp collection
		String createTempCollUrl = "http://localhost:8089/api/v1/collections?user=" + userName;
		String tempCollName = "temp";
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		headers.add(HttpHeaders.COOKIE, cookie);
		String requestBody = String.format("{\"title\":\"%s\",\"public\":true,\"public_index\":true}", tempCollName);
		requestEntity = new HttpEntity<String>(requestBody, headers);
		responseEntity = restTemplate.exchange(createTempCollUrl, HttpMethod.POST, requestEntity, String.class);

		if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			// TODO panic
			log.debug("Could not create WR coll: panic!");
		}

		// create recording session
        String createRecordingSessionUrl = "http://localhost:8089/api/v1/new";
		requestBody = String.format("{\"url\":\"%s\",\"coll\":\"%s\",\"desc\":\"\",\"mode\":\"record\"}",
																					url, tempCollName);
		requestEntity = new HttpEntity<String>(requestBody, headers);
		responseEntity = restTemplate.exchange(createRecordingSessionUrl, HttpMethod.POST, requestEntity, String.class);

		if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			// TODO panic
			log.debug("Could not create WR coll: panic!");
		}

		jsonResponse = new JSONObject(responseEntity.getBody());
		String recordingUrl = jsonResponse.getString("url");
		String recordingName = jsonResponse.getString("rec_name");
		command.setRecordingUrl(recordingUrl);
		command.setRecordingName(recordingName);

		return command;
	}

}
