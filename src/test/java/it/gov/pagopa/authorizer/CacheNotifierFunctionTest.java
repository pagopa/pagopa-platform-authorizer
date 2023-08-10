package it.gov.pagopa.authorizer;

import com.microsoft.azure.functions.*;
import it.gov.pagopa.authorizer.entity.AuthorizedEntity;
import it.gov.pagopa.authorizer.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.service.CacheService;
import it.gov.pagopa.authorizer.util.MockHttpResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.List;
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
    ExecutionContext context;

    @SneakyThrows
    @Test
    void runOk() {

        // Mocking service creation
        Logger logger = Logger.getLogger("example-test-logger");
        when(context.getLogger()).thenReturn(logger);
        when(function.getCacheService(any())).thenReturn(cacheService);

        // Mocking communication with APIM
        MockHttpResponse mockedHttpResponse = MockHttpResponse.builder().statusCode(200).uri(new URI("")).build();
        when(cacheService.addAuthConfigurationToAPIMAuthorizer(any(), anyBoolean())).thenReturn(mockedHttpResponse);

        // Execute function
        SubscriptionKeyDomain subkeyDomain = SubscriptionKeyDomain.builder()
                .id("casual-uuid")
                .domain("domain")
                .subkey("subkey")
                .authorizedEntities(List.of(
                        AuthorizedEntity.builder().name("First entity").value("entity1").build(),
                        AuthorizedEntity.builder().name("Second entity").value("entity2").build()
                ))
                .build();
        function.run(List.of(subkeyDomain), context);

        // Checking assertions
        verify(cacheService, times(1)).addAuthConfigurationToAPIMAuthorizer(any(), anyBoolean());
    }

    @SneakyThrows
    @Test
    void runKO_nullTriggeredRow() {

        // Mocking service creation
        Logger logger = Logger.getLogger("example-test-logger");
        when(context.getLogger()).thenReturn(logger);

        // Execute function
        function.run(List.of(), context);

        // Checking assertions
        verify(cacheService, times(0)).addAuthConfigurationToAPIMAuthorizer(any(), anyBoolean());
    }

    @SneakyThrows
    @Test
    void runOk_interruptedCommunication() {

        // Mocking service creation
        Logger logger = Logger.getLogger("example-test-logger");
        when(context.getLogger()).thenReturn(logger);
        when(function.getCacheService(any())).thenReturn(cacheService);

        // Mocking communication with APIM
        when(cacheService.addAuthConfigurationToAPIMAuthorizer(any(), anyBoolean())).thenThrow(InterruptedException.class);

        // Execute function
        SubscriptionKeyDomain subkeyDomain = SubscriptionKeyDomain.builder()
                .id("casual-uuid")
                .domain("domain")
                .subkey("1")
                .authorizedEntities(List.of(
                        AuthorizedEntity.builder().name("First entity").value("entity1").build(),
                        AuthorizedEntity.builder().name("Second entity").value("entity2").build()
                ))
                .build();

        // Checking assertions
        assertThrows(InterruptedException.class, () -> function.run(List.of(subkeyDomain), context));
    }
}
