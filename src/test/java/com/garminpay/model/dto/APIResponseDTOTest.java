/*
 * Copyright 2024 Garmin International, Inc.
 * Licensed under the Garmin Pay Software License Agreement; you
 * may not use this file except in compliance with the Garmin Pay Software License Agreement.
 */
package com.garminpay.model.dto;

import com.garminpay.TestUtils;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.support.ClassicResponseBuilder;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class APIResponseDTOTest {
    private final Header[] defaultHeaders = {
        new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"),
        TestUtils.CF_RAY_HEADER,
        TestUtils.X_REQUEST_ID_HEADER,
    };

    @Test
    void testBuilder() {
        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_OK)
            .content("Content")
            .headers(defaultHeaders)
            .build();

        assertEquals(HttpStatus.SC_OK, responseDTO.getStatus());
        assertEquals(TestUtils.CF_RAY_HEADER.getValue(), responseDTO.findCFRay());
        assertEquals(TestUtils.X_REQUEST_ID_HEADER.getValue(), responseDTO.findXRequestId());
    }

    @Test
    void testFromHttpResponse() {
        ClassicHttpResponse response = ClassicResponseBuilder.create(HttpStatus.SC_OK)
            .setEntity("Content")
            .setHeaders(defaultHeaders)
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.fromHttpResponse(response, "/");

        assertEquals(HttpStatus.SC_OK, responseDTO.getStatus());
        assertEquals(TestUtils.CF_RAY_HEADER.getValue(), responseDTO.findCFRay());
        assertEquals(TestUtils.X_REQUEST_ID_HEADER.getValue(), responseDTO.findXRequestId());
    }
}
