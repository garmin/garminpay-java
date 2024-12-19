/*
 * Copyright 2024 Garmin International, Inc.
 * Licensed under the Garmin Pay Software License Agreement; you
 * may not use this file except in compliance with the Garmin Pay Software License Agreement.
 */
package com.garmin.garminpay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.garmin.garminpay.GarminPayClient;
import com.garmin.garminpay.model.GarminPayCardData;

import static org.junit.jupiter.api.Assertions.assertThrows;

final class GarminPayClientTest {
    @Test
    void cannotConstructWithNullValues() {
        assertThrows(IllegalArgumentException.class, () -> new GarminPayClient(null, "testClientSecret"));

        assertThrows(IllegalArgumentException.class, () -> new GarminPayClient("testClientId", null));

        assertThrows(IllegalArgumentException.class, () -> new GarminPayClient(null, null));
    }

    @Test
    void cannotRegisterCardWithoutCallbackUrl() {
        GarminPayClient garminPayClient = new GarminPayClient("testClientId", "testClientSecret");
        GarminPayCardData cardData = GarminPayCardData.builder()
            .pan("123")
            .build();
        assertThrows(IllegalArgumentException.class, () -> garminPayClient.registerCard(cardData, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void cannotConstructWithInvalidClientId(String invalidClientId) {
        assertThrows(IllegalArgumentException.class, () -> new GarminPayClient(invalidClientId, "testClientSecret"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void cannotConstructWithInvalidClientSecret(String invalidClientSecret) {
        assertThrows(IllegalArgumentException.class, () -> new GarminPayClient("testClientId", invalidClientSecret));
    }
}
