package it.gov.pagopa.authorizer.client;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import it.gov.pagopa.authorizer.entity.SubscriptionKeyDomain;

public class AuthCosmosClient {

    private static AuthCosmosClient instance;

    private final String databaseId = System.getenv("COSMOS_AUTH_DB_NAME");
    private final String containerId = System.getenv("COSMOS_AUTH_CONTAINER_NAME");

    private final CosmosClient cosmosClient;

    private AuthCosmosClient() {
        String azureKey = System.getenv("COSMOS_AUTH_KEY");
        String serviceEndpoint = System.getenv("COSMOS_AUTH_ENDPOINT");

        this.cosmosClient = new CosmosClientBuilder()
                .endpoint(serviceEndpoint)
                .key(azureKey)
                .buildClient();
    }

    public AuthCosmosClient(CosmosClient cosmosClient) {
        this.cosmosClient = cosmosClient;
    }

    public static AuthCosmosClient getInstance() {
        if (instance == null) {
            instance = new AuthCosmosClient();
        }
        return instance;
    }

    public Iterable<FeedResponse<SubscriptionKeyDomain>> getSubkeyDomainPage(String domain, String continuationToken, Integer pageSize) {
        CosmosDatabase cosmosDatabase = this.cosmosClient.getDatabase(databaseId);
        CosmosContainer cosmosContainer = cosmosDatabase.getContainer(containerId);

        //Build query
        String query = String.format("SELECT * FROM SubscriptionKeyDomain s WHERE s.domain = '%s'",
                domain);

        //Query the container
        Iterable<FeedResponse<SubscriptionKeyDomain>> iterables = cosmosContainer
                .queryItems(query, new CosmosQueryRequestOptions(), SubscriptionKeyDomain.class)
                .iterableByPage(continuationToken, pageSize);

        return iterables;
    }
}
