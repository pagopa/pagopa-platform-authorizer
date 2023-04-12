package it.gov.pagopa.authorizer;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import jakarta.ws.rs.core.MediaType;

import java.util.Optional;
import java.util.logging.Logger;

public class CacheGenerator {




    @FunctionName("CacheGeneratorFunction")
    public HttpResponseMessage run (
            @HttpTrigger(
                    name = "CacheGeneratorTrigger",
                    methods = {HttpMethod.GET},
                    route = "/cache-generation/domains/{domain}",
                    authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            @BindingName("domain") String domain,
            final ExecutionContext context) {

        Logger logger = context.getLogger();

        // retrieve SubscriptionKeyDomain objects from DB by domain
        // for each retrieved object, create an object that contains domains_subkey and authorized entity and execute an HTTP POST call on APIM_REFRESH_CONFIGURATION_PATH

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", MediaType.TEXT_PLAIN)
                .body("")
                .build();
    }
}
