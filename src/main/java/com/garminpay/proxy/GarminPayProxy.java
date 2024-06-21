package com.garminpay.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garminpay.APIClient;
import com.garminpay.exception.GarminPayApiException;
import com.garminpay.exception.GarminPaySDKException;
import com.garminpay.model.dto.APIResponseDTO;
import com.garminpay.model.request.OAuthTokenRequest;
import com.garminpay.model.response.ErrorResponse;
import com.garminpay.model.response.ExchangeKeysResponse;
import com.garminpay.model.response.HealthResponse;

import com.garminpay.model.request.CreateECCEncryptionKeyRequest;
import com.garminpay.model.response.RootResponse;
import com.garminpay.model.request.CreatePaymentCardRequest;
import com.garminpay.model.response.RegisterCardResponse;
import com.garminpay.model.response.OAuthTokenResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicHeader;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * GarminPayProxy class responsible for calling the Garmin Pay API.
 */
public class GarminPayProxy {
    private final String baseUrl;
    private final String authUrl;

    private final APIClient apiClient;
    private final ObjectMapper objectMapper;


    /**
     * GarminPayProxy constructor.
     *
     * @param baseUrl URL to use for base Garmin Pay endpoints
     * @param authUrl URL to use for authentication Garmin Pay endpoints
     */
    public GarminPayProxy(String baseUrl, String authUrl) {
        this.baseUrl = baseUrl;
        this.authUrl = authUrl;

        this.apiClient = APIClient.getInstance();
        this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Retrieves the root endpoint of the Garmin Pay API.
     *
     * @return RootResponse containing the root endpoint details.
     * @throws GarminPayApiException if the API response indicates a failure (status code < 200 or >= 300).
     */
    public RootResponse getRootEndpoint() {
        ClassicHttpRequest request = ClassicRequestBuilder.get(baseUrl).build();
        APIResponseDTO response = apiClient.send(request);

        return parseResponse(response, RootResponse.class);
    }

    /**
     * Retrieves the health status of the Garmin Pay API.
     *
     * @return HealthResponse containing the health status of the Garmin Pay API.
     * @throws GarminPayApiException if the API response indicates a failure (status code < 200 or >= 300).
     */
    public HealthResponse getHealthStatus() {
        ClassicHttpRequest request = ClassicRequestBuilder.get(baseUrl + "/health").build();
        APIResponseDTO response = apiClient.send(request);

        return parseResponse(response, HealthResponse.class);
    }

    /**
     * Generates an OAuth2.0 access token using client credentials.
     *
     * @param clientId     The clientId securely provided during onboarding
     * @param clientSecret The client secret securely provided during onboarding
     * @return OAuthTokenResponse An OAuth2.0 access object containing a string with the token
     * @throws GarminPayApiException if the API response indicates a failure (status code < 200 or >= 300).
     */
    public OAuthTokenResponse getOAuthAccessToken(String clientId, String clientSecret) {
        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        OAuthTokenRequest requestModel = OAuthTokenRequest.builder()
            .grantType("client_credentials")
            .build();

        Header authHeader = new BasicHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials, true);

        ClassicHttpRequest request = ClassicRequestBuilder.post(authUrl + "/oauth/token")
            .setEntity(createRequestEntity(requestModel))
            .setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString())
            .addHeader(authHeader)
            .build();

        APIResponseDTO response = apiClient.send(request);

        return parseResponse(response, OAuthTokenResponse.class);
    }

    /**
     * Generates a client key pair and registers it with the Garmin Pay platform.
     * Creates a shared secret or "key agreement" between the client and server keys to be used for encryption.
     *
     * @param oAuthToken An OAuth2.0 access token as a String.
     * @param publicKey  A public ecc key encoded and represented as a string.
     * @return ExchangeKeysResponse that contains keyId, active status and SecretKey for encryption
     * @throws GarminPayApiException if the API response indicates a failure (status code < 200 or >= 300).
     */
    public ExchangeKeysResponse exchangeKeys(String oAuthToken, String publicKey) {
        CreateECCEncryptionKeyRequest requestModel = CreateECCEncryptionKeyRequest.builder()
            .clientPublicKey(publicKey)
            .build();

        Header tokenHeader = new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + oAuthToken, true);

        ClassicHttpRequest request = ClassicRequestBuilder.post(baseUrl + "/config/encryptionKeys")
            .setEntity(createRequestEntity(requestModel))
            .setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
            .addHeader(tokenHeader)
            .build();

        APIResponseDTO response = apiClient.send(request);

        return parseResponse(response, ExchangeKeysResponse.class);
    }

    /**
     * Makes secure https request to Garmin Pay platform to register customer’s CardData.
     *
     * @param oAuthToken        The OAuthToken to use in the request
     * @param encryptedCardData Serialized and encrypted GarminPayCardDataObject
     * @return sessionResponse reference data for the ephemeral session
     */
    public RegisterCardResponse registerCard(String oAuthToken, String encryptedCardData) {
        CreatePaymentCardRequest requestModel = CreatePaymentCardRequest.builder()
            .encryptedData(encryptedCardData)
            .build();

        Header tokenHeader = new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + oAuthToken, true);

        ClassicHttpRequest request = ClassicRequestBuilder.post(baseUrl + "/paymentCards")
            .setEntity(createRequestEntity(requestModel))
            .setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
            .addHeader(tokenHeader)
            .build();

        APIResponseDTO response = apiClient.send(request);

        return parseResponse(response, RegisterCardResponse.class);
    }

    private <T> StringEntity createRequestEntity(T requestModel) {
        String serializedRequestBody;
        try {
            serializedRequestBody = objectMapper.writeValueAsString(requestModel);
        } catch (JsonProcessingException e) {
            throw new GarminPaySDKException("Failed to serialize " + requestModel.getClass().getSimpleName() + " when trying to send data to GarminPay.");
        }

        return new StringEntity(serializedRequestBody, ContentType.APPLICATION_JSON);
    }

    private <T> T parseResponse(APIResponseDTO responseDTO, Class<T> responseClass) {
        // If status is in [200, 300) range, parse the desired response class
        if (responseDTO.getStatus() >= 200 && responseDTO.getStatus() < 300) {
            try {
                return objectMapper.readValue(responseDTO.getContent(), responseClass);
            } catch (JsonProcessingException e) {
                throw new GarminPaySDKException("Failed to parse response entity.");
            }
        }

        try {
            ErrorResponse errorResponse = objectMapper.readValue(responseDTO.getContent(), ErrorResponse.class);

            throw new GarminPayApiException(errorResponse, responseDTO.findCFRay().orElse(null));
        } catch (JsonProcessingException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                .status(responseDTO.getStatus())
                .path(responseDTO.getPath())
                .build();

            throw new GarminPayApiException(errorResponse, responseDTO.findCFRay().orElse(null));
        }
    }
}
