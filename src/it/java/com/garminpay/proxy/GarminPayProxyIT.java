package com.garminpay.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garminpay.BaseIT;
import com.garminpay.TestUtils;
import com.garminpay.exception.GarminPayApiException;
import com.garminpay.model.response.ErrorResponse;
import com.garminpay.model.response.ExchangeKeysResponse;
import com.garminpay.model.response.HalLink;
import com.garminpay.model.response.HealthResponse;
import com.garminpay.model.response.RegisterCardResponse;
import com.garminpay.model.response.RootResponse;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class GarminPayProxyIT extends BaseIT {
    private static final GarminPayProxy garminPayProxy = new GarminPayProxy(client, TESTING_URL);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void canGetRootEndpoint() throws JsonProcessingException {
        Map<String, HalLink> mockLinks = Collections.singletonMap(
            "self", HalLink.builder().href("https://testing/").build()
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
            .healthStatus("OK") // Add empty links to satisfy superclass requirements
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
    void canCompleteKeyExchange() throws JsonProcessingException {
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

        ExchangeKeysResponse exchangeKeysResponse = garminPayProxy.exchangeKeys(TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY);
        assertNotNull(exchangeKeysResponse);
    }

    @Test
    void canHandle400ResponseFromKeyExchange() throws JsonProcessingException {
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

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> garminPayProxy.exchangeKeys(TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY));

        assertEquals("/config/encryptionKeys", exception.getPath());
        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatus());
    }

    @Test
    void canRegisterCard() throws JsonProcessingException {
        Map<String, String> deepLinks = new HashMap<>();
        deepLinks.put("ios", "https://connect.garmin.com/payment/push/ios/provision?pushToken=abcdefgh");
        deepLinks.put("android", "https://connect.garmin.com/payment/push/android/provision?pushToken=abcdefgh");
        RegisterCardResponse mockRegisterCardResponse = RegisterCardResponse.builder()
            .deepLinkUrls(deepLinks)
            .build();

        String registerCardResponseBody = objectMapper.writeValueAsString(mockRegisterCardResponse);

        stubFor(post(urlPathEqualTo("/paymentCards"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withHeader("CF-RAY", "testing-cf-ray")
                .withBody(registerCardResponseBody)));

        RegisterCardResponse registerCardResponse = garminPayProxy.registerCard("mockEncryptedCardData");

        assertNotNull(registerCardResponse.getDeepLinkUrls().get("ios"));
        assertNotNull(registerCardResponse.getDeepLinkUrls().get("android"));
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

        assertThrows(GarminPayApiException.class, () -> garminPayProxy.registerCard("mockEncryptedCardData"));
    }
}
