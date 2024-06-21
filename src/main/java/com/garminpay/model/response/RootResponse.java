package com.garminpay.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Value
@Jacksonized
@Builder
public class RootResponse {
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
