package com.garminpay.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Value
@EqualsAndHashCode(callSuper = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@SuperBuilder
public class RootResponse extends ErrorResponse {
    /**
     * The links interact with Garmin Pay API.
     */
    @JsonProperty("_links")
    Map<String, HalLink> links;

    @Value
    @Jacksonized
    @Builder
    public static class HalLink {
        String href;
    }
}
