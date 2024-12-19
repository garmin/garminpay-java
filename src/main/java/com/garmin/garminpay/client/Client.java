/*
 * Copyright 2024 Garmin International, Inc.
 * Licensed under the Garmin Pay Software License Agreement; you
 * may not use this file except in compliance with the Garmin Pay Software License Agreement.
 */
package com.garmin.garminpay.client;

import org.apache.hc.core5.http.ClassicHttpRequest;

import com.garmin.garminpay.model.dto.APIResponseDTO;

public interface Client {

    /**
     * Executes a request and returns an APIResponseDTO object with the response information.
     *
     * @param request request to be executed
     * @return response DTO object containing status, body, etc.
     */
    APIResponseDTO executeRequest(ClassicHttpRequest request);

}
