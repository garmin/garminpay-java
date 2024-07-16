package com.garminpay.exception;

import com.garminpay.model.response.ErrorResponse;
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
    private final String cfRay;
    private final String xRequestID;

    /**
     * Constructs a new GarminPayApiException with the specified details.
     * Only to be called when Apache HTTP Client encounters an error, otherwise call a constructor with better details.
     *
     * @param path    The path of the API endpoint that caused the exception.
     * @param message The detail message explaining the reason for the exception.
     */
    public GarminPayApiException(String path, String message) {
        super(message);
        this.path = path;
        this.status = null;
        this.summary = null;
        this.description = null;
        this.details = null;
        this.created = null;
        this.requestId = null;
        this.cfRay = null;
        this.xRequestID = null;
    }

    /**
     * Constructs a new GarminPayApiException with the specified details.
     * Only to be called when an error response is successfully parsed.
     *
     * @param errorResponse error response returned from the API.
     * @param cfRay         CF-RAY from the API response headers.
     * @param xRequestID    X-REQUEST-ID from the API response headers.
     */
    public GarminPayApiException(ErrorResponse errorResponse, String cfRay, String xRequestID) {
        super(errorResponse.getMessage());
        this.path = errorResponse.getPath();
        this.status = errorResponse.getStatus();
        this.summary = errorResponse.getSummary();
        this.description = errorResponse.getDescription();
        this.details = errorResponse.getDetails();
        this.created = errorResponse.getCreatedTs();
        this.requestId = errorResponse.getRequestId();
        this.cfRay = cfRay;
        this.xRequestID = xRequestID;
    }

    /**
     * Constructs a new GarminPayApiException.
     *
     * @param message message thrown from the exception.
     */
    public GarminPayApiException(String message) {
        super(message);
        this.path = null;
        this.status = null;
        this.summary = null;
        this.description = null;
        this.details = null;
        this.created = null;
        this.requestId = null;
        this.cfRay = null;
        this.xRequestID = null;
    }
}
