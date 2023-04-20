package it.gov.pagopa.authorizer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import it.gov.pagopa.authorizer.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.service.CacheService;
import it.gov.pagopa.authorizer.service.DataAccessObject;

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
                    containerName = "skeydomains",
                    connection = "COSMOS_CONN_STRING",
                    leaseContainerName = "authorizer_lease",
                    createLeaseContainerIfNotExists = true
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
                logger.log(Level.SEVERE, "Error while mapping the following document: {0}", document);
            }
        }
        logger.log(Level.INFO, "The execution will end with an HTTP status code {0}", responseContent != null ? responseContent.statusCode() : 500);
    }

    public CacheService getCacheService(Logger logger) {
        return new CacheService(logger, HttpClient.newHttpClient(), authorizerPath);
    }
}
