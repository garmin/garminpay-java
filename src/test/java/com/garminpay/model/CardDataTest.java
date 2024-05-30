package com.garminpay.model;

import com.garminpay.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CardDataTest {
    @Test
    void canCreateCardDataObject() {
        CardData cardData = CardData.builder()
            .pan("1234567890123456")
            .cvv("123")
            .expMonth(12)
            .expYear(2025)
            .name("John Doe")
            .address(TestUtils.TESTING_ADDRESS)
            .build();

        assertNotNull(cardData);
    }

    @Test
    void cannotCreateCardObjectWithoutPan() {
        assertThrows(IllegalArgumentException.class, () -> CardData.builder().build());
    }
}
