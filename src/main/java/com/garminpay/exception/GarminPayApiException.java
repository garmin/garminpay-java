package com.garminpay.exception;

import com.garminpay.model.response.ErrorResponse;
import lombok.Getter;

/**
 * Exception thrown when there is an API-related error in the Garmin Pay integration.
 */
@Getter
public class GarminPayApiException extends GarminPayBaseException {

    private String path;
    private int status;
    private String summary;
    private String description;
    private String details;
    private String createdTs;
    private String requestId;
    private String detailMessage;
    private String cfRay;

    /**
     * Constructs a new GarminPayApiException.
     *
     * @param message message thrown from the exception.
     */
    public GarminPayApiException(String message) {
        super(message);
    }

    /**
     * Constructs a new GarminPayApiException with the specified details.
     *
     * @param message       The detail message explaining the reason for the exception.
     * @param errorResponse error response returned from the API.
     */
    public GarminPayApiException(String message, ErrorResponse errorResponse) {
        this(message + ": Details: " + errorResponse.toString());
        setVariables(errorResponse);
    }

    private void setVariables(ErrorResponse errorResponse) {
        this.path = errorResponse.getPath();
        this.status = errorResponse.getStatus();
        this.summary = errorResponse.getSummary();
        this.description = errorResponse.getDescription();
        this.details = errorResponse.getDetails();
        this.createdTs = errorResponse.getCreatedTs();
        this.requestId = errorResponse.getRequestId();
        this.detailMessage = errorResponse.getMessage();
        this.cfRay = errorResponse.getCfRay();
    }
}
