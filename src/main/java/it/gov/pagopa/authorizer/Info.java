package it.gov.pagopa.authorizer;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;
import java.util.logging.Level;


/**
 * Azure Functions with Azure Http trigger.
 */
public class Info {

    /**
     * This function will be invoked when a Http Trigger occurs
     * @return
     */
    @FunctionName("Info")
    public HttpResponseMessage run (
            @HttpTrigger(name = "PlatformAuthInfoTrigger",
                    methods = {HttpMethod.GET},
                    route = "info",
                    authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().log(Level.INFO,"Triggered Info API on platform-authorizer");
        return request.createResponseBuilder(HttpStatus.OK).build();
    }


}
