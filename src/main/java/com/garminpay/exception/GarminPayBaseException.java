package com.garminpay.exception;

/**
 * Abstract base class for all Garmin Pay exceptions.
 */
public abstract class GarminPayBaseException extends RuntimeException {

    /**
     * Constructs a new GarminPayBaseException with the specified detail message.
     *
     * @param message the detail message.
     */
    protected GarminPayBaseException(String message) {
        super(message);
    }

    /**
     * Constructs a new GarminPayBaseException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause of the exception.
     */
    protected GarminPayBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
