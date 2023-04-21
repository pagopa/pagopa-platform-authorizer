package it.gov.pagopa.authorizer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import it.gov.pagopa.authorizer.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.service.CacheService;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CacheNotifier {

    private final String authorizerPath = System.getenv("REFRESH_CONFIGURATION_PATH");

    @FunctionName("CacheNotifierFunction")
    public void run (
            @CosmosDBTrigger(
                    name = "CacheNotifierTrigger",
                    databaseName = "authorizer",
                    collectionName = "skeydomains",
                    leaseCollectionName = "authorizer-leases",
                    createLeaseCollectionIfNotExists = true,
                    maxItemsPerInvocation=100,
                    connectionStringSetting = "COSMOS_CONN_STRING"
            )
            String[] documents,
            final ExecutionContext context) throws InterruptedException {

        Logger logger = context.getLogger();
        HttpResponse<String> responseContent = null;

        ObjectMapper mapper = new ObjectMapper();
        for (String document : documents) {
            try {
                SubscriptionKeyDomain triggeredSubkeyDomain = mapper.readValue(document, SubscriptionKeyDomain.class);
                responseContent = this.getCacheService(logger).addAuthConfigurationToAPIMAuthorizer(triggeredSubkeyDomain, true);
            } catch (JsonProcessingException e) {
                logger.log(Level.SEVERE, () -> String.format("Error while mapping the following document: %s", document));
            }
        }
        final int statusCode = responseContent != null ? responseContent.statusCode() : 500;
        logger.log(Level.INFO, () -> String.format("The execution will end with an HTTP status code %s", statusCode));
    }

    public CacheService getCacheService(Logger logger) {
        return new CacheService(logger, HttpClient.newHttpClient(), authorizerPath);
    }
}
