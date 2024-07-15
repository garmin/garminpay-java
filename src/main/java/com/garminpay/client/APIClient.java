package com.garminpay.client;

import com.garminpay.exception.GarminPayApiException;
import com.garminpay.model.SDKVersion;
import com.garminpay.model.dto.APIResponseDTO;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.message.BasicHeader;

@Slf4j
public class APIClient implements Client {
    private final HttpClient httpClient;
    private final BasicHeader versionHeader;

    /**
     * Constructs a new APIClient with default settings.
     */
    public APIClient() {
        this(null); // Default constructor delegates to constructor with custom settings
    }

    /**
     * Constructs a new APIClient with a custom HttpClient or the default if null.
     * Custom HttpClient must use an HTTP/S configured proxy, not TCP, etc.
     *
     * @param httpClient the httpClient to use for requests
     */
    public APIClient(HttpClient httpClient) {
        if (httpClient != null) {
            this.httpClient = httpClient;
        } else {
            this.httpClient = HttpClients.createDefault();
        }
        this.versionHeader = new BasicHeader("GP-SDK-Version", SDKVersion.VERSION);
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
        log.debug("Added version header: {}", versionHeader);

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
