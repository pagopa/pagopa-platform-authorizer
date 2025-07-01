package it.gov.pagopa.authorizer.exception;

import lombok.Getter;

/**
 * Thrown in case an unexpected error occur when invoking Authorizer Configuration service
 */
@Getter
public class AuthorizerConfigUnexpectedException extends RuntimeException {

    /**
     * Constructs new exception with provided cause
     *
     * @param cause Exception causing the constructed one
     */
    public AuthorizerConfigUnexpectedException(Throwable cause) {
        super(cause);
    }

    public AuthorizerConfigUnexpectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
