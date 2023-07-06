package it.gov.pagopa.authorizer;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import it.gov.pagopa.authorizer.model.AppInfo;
import it.gov.pagopa.authorizer.util.Constants;

import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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
                .body(getInfo(context.getLogger(), "/META-INF/maven/it.gov.pagopa.authorizer/platform-authorizer/pom.properties"))
                .build();
    }

    public synchronized AppInfo getInfo(Logger logger, String path) {
        String version = null;
        String name = null;
        try {
            Properties properties = new Properties();
            InputStream inputStream = getClass().getResourceAsStream(path);
            if (inputStream != null) {
                properties.load(inputStream);
                version = properties.getProperty("version", null);
                name = properties.getProperty("artifactId", null);
            }
        } catch (Exception e) {
            logger.severe("Impossible to retrieve information from pom.properties file.");
        }
        return AppInfo.builder().version(version).environment("azure-fn").name(name).build();
    }
}
