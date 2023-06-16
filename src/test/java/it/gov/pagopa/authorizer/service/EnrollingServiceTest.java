package it.gov.pagopa.authorizer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.authorizer.model.EnrolledCreditorInstitutions;
import it.gov.pagopa.authorizer.util.MockHttpResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollingServiceTest {

    private static final String DOMAIN = "gpd";

    private static final String APICONFIG_PATH = "http://fake.apiconfig.path.org";

    private static final String APICONFIG_SUBKEY = "fakesubkey";

    private final Logger logger = Logger.getLogger("example-test-logger");

    @Mock
    HttpClient httpClient;

    ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    @Test
    void getEnrolledCI_allValid_OK() {

        // Mocking passed values
        String[] enrolledECs = new String[]{"123456", "789012"};
        MockHttpResponse mockedHttpResponse = MockHttpResponse.builder()
                .statusCode(200)
                .uri(new URI(""))
                .body(readJsonFromFile("request/apiconfig_getsegregationcodes_ok1.json"))
                .build();

        // Mocking execution for service's internal component
        EnrollingService enrollingService = spy(new EnrollingService(logger, httpClient, APICONFIG_PATH, APICONFIG_SUBKEY));
        doReturn(mockedHttpResponse).when(httpClient).send(any(), any());

        // Execute function
        EnrolledCreditorInstitutions result = enrollingService.getEnrolledCI(enrolledECs, DOMAIN);
        String actual = mapper.writeValueAsString(result);
        String expected = readJsonFromFile("response/enrolling_ok1.json");

        // Checking assertions
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(enrolledECs.length)).send(requestCaptor.capture(), any());
        assertEquals(mapper.readTree(expected), mapper.readTree(actual));
    }

    @SneakyThrows
    @Test
    void getEnrolledCI_excludeDuplicate_OK() {

        // Mocking passed values
        String[] enrolledECs = new String[]{"123456", "789012", "789012"};
        MockHttpResponse mockedHttpResponse = MockHttpResponse.builder()
                .statusCode(200)
                .uri(new URI(""))
                .body(readJsonFromFile("request/apiconfig_getsegregationcodes_ok1.json"))
                .build();

        // Mocking execution for service's internal component
        EnrollingService enrollingService = spy(new EnrollingService(logger, httpClient, APICONFIG_PATH, APICONFIG_SUBKEY));
        doReturn(mockedHttpResponse).when(httpClient).send(any(), any());

        // Execute function
        EnrolledCreditorInstitutions result = enrollingService.getEnrolledCI(enrolledECs, DOMAIN);
        String actual = mapper.writeValueAsString(result);
        String expected = readJsonFromFile("response/enrolling_ok1.json");

        // Checking assertions
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(Arrays.stream(enrolledECs).collect(Collectors.toSet()).size())).send(requestCaptor.capture(), any());
        assertEquals(mapper.readTree(expected), mapper.readTree(actual));
    }

    @SneakyThrows
    @Test
    void getEnrolledCI_excludeWildcard_OK() {

        // Mocking passed values
        String[] enrolledECs = new String[]{"123456", "789012", "*"};
        MockHttpResponse mockedHttpResponse = MockHttpResponse.builder()
                .statusCode(200)
                .uri(new URI(""))
                .body(readJsonFromFile("request/apiconfig_getsegregationcodes_ok1.json"))
                .build();

        // Mocking execution for service's internal component
        EnrollingService enrollingService = spy(new EnrollingService(logger, httpClient, APICONFIG_PATH, APICONFIG_SUBKEY));
        doReturn(mockedHttpResponse).when(httpClient).send(any(), any());

        // Execute function
        EnrolledCreditorInstitutions result = enrollingService.getEnrolledCI(enrolledECs, DOMAIN);
        String actual = mapper.writeValueAsString(result);
        String expected = readJsonFromFile("response/enrolling_ok1.json");

        // Checking assertions
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(Arrays.stream(enrolledECs).filter(ec -> !"*".equals(ec)).collect(Collectors.toSet()).size())).send(requestCaptor.capture(), any());
        assertEquals(mapper.readTree(expected), mapper.readTree(actual));
    }

    @SneakyThrows
    @Test
    void getEnrolledCI_invalidCI_OK() {

        // Mocking passed values
        String[] enrolledECs = new String[]{"notfound"};
        MockHttpResponse mockedHttpResponse = MockHttpResponse.builder()
                .statusCode(404)
                .uri(new URI(""))
                .build();

        // Mocking execution for service's internal component
        EnrollingService enrollingService = spy(new EnrollingService(logger, httpClient, APICONFIG_PATH, APICONFIG_SUBKEY));
        doReturn(mockedHttpResponse).when(httpClient).send(any(), any());

        // Execute function
        EnrolledCreditorInstitutions result = enrollingService.getEnrolledCI(enrolledECs, DOMAIN);
        String actual = mapper.writeValueAsString(result);
        String expected = readJsonFromFile("response/enrolling_ok2.json");

        // Checking assertions
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(Arrays.stream(enrolledECs).collect(Collectors.toSet()).size())).send(requestCaptor.capture(), any());
        assertEquals(mapper.readTree(expected), mapper.readTree(actual));
    }

    @SneakyThrows
    @Test
    void getEnrolledCI_noCIFound_OK() {

        // Mocking passed values
        String[] enrolledECs = new String[]{};

        // Mocking execution for service's internal component
        EnrollingService enrollingService = spy(new EnrollingService(logger, httpClient, APICONFIG_PATH, APICONFIG_SUBKEY));

        // Execute function
        EnrolledCreditorInstitutions result = enrollingService.getEnrolledCI(enrolledECs, DOMAIN);
        String actual = mapper.writeValueAsString(result);
        String expected = readJsonFromFile("response/enrolling_ok2.json");

        // Checking assertions
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(0)).send(requestCaptor.capture(), any());
        assertEquals(mapper.readTree(expected), mapper.readTree(actual));
    }

    @SneakyThrows
    @Test
    void getEnrolledCI_communicationError_KO() {

        // Mocking passed values
        String[] enrolledECs = new String[]{"123456", "789012"};

        // Mocking execution for service's internal component
        HttpClient realHttpClient = spy(HttpClient.newHttpClient());
        EnrollingService realEnrollingService = spy(new EnrollingService(logger, realHttpClient, "https://api.ENV.pagopa.it", APICONFIG_SUBKEY));

        // Execute function
        assertThrows(IOException.class, () -> realEnrollingService.getEnrolledCI(enrolledECs, DOMAIN));

        // Checking assertions
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(0)).send(requestCaptor.capture(), any());
    }

    @SneakyThrows
    @Test
    void getEnrolledCI_notRegisteredDomain_KO() {

        // Mocking passed values
        String[] enrolledECs = new String[]{"123456", "789012"};

        // Mocking execution for service's internal component
        HttpClient realHttpClient = spy(HttpClient.newHttpClient());
        EnrollingService realEnrollingService = spy(new EnrollingService(logger, realHttpClient, "https://api.ENV.pagopa.it", APICONFIG_SUBKEY));

        // Execute function
        assertThrows(IllegalArgumentException.class, () -> realEnrollingService.getEnrolledCI(enrolledECs, "nonexistingdomain"));

        // Checking assertions
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(0)).send(requestCaptor.capture(), any());
    }

    public String readJsonFromFile(String relativePath) throws IOException {
        ClassLoader classLoader = EnrollingService.class.getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(relativePath)).getPath());
        return Files.readString(file.toPath());
    }
}
