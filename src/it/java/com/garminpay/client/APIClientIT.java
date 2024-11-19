/*
 * Copyright 2024 Garmin International, Inc.
 * Licensed under the Garmin Pay Software License Agreement; you
 * may not use this file except in compliance with the Garmin Pay Software License Agreement.
 */
package com.garminpay.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garminpay.BaseIT;
import com.garminpay.exception.GarminPayApiException;
import com.garminpay.model.SDKVersion;
import com.garminpay.model.dto.APIResponseDTO;
import com.garminpay.model.response.HealthResponse;
import com.github.tomakehurst.wiremock.http.Fault;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.Test;

import static com.garminpay.TestUtils.checkForHeader;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class APIClientIT extends BaseIT {
    private final ObjectMapper objectMapper = new ObjectMapper();
    APIClient apiClient = new APIClient();

    @Test
    void canExecuteRequest() throws JsonProcessingException {
        HealthResponse response = HealthResponse.builder()
            .healthStatus("OK")
            .build();

        String serializedResponse = objectMapper.writeValueAsString(response);

        Header header = new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        stubFor(get(urlPathEqualTo("/health-testing"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withBody(serializedResponse)
            )
        );

        ClassicHttpRequest request = ClassicRequestBuilder.get(TESTING_URL + "/health-testing").build();
        APIResponseDTO responseDTO = apiClient.executeRequest(request);

        assertEquals(HttpStatus.SC_OK, responseDTO.getStatus());
        assertTrue(checkForHeader(header, responseDTO.getHeaders()));
        assertEquals(serializedResponse, responseDTO.getContent());
    }

    @Test
    void canHandleIOException() {
        stubFor(get(urlPathEqualTo("/testing"))
            .willReturn(aResponse()
                .withFault(Fault.CONNECTION_RESET_BY_PEER)
            )
        );

        ClassicHttpRequest request = ClassicRequestBuilder.get(TESTING_URL + "/testing").build();
        assertThrows(GarminPayApiException.class, () -> apiClient.executeRequest(request));
    }

    @Test
    void requestHasVersionHeader() {
        Header testVersionHeader = new BasicHeader(VERSION_HEADER_NAME, SDKVersion.VERSION);

        stubFor(get(urlPathEqualTo("/testing"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
            )
        );

        ClassicHttpRequest request = ClassicRequestBuilder.get(TESTING_URL + "/testing").build();
        apiClient.executeRequest(request);

        assertTrue(checkForHeader(testVersionHeader, request.getHeaders()));
    }


}
