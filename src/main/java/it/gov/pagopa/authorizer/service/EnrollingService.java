package it.gov.pagopa.authorizer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.authorizer.model.*;
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
        String apiconfigRawPath = getRawPathFromDomain(domain);

        for (String enrolledEC : distinctEnrolledECs) {
            // extracting path for API call
            this.logger.log(Level.INFO, () -> String.format("Analyzing creditor institution with fiscal code [%s]: check if is enrolled to the domain [%s]", enrolledEC, domain));
            HttpResponse<String> apiconfigResponse = executeCallToGetSegregationCodes(enrolledEC, apiconfigRawPath);
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
                this.logger.log(Level.INFO, () -> String.format("Retrieved the following list of used segregation codes for creditor institution with fiscal code [%s]: [%s]", enrolledEC, enrolledCreditorInstitutions));
            }
        }

        return EnrolledCreditorInstitutions.builder()
                .creditorInstitutions(enrolledCreditorInstitutions)
                .build();
    }

    public EnrolledCreditorInstitutionStations getStationForEC(String organizationFiscalCode, String domain)  throws InterruptedException, IOException, URISyntaxException, IllegalArgumentException {
        List<EnrolledCreditorInstitutionStation> enrolledCreditorInstitutionStations = new ArrayList<>();
        if (Constants.WILDCARD_CHARACTER.equals(organizationFiscalCode)) {
            throw new IllegalArgumentException("Impossible to get stations for creditor institution if wildcard is passed.");
        }

        ObjectMapper mapper = new ObjectMapper();
        String apiconfigRawPath = getRawPathFromDomain(domain);

        this.logger.log(Level.INFO, () -> String.format("Analyzing creditor institution with fiscal code [%s]: check if is enrolled to the domain [%s]", organizationFiscalCode, domain));
        HttpResponse<String> apiconfigResponse = executeCallToGetSegregationCodes(organizationFiscalCode, apiconfigRawPath);
        // check if is retrieved a valid response and generate the list of objects from codes
        if (apiconfigResponse.statusCode() == 200) {
            CIAssociatedCodeList ciAssociatedCodeList = mapper.readValue(apiconfigResponse.body(), CIAssociatedCodeList.class);
            enrolledCreditorInstitutionStations.addAll(
                    ciAssociatedCodeList.getUsedCodes().stream()
                            .map(ciAssociatedCode -> EnrolledCreditorInstitutionStation.builder()
                                    .segregationCode(ciAssociatedCode.getCode())
                                    .stationId(ciAssociatedCode.getStationName())
                                    .build())
                            .collect(Collectors.toList())
            );
            this.logger.log(Level.INFO, () -> String.format("Retrieved the following list of used segregation codes for creditor institution with fiscal code [%s]: [%s]", organizationFiscalCode, enrolledCreditorInstitutionStations));
        }

        return EnrolledCreditorInstitutionStations.builder()
                .stations(enrolledCreditorInstitutionStations)
                .build();
    }

    private String getRawPathFromDomain(String domain) {
        String serviceUrl = Constants.DOMAIN_TO_SERVICE_URI_MAPPING.get(domain);
        if (serviceUrl == null) {
            throw new IllegalArgumentException(String.format("No valid service mapping for domain %s", domain));
        }
        String apiconfigRawPath = String.format(Constants.GET_SEGREGATIONCODE_PATH_TEMPLATE, this.apiconfigPath);
        return apiconfigRawPath.replace("{service}", serviceUrl);
    }

    private HttpResponse<String> executeCallToGetSegregationCodes(String creditorInstitution, String path) throws URISyntaxException, IOException, InterruptedException {
        // extracting path for API call
        String refactoredAuthorizerPath = path.replace("{creditorInstitutionCode}", creditorInstitution);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(new URI(refactoredAuthorizerPath))
                .version(HttpClient.Version.HTTP_2)
                .header("Ocp-Apim-Subscription-Key", this.apiconfigSubkey)
                .GET()
                .build();
        HttpResponse<String> apiconfigResponse = this.httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        this.logger.log(Level.INFO, () -> String.format("Communication with APIConfig ended, returned HTTP status code [%s] and body [%s]", apiconfigResponse.statusCode(), apiconfigResponse.body()));
        return apiconfigResponse;
    }
}
