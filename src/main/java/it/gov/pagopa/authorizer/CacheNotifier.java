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

    private final String authorizerPath = System.getenv("REFRESH_CONFIGURATION_PATH");
    private final String cosmosUri = System.getenv("SKEYDOMAINS_COSMOS_URI");
    private final String cosmosKey = System.getenv("SKEYDOMAINS_COSMOS_KEY");
    private final String cosmosDB = System.getenv("SKEYDOMAINS_COSMOS_DB");
    private final String cosmosContainer = System.getenv("SKEYDOMAINS_COSMOS_CONTAINER");

    @FunctionName("CacheNotifierFunction")
    public HttpResponseMessage run (
            @HttpTrigger(
                    name = "CacheNotifierTrigger",
                    methods = {HttpMethod.GET, HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            @CosmosDBInput(
                    name = "CacheNotifierInput",
                    databaseName = "authorizer",
                    containerName = "skeydomains",
                    connection = "COSMOS_CONN_STRING")
                    // sqlQuery = "%TRIGGER_SQL_QUERY%") TODO temporarly commented
            Optional<SubscriptionKeyDomain> triggeredSubkeyDomain,
            final ExecutionContext context) throws InterruptedException {

        Logger logger = context.getLogger();
        HttpResponse<String> responseContent = null;

        if (triggeredSubkeyDomain.isPresent()) {
            responseContent = this.getCacheService(logger).addAuthConfigurationToAPIMAuthorizer(triggeredSubkeyDomain.get(), true);
        }

        int httpStatusCode = responseContent != null ? responseContent.statusCode() : 500;
        HttpResponseMessage response = request.createResponseBuilder(HttpStatus.valueOf(httpStatusCode))
                .header("Content-Type", "application/json")
                .build();
        logger.log(Level.INFO, "The execution will end with an HTTP status code {0}", httpStatusCode);
        return response;
    }

    public CacheService getCacheService(Logger logger) {
        return new CacheService(logger,
                HttpClient.newHttpClient(),
                authorizerPath,
                getDAO(cosmosUri, cosmosKey, cosmosDB, cosmosContainer));
    }

    public DataAccessObject getDAO(String uri, String key, String db, String container) {
        return new DataAccessObject(uri, key, db, container);
    }
}
