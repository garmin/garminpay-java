package com.garminpay.proxy;

import com.garminpay.APIClient;
import com.garminpay.exception.GarminPayApiException;
import com.garminpay.model.response.HealthResponse;
import com.garminpay.model.request.CreateECCEncryptionKeyRequest;
import com.garminpay.model.response.ECCEncryptionKeyResponse;
import com.garminpay.model.response.RootResponse;
import com.garminpay.util.JsonBodyHandler;
import com.garminpay.model.response.OAuthTokenResponse;

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
     * @return RootResponse containing the root endpoint details.
     * @throws GarminPayApiException if the API response indicates a failure (status code < 200 or >= 300).
     */
    public RootResponse getRootEndpoint() {
        HttpResponse<RootResponse> response = apiClient.get(
            baseApiUrl + "/", new JsonBodyHandler<>(RootResponse.class)
        );

        RootResponse rootResponse = response.body();
        if (rootResponse == null) {
            throw new GarminPayApiException("/", response.statusCode(),
                null, null, null, null, null, "Failed to retrieve root endpoint, response is null");
        } else if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new GarminPayApiException(
                rootResponse.getPath(),
                response.statusCode(),
                null,
                null,
                null,
                null,
                rootResponse.getRequestId(),
                rootResponse.getMessage()
            );
        }
        return rootResponse;
    }

    /**
     * Retrieves the health status of the Garmin Pay API.
     *
     * @return HealthResponse containing the health status of the Garmin Pay API.
     * @throws GarminPayApiException if the API response indicates a failure (status code < 200 or >= 300).
     */
    public HealthResponse getHealthStatus() {
        HttpResponse<HealthResponse> response = apiClient.get(
            baseApiUrl + "/health", new JsonBodyHandler<>(HealthResponse.class)
        );

        HealthResponse healthResponse = response.body();
        if (healthResponse == null) {
            throw new GarminPayApiException("/health", response.statusCode(),
                null, null, null, null, null, "Failed to retrieve health status, response is null");
        } else if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new GarminPayApiException(
                healthResponse.getPath(),
                response.statusCode(),
                null,
                null,
                null,
                null,
                healthResponse.getRequestId(),
                healthResponse.getMessage()
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
     * @throws GarminPayApiException if the API response indicates a failure (status code < 200 or >= 300).
     */
    public String getOAuthAccessToken(String clientId, String clientSecret) {
        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Authorization", "Basic " + encodedCredentials);

        String url = authUrl + "/oauth/token";

        HttpResponse<OAuthTokenResponse> response = apiClient.post(url, body, headers,
            new JsonBodyHandler<>(OAuthTokenResponse.class));

        OAuthTokenResponse oAuthTokenResponse = response.body();
        if (oAuthTokenResponse == null) {
            throw new GarminPayApiException("/oauth/token", response.statusCode(),
                null, null, null, null, null, "Failed to get OAuth access token, response is null");
        } else if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new GarminPayApiException(
                oAuthTokenResponse.getPath(),
                response.statusCode(),
                null,
                null,
                null,
                null,
                oAuthTokenResponse.getRequestId(),
                oAuthTokenResponse.getMessage()
            );
        }

        return response.body().getAccessToken();
    }

    /**
     * Generates a client key pair and registers it with the Garmin Pay platform.
     * Creates a shared secret or "key agreement" between the client and server keys to be used for encryption.
     *
     * @param oAuthToken An OAuth2.0 access token as a String.
     * @param publicKey  A public ecc key encoded and represented as a string.
     * @return ECCEncryptionKeyResponse that contains keyId, active status and SecretKey for encryption
     * @throws GarminPayApiException if the API response indicates a failure (status code < 200 or >= 300).
     */
    public ECCEncryptionKeyResponse exchangeKeys(String oAuthToken, String publicKey) {
        String url = baseApiUrl + "/config/encryptionKeys";

        CreateECCEncryptionKeyRequest keyEncryptionRequest = CreateECCEncryptionKeyRequest.builder()
            .clientPublicKey(publicKey)
            .build();

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + oAuthToken);

        HttpResponse<ECCEncryptionKeyResponse> response = apiClient.post(url, keyEncryptionRequest, headers,
            new JsonBodyHandler<>(ECCEncryptionKeyResponse.class));

        ECCEncryptionKeyResponse eccEncryptionKeyResponse = response.body();
        if (eccEncryptionKeyResponse == null) {
            throw new GarminPayApiException("/config/encryptionKeys", response.statusCode(),
                null, null, null, null, null, "Failed to exchange keys, response is null");
        } else if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new GarminPayApiException(
                eccEncryptionKeyResponse.getPath(),
                response.statusCode(),
                null,
                null,
                null,
                null,
                eccEncryptionKeyResponse.getRequestId(),
                eccEncryptionKeyResponse.getMessage()
            );
        }

        return response.body();
    }
}
