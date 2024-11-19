/*
 * Copyright 2024 Garmin International, Inc.
 * Licensed under the Garmin Pay Software License Agreement; you
 * may not use this file except in compliance with the Garmin Pay Software License Agreement.
 */
package com.garminpay.client;

import com.garminpay.model.dto.APIResponseDTO;
import org.apache.hc.core5.http.ClassicHttpRequest;

public interface Client {

    /**
     * Executes a request and returns an APIResponseDTO object with the response information.
     *
     * @param request request to be executed
     * @return response DTO object containing status, body, etc.
     */
    APIResponseDTO executeRequest(ClassicHttpRequest request);

}
