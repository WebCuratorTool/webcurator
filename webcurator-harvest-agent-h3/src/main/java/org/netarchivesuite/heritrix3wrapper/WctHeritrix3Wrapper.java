package org.netarchivesuite.heritrix3wrapper;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

/**
 * Temporary extension to Heritrix3Wrapper until the launch-with-checkpoint method gets added to
 * to their new release
 */
public class WctHeritrix3Wrapper extends Heritrix3Wrapper {

    public static WctHeritrix3Wrapper getInstance(String hostname, int port, File keystoreFile, String keyStorePassword, String userName, String password) {
        WctHeritrix3Wrapper wctHeritrix3Wrapper = new WctHeritrix3Wrapper();
        Heritrix3Wrapper heritrix3Wrapper = Heritrix3Wrapper.getInstance(hostname, port, keystoreFile, keyStorePassword, userName, password);
        wctHeritrix3Wrapper.hostname = heritrix3Wrapper.hostname;
        wctHeritrix3Wrapper.port = heritrix3Wrapper.port;
        wctHeritrix3Wrapper.baseUrl = heritrix3Wrapper.baseUrl;
        wctHeritrix3Wrapper.httpClient = heritrix3Wrapper.httpClient;
        return wctHeritrix3Wrapper;
    }

    /**
     * Launch a built job in pause state, resuming the crawl from the supplied checkpoint.
     * @param jobname job name
     * @param checkpoint checkpoint name
     * @return job state
     */
    public JobResult launchJob(String jobname, String checkpoint) {
        HttpPost postRequest = new HttpPost(baseUrl + "job/" + jobname);
        List<NameValuePair> nvp = new LinkedList<NameValuePair>();
        nvp.add(new BasicNameValuePair("action", "launch"));
        nvp.add(new BasicNameValuePair("checkpoint", checkpoint));
        StringEntity postEntity = null;
        try {
            postEntity = new UrlEncodedFormEntity(nvp);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        postEntity.setContentType("application/x-www-form-urlencoded");
        postRequest.addHeader("Accept", "application/xml");
        postRequest.setEntity(postEntity);
        return jobResult(postRequest);
    }

}
