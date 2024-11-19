/*
 * Copyright 2024 Garmin International, Inc.
 * Licensed under the Garmin Pay Software License Agreement; you
 * may not use this file except in compliance with the Garmin Pay Software License Agreement.
 */
package com.garminpay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garminpay.client.APIClient;
import com.garminpay.client.Client;
import com.garminpay.client.RefreshableOauthClient;
import com.garminpay.model.response.HalLink;
import com.garminpay.model.response.OAuthTokenResponse;
import com.garminpay.model.response.RootResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

;

public class BaseIT {

    protected static final Map<String, HalLink> links = new HashMap<>();
    protected static final String VERSION_HEADER_NAME = "X-GP-SDK-Version";
    protected static final String DEEPLINK_URL = "https://connect.garmin.com/payment/directpush?pushToken=randomjws";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    protected static String TESTING_URL;
    protected static Client client;
    protected static WireMockServer wireMockServer;

    @BeforeAll
    static void setUp() throws JsonProcessingException {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        TESTING_URL = "http://localhost:" + wireMockServer.port();

        links.put("self", HalLink.builder().href(TESTING_URL).build());
        links.put("health", HalLink.builder().href(TESTING_URL + "/health").build());
        links.put("encryptionKeys", HalLink.builder().href(TESTING_URL + "/config/encryptionKeys").build());
        links.put("paymentCards", HalLink.builder().href(TESTING_URL + "/paymentCards").build());

        RootResponse rootResponse = RootResponse.builder()
            .links(new HashMap<>(links))
            .build();

        OAuthTokenResponse mockToken = OAuthTokenResponse.builder()
            .accessToken("testToken")
            .build();

        // Stub for initial authentication
        stubFor(post(urlPathEqualTo("/oauth/token"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withHeader("CF-RAY", "testing-cf-ray")
                .withBody(objectMapper.writeValueAsString(mockToken))
            )
        );

        // Stub for initial root link fetch
        stubFor(get(urlPathEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withHeader("CF-RAY", "testing-cf-ray")
                .withBody(objectMapper.writeValueAsString(rootResponse))
            )
        );

        Client baseClient = new APIClient();
        client = new RefreshableOauthClient(baseClient, ("client_id:client_secret").getBytes(StandardCharsets.UTF_8), TESTING_URL + "/oauth/token");
    }

    @AfterAll
    static void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
