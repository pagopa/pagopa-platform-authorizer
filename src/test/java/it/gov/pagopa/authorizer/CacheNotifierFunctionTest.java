package it.gov.pagopa.authorizer;

import com.microsoft.azure.functions.*;
import it.gov.pagopa.authorizer.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.service.CacheService;
import it.gov.pagopa.authorizer.service.DataAccessObject;
import it.gov.pagopa.authorizer.util.MockHttpResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheNotifierFunctionTest {

    @Spy
    CacheNotifier function;

    @Mock
    CacheService cacheService;

    @Mock
    DataAccessObject dao;

    @Mock
    ExecutionContext context;

    @SneakyThrows
    @Test
    void runOk() {

        // Mocking service creation
        Logger logger = Logger.getLogger("example-test-logger");
        when(context.getLogger()).thenReturn(logger);
        doReturn(dao).when(function).getDAO(any(), any(), any(), any());
        when(function.getCacheService(any())).thenReturn(cacheService);

        // Mocking communication with APIM
        MockHttpResponse mockedHttpResponse = MockHttpResponse.builder().statusCode(200).uri(new URI("")).build();
        when(cacheService.addAuthConfigurationToAPIMAuthorizer(any(), anyBoolean())).thenReturn(mockedHttpResponse);

        // Execute function
        Optional<SubscriptionKeyDomain> subkeyDomain = Optional.of(SubscriptionKeyDomain.builder()
                .id("casual-uuid")
                .domain("domain")
                .subkey("subkey")
                .authorization(List.of("entity1", "entity2"))
                .build());
        function.run(subkeyDomain, context);

        // Checking assertions
        verify(cacheService, times(1)).addAuthConfigurationToAPIMAuthorizer(any(), anyBoolean());
    }

    @SneakyThrows
    @Test
    void runKO_nullTriggeredRow() {

        // Mocking service creation
        Logger logger = Logger.getLogger("example-test-logger");
        when(context.getLogger()).thenReturn(logger);

        // Mocking communication with APIM
        MockHttpResponse mockedHttpResponse = MockHttpResponse.builder().statusCode(500).uri(new URI("")).build();

        // Execute function
        Optional<SubscriptionKeyDomain> subkeyDomain = spy(Optional.empty());
        function.run(subkeyDomain, context);

        // Checking assertions
        verify(cacheService, times(0)).addAuthConfigurationToAPIMAuthorizer(any(), anyBoolean());
    }

    @SneakyThrows
    @Test
    void runOk_interruptedCommunication() {

        // Mocking service creation
        Logger logger = Logger.getLogger("example-test-logger");
        when(context.getLogger()).thenReturn(logger);
        doReturn(dao).when(function).getDAO(any(), any(), any(), any());
        when(function.getCacheService(any())).thenReturn(cacheService);

        // Mocking communication with APIM
        when(cacheService.addAuthConfigurationToAPIMAuthorizer(any(), anyBoolean())).thenThrow(InterruptedException.class);

        // Generating request, mocking the field creation
        HttpRequestMessage<Optional<String>> request = mock(HttpRequestMessage.class);

        // Generate mock response mocking the field creation
        final HttpResponseMessage.Builder builder = mock(HttpResponseMessage.Builder.class);

        // Execute function
        Optional<SubscriptionKeyDomain> subkeyDomain = Optional.of(SubscriptionKeyDomain.builder()
                .id("casual-uuid")
                .domain("domain")
                .subkey("subkey")
                .authorization(List.of("entity1", "entity2"))
                .build());

        // Checking assertions
        assertThrows(InterruptedException.class, () -> function.run(subkeyDomain, context));
    }
}
