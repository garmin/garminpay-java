package com.garminpay.proxy;

import com.garminpay.APIClient;
import com.garminpay.model.HealthResponse;
import com.garminpay.util.JsonBodyHandler;
import com.garminpay.exception.GarminPayApiException;
import com.garminpay.model.OAuthToken;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.net.http.HttpResponse;

/**
 * GarminPayProxy class responsible for calling the Garmin Pay API.
 */
public class GarminPayProxy {
    private final APIClient apiClient;
    public static String baseApiUrl = "https://api.qa.fitpay.ninja";
    public static String authUrl = "https://auth.qa.fitpay.ninja";

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
        String url = baseApiUrl + "/";
        return apiClient.get(url, bodyHandler);
    }

    /**
     * Retrieves the health status of the Garmin Pay API.
     *
     * @return HealthResponse containing the health status of the Garmin Pay API.
     */
    public HealthResponse getHealthStatus() {
        HttpResponse<HealthResponse> response = apiClient.get(baseApiUrl + "/health", new JsonBodyHandler<>(HealthResponse.class));
        if (response.statusCode() != 200) {
            throw new GarminPayApiException("Failed to get URL: " + baseApiUrl + "/health, status code: " + response.statusCode());
        }
        return response.body();
    }

    /**
     * Generates an OAuth2.0 access token using client credentials.
     *
     * @param clientID     The clientId securely provided during onboarding
     * @param clientSecret The client secret securely provided during onboarding
     * @return access_token An OAuth2.0 access token as a String.
     */
    public String generateOAuthAccessToken(String clientID, String clientSecret) {
        String credentials = clientID + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Authorization", "Basic " + encodedCredentials);

        String url = authUrl + "/oauth/token";

        HttpResponse<OAuthToken> response = apiClient.post(url, body, headers,
            new JsonBodyHandler<>(OAuthToken.class));

        if (response.statusCode() != 200) {
            throw new GarminPayApiException("Failed to get OAuth token, status code: " + response.statusCode());
        }

        return response.body().getAccessToken();
    }
}
