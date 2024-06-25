package com.garminpay.client;

import com.garminpay.exception.GarminPayApiException;
import com.garminpay.model.dto.APIResponseDTO;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;

import java.io.IOException;

public class APIClient implements Client {
    private final CloseableHttpClient httpClient;

    /**
     * Constructs a new APIClient.
     */
    public APIClient() {
        this.httpClient = HttpClients.createDefault();
    }

    @Override
    public APIResponseDTO executeRequest(ClassicHttpRequest request) {
        try {
            return httpClient.execute(request, response -> APIResponseDTO.fromHttpResponse(response, request.getPath()));
        } catch (IOException e) {
            throw new GarminPayApiException(
                request.getPath(),
                "HttpClient failed to execute request"
            );
        }
    }
}
