package com.garminpay.client;

import com.garminpay.exception.GarminPayApiException;
import com.garminpay.model.SDKVersion;
import com.garminpay.model.dto.APIResponseDTO;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.message.BasicHeader;

@Slf4j
public class APIClient implements Client {
    private final CloseableHttpClient httpClient;
    private final BasicHeader versionHeader;

    /**
     * Constructs a new APIClient.
     */
    public APIClient() {
        this.httpClient = HttpClients.createDefault();
        this.versionHeader = new BasicHeader("GP-SDK-Version", SDKVersion.VERSION);
        log.debug("Added version header: {}", versionHeader);
    }

    /**
     * Executes the given HTTP request and returns the response.
     * Adds the version header to each request.
     *
     * @param request the HTTP request to execute
     * @return the API response
     * @throws GarminPayApiException if an error occurs during request execution
     */
    @Override
    public APIResponseDTO executeRequest(ClassicHttpRequest request) {
        log.debug("Executing a {} request to path {}", request.getMethod(), request.getPath());
        // Adds version header to request
        request.addHeader(versionHeader);

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
