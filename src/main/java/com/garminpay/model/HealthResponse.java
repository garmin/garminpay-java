package com.garminpay.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class HealthResponse {
    /**
     * The status of the health check.
     */
    @JsonProperty("status")
    String status;
}

