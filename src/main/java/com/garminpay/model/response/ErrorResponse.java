package com.garminpay.model.response;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Getter
@SuperBuilder
public abstract class ErrorResponse {
    String path;
    @Setter
    int status;
    String summary;
    String description;
    String details;
    String requestId;
    String message;
}
