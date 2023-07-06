package it.gov.pagopa.authorizer;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import it.gov.pagopa.authorizer.model.AppInfo;
import it.gov.pagopa.authorizer.util.Constants;

import java.util.Optional;
import java.util.logging.Level;

public class Info {

    @FunctionName("AuthorizerInfoFunction")
    public HttpResponseMessage run (
            @HttpTrigger(
                    name = "InfoTrigger",
                    methods = {HttpMethod.GET},
                    route = "info",
                    authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().log(Level.INFO, "Invoked health check HTTP trigger for pagopa-platform-authorizer.");
        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(AppInfo.builder()
                        .name(System.getenv(Constants.INFO_NAME))
                        .environment(System.getenv(Constants.INFO_ENV))
                        .version(System.getenv(Constants.INFO_VERSION))
                        .build())
                .build();
    }
}
