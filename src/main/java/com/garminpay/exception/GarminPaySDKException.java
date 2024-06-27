package com.garminpay.exception;

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
}
