/*
 * Copyright 2024 Garmin International, Inc.
 * Licensed under the Garmin Pay Software License Agreement; you
 * may not use this file except in compliance with the Garmin Pay Software License Agreement.
 */
package com.garmin.garminpay.model.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import com.garmin.garminpay.exception.GarminPaySDKException;

import java.io.IOException;
import java.util.Arrays;

@Value
@Builder
@Slf4j
public class APIResponseDTO {
    int status;
    String content;
    Header[] headers;
    String path;

    /**
     * Converts a ClassicHttpResponse object to an APIResponseDTO object.
     *
     * @param response The response to build the DTO from
     * @param path The path used by the API
     * @return An APIResponseDTO object
     */
    public static APIResponseDTO fromHttpResponse(ClassicHttpResponse response, String path) {
        log.debug("Mapping {} http response to DTO", path);
        try {
            return APIResponseDTO.builder()
                .status(response.getCode())
                .content(EntityUtils.toString(response.getEntity()))
                .headers(response.getHeaders())
                .path(path)
                .build();
        } catch (ParseException | IOException e) {
            log.warn("Failed to parse http response to DTO", e);
            throw new GarminPaySDKException("Failed to build APIResponseDTO from ClassicHttpResponse");
        }
    }

    /**
     * Filters headers of a response object to find and return the CF-RAY field if it exists.
     *
     * @return CF-RAY id
     */
    public String findCFRay() {
        if (headers != null) {
            return Arrays.stream(headers)
                .filter(header -> "CF-RAY".equalsIgnoreCase(header.getName()))
                .map(Header::getValue)
                .findFirst()
                .orElse("null");
        }
        return "null";
    }

    /**
     * Filters headers of a response object to find and return the x-request-id field if it exists.
     *
     * @return x-request-id
     */
    public String findXRequestId() {
        if (headers != null) {
            return Arrays.stream(headers)
                .filter(header -> "x-request-id".equalsIgnoreCase(header.getName()))
                .map(Header::getValue)
                .findFirst()
                .orElse("null");
        }
        return "null";
    }

    /**
     * Matches headers of a response object to find and return the maintenance-mode field if it exists.
     *
     * @return boolean representing active maintenance
     */
    public boolean isMaintenanceMode() {
        if (headers != null) {
            return Arrays.stream(headers)
                .anyMatch(header -> "maintenance-mode".equalsIgnoreCase(header.getName())
                && "true".equalsIgnoreCase(header.getValue()));
        }
        return false;
    }
}
