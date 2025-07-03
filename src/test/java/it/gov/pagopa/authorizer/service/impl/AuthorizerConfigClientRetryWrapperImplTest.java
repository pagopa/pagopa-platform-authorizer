package it.gov.pagopa.authorizer.service.impl;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import it.gov.pagopa.authorizer.client.impl.AuthorizerConfigClientImpl;
import it.gov.pagopa.authorizer.exception.AuthorizerConfigException;
import it.gov.pagopa.authorizer.model.AuthConfiguration;
import it.gov.pagopa.authorizer.model.enumeration.ReasonErrorCode;
import it.gov.pagopa.authorizer.util.MockHttpResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.http.HttpResponse;

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

    private AuthConfiguration getAuthConfiguration() {
        return AuthConfiguration.builder()
                .key("domain_1")
                .value("test")
                .metadata("_o=not-visible-key:pagoPA;")
                .build();
    }
}
