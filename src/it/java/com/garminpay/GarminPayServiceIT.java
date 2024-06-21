package com.garminpay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garminpay.model.response.ExchangeKeysResponse;
import com.garminpay.model.response.OAuthTokenResponse;
import com.garminpay.model.response.RegisterCardResponse;
import com.garminpay.proxy.GarminPayProxy;
import lombok.SneakyThrows;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GarminPayServiceIT extends BaseIT {
    private final GarminPayProxy garminPayProxy = new GarminPayProxy("http://localhost:" + BaseIT.wireMockServer.port(), "http://localhost:" + BaseIT.wireMockServer.port());
    private final GarminPayService garminPayService = new GarminPayService(garminPayProxy);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @SneakyThrows(JsonProcessingException.class)
    void canRegisterCard() {
        String clientID = "testClientID";
        String clientSecret = "testClientSecret";
        OAuthTokenResponse oAuthToken = OAuthTokenResponse.builder()
            .accessToken("testToken")
            .build();

        ExchangeKeysResponse eccEncryptionKey = ExchangeKeysResponse.builder()
            .keyId(UUID.randomUUID().toString())
            .active(true)
            .serverPublicKey(TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY)
            .build();

        RegisterCardResponse registerCardResponse = RegisterCardResponse.builder()
            .deepLinkUrl("Deep link URL")
            .build();


        stubFor(post(urlPathEqualTo("/oauth/token"))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Basic " + Base64.getEncoder().encodeToString((clientID + ":" + clientSecret).getBytes())))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(ContentType.APPLICATION_FORM_URLENCODED.toString()))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withHeader("CF-RAY", "testing-cf-ray")
                .withBody(objectMapper.writeValueAsString(oAuthToken))));

        stubFor(post(urlPathEqualTo("/config/encryptionKeys"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_CREATED)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withHeader("CF-RAY", "testing-cf-ray")
                .withBody(objectMapper.writeValueAsString(eccEncryptionKey))));

        stubFor(post(urlPathEqualTo("/paymentCards"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withHeader("CF-RAY", "testing-cf-ray")
                .withBody(objectMapper.writeValueAsString(registerCardResponse))));


        String deepLink = garminPayService.registerCard(TestUtils.TESTING_CARD_DATA, clientID, clientSecret);

        assertEquals(deepLink, registerCardResponse.getDeepLinkUrl());
    }
}
