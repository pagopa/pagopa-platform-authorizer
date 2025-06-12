package it.gov.pagopa.authorizer.service.impl;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.core.functions.CheckedFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import it.gov.pagopa.authorizer.client.AuthorizerConfigClient;
import it.gov.pagopa.authorizer.client.impl.AuthorizerConfigClientImpl;
import it.gov.pagopa.authorizer.exception.AuthorizerConfigException;
import it.gov.pagopa.authorizer.exception.AuthorizerConfigUnexpectedException;
import it.gov.pagopa.authorizer.model.AuthConfiguration;
import it.gov.pagopa.authorizer.model.enumeration.ReasonErrorCode;
import it.gov.pagopa.authorizer.service.AuthorizerConfigClientRetryWrapper;

import java.net.http.HttpResponse;

/**
 * {@inheritDoc}
 */
public class AuthorizerConfigClientRetryWrapperImpl implements AuthorizerConfigClientRetryWrapper {

    private static final Long INITIAL_INTERVAL = Long.parseLong(System.getenv().getOrDefault("AUTHORIZER_CONFIG_INITIAL_INTERVAL", "200"));
    private static final Double MULTIPLIER = Double.parseDouble(System.getenv().getOrDefault("AUTHORIZER_CONFIG_MULTIPLIER", "2.0"));
    private static final Double RANDOMIZATION_FACTOR = Double.parseDouble(System.getenv().getOrDefault("AUTHORIZER_CONFIG_RANDOMIZATION_FACTOR", "0.6"));
    private static final Integer MAX_RETRIES = Integer.parseInt(System.getenv().getOrDefault("AUTHORIZER_CONFIG_MAX_RETRIES", "3"));

    private final AuthorizerConfigClient authorizerConfigClient;
    private final Retry retry;

    AuthorizerConfigClientRetryWrapperImpl(AuthorizerConfigClient authorizerClient, Retry retry) {
        this.authorizerConfigClient = authorizerClient;
        this.retry = retry;
    }

    public AuthorizerConfigClientRetryWrapperImpl() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(MAX_RETRIES)
                .intervalFunction(IntervalFunction.ofExponentialRandomBackoff(INITIAL_INTERVAL, MULTIPLIER, RANDOMIZATION_FACTOR))
                .retryOnException(e -> (e instanceof AuthorizerConfigException authConfigException) && ReasonErrorCode.isNotAReasonErrorCode(authConfigException.getStatusCode()))
                .build();

        RetryRegistry registry = RetryRegistry.of(config);

        this.authorizerConfigClient = AuthorizerConfigClientImpl.getInstance();
        this.retry = registry.retry("authorizerConfigRetry");
    }

    @Override
    public HttpResponse<String> refreshConfigurationWithRetry(
            AuthConfiguration authConfiguration,
            String domain,
            boolean addInProgress
    ) throws AuthorizerConfigException {
        CheckedFunction<AuthorizerConfigClientParams, HttpResponse<String>> function =
                Retry.decorateCheckedFunction(retry,
                        params -> authorizerConfigClient.refreshConfiguration(params.authConfiguration, params.domain, params.addInProgress));

        return runFunction(function, new AuthorizerConfigClientParams(authConfiguration, domain, addInProgress));
    }

    private HttpResponse<String> runFunction(
            CheckedFunction<AuthorizerConfigClientParams, HttpResponse<String>> function,
            AuthorizerConfigClientParams params
    ) throws AuthorizerConfigException {

        try {
            return function.apply(params);
        } catch (Throwable e) {
            if (e instanceof AuthorizerConfigException authorizerConfigException) {
                throw authorizerConfigException;
            }
            throw new AuthorizerConfigUnexpectedException(e);
        }
    }

    private record AuthorizerConfigClientParams(AuthConfiguration authConfiguration,
                                                String domain,
                                                boolean addInProgress) {
    }
}