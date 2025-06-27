package it.gov.pagopa.authorizer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.gov.pagopa.authorizer.client.AuthorizerConfigClient;
import it.gov.pagopa.authorizer.exception.AuthorizerConfigException;
import it.gov.pagopa.authorizer.model.AuthConfiguration;

import java.net.http.HttpResponse;

/**
 * Service that wrap the {@link AuthorizerConfigClient} for adding retry logic for Authorizer Configuration responses
 */
public interface AuthorizerConfigClientRetryWrapper {

    HttpResponse<String> refreshConfigurationWithRetry(
            AuthConfiguration authConfiguration,
            String domain,
            boolean addInProgress
    ) throws JsonProcessingException, AuthorizerConfigException;
}
