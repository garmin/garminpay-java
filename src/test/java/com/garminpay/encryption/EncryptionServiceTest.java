package com.garminpay.encryption;

import com.garminpay.TestUtils;
import com.nimbusds.jwt.EncryptedJWT;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EncryptionServiceTest {
    private final EncryptionService service = new EncryptionService();
    private final SecretKey secretKey = service.generateSharedSecret(TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY, TestUtils.TESTING_ENCODED_PRIVATE_ECC_KEY);

    @Test
    void canEncryptCardDataWithSecretKey() {
        String encryptedCardData = service.encryptCardData(TestUtils.TESTING_CARD_DATA, secretKey, TestUtils.TESTING_KEY_ID);

        assertNotNull(encryptedCardData);
        assertDoesNotThrow(() -> {
            EncryptedJWT.parse(encryptedCardData);
        });
    }

    @Test
    void cannotEncryptCardDataWithNullKey() {
        assertThrows(IllegalArgumentException.class, () -> service.encryptCardData(TestUtils.TESTING_CARD_DATA, null, TestUtils.TESTING_KEY_ID));
    }

    @Test
    void cannotEncryptCardDataWithNullCard() {
        assertThrows(IllegalArgumentException.class, () -> service.encryptCardData(null, secretKey, TestUtils.TESTING_KEY_ID));
    }

    @Test
    void cannotEncryptCardDataWithNullKeyId() {
        assertThrows(IllegalArgumentException.class, () -> service.encryptCardData(TestUtils.TESTING_CARD_DATA, secretKey, null));
    }
}
