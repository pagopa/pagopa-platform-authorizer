package it.gov.pagopa.authorizer.model.enumeration;

import java.util.Arrays;

/**
 * Enum that hold value of the error codes produced by Authorizer Configuration client
 */
public enum ReasonErrorCode {
    ERROR_AUTHORIZER_CONFIG(800),
    ERROR_AUTHORIZER_CONFIG_UNEXPECTED(801),
    ERROR_AUTHORIZER_CONFIG_MAPPING(802);

    private final int code;

    ReasonErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static boolean isNotAReasonErrorCode(int statusCode) {
        return Arrays.stream(ReasonErrorCode.values())
                .noneMatch(reasonErrorCode -> reasonErrorCode.code == statusCode);
    }
}
