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
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.Test;

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
    void cannotParseInvalidMaintenanceModeResponse() {
        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
            .headers(new Header[]{new BasicHeader("maintenance-mode", "true")})
            .build();

        assertThrows(GarminPayMaintenanceException.class, () -> ResponseHandlingUtil.parseResponse(responseDTO, ErrorResponse.class));
    }

    @Test
    void cannotParseInvalidUnauthenticatedResponse() {
        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_UNAUTHORIZED)
            .headers(TestUtils.TESTING_HEADERS)
            .build();

        GarminPayCredentialsException exception = assertThrows(GarminPayCredentialsException.class, () -> ResponseHandlingUtil.parseResponse(responseDTO, ErrorResponse.class));
        assertEquals(TestUtils.CF_RAY_HEADER.getValue(), exception.getCfRay());
        assertEquals(TestUtils.X_REQUEST_ID_HEADER.getValue(), exception.getRequestId());
    }

    @Test
    void cannotParseInvalidErrorResponse() {
        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
            .headers(TestUtils.TESTING_HEADERS)
            .build();

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> ResponseHandlingUtil.parseResponse(responseDTO, ErrorResponse.class));
        assertEquals(TestUtils.CF_RAY_HEADER.getValue(), exception.getCfRay());
        assertEquals(TestUtils.X_REQUEST_ID_HEADER.getValue(), exception.getXRequestID());
    }
}
