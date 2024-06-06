package com.garminpay.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import javax.crypto.SecretKey;

@Builder
@Getter
public class KeyExchangeDTO {
    @NonNull
    private SecretKey secretKey;

    @NonNull
    private String keyId;

    @NonNull
    private Boolean active;
}
