package com.garminpay.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garminpay.client.Client;
import com.garminpay.exception.GarminPayApiException;
import com.garminpay.exception.GarminPaySDKException;
import com.garminpay.model.dto.APIResponseDTO;
import com.garminpay.model.request.CreateECCEncryptionKeyRequest;
import com.garminpay.model.request.CreatePaymentCardRequest;
import com.garminpay.model.response.ErrorResponse;
import com.garminpay.model.response.ExchangeKeysResponse;
import com.garminpay.model.response.HalLink;
import com.garminpay.model.response.HealthResponse;
import com.garminpay.model.response.RegisterCardResponse;
import com.garminpay.model.response.RootResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

/**
 * GarminPayProxy class responsible for calling the Garmin Pay API.
 */
@Slf4j
public class GarminPayProxy {
    private static final String[] EXPECTED_LINK_RELS = new String[]{"self", "health", "encryptionKeys", "paymentCards"};
    private final Client client;
    private final ObjectMapper objectMapper;
    private Map<String, HalLink> links = new HashMap<>();


    /**
     * GarminPayProxy constructor.
     *
     * @param client  Client to be used for executing requests
     * @param baseUrl URL to use for base Garmin Pay endpoints
     */
    public GarminPayProxy(Client client, String baseUrl) {
        this.client = client;
        this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Get links from Garmin Pay root endpoint
        this.links.put("self", HalLink.builder().href(baseUrl).build());
        this.refreshRootLinks();
    }

    /**
     * Retrieves the root endpoint of the Garmin Pay API.
     *
     * @return RootResponse containing the root endpoint details.
     * @throws GarminPayApiException if the API response indicates a failure (status code < 200 or >= 300).
     */
    public RootResponse getRootEndpoint() {
        log.debug("Retrieving root endpoint");
        ClassicHttpRequest request = ClassicRequestBuilder
            .get(links.get("self").getHref())
            .build();
        APIResponseDTO response = client.executeRequest(request);

        return parseResponse(response, RootResponse.class);
    }

    /**
     * Retrieves the health status of the Garmin Pay API.
     *
     * @return HealthResponse containing the health status of the Garmin Pay API.
     * @throws GarminPayApiException if the API response indicates a failure (status code < 200 or >= 300).
     */
    public HealthResponse getHealthStatus() {
        log.debug("Retrieving health status");
        ClassicHttpRequest request = ClassicRequestBuilder
            .get(links.get("health").getHref())
            .build();
        APIResponseDTO response = client.executeRequest(request);

        return parseResponse(response, HealthResponse.class);
    }

    /**
     * Generates a client key pair and registers it with the Garmin Pay platform.
     * Creates a shared secret or "key agreement" between the client and server keys to be used for encryption.
     *
     * @param publicKey A public ecc key encoded and represented as a string.
     * @return ExchangeKeysResponse that contains keyId, active status and SecretKey for encryption
     * @throws GarminPayApiException if the API response indicates a failure (status code < 200 or >= 300).
     */
    public ExchangeKeysResponse exchangeKeys(String publicKey) {
        log.debug("Exchanging keys");
        CreateECCEncryptionKeyRequest requestModel = CreateECCEncryptionKeyRequest.builder()
            .clientPublicKey(publicKey)
            .build();

        ClassicHttpRequest request = ClassicRequestBuilder
            .post(links.get("encryptionKeys").getHref())
            .setEntity(createRequestEntity(requestModel))
            .setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
            .build();

        APIResponseDTO response = client.executeRequest(request);

        return parseResponse(response, ExchangeKeysResponse.class);
    }

    /**
     * Makes secure https request to Garmin Pay platform to register customer’s CardData.
     *
     * @param encryptedCardData Serialized and encrypted GarminPayCardDataObject
     * @return sessionResponse reference data for the ephemeral session
     */
    public RegisterCardResponse registerCard(String encryptedCardData) {
        log.debug("Registering card");
        CreatePaymentCardRequest requestModel = CreatePaymentCardRequest.builder()
            .encryptedData(encryptedCardData)
            .build();

        ClassicHttpRequest request = ClassicRequestBuilder
            .post(links.get("paymentCards").getHref())
            .setEntity(createRequestEntity(requestModel))
            .setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
            .build();

        APIResponseDTO response = client.executeRequest(request);

        return parseResponse(response, RegisterCardResponse.class);
    }

    private <T> StringEntity createRequestEntity(T requestModel) {
        log.debug("Creating request entity");
        String serializedRequestBody;
        try {
            serializedRequestBody = objectMapper.writeValueAsString(requestModel);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize request body");
            throw new GarminPaySDKException(
                "Failed to serialize " + requestModel.getClass().getSimpleName() + " when trying to send data to GarminPay."
            );
        }

        return new StringEntity(serializedRequestBody, ContentType.APPLICATION_JSON);
    }

    private <T> T parseResponse(APIResponseDTO responseDTO, Class<T> responseClass) {
        log.debug("Parsing response from Client to class {}", responseClass.getName());
        // If status is in [200, 300) range, parse the desired response class
        if (responseDTO.getStatus() >= 200 && responseDTO.getStatus() < 300) {
            try {
                return objectMapper.readValue(responseDTO.getContent(), responseClass);
            } catch (JsonProcessingException e) {
                log.warn("Found an acceptable response status code but encountered unknown response body."
                        + " status: {}, x-request-id: {}, CF-RAY: {}",
                    responseDTO.getStatus(), responseDTO.findXRequestId().orElse(""), responseDTO.findCFRay().orElse("null")
                );
                throw new GarminPaySDKException("Failed to parse response entity.");
            }
        }

        try {
            log.warn("Response from Client contained an invalid status code. status: {}, x-request-id: {}, CF-RAY: {}",
                responseDTO.getStatus(), responseDTO.findXRequestId().orElse(""), responseDTO.findCFRay().orElse("null")
            );
            ErrorResponse errorResponse = objectMapper.readValue(responseDTO.getContent(), ErrorResponse.class);
            throw new GarminPayApiException(errorResponse, responseDTO.findCFRay().orElse(null));
        } catch (JsonProcessingException e) {
            log.warn("Unable to parse error response", e);
            ErrorResponse errorResponse = ErrorResponse.builder()
                .status(responseDTO.getStatus())
                .path(responseDTO.getPath())
                .build();

            throw new GarminPayApiException(errorResponse, responseDTO.findCFRay().orElse(null));
        }
    }

    /**
     * Refreshes the links to be used by proxy methods.
     */
    public void refreshRootLinks() {
        log.debug("Refreshing root links");
        Map<String, HalLink> responseLinks = getRootEndpoint().getLinks();

        boolean allExist = Arrays.stream(EXPECTED_LINK_RELS).allMatch(responseLinks::containsKey);

        if (!allExist) {
            log.warn("Required links for GarminPay were not found");
            throw new GarminPayApiException("Missing required links for GarminPay, please contact the GarminPay team.");
        } else {
            this.links = new HashMap<>(responseLinks);
        }
    }
}
