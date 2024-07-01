package com.garminpay.model.dto;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.support.ClassicResponseBuilder;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class APIResponseDTOTest {
    private final Header[] defaultHeaders = {
        new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"),
        new BasicHeader("CF-RAY", "testing-cf-ray"),
        new BasicHeader("x-request-id", "testing-x-request-id")
    };

    @Test
    void testBuilder() {
        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_OK)
            .content("Content")
            .headers(defaultHeaders)
            .build();

        assertEquals(HttpStatus.SC_OK, responseDTO.getStatus());
        assertEquals("testing-cf-ray", responseDTO.findCFRay().orElse(null));
        assertEquals("testing-x-request-id", responseDTO.findXRequestId().orElse(null));
    }

    @Test
    void testFromHttpResponse() {
        ClassicHttpResponse response = ClassicResponseBuilder.create(HttpStatus.SC_OK)
            .setEntity("Content")
            .setHeaders(defaultHeaders)
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.fromHttpResponse(response, "/");

        assertEquals(HttpStatus.SC_OK, responseDTO.getStatus());
        assertEquals("testing-cf-ray", responseDTO.findCFRay().orElse(null));
        assertEquals("testing-x-request-id", responseDTO.findXRequestId().orElse(null));
    }
}
