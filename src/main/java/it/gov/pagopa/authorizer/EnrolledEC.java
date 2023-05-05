package it.gov.pagopa.authorizer;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.CosmosDBInput;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;


public class EnrolledEC {

    @FunctionName("EnrolledECFunction")
    public HttpResponseMessage run (
            @HttpTrigger(
                    name = "EnrolledECTrigger",
                    methods = {HttpMethod.GET},
                    route = "api/ec/domain/{domain}",
                    authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            @CosmosDBInput(
                    name = "SkeydomainsInput",
                    databaseName = "authorizer",
                    collectionName = "skeydomains",
                    //sqlQuery = "SELECT VALUE c.authorization FROM c where c.domain = {domain}",
                    sqlQuery = "%EC_SQL_QUERY%",
                    connectionStringSetting = "COSMOS_CONN_STRING"
            ) String[] enrolledECsDomain,
            final ExecutionContext context) {

    	Instant start = Instant.now();
    	
        Logger logger = context.getLogger();
        logger.log(Level.INFO, () -> String.format("Called endpoint [%s]: found [%d] element(s) related to the requested domain.", request.getUri().getPath(), enrolledECsDomain.length));
        
        List<String> distinctResult = Arrays.asList(enrolledECsDomain).stream().distinct().collect(Collectors.toList());

        HttpResponseMessage response = request.createResponseBuilder(HttpStatus.OK)
        		.body(distinctResult)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        logger.log(Level.INFO, () -> String.format("The execution will end with an HTTP status code %d and duration time %d ms", HttpStatus.OK.value(), Duration.between(start, Instant.now()).toMillis()));
        return response;
    }
}
