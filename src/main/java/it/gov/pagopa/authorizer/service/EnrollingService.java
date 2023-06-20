package it.gov.pagopa.authorizer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.authorizer.model.CIAssociatedCode;
import it.gov.pagopa.authorizer.model.CIAssociatedCodeList;
import it.gov.pagopa.authorizer.model.EnrolledCreditorInstitution;
import it.gov.pagopa.authorizer.model.EnrolledCreditorInstitutions;
import it.gov.pagopa.authorizer.util.Constants;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class EnrollingService {

    private final String apiconfigSubkey;

    private final String apiconfigPath;

    private final Logger logger;

    private final HttpClient httpClient;



    public EnrollingService(Logger logger, HttpClient httpClient, String getSegregationCodesPath, String apiconfigSubkey) {
        this.logger = logger;
        this.httpClient = httpClient;
        this.apiconfigPath = getSegregationCodesPath;
        this.apiconfigSubkey = apiconfigSubkey;
    }

    public EnrolledCreditorInstitutions getEnrolledCI(String[] enrolledECsDomain, String domain) throws InterruptedException, IOException, URISyntaxException, IllegalArgumentException {
        List<EnrolledCreditorInstitution> enrolledCreditorInstitutions = new ArrayList<>();
        List<String> distinctEnrolledECs = Arrays.stream(enrolledECsDomain)
                .distinct()
                .filter(enrolledEC -> !Constants.WILDCARD_CHARACTER.equals(enrolledEC))
                .collect(Collectors.toList());

        ObjectMapper mapper = new ObjectMapper();
        String serviceUrl = Constants.DOMAIN_TO_SERVICE_URI_MAPPING.get(domain);
        if (serviceUrl == null) {
            throw new IllegalArgumentException(String.format("No valid service mapping for domain %s", domain));
        }
        String apiconfigRawPath = String.format(Constants.GET_SEGREGATIONCODE_PATH_TEMPLATE, this.apiconfigPath);
        apiconfigRawPath = apiconfigRawPath.replace("{service}", serviceUrl);

        for (String enrolledEC : distinctEnrolledECs) {
            // extracting path for API call
            this.logger.log(Level.INFO, () -> String.format("Analyzing creditor institution with fiscal code [%s]: check if is enrolled to the domain [%s]", enrolledEC, domain));
            String refactoredAuthorizerPath = apiconfigRawPath.replace("{creditorInstitutionCode}", enrolledEC);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(new URI(refactoredAuthorizerPath))
                    .version(HttpClient.Version.HTTP_2)
                    .header("Ocp-Apim-Subscription-Key", this.apiconfigSubkey)
                    .GET()
                    .build();
            HttpResponse<String> apiconfigResponse = this.httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            this.logger.log(Level.INFO, () -> String.format("Communication with APIConfig ended, returned HTTP status code [%s] and body [%s]", apiconfigResponse.statusCode(), apiconfigResponse.body()));
            // check if is retrieved a valid response and generate the list of codes
            if (apiconfigResponse.statusCode() == 200) {
                CIAssociatedCodeList ciAssociatedCodeList = mapper.readValue(apiconfigResponse.body(), CIAssociatedCodeList.class);
                List<CIAssociatedCode> usedCodes = ciAssociatedCodeList.getUsedCodes();
                if (!usedCodes.isEmpty()) {
                    enrolledCreditorInstitutions.add(
                            EnrolledCreditorInstitution.builder()
                                    .organizationFiscalCode(enrolledEC)
                                    .segregationCodes(usedCodes.stream()
                                            .map(CIAssociatedCode::getCode)
                                            .collect(Collectors.toList()))
                                    .build()
                    );
                }
                this.logger.log(Level.INFO, () -> String.format("Retrieved the following list of used segregation codes for creditor institution with fiscal code [%s]: [%s]", enrolledEC, apiconfigResponse.statusCode()));
            }
        }

        return EnrolledCreditorInstitutions.builder()
                .creditorInstitutions(enrolledCreditorInstitutions)
                .build();
    }
}
