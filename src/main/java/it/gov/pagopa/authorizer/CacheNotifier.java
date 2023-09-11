package it.gov.pagopa.authorizer;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import it.gov.pagopa.authorizer.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.service.CacheService;
import it.gov.pagopa.authorizer.util.Constants;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CacheNotifier {

    private final String authorizerPath = System.getenv(Constants.REFRESH_CONFIG_PATH_PARAMETER);

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
            List<SubscriptionKeyDomain> triggeredSubkeyDomains,
            final ExecutionContext context) throws InterruptedException {

        Logger logger = context.getLogger();
        HttpResponse<String> responseContent = null;

        for (SubscriptionKeyDomain triggeredSubkeyDomain : triggeredSubkeyDomains) {
            this.getCacheService(logger).addAuthConfigurationToAPIMAuthorizer(triggeredSubkeyDomain, true);
        }
        logger.log(Level.INFO, () -> "The execution of cache generation from DB trigger is ended.");
    }

    public CacheService getCacheService(Logger logger) {
        return new CacheService(logger, HttpClient.newHttpClient(), authorizerPath);
    }
}
