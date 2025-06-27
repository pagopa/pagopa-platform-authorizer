package it.gov.pagopa.authorizer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.authorizer.entity.AuthorizedEntity;
import it.gov.pagopa.authorizer.entity.GenericPair;
import it.gov.pagopa.authorizer.entity.Metadata;
import it.gov.pagopa.authorizer.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.exception.AuthorizerConfigUnexpectedException;
import it.gov.pagopa.authorizer.model.AuthConfiguration;
import it.gov.pagopa.authorizer.service.impl.AuthorizerConfigClientRetryWrapperImpl;
import it.gov.pagopa.authorizer.util.MockHttpResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    private static final String DOMAIN = "domain";

    private final Logger logger = Logger.getLogger("example-test-logger");

    @Mock
    AuthorizerConfigClientRetryWrapperImpl authorizerConfigClientRetryWrapper;

    @SneakyThrows
    @ParameterizedTest
    @CsvSource(delimiterString = "/", value = {
            "0/{\"key\":\"domain_1\",\"value\":\"entity1#entity2#entity3\",\"metadata\":\"_o=not-visible-key:pagoPA;;\"}",
            "1/{\"key\":\"domain_1\",\"value\":\"entity1#entity2|sub-entity\",\"metadata\":\"_o=not-visible-key:pagoPA;;\"}",
            "3/{\"key\":\"domain_1\",\"value\":\"entity1#entity2#entity3\",\"metadata\":\"\"}"
    })
    void addAuthConfigurationToAPIMAuthorizer_OK(int id, String subkeyDomainAsString) {

        // Mocking passed values
        SubscriptionKeyDomain subkeyDomain = getSubscriptionKeyDomains().get(id);
        //String subkeyDomainAsString = "{\"key\":\"domain_1\",\"value\":\"entity1#entity2#entity3\",\"metadata\":\"_o=not-visible-key:pagoPA;;\"}";
        MockHttpResponse mockedHttpResponse = MockHttpResponse.builder().statusCode(200).uri(new URI("")).build();

        // Mocking execution for service's internal component
        CacheService cacheService = spy(new CacheService(logger, authorizerConfigClientRetryWrapper));
        doReturn(mockedHttpResponse).when(authorizerConfigClientRetryWrapper).refreshConfigurationWithRetry(any(AuthConfiguration.class), anyString(), anyBoolean());

        // Execute function
        cacheService.addAuthConfigurationToAPIMAuthorizer(subkeyDomain, false);

        // Checking assertions
        ArgumentCaptor<AuthConfiguration> requestCaptor = ArgumentCaptor.forClass(AuthConfiguration.class);
        verify(authorizerConfigClientRetryWrapper, times(1)).refreshConfigurationWithRetry(requestCaptor.capture(), anyString(), anyBoolean());
        assertEquals(subkeyDomainAsString, new ObjectMapper().writer().writeValueAsString(requestCaptor.getValue()));
    }

    @SneakyThrows
    @Test
    void addAuthConfigurationToAPIMAuthorizer_OK_noAuthorizationEntities() {

        // Mocking passed values
        SubscriptionKeyDomain subkeyDomain = getSubscriptionKeyDomains().get(0);
        subkeyDomain.setAuthorizedEntities(List.of());
        String subkeyDomainAsString = "{\"key\":\"domain_1\",\"value\":\"\",\"metadata\":\"_o=not-visible-key:pagoPA;;\"}";
        MockHttpResponse mockedHttpResponse = MockHttpResponse.builder().statusCode(200).uri(new URI("")).build();

        // Mocking execution for service's internal component
        CacheService cacheService = spy(new CacheService(logger, authorizerConfigClientRetryWrapper));
        doReturn(mockedHttpResponse).when(authorizerConfigClientRetryWrapper).refreshConfigurationWithRetry(any(AuthConfiguration.class), anyString(), anyBoolean());

        // Execute function
        cacheService.addAuthConfigurationToAPIMAuthorizer(subkeyDomain, false);

        // Checking assertions
        ArgumentCaptor<AuthConfiguration> requestCaptor = ArgumentCaptor.forClass(AuthConfiguration.class);
        verify(authorizerConfigClientRetryWrapper, times(1)).refreshConfigurationWithRetry(requestCaptor.capture(), anyString(), anyBoolean());
        assertEquals(subkeyDomainAsString, new ObjectMapper().writer().writeValueAsString(requestCaptor.getValue()));
    }

    @SneakyThrows
    @Test
    void addAuthConfigurationToAPIMAuthorizer_KO_communicationError() {

        // Mocking passed values
        SubscriptionKeyDomain subkeyDomain = getSubscriptionKeyDomains().get(0);
        String subkeyDomainAsString = "{\"key\":\"domain_1\",\"value\":\"entity1#entity2#entity3\",\"metadata\":\"_o=not-visible-key:pagoPA;;\"}";

        // Mocking execution for service's internal component
        AuthorizerConfigClientRetryWrapperImpl realAuthorizerConfigClientRetryWrapperImpl = spy(new AuthorizerConfigClientRetryWrapperImpl());
        CacheService cacheService = spy(new CacheService(logger, realAuthorizerConfigClientRetryWrapperImpl));

        // Execute function
        assertThrows(AuthorizerConfigUnexpectedException.class, () -> cacheService.addAuthConfigurationToAPIMAuthorizer(subkeyDomain, false));

        // Checking assertions
        ArgumentCaptor<AuthConfiguration> requestCaptor = ArgumentCaptor.forClass(AuthConfiguration.class);
        verify(realAuthorizerConfigClientRetryWrapperImpl, times(1)).refreshConfigurationWithRetry(requestCaptor.capture(), anyString(), anyBoolean());        assertEquals(subkeyDomainAsString, new ObjectMapper().writer().writeValueAsString(requestCaptor.getValue()));
        assertEquals(subkeyDomainAsString, new ObjectMapper().writer().writeValueAsString(requestCaptor.getValue()));
    }

    @SneakyThrows
    @Test
    void addAuthConfigurationToAPIMAuthorizer_KO_invalidParameter() {

        // Mocking execution for service's internal component
        CacheService cacheService = spy(new CacheService(logger, authorizerConfigClientRetryWrapper));

        // Execute function
        assertThrows(IllegalArgumentException.class, () -> cacheService.addAuthConfigurationToAPIMAuthorizer(null, true));

        // Checking assertions
        verify(authorizerConfigClientRetryWrapper, times(0)).refreshConfigurationWithRetry(any(AuthConfiguration.class), anyString(), anyBoolean());
    }

    private List<SubscriptionKeyDomain> getSubscriptionKeyDomains() {
        return List.of(
                SubscriptionKeyDomain.builder()
                        .id(UUID.randomUUID().toString())
                        .domain(DOMAIN)
                        .subkey("1")
                        .authorizedEntities(List.of(
                                AuthorizedEntity.builder().name("First entity").value("entity1").build(),
                                AuthorizedEntity.builder().name("Second entity").value("entity2").build(),
                                AuthorizedEntity.builder().name("Third entity").value("entity3").build()
                        ))
                        .otherMetadata(List.of(
                                Metadata.builder().name("owner").shortKey("_o").content(List.of(
                                        GenericPair.builder().key("not-visible-key").value("pagoPA").build()
                                )).build()
                        ))
                        .build(),
                SubscriptionKeyDomain.builder()
                        .id(UUID.randomUUID().toString())
                        .domain(DOMAIN)
                        .subkey("1")
                        .authorizedEntities(List.of(
                                AuthorizedEntity.builder().name("First entity").value("entity1").build(),
                                AuthorizedEntity.builder().name("Composite entity").values(List.of("entity2", "sub-entity")).build()
                        ))
                        .otherMetadata(List.of(
                                Metadata.builder().name("owner").shortKey("_o").content(List.of(
                                        GenericPair.builder().key("not-visible-key").value("pagoPA").build()
                                )).build()
                        ))
                        .build(),
                SubscriptionKeyDomain.builder()
                        .id(UUID.randomUUID().toString())
                        .domain(DOMAIN)
                        .subkey("1")
                        .authorizedEntities(List.of())
                        .otherMetadata(List.of())
                        .build(),
                SubscriptionKeyDomain.builder()
                        .id(UUID.randomUUID().toString())
                        .domain(DOMAIN)
                        .subkey("1")
                        .authorizedEntities(List.of(
                                AuthorizedEntity.builder().name("First entity").value("entity1").build(),
                                AuthorizedEntity.builder().name("Second entity").value("entity2").build(),
                                AuthorizedEntity.builder().name("Third entity").value("entity3").build()
                        ))
                        .otherMetadata(List.of())
                        .build()
        );
    }
}
