package it.gov.pagopa.authorizer.service;

import it.gov.pagopa.authorizer.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.util.MockHttpResponse;
import it.gov.pagopa.authorizer.util.ResponseSubscriber;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    private static final String DOMAIN = "domain";

    private static final String AUTHORIZER_PATH = "http://fake.authorizer.path.org";

    private final Logger logger = Logger.getLogger("example-test-logger");

    @Mock
    DataAccessObject dao;

    @Mock
    HttpClient httpClient;

    @SneakyThrows
    @Test
    void addAuthConfigurationBulkToApimAuthorizer_OK() {

        // Mocking passed values
        List<SubscriptionKeyDomain> subkeyDomains = getSubscriptionKeyDomains(DOMAIN);
        MockHttpResponse mockedHttpResponse = MockHttpResponse.builder().statusCode(200).uri(new URI("")).build();

        // Mocking execution for service's internal component
        CacheService cacheService = spy(new CacheService(logger, httpClient, AUTHORIZER_PATH, dao));
        doReturn(subkeyDomains).when(dao).findAuthorizationByDomain(DOMAIN);
        doReturn(mockedHttpResponse).when(httpClient).send(any(), any());

        // Execute function
        cacheService.addAuthConfigurationBulkToApimAuthorizer(DOMAIN);

        // Checking assertions
        verify(dao, times(1)).findAuthorizationByDomain(anyString());
        verify(dao, times(1)).close();
        verify(cacheService, times(2)).addAuthConfigurationToAPIMAuthorizer(any());
        verify(httpClient, times(2)).send(any(), any());
    }

    @SneakyThrows
    @Test
    void addAuthConfigurationBulkToApimAuthorizer_OK_noElementFoundInDB() {

        // Mocking execution for service's internal component
        CacheService cacheService = spy(new CacheService(logger, httpClient, AUTHORIZER_PATH, dao));
        doReturn(List.of()).when(dao).findAuthorizationByDomain(DOMAIN);
        //doReturn(mockedHttpResponse).when(httpClient).send(any(), any());

        // Execute function
        cacheService.addAuthConfigurationBulkToApimAuthorizer(DOMAIN);

        // Checking assertions
        verify(dao, times(1)).findAuthorizationByDomain(anyString());
        verify(dao, times(1)).close();
        verify(cacheService, times(0)).addAuthConfigurationToAPIMAuthorizer(any());
        verify(httpClient, times(0)).send(any(), any());
    }

    @SneakyThrows
    @Test
    void addAuthConfigurationBulkToApimAuthorizer_KO_communicationError() {

        // Mocking passed values
        List<SubscriptionKeyDomain> subkeyDomains = getSubscriptionKeyDomains(DOMAIN);

        // Mocking execution for service's internal component
        HttpClient realHttpClient = spy(HttpClient.newHttpClient());
        CacheService cacheService = spy(new CacheService(logger, realHttpClient, "https://api.ENV.pagopa.it", dao));
        doReturn(subkeyDomains).when(dao).findAuthorizationByDomain(DOMAIN);

        // Execute function
        cacheService.addAuthConfigurationBulkToApimAuthorizer(DOMAIN);

        // Checking assertions
        verify(dao, times(1)).findAuthorizationByDomain(anyString());
        verify(dao, times(1)).close();
        verify(cacheService, times(2)).addAuthConfigurationToAPIMAuthorizer(any());
        verify(realHttpClient, times(2)).send(any(), any());
    }

    @SneakyThrows
    @Test
    void addAuthConfigurationBulkToApimAuthorizer_KO_invalidParameter() {

        // Mocking execution for service's internal component
        CacheService cacheService = spy(new CacheService(logger, httpClient, AUTHORIZER_PATH, dao));

        // Execute function
        assertThrows(IllegalArgumentException.class, () -> cacheService.addAuthConfigurationBulkToApimAuthorizer(null));

        // Checking assertions
        verify(dao, times(0)).findAuthorizationByDomain(anyString());
        verify(dao, times(0)).close();
        verify(cacheService, times(0)).addAuthConfigurationToAPIMAuthorizer(any());
        verify(httpClient, times(0)).send(any(), any());
    }

    @SneakyThrows
    @Test
    void addAuthConfigurationToAPIMAuthorizer_OK() {

        // Mocking passed values
        SubscriptionKeyDomain subkeyDomain = getSubscriptionKeyDomains(DOMAIN).get(0);
        String subkeyDomainAsString = "{\"key\":\"domain_1\",\"value\":\"entity1,entity2,entity3\"}";
        MockHttpResponse mockedHttpResponse = MockHttpResponse.builder().statusCode(200).uri(new URI("")).build();

        // Mocking execution for service's internal component
        CacheService cacheService = spy(new CacheService(logger, httpClient, AUTHORIZER_PATH, dao));
        doReturn(mockedHttpResponse).when(httpClient).send(any(), any());

        // Execute function
        cacheService.addAuthConfigurationToAPIMAuthorizer(subkeyDomain);

        // Checking assertions
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(1)).send(requestCaptor.capture(), any());
        assertEquals(subkeyDomainAsString, extractRequestMadeToAPIM(requestCaptor));
    }

    @SneakyThrows
    @Test
    void addAuthConfigurationToAPIMAuthorizer_OK_noAuthorizationEntities() {

        // Mocking passed values
        SubscriptionKeyDomain subkeyDomain = getSubscriptionKeyDomains(DOMAIN).get(0);
        subkeyDomain.setAuthorization(List.of());
        String subkeyDomainAsString = "{\"key\":\"domain_1\",\"value\":\"\"}";
        MockHttpResponse mockedHttpResponse = MockHttpResponse.builder().statusCode(200).uri(new URI("")).build();

        // Mocking execution for service's internal component
        CacheService cacheService = spy(new CacheService(logger, httpClient, AUTHORIZER_PATH, dao));
        doReturn(mockedHttpResponse).when(httpClient).send(any(), any());

        // Execute function
        cacheService.addAuthConfigurationToAPIMAuthorizer(subkeyDomain);

        // Checking assertions
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(1)).send(requestCaptor.capture(), any());
        assertEquals(subkeyDomainAsString, extractRequestMadeToAPIM(requestCaptor));
    }

    @SneakyThrows
    @Test
    void addAuthConfigurationToAPIMAuthorizer_KO_communicationError() {

        // Mocking passed values
        SubscriptionKeyDomain subkeyDomain = getSubscriptionKeyDomains(DOMAIN).get(0);
        String subkeyDomainAsString = "{\"key\":\"domain_1\",\"value\":\"entity1,entity2,entity3\"}";

        // Mocking execution for service's internal component
        HttpClient realHttpClient = spy(HttpClient.newHttpClient());
        CacheService cacheService = spy(new CacheService(logger, realHttpClient, "https://api.ENV.pagopa.it", dao));

        // Execute function
        cacheService.addAuthConfigurationToAPIMAuthorizer(subkeyDomain);

        // Checking assertions
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(realHttpClient, times(1)).send(requestCaptor.capture(), any());
        assertEquals(subkeyDomainAsString, extractRequestMadeToAPIM(requestCaptor));

    }

    @SneakyThrows
    @Test
    void addAuthConfigurationToAPIMAuthorizer_KO_invalidParameter() {

        // Mocking execution for service's internal component
        CacheService cacheService = spy(new CacheService(logger, httpClient, AUTHORIZER_PATH));

        // Execute function
        assertThrows(IllegalArgumentException.class, () -> cacheService.addAuthConfigurationToAPIMAuthorizer(null));

        // Checking assertions
        verify(httpClient, times(0)).send(any(), any());
    }

    private List<SubscriptionKeyDomain> getSubscriptionKeyDomains(String domain) {
        return List.of(
                SubscriptionKeyDomain.builder()
                        .id(UUID.randomUUID().toString())
                        .domain(domain)
                        .subkey("1")
                        .authorization(List.of("entity1", "entity2", "entity3"))
                        .build(),
                SubscriptionKeyDomain.builder()
                        .id(UUID.randomUUID().toString())
                        .domain(domain)
                        .subkey("2")
                        .authorization(List.of("entity1", "entity4"))
                        .build()
        );
    }

    private String extractRequestMadeToAPIM(ArgumentCaptor<HttpRequest> requestCaptor) {
        String response;
        try {
            Optional<String> content = requestCaptor.getValue().bodyPublisher().map(publisher -> {
                HttpResponse.BodySubscriber<String> bodySubscriber = HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
                ResponseSubscriber flowSubscriber = new ResponseSubscriber(bodySubscriber);
                publisher.subscribe(flowSubscriber);
                return bodySubscriber.getBody().toCompletableFuture().join();
            });
            response = content.orElse("");
        } catch (MockitoException e) {
            response = "";
        }
        return response;
    }
}
