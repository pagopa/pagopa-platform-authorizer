package it.gov.pagopa.authorizer.client.impl;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import it.gov.pagopa.authorizer.client.AuthCosmosClient;
import it.gov.pagopa.authorizer.entity.SubscriptionKeyDomain;

public class AuthCosmosClientImpl implements AuthCosmosClient {

    private static AuthCosmosClientImpl instance;

    private final String databaseId = System.getenv("COSMOS_AUTH_DB_NAME");
    private final String containerId = System.getenv("COSMOS_AUTH_CONTAINER_NAME");
    private final int cosmosPageSize = Integer.parseInt(System.getenv().getOrDefault("COSMOS_PAGE_SIZE", "100"));

    private final CosmosClient cosmosClient;

    private AuthCosmosClientImpl() {
        String azureKey = System.getenv("COSMOS_AUTH_KEY");
        String serviceEndpoint = System.getenv("COSMOS_AUTH_ENDPOINT");

        this.cosmosClient = new CosmosClientBuilder()
                .endpoint(serviceEndpoint)
                .key(azureKey)
                .buildClient();
    }

    public AuthCosmosClientImpl(CosmosClient cosmosClient) {
        this.cosmosClient = cosmosClient;
    }

    public static AuthCosmosClientImpl getInstance() {
        if (instance == null) {
            instance = new AuthCosmosClientImpl();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<FeedResponse<SubscriptionKeyDomain>> getSubkeyDomainPage(String domain, String continuationToken) {
        CosmosDatabase cosmosDatabase = this.cosmosClient.getDatabase(databaseId);
        CosmosContainer cosmosContainer = cosmosDatabase.getContainer(containerId);

        //Build query
        String query = String.format("SELECT * FROM SubscriptionKeyDomain s WHERE s.domain = '%s'",
                domain);

        //Query the container
        return cosmosContainer
                .queryItems(query, new CosmosQueryRequestOptions(), SubscriptionKeyDomain.class)
                .iterableByPage(continuationToken, cosmosPageSize);
    }
}
