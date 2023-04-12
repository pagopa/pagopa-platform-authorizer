package it.gov.pagopa.authorizer;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.CosmosDBInput;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import it.gov.pagopa.authorizer.model.SubscriptionKeyDomain;
import jakarta.ws.rs.core.MediaType;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CacheNotifier {

    private final String APIM_REFRESH_CONFIGURATION_PATH = System.getenv("REFRESH_CONFIGURATION_PATH");

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
            Optional<SubscriptionKeyDomain> subkeyDomain,
            final ExecutionContext context) {

        Logger logger = context.getLogger();

        // create an object that contains domains_subkey and authorized entity
        // execute an HTTP POST call on APIM_REFRESH_CONFIGURATION_PATH

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", MediaType.TEXT_PLAIN)
                .body("")
                .build();
    }
}
