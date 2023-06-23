package it.gov.pagopa.authorizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import it.gov.pagopa.authorizer.service.CacheService;
import it.gov.pagopa.authorizer.service.EnrollingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;

import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
class EnrolledEcTest {

    private static final String DOMAIN = "gpd";

    @Spy
    EnrolledEC function;

    @Mock
    EnrollingService enrollingService;

    @Mock
    ExecutionContext context;

    @SneakyThrows
    @Test
    void runOk() {

        // Mocking service creation
        Logger logger = Logger.getLogger("example-test-logger");
        when(context.getLogger()).thenReturn(logger);
        when(function.getEnrollingService(logger)).thenReturn(enrollingService);
        
        // Mocking Cosmos Db query result
        String[] enrolledECsDomain = this.getEnrolledEC();

        // Generating request, mocking the field creation
        HttpRequestMessage<Optional<String>> requestMock = mock(HttpRequestMessage.class);
        doReturn(new URI("organizations/domains/" + DOMAIN)).when(requestMock).getUri();
        
        // Generate mock response mocking the field creation
        final HttpResponseMessage.Builder builderMock = mock(HttpResponseMessage.Builder.class);
    	doReturn(builderMock).when(requestMock).createResponseBuilder(any(HttpStatus.class));
        doReturn(builderMock).when(builderMock).header(anyString(), anyString());
        doReturn(builderMock).when(builderMock).body(any());

        HttpResponseMessage responseMock = mock(HttpResponseMessage.class);
        doReturn(HttpStatus.OK).when(responseMock).getStatus();
        doReturn(Arrays.asList(enrolledECsDomain).stream().distinct().collect(Collectors.toList())).when(responseMock).getBody();
        doReturn(responseMock).when(builderMock).build();

        // Execute function
        HttpResponseMessage response = function.run(requestMock, enrolledECsDomain, DOMAIN, context);

        // Checking assertions
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getBody());
    }

    @SneakyThrows
    @Test
    void runConnectionKO() {

        // Mocking service creation
        Logger logger = Logger.getLogger("example-test-logger");
        when(context.getLogger()).thenReturn(logger);
        when(function.getEnrollingService(logger)).thenReturn(enrollingService);
        when(enrollingService.getEnrolledCI(any(), anyString())).thenThrow(IOException.class);

        // Mocking Cosmos Db query result
        String[] enrolledECsDomain = this.getEnrolledEC();

        // Generating request, mocking the field creation
        HttpRequestMessage<Optional<String>> requestMock = mock(HttpRequestMessage.class);
        doReturn(new URI("organizations/domains/" + DOMAIN)).when(requestMock).getUri();

        // Generate mock response mocking the field creation
        final HttpResponseMessage.Builder builderMock = mock(HttpResponseMessage.Builder.class);
        doReturn(builderMock).when(requestMock).createResponseBuilder(any(HttpStatus.class));
        doReturn(builderMock).when(builderMock).header(anyString(), anyString());
        doReturn(builderMock).when(builderMock).body(any());

        HttpResponseMessage responseMock = mock(HttpResponseMessage.class);
        doReturn(HttpStatus.INTERNAL_SERVER_ERROR).when(responseMock).getStatus();
        doReturn(Arrays.asList(enrolledECsDomain).stream().distinct().collect(Collectors.toList())).when(responseMock).getBody();
        doReturn(responseMock).when(builderMock).build();

        // Execute function
        HttpResponseMessage response = function.run(requestMock, enrolledECsDomain, DOMAIN, context);

        // Checking assertions
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
        assertNotNull(response.getBody());
    }

    @SneakyThrows
    @Test
    void runDomainKO() {

        // Mocking service creation
        Logger logger = Logger.getLogger("example-test-logger");
        when(context.getLogger()).thenReturn(logger);
        when(function.getEnrollingService(logger)).thenReturn(enrollingService);
        when(enrollingService.getEnrolledCI(any(), anyString())).thenThrow(IllegalArgumentException.class);

        // Mocking Cosmos Db query result
        String[] enrolledECsDomain = this.getEnrolledEC();

        // Generating request, mocking the field creation
        HttpRequestMessage<Optional<String>> requestMock = mock(HttpRequestMessage.class);
        doReturn(new URI("organizations/domains/" + DOMAIN)).when(requestMock).getUri();

        // Generate mock response mocking the field creation
        final HttpResponseMessage.Builder builderMock = mock(HttpResponseMessage.Builder.class);
        doReturn(builderMock).when(requestMock).createResponseBuilder(any(HttpStatus.class));
        doReturn(builderMock).when(builderMock).header(anyString(), anyString());
        doReturn(builderMock).when(builderMock).body(any());

        HttpResponseMessage responseMock = mock(HttpResponseMessage.class);
        doReturn(HttpStatus.BAD_REQUEST).when(responseMock).getStatus();
        doReturn(Arrays.asList(enrolledECsDomain).stream().distinct().collect(Collectors.toList())).when(responseMock).getBody();
        doReturn(responseMock).when(builderMock).build();

        // Execute function
        HttpResponseMessage response = function.run(requestMock, enrolledECsDomain, DOMAIN, context);

        // Checking assertions
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertNotNull(response.getBody());
    }
    
    private String[] getEnrolledEC() {
        return new String[] {"123456", "789012", "789012", "*", "345678" };
    }
}
