package org.webcurator.core.screenshot;

import org.apache.commons.io.IOUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.core.store.DigitalAssetStorePaths;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class ScreenshotClientRemote extends AbstractRestClient implements ScreenshotClient {
    public ScreenshotClientRemote(String baseUrl, RestTemplateBuilder restTemplateBuilder) {
        super(baseUrl, restTemplateBuilder);
    }

    @Override
    public Boolean createScreenshots(ScreenshotIdentifierCommand identifiers) throws DigitalAssetStoreException {
        HttpEntity<String> request = this.createHttpRequestEntity(identifiers);
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(ScreenshotPaths.CREATE_SCREENSHOT));
        RestTemplate restTemplate = restTemplateBuilder.build();

        return restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                request, Boolean.class);
    }

    @Override
    public void browseScreenshotImage(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        String imgPath = req.getRequestURI();
        String remoteUrl = getUrl(imgPath);
        URL url = new URL(remoteUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        rsp.setContentType(urlConnection.getContentType());
        urlConnection.setDoOutput(true);
        IOUtils.copy(urlConnection.getInputStream(), rsp.getOutputStream());
    }
}
