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

    /**
     * the protocol type for the client.
     */
    protected String scheme = "http";
    /**
     * the host name or ip-address for the client.
     */
    protected String host = "localhost";
    /**
     * the port number for the client.
     */
    protected int port = 8080;

    protected final RestTemplateBuilder restTemplateBuilder;


    public AbstractRestClient(String scheme, String host, int port, RestTemplateBuilder restTemplateBuilder) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.restTemplateBuilder = restTemplateBuilder;
        this.restTemplateBuilder.errorHandler(new RestClientResponseHandler())
                .setConnectTimeout(Duration.ofSeconds(15L));
    }

    public String baseUrl() {
        return String.format("%s://%s:%d", scheme, host, port);
    }

    public String getUrl(String appendUrl) {
        return String.format("%s%s", this.baseUrl(), appendUrl);
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

        HttpEntity<String> request = null;
        if (headers == null) {
            request = new HttpEntity<String>(json);
        } else {
            request = new HttpEntity<String>(json, headers);
        }

        return request;
    }

    public String encode2json(Object objRequest) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonRequest = null;
        try {
            jsonRequest = objectMapper.writeValueAsString(objRequest);
            log.debug(jsonRequest);
        } catch (JsonProcessingException e) {
            log.error("Encode json failed", e);
            return null;
        }

        return jsonRequest.toString();
    }

    public String toString() {
        return String.format("%s@%s",this.getClass().getName(), this.baseUrl());
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public RestTemplateBuilder getRestTemplateBuilder() {
        return restTemplateBuilder;
    }
}
