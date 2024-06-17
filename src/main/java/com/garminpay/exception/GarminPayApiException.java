package com.garminpay.exception;

import lombok.Getter;

/**
 * Exception thrown when there is an API-related error in the Garmin Pay integration.
 */
@Getter
public class GarminPayApiException extends GarminPayBaseException {

    private final String path;
    private final Integer status;
    private final String summary;
    private final String description;
    private final String details;
    private final String created;
    private final String requestId;

    /**
     * Constructs a new GarminPayApiException with the specified details.
     *
     * @param path        The path of the API endpoint that caused the exception.
     * @param status      The HTTP status code returned by the API.
     * @param summary     A brief summary of the error.
     * @param description A detailed description of the error.
     * @param details     Additional details about the error.
     * @param created     The timestamp when the error occurred.
     * @param requestId   The unique request ID associated with the error.
     * @param message     The detail message explaining the reason for the exception.
     */
    public GarminPayApiException(String path, Integer status, String summary, String description,
                                 String details, String created, String requestId, String message) {
        super(message);
        this.path = path;
        this.status = status;
        this.summary = summary;
        this.description = description;
        this.details = details;
        this.created = created;
        this.requestId = requestId;
    }

    /**
     * Constructs a new GarminPayApiException with the specified details and cause.
     *
     * @param path        The path of the API endpoint that caused the exception.
     * @param status      The HTTP status code returned by the API.
     * @param summary     A brief summary of the error.
     * @param description A detailed description of the error.
     * @param details     Additional details about the error.
     * @param created     The timestamp when the error occurred.
     * @param requestId   The unique request ID associated with the error.
     * @param message     The detail message explaining the reason for the exception.
     * @param cause       The cause of the exception.
     */
    public GarminPayApiException(String path, Integer status, String summary, String description,
                                 String details, String created, String requestId, String message, Throwable cause) {
        super(message, cause);
        this.path = path;
        this.status = status;
        this.summary = summary;
        this.description = description;
        this.details = details;
        this.created = created;
        this.requestId = requestId;
    }

}
