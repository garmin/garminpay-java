/*
 * Copyright 2024 Garmin International, Inc.
 * Licensed under the Garmin Pay Software License Agreement; you
 * may not use this file except in compliance with the Garmin Pay Software License Agreement.
 */
package com.garminpay.exception;

import com.garminpay.model.response.ErrorResponse;
import lombok.Getter;

@Getter
public final class GarminPayCredentialsException extends GarminPayApiException {

    /**
     * Constructs a new GarminPayCredentialsException with the specified details.
     *
     * @param message       The detail message explaining the reason for the exception.
     * @param errorResponse error response returned from the API.
     */
    public GarminPayCredentialsException(String message, ErrorResponse errorResponse) {
        super(message, errorResponse);
    }
}
