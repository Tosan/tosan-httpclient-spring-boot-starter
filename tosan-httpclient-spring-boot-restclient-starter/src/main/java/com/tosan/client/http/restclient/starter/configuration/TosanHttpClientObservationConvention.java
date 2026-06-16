package com.tosan.client.http.restclient.starter.configuration;

import io.micrometer.common.KeyValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.client.observation.ClientHttpObservationDocumentation;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.http.client.observation.ClientHttpObservationDocumentation.LowCardinalityKeyNames.CLIENT_NAME;
import static org.springframework.http.client.observation.ClientHttpObservationDocumentation.LowCardinalityKeyNames.URI;

/**
 * @author MosiDev
 * @since 7/15/25
 */
public class TosanHttpClientObservationConvention extends DefaultClientRequestObservationConvention {
    private String externalName;

    public TosanHttpClientObservationConvention externalName(String externalName) {
        this.externalName = externalName;
        return this;
    }

    @Override
    public KeyValue uri(ClientRequestObservationContext context) {
        if (context.getCarrier() != null) {
            try {
                String cleanUri = UriComponentsBuilder.fromUri(context.getCarrier().getURI())
                        .replaceQuery(null)
                        .build(true)
                        .toUriString();
                return KeyValue.of(URI, cleanUri);
            } catch (Exception e) {
                return KeyValue.of(URI, KeyValue.NONE_VALUE);
            }
        }
        return KeyValue.of(URI, KeyValue.NONE_VALUE);
    }

    @Override
    protected KeyValue clientName(ClientRequestObservationContext context) {
        if (context.getCarrier() != null) {
            return StringUtils.isNotBlank(externalName) ? KeyValue.of(CLIENT_NAME, externalName) :
                    KeyValue.of(CLIENT_NAME, context.getCarrier().getURI().getHost());
        }
        return KeyValue.of(ClientHttpObservationDocumentation.LowCardinalityKeyNames.CLIENT_NAME, KeyValue.NONE_VALUE);
    }
}
