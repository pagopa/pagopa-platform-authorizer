package it.gov.pagopa.authorizer;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import it.gov.pagopa.authorizer.entity.SubscriptionKeyDomain;
import it.gov.pagopa.authorizer.service.CacheService;
import it.gov.pagopa.authorizer.util.MockHttpResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheGeneratorFunctionTest {

    private static final String DOMAIN = "domain";

    @Spy
    CacheGenerator function;

    @Mock
    CacheService cacheService;

    @Mock
    ExecutionContext context;

    @SneakyThrows
    @Test
    void runOk() {

        // Mocking service creation
        Logger logger = Logger.getLogger("example-test-logger");
        SubscriptionKeyDomain[] retrievedSubscriptionKeyDomains = getSubscriptionKeyDomains();
        when(context.getLogger()).thenReturn(logger);
        when(function.getCacheService(logger)).thenReturn(cacheService);

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
        HttpResponseMessage response = function.run(request, retrievedSubscriptionKeyDomains, context);

        // Checking assertions
        assertEquals(HttpStatus.OK, response.getStatus());
    }

    private SubscriptionKeyDomain[] getSubscriptionKeyDomains() {
        return new SubscriptionKeyDomain[]{
                SubscriptionKeyDomain.builder()
                        .id(UUID.randomUUID().toString())
                        .domain(DOMAIN)
                        .subkey("1")
                        .authorization(List.of("entity1", "entity2", "entity3"))
                        .build(),
                SubscriptionKeyDomain.builder()
                        .id(UUID.randomUUID().toString())
                        .domain(DOMAIN)
                        .subkey("2")
                        .authorization(List.of("entity1", "entity4"))
                        .build()
        };
    }
}
