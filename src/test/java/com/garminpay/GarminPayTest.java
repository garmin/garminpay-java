package com.garminpay;

import com.garminpay.exception.GarminPayCredentialsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit Tests for GarminPay Initialization
 */
class GarminPayTest {
    @Test
    void cannotInitializeIfNull() {
        // Assert that initializing with null clientId throws an exception
        assertThrows(GarminPayCredentialsException.class, () -> {
            GarminPay.initialize(null, "testClientSecret");
        });

        // Assert that initializing with null clientSecret throws an exception
        assertThrows(GarminPayCredentialsException.class, () -> {
            GarminPay.initialize("testClientId", null);
        });

        // Assert that initializing with both null clientId and clientSecret throws an exception
        assertThrows(GarminPayCredentialsException.class, () -> {
            GarminPay.initialize(null, null);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void cannotInitializeWithInvalidClientId(String invalidClientId) {
        // Assert that initializing with invalid clientId throws an exception
        assertThrows(GarminPayCredentialsException.class, () -> {
            GarminPay.initialize(invalidClientId, "testClientSecret");
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void cannotInitializeWithInvalidClientSecret(String invalidClientSecret) {
        // Assert that initializing with invalid clientSecret throws an exception
        assertThrows(GarminPayCredentialsException.class, () -> {
            GarminPay.initialize("testClientId", invalidClientSecret);
        });
    }
}
