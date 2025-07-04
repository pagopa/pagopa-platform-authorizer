package it.gov.pagopa.authorizer.client.impl;

import it.gov.pagopa.authorizer.exception.AuthorizerConfigException;
import it.gov.pagopa.authorizer.model.AuthConfiguration;
import it.gov.pagopa.authorizer.util.MockHttpResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.org.webcompere.systemstubs.SystemStubs.withEnvironmentVariables;

@ExtendWith(MockitoExtension.class)
class AuthorizerConfigClientImplTest {

    private static final String DOMAIN = "domain";

    @Mock
    HttpClient httpClient;


    @SneakyThrows
    @Test
    void refreshConfiguration_OK() {

        // Mocking passed values
        AuthConfiguration authConfiguration = getAuthConfiguration();
        MockHttpResponse mockedHttpResponse = MockHttpResponse.builder().statusCode(200).uri(new URI("")).build();

        // Mocking execution for service's internal component
        doReturn(mockedHttpResponse).when(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

        // Execute function
        withEnvironmentVariables(
                "REFRESH_CONFIGURATION_PATH", "http://test:8080"
        ).execute(
                () -> {
                    AuthorizerConfigClientImpl authorizerConfigClient = new AuthorizerConfigClientImpl(httpClient);
                    HttpResponse response = authorizerConfigClient.refreshConfiguration(authConfiguration, DOMAIN, false);
                    // Checking assertions
                    assertEquals(mockedHttpResponse.statusCode(), response.statusCode());
                    assertEquals(mockedHttpResponse.uri(), response.uri());
                }
        );
    }

    @SneakyThrows
    @Test
    void refreshConfiguration_KO() {

        // Mocking passed values
        AuthConfiguration authConfiguration = getAuthConfiguration();
        MockHttpResponse mockedHttpResponse = MockHttpResponse.builder().statusCode(500).uri(new URI("")).build();

        // Mocking execution for service's internal component
        doReturn(mockedHttpResponse).when(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

        // Execute function
        withEnvironmentVariables(
                "REFRESH_CONFIGURATION_PATH", "http://test:8080"
        ).execute(
                () -> {
                    AuthorizerConfigClientImpl authorizerConfigClient = new AuthorizerConfigClientImpl(httpClient);
                    AuthorizerConfigException authorizerConfigException = assertThrows(AuthorizerConfigException.class,
                            () -> authorizerConfigClient.refreshConfiguration(authConfiguration, DOMAIN, false)
                    );
                    // Checking assertions
                    assertEquals(500, authorizerConfigException.getStatusCode());
                    assertEquals("Error invoking refresh configuration API, response status code 500", authorizerConfigException.getMessage());
                }
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
