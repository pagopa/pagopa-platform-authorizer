package it.gov.pagopa.authorizer;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.CosmosDBInput;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import it.gov.pagopa.authorizer.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.service.CacheService;
import it.gov.pagopa.authorizer.service.DataAccessObject;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CacheNotifier {

    @FunctionName("CacheNotifierFunction")
    public HttpResponseMessage run (
            @HttpTrigger(
                    name = "CacheNotifierTrigger",
                    methods = {HttpMethod.GET, HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            @CosmosDBInput(
                    name = "CacheNotifierInput",
                    databaseName = "db",
                    containerName = "authorization",
                    connection = "COSMOS_CONN_STRING",
                    sqlQuery = "%TRIGGER_SQL_QUERY%")
            Optional<SubscriptionKeyDomain> triggeredSubkeyDomain,
            final ExecutionContext context) throws InterruptedException {

        Logger logger = context.getLogger();
        HttpResponse<String> responseContent = null;

        if (triggeredSubkeyDomain.isPresent()) {
            responseContent = this.getCacheService(logger).addAuthConfigurationToAPIMAuthorizer(triggeredSubkeyDomain.get());
        }

        int httpStatusCode = responseContent != null ? responseContent.statusCode() : 500;
        HttpResponseMessage response = request.createResponseBuilder(HttpStatus.valueOf(httpStatusCode))
                .header("Content-Type", "application/json")
                .build();
        logger.log(Level.INFO, "The execution will end with an HTTP status code {}", httpStatusCode);
        return response;
    }

    public CacheService getCacheService(Logger logger) {
        return new CacheService(logger,
                HttpClient.newHttpClient(),
                new DataAccessObject(System.getenv("SKEYDOMAINS_COSMOS_URI"),
                        System.getenv("SKEYDOMAINS_COSMOS_KEY"),
                        System.getenv("SKEYDOMAINS_COSMOS_DB"),
                        System.getenv("SKEYDOMAINS_COSMOS_CONTAINER")
                )
        );
    }
}
