package it.gov.pagopa.authorizer.client;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.util.CosmosPagedIterable;

import it.gov.pagopa.authorizer.entity.SubscriptionKeyDomain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthCosmosClientTest {


    @Test
    void runOk() {
        String domain = "domain";

        CosmosClient mockClient = mock(CosmosClient.class);

        CosmosDatabase mockDatabase = mock(CosmosDatabase.class);
        CosmosContainer mockContainer = mock(CosmosContainer.class);

        CosmosPagedIterable mockIterable = mock(CosmosPagedIterable.class);
        Iterator<FeedResponse> mockIterator = mock(Iterator.class);
        FeedResponse<SubscriptionKeyDomain> mockFeedResponse = mock(FeedResponse.class);
        SubscriptionKeyDomain subscriptionKeyDomain = new SubscriptionKeyDomain();
        subscriptionKeyDomain.setDomain(domain);

        when(mockFeedResponse.getResults()).thenReturn(List.of(subscriptionKeyDomain));
        when(mockIterator.next()).thenReturn(mockFeedResponse);
        when(mockIterable.iterator()).thenReturn(mockIterator);
        doReturn(mockIterable).when(mockContainer)
                .queryItems(anyString(), any(CosmosQueryRequestOptions.class), any());
        when(mockDatabase.getContainer(any())).thenReturn(mockContainer);
        when(mockClient.getDatabase(any())).thenReturn(mockDatabase);

        AuthCosmosClient client = new AuthCosmosClient(mockClient);

        Assertions.assertDoesNotThrow(() -> client.getSubkeyDomainPage(domain, null));
    }
}