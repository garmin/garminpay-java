package com.garminpay.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ECCEncryptionKey {
    String keyId;
    String serverPublicKey;
    boolean active;
    String createdTs;
    String expirationTs;
}
