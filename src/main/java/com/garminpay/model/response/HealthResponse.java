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
     * The status of the health check.
     */
    @JsonProperty("status")
    String healthStatus;
}

