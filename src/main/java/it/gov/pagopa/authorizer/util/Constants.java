package it.gov.pagopa.authorizer.util;

import java.util.Map;

public class Constants {

    public static final String AUTH_CONFIGURATION_KEY_FORMAT = "%s_%s";

    public static final String AUTH_REFRESH_CONFIGURATION_PATH_TEMPLATE = "%s/cache/domains/{domain}?add_in_progress=%s";

    public static final String GET_SEGREGATIONCODE_PATH_TEMPLATE = "%s/creditorinstitutions/{creditorInstitutionCode}/segregationcodes?service={service}";

    public static final String REFRESH_CONFIG_PATH_PARAMETER = "REFRESH_CONFIGURATION_PATH";

    public static final String APICONFIG_SELFCARE_INTEGRATION_PATH_PARAMETER = "APICONFIG_SELFCARE_INTEGRATION_PATH";

    public static final String APICONFIG_SELFCARE_INTEGRATION_SUBKEY_PARAMETER = "APICONFIG_SELFCARE_INTEGRATION_SUBKEY";

    public static final String INFO_VERSION = "API_INFO_VERSION";

    public static final String INFO_NAME = "API_INFO_NAME";

    public static final String INFO_ENV = "API_INFO_ENVIRONMENT";

    public static final String WILDCARD_CHARACTER = "*";

    public static final Map<String, String> DOMAIN_TO_SERVICE_URI_MAPPING = Map.ofEntries(
            // insert here other static mapping from domain to service URI
            Map.entry("gpd", "gpd-payments/api/v1")
    );

    public static final String CONTENT_TYPE = "Content-Type";


    private Constants() {}
}
