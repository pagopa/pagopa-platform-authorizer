package it.gov.pagopa.authorizer.client;

import com.azure.cosmos.models.FeedResponse;
import it.gov.pagopa.authorizer.entity.SubscriptionKeyDomain;

public interface AuthCosmosClient {

    /**
     * Sends a request to refresh the authorization configuration using the given parameters.
     *
     * @param domain the domain for which the configuration is to be refreshed.
     * @param continuationToken continuation token for the pagination implementation.
     * @return the iterable containing the selected pages by the query.
     */
    Iterable<FeedResponse<SubscriptionKeyDomain>> getSubkeyDomainPage(String domain, String continuationToken);
}
