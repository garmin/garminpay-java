package com.garminpay;

import com.garminpay.exception.GarminPayApiException;
import com.garminpay.util.JsonBodyPublisher;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * APIClient class responsible for configuring the HTTP client and providing a singleton instance.
 */
public class APIClient {

    private static APIClient instance;
    private final HttpClient httpClient;

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
     * @param url         the full URL to send the GET request to
     * @param bodyHandler the body handler to handle the response body
     * @param <T>         the type of the response body
     * @return HttpResponse<T> containing the response from the server
     */
    public <T> HttpResponse<T> get(String url, HttpResponse.BodyHandler<T> bodyHandler) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .GET()
            .build();

        try {
            return httpClient.send(request, bodyHandler);
        } catch (IOException | InterruptedException e) {
            throw new GarminPayApiException("Failed to get URL: " + url, e);
        }
    }

    /**
     * Sends a POST request to the specified endpoint with the provided body and returns the response.
     *
     * @param url         the full URL to send the POST request to
     * @param body        the body of the POST request as an object
     * @param headers     the headers for the POST request
     * @param bodyHandler the body handler to handle the response body
     * @param <T>         the type of the response body
     * @return HttpResponse<T> containing the response from the server
     */
    public <T> HttpResponse<T> post(String url, Object body, Map<String, String> headers,
                                    HttpResponse.BodyHandler<T> bodyHandler) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .POST(JsonBodyPublisher.ofObject(body));

        headers.forEach(requestBuilder::header);

        HttpRequest request = requestBuilder.build();

        try {
            HttpResponse<T> response = httpClient.send(request, bodyHandler);
            if (response.statusCode() != 200) {
                throw new GarminPayApiException("Failed to post to URL: " + url + ", status code: " + response.statusCode());
            }
            return response;
        } catch (IOException | InterruptedException e) {
            throw new GarminPayApiException("Failed to post to URL: " + url, e);
        }
    }
}
