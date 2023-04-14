package it.gov.pagopa.authorizer;

import com.microsoft.azure.functions.*;
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
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        when(cacheService.addAuthConfigurationToAPIMAuthorizer(any())).thenReturn(mockedHttpResponse);

        // Generating request, mocking the field creation
        HttpRequestMessage<Optional<String>> request = mock(HttpRequestMessage.class);

        // Generate mock response mocking the field creation
        final HttpResponseMessage.Builder builder = mock(HttpResponseMessage.Builder.class);
        HttpResponseMessage responseMock = mock(HttpResponseMessage.class);
        doReturn(builder).when(request).createResponseBuilder(any(HttpStatus.class));
        doReturn(builder).when(builder).header(anyString(), anyString());
        doReturn(HttpStatus.valueOf(mockedHttpResponse.statusCode())).when(responseMock).getStatus();
        doReturn(responseMock).when(builder).build();

        // Execute function
        Optional<SubscriptionKeyDomain> subkeyDomain = Optional.of(SubscriptionKeyDomain.builder()
                .id("casual-uuid")
                .domain("domain")
                .subkey("subkey")
                .authorization(List.of("entity1", "entity2"))
                .build());
        HttpResponseMessage response = function.run(request, subkeyDomain, context);

        // Checking assertions
        assertEquals(HttpStatus.OK, response.getStatus());
    }
}
