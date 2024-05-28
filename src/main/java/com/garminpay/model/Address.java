package com.garminpay.model;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Address {
    /**
     * The name associated with the address.
     */
    String name;

    /**
     * The first line of the street address.
     */
    String street1;

    /**
     * The second line of the street address.
     */
    String street2;

    /**
     * The third line of the street address.
     */
    String street3;

    /**
     * The city of the address.
     */
    String city;

    /**
     * The state or region of the address.
     */
    String state;

    /**
     * The postal code of the address.
     */
    String postalCode;

    /**
     * The country code of the address.
     */
    String countryCode;
}
