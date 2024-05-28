package com.garminpay.proxy;

import com.garminpay.BaseIT;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;


import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GarminPayProxyIT extends BaseIT {

    private final GarminPayProxy garminPayProxy = new GarminPayProxy();

    @Test
    void canGetRootEndpoint200Response() {
        WireMock.stubFor(get(urlEqualTo("/"))
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
        WireMock.stubFor(get(urlEqualTo("/"))
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
        WireMock.stubFor(get(urlEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(502)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"Bad Gateway\"}")));

        HttpResponse<String> response = garminPayProxy.getRootEndpoint(HttpResponse.BodyHandlers.ofString());

        assertEquals(502, response.statusCode());
        assertEquals("{\"error\":\"Bad Gateway\"}", response.body());
    }
}
