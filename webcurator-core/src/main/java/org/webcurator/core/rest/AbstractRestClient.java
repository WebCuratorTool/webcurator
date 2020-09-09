package org.webcurator.core.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.Duration;

abstract public class AbstractRestClient {
    protected Logger log = LoggerFactory.getLogger(getClass());

    /*
     * the service url for the server, e.x: https://localhost:8080/wct.
     */
    protected String baseUrl;
    protected RestTemplateBuilder restTemplateBuilder;

    public AbstractRestClient() {
    }

    public AbstractRestClient(String baseUrl, RestTemplateBuilder restTemplateBuilder) {
        this.baseUrl = baseUrl;
        this.restTemplateBuilder = restTemplateBuilder;
        this.restTemplateBuilder.errorHandler(new RestClientResponseHandler())
                .setConnectTimeout(Duration.ofSeconds(15L));
    }

    public String getUrl(String appendUrl) {
        return String.format("%s%s", this.baseUrl, appendUrl);
    }

    public HttpEntity<String> createHttpRequestEntity(Object objRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return this.createHttpRequestEntity(objRequest, headers);
    }

    public HttpEntity<String> createHttpRequestEntity(Object objRequest, HttpHeaders headers) {
        if (objRequest == null) {
            log.error("Input parameter is null: objRequest");
            return null;
        }

        String json = this.encode2json(objRequest);

        HttpEntity<String> request;
        if (headers == null) {
            request = new HttpEntity<>(json);
        } else {
            request = new HttpEntity<>(json, headers);
        }

        return request;
    }

    public String encode2json(Object objRequest) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonRequest;
        try {
            jsonRequest = objectMapper.writeValueAsString(objRequest);
            log.debug(jsonRequest);
        } catch (JsonProcessingException e) {
            log.error("Encode json failed", e);
            return null;
        }

        return jsonRequest;
    }

    public String toString() {
        return String.format("%s@%s", this.getClass().getName(), this.baseUrl);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public RestTemplateBuilder getRestTemplateBuilder() {
        return restTemplateBuilder;
    }

    public void setRestTemplateBuilder(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }
}
