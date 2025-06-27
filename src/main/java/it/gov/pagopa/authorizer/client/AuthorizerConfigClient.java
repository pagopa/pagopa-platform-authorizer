package it.gov.pagopa.authorizer.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.gov.pagopa.authorizer.exception.AuthorizerConfigException;
import it.gov.pagopa.authorizer.model.AuthConfiguration;

import java.net.http.HttpResponse;

/**
 * Client for invoking Authorizer Configuration service
 */
public interface AuthorizerConfigClient {

    /**
     * Sends a request to refresh the authorization configuration using the given parameters.
     *
     * @param authConfiguration the authorization configuration details.
     * @param domain the domain for which the configuration is to be refreshed.
     * @param addInProgress flag indicating if the operation should add to the in-progress list.
     * @return the response from the authorization service.
     * @throws AuthorizerConfigException if there is an error during the request.
     */
    HttpResponse<String> refreshConfiguration(
            AuthConfiguration authConfiguration,
            String domain,
            boolean addInProgress
    ) throws JsonProcessingException, AuthorizerConfigException;

}
