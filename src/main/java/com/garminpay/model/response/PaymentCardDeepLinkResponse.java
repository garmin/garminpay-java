package com.garminpay.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@EqualsAndHashCode(callSuper = true)
@Value
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@SuperBuilder
public class PaymentCardDeepLinkResponse extends ErrorResponse {
    @NonNull
    String deepLinkUrl;
}
