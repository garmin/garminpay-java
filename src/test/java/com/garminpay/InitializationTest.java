package com.garminpay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit Tests for GarminPay Initialization
 */
public class InitializationTest {

    @BeforeEach
    public void setUp() {
        // Reset the GarminPay state before each test
        GarminPay.initialize("defaultClientId", "defaultClientSecret");
    }

    @Test
    public void canInitializeSDK() {
        String expectedClientId = "testClientId";
        String expectedClientSecret = "testClientSecret";

        GarminPay.initialize(expectedClientId, expectedClientSecret);

        // Assert that both clientId and clientSecret are set correctly
        assertEquals(expectedClientId, GarminPay.getClientId());
        assertEquals(expectedClientSecret, GarminPay.getClientSecret());
    }

    @Test
    public void canReturnClientId() {
        String expectedClientId = "testClientId";

        GarminPay.initialize(expectedClientId, "testClientSecret");
        // Assert that clientId is returned correctly
        assertEquals(expectedClientId, GarminPay.getClientId());
    }

    @Test
    public void canReturnClientSecret() {
        String expectedClientSecret = "testClientSecret";

        GarminPay.initialize("testClientId", expectedClientSecret);
        // Assert that clientSecret is returned correctly
        assertEquals(expectedClientSecret, GarminPay.getClientSecret());
    }

    @Test
    public void cannotInitializeIfNull() {
        // Assert that initializing with null clientId throws an exception
        assertThrows(IllegalArgumentException.class, () -> {
            GarminPay.initialize(null, "testClientSecret");
        });

        // Assert that initializing with null clientSecret throws an exception
        assertThrows(IllegalArgumentException.class, () -> {
            GarminPay.initialize("testClientId", null);
        });

        // Assert that initializing with both null clientId and clientSecret throws an exception
        assertThrows(IllegalArgumentException.class, () -> {
            GarminPay.initialize(null, null);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    public void cannotInitializeWithInvalidClientId(String invalidClientId) {
        // Assert that initializing with invalid clientId throws an exception
        assertThrows(IllegalArgumentException.class, () -> {
            GarminPay.initialize(invalidClientId, "testClientSecret");
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    public void cannotInitializeWithInvalidClientSecret(String invalidClientSecret) {
        // Assert that initializing with invalid clientSecret throws an exception
        assertThrows(IllegalArgumentException.class, () -> {
            GarminPay.initialize("testClientId", invalidClientSecret);
        });
    }
}