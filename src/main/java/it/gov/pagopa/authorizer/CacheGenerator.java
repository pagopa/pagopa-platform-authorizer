package it.gov.pagopa.authorizer;

import com.azure.cosmos.models.FeedResponse;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import it.gov.pagopa.authorizer.client.AuthCosmosClient;
import it.gov.pagopa.authorizer.client.impl.AuthCosmosClientImpl;
import it.gov.pagopa.authorizer.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.exception.AuthorizerConfigException;
import it.gov.pagopa.authorizer.service.AuthorizerConfigClientRetryWrapper;
import it.gov.pagopa.authorizer.service.CacheService;
import it.gov.pagopa.authorizer.service.impl.AuthorizerConfigClientRetryWrapperImpl;

import java.net.http.HttpResponse;
import java.util.Calendar;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CacheGenerator {

    @FunctionName("CacheGeneratorFunction")
    public HttpResponseMessage run (
            @HttpTrigger(
                    name = "CacheGeneratorTrigger",
                    methods = {HttpMethod.GET},
                    route = "api/cache-generator/domains/{domain}",
                    authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            @BindingName("domain") String domain,
            final ExecutionContext context) throws InterruptedException, AuthorizerConfigException {

        Logger logger = context.getLogger();
        AuthCosmosClient authCosmosClient = getAuthCosmosClient();
        CacheService cacheService = getCacheService(logger);

        String continuationToken = null;
        do {
            Iterable<FeedResponse<SubscriptionKeyDomain>> feedResponseIterable =
                    authCosmosClient.getSubkeyDomainPage(domain, continuationToken);
            for (FeedResponse<SubscriptionKeyDomain> page : feedResponseIterable) {
                logger.log(Level.INFO, () -> String.format("Called endpoint [%s]: found [%d] element(s) in this page related to the requested domain.", request.getUri().getPath(), page.getResults().size()));
                for(SubscriptionKeyDomain subkeyDomain: page.getResults()) {
                    HttpResponse<String> response = cacheService.addAuthConfigurationToAPIMAuthorizer(subkeyDomain, false);
                    final int statusCode = response != null ? response.statusCode() : 500;
                    logger.log(Level.FINE, () -> String.format("Requested configuration to APIM for subscription key domain with id [%s]. Response status: %d", subkeyDomain.getId(), statusCode));
                }
                continuationToken = page.getContinuationToken();
            }
        } while (continuationToken != null);

        HttpResponseMessage response = request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .build();
        logger.log(Level.INFO, () -> String.format("The execution will end with an HTTP status code %d", 200));
        return response;
    }

    public CacheService getCacheService(Logger logger) {
        long start = Calendar.getInstance().getTimeInMillis();
        AuthorizerConfigClientRetryWrapper authorizerConfigClientRetryWrapper = new AuthorizerConfigClientRetryWrapperImpl();
        logger.log(Level.INFO, () -> String.format("Generated a new stub for HTTP Client in [%d] ms", Calendar.getInstance().getTimeInMillis() - start));
        return new CacheService(logger, authorizerConfigClientRetryWrapper);

    }

    public AuthCosmosClient getAuthCosmosClient() {
        return AuthCosmosClientImpl.getInstance();
    }
}
