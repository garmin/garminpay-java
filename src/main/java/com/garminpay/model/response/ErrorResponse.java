package com.garminpay.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.Optional;
import java.util.StringJoiner;

@Data
@Jacksonized
@Builder
public final class ErrorResponse {
    String path;
    int status;
    String summary;
    String description;
    String details;

    @JsonProperty("created")
    String createdTs;

    String requestId;
    String message;

    String cfRay;

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(". ");

        Optional.ofNullable(path)
            .ifPresent(value -> joiner.add("Path: " + value));

        Optional.ofNullable(status)
            .ifPresent(value -> joiner.add("Status: " + value));

        Optional.ofNullable(summary)
            .ifPresent(value -> joiner.add("Summary: " + value));

        Optional.ofNullable(description)
            .ifPresent(value -> joiner.add("Description: " + value));

        Optional.ofNullable(details)
            .ifPresent(value -> joiner.add("Details: " + value));

        Optional.ofNullable(createdTs)
            .ifPresent(value -> joiner.add("Created: " + value));

        Optional.ofNullable(requestId)
            .ifPresent(value -> joiner.add("RequestId: " + value));

        Optional.ofNullable(message)
            .ifPresent(value -> joiner.add("Message: " + value));

        Optional.ofNullable(cfRay)
            .ifPresent(value -> joiner.add("CF Ray: " + value));

        return joiner.toString();
    }
}
