package com.garminpay.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ErrorResponse {
    String path;
    int status;
    String summary;
    String description;
    String details;

    @JsonProperty("created")
    String createdTs;

    String requestId;
    String message;
}
