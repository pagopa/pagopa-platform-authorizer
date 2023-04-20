package it.gov.pagopa.authorizer.service;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import it.gov.pagopa.authorizer.entity.SubscriptionKeyDomain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataAccessObject {

    private String cosmosDatabase;

    private String cosmosContainer;

    private CosmosClient client;

    private Logger logger;

    public DataAccessObject(String uri, String key, String database, String container) {
        this.client = new CosmosClientBuilder()
                .endpoint(uri)
                .credential(new AzureKeyCredential(key))
                .directMode(new DirectConnectionConfig(), new GatewayConnectionConfig())
                .buildClient();
        this.cosmosDatabase = database;
        this.cosmosContainer = container;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public List<SubscriptionKeyDomain> findAuthorizationByDomain(String domain) {
        List<SubscriptionKeyDomain> results = new ArrayList<>();
        CosmosDatabase db = client.getDatabase(this.cosmosDatabase);
        CosmosContainer container = db.getContainer(this.cosmosContainer);
        String query = String.format("SELECT * FROM a WHERE a.domain = '%s'", domain);
        this.logger.log(Level.INFO, () -> String.format("Trying to execute the query: [%s]", query));
        CosmosPagedIterable<SubscriptionKeyDomain> subscriptionKeyDomains = container.queryItems(query, new CosmosQueryRequestOptions(), SubscriptionKeyDomain.class);
        this.logger.log(Level.INFO, () -> String.format("Query executed. Found [%s] elements.", subscriptionKeyDomains.stream().count()));
        Iterator<SubscriptionKeyDomain> it = subscriptionKeyDomains.iterator();
        while (it.hasNext()) {
            SubscriptionKeyDomain subscriptionKeyDomain = subscriptionKeyDomains.iterator().next();
            results.add(subscriptionKeyDomain);
            this.logger.log(Level.INFO, () -> String.format("The following list of entities are related to the domain [%s] at subscription key [%s******]: [%s]", subscriptionKeyDomain.getDomain(), subscriptionKeyDomain.getSubkey().substring(0, 3), subscriptionKeyDomain.getAuthorization()));
        }
        return results;
    }

    public void close() {
        this.client.close();
    }
}
