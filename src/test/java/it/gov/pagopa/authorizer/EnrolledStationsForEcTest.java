package it.gov.pagopa.authorizer;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import it.gov.pagopa.authorizer.model.EnrolledCreditorInstitutionStation;
import it.gov.pagopa.authorizer.model.EnrolledCreditorInstitutionStations;
import it.gov.pagopa.authorizer.service.EnrollingService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrolledStationsForEcTest {

    private static final String DOMAIN = "gpd";

    private static final String CREDITOR_INSTITUTION = "77777777777";

    @Spy
    EnrolledStationsForEC function;

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
        when(enrollingService.getStationForEC(anyString(), anyString())).thenReturn(
                EnrolledCreditorInstitutionStations.builder()
                        .stations(List.of(EnrolledCreditorInstitutionStation.builder().stationId("00001").segregationCode("01").build()))
                        .build()
        );
        
        // Mocking Cosmos Db query result
        Integer[] countOfEnrolledEC = this.getCountOfEnrolledEC();

        // Generating request, mocking the field creation
        HttpRequestMessage<Optional<String>> requestMock = mock(HttpRequestMessage.class);
        doReturn(new URI(String.format("organizations/%s/domains/%s", CREDITOR_INSTITUTION, DOMAIN))).when(requestMock).getUri();

        // Generate mock response mocking the field creation
        final HttpResponseMessage.Builder builderMock = mock(HttpResponseMessage.Builder.class);
    	doReturn(builderMock).when(requestMock).createResponseBuilder(any(HttpStatus.class));
        doReturn(builderMock).when(builderMock).header(anyString(), anyString());
        doReturn(builderMock).when(builderMock).body(any());

        HttpResponseMessage responseMock = mock(HttpResponseMessage.class);
        doReturn(HttpStatus.OK).when(responseMock).getStatus();
        doReturn(Arrays.asList(countOfEnrolledEC)).when(responseMock).getBody();
        doReturn(responseMock).when(builderMock).build();

        // Execute function
        HttpResponseMessage response = function.run(requestMock, countOfEnrolledEC, CREDITOR_INSTITUTION, DOMAIN, context);

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
        when(enrollingService.getStationForEC(anyString(), anyString())).thenThrow(IOException.class);

        // Mocking Cosmos Db query result
        Integer[] countOfEnrolledEC = this.getCountOfEnrolledEC();

        // Generating request, mocking the field creation
        HttpRequestMessage<Optional<String>> requestMock = mock(HttpRequestMessage.class);
        doReturn(new URI(String.format("organizations/%s/domains/%s", CREDITOR_INSTITUTION, DOMAIN))).when(requestMock).getUri();

        // Generate mock response mocking the field creation
        final HttpResponseMessage.Builder builderMock = mock(HttpResponseMessage.Builder.class);
        doReturn(builderMock).when(requestMock).createResponseBuilder(any(HttpStatus.class));
        doReturn(builderMock).when(builderMock).header(anyString(), anyString());
        doReturn(builderMock).when(builderMock).body(any());

        HttpResponseMessage responseMock = mock(HttpResponseMessage.class);
        doReturn(HttpStatus.INTERNAL_SERVER_ERROR).when(responseMock).getStatus();
        doReturn(Arrays.asList(countOfEnrolledEC)).when(responseMock).getBody();
        doReturn(responseMock).when(builderMock).build();

        // Execute function
        HttpResponseMessage response = function.run(requestMock, countOfEnrolledEC, CREDITOR_INSTITUTION, DOMAIN, context);

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
        when(enrollingService.getStationForEC(anyString(), anyString())).thenThrow(IllegalArgumentException.class);

        // Mocking Cosmos Db query result
        Integer[] countOfEnrolledEC = this.getCountOfEnrolledEC();

        // Generating request, mocking the field creation
        HttpRequestMessage<Optional<String>> requestMock = mock(HttpRequestMessage.class);
        doReturn(new URI(String.format("organizations/%s/domains/%s", CREDITOR_INSTITUTION, DOMAIN))).when(requestMock).getUri();

        // Generate mock response mocking the field creation
        final HttpResponseMessage.Builder builderMock = mock(HttpResponseMessage.Builder.class);
        doReturn(builderMock).when(requestMock).createResponseBuilder(any(HttpStatus.class));
        doReturn(builderMock).when(builderMock).header(anyString(), anyString());
        doReturn(builderMock).when(builderMock).body(any());

        HttpResponseMessage responseMock = mock(HttpResponseMessage.class);
        doReturn(HttpStatus.BAD_REQUEST).when(responseMock).getStatus();
        doReturn(Arrays.asList(countOfEnrolledEC)).when(responseMock).getBody();
        doReturn(responseMock).when(builderMock).build();

        // Execute function
        HttpResponseMessage response = function.run(requestMock, countOfEnrolledEC, CREDITOR_INSTITUTION, DOMAIN, context);

        // Checking assertions
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertNotNull(response.getBody());
    }


    @SneakyThrows
    @Test
    void runNotFound() {

        // Mocking service creation
        Logger logger = Logger.getLogger("example-test-logger");
        when(context.getLogger()).thenReturn(logger);

        // Mocking Cosmos Db query result
        Integer[] countOfEnrolledEC = this.getCountOfNotEnrolledEC();

        // Generating request, mocking the field creation
        HttpRequestMessage<Optional<String>> requestMock = mock(HttpRequestMessage.class);
        doReturn(new URI(String.format("organizations/%s/domains/%s", CREDITOR_INSTITUTION, DOMAIN))).when(requestMock).getUri();

        // Generate mock response mocking the field creation
        final HttpResponseMessage.Builder builderMock = mock(HttpResponseMessage.Builder.class);
        doReturn(builderMock).when(requestMock).createResponseBuilder(any(HttpStatus.class));
        doReturn(builderMock).when(builderMock).header(anyString(), anyString());
        doReturn(builderMock).when(builderMock).body(any());

        HttpResponseMessage responseMock = mock(HttpResponseMessage.class);
        doReturn(HttpStatus.NOT_FOUND).when(responseMock).getStatus();
        doReturn(Arrays.asList(countOfEnrolledEC)).when(responseMock).getBody();
        doReturn(responseMock).when(builderMock).build();

        // Execute function
        HttpResponseMessage response = function.run(requestMock, countOfEnrolledEC, CREDITOR_INSTITUTION, DOMAIN, context);

        // Checking assertions
        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertNotNull(response.getBody());
    }

    @SneakyThrows
    @Test
    void runNotFoundEmptyList() {

        // Mocking service creation
        Logger logger = Logger.getLogger("example-test-logger");
        when(context.getLogger()).thenReturn(logger);
        when(function.getEnrollingService(logger)).thenReturn(enrollingService);
        when(enrollingService.getStationForEC(anyString(), anyString())).thenReturn(
                EnrolledCreditorInstitutionStations.builder()
                        .stations(List.of())
                        .build()
        );

        // Mocking Cosmos Db query result
        Integer[] countOfEnrolledEC = this.getCountOfEnrolledEC();

        // Generating request, mocking the field creation
        HttpRequestMessage<Optional<String>> requestMock = mock(HttpRequestMessage.class);
        doReturn(new URI(String.format("organizations/%s/domains/%s", CREDITOR_INSTITUTION, DOMAIN))).when(requestMock).getUri();

        // Generate mock response mocking the field creation
        final HttpResponseMessage.Builder builderMock = mock(HttpResponseMessage.Builder.class);
        doReturn(builderMock).when(requestMock).createResponseBuilder(any(HttpStatus.class));
        doReturn(builderMock).when(builderMock).header(anyString(), anyString());
        doReturn(builderMock).when(builderMock).body(any());

        HttpResponseMessage responseMock = mock(HttpResponseMessage.class);
        doReturn(HttpStatus.NOT_FOUND).when(responseMock).getStatus();
        doReturn(Arrays.asList(countOfEnrolledEC)).when(responseMock).getBody();
        doReturn(responseMock).when(builderMock).build();

        // Execute function
        HttpResponseMessage response = function.run(requestMock, countOfEnrolledEC, CREDITOR_INSTITUTION, DOMAIN, context);

        // Checking assertions
        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertNotNull(response.getBody());
    }
    
    private Integer[] getCountOfEnrolledEC() {
        return new Integer[] { 1 };
    }

    private Integer[] getCountOfNotEnrolledEC() {
        return new Integer[] { 0 };
    }
}
