package com.garminpay.model.response;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder
public class ExchangeKeysResponse {
    String keyId;
    String serverPublicKey;
    boolean active;
    String createdTs;
    String expirationTs;
}
