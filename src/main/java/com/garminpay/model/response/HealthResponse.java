package com.garminpay.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder
public class HealthResponse {
    /**
     * The health of Garmin Platform.
     */
    @JsonProperty("status")
    String healthStatus;

    /**
     * The status code of the health check.
     */
    int statusCode;
}
