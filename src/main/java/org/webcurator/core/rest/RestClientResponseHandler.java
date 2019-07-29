package org.webcurator.core.rest;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.webcurator.core.exceptions.WCTRuntimeException;

import java.io.IOException;
import java.net.URI;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RestClientResponseHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR ||
                response.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (response.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
            throwException(response.getStatusCode(), null, null);
        } else if (response.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
            throwException(response.getStatusCode(), null, null);
        }
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        throwException(response.getStatusCode(), url, method);
    }

    public void throwException(HttpStatus status, URI url, HttpMethod httpMethod) {
        if (httpMethod == null) {
            throw new WCTRuntimeException("Error, httpStatus=" + status.value() + ", reason=" + status.getReasonPhrase());
        } else {
            throw new WCTRuntimeException("Error, httpMethod=" + httpMethod + ", URI=" + url +
                    ", httpStatus=" + status.value() + ", reason=" + status.getReasonPhrase());
        }
    }
}
