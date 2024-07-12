package com.garminpay.model.response;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@EqualsAndHashCode(callSuper = true)
@Value
@Jacksonized
@SuperBuilder
public class ExchangeKeysResponse extends LinksResponse {
    String keyId;
    String serverPublicKey;
    boolean active;
    String createdTs;
    String expirationTs;
}
