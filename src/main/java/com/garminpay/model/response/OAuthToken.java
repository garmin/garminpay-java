package com.garminpay.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;


@Value
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class OAuthToken {
    /**
     * The access token issued by the authorization server.
     */
    @JsonProperty("access_token")
    String accessToken;
}
