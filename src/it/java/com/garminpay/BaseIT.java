package com.garminpay;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.garminpay.proxy.GarminPayProxy;
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

        // Use reflection to change the baseApiUrl and authURL of GarminPayProxy to WireMock's base URL
        Field baseApiUrlField = GarminPayProxy.class.getDeclaredField("baseApiUrl");
        baseApiUrlField.setAccessible(true);
        baseApiUrlField.set(null, "http://localhost:" + wireMockServer.port());

        Field authUrlField = GarminPayProxy.class.getDeclaredField("authUrl");
        authUrlField.setAccessible(true);
        authUrlField.set(null, "http://localhost:" + wireMockServer.port());
    }

    @AfterAll
    static void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
