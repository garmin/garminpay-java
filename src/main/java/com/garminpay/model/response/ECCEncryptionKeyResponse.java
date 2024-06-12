package com.garminpay.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Value
@EqualsAndHashCode(callSuper = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@SuperBuilder
public class ECCEncryptionKeyResponse extends ErrorResponse {
    String keyId;
    String serverPublicKey;
    boolean active;
    String createdTs;
    String expirationTs;
}
