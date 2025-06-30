package it.gov.pagopa.authorizer.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import it.gov.pagopa.authorizer.client.AuthorizerConfigClient;
import it.gov.pagopa.authorizer.client.impl.AuthorizerConfigClientImpl;
import it.gov.pagopa.authorizer.entity.AuthorizedEntity;
import it.gov.pagopa.authorizer.entity.GenericPair;
import it.gov.pagopa.authorizer.entity.Metadata;
import it.gov.pagopa.authorizer.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.exception.AuthorizerConfigException;
import it.gov.pagopa.authorizer.exception.AuthorizerConfigUnexpectedException;
import it.gov.pagopa.authorizer.model.AuthConfiguration;
import it.gov.pagopa.authorizer.model.enumeration.ReasonErrorCode;
import it.gov.pagopa.authorizer.service.CacheService;
import it.gov.pagopa.authorizer.util.MockHttpResponse;
import it.gov.pagopa.authorizer.util.ResponseSubscriber;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizerConfigClientRetryWrapperImplTest {

    private static final String DOMAIN = "domain";

    @Mock
    AuthorizerConfigClientImpl authorizerConfigClient;


    @SneakyThrows
    @Test
    void refreshConfigurationWithRetry_OK() {

        // Mocking passed values
        SubscriptionKeyDomain subkeyDomain = getSubscriptionKeyDomains().get(0);
        subkeyDomain.setAuthorizedEntities(List.of());
        AuthConfiguration authConfiguration = getAuthConfiguration();
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(1)
                .intervalFunction(IntervalFunction.ofExponentialRandomBackoff(200L, 2.0, 0.6))
                .retryOnException(e -> (e instanceof AuthorizerConfigException authConfigException) && ReasonErrorCode.isNotAReasonErrorCode(authConfigException.getStatusCode()))
                .build();
        Retry retry = RetryRegistry.of(config).retry("authorizerConfigRetry");
        MockHttpResponse mockedHttpResponse = MockHttpResponse.builder().statusCode(200).uri(new URI("")).build();

        // Mocking execution for service's internal component
        AuthorizerConfigClientRetryWrapperImpl authorizerConfigClientRetryWrapper = new AuthorizerConfigClientRetryWrapperImpl(authorizerConfigClient, retry);
        doReturn(mockedHttpResponse).when(authorizerConfigClient).refreshConfiguration(any(AuthConfiguration.class), anyString(), anyBoolean());

        // Execute function
        HttpResponse response = authorizerConfigClientRetryWrapper.refreshConfigurationWithRetry(authConfiguration, DOMAIN, false);

        // Checking assertions
        assertEquals(mockedHttpResponse.statusCode(), response.statusCode());
        assertEquals(mockedHttpResponse.uri(), response.uri());
    }

    @SneakyThrows
    @Test
    void refreshConfigurationWithRetry_KO() {

        // Mocking passed values
        SubscriptionKeyDomain subkeyDomain = getSubscriptionKeyDomains().get(0);
        subkeyDomain.setAuthorizedEntities(List.of());
        AuthConfiguration authConfiguration = getAuthConfiguration();
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(1)
                .intervalFunction(IntervalFunction.ofExponentialRandomBackoff(200L, 2.0, 0.6))
                .retryOnException(e -> (e instanceof AuthorizerConfigException authConfigException) && ReasonErrorCode.isNotAReasonErrorCode(authConfigException.getStatusCode()))
                .build();
        Retry retry = RetryRegistry.of(config).retry("authorizerConfigRetry");
        AuthorizerConfigException mockAuthorizerConfigException = new AuthorizerConfigException("Test exception", 500);

        // Mocking execution for service's internal component
        AuthorizerConfigClientRetryWrapperImpl authorizerConfigClientRetryWrapper = new AuthorizerConfigClientRetryWrapperImpl(authorizerConfigClient, retry);
        doThrow(mockAuthorizerConfigException).when(authorizerConfigClient).refreshConfiguration(any(AuthConfiguration.class), anyString(), anyBoolean());

        // Checking assertions after function execute
        AuthorizerConfigException authorizerConfigException = assertThrows(AuthorizerConfigException.class,
                () -> authorizerConfigClientRetryWrapper.refreshConfigurationWithRetry(authConfiguration, DOMAIN, false)
        );
        assertEquals(mockAuthorizerConfigException.getStatusCode(), authorizerConfigException.getStatusCode());
        assertEquals(mockAuthorizerConfigException.getMessage(), authorizerConfigException.getMessage());
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

    private AuthConfiguration getAuthConfiguration() {
        return AuthConfiguration.builder()
                .key("domain_1")
                .value("test")
                .metadata("_o=not-visible-key:pagoPA;")
                .build();
    }
}
