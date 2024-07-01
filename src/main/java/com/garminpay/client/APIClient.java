package com.garminpay.client;

import com.garminpay.exception.GarminPayApiException;
import com.garminpay.model.dto.APIResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;

import java.io.IOException;

@Slf4j
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
        log.debug("Executing a {} request to path {}", request.getMethod(), request.getPath());
        try {
            return httpClient.execute(request, response -> APIResponseDTO.fromHttpResponse(response, request.getPath()));
        } catch (IOException e) {
            log.warn("Encountered an error while executing a {} request to path {}", request.getMethod(), request.getPath(), e);
            throw new GarminPayApiException(
                request.getPath(),
                "HttpClient failed to execute request"
            );
        }
    }
}
