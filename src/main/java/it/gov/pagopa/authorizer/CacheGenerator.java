package it.gov.pagopa.authorizer;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import it.gov.pagopa.authorizer.service.CacheService;
import it.gov.pagopa.authorizer.service.DataAccessObject;

import java.net.http.HttpClient;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CacheGenerator {

    @FunctionName("CacheGeneratorFunction")
    public HttpResponseMessage run (
            @HttpTrigger(
                    name = "CacheGeneratorTrigger",
                    methods = {HttpMethod.GET},
                    route = "/cache-generation/domains/{domain}",
                    authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            @BindingName("domain") String domain,
            final ExecutionContext context) throws InterruptedException {

        Logger logger = context.getLogger();
        this.getCacheService(logger).addAuthConfigurationBulkToApimAuthorizer(domain);
        HttpResponseMessage response = request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .build();
        logger.log(Level.INFO, "The execution will end with an HTTP status code {}", 200);
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
