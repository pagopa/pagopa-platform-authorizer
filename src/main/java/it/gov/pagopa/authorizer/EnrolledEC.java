package it.gov.pagopa.authorizer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.annotation.*;
import it.gov.pagopa.authorizer.model.EnrolledCreditorInstitutions;
import it.gov.pagopa.authorizer.model.ProblemJson;
import it.gov.pagopa.authorizer.service.EnrollingService;
import it.gov.pagopa.authorizer.util.Constants;
import org.springframework.http.MediaType;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;


public class EnrolledEC {

    private final String apiconfigPath = System.getenv(Constants.APICONFIG_SELFCARE_INTEGRATION_PATH_PARAMETER);

    private final String apiconfigSubkey = System.getenv(Constants.APICONFIG_SELFCARE_INTEGRATION_SUBKEY_PARAMETER);

    @FunctionName("EnrolledECFunction")
    public HttpResponseMessage run (
            @HttpTrigger(
                    name = "EnrolledECTrigger",
                    methods = {HttpMethod.GET},
                    route = "/organizations/domains/{domain}",
                    authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            @CosmosDBInput(
                    name = "SkeydomainsInput",
                    databaseName = "authorizer",
                    collectionName = "skeydomains",
                    sqlQuery = "%EC_SQL_QUERY%",
                    connectionStringSetting = "COSMOS_CONN_STRING"
            ) String[] enrolledECsDomain,
            @BindingName("domain") String domain,
            final ExecutionContext context) throws InterruptedException {

    	Instant start = Instant.now();
    	
        Logger logger = context.getLogger();
        logger.log(Level.INFO, () -> String.format("Called endpoint [%s]: found [%d] element(s) related to the requested domain.", request.getUri().getPath(), enrolledECsDomain.length));
        HttpResponseMessage response;
        try {
            EnrollingService enrollingService = getEnrollingService(logger);
            EnrolledCreditorInstitutions result = enrollingService.getEnrolledCI(enrolledECsDomain, domain);
            response = request.createResponseBuilder(HttpStatus.OK)
                    .body(new ObjectMapper().writeValueAsString(result))
                    .header(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            logger.log(Level.FINE, () -> String.format("The execution will end with an HTTP status code %d and duration time %d ms", HttpStatus.OK.value(), Duration.between(start, Instant.now()).toMillis()));
        } catch (URISyntaxException | IOException e) {
            response = request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ProblemJson.builder().status(500).title("Communication error").detail("Error during communication with APIConfig for segregation codes retrieving.").build())
                    .header(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            logger.log(Level.SEVERE, "An error occurred while trying to calling APIConfig \"get segregation codes\" API. ", e);
        }  catch (IllegalArgumentException e) {
            response = request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(ProblemJson.builder().status(400).title("Invalid domain").detail(e.getMessage()).build())
                    .header(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            logger.log(Level.SEVERE, "An error occurred while get the service URL mapping from passed domain. ", e);
        }
        return response;
    }

    public EnrollingService getEnrollingService(Logger logger) {
        long start = Calendar.getInstance().getTimeInMillis();
        HttpClient httpClient = HttpClient.newHttpClient();
        logger.log(Level.INFO, () -> String.format("Generated a new stub for HTTP Client in [%d] ms", Calendar.getInstance().getTimeInMillis() - start));
        return new EnrollingService(logger, httpClient, apiconfigPath, apiconfigSubkey);
    }
}
