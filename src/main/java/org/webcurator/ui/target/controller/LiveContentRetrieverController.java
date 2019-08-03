package org.webcurator.ui.target.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.ui.target.command.LiveContentRetrieverCommand;

@Controller
@RequestMapping("/curator/target/live-content-retriever.html")
public class LiveContentRetrieverController {

	public LiveContentRetrieverController() {
	}

	@GetMapping
	protected ModelAndView handle(@RequestParam("url") String url, @RequestParam("contentFileName") String contentFileName)
			throws Exception {
		LiveContentRetrieverCommand cmd = new LiveContentRetrieverCommand();
		cmd.setUrl(url);
		cmd.setContentFileName(contentFileName);
		
		File f = downloadTemporaryFile(cmd.getUrl());
		AttachmentView v = new AttachmentView(cmd.getContentFileName(), f, true);
		return new ModelAndView(v);
	}

	private File downloadTemporaryFile(String url)
	{
	    GetMethod getMethod = new GetMethod(url);
	    HttpClient client = new HttpClient();
	    try
	    {
	        int result = client.executeMethod(getMethod);
	        if(result != HttpURLConnection.HTTP_OK)
	        {
	        	throw new WCTRuntimeException("Unable to fetch content at "+url+". Status="+result);
	        }
	        return writeTemporaryFile(getMethod.getResponseBody());
	    }
	    catch (WCTRuntimeException re)
	    {
	    	throw re;
	    }
	    catch (Exception e)
	    {
			throw new WCTRuntimeException("Unable to fetch content at "+url+".", e);
	    }
	    finally
	    {
	    	getMethod.releaseConnection();
	    }
	}

	private File writeTemporaryFile(byte[] content) throws IOException
	{
		File outputFile = File.createTempFile("wct", "tmp");
		BufferedOutputStream bufOutStr = new BufferedOutputStream(new FileOutputStream(outputFile));

		try
		{
			bufOutStr.write(content);
			return outputFile;
		}
		finally
		{
			bufOutStr.close();
		}
	}


}
