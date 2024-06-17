package com.garminpay.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garminpay.BaseIT;
import com.garminpay.TestUtils;
import com.garminpay.exception.GarminPayApiException;
import com.garminpay.model.response.HealthResponse;
import com.garminpay.model.response.ECCEncryptionKeyResponse;
import com.garminpay.model.response.OAuthTokenResponse;
import com.garminpay.model.response.RootResponse;
import com.garminpay.model.response.PaymentCardDeepLinkResponse;
import com.github.tomakehurst.wiremock.client.WireMock;
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

    private final GarminPayProxy garminPayProxy = new GarminPayProxy();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void canGetRootEndpoint() throws JsonProcessingException {
        Map<String, RootResponse.HalLink> mockLinks = Collections.singletonMap(
            "self", RootResponse.HalLink.builder().href("https://localhost/").build()
        );

        RootResponse mockRootResponse = RootResponse.builder()
            .links(mockLinks)
            .build();

        String responseBody = objectMapper.writeValueAsString(mockRootResponse);

        WireMock.stubFor(get(urlPathEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)));

        RootResponse response = garminPayProxy.getRootEndpoint();

        assertNotNull(response);
        assertNotNull(response.getLinks().get("self"));
        assertEquals("https://localhost/", response.getLinks().get("self").getHref());
    }

    @Test
    void canHandle404ResponseFromRootEndpoint() {
        WireMock.stubFor(get(urlPathEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"path\":\"/\", \"status\":404, \"message\":\"Not Found\"}")));

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, garminPayProxy::getRootEndpoint);

        assertEquals("/", exception.getPath());
        assertEquals(HttpStatus.SC_NOT_FOUND, exception.getStatus());
        assertEquals("Not Found", exception.getMessage());
    }

    @Test
    void canHandle502ResponseFromRootEndpoint() {
        WireMock.stubFor(get(urlPathEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(502)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"path\":\"/\", \"status\":502, \"message\":\"Bad Gateway\"}")));

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, garminPayProxy::getRootEndpoint);

        assertEquals("/", exception.getPath());
        assertEquals(HttpStatus.SC_BAD_GATEWAY, exception.getStatus());
        assertEquals("Bad Gateway", exception.getMessage());
    }

    @Test
    void canGetHealthStatus() throws JsonProcessingException {
        HealthResponse mockHealthResponse = HealthResponse.builder()
            .healthStatus("OK")
            .build();

        String responseBody = objectMapper.writeValueAsString(mockHealthResponse);

        WireMock.stubFor(get(urlPathEqualTo("/health"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)));

        HealthResponse health = garminPayProxy.getHealthStatus();

        assertNotNull(health);
        assertEquals("OK", health.getHealthStatus());
    }

    @Test
    void canHandle502ResponseFromGetHealthStatus() {
        WireMock.stubFor(get(urlPathEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(502)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"path\":\"/health\", \"status\":502, \"message\":\"Bad Gateway\"}")));

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, garminPayProxy::getHealthStatus);

        assertEquals("/health", exception.getPath());
        assertEquals(HttpStatus.SC_BAD_GATEWAY, exception.getStatus());
        assertEquals("Bad Gateway", exception.getMessage());
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
            .withHeader("Authorization", equalTo("Basic " + Base64.getEncoder().encodeToString((clientID + ":" + clientSecret).getBytes())))
            .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)));

        String token = garminPayProxy.getOAuthAccessToken(clientID, clientSecret);
        assertEquals("testToken", token);
    }

    @Test
    void canHandle401ResponseFromGetOAuthAccessToken() {
        String clientID = "test-client-id";
        String clientSecret = "test-client-secret";

        stubFor(post(urlPathEqualTo("/oauth/token"))
            .withHeader("Authorization", equalTo("Basic " + Base64.getEncoder().encodeToString((clientID + ":" + clientSecret).getBytes())))
            .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
            .willReturn(aResponse()
                .withStatus(401)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"path\":\"/oauth/token\", \"status\":401, \"message\":\"Unauthorized\"}")));

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> garminPayProxy.getOAuthAccessToken(clientID, clientSecret));

        assertEquals("/oauth/token", exception.getPath());
        assertEquals(HttpStatus.SC_UNAUTHORIZED, exception.getStatus());
        assertEquals("Unauthorized", exception.getMessage());
    }

    @Test
    void canCompleteKeyExchange() throws JsonProcessingException {
        String mockOauthToken = "mockAccessToken";

        ECCEncryptionKeyResponse mockKeyResponse = ECCEncryptionKeyResponse.builder()
            .keyId(UUID.randomUUID().toString())
            .active(true)
            .serverPublicKey(TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY)
            .build();

        String responseBody = objectMapper.writeValueAsString(mockKeyResponse);

        stubFor(post(urlPathEqualTo("/config/encryptionKeys"))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)));

        ECCEncryptionKeyResponse eccEncryptionKeyResponse = garminPayProxy.exchangeKeys(mockOauthToken, TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY);
        assertNotNull(eccEncryptionKeyResponse);
    }

    @Test
    void canHandle400ResponseFromKeyExchange() {
        String mockOauthToken = "mockAccessToken";

        stubFor(post(urlPathEqualTo("/config/encryptionKeys"))
            .willReturn(aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"path\":\"/config/encryptionKeys\", \"status\":400, \"message\":\"Bad Request\"}")));

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> garminPayProxy.exchangeKeys(mockOauthToken, TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY));

        assertEquals("/config/encryptionKeys", exception.getPath());
        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatus());
        assertEquals("Bad Request", exception.getMessage());
    }

    @Test
    void canRegisterCard() throws JsonProcessingException {

        PaymentCardDeepLinkResponse paymentCardDeepLinkResponse = PaymentCardDeepLinkResponse.builder()
            .deepLinkUrl("Deep link URL")
            .build();

        String paymentCardDeepLinkResponseBody = objectMapper.writeValueAsString(paymentCardDeepLinkResponse);

        stubFor(post(urlPathEqualTo("/paymentCards"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(paymentCardDeepLinkResponseBody)));

        PaymentCardDeepLinkResponse deepLinkResponse = garminPayProxy.registerCard("mockOAuthToken", "mockEncryptedCardData");

        assertNotNull(deepLinkResponse);
        assertNotNull(deepLinkResponse.getDeepLinkUrl());
    }

    @Test
    void canHandleOAuthFailureWhenRegisterCard() throws JsonProcessingException {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "invalid_client");

        String errorResponseBody = objectMapper.writeValueAsString(errorResponse);

        stubFor(post(urlPathEqualTo("/paymentCards"))
            .willReturn(aResponse()
                .withStatus(401)
                .withHeader("Content-Type", "application/json")
                .withBody(errorResponseBody)));

        assertThrows(GarminPayApiException.class, () -> garminPayProxy.registerCard("mockOAuthToken", "mockEncryptedCardData"));
    }
}
