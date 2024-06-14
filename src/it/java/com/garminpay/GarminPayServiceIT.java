package com.garminpay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garminpay.model.response.ECCEncryptionKeyResponse;
import com.garminpay.model.response.OAuthTokenResponse;
import com.garminpay.model.response.PaymentCardDeepLinkResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;;

class GarminPayServiceIT extends BaseIT {
    private final GarminPayService garminPayService = new GarminPayService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @SneakyThrows(JsonProcessingException.class)
    void canRegisterCard() {
        String clientID = "testClientID";
        String clientSecret = "testClientSecret";
        OAuthTokenResponse oAuthToken = OAuthTokenResponse.builder()
            .accessToken("testToken")
            .build();

        ECCEncryptionKeyResponse eccEncryptionKey = ECCEncryptionKeyResponse.builder()
            .keyId(UUID.randomUUID().toString())
            .active(true)
            .serverPublicKey(TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY)
            .build();

        PaymentCardDeepLinkResponse paymentCardDeepLinkResponse = PaymentCardDeepLinkResponse.builder()
            .deepLinkUrl("Deep link URL")
            .build();


        stubFor(post(urlPathEqualTo("/oauth/token"))
            .withHeader("Authorization", equalTo("Basic " + Base64.getEncoder().encodeToString((clientID + ":" + clientSecret).getBytes())))
            .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(oAuthToken))));

        stubFor(post(urlPathEqualTo("/config/encryptionKeys"))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(eccEncryptionKey))));

        stubFor(post(urlPathEqualTo("/paymentCards"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(paymentCardDeepLinkResponse))));


        String deepLink = garminPayService.registerCard(TestUtils.TESTING_CARD_DATA, clientID, clientSecret);

        assertEquals(deepLink, paymentCardDeepLinkResponse.getDeepLinkUrl());
    }
}
