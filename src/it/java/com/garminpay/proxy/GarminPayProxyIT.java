package com.garminpay.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garminpay.BaseIT;
import com.garminpay.TestUtils;
import com.garminpay.exception.GarminPayApiException;
import com.garminpay.model.response.ErrorResponse;
import com.garminpay.model.response.HealthResponse;
import com.garminpay.model.response.ExchangeKeysResponse;
import com.garminpay.model.response.OAuthTokenResponse;
import com.garminpay.model.response.RootResponse;
import com.garminpay.model.response.RegisterCardResponse;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GarminPayProxyIT extends BaseIT {

    private final GarminPayProxy garminPayProxy = new GarminPayProxy("http://localhost:" + BaseIT.wireMockServer.port(), "http://localhost:" + BaseIT.wireMockServer.port());
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void canGetRootEndpoint() throws JsonProcessingException {
        Map<String, RootResponse.HalLink> mockLinks = Collections.singletonMap(
            "self", RootResponse.HalLink.builder().href("https://testing/").build()
        );

        RootResponse mockRootResponse = RootResponse.builder()
            .links(mockLinks)
            .build();

        String responseBody = objectMapper.writeValueAsString(mockRootResponse);

        WireMock.stubFor(get(urlPathEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withHeader("CF-RAY", "testing-cf-ray")
                .withBody(responseBody)));

        RootResponse response = garminPayProxy.getRootEndpoint();

        assertEquals("https://testing/", response.getLinks().get("self").getHref());
    }

    @Test
    void canHandle404ResponseFromRootEndpoint() throws JsonProcessingException {
        ErrorResponse mockRootResponse = ErrorResponse.builder()
            .status(HttpStatus.SC_NOT_FOUND)
            .path("/")
            .message("Not Found")
            .build();

        String responseBody = objectMapper.writeValueAsString(mockRootResponse);

        WireMock.stubFor(get(urlPathEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_NOT_FOUND)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withHeader("CF-RAY", "testing-cf-ray")
                .withBody(responseBody)));

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, garminPayProxy::getRootEndpoint);

        assertEquals(HttpStatus.SC_NOT_FOUND, exception.getStatus());
    }

    @Test
    void canHandle502ResponseFromRootEndpoint() {
        WireMock.stubFor(get(urlPathEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_BAD_GATEWAY)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withHeader("CF-RAY", "testing-cf-ray")
                .withBody("Invalid response body")));

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, garminPayProxy::getRootEndpoint);

        assertEquals(HttpStatus.SC_BAD_GATEWAY, exception.getStatus());
    }

    @Test
    void canGetHealthStatus() throws JsonProcessingException {
        HealthResponse mockHealthResponse = HealthResponse.builder()
            .healthStatus("OK")
            .build();

        String responseBody = objectMapper.writeValueAsString(mockHealthResponse);

        WireMock.stubFor(get(urlPathEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withHeader("CF-RAY", "testing-cf-ray")
                .withBody(responseBody)));

        HealthResponse health = garminPayProxy.getHealthStatus();

        assertNotNull(health);
        assertEquals("OK", health.getHealthStatus());
    }

    @Test
    void canHandle502ResponseFromGetHealthStatus() {
        WireMock.stubFor(get(urlPathEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_BAD_GATEWAY)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withHeader("CF-RAY", "testing-cf-ray")
                .withBody("Invalid response body")));

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, garminPayProxy::getHealthStatus);

        assertEquals(HttpStatus.SC_BAD_GATEWAY, exception.getStatus());
    }

    @Test
    void canGetOAuthAccessToken() throws JsonProcessingException {
        OAuthTokenResponse mockToken = OAuthTokenResponse.builder()
            .accessToken("testToken")
            .build();

        String responseBody = objectMapper.writeValueAsString(mockToken);

        String clientID = "testClientID";
        String clientSecret = "testClientSecret";

        stubFor(post(urlPathEqualTo("/oauth/token"))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Basic " + Base64.getEncoder().encodeToString((clientID + ":" + clientSecret).getBytes())))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(ContentType.APPLICATION_FORM_URLENCODED.toString()))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withHeader("CF-RAY", "testing-cf-ray")
                .withBody(responseBody)));

        OAuthTokenResponse response = garminPayProxy.getOAuthAccessToken(clientID, clientSecret);
        assertEquals("testToken", response.getAccessToken());
    }

    @Test
    void canHandle401ResponseFromGetOAuthAccessToken() throws JsonProcessingException {
        String clientID = "test-client-id";
        String clientSecret = "test-client-secret";

        ErrorResponse errorResponse = ErrorResponse.builder()
            .path("/oauth/token")
            .message("Unauthorized")
            .status(HttpStatus.SC_UNAUTHORIZED)
            .build();

        String responseBody = objectMapper.writeValueAsString(errorResponse);

        stubFor(post(urlPathEqualTo("/oauth/token"))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Basic " + Base64.getEncoder().encodeToString((clientID + ":" + clientSecret).getBytes())))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(ContentType.APPLICATION_FORM_URLENCODED.toString()))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_UNAUTHORIZED)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withHeader("CF-RAY", "testing-cf-ray")
                .withBody(responseBody)));

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> garminPayProxy.getOAuthAccessToken(clientID, clientSecret));

        assertEquals(HttpStatus.SC_UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void canCompleteKeyExchange() throws JsonProcessingException {
        String mockOauthToken = "mockAccessToken";

        ExchangeKeysResponse mockKeyResponse = ExchangeKeysResponse.builder()
            .keyId(UUID.randomUUID().toString())
            .active(true)
            .serverPublicKey(TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY)
            .build();

        String responseBody = objectMapper.writeValueAsString(mockKeyResponse);

        stubFor(post(urlPathEqualTo("/config/encryptionKeys"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_CREATED)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withHeader("CF-RAY", "testing-cf-ray")
                .withBody(responseBody)));

        ExchangeKeysResponse exchangeKeysResponse = garminPayProxy.exchangeKeys(mockOauthToken, TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY);
        assertNotNull(exchangeKeysResponse);
    }

    @Test
    void canHandle400ResponseFromKeyExchange() throws JsonProcessingException {
        String mockOauthToken = "mockAccessToken";

        ErrorResponse errorResponse = ErrorResponse.builder()
            .path("/config/encryptionKeys")
            .status(HttpStatus.SC_BAD_REQUEST)
            .message("Bad Request")
            .build();

        String responseBody = objectMapper.writeValueAsString(errorResponse);

        stubFor(post(urlPathEqualTo("/config/encryptionKeys"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_BAD_REQUEST)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withHeader("CF-RAY", "testing-cf-ray")
                .withBody(responseBody)));

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> garminPayProxy.exchangeKeys(mockOauthToken, TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY));

        assertEquals("/config/encryptionKeys", exception.getPath());
        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatus());
    }

    @Test
    void canRegisterCard() throws JsonProcessingException {

        RegisterCardResponse mockRegisterCardResponse = RegisterCardResponse.builder()
            .deepLinkUrl("Deep link URL")
            .build();

        String registerCardResponseBody = objectMapper.writeValueAsString(mockRegisterCardResponse);

        stubFor(post(urlPathEqualTo("/paymentCards"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withHeader("CF-RAY", "testing-cf-ray")
                .withBody(registerCardResponseBody)));

        RegisterCardResponse registerCardResponse = garminPayProxy.registerCard("mockOAuthToken", "mockEncryptedCardData");

        assertNotNull(registerCardResponse.getDeepLinkUrl());
    }

    @Test
    void canHandleOAuthFailureWhenRegisterCard() throws JsonProcessingException {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "invalid_client");

        String errorResponseBody = objectMapper.writeValueAsString(errorResponse);

        stubFor(post(urlPathEqualTo("/paymentCards"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_BAD_REQUEST)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withHeader("CF-RAY", "testing-cf-ray")
                .withBody(errorResponseBody)));

        assertThrows(GarminPayApiException.class, () -> garminPayProxy.registerCard("mockOAuthToken", "mockEncryptedCardData"));
    }
}
