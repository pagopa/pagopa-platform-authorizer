package it.gov.pagopa.authorizer;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import it.gov.pagopa.authorizer.util.Constants;
import it.gov.pagopa.authorizer.util.MockHttpResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InfoFunctionTest {

    @Spy
    Info function;

    @Mock
    ExecutionContext context;

    @SneakyThrows
    @Test
    void runOk() {

        // Mocking service creation
        Logger logger = Logger.getLogger("example-test-logger");
        when(context.getLogger()).thenReturn(logger);

        // Mocking communication with APIM
        MockHttpResponse mockedHttpResponse = MockHttpResponse.builder().statusCode(200).uri(new URI("")).build();

        // Generating request, mocking the field creation
        HttpRequestMessage<Optional<String>> request = mock(HttpRequestMessage.class);

        // Generate mock response mocking the field creation
        final HttpResponseMessage.Builder builder = mock(HttpResponseMessage.Builder.class);
        HttpResponseMessage responseMock = mock(HttpResponseMessage.class);
        doReturn(builder).when(request).createResponseBuilder(any(HttpStatus.class));
        doReturn(builder).when(builder).header(anyString(), anyString());
        doReturn(HttpStatus.valueOf(mockedHttpResponse.statusCode())).when(responseMock).getStatus();
        doReturn(builder).when(builder).body(any());
        doReturn(responseMock).when(builder).build();

        // Execute function
        HttpResponseMessage response = function.run(request, context);

        // Checking assertions
        assertEquals(HttpStatus.OK, response.getStatus());
    }
}
