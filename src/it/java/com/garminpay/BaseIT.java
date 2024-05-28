package com.garminpay;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.lang.reflect.Field;

public class BaseIT {

    protected static WireMockServer wireMockServer;

    @BeforeAll
    static void setUp() throws NoSuchFieldException, IllegalAccessException {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        // Use reflection to change the baseUrl of APIClient to WireMock's base URL
        Field baseUrlField = APIClient.class.getDeclaredField("baseApiUrl");
        baseUrlField.setAccessible(true);
        baseUrlField.set(null, "http://localhost:" + wireMockServer.port());
    }

    @AfterAll
    static void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
