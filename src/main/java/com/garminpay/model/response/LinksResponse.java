/*
 * Copyright 2024 Garmin International, Inc.
 * Licensed under the Garmin Pay Software License Agreement; you
 * may not use this file except in compliance with the Garmin Pay Software License Agreement.
 */
package com.garminpay.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public abstract class LinksResponse {
    /**
     * The links interact with Garmin Pay API.
     */
    @JsonProperty("_links")
    private Map<String, HalLink> links;

}
