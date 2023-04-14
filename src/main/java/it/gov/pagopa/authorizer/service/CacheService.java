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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CacheService {

    private final String authorizerPath = System.getenv("REFRESH_CONFIGURATION_PATH");

    private final Logger logger;

    private final HttpClient httpClient;

    private final DataAccessObject dao;

    public CacheService(Logger logger, HttpClient httpClient, DataAccessObject dao) {
        this.logger = logger;
        this.httpClient = httpClient;
        this.dao = dao;
    }

    public void addAuthConfigurationBulkToApimAuthorizer(String domain) throws InterruptedException {
        if (domain == null) {
            throw new IllegalArgumentException("Passed null parameter");
        }
        List<SubscriptionKeyDomain> subscriptionKeyDomains = this.dao.findAuthorizationByDomain(domain);
        for (SubscriptionKeyDomain subkeyDomain : subscriptionKeyDomains) {
            HttpResponse<String> response = addAuthConfigurationToAPIMAuthorizer(subkeyDomain);
            this.logger.log(Level.INFO, "Requested configuration to APIM for subscription key domain with id [{}]. Response status: {}", new Object[] {subkeyDomain.getId(), response.statusCode()});
        }
        this.dao.close();
    }

    public HttpResponse<String> addAuthConfigurationToAPIMAuthorizer(SubscriptionKeyDomain subkeyDomain) throws InterruptedException {
        if (subkeyDomain == null) {
            throw new IllegalArgumentException("Passed null parameter");
        }
        HttpResponse<String> response = null;
        try {
            // generating object to be sent
            this.logger.info(String.format("The record with id [%s] related to the subscription key associated to the domain %s has triggered the execution.", subkeyDomain.getId(), subkeyDomain.getDomain()));
            AuthConfiguration authConfiguration = AuthConfiguration.builder()
                    .key(String.format(Constants.AUTH_CONFIGURATION_KEY_FORMAT, subkeyDomain.getDomain(), subkeyDomain.getSubkey()))
                    .value(Utility.convertListToString(subkeyDomain.getAuthorization(), ","))
                    .build();

            // executing the request towards APIM Authorizer's API
            HttpRequest apimRequest = HttpRequest.newBuilder()
                    .uri(new URI(this.authorizerPath))
                    .version(HttpClient.Version.HTTP_2)
                    .POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(authConfiguration)))
                    .build();
            response = this.httpClient.send(apimRequest, HttpResponse.BodyHandlers.ofString());

        } catch (URISyntaxException e) {
            this.logger.log(Level.SEVERE, String.format("An error occurred while trying to calling APIM's Authorizer API. Trying to invoke a wrong path for APIM's API [calling %s]", authorizerPath));
        } catch (IOException e) {
            this.logger.log(Level.SEVERE, "An error occurred while trying to calling APIM's Authorizer API. The communication with APIM's API failed. ", e);
        }
        return response;
    }
}
