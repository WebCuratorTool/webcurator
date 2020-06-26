package org.webcurator.core.visualization;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.webcurator.core.rest.AbstractRestClient;

public class MockWebAppClient extends AbstractRestClient {
    public MockWebAppClient(String scheme, String host, int port, RestTemplateBuilder restTemplateBuilder) {
        super(scheme, host, port, restTemplateBuilder);
    }
}
