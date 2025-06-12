package it.gov.pagopa.authorizer.client.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.authorizer.client.AuthorizerConfigClient;
import it.gov.pagopa.authorizer.exception.AuthorizerConfigException;
import it.gov.pagopa.authorizer.model.AuthConfiguration;
import it.gov.pagopa.authorizer.model.enumeration.ReasonErrorCode;
import it.gov.pagopa.authorizer.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * {@inheritDoc}
 */
public class AuthorizerConfigClientImpl implements AuthorizerConfigClient {

    private static final String AUTH_REFRESH_CONFIGURATION_PATH_TEMPLATE = "%s/cache/domains/%s?add_in_progress=%s";

    private final Logger logger = LoggerFactory.getLogger(AuthorizerConfigClientImpl.class);
    private final String authorizerPath = System.getenv(Constants.REFRESH_CONFIG_PATH_PARAMETER);

    private final HttpClient client;

    private static AuthorizerConfigClientImpl instance;

    public static AuthorizerConfigClientImpl getInstance() {
        if (instance == null) {
            instance = new AuthorizerConfigClientImpl();
        }
        return instance;
    }

    private AuthorizerConfigClientImpl() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    AuthorizerConfigClientImpl(HttpClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse<String> refreshConfiguration(
            AuthConfiguration authConfiguration,
            String domain,
            boolean addInProgress
    ) throws AuthorizerConfigException {
        String uri = String.format(AUTH_REFRESH_CONFIGURATION_PATH_TEMPLATE, this.authorizerPath, domain, addInProgress);
        String body = parseBody(authConfiguration);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .version(HttpClient.Version.HTTP_2)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = makeCall(request);
        if (response.statusCode() != 200) {
            String msg = String.format("Error invoking refresh configuration API, response status code %s", response.statusCode());
            throw new AuthorizerConfigException(msg, response.statusCode());
        }
        return response;
    }

    private HttpResponse<String> makeCall(HttpRequest request) throws AuthorizerConfigException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new AuthorizerConfigException("I/O error when invoking Authorizer Configuration service", ReasonErrorCode.ERROR_AUTHORIZER_CONFIG.getCode(), e);
        } catch (InterruptedException e) {
            logger.warn("This thread was interrupted, restoring the state");
            Thread.currentThread().interrupt();
            throw new AuthorizerConfigException("Unexpected error when invoking Authorizer Configuration service, the thread was interrupted", ReasonErrorCode.ERROR_AUTHORIZER_CONFIG_UNEXPECTED.getCode(), e);
        }
    }

    private String parseBody(AuthConfiguration authConfiguration) throws AuthorizerConfigException {
        try {
            return new ObjectMapper().writeValueAsString(authConfiguration);
        } catch (JsonProcessingException e) {
            throw new AuthorizerConfigException("Error preparing Authorizer Configuration request", ReasonErrorCode.ERROR_AUTHORIZER_CONFIG_MAPPING.getCode(), e);
        }
    }
}
