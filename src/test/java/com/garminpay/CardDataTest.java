package com.garminpay;

import com.garminpay.model.Address;
import com.garminpay.model.CardData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CardDataTest {
    private static final Address TESTING_ADDRESS = Address.builder()
        .name("Billing Address")
        .street1("123 Main St")
        .city("Anytown")
        .state("CO")
        .postalCode("12345")
        .countryCode("US")
        .build();

    @Test
    void canCreateCardDataObject() {
        CardData cardData = CardData.builder()
            .pan("1234567890123456")
            .cvv("123")
            .expMonth(12)
            .expYear(2025)
            .name("John Doe")
            .address(TESTING_ADDRESS)
            .build();

        assertNotNull(cardData);
    }

    @Test
    void cannotCreateCardObjectWithoutPan() {
        assertThrows(IllegalArgumentException.class, () -> CardData.builder().build());
    }
}
