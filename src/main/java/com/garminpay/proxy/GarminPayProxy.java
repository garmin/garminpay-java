package com.garminpay.proxy;

import com.garminpay.APIClient;
import com.garminpay.exception.GarminPayApiException;
import com.garminpay.model.request.OAuthTokenRequest;
import com.garminpay.model.response.HealthResponse;

import com.garminpay.model.request.CreateECCEncryptionKeyRequest;
import com.garminpay.model.response.ECCEncryptionKeyResponse;
import com.garminpay.model.response.RootResponse;
import com.garminpay.model.request.CreatePaymentCardRequest;
import com.garminpay.model.response.PaymentCardDeepLinkResponse;
import com.garminpay.model.response.OAuthTokenResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.message.BasicHeader;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
        RootResponse response = apiClient.get(baseApiUrl + "/", RootResponse.class);

        if (response == null) {
            throw new GarminPayApiException("/", null,
                null, null, null, null, null, "Failed to retrieve root endpoint, response is null");
        } else if (response.getStatus() < 200 || response.getStatus() >= 300) {
            throw new GarminPayApiException(
                response.getPath(),
                response.getStatus(),
                null,
                null,
                null,
                null,
                response.getRequestId(),
                response.getMessage()
            );
        }

        return response;
    }

    /**
     * Retrieves the health status of the Garmin Pay API.
     *
     * @return HealthResponse containing the health status of the Garmin Pay API.
     * @throws GarminPayApiException if the API response indicates a failure (status code < 200 or >= 300).
     */
    public HealthResponse getHealthStatus() {
        HealthResponse response = apiClient.get(baseApiUrl + "/health", HealthResponse.class);

        if (response == null) {
            throw new GarminPayApiException("/health", null,
                null, null, null, null, null, "Failed to retrieve health status, response is null");
        } else if (response.getStatus() < 200 || response.getStatus() >= 300) {
            throw new GarminPayApiException(
                response.getPath(),
                response.getStatus(),
                null,
                null,
                null,
                null,
                response.getRequestId(),
                response.getMessage()
            );
        }
        return response;
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

        String url = authUrl + "/oauth/token";

        OAuthTokenRequest request = OAuthTokenRequest.builder()
            .grantType("client_credentials")
            .build();

        OAuthTokenResponse response = apiClient.post(
            url,
            OAuthTokenResponse.class,
            request,
            new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded"),
            new BasicHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
        );

        if (response == null) {
            throw new GarminPayApiException("/oauth/token", null,
                null, null, null, null, null, "Failed to get OAuth access token, response is null");
        } else if (response.getStatus() < 200 || response.getStatus() >= 300) {
            throw new GarminPayApiException(
                response.getPath(),
                response.getStatus(),
                null,
                null,
                null,
                null,
                response.getRequestId(),
                response.getMessage()
            );
        }

        return response.getAccessToken();
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

        CreateECCEncryptionKeyRequest request = CreateECCEncryptionKeyRequest.builder()
            .clientPublicKey(publicKey)
            .build();

        ECCEncryptionKeyResponse response = apiClient.post(
            url,
            ECCEncryptionKeyResponse.class,
            request,
            new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded"),
            new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + oAuthToken)
        );

        if (response == null) {
            throw new GarminPayApiException("/config/encryptionKeys", null,
                null, null, null, null, null, "Failed to exchange keys, response is null");
        } else if (response.getStatus() < 200 || response.getStatus() >= 300) {
            throw new GarminPayApiException(
                response.getPath(),
                response.getStatus(),
                null,
                null,
                null,
                null,
                response.getRequestId(),
                response.getMessage()
            );
        }

        return response;
    }

    /**
     * Makes secure https request to Garmin Pay platform to register customer’s CardData.
     *
     * @param oAuthToken        The OAuthToken to use in the request
     * @param encryptedCardData Serialized and encrypted GarminPayCardDataObject
     * @return sessionResponse reference data for the ephemeral session
     */
    public PaymentCardDeepLinkResponse registerCard(String oAuthToken, String encryptedCardData) {
        String url = baseApiUrl + "/paymentCards";

        CreatePaymentCardRequest request = CreatePaymentCardRequest.builder()
            .encryptedData(encryptedCardData)
            .build();

        PaymentCardDeepLinkResponse response = apiClient.post(
            url,
            PaymentCardDeepLinkResponse.class,
            request,
            new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded"),
            new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + oAuthToken)
        );

        if (response == null) {
            throw new GarminPayApiException("/paymentCards", null,
                null, null, null, null, null, "Failed to register card, response is null"
            );
        } else if (response.getStatus() < 200 || response.getStatus() >= 300) {
            throw new GarminPayApiException(
                response.getPath(),
                response.getStatus(),
                null,
                null,
                null,
                null,
                response.getRequestId(),
                response.getMessage()
            );
        }

        return response;
    }
}
