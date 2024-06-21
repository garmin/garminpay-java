package com.garminpay;

import com.garminpay.exception.GarminPayApiException;
import com.garminpay.model.dto.APIResponseDTO;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;

import java.io.IOException;

/**
 * APIClient class responsible for configuring the HTTP client and providing a singleton instance.
 */
public class APIClient {

    private static APIClient instance;
    private final CloseableHttpClient httpClient;

    private APIClient() {
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * Returns the singleton instance of APIClient.
     *
     * @return the APIClient instance
     */
    public static synchronized APIClient getInstance() {
        if (instance == null) {
            instance = new APIClient();
        }
        return instance;
    }

    /**
     * Executes a ClassicHttpRequest and returns an APIResponseDTO.
     * Works for all HTTP methods.
     *
     * @param request Request to execute
     * @return Response containing status code, headers and content
     */
    public APIResponseDTO send(ClassicHttpRequest request) {
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
