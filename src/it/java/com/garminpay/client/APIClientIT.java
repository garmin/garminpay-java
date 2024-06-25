package com.garminpay.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garminpay.BaseIT;
import com.garminpay.exception.GarminPayApiException;
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class APIClientIT extends BaseIT {
    APIClient apiClient = new APIClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void canExecuteRequest() throws JsonProcessingException {
        HealthResponse response = HealthResponse.builder()
            .healthStatus("OK")
            .build();

        String serializedResponse = objectMapper.writeValueAsString(response);

        Header header = new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        stubFor(get(urlPathEqualTo("/testing"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withBody(serializedResponse)
            )
        );

        ClassicHttpRequest request = ClassicRequestBuilder.get(TESTING_URL + "/testing").build();
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
}
