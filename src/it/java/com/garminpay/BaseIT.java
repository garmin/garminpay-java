package com.garminpay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garminpay.client.APIClient;
import com.garminpay.client.Client;
import com.garminpay.client.RefreshableOauthClient;
import com.garminpay.model.response.OAuthTokenResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import wiremock.org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

public class BaseIT {

    protected static String TESTING_URL;
    protected static Client client;
    private static WireMockServer wireMockServer;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setUp() throws JsonProcessingException {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        TESTING_URL = "http://localhost:" + wireMockServer.port();

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

        Client baseClient = new APIClient();
        client = new RefreshableOauthClient(baseClient, ("client_id:client_secret").getBytes(StandardCharsets.UTF_8), TESTING_URL + "/oauth/token");
    }

    @AfterAll
    static void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    public static boolean checkForHeader(Header expectedHeader, Header[] actualHeaders) {
        return Arrays.stream(actualHeaders)
            .anyMatch(
                h -> StringUtils.equalsIgnoreCase(expectedHeader.getName(), h.getName())
                    && StringUtils.equalsIgnoreCase(expectedHeader.getValue(), h.getValue())
            );
    }
}
