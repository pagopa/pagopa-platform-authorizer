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
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class EnrollingService {

    private final String apiconfigPath;

    private final Logger logger;

    private final HttpClient httpClient;



    public EnrollingService(Logger logger, HttpClient httpClient, String getSegregationCodesPath) {
        this.logger = logger;
        this.httpClient = httpClient;
        this.apiconfigPath = getSegregationCodesPath;
    }

    public EnrolledCreditorInstitutions getEnrolledCI(String[] enrolledECsDomain) throws InterruptedException, IOException, URISyntaxException {
        List<EnrolledCreditorInstitution> enrolledCreditorInstitutions = new ArrayList<>();
        List<String> distinctEnrolledECs = Arrays.stream(enrolledECsDomain)
                .distinct()
                .filter(enrolledEC -> !Constants.WILDCARD_CHARACTER.equals(enrolledEC))
                .collect(Collectors.toList());

        ObjectMapper mapper = new ObjectMapper();
        String apiconfigRawPath = String.format(Constants.GET_SEGREGATIONCODE_PATH_TEMPLATE, this.apiconfigPath);

        for (String enrolledEC : distinctEnrolledECs) {

            String refactoredAuthorizerPath = apiconfigRawPath.replace("{creditorInstitutionCode}", enrolledEC);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(new URI(refactoredAuthorizerPath))
                    .version(HttpClient.Version.HTTP_2)
                    .GET()
                    .build();
            HttpResponse<String> apiconfigResponse = this.httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (apiconfigResponse.statusCode() == 200) {
                CIAssociatedCodeList ciAssociatedCodeList = mapper.readValue(apiconfigResponse.body(), CIAssociatedCodeList.class);
                enrolledCreditorInstitutions.add(
                        EnrolledCreditorInstitution.builder()
                                .organizationFiscalCode(enrolledEC)
                                .segregationCodes(ciAssociatedCodeList.getUsedCodes().stream()
                                        .map(CIAssociatedCode::getCode)
                                        .collect(Collectors.toList()))
                                .build()
                );
            }
        }

        return EnrolledCreditorInstitutions.builder()
                .creditorInstitutions(enrolledCreditorInstitutions)
                .build();
    }
}
