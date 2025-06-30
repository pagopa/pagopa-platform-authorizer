package it.gov.pagopa.authorizer;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import it.gov.pagopa.authorizer.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.exception.AuthorizerConfigException;
import it.gov.pagopa.authorizer.service.CacheService;
import it.gov.pagopa.authorizer.service.impl.AuthorizerConfigClientRetryWrapperImpl;
import it.gov.pagopa.authorizer.util.Constants;

import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CacheNotifier {

    @FunctionName("CacheNotifierFunction")
    public void run (
            @CosmosDBTrigger(
                    name = "CacheNotifierTrigger",
                    databaseName = "authorizer",
                    containerName = "skeydomains",
                    leaseContainerName = "authorizer-leases",
                    createLeaseContainerIfNotExists = true,
                    maxItemsPerInvocation=100,
                    connection = "COSMOS_CONN_STRING"
            )
            List<SubscriptionKeyDomain> triggeredSubkeyDomains,
            final ExecutionContext context) throws InterruptedException, AuthorizerConfigException {

        Logger logger = context.getLogger();
        List<SubscriptionKeyDomain> unprocessedSubkeyDomains = List.copyOf(triggeredSubkeyDomains);

        boolean executeStep = true;
        int retry = 1;
        int maxRetries = getRetryNumber();
        long retryDelay = getStartingRetryDelay();

        final long startingRetryDelayLog = retryDelay;
        logger.log(Level.INFO, () -> String.format("The execution of cache generation from DB trigger will be executed. Max retries: [%d], starting delay on exponential back-off: [%d].", maxRetries, startingRetryDelayLog));

        // retry the execution until all request are cached or until there are made a number of retries equals to the max value
        while (executeStep) {

            // executing main task of caching authorizations
            List<SubscriptionKeyDomain> toBeRetried = new LinkedList<>();
            for (SubscriptionKeyDomain unprocessedSubkeyDomain : unprocessedSubkeyDomains) {
                HttpResponse<String> response = this.getCacheService(logger).addAuthConfigurationToAPIMAuthorizer(unprocessedSubkeyDomain, true);
                if (response.statusCode() != 200) {
                    toBeRetried.add(unprocessedSubkeyDomain);
                }
            }

            // setting the next retry step if something went wrong
            executeStep = !toBeRetried.isEmpty() && retry < maxRetries;
            if (executeStep) {
                retryDelay = (long) (retryDelay * (Math.pow(2.0, retry++)));
                final long retryDelayLog = retryDelay;
                logger.log(Level.INFO, () -> String.format("There are [%d] authorizations that were not correctly cached. Retrying the execution, waiting [%s] ms.", toBeRetried.size(), retryDelayLog));
                unprocessedSubkeyDomains = toBeRetried;
                Thread.sleep(retryDelay);
            }
        }

        logger.log(Level.INFO, () -> "The execution of cache generation from DB trigger is ended.");
    }

    public CacheService getCacheService(Logger logger) {
        return new CacheService(logger, new AuthorizerConfigClientRetryWrapperImpl());
    }

    public int getRetryNumber() {
        String retries = System.getenv(Constants.RETRY_NUMBER_PARAMETER);
        return retries != null ? Integer.parseInt(retries) : Constants.RETRY_NUMBER_PARAMETER_DEFAULT;
    }

    public int getStartingRetryDelay() {
        String retries = System.getenv(Constants.STARTING_RETRY_DELAY_MILLIS_PARAMETER);
        return retries != null ? Integer.parseInt(retries) : Constants.STARTING_RETRY_DELAY_MILLIS_PARAMETER_DEFAULT;
    }
}
