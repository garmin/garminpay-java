package com.garminpay;

import com.garminpay.exception.GarminPayApiException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * APIClient class responsible for configuring the HTTP client and providing a singleton instance.
 */
public class APIClient {

    private static APIClient instance;
    private final HttpClient httpClient;
    private static String baseApiUrl = "https://api.qa.fitpay.ninja";

    private APIClient() {
        this.httpClient = HttpClient.newBuilder().build();
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
     * Sends a GET request to the specified endpoint and returns the response.
     *
     * @param endpoint    the endpoint to send the GET request to
     * @param bodyHandler the body handler to handle the response body
     * @param <T>         the type of the response body
     * @return HttpResponse<T> containing the response from the server
     */
    public <T> HttpResponse<T> get(String endpoint, HttpResponse.BodyHandler<T> bodyHandler) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseApiUrl + endpoint))
            .header("Accept", "application/json")
            .GET()
            .build();

        try {
            return httpClient.send(request, bodyHandler);
        } catch (IOException | InterruptedException e) {
            throw new GarminPayApiException("Failed to get endpoint: " + endpoint, e);
        }
    }
}
