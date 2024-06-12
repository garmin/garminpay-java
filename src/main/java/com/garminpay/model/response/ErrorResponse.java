package com.garminpay.model.response;

import lombok.Getter;
import lombok.experimental.SuperBuilder;


@Getter
@SuperBuilder
public abstract class ErrorResponse {
    String path;
    int status;
    String summary;
    String description;
    String details;
    String requestId;
    String message;
}
