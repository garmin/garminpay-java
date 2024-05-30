package com.garminpay.exception;

public class GarminPayEncryptionException extends RuntimeException {

    /**
     * Constructs a new GarminPayApiException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param error   the cause (A null value is permitted, and indicates that the cause is nonexistent or unknown)
     */
    public GarminPayEncryptionException(String message, Throwable error) {
        super(message, error);
    }

    /**
     * Constructs a new GarminPayApiException with the specified detail message.
     *
     * @param message the detail message
     */
    public GarminPayEncryptionException(String message) {
        super(message);
    }
}
