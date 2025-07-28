package com.tosan.client.http.starter.configuration;

import feign.Request;
import feign.Response;
import feign.micrometer.FeignContext;
import feign.micrometer.FeignObservationConvention;
import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.observation.ClientHttpObservationDocumentation;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Locale;

import static org.springframework.http.client.observation.ClientHttpObservationDocumentation.LowCardinalityKeyNames.*;


/**
 * @author MosiDev
 * @since 7/15/25
 */
public class TosanFeignObservationConvention implements FeignObservationConvention {
    private static final String DEFAULT_NAME = "http.client.requests";
    private static final String UNKNOWN_VALUE = "UNKNOWN";
    private static final KeyValue STATUS_CLIENT_ERROR = KeyValue.of(STATUS, "CLIENT_ERROR");
    private static final KeyValue HTTP_OUTCOME_SUCCESS = KeyValue.of(OUTCOME, "SUCCESS");

    private String externalName;

    public TosanFeignObservationConvention externalName(String externalName) {
        this.externalName = externalName;
        return this;
    }

    @Override
    public String getName() {
        return DEFAULT_NAME;
    }

    @Override
    public String getContextualName(FeignContext context) {
        Request request = context.getCarrier();
        return (request != null ? "feign http " + request.httpMethod().name().toLowerCase(Locale.ROOT) : null);
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(FeignContext context) {
        // Make sure that KeyValues entries are already sorted by name for better performance
        return KeyValues.of(clientName(context), exception(context), method(context), outcome(context), status(context), uri(context));
    }

    protected KeyValue uri(FeignContext context) {
        if (context.getCarrier() != null) {
            try {
                String cleanUri = UriComponentsBuilder.fromUriString(context.getCarrier().url())
                        .replaceQuery(null)
                        .build(true)
                        .toUriString();
                return KeyValue.of(URI, cleanUri);
            } catch (Exception e) {
                return KeyValue.of(URI, UNKNOWN_VALUE);
            }
        }
        return KeyValue.of(URI, KeyValue.NONE_VALUE);
    }

    protected KeyValue method(FeignContext context) {
        if (context.getCarrier() != null) {
            return KeyValue.of(METHOD, context.getCarrier().httpMethod().name());
        } else {
            return KeyValue.of(METHOD, KeyValue.NONE_VALUE);
        }
    }

    protected KeyValue status(FeignContext context) {
        Response response = context.getResponse();
        if (response == null) {
            return STATUS_CLIENT_ERROR;
        }
        return KeyValue.of(STATUS, String.valueOf(response.status()));
    }

    protected KeyValue clientName(FeignContext context) {
        if (context.getCarrier() != null) {
            return StringUtils.isNotBlank(externalName) ? KeyValue.of(CLIENT_NAME, externalName) :
                    KeyValue.of(CLIENT_NAME, context.getCarrier().requestTemplate().url());
        }
        return KeyValue.of(CLIENT_NAME, KeyValue.NONE_VALUE);
    }

    protected KeyValue exception(FeignContext context) {
        Throwable error = context.getError();
        if (error != null) {
            String simpleName = error.getClass().getSimpleName();
            return KeyValue.of(EXCEPTION,
                    StringUtils.isNotBlank(simpleName) ? simpleName : error.getClass().getName());
        }
        return KeyValue.of(EXCEPTION, KeyValue.NONE_VALUE);
    }

    protected KeyValue outcome(FeignContext context) {
        if (context.getResponse() != null) {
            try {
                HttpStatus statusCode = HttpStatus.valueOf(context.getResponse().status());
                if (statusCode.is2xxSuccessful()) {
                    return HTTP_OUTCOME_SUCCESS;
                } else {
                    return KeyValue.of(OUTCOME, statusCode.series().name());
                }
            } catch (Exception ex) {
                return KeyValue.of(OUTCOME, UNKNOWN_VALUE);
            }
        }
        return KeyValue.of(OUTCOME, UNKNOWN_VALUE);
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(FeignContext context) {
        // Make sure that KeyValues entries are already sorted by name for better performance
        return KeyValues.of(requestUri(context));
    }

    protected KeyValue requestUri(FeignContext context) {
        if (context.getCarrier() != null) {
            return KeyValue.of(ClientHttpObservationDocumentation.HighCardinalityKeyNames.HTTP_URL, context.getCarrier().url());
        }
        return KeyValue.of(ClientHttpObservationDocumentation.HighCardinalityKeyNames.HTTP_URL, KeyValue.NONE_VALUE);
    }
}
