package com.garminpay.model.request;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class CreatePaymentCardRequest {
    String encryptedData;
}
