package it.gov.pagopa.authorizer.util;

import lombok.*;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockHttpResponse<T> implements HttpResponse<T> {

    private int statusCode;
    private HttpRequest request;
    private Optional<HttpResponse<T>> previousResponse;
    private HttpHeaders headers;
    private T body;
    private Optional<SSLSession> sslSession;
    private URI uri;
    private HttpClient.Version version;

    @Override
    public int statusCode() {
        return this.statusCode;
    }

    @Override
    public HttpRequest request() {
        return this.request;
    }

    @Override
    public Optional<HttpResponse<T>> previousResponse() {
        return this.previousResponse;
    }

    @Override
    public HttpHeaders headers() {
        return this.headers;
    }

    @Override
    public T body() {
        return this.body;
    }

    @Override
    public Optional<SSLSession> sslSession() {
        return this.sslSession;
    }

    @Override
    public URI uri() {
        return this.uri;
    }

    @Override
    public HttpClient.Version version() {
        return this.version;
    }
}
