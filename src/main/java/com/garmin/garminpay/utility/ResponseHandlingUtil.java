/*
 * Copyright 2024 Garmin International, Inc.
 * Licensed under the Garmin Pay Software License Agreement; you
 * may not use this file except in compliance with the Garmin Pay Software License Agreement.
 */
package com.garmin.garminpay.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garmin.garminpay.exception.GarminPayApiException;
import com.garmin.garminpay.exception.GarminPayCredentialsException;
import com.garmin.garminpay.exception.GarminPayMaintenanceException;
import com.garmin.garminpay.exception.GarminPaySDKException;
import com.garmin.garminpay.model.dto.APIResponseDTO;
import com.garmin.garminpay.model.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ResponseHandlingUtil {
    private static final ObjectMapper OBJECT_MAPPER
        = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private ResponseHandlingUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Parses an APIResponseDTO into an object of the specified response class type.
     *
     * @param responseDTO   The APIResponseDTO containing the data to be parsed.
     * @param responseClass The class type to which the responseDTO should be parsed.
     * @param <T>           The type of the response object.
     * @return An instance of the specified response class type, populated with data from responseDTO.
     */
    public static <T> T parseResponse(APIResponseDTO responseDTO, Class<T> responseClass) {
        log.debug("Parsing response from Client to class {}", responseClass.getName());

        // If status is in [200, 300) range, parse the desired response class and return it
        if (responseDTO.getStatus() >= 200 && responseDTO.getStatus() < 300) {
            try {
                return OBJECT_MAPPER.readValue(responseDTO.getContent(), responseClass);
            } catch (JsonProcessingException e) {
                log.warn("Found an acceptable response status code but encountered unknown response body."
                        + " status: {}, x-request-id: {}, CF-RAY: {}",
                    responseDTO.getStatus(), responseDTO.findXRequestId(), responseDTO.findCFRay()
                );
                throw new GarminPaySDKException("Failed to parse response entity.");
            }
        }

        // Check for maintenance mode
        if (responseDTO.isMaintenanceMode()) {
            handleGarminPayMaintenanceMode(responseDTO);
        }

        // Check for authentication error
        if (responseDTO.getStatus() == 401) {
            parseGarminPayAuthenticationError(responseDTO);
        }

        // Parse as an error from GarminPay platform
        parseGarminPayPlatformError(responseDTO);

        // Flow will never reach this point as exceptions will have been thrown by this point
        return null;
    }

    private static void handleGarminPayMaintenanceMode(APIResponseDTO responseDTO) {
        log.warn("GarminPay platform is currently undergoing maintenance, try again in a little while."
                + " status: {}, x-request-id: {}, CF-RAY: {}",
            responseDTO.getStatus(), responseDTO.findXRequestId(), responseDTO.findCFRay()
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
            .status(responseDTO.getStatus())
            .path(responseDTO.getPath())
            .requestId(responseDTO.findXRequestId())
            .cfRay(responseDTO.findCFRay())
            .build();

        throw new GarminPayMaintenanceException(
            "GarminPay platform is currently undergoing maintenance, try again in a little while.",
            errorResponse
        );
    }

    private static void parseGarminPayAuthenticationError(APIResponseDTO responseDTO) {
        try {
            log.warn("Request failed to authenticate with status: {}, x-request-id: {}, CF-RAY: {}",
                responseDTO.getStatus(), responseDTO.findXRequestId(), responseDTO.findCFRay()
            );
            ErrorResponse errorResponse = OBJECT_MAPPER.readValue(responseDTO.getContent(), ErrorResponse.class);
            errorResponse.setCfRay(responseDTO.findCFRay());

            throw new GarminPayCredentialsException(
                "Failed to authenticate, client credentials may be invalid.",
                errorResponse
            );
        } catch (JsonProcessingException | IllegalArgumentException e) {
            log.trace("Unable to parse error response for authentication failure. Parse exception: ", e);
            ErrorResponse errorResponse = ErrorResponse.builder()
                .status(responseDTO.getStatus())
                .path(responseDTO.getPath())
                .requestId(responseDTO.findXRequestId())
                .cfRay(responseDTO.findCFRay())
                .build();

            throw new GarminPayCredentialsException(
                "Failed to authenticate, client credentials may be invalid.",
                errorResponse
            );
        }
    }

    private static void parseGarminPayPlatformError(APIResponseDTO responseDTO) {
        try {
            log.warn("Response from GarminPay contained an invalid status code. status: {}, x-request-id: {}, CF-RAY: {}",
                responseDTO.getStatus(), responseDTO.findXRequestId(), responseDTO.findCFRay()
            );
            ErrorResponse errorResponse = OBJECT_MAPPER.readValue(responseDTO.getContent(), ErrorResponse.class);
            errorResponse.setCfRay(responseDTO.findCFRay());
            throw new GarminPayApiException("GarminPay platform returned an invalid status code", errorResponse);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            log.trace("Unable to parse error response from GarminPay platform. Parse exception: ", e);
            ErrorResponse errorResponse = ErrorResponse.builder()
                .status(responseDTO.getStatus())
                .path(responseDTO.getPath())
                .requestId(responseDTO.findXRequestId())
                .cfRay(responseDTO.findCFRay())
                .build();

            throw new GarminPayApiException("GarminPay platform returned an invalid status code", errorResponse);
        }
    }
}
