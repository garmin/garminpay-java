package com.garminpay.proxy;

import com.garminpay.APIClient;

import java.net.http.HttpResponse;

/**
 * GarminPayProxy class responsible for calling the Garmin Pay API.
 */
public class GarminPayProxy {
    private final APIClient apiClient;

    /**
     * GarminPayProxy constructor.
     */
    public GarminPayProxy() {
        this.apiClient = APIClient.getInstance();
    }

    /**
     * Retrieves the root endpoint of the Garmin Pay API.
     *
     * @param bodyHandler the body handler to handle the response body
     * @param <T>         the type of the response body
     * @return HttpResponse<T> containing the response from the Garmin Pay API.
     */
    public <T> HttpResponse<T> getRootEndpoint(HttpResponse.BodyHandler<T> bodyHandler) {
        return apiClient.get("/", bodyHandler);
    }
}
