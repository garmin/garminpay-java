package com.garminpay.proxy;

import com.garminpay.BaseIT;
import com.garminpay.exception.GarminPayApiException;
import com.garminpay.model.HealthResponse;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.Base64;

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
}
