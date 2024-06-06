package com.garminpay.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garminpay.BaseIT;
import com.garminpay.TestUtils;
import com.garminpay.exception.GarminPayApiException;
import com.garminpay.model.HealthResponse;
import com.garminpay.model.KeyExchangeDTO;
import com.garminpay.model.response.ECCEncryptionKey;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.Base64;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


class GarminPayProxyIT extends BaseIT {

    private final GarminPayProxy garminPayProxy = new GarminPayProxy();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void canGetRootEndpoint200Response() {
        WireMock.stubFor(get(urlPathEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"ok\"}")));

        HttpResponse<String> response = garminPayProxy.getRootEndpoint(HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("{\"status\":\"ok\"}", response.body());
    }

    @Test
    void cannotGetRootEndpoint404Response() {
        WireMock.stubFor(get(urlPathEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"Not Found\"}")));

        HttpResponse<String> response = garminPayProxy.getRootEndpoint(HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertEquals("{\"error\":\"Not Found\"}", response.body());
    }

    @Test
    void cannotGetRootEndpoint502Response() {
        WireMock.stubFor(get(urlPathEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(502)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"Bad Gateway\"}")));

        HttpResponse<String> response = garminPayProxy.getRootEndpoint(HttpResponse.BodyHandlers.ofString());

        assertEquals(502, response.statusCode());
        assertEquals("{\"error\":\"Bad Gateway\"}", response.body());
    }

    @Test
    void canGetHealthStatus(){
        WireMock.stubFor(get(urlPathEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"ok\"}")));

        HealthResponse healthResponse = garminPayProxy.getHealthStatus();

        assertNotNull(healthResponse, "Health response should not be null");
        assertEquals("ok", healthResponse.getStatus(), "Health status should be 'ok'");
    }

    @Test
    void canHandle502ResponseFromGetHealthStatus() {
        WireMock.stubFor(get(urlPathEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(502)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"Bad Gateway\"}")));

        assertThrows(GarminPayApiException.class, () -> garminPayProxy.getHealthStatus());
    }

    @Test
    void canGenerateOAuthAccessToken() {
        String clientID = "testClientID";
        String clientSecret = "testClientSecret";

        stubFor(post(urlPathEqualTo("/oauth/token"))
            .withHeader("Authorization", equalTo("Basic " + Base64.getEncoder().encodeToString((clientID + ":" + clientSecret).getBytes())))
            .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"access_token\":\"testToken\"}")));

        String token = garminPayProxy.generateOAuthAccessToken(clientID, clientSecret);
        assertEquals("testToken", token);
    }

    @Test
    void canHandle401ResponseFromGenerateOAuthAccessToken() {
        String clientID = "test-client-id";
        String clientSecret = "test-client-secret";


        stubFor(post(urlPathEqualTo("/oauth/token"))
            .withHeader("Authorization", equalTo("Basic " + Base64.getEncoder().encodeToString((clientID + ":" + clientSecret).getBytes())))
            .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
            .withRequestBody(equalTo("grant_type=client_credentials"))
            .willReturn(aResponse()
                .withStatus(401) // Unauthorized
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"invalid_client\", \"error_description\":\"Client authentication failed\"}")));


        assertThrows(GarminPayApiException.class, () -> garminPayProxy.generateOAuthAccessToken(clientID, clientSecret));
    }

    @Test
    @SneakyThrows(JsonProcessingException.class)
    void canCompleteKeyExchange() {
        String mockOauthToken = "mockAccessToken";

        ECCEncryptionKey mockKeyResponse = ECCEncryptionKey.builder()
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

        ECCEncryptionKey eccEncryptionKeyResponse = garminPayProxy.exchangeKeys(mockOauthToken, TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY);
        assertNotNull(eccEncryptionKeyResponse);
    }

    @Test
    @SneakyThrows(JsonProcessingException.class)
    void canHandle400ResponseFromKeyExchange() {
        String mockOauthToken = "mockAccessToken";
        String errorMessage = "Simulated error message from key exchange endpoint";

        Map<String, String> body = new HashMap<>();
        body.put("error", errorMessage);

        String responseBody = objectMapper.writeValueAsString(body);

        stubFor(post(urlPathEqualTo("/config/encryptionKeys"))
            .willReturn(aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)));

        assertThrows(GarminPayApiException.class, () -> {
            garminPayProxy.exchangeKeys(mockOauthToken, TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY);
        });
    }
}
