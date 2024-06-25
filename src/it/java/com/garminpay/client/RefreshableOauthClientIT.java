package com.garminpay.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garminpay.BaseIT;
import com.garminpay.exception.GarminPayCredentialsException;
import com.garminpay.model.dto.APIResponseDTO;
import com.garminpay.model.response.ErrorResponse;
import com.garminpay.model.response.OAuthTokenResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RefreshableOauthClientIT extends BaseIT {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void willRetryRequestWhenUnauthorized() throws JsonProcessingException {
        OAuthTokenResponse mockToken = OAuthTokenResponse.builder()
            .accessToken("testToken")
            .build();

        String authResponseBody = objectMapper.writeValueAsString(mockToken);

        stubFor(post(urlPathEqualTo("/oauth/token"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .withHeader("CF-RAY", "testing-cf-ray")
                .withBody(authResponseBody)
            )
        );

        stubFor(get(urlPathEqualTo("/testing"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_UNAUTHORIZED)
            )
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
            )
        );

        // Make a request
        ClassicHttpRequest request = ClassicRequestBuilder.get(TESTING_URL + "/testing").build();
        APIResponseDTO responseDTO = client.executeRequest(request);
        assertEquals(HttpStatus.SC_OK, responseDTO.getStatus());
    }

    @Test
    void willNotRetryRequestWhenUnauthorizedTwice() throws JsonProcessingException {
        ErrorResponse errorResponse = ErrorResponse.builder()
            .path("/oauth/token")
            .message("Unauthorized")
            .status(HttpStatus.SC_UNAUTHORIZED)
            .build();

        String authResponseBody = objectMapper.writeValueAsString(errorResponse);

        stubFor(post(urlPathEqualTo("/oauth/token"))
            .willReturn(aResponse()
                .withStatus(401)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .withHeader("CF-RAY", "testing-cf-ray")
                .withBody(authResponseBody)
            )
        );

        stubFor(get(urlPathEqualTo("/testing"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_UNAUTHORIZED)
            )
        );

        ClassicHttpRequest request = ClassicRequestBuilder.get(TESTING_URL + "/testing").build();

        assertThrows(GarminPayCredentialsException.class, () -> client.executeRequest(request));
    }
}
