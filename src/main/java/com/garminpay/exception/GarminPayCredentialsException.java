package com.garminpay.exception;

import com.garminpay.model.response.ErrorResponse;

public class GarminPayCredentialsException extends GarminPayBaseException {
    private final String path;
    private final Integer status;
    private final String summary;
    private final String description;
    private final String details;
    private final String created;
    private final String requestId;
    private final String cfRay;

    /**
     * Constructs a new GarminPayCredentialsException with the specified details.
     *
     * @param message The detail message explaining the reason for the exception.
     * @param errorResponse error response returned from the API.
     * @param cfRay         CF-RAY from the API response headers.
     */
    public GarminPayCredentialsException(String message, ErrorResponse errorResponse, String cfRay) {
        super(message);
        this.path = errorResponse.getPath();
        this.status = errorResponse.getStatus();
        this.summary = errorResponse.getSummary();
        this.description = errorResponse.getDescription();
        this.details = errorResponse.getDetails();
        this.created = errorResponse.getCreatedTs();
        this.requestId = errorResponse.getRequestId();
        this.cfRay = cfRay;
    }

    /**
     * Constructs a new GarminPayCredentialsException with the specified details.
     *
     * @param message The detail message explaining the reason for the exception.
     */
    public GarminPayCredentialsException(String message) {
        super(message);
        this.path = null;
        this.status = null;
        this.summary = null;
        this.description = null;
        this.details = null;
        this.created = null;
        this.requestId = null;
        this.cfRay = null;
    }
}
