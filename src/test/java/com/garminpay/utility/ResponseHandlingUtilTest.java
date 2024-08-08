package com.garminpay.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garminpay.TestUtils;
import com.garminpay.exception.GarminPayApiException;
import com.garminpay.exception.GarminPayCredentialsException;
import com.garminpay.exception.GarminPayMaintenanceException;
import com.garminpay.model.dto.APIResponseDTO;
import com.garminpay.model.response.ErrorResponse;
import com.garminpay.model.response.HealthResponse;
import com.garminpay.model.response.OAuthTokenResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResponseHandlingUtilTest {

    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    void canParseValidResponse() throws JsonProcessingException {
        HealthResponse healthResponse = HealthResponse.builder()
            .healthStatus("UP")
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_OK)
            .content(objectMapper.writeValueAsString(healthResponse))
            .headers(TestUtils.TESTING_HEADERS)
            .build();

        assertDoesNotThrow(() -> {
            HealthResponse parsedResponse = ResponseHandlingUtil.parseResponse(responseDTO, HealthResponse.class);
            assertNotNull(parsedResponse);
            assertEquals(parsedResponse.getHealthStatus(), healthResponse.getHealthStatus());
        });
    }

    @Test
    void canParseMaintenanceModeResponse() {
        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
            .headers(new Header[]{new BasicHeader("maintenance-mode", "true")})
            .build();

        assertThrows(GarminPayMaintenanceException.class, () -> ResponseHandlingUtil.parseResponse(responseDTO, HealthResponse.class));
    }

    @Test
    void canParseUnauthenticatedResponseWithBody() {
        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_UNAUTHORIZED)
            .headers(TestUtils.TESTING_HEADERS)
            .content("access denied")
            .build();

        GarminPayCredentialsException exception = assertThrows(GarminPayCredentialsException.class, () -> ResponseHandlingUtil.parseResponse(responseDTO, OAuthTokenResponse.class));
        assertEquals(TestUtils.CF_RAY_HEADER.getValue(), exception.getCfRay());
        assertEquals(TestUtils.X_REQUEST_ID_HEADER.getValue(), exception.getRequestId());
    }

    @Test
    void canParseUnauthenticatedResponseWithoutBody() {
        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_UNAUTHORIZED)
            .headers(TestUtils.TESTING_HEADERS)
            .build();

        GarminPayCredentialsException exception = assertThrows(GarminPayCredentialsException.class, () -> ResponseHandlingUtil.parseResponse(responseDTO, OAuthTokenResponse.class));
        assertEquals(TestUtils.CF_RAY_HEADER.getValue(), exception.getCfRay());
        assertEquals(TestUtils.X_REQUEST_ID_HEADER.getValue(), exception.getRequestId());
    }

    @Test
    void canParsePlatformErrorWithValidErrorResponse() throws JsonProcessingException {
        ErrorResponse response = ErrorResponse.builder()
            .path("/invalidPath")
            .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
            .summary("Error summary")
            .description("Error description")
            .details("Error details")
            .createdTs(Instant.now().toString())
            .requestId(TestUtils.X_REQUEST_ID_HEADER.getValue())
            .message("Test message")
            .cfRay(TestUtils.CF_RAY_HEADER.getValue())
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
            .headers(TestUtils.TESTING_HEADERS)
            .content(objectMapper.writeValueAsString(response))
            .build();

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> ResponseHandlingUtil.parseResponse(responseDTO, ErrorResponse.class));
        assertEquals(TestUtils.CF_RAY_HEADER.getValue(), exception.getCfRay());
        assertEquals(TestUtils.X_REQUEST_ID_HEADER.getValue(), exception.getRequestId());
    }

    @Test
    void canParsePlatformErrorWithInvalidErrorResponse() {
        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
            .headers(TestUtils.TESTING_HEADERS)
            .build();

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> ResponseHandlingUtil.parseResponse(responseDTO, ErrorResponse.class));
        assertEquals(TestUtils.CF_RAY_HEADER.getValue(), exception.getCfRay());
        assertEquals(TestUtils.X_REQUEST_ID_HEADER.getValue(), exception.getRequestId());
    }
}
