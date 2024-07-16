package com.garminpay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garminpay.model.response.ExchangeKeysResponse;
import com.garminpay.model.response.OAuthTokenResponse;
import com.garminpay.model.response.RegisterCardResponse;
import com.garminpay.proxy.GarminPayProxy;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GarminPayServiceIT extends BaseIT {
    private final GarminPayProxy garminPayProxy = new GarminPayProxy(BaseIT.client, TESTING_URL);
    private final GarminPayService garminPayService = new GarminPayService(garminPayProxy);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void canRegisterCard() throws JsonProcessingException {
        String clientID = "testClientID";
        String clientSecret = "testClientSecret";
        OAuthTokenResponse oAuthToken = OAuthTokenResponse.builder()
            .accessToken("testToken")
            .build();

        ExchangeKeysResponse eccEncryptionKey = ExchangeKeysResponse.builder()
            .keyId(UUID.randomUUID().toString())
            .active(true)
            .serverPublicKey(TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY)
            .createdTs(Instant.now().toString())
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


        String deepLink = garminPayService.registerCard(TestUtils.TESTING_CARD_DATA);
        garminPayService.registerCard(TestUtils.TESTING_CARD_DATA);

        assertEquals(deepLink, registerCardResponse.getDeepLinkUrl());
    }

    @Test
    void canRegisterCardWithRefreshedKeys() throws JsonProcessingException {
        String clientID = "testClientID";
        String clientSecret = "testClientSecret";
        OAuthTokenResponse oAuthToken = OAuthTokenResponse.builder()
            .accessToken("testToken")
            .build();

        // Returns a key that our SDK will mark as overdue
        // It will only check that key a 2nd time, assumes when it gets a new key that it is valid
        ExchangeKeysResponse eccEncryptionKey = ExchangeKeysResponse.builder()
            .keyId(UUID.randomUUID().toString())
            .active(true)
            .serverPublicKey(TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY)
            .createdTs(Instant.now().plus(Duration.ofHours(5)).toString())
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


        String deepLink = garminPayService.registerCard(TestUtils.TESTING_CARD_DATA);
        garminPayService.registerCard(TestUtils.TESTING_CARD_DATA);

        assertEquals(deepLink, registerCardResponse.getDeepLinkUrl());
        // Keys should have been refreshed b/c TS was invalid, expected 2 calls to this
        verify(exactly(2), postRequestedFor(urlPathEqualTo("/config/encryptionKeys")));
    }
}
