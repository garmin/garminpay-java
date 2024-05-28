package com.garminpay.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Builder
@Value
public class CardData {
    /**
     * The primary account number (PAN) of the card.
     */
    @NonNull
    String pan;

    /**
     * The Card Verification Value (CVV) of the card.
     */
    String cvv;

    /**
     * The expiration month of the card.
     */
    Integer expMonth;

    /**
     * The expiration year of the card.
     */
    Integer expYear;

    /**
     * The name of the cardholder.
     */
    String name;

    /**
     * The billing address associated with the card.
     */
    Address address;
}
