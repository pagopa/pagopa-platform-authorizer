package it.gov.pagopa.authorizer;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import it.gov.pagopa.authorizer.service.CacheService;
import it.gov.pagopa.authorizer.service.DataAccessObject;

import java.net.http.HttpClient;
import java.util.Calendar;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CacheGenerator {

    private final String authorizerPath = System.getenv("REFRESH_CONFIGURATION_PATH");
    private final String cosmosUri = System.getenv("SKEYDOMAINS_COSMOS_URI");
    private final String cosmosKey = System.getenv("SKEYDOMAINS_COSMOS_KEY");
    private final String cosmosDB = System.getenv("SKEYDOMAINS_COSMOS_DB");
    private final String cosmosContainer = System.getenv("SKEYDOMAINS_COSMOS_CONTAINER");

    private HttpClient httpClient;
    private DataAccessObject dao;

    @FunctionName("CacheGeneratorFunction")
    public HttpResponseMessage run (
            @HttpTrigger(
                    name = "CacheGeneratorTrigger",
                    methods = {HttpMethod.GET},
                    route = "api/cache-generator/domains/{domain}",
                    authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            @BindingName("domain") String domain,
            final ExecutionContext context) throws InterruptedException {

        Logger logger = context.getLogger();
        this.getCacheService(logger).addAuthConfigurationBulkToApimAuthorizer(domain);
        HttpResponseMessage response = request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .build();
        logger.log(Level.INFO, () -> String.format("The execution will end with an HTTP status code %d", 200));
        return response;
    }

    public CacheService getCacheService(Logger logger) {
        if (this.httpClient == null) {
            long start = Calendar.getInstance().getTimeInMillis();
            this.httpClient = HttpClient.newHttpClient();
            logger.log(Level.INFO, () -> String.format("Generated a new stub for HTTP Client in [%d] ms", Calendar.getInstance().getTimeInMillis() - start));
        }
        if (this.dao == null) {
            long start = Calendar.getInstance().getTimeInMillis();
            this.dao = getDAO(cosmosUri, cosmosKey, cosmosDB, cosmosContainer);
            logger.log(Level.INFO, () -> String.format("Generated a new stub for DAO in  [%d] ms", Calendar.getInstance().getTimeInMillis() - start));
        }
        return new CacheService(logger,
                this.httpClient,
                authorizerPath,
                this.dao);
    }

    public DataAccessObject getDAO(String uri, String key, String db, String container) {
        return new DataAccessObject(uri, key, db, container);
    }
}
