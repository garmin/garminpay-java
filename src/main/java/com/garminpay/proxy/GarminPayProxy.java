package com.garminpay.proxy;

import com.garminpay.APIClient;
import com.garminpay.exception.GarminPayEncryptionException;
import com.garminpay.model.HealthResponse;
import com.garminpay.model.request.CreateECCEncryptionKeyRequest;
import com.garminpay.model.response.ECCEncryptionKey;
import com.garminpay.util.JsonBodyHandler;
import com.garminpay.exception.GarminPayApiException;
import com.garminpay.model.response.OAuthToken;

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
        HttpResponse<HealthResponse> response = apiClient.get(
            baseApiUrl + "/health", new JsonBodyHandler<>(HealthResponse.class)
        );
        if (response.statusCode() != 200) {
            throw new GarminPayApiException(
                "Failed to get URL: " + baseApiUrl + "/health, status code: " + response.statusCode()
            );
        }
        return response.body();
    }

    /**
     * Generates an OAuth2.0 access token using client credentials.
     *
     * @param clientId     The clientId securely provided during onboarding
     * @param clientSecret The client secret securely provided during onboarding
     * @return access_token An OAuth2.0 access token as a String.
     */
    public String generateOAuthAccessToken(String clientId, String clientSecret) {
        String credentials = clientId + ":" + clientSecret;
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

    /**
     * Generates a client key pair and registers it with the Garmin Pay platform.
     * Creates a shared secret or "key agreement" between the client and server keys to be used for encryption.
     *
     * @param oAuthToken An OAuth2.0 access token as a String.
     * @param publicKey A public ecc key encoded and represented as a string.
     * @return KeyExchangeDTO that contains keyId, active status and SecretKey for encryption
     */
    public ECCEncryptionKey exchangeKeys(String oAuthToken, String publicKey) {
        String url = baseApiUrl + "/config/encryptionKeys";

        CreateECCEncryptionKeyRequest keyEncryptionRequest = CreateECCEncryptionKeyRequest.builder()
            .clientPublicKey(publicKey)
            .build();

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + oAuthToken);

        HttpResponse<ECCEncryptionKey> response = apiClient.post(url, keyEncryptionRequest, headers,
            new JsonBodyHandler<>(ECCEncryptionKey.class));

        if (response == null) {
            throw new GarminPayEncryptionException("Failed to complete key exchange, response is null");
        }

        return response.body();
    }
}
