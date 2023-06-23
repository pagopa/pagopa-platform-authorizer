package it.gov.pagopa.authorizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import it.gov.pagopa.authorizer.model.EnrolledCreditorInstitutionStations;
import it.gov.pagopa.authorizer.model.EnrolledCreditorInstitutions;
import it.gov.pagopa.authorizer.model.ProblemJson;
import it.gov.pagopa.authorizer.service.EnrollingService;
import it.gov.pagopa.authorizer.util.Constants;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


public class EnrolledStationsForEC {

    private final String apiconfigPath = System.getenv(Constants.APICONFIG_SELFCARE_INTEGRATION_PATH_PARAMETER);

    private final String apiconfigSubkey = System.getenv(Constants.APICONFIG_SELFCARE_INTEGRATION_SUBKEY_PARAMETER);

    @FunctionName("EnrolledStationsForECFunction")
    public HttpResponseMessage run (
            @HttpTrigger(
                    name = "EnrolledStationsForECTrigger",
                    methods = {HttpMethod.GET},
                    route = "/organizations/{organizationFiscalCode}/domains/{domain}",
                    authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            @CosmosDBInput(
                    name = "SkeydomainsInput",
                    databaseName = "authorizer",
                    collectionName = "skeydomains",
                    sqlQuery = "%IS_EC_ENROLLED_SQL_QUERY%",
                    connectionStringSetting = "COSMOS_CONN_STRING"
            ) Integer[] enrolledCICount,
            @BindingName("organizationFiscalCode") String organizationFiscalCode,
            @BindingName("domain") String domain,
            final ExecutionContext context) throws InterruptedException {

        Object body;
        HttpStatus status = HttpStatus.OK;

    	Instant start = Instant.now();

        boolean isECEnrolled = enrolledCICount[0] != 0;
        Logger logger = context.getLogger();
        logger.log(Level.INFO, () -> String.format("Called endpoint [%s]: Is CI enrolled? [%s].", request.getUri().getPath(), isECEnrolled));
        HttpResponseMessage response;
        try {
            if (!isECEnrolled) {
                status = HttpStatus.NOT_FOUND;
                body = ProblemJson.builder()
                        .status(status.value())
                        .title("Invalid creditor institution")
                        .detail(String.format("No enrolled creditor institution is found with fiscal code %s.", organizationFiscalCode))
                        .build();
            } else {
                EnrollingService enrollingService = getEnrollingService(logger);
                EnrolledCreditorInstitutionStations result = enrollingService.getStationForEC(organizationFiscalCode, domain);
                body = new ObjectMapper().writeValueAsString(result);
            }
        } catch (URISyntaxException | IOException e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            body = ProblemJson.builder()
                    .status(status.value())
                    .title("Communication error")
                    .detail("Error during communication with APIConfig for segregation codes retrieving.")
                    .build();
            logger.log(Level.SEVERE, "An error occurred while trying to calling APIConfig \"get segregation codes\" API. ", e);
        }  catch (IllegalArgumentException e) {
            status = HttpStatus.BAD_REQUEST;
            body = ProblemJson.builder()
                    .status(status.value())
                    .title("Invalid parameters")
                    .detail(e.getMessage()).build();
            logger.log(Level.SEVERE, "An error occurred while analyzing CI fiscal code or while get the service URL mapping from passed domain. ", e);
        }
        response = request.createResponseBuilder(status)
                .body(body)
                .header(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        logger.log(Level.FINE, () -> String.format("The execution will end with an HTTP status code %d and duration time %d ms", HttpStatus.OK.value(), Duration.between(start, Instant.now()).toMillis()));
        return response;
    }

    public EnrollingService getEnrollingService(Logger logger) {
        long start = Calendar.getInstance().getTimeInMillis();
        HttpClient httpClient = HttpClient.newHttpClient();
        logger.log(Level.INFO, () -> String.format("Generated a new stub for HTTP Client in [%d] ms", Calendar.getInstance().getTimeInMillis() - start));
        return new EnrollingService(logger, httpClient, apiconfigPath, apiconfigSubkey);
    }
}
