/*
 * Copyright 2024 Garmin International, Inc.
 * Licensed under the Garmin Pay Software License Agreement; you
 * may not use this file except in compliance with the Garmin Pay Software License Agreement.
 */
package com.garmin.garminpay.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
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
