package com.garminpay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertThrows;

final class GarminPayClientTest {
    @Test
    void cannotConstructWithNullValues() {
        assertThrows(IllegalArgumentException.class, () -> new GarminPayClient(null, "testClientSecret"));

        assertThrows(IllegalArgumentException.class, () -> new GarminPayClient("testClientId", null));

        assertThrows(IllegalArgumentException.class, () -> new GarminPayClient(null, null));
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
