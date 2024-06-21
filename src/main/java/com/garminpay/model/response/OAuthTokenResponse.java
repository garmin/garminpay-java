package com.garminpay.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;


@Value
@Jacksonized
@Builder
public class OAuthTokenResponse {
    /**
     * The access token issued by the authorization server.
     */
    @JsonProperty("access_token")
    String accessToken;
}
