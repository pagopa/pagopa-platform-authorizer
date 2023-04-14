package it.gov.pagopa.authorizer.service;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import it.gov.pagopa.authorizer.entity.SubscriptionKeyDomain;

import java.util.ArrayList;
import java.util.List;

public class DataAccessObject {

    private String cosmosDatabase;

    private String cosmosContainer;

    private CosmosClient client;

    public DataAccessObject(String uri, String key, String database, String container) {
        this.client = new CosmosClientBuilder()
                .endpoint(uri)
                .credential(new AzureKeyCredential(key))
                .directMode(new DirectConnectionConfig(), new GatewayConnectionConfig())
                .buildClient();
        this.cosmosDatabase = database;
        this.cosmosContainer = container;
    }

    public List<SubscriptionKeyDomain> findAuthorizationByDomain(String domain) {
        List<SubscriptionKeyDomain> results = new ArrayList<>();
        CosmosDatabase db = client.getDatabase(this.cosmosDatabase);
        CosmosContainer container = db.getContainer(this.cosmosContainer);
        CosmosPagedIterable<SubscriptionKeyDomain> subscriptionKeyDomains = container.queryItems(
                String.format("SELECT * FROM a WHERE a.domain = '%s'", domain),
                new CosmosQueryRequestOptions(),
                SubscriptionKeyDomain.class);
        while (subscriptionKeyDomains.iterator().hasNext()) {
            results.add(subscriptionKeyDomains.iterator().next());
        }
        return results;
    }

    public void close() {
        this.client.close();
    }
}
