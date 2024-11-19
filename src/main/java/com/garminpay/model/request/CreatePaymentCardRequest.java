/*
 * Copyright 2024 Garmin International, Inc.
 * Licensed under the Garmin Pay Software License Agreement; you
 * may not use this file except in compliance with the Garmin Pay Software License Agreement.
 */
package com.garminpay.model.request;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class CreatePaymentCardRequest {
    String encryptedData;
}
