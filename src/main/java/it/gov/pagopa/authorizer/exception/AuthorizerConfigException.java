package it.gov.pagopa.authorizer.exception;

import lombok.Getter;

/**
 * Thrown in case an error occur when invoking Authorizer Configuration service
 */
@Getter
public class AuthorizerConfigException extends Exception {

    private final int statusCode;

    /**
     * Constructs new exception with provided message
     *
     * @param message    Detail message
     * @param statusCode status code
     */
    public AuthorizerConfigException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Constructs new exception with provided message
     *
     * @param message    Detail message
     * @param statusCode status code
     * @param cause      Exception causing the constructed one
     */
    public AuthorizerConfigException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}
