package com.garminpay.encryption;

import com.garminpay.TestUtils;
import com.garminpay.exception.GarminPayEncryptionException;
import com.nimbusds.jwt.EncryptedJWT;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EncryptionServiceTest {
    private final String mockPublicECKey = TestUtils.TESTING_PUBLIC_ECC_KEY_STRING;

    @Test
    void canEncryptCardDataWithEC() {
        EncryptionService service = new EncryptionService();
        String encryptedCardData = service.encryptCardData(TestUtils.TESTING_CARD_DATA, mockPublicECKey);

        assertDoesNotThrow(() -> {
            EncryptedJWT.parse(encryptedCardData);
        });
    }

    @Test
    void cannotEncryptCardDataWithInvalidKey() {
        EncryptionService service = new EncryptionService();
        String mockInvalidPublicKey = "Invalid Public Key";

        assertThrows(GarminPayEncryptionException.class, () -> service.encryptCardData(TestUtils.TESTING_CARD_DATA, mockInvalidPublicKey));
    }

    @Test
    void cannotEncryptCardDataWithNullKey() {
        EncryptionService service = new EncryptionService();

        assertThrows(IllegalArgumentException.class, () -> service.encryptCardData(TestUtils.TESTING_CARD_DATA, null));
    }

    @Test
    void cannotEncryptCardDataWithNullCard() {
        EncryptionService service = new EncryptionService();

        assertThrows(IllegalArgumentException.class, () -> service.encryptCardData(null, mockPublicECKey));
    }
}
