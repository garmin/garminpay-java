package com.garminpay.model.response;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
public class RegisterCardResponse {
    String deepLinkUrl;
    String pushId;
}
