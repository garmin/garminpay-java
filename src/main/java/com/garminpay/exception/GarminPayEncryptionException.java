package com.garminpay.exception;

/**
 * Exception thrown when there is an encryption-related error in the Garmin Pay integration.
 */
public class GarminPayEncryptionException extends GarminPaySDKException {
    /**
     * Constructs a new GarminPayEncryptionException with the specified detail message.
     *
     * @param message The detail message explaining the reason for the exception.
     */
    public GarminPayEncryptionException(String message) {
        super(message);
    }

    /**
     * Constructs a new GarminPayEncryptionException with the specified detail message and cause.
     *
     * @param message The detail message explaining the reason for the exception.
     * @param cause   The cause of the exception.
     */
    public GarminPayEncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
