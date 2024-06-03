package com.garminpay.exception;

/**
 * GarminPayApiException is a custom exception class used to indicate errors
 * when interacting with the Garmin Pay API.
 */
public class GarminPayApiException extends RuntimeException {

    /**
     * Constructs a new GarminPayApiException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param error   the cause (A null value is permitted, and indicates that the cause is nonexistent or unknown)
     */
    public GarminPayApiException(String message, Throwable error) {
        super(message, error);
    }

    /**
     * Constructs a new GarminPayApiException with the specified detail message.
     *
     * @param message the detail message
     */
    public GarminPayApiException(String message) {
        super(message);
    }

}
