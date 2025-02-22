/*
 * Copyright 2024 Garmin International, Inc.
 * Licensed under the Garmin Pay Software License Agreement; you
 * may not use this file except in compliance with the Garmin Pay Software License Agreement.
 */
package com.garmin.garminpay.exception;

/**
 * Exception thrown when there is an SDK-related error in the Garmin Pay integration.
 */
public class GarminPaySDKException extends GarminPayBaseException {
    /**
     * Constructs a new GarminPaySDKException with the specified detail message.
     *
     * @param message The detail message explaining the reason for the exception.
     */
    public GarminPaySDKException(String message) {
        super(message);
    }

    /**
     * Constructs a new GarminPaySDKException with the specified detail message and cause.
     *
     * @param message The detail message explaining the reason for the exception.
     * @param cause   The cause of the exception.
     */
    public GarminPaySDKException(String message, Throwable cause) {
        super(message, cause);
    }
}
