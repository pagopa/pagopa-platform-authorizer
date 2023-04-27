package it.gov.pagopa.authorizer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.authorizer.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.model.AuthConfiguration;
import it.gov.pagopa.authorizer.util.Constants;
import it.gov.pagopa.authorizer.util.Utility;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CacheService {

    private final String authorizerPath;

    private final Logger logger;

    private final HttpClient httpClient;

    public CacheService(Logger logger, HttpClient httpClient, String authorizerPath) {
        this.logger = logger;
        this.httpClient = httpClient;
        this.authorizerPath = authorizerPath;
    }

    public HttpResponse<String> addAuthConfigurationToAPIMAuthorizer(SubscriptionKeyDomain subkeyDomain, boolean addInProgress) throws InterruptedException {
        if (subkeyDomain == null) {
            throw new IllegalArgumentException("Passed null parameter");
        }
        HttpResponse<String> response = null;
        try {
            String domain = subkeyDomain.getDomain();
            // generating object to be sent
            AuthConfiguration authConfiguration = AuthConfiguration.builder()
                    .key(String.format(Constants.AUTH_CONFIGURATION_KEY_FORMAT, domain, subkeyDomain.getSubkey()))
                    .value(Utility.convertListToString(subkeyDomain.getAuthorization(), ","))
                    .build();
            this.logger.log(Level.INFO, () -> String.format("The record with id [%s] related to the subscription key associated to the domain [%s] has triggered the execution. Will be added the following entities: [%s]", subkeyDomain.getId(), subkeyDomain.getDomain(), authConfiguration.getValue()));

            // executing the request towards APIM Authorizer's API
            String refactoredAuthorizerPath = String.format(Constants.AUTH_REFRESH_CONFIGURATION_PATH_TEMPLATE, this.authorizerPath, addInProgress).replace("{domain}", domain);
            this.logger.log(Level.INFO, () -> String.format("Trying to execute a request to the path [%s]", refactoredAuthorizerPath));
            HttpRequest apimRequest = HttpRequest.newBuilder()
                    .uri(new URI(refactoredAuthorizerPath))
                    .version(HttpClient.Version.HTTP_2)
                    .POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(authConfiguration)))
                    .build();
            response = this.httpClient.send(apimRequest, HttpResponse.BodyHandlers.ofString());

        } catch (URISyntaxException | IOException e) {
            this.logger.log(Level.SEVERE, "An error occurred while trying to calling APIM's Authorizer API. The communication with APIM's API failed. ", e);
        }
        return response;
    }
}
