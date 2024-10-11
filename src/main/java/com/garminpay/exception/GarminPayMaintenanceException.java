package com.garminpay.exception;

import com.garminpay.model.response.ErrorResponse;

public final class GarminPayMaintenanceException extends GarminPayApiException {

    /**
     * Constructs a new GarminPayMaintenanceException with the specified detail message.
     *
     * @param message       the detail message.
     * @param errorResponse error response returned from the API.
     */
    public GarminPayMaintenanceException(String message, ErrorResponse errorResponse) {
        super(message, errorResponse);
    }
}
