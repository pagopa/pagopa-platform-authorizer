package it.gov.pagopa.authorizer;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import it.gov.pagopa.authorizer.service.CacheService;
import it.gov.pagopa.authorizer.util.Constants;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class EnrolledEC {

    private final String authorizerPath = System.getenv(Constants.REFRESH_CONFIG_PATH_PARAMETER);

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
                    sqlQuery = "SELECT VALUE i FROM c JOIN i IN c.authorization WHERE c.domain = {domain}",
                    connectionStringSetting = "COSMOS_CONN_STRING"
            ) String[] enrolledECsDomain,
            final ExecutionContext context) throws InterruptedException {

        Logger logger = context.getLogger();
        logger.log(Level.INFO, () -> String.format("Called endpoint [%s]: found [%d] element(s) related to the requested domain.", request.getUri().getPath(), enrolledECsDomain.length));
        
        List<String> distinctResult = Arrays.asList(enrolledECsDomain).stream().distinct().collect(Collectors.toList());
        
        CacheService cacheService = getCacheService(logger);
        for (String ec : enrolledECsDomain) {
        	// TODO: Capire come deve essere memorizzata in cache l'informazione
            //HttpResponse<String> response = cacheService.addAuthConfigurationToAPIMAuthorizer(subkeyDomain, false);
            //final int statusCode = response != null ? response.statusCode() : 500;
            //logger.log(Level.INFO, () -> String.format("Requested configuration to APIM for subscription key domain with id [%s]. Response status: %d", subkeyDomain.getId(), statusCode));
        }

        HttpResponseMessage response = request.createResponseBuilder(HttpStatus.OK)
        		.body(distinctResult)
                .header("Content-Type", "application/json")
                .build();
        logger.log(Level.INFO, () -> String.format("The execution will end with an HTTP status code %d", HttpStatus.OK.value()));
        return response;
    }

    public CacheService getCacheService(Logger logger) {
        long start = Calendar.getInstance().getTimeInMillis();
        HttpClient httpClient = HttpClient.newHttpClient();
        logger.log(Level.INFO, () -> String.format("Generated a new stub for HTTP Client in [%d] ms", Calendar.getInstance().getTimeInMillis() - start));
        return new CacheService(logger, httpClient, authorizerPath);
    }
}
