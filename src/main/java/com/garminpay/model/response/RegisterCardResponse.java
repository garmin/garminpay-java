package com.garminpay.model.response;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Value
@Jacksonized
@Builder
public class RegisterCardResponse {
    Map<String, String> deepLinkUrls;
}
