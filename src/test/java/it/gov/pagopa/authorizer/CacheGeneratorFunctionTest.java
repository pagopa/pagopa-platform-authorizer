package it.gov.pagopa.authorizer;

import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import it.gov.pagopa.authorizer.client.AuthCosmosClient;
import it.gov.pagopa.authorizer.entity.AuthorizedEntity;
import it.gov.pagopa.authorizer.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.exception.AuthorizerConfigException;
import it.gov.pagopa.authorizer.service.CacheService;
import it.gov.pagopa.authorizer.util.MockHttpResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CacheGeneratorFunctionTest {

    private static final String DOMAIN = "domain";

    @Spy
    CacheGenerator function;

    @Mock
    CacheService cacheService;

    @Mock
    AuthCosmosClient authCosmosClient;

    @Mock
    ExecutionContext context;

    @SneakyThrows
    @Test
    void runOk() {

        // Mocking service and client creation
        Logger logger = Logger.getLogger("example-test-logger");
        List<SubscriptionKeyDomain> subscriptionKeyDomains = List.of(getSubscriptionKeyDomains());
        FeedResponse<SubscriptionKeyDomain> feedResponse = mock(FeedResponse.class);

        when(context.getLogger()).thenReturn(logger);
        when(function.getCacheService(logger)).thenReturn(cacheService);
        when(feedResponse.getContinuationToken()).thenReturn(null);
        when(feedResponse.getResults()).thenReturn(subscriptionKeyDomains);
        doReturn(Collections.singletonList(feedResponse)).when(authCosmosClient)
                .getSubkeyDomainPage(Mockito.eq(DOMAIN), nullable(String.class));
        doReturn(authCosmosClient).when(function).getAuthCosmosClient();


        // Mocking communication with APIM
        MockHttpResponse mockedHttpResponse = MockHttpResponse.builder().statusCode(200).uri(new URI("")).build();
        doReturn(mockedHttpResponse).when(cacheService).addAuthConfigurationToAPIMAuthorizer(any(), anyBoolean());

        // Generating request, mocking the field creation
        HttpRequestMessage<Optional<String>> request = mock(HttpRequestMessage.class);
        doReturn(new URI("")).when(request).getUri();

        // Generate mock response mocking the field creation
        final HttpResponseMessage.Builder builder = mock(HttpResponseMessage.Builder.class);
        HttpResponseMessage responseMock = mock(HttpResponseMessage.class);
        doReturn(builder).when(request).createResponseBuilder(any(HttpStatus.class));
        doReturn(builder).when(builder).header(anyString(), anyString());
        doReturn(HttpStatus.valueOf(mockedHttpResponse.statusCode())).when(responseMock).getStatus();
        doReturn(responseMock).when(builder).build();

        // Execute function
        HttpResponseMessage response = function.run(request, DOMAIN, context);

        // Checking assertions
        assertEquals(HttpStatus.OK, response.getStatus());
    }

    @SneakyThrows
    @Test
    void runOk_nullResponse() {

        // Mocking service and client creation
        Logger logger = Logger.getLogger("example-test-logger");
        List<SubscriptionKeyDomain> subscriptionKeyDomains = List.of(getSubscriptionKeyDomains());
        FeedResponse feedResponse = mock(FeedResponse.class);

        when(context.getLogger()).thenReturn(logger);
        when(function.getCacheService(logger)).thenReturn(cacheService);
        doReturn(authCosmosClient).when(function).getAuthCosmosClient();
        doReturn(subscriptionKeyDomains).when(feedResponse).getResults();
        doReturn(Collections.singletonList(feedResponse)).when(authCosmosClient)
                .getSubkeyDomainPage(Mockito.eq(DOMAIN), nullable(String.class));

        // Mocking communication with APIM
        doReturn(null).when(cacheService).addAuthConfigurationToAPIMAuthorizer(any(), anyBoolean());

        // Generating request, mocking the field creation
        HttpRequestMessage<Optional<String>> request = mock(HttpRequestMessage.class);
        doReturn(new URI("")).when(request).getUri();

        // Generate mock response mocking the field creation
        final HttpResponseMessage.Builder builder = mock(HttpResponseMessage.Builder.class);
        HttpResponseMessage responseMock = mock(HttpResponseMessage.class);
        doReturn(builder).when(request).createResponseBuilder(any(HttpStatus.class));
        doReturn(builder).when(builder).header(anyString(), anyString());
        doReturn(HttpStatus.valueOf(200)).when(responseMock).getStatus();
        doReturn(responseMock).when(builder).build();

        // Execute function
        HttpResponseMessage response = function.run(request, DOMAIN, context);

        // Checking assertions
        assertEquals(HttpStatus.OK, response.getStatus());
    }

    @SneakyThrows
    @Test
    void runKo_interruptedException() {

        // Mocking service and client creation
        Logger logger = Logger.getLogger("example-test-logger");
        List<SubscriptionKeyDomain> subscriptionKeyDomains = List.of(getSubscriptionKeyDomains());
        FeedResponse feedResponse = mock(FeedResponse.class);
        InterruptedException interruptedException = new InterruptedException();

        when(context.getLogger()).thenReturn(logger);
        when(function.getCacheService(logger)).thenReturn(cacheService);
        doReturn(authCosmosClient).when(function).getAuthCosmosClient();
        doReturn(subscriptionKeyDomains).when(feedResponse).getResults();
        doReturn(Collections.singletonList(feedResponse)).when(authCosmosClient)
                .getSubkeyDomainPage(Mockito.eq(DOMAIN), nullable(String.class));

        // Mocking communication with APIM
        doThrow(interruptedException).when(cacheService).addAuthConfigurationToAPIMAuthorizer(any(), anyBoolean());

        // Generating request, mocking the field creation
        HttpRequestMessage<Optional<String>> request = mock(HttpRequestMessage.class);
        doReturn(new URI("")).when(request).getUri();

        // Generate mock response mocking the field creation
        final HttpResponseMessage.Builder builder = mock(HttpResponseMessage.Builder.class);
        HttpResponseMessage responseMock = mock(HttpResponseMessage.class);
        doReturn(builder).when(request).createResponseBuilder(any(HttpStatus.class));
        doReturn(builder).when(builder).header(anyString(), anyString());
        doReturn(HttpStatus.valueOf(200)).when(responseMock).getStatus();
        doReturn(responseMock).when(builder).build();
    }

    private SubscriptionKeyDomain[] getSubscriptionKeyDomains() {
        return new SubscriptionKeyDomain[]{
                SubscriptionKeyDomain.builder()
                        .id(UUID.randomUUID().toString())
                        .domain(DOMAIN)
                        .subkey("1")
                        .authorizedEntities(List.of(
                                AuthorizedEntity.builder().name("First entity").value("entity1").build(),
                                AuthorizedEntity.builder().name("Second entity").value("entity2").build(),
                                AuthorizedEntity.builder().name("Third entity").value("entity3").build()
                        ))
                        .build(),
                SubscriptionKeyDomain.builder()
                        .id(UUID.randomUUID().toString())
                        .domain(DOMAIN)
                        .subkey("2")
                        .authorizedEntities(List.of(
                                AuthorizedEntity.builder().name("First entity").value("entity1").build(),
                                AuthorizedEntity.builder().name("Composite entity").values(List.of("entity4", "sub-entity")).build()
                        ))
                        .build()
        };
    }
}
